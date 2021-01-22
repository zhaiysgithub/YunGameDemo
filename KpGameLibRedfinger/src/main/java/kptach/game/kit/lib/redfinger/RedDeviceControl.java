package kptach.game.kit.lib.redfinger;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;

import com.mci.commonplaysdk.PlayMCISdkManager;

import java.lang.ref.WeakReference;

import kptach.game.kit.inter.game.APIConstants;
import kptach.game.kit.inter.game.IDeviceControl;
import kptach.game.kit.inter.game.IGameCallback;
import kptach.game.kit.inter.game.SensorConstants;
import kptach.game.kit.lib.redfinger.fragment.PlayFragment;
import kptach.game.kit.lib.redfinger.model.DeviceInfo;
import kptach.game.kit.lib.redfinger.play.IPlayInitListener;
import kptach.game.kit.lib.redfinger.play.PlaySDKManager;
import kptach.game.kit.lib.redfinger.utils.Logger;
import kptach.game.kit.lib.redfinger.utils.MillisecondsDuration;

public class RedDeviceControl implements IDeviceControl {
    private static final String TAG = RedDeviceControl.class.getSimpleName();

    private static final int MSG_INIT_SUCC = 1;
    private static final int MSG_ON_APICALLBACK = 2;

    private MyHandler mHandler;

    private Activity mActivity;
    private int mContainer;
    private IGameCallback<String> mCallback;

    protected IDeviceControl.PlayListener mPlayListener;
    protected IDeviceControl.SensorSamplerListener mSensorListener;

    private DeviceInfo mDeviceInfo;
    private String mPkgName;

    public RedDeviceControl(String devInfo, String pkgName){
        this.mDeviceInfo = DeviceInfo.getInstance(devInfo);
        this.mPkgName = pkgName;
        this.mHandler = new MyHandler(this);
    }

    @Override
    public void startGame(Activity activity, int res, IGameCallback<String> callback) {
        this.mActivity = activity;
        this.mCallback = callback;
        this.mContainer = res;

        IPlayInitListener initListener = new PlayInitListener(this);

        //初始化SDK
        PlaySDKManager.getInstance().loadSdk(activity.getApplication(), initListener);
    }

    @Override
    public void stopGame() {
        PlaySDKManager.getInstance().stop();
    }

    @Override
    public String getPadcode() {
        if (mDeviceInfo != null){
            return mDeviceInfo.padCode;
        }
        return null;
    }

    @Override
    public boolean isSoundEnable() {
        if (mDeviceInfo != null){
            return mDeviceInfo.isAudio;
        }
        return false;
    }

    @Override
    public String getVideoQuality() {
        if (mDeviceInfo != null){
            return mDeviceInfo.videoQuality;
        }
        return "";
    }

