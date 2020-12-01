package kptech.game.kit;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Display;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kptech.game.kit.ad.AdManager;
import kptech.game.kit.ad.IAdCallback;
import kptech.game.kit.analytic.DeviceInfo;
import kptech.game.kit.analytic.Event;
import kptech.game.kit.analytic.EventCode;
import kptech.game.kit.analytic.MobclickAgent;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.data.RequestClientNotice;
import kptech.game.kit.msg.IMsgReceiver;
import kptech.game.kit.msg.MsgManager;
import kptech.game.kit.thread.HeartThread;
import kptech.game.kit.utils.DeviceUtils;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;

public class DeviceControl {
    private static final String TAG = "GameControl";
//    private Logger logger = new Logger(TAG);

    private com.yd.yunapp.gameboxlib.DeviceControl mDeviceControl;
    private GameInfo mGameInfo;
    private JSONObject mDeviceToken;
    private AdManager mAdManager;
    private String mCorpKey;

    //耗时统计
    private boolean sendTmEvent = false;
    private long TM_DEVICE_END, TM_VIDEO_START, TM_VIDEO_END;

    private String mPadcode = "";
    private boolean mIsSoundEnable = true;
    private String mPicQuality = "";

    private Handler mGameHandler;
    private Activity mActivity;
    private int mGameRes;
    private APICallback<String> mGameStartCallback;

    protected DeviceControl(com.yd.yunapp.gameboxlib.DeviceControl control){
        this(control,null);
    }

    protected DeviceControl(com.yd.yunapp.gameboxlib.DeviceControl control, GameInfo game){
        this.mDeviceControl = control;
        this.mGameInfo = game;
        this.mGameHandler = new GameHandler();
        //解析deviceToken
        parseDeviceToken();
    }

    public void setCorpKey(String corpKey){
        this.mCorpKey = corpKey;
    }

    public void setAdManager(AdManager adManager) {
        this.mAdManager = adManager;
    }

    public void setTM_DEVICE_END(long tm){
        this.TM_DEVICE_END = tm;
    }

    private void parseDeviceToken(){
        try {
            String deviceStr = mDeviceControl.getDeviceToken();
            JSONObject deviceTokenObj = new JSONObject(deviceStr);
            if (deviceTokenObj!=null && deviceTokenObj.has("token")){
                String tokenStr = deviceTokenObj.getString("token");
                JSONObject tokenObject = new JSONObject(tokenStr);

                mDeviceToken = tokenObject;

                //解析padcode
                if (mDeviceToken != null && mDeviceToken.has("deviceId")) {
                    String str = mDeviceToken.getString("deviceId");
                    mPadcode = str;
                }

                //解析当前声音开关
                if (mDeviceToken != null && mDeviceToken.has("sound")) {
                    String str = mDeviceToken.getString("sound");
                    mIsSoundEnable = "true".equals(str);
                }

                //解析当前画面质量
                if (mDeviceToken != null && mDeviceToken.has("picQuality")) {
                    String str = mDeviceToken.getString("picQuality");
                    mPicQuality = str;
                }
            }

        }catch (Exception e){
            Logger.error(TAG, "parseDeviceToken, error:"+e.getMessage());
        }
    }

    /**
     * 获取padcode
     * @return
     */
    public String getPadcode(){
        return mPadcode;
    }

    /**
     * 当前声音是否打开
     * @return
     */
    public boolean isSoundEnable(){
        return mIsSoundEnable;
    }

    /**
     * 画面质量
     * @return
     */
    public String getVideoQuality(){
        return mPicQuality;
    }

    /**
     * 启动游戏
     * @param activity
     * @param res
     * @param callback
     */
    public void startGame(@NonNull final Activity activity, @IdRes final int res, @NonNull final APICallback<String> callback){
        if (this.mGameInfo == null){
            if (callback!=null){
                callback.onAPICallback("gameInfo is null", APIConstants.ERROR_GAME_INF_EMPTY);
            }
            return;
        }

        this.mActivity = activity;
        this.mGameRes = res;
        this.mGameStartCallback = callback;

        //连接设备
        MsgManager.start(activity, GameBoxManager.mCorpID, mDeviceControl.getDeviceToken(), this.mGameInfo.pkgName, this.mGameInfo.kpGameId, this.mGameInfo.name);

        //同步设备信息
        sendMockDeviceInfo();

        //云存档接口
        sendClientNotice();

        //加载广告
        loadGameAd();

    }

