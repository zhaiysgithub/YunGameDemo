package kptech.game.kit.redfinger;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.mci.commonplaysdk.PlayMCISdkManager;

import java.lang.ref.WeakReference;

import kptech.game.kit.APICallback;
import kptech.game.kit.APIConstants;
import kptech.game.kit.DeviceInfo;
import kptech.game.kit.GameBoxManager;
import kptech.game.kit.GameInfo;
import kptech.game.kit.IDeviceControl;
import kptech.game.kit.SensorConstants;
import kptech.game.kit.msg.IMsgReceiver;
import kptech.game.kit.redfinger.fragment.PlayFragment;
import kptech.game.kit.utils.Logger;

public class RedDeviceControl implements IDeviceControl {
    private static final String TAG = RedDeviceControl.class.getSimpleName();
//    private PlayMCISdkManager mPlayMCISdkManager;

//    private IDeviceControl.PlayListener mPlayListener;

    private MainHandler mHandler;

    private Activity mActivity;
    private int mContainer;

    private APICallback<String> mCallback;
    private IDeviceControl.PlayListener mPlayListener;

    private IDeviceControl.SensorSamplerListener mSensorListener;

//    private int mApiLevel = 2;
//    private int mUseSSL = 0;
//    private String mDeviceInfo;
    private DeviceInfo mDeviceInfo;
    private GameInfo mGameInfo;
//    private String mPadCode;
//    private boolean isAudio = false;

//    private String mVideoQuality;

//    private PlayFragment fragment;
//    private String mResolutionRatio;