    @Override
    public int[] getVideoSize() {
        try {
            if(mDeviceInfo != null && mDeviceInfo.resolutionRatio != null){
                return new int[]{mDeviceInfo.resolutionRatio.width, mDeviceInfo.resolutionRatio.height};
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }

        return new int[]{720, 1280};
    }

    @Override
    public boolean isReleased() {
        return PlaySDKManager.getInstance().isReleased();
    }

    @Override
    public void setNoOpsTimeout(long font, long back) {
        if (font > 0 && back > 0) {
            PlaySDKManager.backTime = back * 1000;
            PlaySDKManager.fontTime = font * 1000;
        }
    }

    @Override
    public void switchQuality(String levelStr) {

        int ordinal = DeviceInfo.VideoQuality.valueOf(levelStr).ordinal();
//        PlaySDKManager.getInstance().setVideoBitrateMode(ordinal, false);

        DeviceInfo.ResolutionLevel level = DeviceInfo.ResolutionLevel.LEVEL_720_1280;
        if (ordinal == DeviceInfo.VideoQuality.GRADE_LEVEL_ORDINARY.ordinal()) {
            level = DeviceInfo.ResolutionLevel.LEVEL_480_856;
        } else if (ordinal == DeviceInfo.VideoQuality.GRADE_LEVEL_LS.ordinal()) {
            level = DeviceInfo.ResolutionLevel.LEVEL_368_652;
        }

        int i = PlaySDKManager.getInstance().getVideoLevel();
        PlaySDKManager.getInstance().setVideoLevel(level.ordinal());
//        PlaySDKManager.getInstance().setResolutionLevel(level);
    }

    @Override
    public void setAudioSwitch(boolean enable) {
        PlaySDKManager.getInstance().setAudioSwitch(enable);
    }

    @Override
    public void sendPadKey(int padKey) {
        if (padKey == APIConstants.PAD_KEY_BACK){
            PlaySDKManager.getInstance().sendPadKey(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
        }else if (padKey == APIConstants.PAD_KEY_HOME){
            PlaySDKManager.getInstance().sendPadKey(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HOME);
        }
    }

    @Override
    public void sendSensorInputData(int sendor, int type, byte[] data) {
        if (sendor == SensorConstants.HARDWARE_ID_MIC){
            PlaySDKManager.getInstance().sendAVData(PlayMCISdkManager.SENSOR_TYPE_AUDIO, type, data);
        }else if (sendor == SensorConstants.HARDWARE_ID_VIDEO_BACK || sendor == SensorConstants.HARDWARE_ID_VIDEO_FRONT){
            PlaySDKManager.getInstance().sendAVData(PlayMCISdkManager.SENSOR_TYPE_VIDEO, type, data);
        }
    }

    @Override
    public void sendSensorInputData(int sendor, int sensorType, float... data) {
        if (sendor == SensorConstants.HARDWARE_ID_LOCATION) {
            PlaySDKManager.getInstance().sendLocationData(data[0],data[1],data[2],data[3],data[4],data[5],data[6],data[7]);
        }else if (sendor == SensorConstants.HARDWARE_ID_ACCELEROMETER){
            PlaySDKManager.getInstance().sendSensorData(PlayMCISdkManager.SENSOR_TYPE_ACCELEROMETER, data);
        }else if (sendor == SensorConstants.HARDWARE_ID_PRESSURE){
            //压力传感器
//            PlaySDKManager.getInstance().sendSensorData(PlayMCISdkManager.SENSOR_TYPE_, data);
        }else if (sendor == SensorConstants.HARDWARE_ID_GYROSCOPE){
            PlaySDKManager.getInstance().sendSensorData(PlayMCISdkManager.SENSOR_TYPE_GYRO, data);
        }else if (sendor == SensorConstants.HARDWARE_ID_MAGNETOMETER){
            PlaySDKManager.getInstance().sendSensorData(PlayMCISdkManager.SENSOR_TYPE_MAGNETOMETER, data);
        }else if (sendor == SensorConstants.HARDWARE_ID_GRAVITY){
            PlaySDKManager.getInstance().sendSensorData(PlayMCISdkManager.SENSOR_TYPE_GRAVITY, data);
        }
    }

    @Override
    public void registerSensorSamplerListener(SensorSamplerListener listener) {
        this.mSensorListener = listener;
    }

    @Override
    public void setPlayListener(PlayListener listener) {
        this.mPlayListener = listener;
    }

    @Override
    public void mockDeviceInfo() {

    }

    public void callback(String text, int code){
        if (mHandler != null){
            Object[] objs = new Object[]{code, text};
            Message msg = Message.obtain(mHandler, MSG_ON_APICALLBACK, objs);
            mHandler.sendMessage(msg);
        }
    }

    private void doPlay(){
        if (!PlaySDKManager.getInstance().isReleased()){
            return;
        }

        Logger.info(TAG, "doPlay");
        try {
            SDKListener listener = new SDKListener(this);
            PlaySDKManager.getInstance().setPlayListener(listener);
            PlaySDKManager.getInstance().setVideoListener(listener);

            PlaySDKManager.getInstance().setDeviceParams(mDeviceInfo);
            PlaySDKManager.getInstance().setGamePkg(mPkgName);

            if (mActivity != null && !mActivity.isFinishing()) {
                mActivity.getFragmentManager().beginTransaction().add(this.mContainer, new PlayFragment()).commitAllowingStateLoss();
                return;
            }
        }catch (Exception e){
            Logger.error(TAG, "doPlay error: " + e.getMessage());
            callback("doPlay error: " + e.getMessage(), APIConstants.ERROR_API_CALL_ERROR);
            return;
        }
        callback("doPlay faile", APIConstants.ERROR_API_CALL_ERROR);
    }

    public static class PlayInitListener implements IPlayInitListener {
        MillisecondsDuration duration;

        private WeakReference<RedDeviceControl> ref = null;
        public PlayInitListener(RedDeviceControl control){
            ref = new WeakReference<>(control);
            duration = new MillisecondsDuration();
        }

        @Override
        public void success() {
            Logger.info(TAG, "initSDK() success! timeUsed = " + this.duration);
            //初始化成功
            if (ref != null && ref.get() != null && ref.get().mHandler!=null){
                ref.get().mHandler.sendEmptyMessage(MSG_INIT_SUCC);
            }
        }

        @Override
        public void failed(int code, String err) {
            Logger.info(TAG, String.format("initSDK() fail! code = %d, msg = %s, timeUsed = %s", Integer.valueOf(code), err, this.duration));
            //初始化失败
            if (ref != null && ref.get() != null){
                ref.get().callback(err, code);
            }
        }

    }

    private static class MyHandler extends Handler {
        private WeakReference<RedDeviceControl> ref = null;
        public MyHandler(RedDeviceControl control){
            super(Looper.getMainLooper());
            ref = new WeakReference<>(control);
        }

        @Override
        public void handleMessage(Message msg) {
            if (ref == null || ref.get() == null){
                return;
            }

            switch (msg.what) {
                case MSG_INIT_SUCC:
                    ref.get().doPlay();
                    break;
                case MSG_ON_APICALLBACK:
                    if (ref.get().mCallback != null){
                        Object[] objs = (Object[]) msg.obj;
                        int code = (int) objs[0];
                        String text = (String) objs[1];
                        ref.get().mCallback.onGameCallback(text, code);
                    }
                    break;
            }
        }
    }


}
