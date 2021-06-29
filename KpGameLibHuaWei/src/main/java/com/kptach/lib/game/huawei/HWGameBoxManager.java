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

import java.util.HashMap;

public class HWGameBoxManager implements IGameBoxManager {

    private boolean isDebug;
    private String mResource = "";
    private String corpKey = "";
    private String sdkVersion = "";
//    private HWLoadLibHelper mLibHelper;
    public static int soVersion = 1;
    private HashMap<String, Object> params;
    @Override
    public void initLib(Application application, HashMap params, IGameCallback<String> iGameCallback) {

        //TODO 检测 so 文件是否存在，不存在执行下载
        /*if (mLibHelper == null){
            mLibHelper = new HWLoadLibHelper(application);
        }*/

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
                this.params = params;
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private void startInitCloudGameManager(Activity activity){
        CloudGameManager.CreateCloudGameInstance().enableDebugLog(true);
        //是否使用真机输入法
        CloudGameManager.CreateCloudGameInstance().enableRemoteIME(true);

        HWCloudGameUtils.setDebug(true);

        boolean tabletDevice = (activity.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE;
        CloudGameManager.CreateCloudGameInstance().init(activity
                , tabletDevice ? CloudGameParas.DevType.DEV_PAD : CloudGameParas.DevType.DEV_PHONE);
    }

    @Override
    public void applyCloudDevice(Activity activity, String gameInf, IGameCallback<IDeviceControl> callback) {
        createDeviceControl(activity, gameInf, params, callback );
    }

    public void createDeviceControl(Activity activity, String gameInf, HashMap<String, Object> params, IGameCallback<IDeviceControl> callback) {
        //创建 deviceControl
        try{
            /*String pkgName = "";

            JSONObject obj = new JSONObject(gameInf);
            if (obj.has("pkgName")){
                pkgName = obj.optString("pkgName");
            }*/
            if (callback == null){
                return;
            }

            /*mLibHelper.loadLib(corpKey, sdkVersion, soVersion, (code, msg) -> {
                HWCloudGameUtils.info("loadLib: code = " + code + ";msg = " + msg);
                if (code == HWLoadLibHelper.LOADLIB_STATUS_SUCCESS){
                    startInitCloudGameManager(activity);
                    HWDeviceControl instance = new HWDeviceControl(params);
                    callback.onGameCallback(instance, APIConstants.APPLY_DEVICE_SUCCESS);
                }else {
                    callback.onGameCallback(null, APIConstants.ERROR_APPLY_DEVICE);
                }
            });*/

            startInitCloudGameManager(activity);
            HWDeviceControl instance = new HWDeviceControl(params);
            callback.onGameCallback(instance, APIConstants.APPLY_DEVICE_SUCCESS);
        }catch (Exception e){
            e.printStackTrace();
            callback.onGameCallback(null, APIConstants.ERROR_APPLY_DEVICE);
        }


    }

}