    /**
     * 加载广告
     */
    private void loadGameAd(){
        try {
            //加载广告
            if (this.mAdManager != null && this.mGameInfo.showAd == GameInfo.GAME_AD_SHOW_ON) {
                this.mAdManager.loadGameAd(new IAdCallback<String>() {
                    @Override
                    public void onAdCallback(String msg, int code) {
                        switch (code) {
                            //点击取消
                            case AdManager.CB_AD_CANCELED:
                                if (mGameStartCallback != null) {
                                    mGameStartCallback.onAPICallback("game cancel", APIConstants.ERROR_GAME_CANCEL);
                                }
                                break;
                            //加载广告弹窗
                            case AdManager.CB_AD_LOADING:
                                //显示广告
                                if (mGameStartCallback != null) {
                                    mGameStartCallback.onAPICallback("", APIConstants.AD_LOADING);
                                }
                                break;
                            //激励视频广告加载失败
                            case AdManager.CB_AD_FAILED:
                                //其它状态，加载游戏
                            default:
                                if (mGameStartCallback!=null){
                                    mGameStartCallback.onAPICallback("", APIConstants.GAME_LOADING);
                                }
                                //启动游戏
                                if (mGameHandler != null){
                                    mGameHandler.sendMessage(Message.obtain(mGameHandler, MSG_GAME_EXEC, FLAG_AD));
                                }

                                break;
                        }
                    }
                });
                return;
            }
        }catch (Exception e){}

        //启动游戏
        if (mGameHandler != null) {
            mGameHandler.sendMessage(Message.obtain(mGameHandler, MSG_GAME_EXEC, FLAG_AD));
        }
    }