    private static final int MSG_INIT_SUCC = 1;
    private static final int MSG_START = 2;
//    private static final int MSG_STOP = 3;
    private static final int MSG_ON_APICALLBACK = 4;
    private static class MainHandler extends Handler {
        private WeakReference<RedDeviceControl> ref = null;
        public MainHandler(RedDeviceControl control){
            super(Looper.getMainLooper());
            ref = new WeakReference<>(control);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
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
                        ref.get().mCallback.onAPICallback(text, code);
                    }
                    break;
            }
        }
    }

    private void onApiCallback(String info, int code){
        if (mHandler != null){
            Object[] objs = new Object[]{code, info};
            Message msg = Message.obtain(mHandler, MSG_ON_APICALLBACK, objs);
            mHandler.sendMessage(msg);
        }
    }

    public RedDeviceControl(DeviceInfo devInfo, GameInfo gameInfo){
        this.mDeviceInfo = devInfo;
        this.mGameInfo = gameInfo;
        this.mHandler = new MainHandler(this);
    }

    @Override
    public void startGame(@NonNull Activity activity, int res, @NonNull final APICallback<String> callback) {
        this.mActivity = activity;
        this.mCallback = callback;
        this.mContainer = res;

//        String logFilePath = GameBoxManager.getInstance().getLogFilePath();
//        boolean isArm64 = DeviceUtils.is64Bit();

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
    public void setNoOpsTimeout(long font, long back) {
        if (font > 0 && back > 0) {
            PlaySDKManager.backTime = back * 1000;
            PlaySDKManager.fontTime = font * 1000;
        }
    }

    @Override
    public void switchQuality(@APIConstants.VideoQuality String str) {

        int ordinal = DeviceInfo.VideoQuality.valueOf(str).ordinal();
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

    public static class PlayInitListener implements IPlayInitListener{
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
            //初始化失败
            Logger.info(TAG, String.format("initSDK() fail! code = %d, msg = %s, timeUsed = %s", Integer.valueOf(code), err, this.duration));

            if (ref != null && ref.get() !=  null){
                ref.get().onApiCallback("SDK init failed", APIConstants.ERROR_SDK_INIT_ERROR);
            }
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
            PlaySDKManager.getInstance().setGamePkg(mGameInfo.pkgName);

            if (mActivity != null && !mActivity.isFinishing()) {
                mActivity.getFragmentManager().beginTransaction().add(this.mContainer, new PlayFragment()).commitAllowingStateLoss();
                return;
            }
        }catch (Exception e){
            Logger.error(TAG, "doPlay error: " + e.getMessage());
            if (this.mCallback != null) {
                this.mCallback.onAPICallback("doPlay error: " + e.getMessage(), APIConstants.ERROR_API_CALL_ERROR);
            }
            return;
        }
        if (this.mCallback != null) {
            this.mCallback.onAPICallback("doPlay faile", APIConstants.ERROR_API_CALL_ERROR);
        }
    }

    /**
     * 注册监听，接收硬件采集信息
     *
     * @param listener
     */
    public void registerSensorSamplerListener(final SensorSamplerListener listener){
        this.mSensorListener = listener;
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
    public void sendPadKey(int padKey) {

    }

    @Override
    public boolean isReleased() {
        return PlaySDKManager.getInstance().isReleased();
    }

    @Override
    public void setMessageReceiver(IMsgReceiver receiver) {

    }

    @Override
    public void sendMessage(String msg) {

    }

    @Override
    public void setPlayListener(IDeviceControl.PlayListener listener) {
        this.mPlayListener = listener;
    }

    private void onPingUpdate(int delay) {

        if (this.mPlayListener != null){
            this.mPlayListener.onPingUpdate(delay);
        }
    }

    private void onNoOpsTimeout(int type, long timeout){
        if (this.mPlayListener != null){
            this.mPlayListener.onNoOpsTimeout(type, timeout);
        }
    }

    private void onScreenChange(int screen){
        if (this.mPlayListener != null){
            this.mPlayListener.onScreenChange(screen);
        }
    }


    private static class SDKListener implements IPlayListener, IVideoListener {
        WeakReference<RedDeviceControl> ref = null;
        private SDKListener(RedDeviceControl control){
            ref = new WeakReference<>(control);
        }

        @Override
        public void onRelease() {
            if(ref!=null && ref.get()!=null){
                ref.get().onApiCallback("Release Success", APIConstants.RELEASE_SUCCESS);
            }
        }

        @Override
        public void onConnectSuccess(String str, int i) {
            if(ref!=null && ref.get()!=null){
                ref.get().onApiCallback(str, APIConstants.CONNECT_DEVICE_SUCCESS);
            }
        }

        @Override
        public void onConnectError(String str, int i) {
            if(ref!=null && ref.get()!=null){
                ref.get().onApiCallback(str, APIConstants.ERROR_DEVICE_OTHER_ERROR);
            }
        }

        @Override
        public void onReceiverBuffer() {

        }

        @Override
        public void onScreenCapture(byte[] bArr) {

        }

        @Override
        public void onSensorInput(int i, int i2) {
            if(ref!=null && ref.get()!=null && ref.get().mSensorListener!=null){
                ref.get().mSensorListener.onSensorSamper(i,i2);
            }
        }

        @Override
        public void onTransparentMsg(int i, String str, String str2) {

        }

        @Override
        public void onMsgSendFailed(int i, int i2, String str) {

        }

        @Override
        public void onScreenChange(final int i) {
            ThreadUtils.runUi(new Runnable() {
                @Override
                public void run() {
                    if (ref != null && ref.get() != null){
                        ref.get().onScreenChange(i);
                    }
                }
            });
        }

        @Override
        public void onDelayTime(final int i) {
            ThreadUtils.runUi(new Runnable() {
                @Override
                public void run() {
                    if (ref != null && ref.get() != null){
                        ref.get().onPingUpdate(i);
                    }
                }
            });
        }

        @Override
        public void onNoOpsTimeout(final int type, final long timeout) {
            ThreadUtils.runUi(new Runnable() {
                @Override
                public void run() {
                    if (ref != null && ref.get() != null){
                        ref.get().onNoOpsTimeout(type,timeout);
                    }
                }
            });
        }

        @Override
        public void onResolutionChange(int i, int i2) {

        }

        @Override
        public void onEncodeChange(int i) {

        }

        @Override
        public void onFPSChange(int i) {

        }

        @Override
        public void onBitrateChange(int i) {

        }

        @Override
        public void onQualityChange(int i) {

        }

        @Override
        public void onMaxIdrChange(int i) {

        }

    }

}
