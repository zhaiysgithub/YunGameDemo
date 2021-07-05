package com.kptach.lib.game.bdsdk;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.kptach.lib.game.bdsdk.fragment.PlayFragment;
import com.kptach.lib.game.bdsdk.model.DeviceInfo;
import com.kptach.lib.game.bdsdk.play.IPlayInitListener;
import com.kptach.lib.game.bdsdk.play.KpPlaySDKManager;
import com.kptach.lib.game.bdsdk.utils.Logger;
import com.kptach.lib.game.bdsdk.utils.MillisecondsDuration;
import com.kptach.lib.inter.game.APIConstants;
import com.kptach.lib.inter.game.IDeviceControl;
import com.kptach.lib.inter.game.IGameCallback;
import com.kptach.lib.inter.game.SensorConstants;
import com.mci.play.PlaySdkManager;

import java.lang.ref.WeakReference;

public class BDSdkDeviceControl implements IDeviceControl {
    private static final String TAG = BDSdkDeviceControl.class.getSimpleName();

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
    private int videoWidth = 0;
    private int videoHeight = 0;

    public BDSdkDeviceControl(String devInfo, String pkgName, String deviceId){
        this.mDeviceInfo = DeviceInfo.getInstance(devInfo, deviceId);
        this.mPkgName = pkgName;
        this.mHandler = new MyHandler(this);
    }

    @Override
    public void startGame(Activity activity, int res, IGameCallback<String> callback) {
        if( activity == null || activity.isFinishing()){
            return;
        }
        this.mActivity = activity;
        this.mCallback = callback;
        this.mContainer = res;

        IPlayInitListener initListener = new PlayInitListener(this);

        //初始化SDK
        KpPlaySDKManager.getInstance().loadSdk(activity.getApplication(), initListener);
    }

