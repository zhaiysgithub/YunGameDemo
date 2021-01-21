package kptach.game.kit.lib.baidu;

import android.app.Activity;
import android.app.Application;

import com.yd.yunapp.gameboxlib.APICallback;
import com.yd.yunapp.gameboxlib.APIConstants;
import com.yd.yunapp.gameboxlib.DeviceControl;
import com.yd.yunapp.gameboxlib.GameInfo;

import org.json.JSONObject;

import java.util.HashMap;

import kptach.game.kit.inter.game.IDeviceControl;
import kptach.game.kit.inter.game.IGameBoxManager;
import kptach.game.kit.inter.game.IGameCallback;
import kptach.game.kit.lib.baidu.utils.Logger;

public class BdGameBoxManager implements IGameBoxManager {
    private static final String TAG = BdGameBoxManager.class.getSimpleName();

    private boolean devLoading;
    private boolean isInited = false;

    private boolean debug = false;
    private String ak = "";
    private String sk = "";
    private String ch = "";

    private Application mApplication;

    @Override
    public void initLib(Application application, HashMap params, IGameCallback<String> callback) {
        mApplication = application;

        if (isInited){
            return;
        }
        try {
            if (params != null){
                if (params.containsKey(PARAMS_KEY_DEBUG)){
                    debug = (Boolean) params.get(PARAMS_KEY_DEBUG);
                }
                if (params.containsKey(PARAMS_KEY_BD_AK)){
                    ak = (String) params.get(PARAMS_KEY_BD_AK);
                }
                if (params.containsKey(PARAMS_KEY_BD_SK)){
                    sk = (String) params.get(PARAMS_KEY_BD_SK);
                }
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }


        com.yd.yunapp.gameboxlib.GameBoxManager.getInstance(application).setDebug(debug);
        //初始化游戏
        com.yd.yunapp.gameboxlib.GameBoxManager.getInstance(application).init(ak, sk, ch);

        isInited = true;
    }

    @Override
    public void applyCloudDevice(Activity activity, String inf, final IGameCallback<IDeviceControl> callback) {
        if (devLoading){
            return;
        }

        GameInfo game = getLibGameInfo(inf);
        if (game == null){
            if (callback != null){
                callback.onGameCallback(null, kptach.game.kit.inter.game.APIConstants.ERROR_GAME_INF_EMPTY);
            }
            return;
        }

        devLoading = true;
        com.yd.yunapp.gameboxlib.GameBoxManager manager = com.yd.yunapp.gameboxlib.GameBoxManager.getInstance(mApplication);
        manager.applyCloudDevice(game, new APICallback<DeviceControl>() {
            @Override
            public void onAPICallback(DeviceControl inner, int code) {
                devLoading = false;

                IDeviceControl control = null;
                if (code == APIConstants.APPLY_DEVICE_SUCCESS) {
                    control = new BdDeviceControl(inner);
                }

                if (callback != null){
                    callback.onGameCallback(control, code);
                }
            }
        });
    }

    private GameInfo getLibGameInfo(String inf){
        try {
            JSONObject obj = new JSONObject(inf);
            GameInfo game = new GameInfo();
            game.gid = obj.optInt("gid");
            game.pkgName = obj.optString("pkgName");
            game.iconUrl = obj.optString("iconUrl");
            game.usedTime = obj.optInt("usedTime");
            game.totalTime = obj.optInt("totalTime");
            game.name = obj.optString("name");
            game.playCount = obj.optInt("playCount");
            return game;
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }

        return null;

    }
}