    /**
     * 云存档发送通知
     * @return
     */
    private void sendClientNotice(){
        if (mGameInfo.recoverCloudData == 1){
            if (mGameStartCallback!=null){
                mGameStartCallback.onAPICallback("", APIConstants.RECOVER_DATA_LOADING);
            }

            //发送打点事件
            try {
                Event event = Event.getEvent(EventCode.DATA_DEVICE_SEND_NOTICE, mGameInfo.pkgName, getPadcode());
                MobclickAgent.sendEvent(event);
            }catch (Exception e){}

            //调用通知接口
            try {
                new RequestClientNotice()
                        .setCallback(new RequestClientNotice.ICallback() {
                            @Override
                            public void onResult(String ret) {
                                long sleeptime = -1;
                                try {
                                    JSONObject obj = new JSONObject(ret);
                                    if (obj.has("sleeptime")){
                                        sleeptime = Long.parseLong(obj.getString("sleeptime"));
                                    }
                                }catch (Exception e){
                                }
                                //默认等待3秒
                                if (sleeptime <= 0){
                                    sleeptime = 3000;
                                }
                                Logger.info(TAG, "clientNotice, ret = " + ret);
                                //延时3秒
                                if (mGameHandler != null) {
                                    mGameHandler.sendMessageDelayed(Message.obtain(mGameHandler, MSG_GAME_EXEC, FLAG_NOTICE), sleeptime);
                                }
                            }
                        })
                        .execute(mPadcode,mGameInfo.pkgName, DeviceInfo.getUserId(mActivity), mCorpKey);
                return;
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        //启动游戏
        if (mGameHandler != null) {
            mGameHandler.sendMessage(Message.obtain(mGameHandler, MSG_GAME_EXEC, FLAG_NOTICE));
        }
        return;
    }

    /**
     * 发送云设备信息
     */
    private void sendMockDeviceInfo(){

        long sleepTime = 3000;
        try {
            if (mGameInfo!=null && mGameInfo.mockSleepTime == -1){
                //获取整体时间
                String str = ProferencesUtils.getString(mActivity, SharedKeys.KEY_GAME_MOCK_SLEEPTIME,null);
                if (str != null){
                    sleepTime = Long.parseLong(str);
                }
            }else if (mGameInfo!=null){
                //获取游戏设置的时间
                sleepTime = mGameInfo.mockSleepTime;
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }

        try {
            //不同步信息
            if (sleepTime == -3){

            }
            //同步信息，不等待
            else if (sleepTime == -2){
                //上传信息并等待
                new Thread(){
                    @Override
                    public void run() {
                        mDeviceControl.mockDeviceInfo();
                    }
                }.start();
            }
            //同步信息，等待
            else {
                final long time = sleepTime >= 0 ? sleepTime : 3000;
                //上传信息并等待
                new Thread(){
                    @Override
                    public void run() {
                        mDeviceControl.mockDeviceInfo();
                        if (mGameHandler != null) {
                            mGameHandler.sendMessageDelayed(Message.obtain(mGameHandler, MSG_GAME_EXEC, FLAG_MOCK), time);
                        }
                    }
                }.start();
                return;
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }

        if (mGameHandler != null) {
            mGameHandler.sendMessage(Message.obtain(mGameHandler, MSG_GAME_EXEC, FLAG_MOCK));
        }
    }

    private static final int MSG_GAME_EXEC = 1;
    private static final int MSG_FLAG_REST = 2;

    private static final int FLAG_NOTICE = 1;
    private static final int FLAG_AD = 2;
    private static final int FLAG_MOCK = 4;
    private static final int FLAG_SUCCESS = FLAG_NOTICE | FLAG_AD | FLAG_MOCK;

    public void removerListener() {
        if (mDeviceControl != null){
            mDeviceControl.setPlayListener(null);
        }
    }

    private class GameHandler extends Handler{
        private int flag = 0;
        private boolean isExec = false;

        public GameHandler(){
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MSG_FLAG_REST:
                    flag = 0;
                    isExec = false;
                    break;
                //运行游戏
                case MSG_GAME_EXEC:
                    try {
                        int curFlag = (int) msg.obj;
                        flag = flag | curFlag;
                    }catch (Exception e){}

                    if (flag == FLAG_SUCCESS && !isExec){
                        //只运行1次
                        isExec = true;
                        execStartGame(mActivity,mGameRes,mGameStartCallback);
                    }
                    break;
            }

        }
    }


    private synchronized void execStartGame(@NonNull final Activity activity, @IdRes int res, @NonNull final APICallback<String> callback){

        //发送打点事件
        try {
            Event event = Event.getEvent(EventCode.DATA_VIDEO_READY_RECVING, mGameInfo.pkgName, getPadcode());
            MobclickAgent.sendEvent(event);
        }catch (Exception e){}

        //发送事件耗时统计
        if (!sendTmEvent) {
            if (TM_DEVICE_END  == 0){
                TM_DEVICE_END = new Date().getTime();
            }

            sendTmEvent = true;
            try {
                //发送事件耗时统计
                TM_VIDEO_START = new Date().getTime();
                long useTm = ( TM_VIDEO_START - TM_DEVICE_END );
                HashMap data = new HashMap();
                data.put("stime", TM_DEVICE_END);
                data.put("etime", TM_VIDEO_START);
                Event event = Event.getTMEvent(EventCode.DATA_TMDATA_VIDEO_START, useTm, data);
                event.setPadcode(getPadcode());
                event.setGamePkg(mGameInfo.pkgName);
                MobclickAgent.sendTMEvent(event);
            }catch (Exception e){}
        }

        if (callback!=null){
            callback.onAPICallback("", APIConstants.GAME_LOADING);
        }
        mDeviceControl.startGame(activity, res, new com.yd.yunapp.gameboxlib.APICallback<String>() {
            @Override
            public void onAPICallback(String msg, int code) {
                if (callback!=null){
                    callback.onAPICallback(msg, code);
                }

                //成功连接游戏,删除激励广告标记
                try {
                    int adVerify = ProferencesUtils.getIng(activity, SharedKeys.KEY_AD_REWARD_VERIFY_FLAG, 0);
                    if (adVerify > 0 && (code == APIConstants.CONNECT_DEVICE_SUCCESS || code == APIConstants.RECONNECT_DEVICE_SUCCESS)){
                        ProferencesUtils.setInt(activity, SharedKeys.KEY_AD_REWARD_VERIFY_FLAG, 0);
                    }
                }catch (Exception e){
                    Logger.error(TAG, e.getMessage());
                }


                //发送打点事件
                try {
                    Event event = Event.getEvent(EventCode.getGameEventCode(code), mGameInfo.pkgName, getPadcode(), msg, null);
                    HashMap ext = new HashMap<>();
                    ext.put("code", code);
                    ext.put("msg", msg);
                    event.setExt(ext);
                    MobclickAgent.sendEvent(event);
                }catch (Exception e){}

                //发送事件耗时统计
                if (sendTmEvent){
                    try {
                        sendTmEvent = false;
                        boolean ret = false;
                        if (code == APIConstants.CONNECT_DEVICE_SUCCESS || code == APIConstants.RECONNECT_DEVICE_SUCCESS){
                            ret = true;
                        }else {
                            ret = false;
                        }

                        //发送事件耗时统计
                        TM_VIDEO_END = new Date().getTime();
                        long useTm = ( TM_VIDEO_END - TM_VIDEO_START );
                        HashMap data = new HashMap();
                        data.put("stime", TM_VIDEO_START);
                        data.put("etime", TM_VIDEO_END);
                        data.put("state", ret ? "ok" : "failed");
                        data.put("code", code);
                        Event event = Event.getTMEvent(EventCode.DATA_TMDATA_VIDEO_END, useTm, data);
                        event.setPadcode(getPadcode());
                        event.setGamePkg(mGameInfo.pkgName);
                        MobclickAgent.sendTMEvent(event);
                    }catch (Exception e){}
                }


                //记录游戏开始时间
                if (code == APIConstants.CONNECT_DEVICE_SUCCESS || code == APIConstants.RECONNECT_DEVICE_SUCCESS){
                    //开始心跳统计
                    startSendPlayTime();
                }else if (code == APIConstants.RELEASE_SUCCESS){
                    //关闭心跳统计
                    stopSendPlayTime();
                }

            }
        });
    }

    /**
     * 停止试玩，在退出试玩的时候必须回调，否则无法进行下一次试玩
     */
    public void stopGame(){
        try {
            MsgManager.stop();
        }catch (Exception e){
            e.printStackTrace();
        }

        mDeviceControl.stopGame();


        try {
            if (mAdManager != null){
                mAdManager.destory();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        mActivity = null;
        mGameStartCallback = null;
        mGameHandler = null;

//        GameBoxManager.getInstance().removeDeviceInfo();
    }

    /**
     * 设置无操作超时
     * @param font 前台超时，单位s
     * @param back 后台超时，单位s
     */
    public void setNoOpsTimeout(long font, long back) {
        mDeviceControl.setNoOpsTimeout(font, back);
    }

    /**
     * 调整试玩的码率
     * @param level 等级，目前支持5档
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_AUTO} 自动
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_HD} 高清
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_ORDINARY} 普通
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_HS} 一般
     * {@link APIConstants#DEVICE_VIDEO_QUALITY_LS} 流畅
     */
    public void switchQuality(@APIConstants.VideoQuality String level){
        mPicQuality = level;
        mDeviceControl.switchQuality(level);
    }

    /**
     * 试玩声音开关
     * @param audioSwitch
     */
    public void setAudioSwitch(boolean audioSwitch){
        mIsSoundEnable = audioSwitch;
        mDeviceControl.setAudioSwitch(audioSwitch);
    }

    public boolean isReleased(){
        return mDeviceControl.isReleased();
    }

    /**
     * 注册排队监听回调
     * @param callback 回调
     */
    public void registerQueueCallback(final APICallback<QueueRankInfo> callback){
        mDeviceControl.registerQueueCallback(new com.yd.yunapp.gameboxlib.APICallback<com.yd.yunapp.gameboxlib.QueueRankInfo>() {
            @Override
            public void onAPICallback(com.yd.yunapp.gameboxlib.QueueRankInfo queueRankInfo, int i) {
                if (callback!=null){
                    callback.onAPICallback(new QueueRankInfo(queueRankInfo), i);
                }
            }
        });
    }

    /**
     * 设置试玩监听
     * @param listener  PlayListener 回调
     */
    public void setPlayListener(final PlayListener listener){
        mDeviceControl.setPlayListener(new com.yd.yunapp.gameboxlib.DeviceControl.PlayListener(){
            @Override
            public void onPingUpdate(int ping) {
                if (listener!=null){
                    listener.onPingUpdate(ping);
                }
            }

            @Override
            public boolean onNoOpsTimeout(int type, long timeout) {
                boolean b = false;
                if (listener!=null){
                    b = listener.onNoOpsTimeout(type,timeout);
                }

                try {
                    //发送打点事件
                    Event event = Event.getEvent(EventCode.DATA_VIDEO_USER_LEAVE, mGameInfo.pkgName, getPadcode());
                    HashMap ext = new HashMap<>();
                    ext.put("type", type);
                    ext.put("timeout", timeout);
                    event.setExt(ext);
                    MobclickAgent.sendEvent(event);
                }catch (Exception e){}

                return b;
            }

            @Override
            public void onScreenChange(int i) {
                if (listener!=null){
                    listener.onScreenChange(i);
                }
            }

            @Override
            public void onScreenCapture(byte[] bytes) {

            }
        });
    }

    /**
     * 设置试玩倒计时监听
     * @param threshold 阈值，到达开始倒计时回调，如果小于等于0，则从设置监听起开始回调
     * @param listener 监听listener
     */
    public void setTimeDownListener(int threshold, final TimeDownListener listener){

        mDeviceControl.setTimeCountDownListener(threshold, new com.yd.yunapp.gameboxlib.DeviceControl.TimeCountDownListener() {
            @Override
            public void countDown(int remainingTime) {
                if (listener != null) {
                    listener.countDown(remainingTime);
                }
            }
        });
    }

    /**
     * 注册监听，接收云手机App返回的消息
     *
     * @param listener
     */
    public void registerDataTransferListener(final DataTransferListener listener){
        mDeviceControl.registerDataTransferListener(new com.yd.yunapp.gameboxlib.DeviceControl.DataTransferListener(){

            @Override
            public void onReceiveData(Map<String, String> map) {
                if (listener != null) {
                    listener.onReceiveData(map);
                }
            }

            @Override
            public void onSendFailed(int i) {
                if (listener != null) {
                    listener.onSendFailed(i);
                }
            }
        });
    }

    /**
     * 注册监听，接收硬件采集信息
     *
     * @param listener
     */
    public void registerSensorSamplerListener(final SensorSamplerListener listener){
        mDeviceControl.registerSensorSamplerListener(new com.yd.yunapp.gameboxlib.DeviceControl.SensorSamplerListener() {
            @Override
            public void onSensorSamper(int sensor, int state) {
                if (listener != null) {
                    listener.onSensorSamper(sensor, state);
                }
            }
        });
    }

    /**
     * 发送硬件信息，此接口针对摄像头和麦克风数据
     *
     * @param type 硬件类型 @{@SensorConstants.CloudPhoneSensorId}
     * @param type 数据@{@SensorConstants.CameraVideoType}
     * @return
     */
    public void sendSensorInputData(@SensorConstants.CloudPhoneSensorId int sendor,
                                             @SensorConstants.AudioType @SensorConstants.CameraVideoType int type,
                                             byte[] data){
        mDeviceControl.sendSensorInputData(sendor, type, data);
    }

    /**
     * 发送硬件传感器信息，此接口针对陀螺仪、加速器、重力感应等传感器
     * @param sendor 传感器id @{@SensorConstants.CloudPhoneSensorId}
     * @param sensorType @{传感器类型 SensorConstants.SensorType}
     * @param data 传感器数据参数
     */
    public void sendSensorInputData(@SensorConstants.CloudPhoneSensorId int sendor,
                                             @SensorConstants.SensorType int sensorType,
                                             float... data){
        mDeviceControl.sendSensorInputData(sendor,sensorType,data);
    }


    /**
     * 设置视频流显示模式，默认自动切换，需要在startGame之前调用
     *
     * @param orientation ActivityInfo#SCREEN_ORIENTATION_PORTRAIT，ActivityInfo#SCREEN_ORIENTATION_LANDSCAPE
     */
    public void setVideoOrientation(int orientation){
        mDeviceControl.setVideoOrientation(orientation);
    }

    public int[] getVideoSize() {
        try {
            if (mDeviceToken!=null && mDeviceToken.has("resolution")){
                String str = mDeviceToken.getString("resolution");
                String[] arr = str.split("_");
                if (arr.length == 3){
                    int w = Integer.parseInt(arr[1]);
                    int h = Integer.parseInt(arr[2]);
                    return new int[]{w, h};
                }
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }

        return new int[]{720, 1280};
    }

    /**
     * 发送消息
     * @param msg
     */
    public void sendMessage(String msg){
        try {
            MsgManager.sendMessage(msg);
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }
    }

    /**
     * 设置消息接收器
     * @param receiver
     */
    public void setMessageReceiver(IMsgReceiver receiver) {
        try {
            MsgManager manager = MsgManager.getInstance();
            if (manager != null){
                manager.setMessageReceiver(receiver);
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }
    }

    /**
     * 试玩监听
     */
    public interface PlayListener {
        /**
         * 网络延时反馈
         * @param ping 网络延时，单位ms
         */
        void onPingUpdate(int ping);

        /**
         * 无操作超时回调
         * @param type 类型。1为后台，2为前台
         * @param timeout 超时时长，单位s
         * @return 返回true表示消耗了事件，sdk不处理超时逻辑；返回false表示未消耗事件，在倒计时结束后，sdk会停止试玩
         */
        boolean onNoOpsTimeout(int type, long timeout);

        void onScreenChange(int value);
    }

    /**
     * 试玩结束倒计时监听
     */
    public interface TimeDownListener {
        /**
         *  倒计时触发
         * @param remainingTime remainingTime 剩余时间，单位s秒
         */
        void countDown(int remainingTime);
    }


    /**
     * 云手机消息接收监听
     */
    public interface DataTransferListener {

        /**
         * 接收到云手机消息回掉
         *
         * @param data 消息未自定义格式
         */
        void onReceiveData(Map<String, String> data);

        /**
         * 消息发送失败
         *
         * @param code
         */
        void onSendFailed(int code);
    }

    /**
     * 采集硬件信息回掉
     */
    public interface SensorSamplerListener {

        /**
         * 采集硬件信息状态发生变化
         *
         * @param sensor 硬件类型  @SensorConstants.CloudPhoneSensorId
         * @param state  硬件状态  @SensorConstants.SensorState
         */
        void onSensorSamper(@SensorConstants.CloudPhoneSensorId int sensor,
                            @SensorConstants.SensorState int state);
    }

    //心跳统计游戏运行时长
    private Handler mPlayTimeHandler = null;
    private long playTimestamp = 0;
    private synchronized void startSendPlayTime(){
        if (mPlayTimeHandler == null) {
            mPlayTimeHandler = new Handler(HeartThread.getInstance().getLooper()){
                @Override
                public void handleMessage(@NonNull Message msg) {
                    if (msg.what == 1){
                        int len = 0;
                        if (playTimestamp == 0){
                            //首次发送
                            playTimestamp = new Date().getTime();
                            len = 0;
                        }else {
                            long time = new Date().getTime();
                            len = (int) (time - playTimestamp)/1000;
                            playTimestamp = time;
                        }

                        if (len<0){
                            return;
                        }

                        //发送打点事件
                        try {
                            Event event = Event.getEvent(EventCode.DATA_GAME_PLAY_TIME, mGameInfo.pkgName, getPadcode());
                            event.setHearttimes(len);
                            MobclickAgent.sendPlayTimeEvent(event);
                        }catch (Exception e){}

                        //心跳10秒
                        if (mPlayTimeHandler!=null){
                            mPlayTimeHandler.sendEmptyMessageDelayed(1, 15*1000);
                        }
                    }
                }
            };
        }

        //开始
        playTimestamp = 0;
        mPlayTimeHandler.sendEmptyMessage(1);
    }

    private synchronized void stopSendPlayTime(){
        if (mPlayTimeHandler!=null){
            if (mPlayTimeHandler.hasMessages(1)){
                mPlayTimeHandler.removeMessages(1);
                mPlayTimeHandler.sendEmptyMessage(1);
            }
            mPlayTimeHandler = null;
        }
    }
}
