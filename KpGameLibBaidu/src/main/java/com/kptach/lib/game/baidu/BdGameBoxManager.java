package com.kptach.lib.game.baidu;

import android.app.Activity;
import android.app.Application;

import com.yd.yunapp.gameboxlib.APICallback;
import com.yd.yunapp.gameboxlib.APIConstants;
import com.yd.yunapp.gameboxlib.CloudPhoneManager;
import com.yd.yunapp.gameboxlib.DeviceControl;
import com.yd.yunapp.gameboxlib.DeviceMockInfo;
import com.yd.yunapp.gameboxlib.GameBoxManager;
import com.yd.yunapp.gameboxlib.GameInfo;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.kptach.lib.inter.game.IDeviceControl;
import com.kptach.lib.inter.game.IGameBoxManager;
import com.kptach.lib.inter.game.IGameCallback;
import com.kptach.lib.game.baidu.utils.DeviceUtils;
import com.kptach.lib.game.baidu.utils.Logger;

public class BdGameBoxManager implements IGameBoxManager {
    private static final String TAG = BdGameBoxManager.class.getSimpleName();

    private boolean devLoading;
    private boolean isInited = false;

    private boolean debug = false;
    private String ak = "";
    private String sk = "";
    private String ch = "";

    private Application mApplication;

