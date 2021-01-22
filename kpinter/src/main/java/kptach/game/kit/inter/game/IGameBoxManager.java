package kptach.game.kit.inter.game;

import android.app.Activity;
import android.app.Application;

import java.util.HashMap;

public interface IGameBoxManager {

    public static final String PARAMS_KEY_DEBUG = "debug";
    public static final String PARAMS_KEY_CORPID = "corpId";
    public static final String PARAMS_KEY_USERID = "userId";
    public static final String PARAMS_KEY_SDKURL = "sdkUrl";
    public static final String PARAMS_KEY_SDKVER = "sdkVer";

    public static final String PARAMS_KEY_BD_AK = "ak";
    public static final String PARAMS_KEY_BD_SK = "sk";

    public static final String PARAMS_KEY_PADINF = "padInf";

    /**
     * 初始化sdk
     * @param application
     * @param params
     * @param callback
     */
    void initLib(Application application, HashMap params, IGameCallback<String> callback);

    /**
     * 申请云设备
     * @param activity
     * @param gameInf
     * @param callback
     */
    void applyCloudDevice(Activity activity, String gameInf, IGameCallback<IDeviceControl> callback);
}
