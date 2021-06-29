package com.kptach.lib.game.bdsdk;

import android.app.Activity;
import android.app.Application;

import com.kptach.lib.game.bdsdk.utils.Logger;
import com.kptach.lib.inter.game.APIConstants;
import com.kptach.lib.inter.game.IDeviceControl;
import com.kptach.lib.inter.game.IGameBoxManager;
import com.kptach.lib.inter.game.IGameCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class BDSdkGameBoxManager implements IGameBoxManager {

    public static boolean debug = false;

    private boolean devLoading;
    private boolean isInited = false;

    private HashMap<String,Object> params;
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
                this.params = params;
            }
        }catch (Exception e){}

        Logger.setDebug(debug);
        isInited = true;
    }

    @Override
    public void applyCloudDevice(Activity activity, String inf, final IGameCallback<IDeviceControl> callback) {
        createDeviceControl(activity, inf, params, callback);
    }

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
