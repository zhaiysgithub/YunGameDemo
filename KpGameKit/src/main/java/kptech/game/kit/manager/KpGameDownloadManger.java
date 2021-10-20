package kptech.game.kit.manager;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.kptech.kputils.download.DownloadExtCallback;
import com.kptech.kputils.download.DownloadManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import kptech.game.kit.GameInfo;
import kptech.game.kit.dialog.PlayWhenDownDialog;
import kptech.game.kit.download.DownloadService;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.NetUtils;
import kptech.game.kit.utils.StringUtil;
import kptech.lib.analytic.Event;
import kptech.lib.analytic.EventCode;
import kptech.lib.analytic.MobclickAgent;

public class KpGameDownloadManger {

    private static final String TAG = "KpGameDownloadManger";
    public static final int STATE_NONE = -1;
    public static final int STATE_WAITING = 0;
    public static final int STATE_STARTED = 1;
    public static final int STATE_FINISHED = 2;
    public static final int STATE_STOPPED = 3;
    public static final int STATE_ERROR = 4;

    public static long BUF_SIZE_H = 2 * 1024 * 1024;
    public static long BUF_SIZE_M = 1024 * 1024;
    public static long BUF_SIZE_L = 512 * 1024;

    private final DownloadManager mDownloadInstance;
    private GameInfo mGameInfo;
    private Intent downloadIntent;
    //是否限速 默认不限速
    private boolean speedLimitEnable = false;
    //边玩边下弹窗
    private PlayWhenDownDialog mPlayWhenDownDialog;


    private KpGameDownloadManger() {
        mDownloadInstance = DownloadManager.getInstance();
    }

    private static class DownloadHolder {
        private static final KpGameDownloadManger INSTANCE = new KpGameDownloadManger();
    }

    public static KpGameDownloadManger instance() {
        return DownloadHolder.INSTANCE;
    }

    public void setGameInfo(GameInfo gameInfo) {
        this.mGameInfo = gameInfo;
    }

    /**
     * 设置限速值
     *
     * @param dataByteBuf 每秒接收的数据量  单位byte
     */
    public void setSpeedPerSecond(long dataByteBuf) {
        if (mDownloadInstance != null) {
            //大于等于2kb 小于 10M
            if (dataByteBuf >= 2048 && dataByteBuf < 10240 * 1024) {
                mDownloadInstance.updateSpeedPerSecond(speedLimitEnable,dataByteBuf);
            } else {
                mDownloadInstance.updateSpeedPerSecond(speedLimitEnable,2048);
            }
        }
    }

    /**
     * 设置限速值
     */
    public static void setSpeedLimitValue(long limitH,long limitM, long limitL ){
        BUF_SIZE_H = limitH;
        BUF_SIZE_M = limitM;
        BUF_SIZE_L = limitL;

        DownloadManager.SPEED_PER_SECOND_H = limitH;
        DownloadManager.SPEED_PER_SECOND_M = limitM;
        DownloadManager.SPEED_PER_SECOND_L = limitL;
    }

