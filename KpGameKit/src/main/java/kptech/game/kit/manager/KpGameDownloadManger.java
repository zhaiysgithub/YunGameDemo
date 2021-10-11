package kptech.game.kit.manager;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.kptech.kputils.download.DownloadExtCallback;
import com.kptech.kputils.download.DownloadManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import kptech.game.kit.GameInfo;
import kptech.game.kit.download.DownloadService;
import kptech.game.kit.utils.AppUtils;
import kptech.game.kit.utils.Logger;
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

    private final DownloadManager mDownloadInstance;
    private GameInfo mGameInfo;
    private boolean showInstallDialog = false;
    private Intent downloadIntent;

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
        //TODO 设置下载速度
    }

    /**
     * 是否显示安装的弹窗
     */
    public void setShowInstallDialog(boolean show){
        this.showInstallDialog = show;
    }

    /**
     * 开始下载
     * <p>
     * 开始前需要权限检测
     */
    public void initDownload(Context context) {
        if (mGameInfo == null || mDownloadInstance == null) {
            return;
        }
        if (mGameInfo.enableDownload != 1 || StringUtil.isEmpty(mGameInfo.downloadUrl)) {
            return;
        }
        String runningUrl = mDownloadInstance.taskRunningUrl();
        if (runningUrl != null && !runningUrl.isEmpty()) {
            if (runningUrl.equals(mGameInfo.downloadUrl)) {
                //暂停
                stopDownload(runningUrl);
            } else {
                //下载完成一个才能执行另一个下载
                Toast.makeText(context, "其他游戏在下载中，请稍后在试", Toast.LENGTH_SHORT).show();
                return;
            }
        }
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
            continueDownload(context, mGameInfo);
        }

    }

    /**
     * 继续执行下载
     */
    public void continueDownload(Context context, GameInfo info) {
        if (mDownloadInstance == null || info == null){
            return;
        }
        String url = info.downloadUrl;
        String originPkgName = info.pkgName;
        String savedPath = getSavedPath(originPkgName);
        File file = new File(savedPath);
        if (file.exists()) {
            //TODO 暂时无法判断文件对应版本号，通知执行系统安装程序
            doInstallApk(context,originPkgName);
            return;
        }
        try{
            mDownloadInstance.startDownload(url, originPkgName, savedPath);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取文件下载路径
     */
    public String getSavedPath(String originPkgName) {
        /*File dir = context.getExternalFilesDir("download");
        if (!dir.exists()){
            dir.mkdir();
        }
        String fileName = originPkgName + ".apk";
        File file = new File(dir,fileName);
        return file.getPath();*/

//        return "/sdcard/download/" + originPkgName + ".apk";
        String dirPath = Environment.getExternalStorageDirectory().getPath();
        String savedPath = dirPath + "/" + originPkgName + ".apk";
        Logger.info(TAG, "dirPath=" + dirPath + ";savedPath=" + savedPath);
        return savedPath;

    }

    /**
     * 暂停下载
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
    public void destroyService(Context context,String url){
        stopDownload(url);
        if (context != null && downloadIntent != null){
            context.stopService(downloadIntent);
        }
    }

    /**
     * 获取当前文件的下载状态
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
        //TODO 是否需要检测已经安装了， 是否需要弹出安装提示框
        String savedPath = getSavedPath(pkgName);
        File apkFile = new File(savedPath);
        if (!apkFile.exists()) {
            return;
        }
        //检测apk是否可用
        boolean able = AppUtils.getUninatllApkInfo(context, apkFile.getAbsolutePath());
        if (!able) {
            //TODO 文件出错,删除原文件，重新下载
            return;
        }
        if (showInstallDialog){
            //TODO 安装提示框

        }else {
            startInstallApk(context, apkFile);
        }
    }

    /**
     * 开始安装apk
     */
    private void startInstallApk(Context context,File apkFile){

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
            } catch (IOException ignored) {
            }
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }

    public void delErrorFile(String downloadUrl){
        if (mDownloadInstance == null ||downloadUrl == null || downloadUrl.isEmpty()){
            return;
        }
        mDownloadInstance.removeDownload(downloadUrl);
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
            //下载失败的打点
            Event event = Event.getEvent(EventCode.DATA_ACTIVITY_RECEIVE_DOWNLOADERROR, mGameInfo != null ? mGameInfo.pkgName : "");
            MobclickAgent.sendEvent(event);

            KpGameManager.instance().sendDownloadStatus(STATE_ERROR, url);
        }

        @Override
        public void onCancelled(String msg, String url) {
        }
    };
}
