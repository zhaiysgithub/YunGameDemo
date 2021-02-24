package com.kptach.lib.game.redfinger;

import android.app.Activity;
import android.app.Application;

import com.kptach.lib.game.redfinger.utils.dx.DXStatService;

import org.json.JSONObject;

import java.util.HashMap;

import com.kptach.lib.game.redfinger.model.PadModel;
import com.kptach.lib.game.redfinger.task.RequestDeviceTask;
import com.kptach.lib.game.redfinger.utils.Logger;
import com.kptach.lib.inter.game.APIConstants;
import com.kptach.lib.inter.game.IDeviceControl;
import com.kptach.lib.inter.game.IGameBoxManager;
import com.kptach.lib.inter.game.IGameCallback;

public class RedGameBoxManager implements IGameBoxManager {

    public static boolean debug = false;
    public static String mCorpID = "";
    public static String mUserID = "";
    public static String mSdkUrl = "";
    public static String mSdkVer = "";
//    private String mPadInfo = "";

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
                if (params.containsKey(PARAMS_KEY_CORPID)){
                    mCorpID = (String) params.get(PARAMS_KEY_CORPID);
                }
                if (params.containsKey(PARAMS_KEY_USERID)){
                    mUserID = (String) params.get(PARAMS_KEY_USERID);
                }
                if (params.containsKey(PARAMS_KEY_SDKURL)){
                    mSdkUrl = (String) params.get(PARAMS_KEY_SDKURL);
                }
                if (params.containsKey(PARAMS_KEY_SDKVER)){
                    mSdkVer = (String) params.get(PARAMS_KEY_SDKVER);
                }
//                if (params.containsKey(PARAMS_KEY_PADINF)){
//                    mPadInfo = (String) params.get(PARAMS_KEY_PADINF);
//                }
            }
        }catch (Exception e){}

        isInited = true;
    }

    @Override
    public void applyCloudDevice(Activity activity, String inf, final IGameCallback<IDeviceControl> callback) {
        if (devLoading){
            return;
        }
        String pkgName = "";
        String kpGameId = "";
        try {
            JSONObject obj = new JSONObject(inf);
            pkgName = obj.optString("pkgName");
            kpGameId = obj.optString("kpGameId");
        }catch (Exception e){
            Logger.error("RedGameBoxManager", e.getMessage());
        }
        devLoading = true;

        PadModel padModel = PadModel.createPadModel(activity);
        HashMap padInfo = new HashMap();
        padInfo.put("devData",padModel.combPadModel().toString());
        String devInf = DXStatService.b(activity);
        padInfo.put("devInf",devInf);

        String padModelStr = padModel.combPadModel().toString();
        String padInfoStr = devInf;//new JSONObject(padInfo).toString();

        final String finalPkgName = pkgName;
        new RequestDeviceTask()
                .setSdkUrl(mSdkUrl)
                .setSdkVer(mSdkVer)
                .setCallback(new RequestDeviceTask.ICallback() {
                    @Override
                    public void onResult(int code, String devInfo) {
                        devLoading = false;

                        IDeviceControl control = null;
                        if (code == APIConstants.APPLY_DEVICE_SUCCESS) {
                            control = new RedDeviceControl(devInfo, finalPkgName);
                        }

                        if (callback != null) {
                            callback.onGameCallback(control, code);
                        }
                    }
                })
                .execute(mCorpID, pkgName, mUserID, kpGameId, padInfoStr, padModelStr);
    }

}