    /**
     * 开始下载
     * <p>
     * 开始前需要权限检测
     */
    public void initDownload(Context context) {
        if (mGameInfo == null || mDownloadInstance == null || context == null) {
            return;
        }
        if (mGameInfo.enableDownload != 1 || StringUtil.isEmpty(mGameInfo.downloadUrl)) {
            return;
        }
        try{
            int state = getDownloadState(mGameInfo.downloadUrl);
            if (state == STATE_FINISHED) {
                boolean isWifi = NetUtils.isWiFi(context);
                showPlayWhenDownDialog(context,isWifi);
            } else if (state == STATE_STARTED) {//有任务正在下载
                //下载完成一个才能执行另一个下载
                Toast.makeText(context, "游戏正在下载中...", Toast.LENGTH_SHORT).show();
            } else {
                mDownloadInstance.setOnDownloadListener(mDownloadListener);
                String downloadType = mGameInfo.downloadType;
                if (!GameInfo.GAME_DOWNLOADTYPE_SILENT.equals(downloadType)) {
                    //开服务通知栏下载
                    downloadIntent = new Intent(context, DownloadService.class);
                    downloadIntent.putExtra(DownloadService.EXTRA_GAME, mGameInfo);
                    //android8.0以上通过startForegroundService启动service
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(downloadIntent);
                    } else {
                        context.startService(downloadIntent);
                    }
                } else {
                    //继续静默下载
                    continueDownload(context,mGameInfo);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 继续执行下载
     */
    public void continueDownload(Context context,GameInfo info) {
        if (mDownloadInstance == null || info == null) {
            return;
        }
        try {
            String url = info.downloadUrl;
            String originPkgName = info.pkgName;
            String savedPath = getSavedPath(context,originPkgName);
            mDownloadInstance.startDownload(url, originPkgName, savedPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取文件下载路径
     */
    public String getSavedPath(Context context,String originPkgName) {
//        String dirPath = Environment.getExternalStorageDirectory().getPath();
//        return dirPath + "/download/" + originPkgName + ".apk";
        File downloadFile = context.getExternalFilesDir("download");
        if (!downloadFile.exists()){
            downloadFile.mkdir();
        }
        File apkFile = new File(downloadFile,originPkgName + ".apk");
        Logger.info(TAG,"apkFile:" + apkFile.getAbsolutePath());
        return apkFile.getAbsolutePath();
    }

    /**
     * 暂停下载
     *
     * @param url 下载地址
     */
    public void stopDownload(String url) {
        if (mDownloadInstance != null && url != null && !url.isEmpty()) {
            mDownloadInstance.stopDownload(url);
        }
    }

    /**
     * 销毁服务
     */
    public void destroyService(Context context, String url) {
        stopDownload(url);
        if (context != null && downloadIntent != null) {
            context.stopService(downloadIntent);
        }
    }

    /**
     * 获取当前文件的下载状态
     *
     * @param url 文件下载地址
     */
    public int getDownloadState(String url) {
        if (mDownloadInstance == null || url == null || url.isEmpty()) {
            return STATE_NONE;
        }
        int downloadState = mDownloadInstance.getDownloadInfoState(url);

        return (downloadState == -1) ? STATE_WAITING : downloadState;
    }

    /**
     * 获取当前已经下载的进度
     */
    public int getDownloadProgress(String url){
        if (mDownloadInstance == null || url == null || url.isEmpty()) {
            return 0;
        }
        int porgress = mDownloadInstance.getDownloadProgress(url);
        if (porgress > 0 && porgress <= 100){
            return porgress;
        }else {
            return 0;
        }
    }

    public void notifyDownloadState(int status){
        try{
            if (mPlayWhenDownDialog != null && mPlayWhenDownDialog.isShowing()){
                mPlayWhenDownDialog.updateDownStatus(status);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 启动对应的游戏
     */
    public static boolean openPackage(Context context, String pkgName) {
        try {
            Context pkgContext = getPackageContext(context, pkgName);
            Intent intent = getAppOpenIntentByPackageName(context, pkgName);
            if (pkgContext != null && intent != null) {
                intent.putExtra("openMoudle", "serviceHall");
                pkgContext.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Context getPackageContext(Context context, String packageName) {
        Context pkgContext = null;
        if (context.getPackageName().equals(packageName)) {
            pkgContext = context;
        } else {
            try {
                pkgContext = context.createPackageContext(packageName,
                        Context.CONTEXT_IGNORE_SECURITY
                                | Context.CONTEXT_INCLUDE_CODE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return pkgContext;
    }

    /**
     * 检查对应package的intent
     */
    public static Intent getAppOpenIntentByPackageName(Context context, String packageName) {
        //Activity完整名
        String mainAct = null;
        //根据包名寻找
        PackageManager pkgMag = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

        @SuppressLint("WrongConstant")
        List<ResolveInfo> list = pkgMag.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            if (info.activityInfo.packageName.equals(packageName)) {
                mainAct = info.activityInfo.name;
                break;
            }
        }
        if (mainAct == null || "".equals(mainAct)) {
            return null;
        }
        intent.setComponent(new ComponentName(packageName, mainAct));
        return intent;
    }

    /**
     * 执行安装流程
     */
    public void doInstallApk(Context context, String pkgName) {
        String savedPath = getSavedPath(context,pkgName);
        File apkFile = new File(savedPath);
        if (!apkFile.exists()) {
            return;
        }
        startInstallApk(context, apkFile);
    }

    /**
     * 开始安装apk
     */
    private void startInstallApk(Context context, File apkFile) {

        try{
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                String authority = context.getPackageName() + ".fileProvider";
                Uri apkUri = FileProvider.getUriForFile(context, authority, apkFile);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                //如果没有设置SDCard写权限，或者没有sdcard,apk文件保存在内存中，需要授予权限才能安装
                try {
                    String[] command = {"chmod", "777", apkFile.toString()};
                    ProcessBuilder builder = new ProcessBuilder(command);
                    builder.start();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            }
            context.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void delErrorFile(String downloadUrl) {
        if (mDownloadInstance == null || downloadUrl == null || downloadUrl.isEmpty()) {
            return;
        }
        mDownloadInstance.removeDownload(downloadUrl);
    }


    public boolean isSpeedLimitEnable() {
        return speedLimitEnable;
    }

    public void setSpeedLimitEnable(boolean speedLimitEnable) {
        this.speedLimitEnable = speedLimitEnable;
    }

    /**
     * 显示边玩边下弹窗
     */
    public void showPlayWhenDownDialog(Context context, boolean isWifi){
        mPlayWhenDownDialog = new PlayWhenDownDialog(context);
        mPlayWhenDownDialog.setGameConfig(mGameInfo,isWifi);
        mPlayWhenDownDialog.show();
    }

    /**
     * 隐藏边玩边下弹窗
     */
    public void dismissPlayDownDialog() {
        if (mPlayWhenDownDialog != null && mPlayWhenDownDialog.isShowing()){
            mPlayWhenDownDialog.dismiss();
        }
    }

    private final DownloadExtCallback mDownloadListener = new DownloadExtCallback() {
        @Override
        public void onStarted(String url) {
            //发送开始下载的打点数据
            Event event = Event.getEvent(EventCode.DATA_ACTIVITY_RECEIVE_DOWNLOADSTART, mGameInfo != null ? mGameInfo.pkgName : "");
            MobclickAgent.sendEvent(event);
            KpGameManager.instance().sendDownloadStatus(STATE_STARTED, url);
        }

        @Override
        public void onPaused(String url) {
            //下载暂停打点
            Event event = Event.getEvent(EventCode.DATA_ACTIVITY_RECEIVE_DOWNLOADSTOP, mGameInfo != null ? mGameInfo.pkgName : "");
            MobclickAgent.sendEvent(event);
            KpGameManager.instance().sendDownloadStatus(STATE_STOPPED, url);
        }

        @Override
        public void onProgress(long total, long current, String url) {
            KpGameManager.instance().sendDownloadProgress(total, current, url);
            if (mPlayWhenDownDialog != null){
                double precent = Double.parseDouble(current + "") / Double.parseDouble(total + "");
                int progress = (int) (precent * 100);
                mPlayWhenDownDialog.updateProgress(progress);
            }
        }

        @Override
        public void onSuccess(File result, String url) {
            //下载完成的打点
            Event event = Event.getEvent(EventCode.DATA_ACTIVITY_RECEIVE_DOWNLOADCOMPLETE, mGameInfo != null ? mGameInfo.pkgName : "");
            MobclickAgent.sendEvent(event);
            KpGameManager.instance().sendDownloadStatus(STATE_FINISHED, url);


        }

        @Override
        public void onError(String error, String url) {
            try{
                //下载失败的打点
                Event event = Event.getEvent(EventCode.DATA_ACTIVITY_RECEIVE_DOWNLOADERROR, mGameInfo != null ? mGameInfo.pkgName : "");
                event.setErrMsg(error);
                MobclickAgent.sendEvent(event);
                KpGameManager.instance().sendDownloadStatus(STATE_ERROR, url);
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        @Override
        public void onCancelled(String msg, String url) {
        }
    };
}
