package kptech.game.kit.download;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import kptech.game.kit.GameInfo;
import kptech.game.kit.R;
import kptech.game.kit.callback.IGameObservable;
import kptech.game.kit.callback.SimpleGameObservable;
import kptech.game.kit.manager.KpGameDownloadManger;
import kptech.game.kit.manager.KpGameManager;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.StringUtil;

public class DownloadService extends Service {

    private static final String TAG = "DownloadService";
    public static final String EXTRA_GAME = "extra_gameinfo";
    private static final int NOTIFICATION_ID = 1223;
    private static final int REQUEST_CODE_BROADCAST = 0x0001;
    private static final String CHANNEL_ID = "kpCloudGameDownload";
    public static final String BROADCAST_ACTION_CLICK = "servicetask";
    public static final String BROADCAST_ACTION_CLICKCLOSE = "kp_close";
    private RemoteViews mRemoteViews;
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private GameInfo mGameInfo;
    private String mDownloadUrl;
    private RemoteViewGameIconTask mGameIconTask;
    private KpGameDownloadManger mKpDownloadManager;
    private NotifyBroadcastReceiver mNotifyReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        mKpDownloadManager = KpGameDownloadManger.instance();
        //通知栏
        createNotification(this);
        //注册广播
        registerBroadCast();

        KpGameManager.instance().addObservable(mObservable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null || mKpDownloadManager == null) {
            Logger.error(TAG, "intent == null || mKpDownloadManager == null");
            stopSelf();
            return super.onStartCommand(null, flags, startId);
        }
        mGameInfo = intent.getParcelableExtra(EXTRA_GAME);
        if (mGameInfo == null) {
            stopSelf();
            Logger.error(TAG, "下载服务未获取到游戏信息");
            return super.onStartCommand(intent, flags, startId);
        }
        mDownloadUrl = mGameInfo.downloadUrl;
        if (mDownloadUrl == null || mDownloadUrl.isEmpty()){
            stopSelf();
            Toast.makeText(DownloadService.this,"未获取到下载地址",Toast.LENGTH_SHORT).show();
            return super.onStartCommand(intent, flags, startId);
        }

        int state = mKpDownloadManager.getDownloadState(mDownloadUrl);
        if (state == KpGameDownloadManger.STATE_FINISHED){
            stopSelf();
            showInstallApk();
            return super.onStartCommand(intent, flags, startId);
        }

        //加载游戏图标
        loadGameIcon(mGameInfo.iconUrl);

        //通知栏
        startForeground(NOTIFICATION_ID, mNotification);

        mKpDownloadManager.continueDownload(this,mGameInfo);

