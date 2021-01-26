package kptach.game.kit.lib.redfinger;

import android.app.Activity;
import android.app.Application;

import org.json.JSONObject;

import java.util.HashMap;

import kptach.game.kit.inter.game.APIConstants;
import kptach.game.kit.inter.game.IDeviceControl;
import kptach.game.kit.inter.game.IGameBoxManager;
import kptach.game.kit.inter.game.IGameCallback;
import kptach.game.kit.lib.redfinger.task.RequestDeviceTask;
import kptach.game.kit.lib.redfinger.utils.Logger;

public class RedGameBoxManager implements IGameBoxManager {

    private boolean debug = false;
    private String mCorpID = "";
    private String mUserID = "";
    private String mSdkUrl = "";
    private String mSdkVer = "";
    private String mPadInfo = "";

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
                if (params.containsKey(PARAMS_KEY_PADINF)){
                    mPadInfo = (String) params.get(PARAMS_KEY_PADINF);
                }
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
                .execute(mCorpID, pkgName, mUserID, kpGameId, mPadInfo);
    }

}
