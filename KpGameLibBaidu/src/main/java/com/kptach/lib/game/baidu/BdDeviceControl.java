package com.kptach.lib.game.baidu;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import com.yd.yunapp.gameboxlib.DeviceControl;
import com.yd.yunapp.gameboxlib.GamePadKey;

import org.json.JSONObject;

import com.kptach.lib.inter.game.APIConstants;
import com.kptach.lib.inter.game.IDeviceControl;
import com.kptach.lib.inter.game.IGameCallback;
import com.kptach.lib.game.baidu.utils.Logger;

public class BdDeviceControl implements IDeviceControl {
    private static final String TAG = BdDeviceControl.class.getSimpleName();

    private com.yd.yunapp.gameboxlib.DeviceControl mDeviceControl;

    private JSONObject mDeviceToken;
    private String mPadcode = "";
    private boolean mIsSoundEnable = true;
    private String mPicQuality = "";
    private Handler mainHandler;

    protected BdDeviceControl(com.yd.yunapp.gameboxlib.DeviceControl control){
        this.mDeviceControl = control;
        if (mDeviceControl != null){
            mDeviceControl.openRedLog(true);
        }
        mainHandler = new Handler(Looper.getMainLooper());
        parseDeviceToken();
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
//                switchQuality(getVideoQuality());
//                if (mDeviceToken != null && mDeviceToken.has("picQuality")) {
//                    String str = mDeviceToken.getString("picQuality");
//                    mPicQuality = str;
//                }

            }

        }catch (Exception e){
            Logger.error(TAG, "parseDeviceToken, error:"+e.getMessage());
        }
    }

    @Override
    public void startGame(Activity activity, int res, final IGameCallback<String> callback) {
        if (mDeviceControl == null){
            if (callback != null){
                callback.onGameCallback("device control error", -200001 );
            }
        }
        mDeviceControl.startGame(activity, res, new com.yd.yunapp.gameboxlib.APICallback<String>(){
            @Override
            public void onAPICallback(final String s, final int i) {
                if (mainHandler != null){
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null){
                                callback.onGameCallback(s, i);
                            }
                            if (i == com.yd.yunapp.gameboxlib.APIConstants.APPLY_DEVICE_SUCCESS){
                                switchQuality(getVideoQuality());
                            }

                            if (i == APIConstants.RELEASE_SUCCESS){
                                mainHandler.removeCallbacksAndMessages(null);
                                mainHandler = null;
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void stopGame() {
        if (mDeviceControl != null) {
            mDeviceControl.stopGame();
        }
    }

    @Override
    public String getPadcode() {
        return mPadcode;
    }

    @Override
    public boolean isSoundEnable() {
        return mIsSoundEnable;
    }

    @Override
    public String getVideoQuality() {
        String pic = "";
        if (mDeviceControl != null){
            int level = mDeviceControl.getDefaultGameLevel();
            switch (level){
                case com.yd.yunapp.gameboxlib.APIConstants.DEVICE_VIDEO_QUALITY_AUTO:
                    pic =  APIConstants.DEVICE_VIDEO_QUALITY_AUTO;
                    break;
                case com.yd.yunapp.gameboxlib.APIConstants.DEVICE_VIDEO_QUALITY_HD:
                    pic = APIConstants.DEVICE_VIDEO_QUALITY_HD;
                    break;
                case com.yd.yunapp.gameboxlib.APIConstants.DEVICE_VIDEO_QUALITY_ORDINARY:
                    pic = APIConstants.DEVICE_VIDEO_QUALITY_ORDINARY;
                    break;
                case com.yd.yunapp.gameboxlib.APIConstants.DEVICE_VIDEO_QUALITY_LS:
                    pic = APIConstants.DEVICE_VIDEO_QUALITY_LS;
                    break;
            }
        }
        return pic;
    }

    @Override
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

    @Override
    public boolean isReleased() {
        if (mDeviceControl == null){
            return true;
        }
        return mDeviceControl.isReleased();
    }

    @Override
    public void setNoOpsTimeout(long font, long back) {
        if (mDeviceControl != null){
            mDeviceControl.setNoOpsTimeout(font, back);
        }
    }

    @Override
    public void switchQuality(String level) {
        if (mPicQuality!=null && mPicQuality.equals(level)){
            return;
        }
        mPicQuality = level;
        if (mDeviceControl != null){
            if (level.equals(APIConstants.DEVICE_VIDEO_QUALITY_AUTO)){
                mDeviceControl.switchQuality(com.yd.yunapp.gameboxlib.APIConstants.DEVICE_VIDEO_QUALITY_AUTO);
            }else if (level.equals(APIConstants.DEVICE_VIDEO_QUALITY_HD)){
                mDeviceControl.switchQuality(com.yd.yunapp.gameboxlib.APIConstants.DEVICE_VIDEO_QUALITY_HD);
            }else if (level.equals(APIConstants.DEVICE_VIDEO_QUALITY_LS)){
                mDeviceControl.switchQuality(com.yd.yunapp.gameboxlib.APIConstants.DEVICE_VIDEO_QUALITY_LS);
            }else if (level.equals(APIConstants.DEVICE_VIDEO_QUALITY_ORDINARY)){
                mDeviceControl.switchQuality(com.yd.yunapp.gameboxlib.APIConstants.DEVICE_VIDEO_QUALITY_ORDINARY);
            }
        }
    }

    @Override
    public void setAudioSwitch(boolean audioSwitch) {
        mIsSoundEnable = audioSwitch;
        if (mDeviceControl != null) {
            mDeviceControl.setAudioSwitch(audioSwitch);
        }
    }

    @Override
    public void sendPadKey(int key) {
        try {
            if (mDeviceControl != null){
                if (key == APIConstants.PAD_KEY_BACK){
                    mDeviceControl.setGamePadKey(GamePadKey.GAMEPAD_BACK);
                }else if (key == APIConstants.PAD_KEY_HOME){
                    mDeviceControl.setGamePadKey(GamePadKey.GAMEPAD_HOME);
                }
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }
    }

    @Override
    public void sendSensorInputData(int sendor, int type, byte[] data) {
        try {
            if (mDeviceControl != null) {
                mDeviceControl.sendSensorInputData(sendor, type, data);
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }
    }

    @Override
    public void sendSensorInputData(int sendor, int sensorType, float... data) {
        try {
            if (mDeviceControl != null){
                mDeviceControl.sendSensorInputData(sendor,sensorType,data);
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }
    }

    @Override
    public void registerSensorSamplerListener(final SensorSamplerListener listener) {
        try {
            if (mDeviceControl != null){
                mDeviceControl.registerSensorSamplerListener(new DeviceControl.SensorSamplerListener() {
                    @Override
                    public void onSensorSamper(int i, int i1) {
                        if (listener != null){
                            listener.onSensorSamper(i, i1);
                        }
                    }
                });
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }
    }

    @Override
    public void setPlayListener(final PlayListener listener) {
        if (mDeviceControl == null){
            return;
        }
        mDeviceControl.setPlayListener(new DeviceControl.PlayListener() {
            @Override
            public void onPingUpdate(int i) {
                try {
                    Logger.info(TAG,"onPingUpdate:" + i);
                    if (listener != null){
                        listener.onPingUpdate(i);
                    }
                }catch (Exception e){
                    Logger.error(TAG, e.getMessage());
                }

            }

            @Override
            public boolean onNoOpsTimeout(int i, long l) {
                try {
                    if (listener != null){
                        return listener.onNoOpsTimeout(i, l);
                    }
                }catch (Exception e){
                    Logger.error(TAG, e.getMessage());
                }
                return true;
            }

            @Override
            public void onVideoSizeChanged(int i, int i1) {

            }

            @Override
            public void onControlVideo(int i, int i1) {

            }

            @Override
            public void onPlayInfo(String s) {
                Logger.info(TAG,"onPlayInfo:" + s);
            }

//            @Override
//            public void onScreenChange(int i) {
//                if (listener != null){
//                    listener.onScreenChange(i);
//                }
//            }
//
//            @Override
//            public void onScreenCapture(byte[] bytes) {
//                if (listener != null){
//                    listener.onScreenCapture(bytes);
//                }
//            }
        });
    }

    @Override
    public void mockDeviceInfo() {
        try {
            if (mDeviceControl != null){
                mDeviceControl.mockDeviceInfo();
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }
    }

    @Override
    public SdkType getSdkType() {
        return SdkType.BD;
    }

    @Override
    public String getDeviceInfo() {
        if (mDeviceControl != null){
            return mDeviceControl.getDeviceToken();
        }
        return "";
    }


    public void onResume(){
        Logger.info(TAG,"onResume");
        if (mDeviceControl != null){
            mDeviceControl.resume();
        }
    }

    public void onPause(){
        Logger.info(TAG,"onPause");
        if (mDeviceControl != null){
            mDeviceControl.pause();
        }
    }

}
