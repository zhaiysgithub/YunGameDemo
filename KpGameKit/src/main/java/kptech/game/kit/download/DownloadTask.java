package kptech.game.kit.download;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import kptech.game.kit.GameInfo;
import kptech.game.kit.R;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.StringUtil;

/**
 * Created by yuandl on 2016-12-19.
 */

public class DownloadTask extends Service {
    public static final String EXTRA_GAME = "extra.game";
    private static final String TAG = "DownloadService";

    public static final int STATUS_STARTED = 1;
    public static final int STATUS_PAUSED = 2;
    public static final int STATUS_STOPED = 3;
    public static final int STATUS_FINISHED = 4;
    public static final int STATUS_CANCEL = 5;
    public static final int STATUS_WAITTING = 6;
    public static final int STATUS_ERROR = 7;

    /****
     * 发送广播的请求码
     */
    private final int REQUEST_CODE_BROADCAST = 0X0001;

//    private final int REQUEST_CODE_CLOSE = 0X0002;
    /****
     * 发送广播的action
     */
    public static final String BROADCAST_ACTION_CLICK = "servicetask";
    public static final String BROADCAST_ACTION_CLICKCLOSE = "kp_close";
    /**
     * 通知的Id
     */
    private static final int NOTIFICATION_ID = 1223;
    private static final String CHANNEL_ID = "kpCloudGameDownload";
    /**
     * 通知管理器
     */
    private NotificationManager mNotificationManager;
    /**
     * 通知
     */
    private Notification mNotification;
    /**
     * 通知栏的远程View
     */
    private RemoteViews mRemoteViews;

    private XutilDownload mXutilDownload;

    private String mFilePath;
    private String mFileName;
    private String mDownUrl;
//    private String mPkgName;
//    private String mGameName;
//    private String mGameIcon;

//    Notification notify;

    private GameInfo mGameInfo;

    /**
     * 通知栏操作的四种状态
     */
    public enum Status {
        NONE, DOWNLOADING, PAUSE, FAIL, SUCCESS, INSTALLED
    }

    /**
     * 当前在状态 默认未下载
     */
    private Status status = Status.NONE;
    private MyBroadcastReceiver myBroadcastReceiver;
    private NetworkReceiver mNetworkReceiver;
    private InstallApkReceiver mInstallReceive;

    private DataCallback mDataCallback;

    private RemoteViewGameIconTask mGameIconTask;

    public interface DataCallback{
        void onStart(int id);
        void onPause(int id);
        void onSuccess(String filePath, int id);
        void onFail(String err, int id);
        void onProgress(long total, long current, int id);
        void onInstallApkError(String filePaht, String msg);
        void onStopService();
    }

    private DownloadBinder mBinder = new DownloadBinder();
    public class DownloadBinder extends Binder {
        public DownloadTask getService(){
            return DownloadTask.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        //通知栏
        createNotification(this);

        //注册广播
        registerBroadCast();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("DownloadTask", "onStartCommand  ");
        if (intent == null){
            return super.onStartCommand(intent, flags, startId);
        }
        String action = intent.getStringExtra("action");
        Log.d("DownloadTask", "action: " + action);
        if ("stop".equals(action)){
            stopHttp();
        }else if ("start".equals(action)){
            //获取游戏信息
            GameInfo game = null;
            try {
                game = intent.getParcelableExtra("extra.game");
            }catch (Exception e){
                Logger.info(TAG, e.getMessage());
            }

            //下载中
            if (status == Status.DOWNLOADING){
                //判断是否是同一个游戏
                if (game!=null && game.downloadUrl != null && mDownUrl.equals(game.downloadUrl)) {
                    return super.onStartCommand(intent, flags, startId);
                }
            }

            //判断是否已安装
            if (status == Status.INSTALLED && mGameInfo!=null){
                //启动游戏
                openPackage(this, mGameInfo.pkgName);
                return super.onStartCommand(intent, flags, startId);
            }

            //判断是否下载完成
            if (status == Status.SUCCESS){
                //调用安装
                installAPk();
                return super.onStartCommand(intent, flags, startId);
            }

            //验证参数
            mGameInfo = game;
            if (mGameInfo == null || mGameInfo.downloadUrl == null){
                if (mDataCallback != null){
                    mDataCallback.onFail("未获取到下载地址", mGameInfo.gid);
                }
                return super.onStartCommand(intent, flags, startId);
            }

            //创建文件
            File dir = getExternalFilesDir("download");
            if (!dir.exists()){
                dir.mkdir();
            }
            String url = mGameInfo.downloadUrl;
//            String apkName = url.substring(url.lastIndexOf("/") + 1, url.length());
            String apkName = mGameInfo.pkgName + ".apk";
            File file = new File(dir,apkName);


            mFilePath = file.getPath();
            mFileName = apkName;
            mDownUrl = url;

            //加载游戏图标
            loadGameIcon(mGameInfo.iconUrl);

            //通知栏
            startForeground(NOTIFICATION_ID, mNotification);

            //开始下载
            startHttp();
        }

        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * 注册按钮点击广播*
     */
    private void registerBroadCast() {
        //通知栏点击事件
        myBroadcastReceiver = new MyBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_ACTION_CLICK);
        filter.addAction(BROADCAST_ACTION_CLICKCLOSE);
        registerReceiver(myBroadcastReceiver, filter);

        //监听网络变化
        mNetworkReceiver = new NetworkReceiver(this);
        IntentFilter netfilter = new IntentFilter();
        netfilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkReceiver, netfilter);

