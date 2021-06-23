package com.kptach.lib.game.bdsdk;

import android.app.Activity;
import android.app.Application;

import com.kptach.lib.game.bdsdk.model.PadModel;
import com.kptach.lib.game.bdsdk.task.RequestDeviceTask;
import com.kptach.lib.game.bdsdk.utils.Logger;
import com.kptach.lib.game.bdsdk.utils.dx.DXStatService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import com.kptach.lib.inter.game.APIConstants;
import com.kptach.lib.inter.game.IDeviceControl;
import com.kptach.lib.inter.game.IGameBoxManager;
import com.kptach.lib.inter.game.IGameCallback;

public class BDSdkGameBoxManager implements IGameBoxManager {

    public static boolean debug = false;

    private boolean devLoading;
    private boolean isInited = false;

    @Override
    public void initLib(Application application, HashMap params, IGameCallback<String> callback) {
        if (isInited){
            return;
        }
        try {
            if (params != null){
                if (params.containsKey(PARAMS_KEY_DEBUG)){
                    debug = (Boolean) params.get(PARAMS_KEY_DEBUG);
                }
            }
        }catch (Exception e){}

        Logger.setDebug(debug);
        isInited = true;
    }

    @Override
    public void applyCloudDevice(Activity activity, String inf, final IGameCallback<IDeviceControl> callback) {
        if (devLoading){
            return;
        }
        String pkgName = "";
//        String kpGameId = "";
        try {
            JSONObject obj = new JSONObject(inf);
            pkgName = obj.optString("pkgName");
//            kpGameId = obj.optString("kpGameId");
        }catch (Exception e){
            Logger.error("RedGameBoxManager", e.getMessage());
        }
        devLoading = true;

//        PadModel padModel = PadModel.createPadModel(activity);
//        HashMap padInfo = new HashMap();
//        padInfo.put("devData", padModel.combPadModel().toString());
//        String devInf = DXStatService.b(activity);
//        padInfo.put("devInf", devInf);


        final String finalPkgName = pkgName;

        devLoading = false;

        IDeviceControl control = null;
        int code = 0;
        String devInfo = "";
        if (code == APIConstants.APPLY_DEVICE_SUCCESS) {
            control = new BDSdkDeviceControl(devInfo, finalPkgName, "");
        }

        if (callback != null) {
            callback.onGameCallback(control, code);
        }
    }


    @Override
    public void createDeviceControl(Activity activity, String gameInf, HashMap<String, Object> params, IGameCallback<IDeviceControl> callback) {


        String deviceData = params.get("resource").toString();
        String deviceId = params.get("deviceid").toString();

        Logger.info("KpPassCMWManager", "result.data = " + deviceData);
        String pkgName = "";
        try {
            JSONObject packInfo = new JSONObject(gameInf);
            pkgName = packInfo.optString("pkgName");
            Logger.info("KpPassCMWManager", "result.data  deviceData = " + deviceData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        IDeviceControl control = (IDeviceControl) new BDSdkDeviceControl(deviceData, pkgName, deviceId);

        if (callback != null) {
            callback.onGameCallback(control, APIConstants.APPLY_DEVICE_SUCCESS);
        }
    }

}