        return super.onStartCommand(intent, flags, startId);
    }

    private void loadGameIcon(String iconUrl) {
        destoryRemoteViewGameIconTask();
        if (!StringUtil.isEmpty(iconUrl)){
            mGameIconTask = new RemoteViewGameIconTask(this);
            mGameIconTask.execute(iconUrl);
        }
    }

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
     * 销毁task
     */
    private void destoryRemoteViewGameIconTask(){
        try {
            if (mGameIconTask!=null && !mGameIconTask.isCancelled()){
                mGameIconTask.cancel(true);
            }
            mGameIconTask = null;
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void registerBroadCast() {
        mNotifyReceiver = new NotifyBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_ACTION_CLICK);
        filter.addAction(BROADCAST_ACTION_CLICKCLOSE);

        registerReceiver(mNotifyReceiver, filter);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

        //新建通知自定义布局
        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.kp_download_notification);
        //进度条ProgressBar
        mRemoteViews.setProgressBar(R.id.pb, 100, 0, false);
        //提示信息的TextView
        mRemoteViews.setTextViewText(R.id.tv_message, "下载中...");
        //操作按钮的Button
        mRemoteViews.setTextViewText(R.id.bt, "暂停");

        try {
            if (mGameInfo != null) {
                //游戏名
                mRemoteViews.setTextViewText(R.id.tv_name, mGameInfo.name);
                //游戏图标
                Uri uri = Uri.parse(mGameInfo.iconUrl);
                mRemoteViews.setImageViewUri(R.id.iv, uri);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //设置通过广播形式的PendingIntent
        Intent intent = new Intent(BROADCAST_ACTION_CLICK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_BROADCAST, intent, 0);
        mRemoteViews.setOnClickPendingIntent(R.id.bt, pendingIntent);

        //设置通过广播形式的PendingIntent
        Intent closeIntent = new Intent(BROADCAST_ACTION_CLICKCLOSE);
        PendingIntent closePendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_BROADCAST, closeIntent, 0);
        mRemoteViews.setOnClickPendingIntent(R.id.bt_close, closePendingIntent);

        //设置自定义布局
        builderProgress.setContent(mRemoteViews);
        mNotification = builderProgress.build();
    }

    private void downloadStopService() {
        KpGameDownloadManger.instance().stopDownload(mDownloadUrl);
        mNotificationManager.cancel(NOTIFICATION_ID);
        stopSelf();
    }

    private void dealNotifyClick() {
        int state = KpGameDownloadManger.instance().getDownloadState(mDownloadUrl);
        switch (state) {
            case KpGameDownloadManger.STATE_STARTED: //下载中
                downloadPause();
                break;
            case KpGameDownloadManger.STATE_FINISHED: //下载完成
                showInstallApk();
                break;
            case KpGameDownloadManger.STATE_ERROR://下载失败
                downloadFail();
                break;
            default:
                downloadStart();
                break;
        }
    }

    private void downloadFail() {
        if (mRemoteViews != null){
            mRemoteViews.setTextViewText(R.id.bt, "重试");
            mRemoteViews.setTextViewText(R.id.tv_message, "下载失败");
        }
        if (mNotificationManager != null){
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        }
    }

    private void downloadStart() {
        try{
            KpGameDownloadManger.instance().continueDownload(this,mGameInfo);
            if (mRemoteViews != null){
                mRemoteViews.setTextViewText(R.id.tv_name, mGameInfo.name);
                mRemoteViews.setTextViewText(R.id.bt, "暂停");
                mRemoteViews.setTextViewText(R.id.tv_message, "下载中...");
            }
            if (mNotificationManager != null){
                mNotificationManager.notify(NOTIFICATION_ID, mNotification);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showInstallApk() {
        if (mRemoteViews != null){
            mRemoteViews.setTextViewText(R.id.bt, "完成");
            mRemoteViews.setTextViewText(R.id.tv_message, "下载完成");
        }
        if (mNotificationManager != null){
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        }
        KpGameDownloadManger.instance().doInstallApk(DownloadService.this,mGameInfo.pkgName);
    }

    private void downloadPause() {
        try{
            KpGameDownloadManger.instance().stopDownload(mDownloadUrl);
            if (mRemoteViews != null){
                mRemoteViews.setTextViewText(R.id.bt, "下载");
                mRemoteViews.setTextViewText(R.id.tv_message, "已暂停");
            }
            if (mNotificationManager != null){
                mNotificationManager.notify(NOTIFICATION_ID, mNotification);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private static class NotifyBroadcastReceiver extends BroadcastReceiver {

        private final WeakReference<DownloadService> ref;

        private NotifyBroadcastReceiver(DownloadService service) {
            ref = new WeakReference<>(service);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ref.get() == null) {
                return;
            }
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (action == null || action.isEmpty()) {
                return;
            }
            DownloadService service = ref.get();

            if (action.equals(BROADCAST_ACTION_CLICKCLOSE)) {
                service.downloadStopService();
            } else if (action.equals(BROADCAST_ACTION_CLICK)) {
                service.dealNotifyClick();
            }
        }
    }

    /**
     * 加载游戏图标
     */
    private static class RemoteViewGameIconTask extends AsyncTask<String, Void, Bitmap> {

        private final WeakReference<DownloadService> ref;
        private RemoteViewGameIconTask(DownloadService service){
            ref = new WeakReference<>(service);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            HttpURLConnection conn = null;
            try {
                URL url = new URL(params[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(6000);//设置超时
                conn.setDoInput(true);
                conn.setUseCaches(false);//不缓存
                conn.connect();
                int code = conn.getResponseCode();
                if(code==200) {
                    InputStream is = conn.getInputStream();//获得图片的数据流
                    bitmap = BitmapFactory.decodeStream(is);
                }
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (conn != null){
                        conn.disconnect();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return null;
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

    private final IGameObservable mObservable = new SimpleGameObservable() {

        @Override
        public void updateDownloadStatus(int status, String url) {
            switch (status){
                case KpGameDownloadManger.STATE_WAITING:
                    break;
                case KpGameDownloadManger.STATE_STARTED:
                    downloadStart();
                    break;
                case KpGameDownloadManger.STATE_FINISHED:
                    showInstallApk();
                    break;
                case KpGameDownloadManger.STATE_STOPPED:
                    downloadPause();
                    break;
                case KpGameDownloadManger.STATE_ERROR:
                    downloadFail();
                    break;
            }
        }

        @Override
        public void updateDownloadProgress(long total, long current, String url) {
            if (mDownloadUrl.equals(url)){
                updateProgress(total, current);
            }
        }
    };

    //延时刷新通知栏
    private long lastTime = 0;
    /**
     * 下载更改进度
     * @param total   总大小
     * @param current 当前已下载大小
     */
    private void updateProgress(long total, long current) {
        Log.i(TAG,"updateProgress: " + current);

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
    }

    @Override
    public void onDestroy() {
        KpGameManager.instance().removeObservable(mObservable);
        if (mNotifyReceiver != null){
            unregisterReceiver(mNotifyReceiver);
        }
        super.onDestroy();
    }
}
