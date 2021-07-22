package kptech.game.kit;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import com.kptach.lib.game.baidu.BdDeviceControl;
import com.kptach.lib.inter.game.IGameCallback;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

import kptech.lib.ad.AdManager;
import kptech.lib.ad.IAdCallback;
import kptech.lib.analytic.DeviceInfo;
import kptech.lib.analytic.Event;
import kptech.lib.analytic.EventCode;
import kptech.lib.analytic.MobclickAgent;
import kptech.lib.constants.SharedKeys;
import kptech.lib.data.RequestClientNotice;
import kptech.game.kit.msg.IMsgReceiver;
import kptech.game.kit.msg.MsgManager;
import kptech.lib.thread.HeartThread;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.MillisecondsDuration;
import kptech.game.kit.utils.ProferencesUtils;

public class DeviceControl implements IDeviceControl{
    private static final String TAG = DeviceControl.class.getSimpleName();

    private com.kptach.lib.inter.game.IDeviceControl mInnerControl;

    private Handler mGameHandler;
    private Activity mActivity;
    private int mGameRes;
    private APICallback<String> mGameStartCallback;

    private GameInfo mGameInfo;
    private JSONObject mDeviceToken;
    private AdManager mAdManager;
    private String mCorpKey;

    private String mPadcode = "";
    private boolean mIsSoundEnable = true;
    private String mPicQuality = "";

    //耗时统计
    private boolean sendTmEvent = false;
    private MillisecondsDuration mTimeDuration;

    protected DeviceControl(com.kptach.lib.inter.game.IDeviceControl control){
        this(control,null);
    }

    protected DeviceControl(com.kptach.lib.inter.game.IDeviceControl control, GameInfo game){
        this.mInnerControl = control;
        this.mGameInfo = game;
        this.mGameHandler = new GameHandler();

        mTimeDuration = new MillisecondsDuration();

        //解析deviceToken
//        parseDeviceToken();
    }

    @Override
    public void startGame(Activity activity, int res, APICallback<String> callback) {

        if (this.mGameInfo == null){
            if (callback!=null){
                callback.onAPICallback("gameInfo is null", APIConstants.ERROR_GAME_INF_EMPTY);
            }
            return;
        }

        this.mActivity = activity;
        this.mGameRes = res;
        this.mGameStartCallback = callback;

        //预加载广告
        mAdManager = (AdManager.adEnable && mGameInfo.showAd == GameInfo.GAME_AD_SHOW_ON)  ? new AdManager(activity) : null;
        if (mAdManager!=null){
            mAdManager.setPackageName(mGameInfo.pkgName);
            mAdManager.prepareAd();
        }

        //连接设备
        MsgManager.start(activity, GameBoxManager.mCorpID, getPadcode(), this.mGameInfo.pkgName, this.mGameInfo.kpGameId, this.mGameInfo.name);

        //同步设备信息
        sendMockDeviceInfo();

        //云存档接口
        sendClientNotice();

        //加载广告
        loadGameAd();

    }

    @Override
    public void stopGame() {

        try {
            MsgManager.stop();
        }catch (Exception e){
            e.printStackTrace();
        }

        mInnerControl.stopGame();

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
    }

    @Override
    public String getPadcode() {
        return mInnerControl.getPadcode();
    }

    @Override
    public boolean isSoundEnable() {
        return mInnerControl.isSoundEnable();
    }

    @Override
    public String getVideoQuality() {
        return mInnerControl.getVideoQuality();
    }

    @Override
    public int[] getVideoSize() {
        return mInnerControl.getVideoSize();
    }

    @Override
    public boolean isReleased() {
        return mInnerControl.isReleased();
    }

    @Override
    public void setNoOpsTimeout(long font, long back) {
        mInnerControl.setNoOpsTimeout(font, back);
    }

    @Override
    public void switchQuality(String level) {
        mInnerControl.switchQuality(level);
    }

    @Override
    public void setAudioSwitch(boolean audioSwitch) {
        mInnerControl.setAudioSwitch(audioSwitch);
    }

    @Override
    public void sendPadKey(int padKey) {
        mInnerControl.sendPadKey(padKey);
    }