        //监听App安装
        mInstallReceive = new InstallApkReceiver(this);
        IntentFilter infilter = new IntentFilter();
        infilter.addAction("android.intent.action.PACKAGE_ADDED");
        infilter.addDataScheme("package");
        registerReceiver(mInstallReceive, infilter);
    }

    /**
     * 销毁时取消下载，并取消注册广播，防止内存溢出
     */
    @Override
    public void onDestroy() {
//        if (cancelable != null && !cancelable.isCancelled()) {
//            cancelable.cancel();
//        }
        destoryRemoteViewGameIconTask();
        if (myBroadcastReceiver != null) {
            unregisterReceiver(myBroadcastReceiver);
            myBroadcastReceiver = null;
        }
        if (mNetworkReceiver != null) {
            unregisterReceiver(mNetworkReceiver);
            mNetworkReceiver = null;
        }
        if (mInstallReceive != null) {
            unregisterReceiver(mInstallReceive);
            mInstallReceive = null;
        }
        super.onDestroy();
        stopForeground(true);
    }

    public Status getStatus(){
        return status;
    }

    public int getId(){
        if (mGameInfo!=null){
            return mGameInfo.gid;
        }
        return 0;
    }
    public boolean isDownloading(){
        return status == Status.DOWNLOADING;
    }

    public void setDataCallback(DataCallback callback){
        this.mDataCallback = callback;
    }

//    public void toggle(){
//        switch (status){
//            case SUCCESS:
//                //安装
//                installAPk();
//                break;
//            case DOWNLOADING:
//            case PAUSE:
//            case FAIL:
//                //下载或暂停
//                sendBroadcast(new Intent(DownloadTask.BROADCAST_ACTION_CLICK));
//                break;
//            case NONE:
//                break;
//            case INSTALLED:
//                //打开游戏
//                try {
//                    openPackage(this, mGameInfo.pkgName);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//                break;
//        }
//    }


    private synchronized void startHttp(){
        xUtilDownload();
    }

    private synchronized void stopHttp() {
        if (mXutilDownload != null){
            mXutilDownload.cancel();
        }
    }

    private synchronized void xUtilDownload(){
        if (mXutilDownload!=null){
            mXutilDownload.cancel();
            mXutilDownload = null;
        }

        mXutilDownload = new XutilDownload();
        mXutilDownload.setFilePath(mFilePath,mFileName);
        mXutilDownload.setUrl(mDownUrl);
        mXutilDownload.setHandler(mDownloadHandler);
        mXutilDownload.start();
    }


