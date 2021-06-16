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

    private boolean isDebug;
    private String mResource = "";
    private String corpKey = "";
    private String sdkVersion = "";
    private HWLoadLibHelper mLibHelper;
    private int soVersion = 1;

    @Override
    public void initLib(Application application, HashMap params, IGameCallback<String> iGameCallback) {

        //TODO 检测 so 文件是否存在，不存在执行下载
        if (mLibHelper == null){
            mLibHelper = new HWLoadLibHelper(application);
        }

        try {

            if (params != null){
                if (params.containsKey("corpKey")){
                    corpKey = (String) params.get("corpKey");
                }
                if (params.containsKey("sdkVersion")){
                    sdkVersion = (String) params.get("sdkVersion");
                }

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

                startInitCloudGameManager(application);

            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private void startInitCloudGameManager(Application application){
        CloudGameManager.CreateCloudGameInstance().enableDebugLog(isDebug);
        //是否使用真机输入法
        CloudGameManager.CreateCloudGameInstance().enableRemoteIME(true);

        HWCloudGameUtils.setDebug(isDebug);

        boolean tabletDevice = (application.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE;
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
            if (callback == null){
                return;
            }
            mLibHelper.loadLib(corpKey, sdkVersion, soVersion, (code, msg) -> {
                HWCloudGameUtils.info("loadLib: code = " + code + ";msg = " + msg);
                if (code == HWLoadLibHelper.LOADLIB_STATUS_SUCCESS){
                    callback.onGameCallback(instance, APIConstants.APPLY_DEVICE_SUCCESS);
                }else {
                    callback.onGameCallback(null, APIConstants.ERROR_APPLY_DEVICE);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            if (callback != null){
                callback.onGameCallback(null, APIConstants.ERROR_APPLY_DEVICE);
            }
        }


    }

}