    @Override
    public void sendSensorInputData(int sendor, int type, byte[] data) {
        mInnerControl.sendSensorInputData(sendor, type, data);
    }

    @Override
    public void sendSensorInputData(int sendor, int sensorType, float... data) {
        mInnerControl.sendSensorInputData(sendor, sensorType, data);
    }

    @Override
    public void registerSensorSamplerListener(final SensorSamplerListener listener) {
        mInnerControl.registerSensorSamplerListener(new com.kptach.lib.inter.game.IDeviceControl.SensorSamplerListener() {
            @Override
            public void onSensorSamper(int sensor, int state) {
                if (listener != null){
                    listener.onSensorSamper(sensor, state);
                }
            }
        });
    }

    @Override
    public void setPlayListener(final PlayListener listener) {
        mInnerControl.setPlayListener(new com.kptach.lib.inter.game.IDeviceControl.PlayListener() {
            @Override
            public void onPingUpdate(int ping) {
                if (listener != null){
                    listener.onPingUpdate(ping);
                }
            }

            @Override
            public boolean onNoOpsTimeout(int type, long timeout) {
                if (listener != null){
                    return listener.onNoOpsTimeout(type, timeout);
                }
                return false;
            }

            @Override
            public void onScreenChange(int value) {
                if (listener != null){
                    listener.onScreenChange(value);
                }
            }

            @Override
            public void onScreenCapture(byte[] bytes) {

            }

            @Override
            public void onVideoSizeChanged(int var1, int var2) {

            }

            @Override
            public void onControlVideo(int var1, int var2) {

            }
        });
    }

    @Override
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

    @Override
    public void sendMessage(String msg) {
        try {
            MsgManager.sendMessage(msg);
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }
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
        boolean conRecoverCloudDataOpen = ProferencesUtils.getBoolean(mActivity, "conRecoverCloudData", true);
        if (mGameInfo.recoverCloudData == 1 && conRecoverCloudDataOpen){
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
                        .execute(mInnerControl.getPadcode(),mGameInfo.pkgName, DeviceInfo.getUserId(mActivity), mCorpKey);
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
        //百度SDK调用一键新机功能
        if (mInnerControl!=null && mInnerControl.getSdkType() == com.kptach.lib.inter.game.IDeviceControl.SdkType.BD ){
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
                            mInnerControl.mockDeviceInfo();
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
                            mInnerControl.mockDeviceInfo();
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

    public void setCorpKey(String mCorpID) {
        this.mCorpKey = mCorpID;
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
            sendTmEvent = true;
            if (mTimeDuration == null){
                mTimeDuration = new MillisecondsDuration();
            }

            try {
                //发送事件耗时统计
                long useTm = mTimeDuration.duration();
                HashMap data = new HashMap();
                data.put("stime", mTimeDuration.getSavedTime());
                data.put("etime", mTimeDuration.getCurentTime());
                Event event = Event.getTMEvent(EventCode.DATA_TMDATA_VIDEO_START, useTm, data);
                event.setPadcode(getPadcode());
                event.setGamePkg(mGameInfo.pkgName);
                MobclickAgent.sendTMEvent(event);
            }catch (Exception e){}

            mTimeDuration = new MillisecondsDuration();
        }

        if (callback!=null){
            callback.onAPICallback("", APIConstants.GAME_LOADING);
        }


        mInnerControl.startGame(activity, res, new IGameCallback<String>() {
            @Override
            public void onGameCallback(String msg, int code) {
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
                        long useTm = mTimeDuration.duration();
                        HashMap data = new HashMap();
                        data.put("stime", mTimeDuration.getSavedTime());
                        data.put("etime", mTimeDuration.getCurentTime());
                        data.put("state", ret ? "ok" : "failed");
                        data.put("code", code);
                        Event event = Event.getTMEvent(EventCode.DATA_TMDATA_VIDEO_END, useTm, data);
                        event.setPadcode(getPadcode());
                        event.setGamePkg(mGameInfo.pkgName);
                        MobclickAgent.sendTMEvent(event);
                    }catch (Exception e){}

                    mTimeDuration = null;
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

    public String getDeviceInfo(){
        if (mInnerControl == null){
            return "";
        }
        return mInnerControl.getDeviceInfo();
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }
}
