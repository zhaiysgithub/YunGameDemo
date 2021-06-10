package com.kptach.lib.game.bdsdk.play;

import android.app.Activity;
import android.app.Application;

import com.kptach.lib.game.bdsdk.utils.Logger;
import com.kptach.lib.game.bdsdk.BuildConfig;
import com.kptach.lib.game.bdsdk.BDSdkGameBoxManager;
import com.kptach.lib.inter.game.APIConstants;
import com.mci.play.MCISdkView;
import com.mci.play.PlayInitListener;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

import com.kptach.lib.game.bdsdk.model.DeviceInfo;
import com.mci.play.PlaySdkManager;
import com.mci.play.SWDataSourceListener;
import com.mci.play.SWPlayer;

public class KpPlaySDKManager {
    private static String TAG = "PlaySDKManager";

    private static KpPlaySDKManager instance = null;
    private Application mApplication;
    private IPlayInitListener mInitListener;

    private PlaySdkManager mPlayMCISdkManager;

    private IVideoListener mVideoListener;
    private IPlayListener mPlayListener;

    private DeviceInfo mDeviceInfo;
    private String mGamePkg;

    public static long backTime = 60000;
    public static long fontTime = 180000;

    private boolean isStarted = false;

    private boolean isInited;
    public static KpPlaySDKManager getInstance() {
        if (instance == null) {
            instance = new KpPlaySDKManager();
        }
        return instance;
    }

    public void setVideoListener(IVideoListener listener){
        mVideoListener = listener;
    }

    public void setPlayListener(IPlayListener playListener){
        mPlayListener = playListener;
    }

    public void setDeviceParams(DeviceInfo deviceInfo){
        this.mDeviceInfo = deviceInfo;
    }

    public void setGamePkg(String gamePkg){
        this.mGamePkg = gamePkg;
    }

    public synchronized void start(Activity activity,  MCISdkView mciSdkView) {
        if (isStarted) {
            return;
        }
        isStarted = true;

        //初始化SDK
        mPlayMCISdkManager = new PlaySdkManager(activity, false);

        //5、set game parameters
        if (mPlayMCISdkManager.setParams(this.mDeviceInfo.deviceParams, this.mGamePkg, this.mDeviceInfo.apiLevel, this.mDeviceInfo.useSSL, mciSdkView, new InnerPlayListener(this)) != 0) {
            //设置参数错误，返回
            return;
        }

        //6、start game
        if (mPlayMCISdkManager.start() != 0) {
            //开始游戏失败，返回
            return;
        }
    }

    public synchronized void stop(){
        if (!isStarted){
            return;
        }

        isStarted = false;
        if (mPlayMCISdkManager != null){
            mPlayMCISdkManager.stop();
            mPlayMCISdkManager.release();
            mPlayMCISdkManager = null;

            if (mPlayListener != null){
                mPlayListener.onRelease();
            }
        }
    }

    public void resume(){
        if (mPlayMCISdkManager != null){
            mPlayMCISdkManager.resume();
        }
    }

    public void pause(){
        if (mPlayMCISdkManager != null){
            mPlayMCISdkManager.pause();
        }
    }

    public void destory(){
        Logger.info(TAG, "PlaySDKManager destory");
        this.mPlayListener = null;
        this.mVideoListener = null;
        this.mDeviceInfo = null;

    }

    public boolean isReleased() {
        return !isStarted;
    }

    public boolean isAudio(){
        if (mPlayMCISdkManager != null){
            return mPlayMCISdkManager.mIsAudioResume;
        }
        return true;
    }

    public void setAudioSwitch(boolean enable) {
        if (mPlayMCISdkManager != null){
            mPlayMCISdkManager.audioPauseOrResume(enable);
        }
    }

    public void sendPadKey(int action, int keycode) {
        if (mPlayMCISdkManager != null){
            mPlayMCISdkManager.sendKeyEvent(action, keycode);
        }
    }

    public void sendSensorData(int type, float[] data){
        if (mPlayMCISdkManager != null){
            mPlayMCISdkManager.sendSensorData(type, data);
        }
    }

    public void sendAVData(int avType, int frameType, byte[] data){
        if (mPlayMCISdkManager != null){
            mPlayMCISdkManager.sendAVData(avType, frameType, data);
        }
    }

    public void sendLocationData(float longitude, float latitude, float altitude, float floor, float horizontalAccuracy,
                                 float verticalAccuracy, float speed, float direction) {
        if (mPlayMCISdkManager != null){
            mPlayMCISdkManager.sendLocationData(longitude, latitude,altitude,floor,horizontalAccuracy,verticalAccuracy, speed, direction,System.currentTimeMillis() + "");
        }
    }

    public int getVideoLevel(){
        if (mPlayMCISdkManager != null){
            return mPlayMCISdkManager.getVideoLevel();
        }
        return -1;
    }

