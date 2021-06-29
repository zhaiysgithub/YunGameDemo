package kptech.game.kit.pass;

import android.app.Activity;
import android.app.Application;

import com.kptach.lib.inter.game.APIConstants;
import com.kptach.lib.inter.game.IDeviceControl;
import com.kptach.lib.inter.game.IGameBoxManager;
import com.kptach.lib.inter.game.IGameCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import kptech.game.kit.pass.manager.GameBoxManagerFactory;
import kptech.game.kit.pass.manager.KpPassCMWManager;
import kptech.game.kit.pass.manager.Logger;
import kptech.game.kit.pass.manager.PassCMWCallback;
import kptech.game.kit.pass.manager.PassConstants;
import kptech.game.kit.pass.manager.PassDeviceResponseBean;

public class PassV3 implements IGameBoxManager {
    private static final String TAG = "IGameBoxManager";
    String mCorpID;
    boolean mDebug;
    HashMap mParams;
    IGameBoxManager gameBoxManager;
    @Override
    public void initLib(Application application, HashMap params, IGameCallback<String> callback) {
        try {
            if (params != null){
                if (params.containsKey(PARAMS_KEY_DEBUG)){
                    mDebug = (Boolean) params.get(PARAMS_KEY_DEBUG);
                }
                if (params.containsKey(PARAMS_KEY_CORPID)){
                    mCorpID = (String) params.get(PARAMS_KEY_CORPID);
                }

                mParams = params;
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }


    }

    @Override
    public void applyCloudDevice(Activity activity, String gameInf, IGameCallback<IDeviceControl> callback) {
        String pkgName = "";
        try {
            JSONObject packInfo = new JSONObject(gameInf);
            pkgName = packInfo.optString("pkgName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        if (pkgName == null || "".equals(pkgName)){
//            callback.onGameCallback(null, APIConstants.ERROR_APPLY_DEVICE);
//            return;
//        }
        KpPassCMWManager.instance().startRequestPassCMW(mCorpID, pkgName, new PassCMWCallback() {
            @Override
            public void onSuccess(PassDeviceResponseBean result) {
                if (result == null){
                    callback.onGameCallback(null, APIConstants.ERROR_APPLY_DEVICE);
                    return;
                }
                Logger.info("KpPassCMWManager","result = " + result.toString());
                int code = result.code;
                if(code == PassConstants.PASS_CODE_SUCCESS){

                    HashMap<String,Object> sdkParams = new HashMap<>();
                    sdkParams.put("resource",result.data.resource);
                    sdkParams.put("direction",result.data.direction);
                    sdkParams.put(IGameBoxManager.PARAMS_KEY_DEBUG, mDebug);
                    sdkParams.put("deviceid",result.data.deviceid);
                    sdkParams.put("corpKey",mCorpID);
                    String iaas = result.data.iaas;

                    gameBoxManager = GameBoxManagerFactory.getGameBoxManager(iaas, activity.getApplication(), null);
                    gameBoxManager.initLib(activity.getApplication(), sdkParams, new IGameCallback<String>() {
                        @Override
                        public void onGameCallback(String msg, int code) {
                            if (code == 1) {
                                gameBoxManager.applyCloudDevice(activity, gameInf, callback);
                            }
                        }
                    });
                    return;
                }
                int erroCode = KpPassCMWManager.instance().getErrorCode(code);
                callback.onGameCallback(null, erroCode);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Logger.error("GamePlay", "申请设备接口失败,code = " + errorCode + "; errorMsg = " + errorMsg);
                callback.onGameCallback(null, APIConstants.ERROR_APPLY_DEVICE);
            }
        });
    }

}
