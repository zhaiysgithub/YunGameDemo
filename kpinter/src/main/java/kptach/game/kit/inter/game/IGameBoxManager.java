package kptach.game.kit.inter.game;

import android.app.Activity;
import android.app.Application;

import java.util.HashMap;

public interface IGameBoxManager {
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
     * @param inf
     * @param callback
     */
    void applyCloudDevice(Activity activity, GameInfo inf, IGameCallback<IDeviceControl> callback);
}
