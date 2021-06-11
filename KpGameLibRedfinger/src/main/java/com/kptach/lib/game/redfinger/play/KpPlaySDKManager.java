package com.kptach.lib.game.redfinger.play;

import android.app.Activity;
import android.app.Application;

import com.kptach.lib.game.redfinger.BuildConfig;
import com.kptach.lib.game.redfinger.RedGameBoxManager;
import com.kptach.lib.inter.game.APIConstants;
import com.mci.play.MCISdkView;
import com.mci.play.PlayInitListener;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

import com.kptach.lib.game.redfinger.model.DeviceInfo;
import com.kptach.lib.game.redfinger.utils.Logger;
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

//    private File mSoFile;
//    private File mZipFile;

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
                ref.get().mPlayListener.onConnectError(info, APIConstants.ERROR_OTHER);
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

//        @Override
//        public void onRenderedFirstFrame(int i, int i1) {
//            Logger.info(TAG, "onRenderedFirstFrame seetResolution = " + i + ", i1 = " + i1);
//            if (ref!=null && ref.get()!=null && ref.get().mPlayListener!=null){
//                ref.get().mPlayListener.onReceiverBuffer();
//            }
//        }

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

        if (this.isInited){
            Logger.info(TAG, "isInit = true");
            sdkInitSuccess();
            return;
        }

        int logType = RedGameBoxManager.debug ? PlaySdkManager.LOG_DEFAULT : PlaySdkManager.LOG_WARN;

        PlaySdkManager.init(mApplication, null, logType, true, new PlayInitListener() {
            @Override
            public void initCallBack(int code, String msg) {
                if (code == 0){
                    sdkInitSuccess();
                }else {
                    sdkInitFailed(code, msg);
                }
            }
        },"http://socheck.cloud-control.top", "123", "789", Boolean.TRUE, "/sdcard/mci/log/baidu/");

//        PlayMCISdkManager.init(mApplication, null, logType, true);
//        sdkInitSuccess();

//        DynamicLoadLibHelper helper = new DynamicLoadLibHelper(application);
//        helper.loadLib(new DynamicLoadLibHelper.ILoadLibListener() {
//            @Override
//            public void onResult(int code, String msg) {
//                Logger.info(TAG,"init play start");
//                try {
//                    if (code == DynamicLoadLibHelper.LOADLIB_STATUS_SUCCESS){
//                        String soPath = msg;
//
//                        try {
//                            System.load(soPath);
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//
//                        PlayMCISdkManager.setUseLocalSo(false);
//                        int logType = RedGameBoxManager.debug ? PlayMCISdkManager.LOG_DEFAULT : PlayMCISdkManager.LOG_WARN;
//                        PlayMCISdkManager.init(mApplication, soPath, logType, true);
//                        sdkInitSuccess();
//                    }else {
//
//                    }
//
//                } catch (Exception e2) {
//                    Logger.error(TAG,"init failed :" + e2.getMessage());
//                    sdkInitFailed(APIConstants.ERROR_SDK_INIT_ERROR, e2.toString());
//                }
//            }
//        });

    }


//
//    private void loadLibSo(Context context){
//        //验证本地文件是否存在
//        String filePath = FilePathUtils.getLibMciFilePath(context);
//        File file = new File(filePath);
//        if (file.exists()){
//            String md5 = mApplication.getSharedPreferences("RED_FINGER", 0).getString("libMD5", "");
//            if (FileUtils.checkFileMd5(file, md5)){
//                return;
//            }
//        }
//
//        //下载文件
//        downloadLib();
//    }


//
//    private void loadSo(String soPath){
//        Logger.info(TAG,"加载so");
//        try {
////            String soPath = null;
////            if (this.mSoFile!=null && this.mSoFile.exists()){
////                soPath = this.mSoFile.getAbsolutePath();
//////                System.load(soPath);
////            }
//            Logger.info(TAG,"init play start");
//            try {
//                PlayMCISdkManager.init(mApplication, soPath, PlayMCISdkManager.LOG_DEFAULT, true);
//                sdkInitSuccess();
//            } catch (Exception e2) {
//                Logger.error(TAG,"init failed :" + e2.getMessage());
//                sdkInitFailed(APIConstants.ERROR_SDK_INIT_ERROR, e2.toString());
//            }
//        } catch (Throwable th) {
//            th.printStackTrace();
//            sdkInitFailed(APIConstants.ERROR_SDK_INIT_ERROR, "加载so动态库失败！");
//        }
//    }

