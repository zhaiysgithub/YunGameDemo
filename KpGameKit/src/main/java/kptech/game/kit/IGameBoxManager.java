package kptech.game.kit;

import android.app.Activity;
import android.app.Application;

import java.util.HashMap;
import java.util.List;

import kptech.game.kit.model.GameBoxConfig;

public interface IGameBoxManager {
    /**
     * 初始化
     * @param application
     * @param appKey
     * @param gameConfig
     * @param callback
     */
    void init(Application application, String appKey, GameBoxConfig gameConfig, APICallback<String> callback);


    /**
     * 初始化
     * @param application
     * @param appKey
     * @param callback
     */
    void init(Application application, String appKey, APICallback<String> callback);
    /**
     * 申请云设备
     * @param activity
     * @param inf
     * @param callback
     */
    void applyCloudDevice(Activity activity, GameInfo inf, APICallback<DeviceControl> callback);

    /**
     *
     * @param activity
     * @param pkgName
     * @param callback
     */
    void applyCloudDevice(Activity activity, String pkgName, APICallback<DeviceControl> callback);

    /**
     * 加入队列
     * @param pkgName
     * @param checkInterval
     * @param callback
     */
    void joinQueue(String pkgName, int checkInterval, APICallback<QueueRankInfo> callback);

    /**
     * 退出队列
     */
    void exitQueue();

    /**
     * 游戏列表获取，游戏列表支持分页获取。
     * @param page
     * @param limit
     * @return
     */
    List<GameInfo> queryGameList(int page, int limit);

    /**
     * 根据gid获取运营平台配置的游戏
     * @param gid
     * @return
     */
    GameInfo queryGame(int gid);

    /**
     * 根据包名获取运营平台配置的游戏列表
     * @param pkg
     * @return
     */
    List<GameInfo> queryGames(String pkg);
}