    @Override
    public void initLib(Application application, HashMap params, IGameCallback<String> callback) {
        mApplication = application;

        if (isInited){
            return;
        }
        try {
            if (params != null){
                if (params.containsKey(PARAMS_KEY_DEBUG)){
                    debug = (Boolean) params.get(PARAMS_KEY_DEBUG);
                }
                if (params.containsKey(PARAMS_KEY_BD_AK)){
                    ak = (String) params.get(PARAMS_KEY_BD_AK);
                }
                if (params.containsKey(PARAMS_KEY_BD_SK)){
                    sk = (String) params.get(PARAMS_KEY_BD_SK);
                }
//                if (params.containsKey(PARAMS_KEY_PADINF)){
//                    padInf = (String) params.get(PARAMS_KEY_PADINF);
//                }
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }

        Logger.setDebug(debug);

        //初始化游戏
        com.yd.yunapp.gameboxlib.GameBoxManager.getInstance(application).setDebug(debug);
        com.yd.yunapp.gameboxlib.GameBoxManager.getInstance(application).init(ak, sk, ch, false);

        CloudPhoneManager.getInstance(application).setDebug(debug);
        CloudPhoneManager.getInstance(application).init(ak, sk, ch,false);
        isInited = true;
    }

    @Override
    public void applyCloudDevice(final Activity activity, String inf, final IGameCallback<IDeviceControl> callback) {
        if (devLoading){
            return;
        }

        GameInfo game = getLibGameInfo(inf);
        if (game == null){
            if (callback != null){
                callback.onGameCallback(null, com.kptach.lib.inter.game.APIConstants.ERROR_GAME_INFO);
            }
            return;
        }

        devLoading = true;
        com.yd.yunapp.gameboxlib.GameBoxManager manager = com.yd.yunapp.gameboxlib.GameBoxManager.getInstance(mApplication);
        addDeviceInfo(manager);
        manager.applyCloudDevice(game, new APICallback<DeviceControl>() {
            @Override
            public void onAPICallback(final DeviceControl inner,final int code) {
                if (activity != null && !activity.isFinishing()){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            devLoading = false;

                            IDeviceControl control = null;
                            if (code == APIConstants.APPLY_DEVICE_SUCCESS) {
                                control = new BdDeviceControl(inner);
                            }

                            if (callback != null){
                                callback.onGameCallback(control, code);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void createDeviceControl(Activity activity, String gameInf, HashMap<String, Object> params, IGameCallback<IDeviceControl> callback) {

    }

    private GameInfo getLibGameInfo(String inf){
        try {
            JSONObject obj = new JSONObject(inf);
            GameInfo game = new GameInfo();
            game.gid = obj.optInt("gid");
            game.pkgName = obj.optString("pkgName");
            game.iconUrl = obj.optString("iconUrl");
            game.usedTime = obj.optInt("usedTime");
            game.totalTime = obj.optInt("totalTime");
            game.name = obj.optString("name");
            game.playCount = obj.optInt("playCount");
            return game;
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }

        return null;

    }


    private void addDeviceInfo(com.yd.yunapp.gameboxlib.GameBoxManager manager){
        //获取设备id, 发送到云手机，用来解决风控问题
        //297ebd358f8d1d5f,  //864131034311009 //VM010127052028
        //DeviceInfo{id=0, status=0, deviceId='VM010127052028', token='{"webControlList":[{"webControlCode":"XA-WEBSOCKET-CONTROL-41","webControlInfoList":[{"controlIp":"xian.cloud-control.top","controlPort":9741}]}],"controlList":[{"controlCode":"XA-USER-CONTROL-41","controlInfoList":[{"controlIp":"xian.cloud-control.top","controlPort":9641}]}],"padList":[{"controlCode":"XA-USER-CONTROL-41","padCode":"VM010127052028","padStatus":"1","padType":"0","videoCode":"GZ-TEST-USER-VIDEO-01"}],"videoList":[{"videoCode":"GZ-TEST-USER-VIDEO-01","videoInfoList":[{"videoUrl":"rtmp://117.48.196.66:110/live","videoProtocol":"2","videoDomain":"live","videoPort":110,"videoContext":"1"},{"videoUrl":"rtmp://117.48.196.66:1936/live","videoProtocol":"","videoDomain":"","videoPort":-1,"videoContext":""}]}],"wssWebControlList":[{"wssWebControlInfoList":[{"controlIp":"xian.cloud-control.top","controlPort":9841}],"wssWebControlCode":"XA-WSS-CONTROL-41"}],"webRtcControlList":[{"webRtcControlInfoList":[{"controlIp":"10.3.98.1","controlPort":9641}],"controlCode":"XA-USER-CONTROL-41","gateway":{"gatewayWssPort":8191,"gatewayIp":"xian.cloud-control.top","gatewayPort":8190}}],"sessionId":"b6d822fcc481462ead6c57741bf6d3f0","userId":11357855}', type=0, usedTime=0, totalTime=86400, gop=50, bitRate=3600, compressionType=VPU, maxDescentFrame=1, maxFrameRate=30, minDescentFrame=1, minFrameRate=20, picQuality=GRADE_LEVEL_HD, resolution=LEVEL_720_1280, sound=true, queueInfo=null}
        try {
            String ANDROID_ID = DeviceUtils.getAndroidId(mApplication);;//Settings.System.getString(mApplication.getContentResolver(), Settings.System.ANDROID_ID);
            String Imei = DeviceUtils.getIMEI(mApplication);

            manager.addDeviceMockInfo(DeviceMockInfo.MOCK_IMEI, Imei);
            manager.addDeviceMockInfo(DeviceMockInfo.MOCK_ANDROID_ID, ANDROID_ID);

            manager.addDeviceMockInfo("brand", DeviceUtils.getDeviceBrand());
            manager.addDeviceMockInfo("model", DeviceUtils.getDeviceModel());
            manager.addDeviceMockInfo("manufacturer", DeviceUtils.getDeviceManufacturer());
            manager.addDeviceMockInfo(DeviceMockInfo.MOCK_BOOTLOADER, DeviceUtils.getDeviceBootloader());

            manager.addDeviceMockInfo(DeviceMockInfo.MOCK_SERIALNO, DeviceUtils.getSERIAL());

            manager.addDeviceMockInfo(DeviceMockInfo.MOCK_BOARD, DeviceUtils.getDeviceBoard());
            manager.addDeviceMockInfo(DeviceMockInfo.MOCK_DEVICE, DeviceUtils.getDeviceDevice());
            manager.addDeviceMockInfo(DeviceMockInfo.MOCK_FINGERPRINT, DeviceUtils.getDeviceFingerprint());
            manager.addDeviceMockInfo(DeviceMockInfo.MOCK_PRODUCTNAME, DeviceUtils.getDeviceProduct());

            String imsi = DeviceUtils.getIMSI(mApplication);
            if (imsi != null){
                manager.addDeviceMockInfo(APIConstants.MOCK_IMEI, imsi);
            }
            String wifimac = DeviceUtils.getWifiMacAddress(mApplication);
            if (wifimac != null){
                manager.addDeviceMockInfo(DeviceMockInfo.MOCK_WIFIMAC, wifimac);
            }
            String wifiname = DeviceUtils.getWifiName(mApplication);
            if (wifiname != null){
                manager.addDeviceMockInfo(DeviceMockInfo.MOCK_WIFINAME, wifiname);
            }
            String bssid = DeviceUtils.getBSSID(mApplication);
            if (bssid != null){
                manager.addDeviceMockInfo(DeviceMockInfo.MOCK_BSSID, bssid);
            }

            manager.addDeviceMockInfo(DeviceMockInfo.MOCK_BUILDID, DeviceUtils.getBuildId());
            manager.addDeviceMockInfo(DeviceMockInfo.MOCK_BUILDHOST, DeviceUtils.getBuildHost());
            manager.addDeviceMockInfo(DeviceMockInfo.MOCK_BUILDTAGS, DeviceUtils.getBuildTags());
            manager.addDeviceMockInfo(DeviceMockInfo.MOCK_BUILDTYPE, DeviceUtils.getBuildType());
            manager.addDeviceMockInfo(DeviceMockInfo.MOCK_BUILDVERSIONINC, DeviceUtils.getVersionInc());

            Map<String, String> map = manager.getDeviceMockInfo();
            Logger.info(TAG, map.toString());

        }catch (Exception e){
        }
    }

    public void removeDeviceInfo(){
        try {
            com.yd.yunapp.gameboxlib.GameBoxManager manager = GameBoxManager.getInstance(mApplication);
            manager.removeDeviceMockInfo(com.yd.yunapp.gameboxlib.APIConstants.MOCK_IMEI);
            manager.removeDeviceMockInfo(com.yd.yunapp.gameboxlib.APIConstants.MOCK_ANDROID_ID);
            manager.removeDeviceMockInfo("brand");
            manager.removeDeviceMockInfo("model");
            manager.removeDeviceMockInfo("manufacturer");
            manager.removeDeviceMockInfo("bootloader");
            manager.removeDeviceMockInfo("serialno");
            manager.removeDeviceMockInfo("device");
            manager.removeDeviceMockInfo("fingerprint");
            manager.removeDeviceMockInfo("productName");
            manager.removeDeviceMockInfo("imsi");
            manager.removeDeviceMockInfo("wifimac");
            manager.removeDeviceMockInfo("wifiname");
            manager.removeDeviceMockInfo("bssid");
            manager.removeDeviceMockInfo("buildId");
            manager.removeDeviceMockInfo("buildHost");
            manager.removeDeviceMockInfo("buildTags");
            manager.removeDeviceMockInfo("buildType");
            manager.removeDeviceMockInfo("buildVersionInc");
        }catch (Exception e){

        }

    }
}
