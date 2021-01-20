package kptech.game.kit.redfinger;

import android.app.Activity;
import android.app.Application;
import android.os.Looper;

import com.mci.commonplaysdk.MCISdkView;
import com.mci.commonplaysdk.PlayMCISdkManager;
import com.mci.commonplaysdk.PlaySdkCallbackInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;

import kptech.game.kit.APIConstants;
import kptech.game.kit.BuildConfig;
import kptech.game.kit.DeviceInfo;
import kptech.game.kit.redfinger.fragment.PlayFragment;
import kptech.game.kit.utils.Logger;

public class PlaySDKManager {
    private static String TAG = "PlaySDKManager";

    private static PlaySDKManager instance = null;
    private Application mApplication;
    private IPlayInitListener mInitListener;

    private PlayMCISdkManager mPlayMCISdkManager;

    private IVideoListener mVideoListener;
    private IPlayListener mPlayListener;

    private DeviceInfo mDeviceInfo;
    private String mGamePkg;

    private File mSoFile;
    private File mZipFile;

    public static long backTime = 60000;
    public static long fontTime = 180000;

    private boolean isStarted = false;

    private boolean isInited;
    public static PlaySDKManager getInstance() {
        if (instance == null) {
            instance = new PlaySDKManager();
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
        mPlayMCISdkManager = new PlayMCISdkManager(activity, false);


        JSONObject jo = null;
        try {
            jo = new JSONObject(this.mDeviceInfo.deviceParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        jo.remove("appConfigList");
        try {
            JSONArray jarr = jo.getJSONArray("appConfigList");
            for (int i = 0; i < jarr.length(); i++) {
                JSONObject obj = jarr.getJSONObject(i);
                String level = obj.optString("level");
                if (level.equals("high")){
                    obj.put("bitrate", 4800);
                    obj.put("maxFPS", 30);
                    obj.put("minFPS", 20);
                    obj.put("gameVideoQuality", 0);
                    obj.put("resolutionRatio", "720 X 1280");
                    obj.put("width", 720);
                    obj.put("height", 1280);
                }else if (level.equals("medium")){
                    obj.put("bitrate", 3600);
                    obj.put("maxFPS", 20);
                    obj.put("minFPS", 10);
                    obj.put("gameVideoQuality", 1);
                    obj.put("resolutionRatio", "480 X 850");
                    obj.put("width", 480);
                    obj.put("height", 850);
                }else if (level.equals("low")){
                    obj.put("bitrate", 2400);
                    obj.put("maxFPS", 10);
                    obj.put("minFPS", 5);
                    obj.put("gameVideoQuality", 2);
                    obj.put("resolutionRatio", "360 X 652");
                    obj.put("width", 360);
                    obj.put("height", 652);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        //5、set game parameters
        if (mPlayMCISdkManager.setParams(jo.toString(), this.mGamePkg, this.mDeviceInfo.apiLevel, this.mDeviceInfo.useSSL, mciSdkView, new InnerPlayListener(this)) != 0) {
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
        if (mPlayListener != null){
            mPlayListener.onRelease();
        }
        this.mPlayListener = null;
        this.mVideoListener = null;
        this.mDeviceInfo = null;

    }

    public boolean isReleased() {
        return !isStarted;
    }


    public void setAudioSwitch(boolean enable) {

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

//    public void sendMsg(){
//        if (mPlayMCISdkManager != null){
//            mPlayMCISdkManager.sendTransparentMsgReq("");
//        }
//    }

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

//    public void setResolutionLevel(DeviceInfo.ResolutionLevel level) {
//        Logger.info(TAG, "sendResolutionLevel level:" + level);
//        if (mPlayMCISdkManager == null) {
//            v.b("sendEncodeType mPlayer=null");
//        } else if (DeviceInfo.ResolutionLevel.LEVEL_DEFAULT == level) {
//            mPlayMCISdkManager.setVideoLevel().setPadResolutionLevel(1);
//        } else {
//            mPlayMCISdkManager.setPadResolutionLevel(level.ordinal());
//        }
//    }
//
//    public void setVideoBitrateMode(int i2, boolean z2) {
//        int i3 = 1;
//        if (B != i2) {
//            v.b("setVideoBitrateMode");
//            v.d("PlaySDKManager setVideoBitrateMode quality:" + i2 + "\tnIsAutoChangeMode:" + z2);
//            r = z2;
//            PlayFragment playFragment = this.f;
//            if ( playFragment != null) {
//                playFragment.getDisconnectNumber();
//                this.f.getReconnectNumber();
//            }
//            if (l != null) {
//                try {
//                    if (i2 == g.GRADE_LEVEL_AUTO.ordinal()) {
//                        u = true;
//                        i2 = g.GRADE_LEVEL_ORDINARY.ordinal();
//                    } else {
//                        u = false;
//                    }
//                    Player player = l;
//                    if (!z2) {
//                        i3 = 0;
//                    }
//                    player.setupPlay(i2, i3, x);
//                } catch (Exception e2) {
//                    e2.printStackTrace();
//                    v.b("setVideoBitrateMode error:" + e2.getMessage());
//                }
//            }
//        }
//    }

    public void onNoOpsTimeout(int type, long timeout){
        stop();
        if (mPlayListener != null){
            mPlayListener.onNoOpsTimeout(type, timeout);
        }
    }


    private static class InnerPlayListener implements PlaySdkCallbackInterface {
        WeakReference<PlaySDKManager> ref = null;

        private InnerPlayListener(PlaySDKManager sdkManager){
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
                ref.get().mPlayListener.onConnectSuccess("",APIConstants.CONNECT_DEVICE_SUCCESS);
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
        public void onRenderedFirstFrame(int i, int i1) {
            Logger.info(TAG, "onRenderedFirstFrame seetResolution = " + i + ", i1 = " + i1);
            if (ref!=null && ref.get()!=null && ref.get().mPlayListener!=null){
                ref.get().mPlayListener.onReceiverBuffer();
            }
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

        boolean isArm64 = DeviceUtils.is64Bit();
        loadSo();
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

    private void loadSo(){
        Logger.info(TAG,"加载so");
        try {
            String soPath = null;
            if (this.mSoFile!=null && this.mSoFile.exists()){
                soPath = this.mSoFile.getAbsolutePath();
                System.load(soPath);
            }
            Logger.info(TAG,"init play start");
            try {
                PlayMCISdkManager.init(mApplication, soPath, PlayMCISdkManager.LOG_DEFAULT, true);
                sdkInitSuccess();
            } catch (Exception e2) {
                Logger.error(TAG,"init failed :" + e2.getMessage());
                sdkInitFailed(APIConstants.ERROR_SDK_INIT_ERROR, e2.toString());
            }
        } catch (Throwable th) {
            th.printStackTrace();
            sdkInitFailed(APIConstants.ERROR_SDK_INIT_ERROR, "加载so动态库失败！");
        }
    }

    private interface NetworkRequest {
        void faile(int code, String str);
        void success(String str);
    }

    private void requestSoVersion(){

    }

    private class SoVersionResult implements  NetworkRequest {

        @Override
        public void success(String str) {
            try {
                JSONObject jSONObject = new JSONObject(str);
                if (!jSONObject.has("resultCode") || jSONObject.getInt("resultCode") != 0) {
                    Logger.error(TAG, "checkLib failed:" + str);
                    sdkInitFailed(APIConstants.CONNECT_DEVICE_SUCCESS, "检测更新失败");
                    return;
                }
                String string = jSONObject.getString("resultInfo");
                String string2 = mApplication.getSharedPreferences("RED_FINGER", 0).getString("libMD5", "");
                Logger.info(TAG, "libMd5:" + string2);
                Logger.info(TAG, "md5:" + string);
                if (!string2.equals(string) || !PlaySDKManager.this.mSoFile.exists() || !PlaySDKManager.this.mSoFile.isFile()) {
                    String a2 = "";
                    Logger.info(TAG, "os zip dlUrl is " + a2);
                    PlaySDKManager.this.downloadSo(a2);
                    return;
                }
                loadSo();
            } catch (Exception e) {
                Logger.error(TAG, "checkLib exception:" + e.toString());
                sdkInitFailed(APIConstants.RECONNECT_DEVICE_SUCCESS, "检测更新异常：" + e.toString());
            }
        }

        @Override
        public void faile(int code, String str) {
            Logger.error(TAG, "checkLib failed:errorCode:" + code + "  msg:" + str);
            sdkInitFailed(APIConstants.RECONNECT_DEVICE_SUCCESS, "检测更新失败：网络连接失败");
        }
    }

    private void downloadSo(String url){
        Logger.info(TAG, "start download redfinger.so, dlUrl: " + url);
//        requestDownloadFile(string, this.mZipFile.getAbsolutePath(), new SoZipResult());
    }

    public class SoZipResult implements NetworkRequest {
        @Override
        public void success(String str) {
            try {
                String md5 = "";
                PlaySDKManager.this.mApplication.getSharedPreferences("RED_FINGER", 0).edit()
                        .putString("libMD5", md5).apply();
                if (DynamicLoadLibHelper.getInstance(mApplication).zip(mZipFile, mSoFile.getParent())) {
                    loadSo();
                } else {
                    PlaySDKManager.this.sdkInitFailed(APIConstants.ERROR_SDK_INIT_ERROR,  "解压更新包失败");
                }
            } catch (Exception e) {
                PlaySDKManager.this.sdkInitFailed( APIConstants.ERROR_SDK_INIT_ERROR, "解压更新包异常：" + e.toString());
            }
        }

        @Override
        public void faile(int code, String str) {
            Logger.error(TAG, "download error, errorCode: " + code + ", errorMsg: " + str);
            sdkInitFailed(APIConstants.ERROR_SDK_INIT_ERROR, "下载更新包失败：" + str);
        }
    }


}