    @Override
    public void stopGame() {
        KpPlaySDKManager.getInstance().stop();
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
    public void setAudioSwitch(boolean enable) {
        KpPlaySDKManager.getInstance().setAudioSwitch(enable);
        if (mDeviceInfo != null){
            mDeviceInfo.isAudio = enable;
        }
    }

    @Override
    public String getVideoQuality() {
        int level = KpPlaySDKManager.getInstance().getVideoLevel();
        String str = null;
        switch (level){
            case PlaySdkManager.VIDEO_LEVEL_AUTO:
                str = APIConstants.DEVICE_VIDEO_QUALITY_AUTO;
                break;
            case 1:
                str = APIConstants.DEVICE_VIDEO_QUALITY_HD;
                break;
            case 2:
                str = APIConstants.DEVICE_VIDEO_QUALITY_ORDINARY;
                break;
            case 3:
                str = APIConstants.DEVICE_VIDEO_QUALITY_LS;
                break;

        }
        if (str == null){
            return  APIConstants.DEVICE_VIDEO_QUALITY_AUTO;
        }

        return str;
    }

    @Override
    public int[] getVideoSize() {
        try {
            if(mDeviceInfo != null && mDeviceInfo.resolutionRatio != null){
                videoWidth = mDeviceInfo.resolutionRatio.width;
                videoHeight = mDeviceInfo.resolutionRatio.height;
                return new int[]{videoWidth, videoHeight};
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }
        videoWidth = 720;
        videoHeight = 1280;
        return new int[]{videoWidth, videoHeight};
    }

    @Override
    public boolean isReleased() {
        return KpPlaySDKManager.getInstance().isReleased();
    }

    @Override
    public void setNoOpsTimeout(long font, long back) {
        if (font > 0 && back > 0) {
            KpPlaySDKManager.backTime = back * 1000;
            KpPlaySDKManager.fontTime = font * 1000;
        }
    }

    @Override
    public void switchQuality(String level) {
        if (level == null){
            return;
        }
        if (level.equals(APIConstants.DEVICE_VIDEO_QUALITY_AUTO)){
            KpPlaySDKManager.getInstance().setVideoLevel(PlaySdkManager.VIDEO_LEVEL_AUTO);
        }else if (level.equals(APIConstants.DEVICE_VIDEO_QUALITY_HD)){
            KpPlaySDKManager.getInstance().setVideoLevel(1);
        }else if (level.equals(APIConstants.DEVICE_VIDEO_QUALITY_ORDINARY)){
            KpPlaySDKManager.getInstance().setVideoLevel(2);
        } else if (level.equals(APIConstants.DEVICE_VIDEO_QUALITY_LS)){
            KpPlaySDKManager.getInstance().setVideoLevel(3);
        }
    }

    @Override
    public void sendPadKey(int padKey) {
        if (padKey == APIConstants.PAD_KEY_BACK){
            KpPlaySDKManager.getInstance().sendPadKey(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
        }else if (padKey == APIConstants.PAD_KEY_HOME){
            KpPlaySDKManager.getInstance().sendPadKey(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HOME);
        }
    }

    @Override
    public void sendSensorInputData(int sendor, int type, byte[] data) {
        if (sendor == SensorConstants.HARDWARE_ID_MIC){
            KpPlaySDKManager.getInstance().sendAVData(PlaySdkManager.SENSOR_TYPE_AUDIO, type, data);
        }else if (sendor == SensorConstants.HARDWARE_ID_VIDEO_BACK || sendor == SensorConstants.HARDWARE_ID_VIDEO_FRONT){
            KpPlaySDKManager.getInstance().sendAVData(PlaySdkManager.SENSOR_TYPE_BACK_VIDEO, type, data);
        }
    }

    @Override
    public void sendSensorInputData(int sendor, int sensorType, float... data) {
        if (sendor == SensorConstants.HARDWARE_ID_LOCATION) {
            KpPlaySDKManager.getInstance().sendLocationData(data[0],data[1],data[2],data[3],data[4],data[5],data[6],data[7]);
        }else if (sendor == SensorConstants.HARDWARE_ID_ACCELEROMETER){
            KpPlaySDKManager.getInstance().sendSensorData(PlaySdkManager.SENSOR_TYPE_ACCELEROMETER, data);
        }else if (sendor == SensorConstants.HARDWARE_ID_PRESSURE){
            //压力传感器
//            PlaySDKManager.getInstance().sendSensorData(PlayMCISdkManager.SENSOR_TYPE_, data);
        }else if (sendor == SensorConstants.HARDWARE_ID_GYROSCOPE){
            KpPlaySDKManager.getInstance().sendSensorData(PlaySdkManager.SENSOR_TYPE_GYRO, data);
        }else if (sendor == SensorConstants.HARDWARE_ID_MAGNETOMETER){
            KpPlaySDKManager.getInstance().sendSensorData(PlaySdkManager.SENSOR_TYPE_MAGNETOMETER, data);
        }else if (sendor == SensorConstants.HARDWARE_ID_GRAVITY){
            KpPlaySDKManager.getInstance().sendSensorData(PlaySdkManager.SENSOR_TYPE_GRAVITY, data);
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

    @Override
    public SdkType getSdkType() {
        return SdkType.BD3;
    }

    @Override
    public String getDeviceInfo() {
        return mDeviceInfo.deviceParams;
    }

    @Override
    public void setVideoDisplayMode(boolean isFill) {
        try{
            if (mActivity != null && mContainer > 0){
                View containerView = mActivity.findViewById(mContainer);
                //屏幕尺寸
                int sw = containerView.getWidth();
                int sh = containerView.getHeight();

                if (sw <= 0 || sh <= 0) {
                    DisplayMetrics displayMetrics = mActivity.getResources().getDisplayMetrics();
                    sw = displayMetrics.widthPixels;
                    sh = displayMetrics.heightPixels;
                }

                //处理横竖屏
                int screenWidth = sh < sw ? sh : sw;
                int screenHeight = sh < sw ? sw : sh;
                int vw = videoHeight < videoWidth ? videoHeight : videoWidth;
                int vh = videoHeight < videoWidth ? videoWidth : videoHeight;

                //宽高比
                float videoScale = (float) vw / (float) vh;
//                float screenScale = (float) screenWidth / (float) screenHeight;

                float widthScale = (float) vw / (float) screenWidth;
                float heightScale = (float) vh / (float) screenHeight;
                int resizeWidth = 0;
                int resizeHeight = 0;
                if (widthScale < heightScale) {
                    resizeHeight = screenHeight;
                    resizeWidth = (int) (screenHeight * videoScale);
                } else {
                    resizeWidth = screenWidth;
                    resizeHeight = (int) (screenWidth / videoScale);
                }
                if (isFill){
                    //全屏
                    ViewGroup.LayoutParams lp = containerView.getLayoutParams();
                    lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    containerView.setLayoutParams(lp);
                }else {
                    Configuration mConfiguration = mActivity.getResources().getConfiguration(); //获取设置的配置信息
                    int ori = mConfiguration.orientation; //获取屏幕方向
                    if (resizeWidth > 0 && resizeHeight > 0) {
                        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                            //横屏
                            ViewGroup.LayoutParams lp = containerView.getLayoutParams();
                            lp.width = resizeHeight;
                            lp.height = resizeWidth;
                            containerView.setLayoutParams(lp);
                        } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
                            //竖屏
                            ViewGroup.LayoutParams lp = containerView.getLayoutParams();
                            lp.width = resizeWidth;
                            lp.height = resizeHeight;
                            containerView.setLayoutParams(lp);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void callback(String text, int code){
        if (mHandler != null){
            Object[] objs = new Object[]{code, text};
            Message msg = Message.obtain(mHandler, MSG_ON_APICALLBACK, objs);
            mHandler.sendMessage(msg);
        }
    }

    private void doPlay(){
        if (!KpPlaySDKManager.getInstance().isReleased()){
            return;
        }

        Logger.info(TAG, "doPlay");
        try {
            SDKListener listener = new SDKListener(this);
            KpPlaySDKManager.getInstance().setPlayListener(listener);
            KpPlaySDKManager.getInstance().setVideoListener(listener);

            KpPlaySDKManager.getInstance().setDeviceParams(mDeviceInfo);
            KpPlaySDKManager.getInstance().setGamePkg(mPkgName);

            if (mActivity != null && !mActivity.isFinishing()) {
                mActivity.getFragmentManager().beginTransaction().add(this.mContainer, new PlayFragment()).commitAllowingStateLoss();
                return;
            }
        }catch (Exception e){
            Logger.error(TAG, "doPlay error: " + e.getMessage());
            callback("doPlay error: " + e.getMessage(), APIConstants.ERROR_CALL_API);
            return;
        }
        callback("doPlay faile", APIConstants.ERROR_CALL_API);
    }

    public static class PlayInitListener implements IPlayInitListener {
        MillisecondsDuration duration;

        private WeakReference<BDSdkDeviceControl> ref = null;
        public PlayInitListener(BDSdkDeviceControl control){
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
        private WeakReference<BDSdkDeviceControl> ref = null;
        public MyHandler(BDSdkDeviceControl control){
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