//
//    private interface NetworkRequest {
//        void faile(int code, String str);
//        void success(String str);
//    }
//
//    private static final String SO_VER = "";
//
//    private void requestSo(Context context){
//        boolean isArm64 = DeviceUtils.is64Bit();
//        String url = "";
//        if (isArm64){
//            url = "https://osspic.kuaipantech.com/android/kpscorp/REDF-LIB/arm64-v8a/libmci.so";
//        }else {
//            url = "https://osspic.kuaipantech.com/android/kpscorp/REDF-LIB/armeabi-v7a/libmci.so";
//        }
//
//        String filePath = FilePathUtils.getLibMciFilePath(context);
//        File file = new File(filePath);
//        if (file.exists()){
//            mSoFile = file;
//            loadSo();
//            return;
//        }
//
//        HttpDownload down = new HttpDownload(url, filePath);
//        down.setCallback(new HttpDownload.ICallback() {
//            @Override
//            public void onSuccess(String file) {
//                Logger.info(TAG, "下载完成");
//                mSoFile = new File(file);
//                if (!mSoFile.exists()){
//
//                }
//                loadSo();
//
////                //验证文件
////                String str = FileUtils.getMD5(new File(file));
////                if (str.equals(newMd5)){
////                    String verFile = SdkPath.getInstance().getUpdateVerFile();
////                    //保存版本文件
////                    FileOutputStream out = null;
////                    try {
////                        Properties properties = new Properties();
////                        properties.put(SdkPath.prop_key_ver, newVer);
////                        properties.put(SdkPath.prop_key_md5, newMd5);
////                        out = new FileOutputStream(verFile, false);
////                        properties.store(out,null);
////                        Logger.info(TAG, "保存版本信息，"+newVer+","+newMd5);
////
////                        //更新stable版本
////                        SdkPath.getInstance().updateStablePath(SdkPath.getInstance().getUpdatePath());
////
////                    }catch (Exception e){
////                        e.printStackTrace();
////                    }finally {
////                        StreamUtil.close(out);
////                    }
////                }else {
////                    Logger.error(TAG, "校验新版本失败," + newMd5 + "," + str);
////                }
////
////                //同步加载sdk
////                if (mDownType == DOWN_TYPE_SYNC){
////                    mHandler.sendEmptyMessage(MSG_LOAD);
////                }
//
//            }
//
//            @Override
//            public void onFailed() {
//                Logger.error(TAG, "下载SDK失败");
//
//                //同步加载sdk
////                if (mDownType == DOWN_TYPE_SYNC){
////                    mHandler.sendEmptyMessage(MSG_LOAD);
////                }
//
//            }
//        });
//        down.start();
//    }
//
//    private class SoVersionResult implements  NetworkRequest {
//
//        @Override
//        public void success(String str) {
//            try {
//                JSONObject jSONObject = new JSONObject(str);
//                if (!jSONObject.has("resultCode") || jSONObject.getInt("resultCode") != 0) {
//                    Logger.error(TAG, "checkLib failed:" + str);
//                    sdkInitFailed(APIConstants.CONNECT_DEVICE_SUCCESS, "检测更新失败");
//                    return;
//                }
//                String string = jSONObject.getString("resultInfo");
//                String string2 = mApplication.getSharedPreferences("RED_FINGER", 0).getString("libMD5", "");
//                Logger.info(TAG, "libMd5:" + string2);
//                Logger.info(TAG, "md5:" + string);
//                if (!string2.equals(string) || !PlaySDKManager.this.mSoFile.exists() || !PlaySDKManager.this.mSoFile.isFile()) {
//                    String a2 = "";
//                    Logger.info(TAG, "os zip dlUrl is " + a2);
//                    PlaySDKManager.this.downloadSo(a2);
//                    return;
//                }
//                loadSo();
//            } catch (Exception e) {
//                Logger.error(TAG, "checkLib exception:" + e.toString());
//                sdkInitFailed(APIConstants.RECONNECT_DEVICE_SUCCESS, "检测更新异常：" + e.toString());
//            }
//        }
//
//        @Override
//        public void faile(int code, String str) {
//            Logger.error(TAG, "checkLib failed:errorCode:" + code + "  msg:" + str);
//            sdkInitFailed(APIConstants.RECONNECT_DEVICE_SUCCESS, "检测更新失败：网络连接失败");
//        }
//    }
//
//    private void downloadSo(String url){
//        Logger.info(TAG, "start download redfinger.so, dlUrl: " + url);
////        requestDownloadFile(string, this.mZipFile.getAbsolutePath(), new SoZipResult());
//    }
//
//    public class SoZipResult implements NetworkRequest {
//        @Override
//        public void success(String str) {
//            try {
//                String md5 = "";
//                PlaySDKManager.this.mApplication.getSharedPreferences("RED_FINGER", 0).edit().putString("libMD5", md5).apply();
//                if (DynamicLoadLibHelper.getInstance(mApplication).zip(mZipFile, mSoFile.getParent())) {
//                    loadSo();
//                } else {
//                    PlaySDKManager.this.sdkInitFailed(APIConstants.ERROR_SDK_INIT_ERROR,  "解压更新包失败");
//                }
//            } catch (Exception e) {
//                PlaySDKManager.this.sdkInitFailed( APIConstants.ERROR_SDK_INIT_ERROR, "解压更新包异常：" + e.toString());
//            }
//        }
//
//        @Override
//        public void faile(int code, String str) {
//            Logger.error(TAG, "download error, errorCode: " + code + ", errorMsg: " + str);
//            sdkInitFailed(APIConstants.ERROR_SDK_INIT_ERROR, "下载更新包失败：" + str);
//        }
//    }


}
