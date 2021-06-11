package com.kptach.lib.game.huawei;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;

import com.huawei.cloudgame.api.CloudGameManager;
import com.huawei.cloudgame.api.CloudGameParas;
import com.kptach.lib.inter.game.APIConstants;
import com.kptach.lib.inter.game.IDeviceControl;
import com.kptach.lib.inter.game.IGameBoxManager;
import com.kptach.lib.inter.game.IGameCallback;

import org.json.JSONObject;

import java.util.HashMap;

public class HWGameBoxManager implements IGameBoxManager {

    private Application mApplication;
    private String mResource;
    private boolean isDebug;
    private boolean sdkIsInit = false;

    @Override
    public void initLib(Application application, HashMap params, IGameCallback<String> iGameCallback) {
        mApplication = application;
        try {
            if (params != null){
                if (params.containsKey("resource")){
                    Object resObjcet = params.get("resource");
                    if (resObjcet != null){
                        mResource = resObjcet.toString();
                    }
                }
                if (params.containsKey(PARAMS_KEY_DEBUG)){
                    Object debugObjcet = params.get(PARAMS_KEY_DEBUG);
                    if (debugObjcet instanceof Boolean){
                        isDebug = (boolean) debugObjcet;
                    }
                }
                CloudGameManager.CreateCloudGameInstance().enableDebugLog(true);
                //是否使用真机输入法
                CloudGameManager.CreateCloudGameInstance().enableRemoteIME(true);

                HWCloudGameUtils.setDebug(true);

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        boolean tabletDevice = (application.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE;
//        CloudGameManager.CreateCloudGameInstance().deinit();
        CloudGameManager.CreateCloudGameInstance().init(application
                , tabletDevice ? CloudGameParas.DevType.DEV_PAD : CloudGameParas.DevType.DEV_PHONE);
    }

    @Override
    public void applyCloudDevice(Activity activity, String s, IGameCallback<IDeviceControl> iGameCallback) {


    }

    @Override
    public void createDeviceControl(Activity activity, String gameInf, HashMap<String, Object> params, IGameCallback<IDeviceControl> callback) {
        //创建 deviceControl
        try{
            String pkgName = "";
            HWDeviceControl instance = new HWDeviceControl(params);
            JSONObject obj = new JSONObject(gameInf);
            if (obj.has("pkgName")){
                pkgName = obj.optString("pkgName");
            }
            if (callback != null){
                callback.onGameCallback(instance, APIConstants.APPLY_DEVICE_SUCCESS);
            }
        }catch (Exception e){
            e.printStackTrace();
            if (callback != null){
                callback.onGameCallback(null, APIConstants.ERROR_APPLY_DEVICE);
            }
        }


    }

}
