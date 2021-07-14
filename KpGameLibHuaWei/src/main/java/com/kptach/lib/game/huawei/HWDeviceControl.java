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
import com.kptach.lib.inter.game.IPlayDataListener;
import com.kptach.lib.inter.game.IPlayScreenListener;
import com.kptach.lib.inter.game.IPlayStateListener;

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
    private String availablePlayTime;
    private final int []screenSize = new int[2];
    //游戏状态监听
    private IPlayStateListener mStateListener;
    //游戏数据监听
    private IPlayDataListener mDataListener;
    //游戏屏幕数据监听
    private IPlayScreenListener mScreenListener;

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
                    //前台无操作超时的时长，单位是秒 5min
                    sdkParams.put("touch_timeout", "300");
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
    public void registerPlayStateListener(IPlayStateListener listener) {
        this.mStateListener = listener;
    }

    @Override
    public void registerPlayDataListener(IPlayDataListener listener) {
        this.mDataListener = listener;
    }

    @Override
    public void registerPlayScreenListener(IPlayScreenListener listener) {
        this.mScreenListener = listener;
    }

    @Override
    public void startGame(Activity activity, int res, IGameCallback<String> callback) {

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
            HashMap<String, String> mediaConfigMap = new HashMap<>();
            mediaConfigMap.put("physical_width", Integer.toString(width));
            mediaConfigMap.put("physical_height", Integer.toString(height));
            mediaConfigMap.put("frame_rate", Integer.toString(30));
            mediaConfigMap.put("bitrate", Integer.toString(10000000));
            CloudGameManager.CreateCloudGameInstance().setMediaConfig(mediaConfigMap);
            setVideoDisplayMode(true);
            CloudGameManager.CreateCloudGameInstance().startCloudApp(activity, viewGroup, sdkParams);
            if (callback != null){
                callback.onGameCallback("startCloudApp", APIConstants.GAME_LOADING);
            }
        } catch (Exception e) {
            e.printStackTrace();
            stopGame();
            if (callback != null){
                callback.onGameCallback(e.getMessage(), APIConstants.ERROR_CONNECT_DEVICE);
            }
        }

    }

    @Override
    public void startGame(Activity activity, int container) {

    }

    @Override
    public void stopGame() {
        try{
            if (!sdkIsRelease){
                sdkIsRelease = true;
                CloudGameManager.CreateCloudGameInstance().exitCloudApp();
                CloudGameManager.CreateCloudGameInstance().deinit();
            }
            if (mCallback != null){
                mCallback.onGameCallback("game release success" , APIConstants.RELEASE_SUCCESS);
            }
            if (mStateListener != null){
                mStateListener.onNotify(APIConstants.RELEASE_SUCCESS, "game release success");
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
            if (mActivity == null || mViewgroup == null){
                return;
            }
            mActivity.runOnUiThread(() -> {

                if (mActivity.isFinishing()){
                    return;
                }
                int stateIndex = Arrays.binarySearch(HWStateCode.errorCodeArray, state);
                if (stateIndex >= 0){
                    //SDK游戏内部报错
                    if (mCallback != null){
                        mCallback.onGameCallback(msg, APIConstants.ERROR_SDK_INNER);
                    }
                    if (mStateListener != null){
                        mStateListener.onNotify(APIConstants.ERROR_SDK_INNER, msg);
                    }
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
                        if (mCallback != null) {
                            mCallback.onGameCallback("startCloudApp", APIConstants.CONNECT_DEVICE_SUCCESS);
                        }
                        if (mStateListener != null){
                            mStateListener.onNotify(APIConstants.CONNECT_DEVICE_SUCCESS, "connect device success");
                        }
                        break;
                    case HWStateCode.code_game_first_frame:
                        if (sdkIsRelease){
                            sdkIsRelease = false;
                            if (mCallback != null) {
                                mCallback.onGameCallback("startCloudApp", APIConstants.CONNECT_DEVICE_SUCCESS);
                            }

                            if (mStateListener != null){
                                mStateListener.onNotify(APIConstants.CONNECT_DEVICE_SUCCESS , "connect device success");
                            }
                        }
                        break;
                    case HWStateCode.code_available_time_usedup:
                        if (mCallback != null) {
                            mCallback.onGameCallback("试玩时间到达:" + availablePlayTime, APIConstants.TIMEOUT_AVAILABLE_TIME);
                        }
                        if (mStateListener != null){
                            mStateListener.onNotify(APIConstants.TIMEOUT_AVAILABLE_TIME, "试玩时间:"+ availablePlayTime +"到达");
                        }
                        break;
                    case HWStateCode.code_switch_background_timeout:
                        //切换后台超时
                        sdkIsRelease = true;
                        if (mCallback != null) {
                            mCallback.onGameCallback("switch background timeout", APIConstants.ERROR_OTHER);
                        }
                        if (mStateListener != null){
                            mStateListener.onNotify(APIConstants.ERROR_OTHER, "switch background timeout");
                        }
                        break;
                    case HWStateCode.code_notouch_timeout:
                        try{
                            if (mPlayListener != null){
                                sdkIsRelease = true;
                                long noOpsTime = Long.parseLong(gameTimeout);
                                mPlayListener.onNoOpsTimeout(2,noOpsTime);
                            }

                            if (mDataListener != null){
                                sdkIsRelease = true;
                                long noOpsTime = Long.parseLong(gameTimeout);
                                mDataListener.onNoOpsTimeout(2,noOpsTime);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;
                    case HWStateCode.code_game_exit:
                        sdkIsRelease = true;
                        break;
                    case HWStateCode.code_set_resolution_success:
                        if (mCallback != null) {
                            mCallback.onGameCallback(msg, APIConstants.SWITCH_GAME_RESOLUTION_SUCCESS);
                        }
                        if (mStateListener != null){
                            mStateListener.onNotify(APIConstants.SWITCH_GAME_RESOLUTION_SUCCESS, msg);
                        }
                        break;
                    case HWStateCode.code_set_resolution_error:
                        if (mCallback != null) {
                            mCallback.onGameCallback(msg, APIConstants.SWITCH_GAME_RESOLUTION_ERROR);
                        }
                        if (mStateListener != null){
                            mStateListener.onNotify(APIConstants.SWITCH_GAME_RESOLUTION_ERROR, msg);
                        }
                        break;
                    case HWStateCode.code_verify_parameter_missing:
                    case HWStateCode.code_verify_parameter_invalid:
                        if (mCallback != null) {
                            mCallback.onGameCallback(msg, APIConstants.ERROR_AUTH);
                        }
                        if (mStateListener != null){
                            mStateListener.onNotify(APIConstants.ERROR_AUTH, msg);
                        }
                        break;
                    case HWStateCode.code_invalid_operation:
                        if (mCallback != null) {
                            mCallback.onGameCallback(msg, APIConstants.ERROR_OTHER);
                        }
                        if (mStateListener != null){
                            mStateListener.onNotify(APIConstants.ERROR_OTHER, msg);
                        }
                        break;
                    case HWStateCode.code_server_unreachable: //服务不可用
                        if (mCallback != null) {
                            mCallback.onGameCallback(msg, APIConstants.ERROR_NETWORK);
                        }
                        if (mStateListener != null){
                            mStateListener.onNotify(APIConstants.ERROR_NETWORK, msg);
                        }
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

            if (mActivity != null && mViewgroup != null && mScreenListener != null){
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mActivity.isFinishing()){
                            return;
                        }
                        //1、横屏 0、竖屏
                        mScreenListener.onOrientationChange(orientation);
                    }
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
                            if (rtt < 10){
                                rtt = 10;
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
