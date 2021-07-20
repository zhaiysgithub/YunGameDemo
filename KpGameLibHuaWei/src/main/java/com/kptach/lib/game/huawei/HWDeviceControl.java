package com.kptach.lib.game.huawei;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import com.huawei.cloudgame.api.CloudGameManager;
import com.huawei.cloudgame.api.CloudGameParas;
import com.kptach.lib.inter.game.APIConstants;
import com.kptach.lib.inter.game.IDeviceControl;
import com.kptach.lib.inter.game.IGameCallback;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class HWDeviceControl implements IDeviceControl {

//    private static final String TAG = HWDeviceControl.class.getSimpleName();
    //声音是否开启
    private boolean iSoundUnMute;
    //设置分辨率
    private CloudGameParas.Resolution videoResolution = CloudGameParas.Resolution.DISPLAY_1080P;
    //分辨率
    private String resolutionQuality = "";
    //设备deviceID
    private String hwDeviceId;
    private int hwDirection;
    private final HashMap<String, String> sdkParams = new HashMap<>();
    private boolean sdkIsRelease;
    private IGameCallback<String> mCallback;
    private PlayListener mPlayListener;
    private ViewGroup mViewgroup;
    private Activity mActivity;
    private String gameTimeout;
    private String backNoTouchTimeOut;
    private String availablePlayTime;
    private final int []screenSize = new int[2];

    public HWDeviceControl(HashMap<String, Object> params) {
        //默认取消静音
        CloudGameManager.CreateCloudGameInstance().unmute();
        iSoundUnMute = true;
        sdkParams.clear();
        parseParams(params);
        registerListener();
    }

    private void parseParams(HashMap<String, Object> params) {
        try {
            if (params.containsKey("deviceid")) {
                hwDeviceId = (String) params.get("deviceid");
            }
            if (params.containsKey("direction")){
                Object direc = params.get("direction");
                if (direc instanceof Integer){
                    hwDirection = (int) direc;
                }
            }
            if (params.containsKey("resource")) {
                Object resource = params.get("resource");
                if (resource != null) {
                    String deviceResource = resource.toString();
                    JSONObject json = new JSONObject(deviceResource);
                    Iterator<String> keys = json.keys();
                    while (keys.hasNext()){
                        String nextKey = keys.next();
                        Object value = json.get(nextKey);
                        if (value instanceof String){
                            sdkParams.put(nextKey, (String) value);
                        }else {
                            sdkParams.put(nextKey, String.valueOf(value));
                        }
                    }
                    sdkParams.put("launcher_activity", "");
                    if (!sdkParams.containsKey("touch_timeout")){
                        //前台无操作超时的时长，单位是秒 5min
                        sdkParams.put("touch_timeout", "300");
                        backNoTouchTimeOut = "300";
                    }else {
                        backNoTouchTimeOut = sdkParams.get("touch_timeout");
                    }

                    //备用参数
                    sdkParams.put("user_id", "");

                    if (sdkParams.containsKey("game_timeout")){
                        gameTimeout = sdkParams.get("game_timeout");
                    }
                    if (sdkParams.containsKey("available_playtime")){
                        availablePlayTime = sdkParams.get("available_playtime");
                    }
                }
//                String detailStr = getDetailStr();
//                HWCloudGameUtils.info(TAG,detailStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void startGame(Activity activity, int res, IGameCallback<String> callback) {
        if (callback == null){
            return;
        }
        try {
            mCallback = callback;
            ViewGroup viewGroup = activity.findViewById(res);
            mViewgroup = viewGroup;
            mActivity = activity;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            int height = displayMetrics.heightPixels;
            screenSize[0] = width;
            screenSize[1] = height;
            setResolution(videoResolution);
            setVideoDisplayMode(true);
            CloudGameManager.CreateCloudGameInstance().startCloudApp(activity, viewGroup, sdkParams);
//            callback.onGameCallback("startCloudApp", APIConstants.CONNECT_DEVICE_SUCCESS);
            callback.onGameCallback("startCloudApp", APIConstants.GAME_LOADING);
        } catch (Exception e) {
            e.printStackTrace();
            stopGame();
            callback.onGameCallback(e.getMessage(), APIConstants.ERROR_CONNECT_DEVICE);
        }

    }

    private void setMediaConfig(CloudGameParas.Resolution resolution){
        HashMap<String, String> mediaConfigMap = new HashMap<>();
        mediaConfigMap.put("physical_width", Integer.toString(screenSize[0]));
        mediaConfigMap.put("physical_height", Integer.toString(screenSize[1]));
        mediaConfigMap.put("frame_rate", Integer.toString(30));
        if (resolution == CloudGameParas.Resolution.DISPLAY_1080P){
            mediaConfigMap.put("bitrate", Integer.toString(10000000));
        }else if(resolution == CloudGameParas.Resolution.DISPLAY_720P){
            mediaConfigMap.put("bitrate", Integer.toString(3000000));
        }else if(resolution == CloudGameParas.Resolution.DISPLAY_540P){
            mediaConfigMap.put("bitrate", Integer.toString(1800000));
        }
        CloudGameManager.CreateCloudGameInstance().setMediaConfig(mediaConfigMap);
    }


    @Override
    public void stopGame() {
        try{
            if (!sdkIsRelease){
                sdkIsRelease = true;
                CloudGameManager.CreateCloudGameInstance().exitCloudApp();
            }
            CloudGameManager.CreateCloudGameInstance().deinit();
            if (mCallback != null){
                mCallback.onGameCallback("game release success" , APIConstants.RELEASE_SUCCESS);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public String getPadcode() {
        return hwDeviceId;
    }

    @Override
    public boolean isSoundEnable() {
        return iSoundUnMute;
    }

    @Override
    public String getVideoQuality() {
        String videoQuality;
        if (videoResolution == CloudGameParas.Resolution.DISPLAY_1080P) {
            videoQuality = APIConstants.DEVICE_VIDEO_QUALITY_HD;
        } else if (videoResolution == CloudGameParas.Resolution.DISPLAY_720P) {
            videoQuality = APIConstants.DEVICE_VIDEO_QUALITY_ORDINARY;
        } else if (videoResolution == CloudGameParas.Resolution.DISPLAY_540P ||
                videoResolution == CloudGameParas.Resolution.DISPLAY_480P) {
            videoQuality = APIConstants.DEVICE_VIDEO_QUALITY_LS;
        } else {
            videoQuality = APIConstants.DEVICE_VIDEO_QUALITY_AUTO;
        }
        resolutionQuality = videoQuality;
        return videoQuality;
    }

    @Override
    public int[] getVideoSize() {
        return screenSize;
    }

    @Override
    public boolean isReleased() {
        return sdkIsRelease;
    }

    @Override
    public void setNoOpsTimeout(long font, long back) {

    }

    @Override
    public void switchQuality(String level) {
        //切换分辨率
        if (resolutionQuality.equals(level)) {
            return;
        }
        resolutionQuality = level;
        switch (level) {
            case APIConstants.DEVICE_VIDEO_QUALITY_HD:
                setResolution(CloudGameParas.Resolution.DISPLAY_1080P);
                break;
            case APIConstants.DEVICE_VIDEO_QUALITY_LS:
                setResolution(CloudGameParas.Resolution.DISPLAY_540P);
                break;
            case APIConstants.DEVICE_VIDEO_QUALITY_ORDINARY:
                setResolution(CloudGameParas.Resolution.DISPLAY_720P);
                break;
            default:
                setResolution(videoResolution);
                break;
        }
    }

    @Override
    public void setAudioSwitch(boolean b) {
        if (iSoundUnMute != b) {
            iSoundUnMute = b;
            if (b) {
                CloudGameManager.CreateCloudGameInstance().unmute(); //取消静音
            } else {
                CloudGameManager.CreateCloudGameInstance().mute(); //静音
            }
        }
    }

    @Override
    public void sendPadKey(int key) {
        //控制云设备按键事件
    }

    @Override
    public void sendSensorInputData(int i, int i1, byte[] bytes) {

    }

    @Override
    public void sendSensorInputData(int i, int i1, float... floats) {

    }

    @Override
    public void registerSensorSamplerListener(SensorSamplerListener sensorSamplerListener) {

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
        return SdkType.HW;
    }

    @Override
    public String getDeviceInfo() {
        return "";
    }

    public void setResolution(CloudGameParas.Resolution resolution) {
        this.videoResolution = resolution;
        HWCloudGameUtils.info("setResolution:" + resolution.name());
        setMediaConfig(videoResolution);
        CloudGameManager.CreateCloudGameInstance().setResolution(resolution);
    }

    /*public String getHSdkVersion() {
        return CloudGameManager.CreateCloudGameInstance().getVersion();
    }

    public String getDetailStr(){
        String detailString = CloudGameManager.CreateCloudGameInstance().getDetailString();
        HWCloudGameUtils.info("detailString", detailString);
        return detailString;
    }*/

    @Override
    public void setVideoDisplayMode(boolean isFill) {
        CloudGameManager.CreateCloudGameInstance().setDisplayMode(isFill ? CloudGameParas.DisplayMode.DISPLAY_MODE_FILL : CloudGameParas.DisplayMode.DISPLAY_MODE_FIT);

    }

    private void registerListener(){
        //注册状态监听
        CloudGameManager.CreateCloudGameInstance().registerCloudAppStateListener((state, msg) -> {

            HWCloudGameUtils.info("onNotify","state=" + state + ";msg=" + msg);
            if (mCallback == null || mActivity == null || mViewgroup == null){
                return;
            }
            mActivity.runOnUiThread(() -> {

                if (mActivity.isFinishing()){
                    return;
                }
                int stateIndex = Arrays.binarySearch(HWStateCode.errorCodeArray, state);
                if (stateIndex >= 0){
                    //SDK游戏内部报错
                    mCallback.onGameCallback(msg, APIConstants.ERROR_SDK_INNER);
                    return;
                }
                switch (state){
                    case HWStateCode.code_connecting:
                    case HWStateCode.code_reconnecting_success:
                    case HWStateCode.code_connect_success:
                    case HWStateCode.code_verifying:
                    case HWStateCode.code_verify_success:
                    case HWStateCode.code_game_starting:
                    case HWStateCode.code_reconnection:
                    case HWStateCode.code_switch_foregroud:
                    case HWStateCode.code_switch_backgroud:
                        //TODO 不处理
                        break;
                    case HWStateCode.code_game_start_success:
                        sdkIsRelease = false;
                        mCallback.onGameCallback("startCloudApp", APIConstants.CONNECT_DEVICE_SUCCESS);
                        break;
                    case HWStateCode.code_game_first_frame:
                        if (sdkIsRelease){
                            sdkIsRelease = false;
                            mCallback.onGameCallback("startCloudApp", APIConstants.CONNECT_DEVICE_SUCCESS);
                        }
                        break;
                    case HWStateCode.code_available_time_usedup:
                        mCallback.onGameCallback("试玩时间到达:" + availablePlayTime,APIConstants.TIMEOUT_AVAILABLE_TIME);
                        break;
                    case HWStateCode.code_switch_background_timeout://切换后台超时
                        try{
                            if (mPlayListener != null){
                                sdkIsRelease = true;
                                long noOpsTime = Long.parseLong(backNoTouchTimeOut);
                                mPlayListener.onNoOpsTimeout(2,noOpsTime);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;
                    case HWStateCode.code_game_exit:
                        sdkIsRelease = true;
                        break;
                    case HWStateCode.code_notouch_timeout:
                        //前台无操作超时
                        try{
                            if (mPlayListener != null){
                                sdkIsRelease = true;
                                long noOpsTime = Long.parseLong(gameTimeout);
                                mPlayListener.onNoOpsTimeout(2,noOpsTime);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;
                    case HWStateCode.code_set_resolution_success:
                        mCallback.onGameCallback(msg, APIConstants.SWITCH_GAME_RESOLUTION_SUCCESS);
                        break;
                    case HWStateCode.code_set_resolution_error:
                        mCallback.onGameCallback(msg, APIConstants.SWITCH_GAME_RESOLUTION_ERROR);
                        break;
                    case HWStateCode.code_verify_parameter_missing:
                    case HWStateCode.code_verify_parameter_invalid:
                        mCallback.onGameCallback(msg, APIConstants.ERROR_AUTH);
                        break;
                    case HWStateCode.code_invalid_operation:
                        mCallback.onGameCallback(msg, APIConstants.ERROR_OTHER);
                        break;
                    case HWStateCode.code_server_unreachable: //服务不可用
                        mCallback.onGameCallback(msg, APIConstants.ERROR_NETWORK);
                        break;
                }
            });

        });

        //注册云游戏数据监听
        CloudGameManager.CreateCloudGameInstance().registerCloudAppDataListener((bytes, length) -> {
            //TODO
            HWCloudGameUtils.info("onRecvCloudGameData","bytes=" + new String(bytes, StandardCharsets.UTF_8) + ";length=" + length);
        });
        //游戏画面方向变化监听器  获取游戏画面方向的变化
        CloudGameManager.CreateCloudGameInstance().registerOnOrientationChangeListener(orientation -> {
            //1、横屏 0、竖屏
            HWCloudGameUtils.info("onOrientationChange","orientation=" + orientation);
            int oriParams = 1;
            if (orientation == 1){
                oriParams = 0;
            }
            setScreenOrientation(oriParams);
            if (mActivity != null && mViewgroup != null && mPlayListener != null){
                 mActivity.runOnUiThread(() -> {
                     if (mActivity.isFinishing()){
                         return;
                     }
                     mPlayListener.onScreenChange(orientation);
                 });
            }
        });

        //统计数据监听  每相隔5S数据监听
        CloudGameManager.CreateCloudGameInstance().registerStatDataListener(statData -> {

//            HWCloudGameUtils.info("onReceiveStatData","statData=" + statData);
            if (mActivity != null && mViewgroup != null){

                mActivity.runOnUiThread(() -> {
                    try {
                        if (mActivity.isFinishing()){
                            return;
                        }

                        //网络延迟
                        if (mPlayListener != null){
                            int rtt = CloudGameManager.CreateCloudGameInstance().getRtt();
//                            HWCloudGameUtils.info("onReceiveStatData","rtt=" + rtt);
                            if (rtt < 1){
                                rtt = 1;
                            }else if (rtt > 1000){
                                rtt = 1000;
                            }
                            mPlayListener.onPingUpdate(rtt);
                        }

                        /*if (mCallback != null){
                            JSONObject json = new JSONObject(statData);
                            String refresh_fps = "";
                            if (json.has("refresh_fps")){
                                refresh_fps = json.getString("refresh_fps");
                            }
//                                  mCallback.onGameCallback(refresh_fps, APIConstants.REFRESH_FPS);
                        }*/
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                });

            }
        },1);
    }

    /**
     * 设置屏幕方向
     * @param orientation  0 横屏 1 竖屏
     */
    private void setScreenOrientation(int orientation) {
        if (mActivity != null && mViewgroup != null){
            boolean directionLand = (orientation == 0);
            mActivity.setRequestedOrientation(directionLand ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

}
