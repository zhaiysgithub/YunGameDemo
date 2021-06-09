package com.kptach.lib.game.huawei;

import android.app.Activity;

import com.huawei.cloudgame.api.CloudGameManager;
import com.huawei.cloudgame.api.CloudGameParas;
import com.huawei.cloudgame.api.ICloudGame;
import com.kptach.lib.inter.game.APIConstants;
import com.kptach.lib.inter.game.IDeviceControl;
import com.kptach.lib.inter.game.IGameCallback;

import org.json.JSONObject;

import java.util.logging.Logger;

public class HWDeviceControl implements IDeviceControl {

    private static final String TAG = HWDeviceControl.class.getSimpleName();

    private final ICloudGame iCloudGame;
    //声音是否开启
    private boolean iSoundUnMute;
    //设置分辨率
    private CloudGameParas.Resolution defaultResolution = CloudGameParas.Resolution.DISPLAY_720P;

    public HWDeviceControl(String resource, String pkgName){
        iCloudGame = CloudGameManager.CreateCloudGameInstance();
        //默认取消静音
        iCloudGame.unmute();
        iSoundUnMute = true;

        parseResource(resource);
    }

    private void parseResource(String resource) {
        if (resource == null || resource.isEmpty()){
            return;
        }
        try{
            JSONObject jsonObject = new JSONObject(resource);



        }catch (Exception e){
            e.printStackTrace();
            HWCloudGameUtils.error(TAG, "parseResource, error:"+e.getMessage());
        }
    }

    @Override
    public void startGame(Activity activity, int i, IGameCallback<String> iGameCallback) {
        if (iCloudGame != null){

        }
    }

    @Override
    public void stopGame() {
        if (iCloudGame != null){
            iCloudGame.exitCloudApp();
        }
    }

    @Override
    public String getPadcode() {
        return null;
    }

    @Override
    public boolean isSoundEnable() {
        return iSoundUnMute;
    }

    @Override
    public String getVideoQuality() {
        //TODO 待修改
        String videoQuality = "";
        if (iCloudGame != null){
            if (defaultResolution == CloudGameParas.Resolution.DISPLAY_1080P){
                videoQuality = APIConstants.DEVICE_VIDEO_QUALITY_HD;
            }else if(defaultResolution == CloudGameParas.Resolution.DISPLAY_720P){
                videoQuality = APIConstants.DEVICE_VIDEO_QUALITY_ORDINARY;
            }else if(defaultResolution == CloudGameParas.Resolution.DISPLAY_540P ||
                    defaultResolution == CloudGameParas.Resolution.DISPLAY_480P){
                videoQuality = APIConstants.DEVICE_VIDEO_QUALITY_LS;
            }else {
                // TODO 自动分辨率
                videoQuality = APIConstants.DEVICE_VIDEO_QUALITY_AUTO;
            }
        }
        return videoQuality;
    }

    @Override
    public int[] getVideoSize() {
        return new int[0];
    }

    @Override
    public boolean isReleased() {
        return false;
    }

    @Override
    public void setNoOpsTimeout(long l, long l1) {

    }

    @Override
    public void switchQuality(String s) {

    }

    @Override
    public void setAudioSwitch(boolean b) {
        if (iSoundUnMute != b){
            iSoundUnMute = b;
            if (b){
                iCloudGame.unmute(); //取消静音
            }else {
                iCloudGame.mute(); //静音
            }
        }
    }

    @Override
    public void sendPadKey(int i) {

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
    public void setPlayListener(PlayListener playListener) {

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
        return null;
    }

    public void setDefaultResolution(CloudGameParas.Resolution defaultResolution) {
        this.defaultResolution = defaultResolution;
    }
}
