package com.kptach.lib.game.huawei;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import com.huawei.cloudgame.api.CloudGameDataListener;
import com.huawei.cloudgame.api.CloudGameManager;
import com.huawei.cloudgame.api.CloudGameOrientationChangeListener;
import com.huawei.cloudgame.api.CloudGameParas;
import com.huawei.cloudgame.api.CloudGameStatDataListener;
import com.huawei.cloudgame.api.CloudGameStateListener;
import com.kptach.lib.inter.game.APIConstants;
import com.kptach.lib.inter.game.IDeviceControl;
import com.kptach.lib.inter.game.IGameCallback;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;

public class HWDeviceControl implements IDeviceControl {

//    private static final String TAG = HWDeviceControl.class.getSimpleName();
    //声音是否开启
    private boolean iSoundUnMute;
    //设置分辨率
    private CloudGameParas.Resolution videoResolution = CloudGameParas.Resolution.DISPLAY_720P;
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
                    //无操作超时的时长，单位是秒
                    sdkParams.put("touch_timeout", "0");
                    //备用参数
                    sdkParams.put("user_id", "");
                    if (sdkParams.containsKey("ip")){
                        sdkParams.put("ip","114.116.222.169");
                    }
                    //测试修改超时时间
                    if (sdkParams.containsKey("game_timeout")){
                        sdkParams.put("game_timeout","100");
                    }
                    //测试可试玩时间
                    if (sdkParams.containsKey("available_playtime")){
                        sdkParams.put("available_playtime","200");
                    }
                    if (sdkParams.containsKey("game_timeout")){
                        gameTimeout = sdkParams.get("game_timeout");
                    }
                    if (sdkParams.containsKey("available_playtime")){
                        availablePlayTime = sdkParams.get("available_playtime");
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            HashMap<String, String> mediaConfigMap = new HashMap<>();
            mediaConfigMap.put("physical_width", Integer.toString(width));
            mediaConfigMap.put("physical_height", Integer.toString(height));
            mediaConfigMap.put("frame_rate", Integer.toString(30));
            mediaConfigMap.put("bitrate", Integer.toString(10000000));
            CloudGameManager.CreateCloudGameInstance().setMediaConfig(mediaConfigMap);

            CloudGameManager.CreateCloudGameInstance().setDisplayMode(CloudGameParas.DisplayMode.DISPLAY_MODE_FIT);
            CloudGameManager.CreateCloudGameInstance().startCloudApp(activity, viewGroup, sdkParams);
            if (callback != null){
                callback.onGameCallback("startCloudApp", APIConstants.CONNECT_DEVICE_SUCCESS);
            }
            setScreenOrientation(hwDirection);
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null){
                callback.onGameCallback(e.getMessage(), APIConstants.ERROR_CONNECT_DEVICE);
            }
        }

    }

    @Override
    public void stopGame() {
        CloudGameManager.CreateCloudGameInstance().exitCloudApp();
        CloudGameManager.CreateCloudGameInstance().deinit();
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
        //TODO 待修改
        String videoQuality = "";
        if (videoResolution == CloudGameParas.Resolution.DISPLAY_1080P) {
            videoQuality = APIConstants.DEVICE_VIDEO_QUALITY_HD;
        } else if (videoResolution == CloudGameParas.Resolution.DISPLAY_720P) {
            videoQuality = APIConstants.DEVICE_VIDEO_QUALITY_ORDINARY;
        } else if (videoResolution == CloudGameParas.Resolution.DISPLAY_540P ||
                videoResolution == CloudGameParas.Resolution.DISPLAY_480P) {
            videoQuality = APIConstants.DEVICE_VIDEO_QUALITY_LS;
        } else {
            // TODO 自动分辨率
            videoQuality = APIConstants.DEVICE_VIDEO_QUALITY_AUTO;
        }
        resolutionQuality = videoQuality;
        return videoQuality;
    }

    @Override
    public int[] getVideoSize() {
        try {
            //TODO 获取视频尺寸
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new int[]{1280, 720};
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

    public String getHSdkVersion() {
        return CloudGameManager.CreateCloudGameInstance().getVersion();
    }

    public String getDetailStr(){
        return CloudGameManager.CreateCloudGameInstance().getDetailString();
    }

    public void setDisplayMode(CloudGameParas.DisplayMode displayMode) {
        CloudGameManager.CreateCloudGameInstance().setDisplayMode(displayMode);
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
                switch (state){
                    case HWStateCode.code_connecting:
                    case HWStateCode.code_reconnecting_success:
//                        setScreenOrientation(hwDirection);
                        mCallback.onGameCallback(hwDirection + "",APIConstants.GAME_START_CONNECT);
                        break;
                    case HWStateCode.code_game_start_success:
                        sdkIsRelease = false;
                        mCallback.onGameCallback(msg,APIConstants.GAME_SDK_INIT_SUCCESS);
                        break;
                    case HWStateCode.code_available_time_usedup:
                        mCallback.onGameCallback("试玩时间到达:" + availablePlayTime,APIConstants.TIMEOUT_AVAILABLE_TIME);
                        break;
                    case HWStateCode.code_notouch_timeout:
//                        mCallback.onGameCallback("长时间未操作", APIConstants.TIMEOUT_NO_OPS);
                        try{
                            if (mPlayListener != null){
                                long noOpsTime = Long.parseLong(gameTimeout);
                                mPlayListener.onNoOpsTimeout(2,noOpsTime);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;
                    case HWStateCode.code_switch_background_timeout:
//                        mCallback.onGameCallback(msg, APIConstants.TIMEOUT_SWITCH_BACKGROUND);
                        break;
                    case HWStateCode.code_switch_backgroud:
//                        mCallback.onGameCallback(msg, APIConstants.SWITCH_BACKGROUND);
                        break;
                    case HWStateCode.code_switch_foregroud:
//                        mCallback.onGameCallback(msg, APIConstants.SWITCH_FOREGROUND);
                        break;
                    case HWStateCode.code_game_exit:
                        sdkIsRelease = true;
                        break;
                    case HWStateCode.code_set_resolution_success:
                        mCallback.onGameCallback(msg, APIConstants.SWITCH_GAME_RESOLUTION_SUCCESS);
                        break;
                    case HWStateCode.code_set_resolution_error:
                        mCallback.onGameCallback(msg, APIConstants.SWITCH_GAME_RESOLUTION_ERROR);
                        break;
                    default:
//                        mCallback.onGameCallback(msg, APIConstants.ERROR_SDK_INNER);
                        break;
                }
            });

        });

        //注册云游戏数据监听
        CloudGameManager.CreateCloudGameInstance().registerCloudAppDataListener(new CloudGameDataListener() {
            @Override
            public void onRecvCloudGameData(byte[] bytes, int length) {
                HWCloudGameUtils.info("onRecvCloudGameData","bytes=" + new String(bytes, StandardCharsets.UTF_8) + ";length=" + length);
            }
        });
        //游戏画面方向变化监听器
        CloudGameManager.CreateCloudGameInstance().registerOnOrientationChangeListener(orientation -> {

            HWCloudGameUtils.info("onOrientationChange","orientation=" + orientation);
            if (mActivity != null && mViewgroup != null && mPlayListener != null){
                 mActivity.runOnUiThread(() -> {
                     if (mActivity.isFinishing()){
                         return;
                     }
                     mPlayListener.onScreenChange(orientation);
                 });
            }
        });

        //统计数据监听  每相隔10S数据监听
        CloudGameManager.CreateCloudGameInstance().registerStatDataListener(statData -> {

            HWCloudGameUtils.info("onReceiveStatData","statData=" + statData);
            if (statData != null && !statData.isEmpty() && mActivity != null && mViewgroup != null){

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (mActivity.isFinishing()){
                                return;
                            }

                            //网络延迟
                            if (mPlayListener != null){
                                int rtt = CloudGameManager.CreateCloudGameInstance().getRtt();
                                mPlayListener.onPingUpdate(rtt);
                            }

                            if (mCallback != null){
                                JSONObject json = new JSONObject(statData);
                                String refresh_fps = "";
                                if (json.has("refresh_fps")){
                                    refresh_fps = json.getString("refresh_fps");
                                }
//                                  mCallback.onGameCallback(refresh_fps, APIConstants.REFRESH_FPS);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                });

            }
        },10);
    }

    /**
     * 设置屏幕方向
     * @param orientation  0 横屏 1 竖屏
     */
    private void setScreenOrientation(int orientation) {
        if (mActivity != null && mViewgroup != null){
            boolean directionLand = (orientation == 0);
            int curOri = mActivity.getResources().getConfiguration().orientation;
            boolean isLandSpace = (curOri == Configuration.ORIENTATION_LANDSCAPE);
            if (isLandSpace == directionLand){
                return;
            }
            mActivity.setRequestedOrientation(directionLand ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

}