    Handler mDownloadHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {


            switch (msg.what){
                case HttpDownload.DOWNLOAD_MSG_START:
                    try{
                        String[] obj = (String[])msg.obj;
                        if (!obj[0].equals(mFileName)){
                            return;
                        }
                        downloadStart();
                    }catch (Exception e){}
                    break;
                case HttpDownload.DOWNLOAD_MSG_PUASE:
                    try{
                        String[] obj = (String[])msg.obj;
                        if (!obj[0].equals(mFileName)){
                            return;
                        }
                        downloadPause();
                    }catch (Exception e){}
                    break;
                case HttpDownload.DOWNLOAD_MSG_FAIL:
                    try{
                        String[] obj = (String[])msg.obj;
                        if (!obj[0].equals(mFileName)){
                            return;
                        }
                        downloadFail(obj[1]);
                    }catch (Exception e){}
                    break;
                case HttpDownload.DOWNLOAD_MSG_SUCCESS:
                    try{
                        String[] obj = (String[])msg.obj;
                        if (!obj[0].equals(mFileName)){
                            return;
                        }
                        downloadSuccess(obj[1]);
                    }catch (Exception e){}

                    break;
                case HttpDownload.DOWNLOAD_MSG_PROGRESS:
                    try{
                        long[] arr = (long[]) msg.obj;
                        updateProgress(arr[0],arr[1]);
                    }catch (Exception e){}
                    break;
            }
        }
    };

    private void setRemoteViewGameIcon(Bitmap bitmap){
        try {
            mRemoteViews.setImageViewBitmap(R.id.iv,bitmap);
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        }catch (Exception e){
            e.printStackTrace();
        }

        destoryRemoteViewGameIconTask();
    }

    /**
     * 停止Service
     * @param context
     */
    private void downloadStopService(Context context){

        /**当下载完成点击完成按钮时关闭通知栏**/
        mNotificationManager.cancel(NOTIFICATION_ID);

        stopService(new Intent(context, DownloadTask.class));

        //解除绑定
        if (mDataCallback!=null){
            mDataCallback.onStopService();
        }

    }


    /**
     * 开始下载
     */
    private void downloadStart(){
//        if (status == Status.DOWNLOADING){
//            return;
//        }
        Log.i(TAG,"downloadStart");
        status = Status.DOWNLOADING;

        try {
            if (mGameInfo != null) {
                //游戏名
                mRemoteViews.setTextViewText(R.id.tv_name, mGameInfo.name);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        mRemoteViews.setTextViewText(R.id.bt, "暂停");
        mRemoteViews.setTextViewText(R.id.tv_message, "下载中...");
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);

        if (mDataCallback!=null){
            mDataCallback.onStart(mGameInfo.gid);
        }
    }

    /**
     * 暂停下载
     */
    private void downloadPause(){
        if (status == Status.PAUSE){
            return;
        }
        Log.i(TAG,"downloadPause");
        status = Status.PAUSE;

        mRemoteViews.setTextViewText(R.id.bt, "下载");
        mRemoteViews.setTextViewText(R.id.tv_message, "已暂停");
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);

        if (mDataCallback!=null){
            mDataCallback.onPause(mGameInfo.gid);
        }
    }


    /**
     * 下载失败
     */
    private void downloadFail(String err) {
        Log.i(TAG,"downloadFail");
        status = Status.FAIL;

//        if (cancelable!=null && !cancelable.isCancelled()) {
//            cancelable.cancel();
//        }
        mRemoteViews.setTextViewText(R.id.bt, "重试");
        mRemoteViews.setTextViewText(R.id.tv_message, "下载失败");
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);

        if (mDataCallback!=null){
            mDataCallback.onFail(err, mGameInfo.gid);
        }
    }

    /**
     * 下载成功
     */
    private void downloadSuccess(String filePath) {
        Log.i(TAG,"downloadSuccess");

        status = Status.SUCCESS;
        mRemoteViews.setTextViewText(R.id.bt, "完成");
        mRemoteViews.setTextViewText(R.id.tv_message, "下载完成");
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);

        if (mDataCallback!=null){
            mDataCallback.onSuccess(filePath, mGameInfo.gid);
        }

        //发送安装事件
        installAPk();
    }

    //延时刷新通知栏
    private long lastTime = 0;
    /**
     * 下载更改进度
     *
     * @param total   总大小
     * @param current 当前已下载大小
     */
    private void updateProgress(long total, long current) {
        Log.i(TAG,"updateProgress: " + current);
        if (status != Status.DOWNLOADING){
            return;
        }

        int result = Math.round((float) current / (float) total * 100);

        //降低更新频率
        long curTime = new Date().getTime();
        if(curTime - lastTime > 1000 || total == current){
            mRemoteViews.setTextViewText(R.id.tv_size, StringUtil.formatSize(current) + "/" + StringUtil.formatSize(total));
            mRemoteViews.setProgressBar(R.id.pb, 100, result, false);
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
            lastTime = curTime;

            Log.i(TAG,"-----------------updateProgress: " + result);
        }

        if (mDataCallback!=null){
            mDataCallback.onProgress(total,current, mGameInfo.gid);
        }
    }


    public void installAPk() {
        if (this.mFilePath==null){
            return;
        }
        File apkFile = new File(this.mFilePath);
        if (!apkFile.exists()){
            return;
        }

        //检测apk是否可用
        boolean able = getUninatllApkInfo(this, this.mFilePath);
        if (!able){
            if (mDataCallback != null){
                mDataCallback.onInstallApkError(this.mFilePath, "Parse APK error!");
            }
            return;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String authority = getPackageName() + ".fileProvider";
            Uri apkUri = FileProvider.getUriForFile(this, authority, apkFile);
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
        startActivity(intent);
    }


    private boolean checkPackInfo() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(mGameInfo.pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
        }
        return packageInfo != null;
    }


    public static boolean openPackage(Context context, String pkgName) {
        try {
            Context pkgContext = getPackageContext(context, pkgName);
            Intent intent = getAppOpenIntentByPackageName(context, pkgName);
            if (pkgContext != null && intent != null) {
                intent.putExtra("openMoudle","serviceHall");
                pkgContext.startActivity(intent);
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static Intent getAppOpenIntentByPackageName(Context context,String packageName){
        //Activity完整名
        String mainAct = null;
        //根据包名寻找
        PackageManager pkgMag = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED|Intent.FLAG_ACTIVITY_NEW_TASK);


        @SuppressLint("WrongConstant") List<ResolveInfo> list = pkgMag.queryIntentActivities(intent,
                PackageManager.GET_ACTIVITIES);
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

    public static Context getPackageContext(Context context, String packageName) {
        Context pkgContext = null;
        if (context.getPackageName().equals(packageName)) {
            pkgContext = context;
        } else {
            // 创建第三方应用的上下文环境
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
     * 显示一个下载带进度条的通知
     *
     * @param context 上下文
     */
    public void createNotification(Context context) {
        //获取通知管理器
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //创建NotificationChannel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "云游戏下载通知", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("云游戏下载状态和进度通知");
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null);
            mNotificationManager.createNotificationChannel(channel);
        }

        //进度条通知构建
        NotificationCompat.Builder builderProgress = new NotificationCompat.Builder(context, CHANNEL_ID);

        //将Ongoing设为true 那么notification将不能滑动删除
        builderProgress.setOngoing(true);

        //设置小图标
        int smallIconRes = 0;
        try {
            //获取应用图标
            PackageManager pm = getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(getPackageName(), 0);
            smallIconRes = info.icon;
        } catch (Exception e) {
            e.printStackTrace();
        }
        builderProgress.setSmallIcon(smallIconRes);
        //启动通知时，弹出提示
        builderProgress.setTicker("开始下载...");
        //震动
        builderProgress.setVibrate(new long[]{0});
        //声音
        builderProgress.setSound(null);

        /**新建通知自定义布局**/
        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.kp_download_notification);
        /**进度条ProgressBar**/
        mRemoteViews.setProgressBar(R.id.pb, 100, 0, false);
        /**提示信息的TextView**/
        mRemoteViews.setTextViewText(R.id.tv_message, "下载中...");
        /**操作按钮的Button**/
        mRemoteViews.setTextViewText(R.id.bt, "暂停");

        try {
            if (mGameInfo != null) {
                //游戏名
                mRemoteViews.setTextViewText(R.id.tv_name, mGameInfo.name);
                //游戏图标
                Uri uri = Uri.parse(mGameInfo.iconUrl);
                mRemoteViews.setImageViewUri(R.id.iv, uri);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        /**设置通过广播形式的PendingIntent**/
        Intent intent = new Intent(BROADCAST_ACTION_CLICK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_BROADCAST, intent, 0);
        mRemoteViews.setOnClickPendingIntent(R.id.bt, pendingIntent);

        /**设置通过广播形式的PendingIntent**/
        Intent closeIntent = new Intent(BROADCAST_ACTION_CLICKCLOSE);
        PendingIntent closePendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_BROADCAST, closeIntent, 0);
        mRemoteViews.setOnClickPendingIntent(R.id.bt_close, closePendingIntent);
        /**设置自定义布局**/
        builderProgress.setContent(mRemoteViews);
        mNotification = builderProgress.build();
    }

    /**
     * 更新通知界面的按钮的广播
     */
    private static class MyBroadcastReceiver extends BroadcastReceiver {
        WeakReference<DownloadTask> ref;
        private MyBroadcastReceiver(DownloadTask service){
            ref = new WeakReference<>(service);
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if (ref==null || ref.get()==null){
                return;
            }

            if (intent.getAction().equals(BROADCAST_ACTION_CLICKCLOSE)){

                //停止下载
                if (ref.get().status == Status.DOWNLOADING){
                    ref.get().stopHttp();
                    ref.get().downloadPause();
                }

                //停止service
                ref.get().downloadStopService(context);

            }else if (intent.getAction().equals(BROADCAST_ACTION_CLICK)){

                Logger.info(TAG, "status=" + ref.get().status);

                switch (ref.get().status) {
                    case DOWNLOADING:
                        /**当在下载中点击暂停按钮**/
                        ref.get().stopHttp();
                        ref.get().downloadPause();
                        break;
                    case SUCCESS:
                        //判断是否安装
                        if(!ref.get().checkPackInfo()){
                            ref.get().installAPk();
                        }
                        //停止service
                        ref.get().downloadStopService(context);
                        break;
                    case FAIL:
                    case PAUSE:
                        /**当在暂停时点击下载按钮**/
                        ref.get().startHttp();
                        ref.get().downloadStart();
                        break;
                }

            }

        }
    }

    /**
     * 网络状态监听广播
     */
    private static class NetworkReceiver extends BroadcastReceiver {
        WeakReference<DownloadTask> ref;
        private NetworkReceiver(DownloadTask service){
            ref = new WeakReference<>(service);
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                //说明当前有网络
                if (networkInfo != null && networkInfo.isAvailable()) {
                    int type = networkInfo.getType();
                    switch (type) {
                        case ConnectivityManager.TYPE_MOBILE:
//                            Toast.makeText(context, "移动网络暂停下载", Toast.LENGTH_SHORT).show();
                            if (ref!=null && ref.get()!=null){
                                //移动网络暂停下载
                                if(ref.get().getStatus() == Status.DOWNLOADING){
                                    ref.get().stopHttp();
                                    ref.get().downloadPause();
                                }
                            }
                            break;
                    }
                } else {
                    //说明当前没有网络
//                    Toast.makeText(context, "当前网络异常", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * App安装广播
     */
    private static class InstallApkReceiver extends BroadcastReceiver {

        WeakReference<DownloadTask> ref;
        private InstallApkReceiver(DownloadTask service){
            ref = new WeakReference<>(service);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || ref==null || ref.get() == null){
                return;
            }
            //接收安装广播  
            if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
                String packageName = intent.getDataString();
                if (StringUtil.isEmpty(packageName) || StringUtil.isEmpty(ref.get().mGameInfo.pkgName)){
                    return;
                }
                if (packageName!=null && packageName.endsWith(ref.get().mGameInfo.pkgName)){
                    //安装成功，删除本地文件
                    File file = new File(ref.get().mFilePath);
                    if (file!=null && file.exists()){
                        file.delete();
                    }
//                    stopService(new Intent(context,DownloadTask.class));
                    ref.get().downloadStopService(context);
                }
                System.out.println("安装了:" +packageName + "包名的程序");
            }
        }
    }

    /**
     * 判断Apk包是否可用
     * @param context
     * @param filePath
     * @return
     */
    public boolean getUninatllApkInfo(Context context, String filePath) {
        boolean result = false;
        try {
            PackageManager pm = context.getPackageManager();
            Log.e("archiveFilePath", filePath);
            PackageInfo info = pm.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
            String packageName = null;
            if (info != null) {
                result = true;
            }
        } catch (Exception e) {
            result = false;
            Log.e(TAG,e.getMessage());
        }
        return result;
    }


    /**
     * 加载游戏图片
     * @param iconUrl
     */
    private void loadGameIcon(String iconUrl){
        destoryRemoteViewGameIconTask();
        if (!StringUtil.isEmpty(iconUrl)){
            mGameIconTask = new RemoteViewGameIconTask(this);
            mGameIconTask.execute(iconUrl);
        }
    }

    /**
     * 销毁task
     */
    private void destoryRemoteViewGameIconTask(){
        try {
            if (mGameIconTask!=null && !mGameIconTask.isCancelled()){
                mGameIconTask.cancel(true);
            }
            mGameIconTask = null;
        }catch (Exception e){
        }
    }

    /**
     * 加载游戏图标
     */
    private static class RemoteViewGameIconTask extends AsyncTask<String, Void, Bitmap>{
        private WeakReference<DownloadTask> ref = null;
        private RemoteViewGameIconTask(DownloadTask service){
            ref = new WeakReference<>(service);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(6000);//设置超时
                conn.setDoInput(true);
                conn.setUseCaches(false);//不缓存
                conn.connect();
                int code = conn.getResponseCode();
                Bitmap bitmap = null;
                if(code==200) {
                    InputStream is = conn.getInputStream();//获得图片的数据流
                    bitmap = BitmapFactory.decodeStream(is);
                }
                return bitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (result != null) {
                if (ref!=null && ref.get()!= null){
                    ref.get().setRemoteViewGameIcon(result);
                }
            }
        }
    }
}