    public void setVideoLevel(int level){
        if (mPlayMCISdkManager != null){
            mPlayMCISdkManager.setVideoLevel(level);
        }
    }

    public void onNoOpsTimeout(int type, long timeout){
        stop();
        if (mPlayListener != null){
            mPlayListener.onNoOpsTimeout(type, timeout);
        }
    }

    private static class InnerPlayListener extends SWDataSourceListener {
        WeakReference<KpPlaySDKManager> ref = null;

        private InnerPlayListener(KpPlaySDKManager sdkManager){
            ref = new WeakReference<>(sdkManager);
        }

        @Override
        public void onReconnecting(int i) {
            Logger.info(TAG, "onReconnecting = " + i);
        }

        @Override
        public void onConnected() {
            Logger.info(TAG, "onConnected");
            if (ref!=null && ref.get()!=null && ref.get().mPlayListener!=null){
                ref.get().mPlayListener.onConnectSuccess("", APIConstants.CONNECT_DEVICE_SUCCESS);
            }
        }

        @Override
        public void onDisconnected(final int code) {
            Logger.info(TAG, "onDisconnected code = " + code);
            if (ref!=null && ref.get()!=null && ref.get().mPlayListener!=null){
                String info = "";
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("code",code);
                    info = obj.toString();
                }catch (Exception e){
                }
                ref.get().mPlayListener.onConnectError(info, APIConstants.ERROR_DEVICE_OTHER_ERROR);
                ref.get().isStarted = false;
            }
        }

        @Override
        public void onScreenRotation(int i) {
            Logger.info(TAG, "onScreenRotation seetResolution = " + i);
            if (ref!=null && ref.get()!=null && ref.get().mPlayListener!=null){
                ref.get().mPlayListener.onScreenChange(i);
            }
        }

        @Override
        public void onSensorInput(int i, int i1) {
            Logger.info(TAG, "onSensorInput seetResolution = " + i + ", i1 = " + i1);
            if (ref!=null && ref.get()!=null && ref.get().mPlayListener!=null){
                ref.get().mPlayListener.onSensorInput(i, i1);
            }
        }

        @Override
        public void onPlayInfo(String s) {
//            Logger.info(TAG, "onPlayInfo s = " + s);
            //解析延时
            if (ref!=null && ref.get()!=null && ref.get().mPlayListener!=null){
                try {
                    JSONObject obj = new JSONObject(s);
                    int ping = obj.optInt("delayTime");
                    ref.get().mPlayListener.onDelayTime(ping);
                }catch (Exception e){}
            }
        }

        @Override
        public void onRenderedFirstFrame(SWPlayer swPlayer, int i, int i1) {
            Logger.info(TAG, "onRenderedFirstFrame seetResolution = " + i + ", i1 = " + i1);
            if (ref!=null && ref.get()!=null && ref.get().mPlayListener!=null){
                ref.get().mPlayListener.onReceiverBuffer();
            }
        }

        @Override
        public void onScreenRotation(SWPlayer swPlayer, int i) {

        }

        @Override
        public void onVideoSizeChanged(int i, int i1) {
            Logger.info(TAG, "onVideoSizeChanged seetResolution = " + i + ", i1 = " + i1);
            if (ref!=null && ref.get()!=null && ref.get().mVideoListener!=null){
                ref.get().mVideoListener.onResolutionChange(i, i1);
            }
        }

        @Override
        public void onTransparentMsg(int i, int i1, int i2, String s, String s1) {
            if (ref!=null && ref.get()!=null && ref.get().mPlayListener!=null){
                ref.get().mPlayListener.onTransparentMsg(i, s, s1);
            }
        }
    }

    private void sdkInitSuccess(){
        if (!this.isInited){
            this.isInited = true;
        }
        if (this.mInitListener != null){
            this.mInitListener.success();
        }
    }

    private void sdkInitFailed(int code, String err) {
        if (this.mInitListener != null){
            this.mInitListener.failed(code, err);
        }
    }

    public void loadSdk(Application application, IPlayInitListener initListener) {
        Logger.info(TAG, "PlaySDK_VERSION:" + BuildConfig.VERSION_NAME);
        Logger.info(TAG, "PlaySDKManager.init");

        this.mApplication = application;
        this.mInitListener = initListener;

        if (this.isInited) {
            Logger.info(TAG, "isInit = true");
            sdkInitSuccess();
            return;
        }

        int logType = BDSdkGameBoxManager.debug ? PlaySdkManager.LOG_DEFAULT : PlaySdkManager.LOG_WARN;

        PlaySdkManager.init(mApplication, null, logType, true, new PlayInitListener() {
            @Override
            public void initCallBack(int code, String msg) {
                if (code == 0) {
                    sdkInitSuccess();
                } else {
                    sdkInitFailed(code, msg);
                }
            }
        }, "http://socheck.cloud-control.top", "123", "789", Boolean.TRUE, "/sdcard/mci/log/baidu/");

    }
}
