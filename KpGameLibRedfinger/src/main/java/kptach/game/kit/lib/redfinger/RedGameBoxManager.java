package kptach.game.kit.lib.redfinger;

import android.app.Activity;
import android.app.Application;

import com.mci.commonplaysdk.PlayMCISdkManager;

import java.util.HashMap;

import kptach.game.kit.inter.game.GameInfo;
import kptach.game.kit.inter.game.IDeviceControl;
import kptach.game.kit.inter.game.IGameBoxManager;
import kptach.game.kit.inter.game.IGameCallback;
import kptach.game.kit.lib.redfinger.task.RequestDeviceTask;

public class RedGameBoxManager implements IGameBoxManager {

    public static boolean debug = false;
    public static String mCorpID = "";
    public static String mUserID = "";
    public static String mSdkUrl = "";
    public static String mSdkVer = "";

    private boolean devLoading;

    @Override
    public void initLib(Application application, HashMap params, IGameCallback<String> callback) {
        try {
            if (params != null){
                if (params.containsKey("debug")){
                    debug = (Boolean) params.get("debug");
                }
                if (params.containsKey("corpId")){
                    mCorpID = (String) params.get("corpId");
                }
                if (params.containsKey("userId")){
                    mUserID = (String) params.get("userId");
                }
                if (params.containsKey("sdkUrl")){
                    mSdkUrl = (String) params.get("sdkUrl");
                }
                if (params.containsKey("sdkVer")){
                    mSdkVer = (String) params.get("sdkVer");
                }
            }
        }catch (Exception e){}
    }

    @Override
    public void applyCloudDevice(Activity activity, final GameInfo inf, final IGameCallback<IDeviceControl> callback) {
        if (devLoading){
            return;
        }
        devLoading = true;
        new RequestDeviceTask()
                .setSdkUrl(mSdkUrl)
                .setSdkVer(mSdkVer)
                .setCallback(new RequestDeviceTask.ICallback() {
                    @Override
                    public void onResult(int code, String devInfo) {
                        devLoading = false;

                        IDeviceControl control = null;
                        if (code == APIConstants.APPLY_DEVICE_SUCCESS) {
                            control = new RedDeviceControl(devInfo, inf);
                        }

                        if (callback != null) {
                            callback.onGameCallback(control, code);
                        }
                    }
                })
                .execute(mCorpID, inf.pkgName, mUserID, inf.kpGameId);
    }

}
