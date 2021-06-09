package com.kptach.lib.game.huawei;

import android.app.Activity;
import android.app.Application;

import com.kptach.lib.inter.game.IDeviceControl;
import com.kptach.lib.inter.game.IGameBoxManager;
import com.kptach.lib.inter.game.IGameCallback;

import java.util.HashMap;

public class HWGameBoxManager implements IGameBoxManager {

    private Application mApplication;
    private String mResource;
    private boolean isDebug;

    @Override
    public void initLib(Application application, HashMap params, IGameCallback<String> iGameCallback) {
        mApplication = application;


        try {
            if (params != null){
                if (params.containsKey("resource")){
                    mResource = (String) params.get("resource");
                }
                if (params.containsKey(PARAMS_KEY_DEBUG)){
                    isDebug = (boolean) params.get(PARAMS_KEY_DEBUG);
                }

                HWCloudGameUtils.setDebug(isDebug);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void applyCloudDevice(Activity activity, String s, IGameCallback<IDeviceControl> iGameCallback) {


    }

    @Override
    public void createDeviceControl(Activity activity, String gameInf, HashMap<String, Object> params, IGameCallback<IDeviceControl> callback) {

        //创建 deviceControl
    }
}
