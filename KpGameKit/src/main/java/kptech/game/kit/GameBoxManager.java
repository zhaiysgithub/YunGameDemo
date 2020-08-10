package kptech.game.kit;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import kptech.cloud.kit.msg.Messager;
import kptech.game.kit.ad.AdManager;
import kptech.game.kit.msg.MsgManager;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.view.AdRemindDialog;


public class GameBoxManager {

    // app key. 查看地址： http://yunapp-console.bj.bcebos.com/sandbox_new/#/deviceGroups
    private static final String GAME_AK = "TOphL4quGn1a7dVRisS5ywU0";
    // app secret
    private static final String GAME_SK = "foeGZYkV4NOICn9Qpuq507ElvagTMHybhrKPLX6S";
    // 渠道值，自定义
    private static final String GAME_CH = "test";

    private static final String  AD_APP_KEY = "ZM_appSDK_00029";
    private static final String  AD_APP_TOKEN = "MadXeXeJf7zNzBIH";

    private static final String TAG = "GameManager";

    private static Application mApplication = null;

    private Context context;
    private static volatile GameBoxManager box = null;
    private com.yd.yunapp.gameboxlib.GameBoxManager mLibManager;
    private Messager mManager;
    private String mUniqueId;

    private boolean isLibInited = false;

    private static boolean mDebug = false;
    public static void setDebug(boolean debug){
        mDebug = debug;

        //Logger
        Logger.setDebug(debug);
        //Messager
        MsgManager.setDebug(debug);
    }

    public static void init(@NonNull Application application, String appKey, String appSecret){
        mApplication = application;

        //请求获取数据

        //根据配置初始化相应模块


        //初始化游戏
        GameBoxManager.getInstance(application).initLibManager(GAME_AK, GAME_SK, GAME_CH);
        //初始化通讯
//        MsgManager.init(application);
        //初始化广告
        AdManager.init(application,AD_APP_KEY,AD_APP_TOKEN);

    }

    public static GameBoxManager getInstance(Context context) {
        if (box == null) {
            synchronized(GameBoxManager.class) {
                if (box == null) {
                    box = new GameBoxManager(context);
                }
            }
        }
        return box;
    }

    private GameBoxManager(Context context){
        this.context = context;
        this.mLibManager = com.yd.yunapp.gameboxlib.GameBoxManager.getInstance(context);
    }

    /**
     * 初始化gameBox
     * @param appKey
     * @param appSecret
     * @param appChannel
     */
    private void initLibManager(String appKey, String appSecret, String appChannel){
        mLibManager.init(appKey, appSecret, appChannel);
        isLibInited = true;
    }

    private com.yd.yunapp.gameboxlib.GameBoxManager getLibManager(){
        //处理未已初始化
        if (!isLibInited){
            //初始化

            return mLibManager;
        }

        return mLibManager;
    }

    /**
     * 申请游戏的云设备
     * @param inf 游戏信息
     * @param callback 申请设备成功则返回状态码：APIConstants.API_CALL_SUCCESS和DeviceControl用于控制设备，否则返回对应错误码。
     */
    public void applyCloudDevice(@NonNull final GameInfo inf, @NonNull final APICallback<DeviceControl> callback){
        this.applyCloudDevice(inf, false, callback);
    }

    /**
     * 申请游戏的云设备
     * @param inf 游戏信息
     * @param playQueue 设备不足时，是否进入队列等待，默认false不等待。
     * @param callback 申请设备成功则返回状态码：APIConstants.API_CALL_SUCCESS和DeviceControl用于控制设备，否则返回对应错误码。
     */
    public void applyCloudDevice(@NonNull final GameInfo inf, final boolean playQueue, @NonNull final APICallback<DeviceControl> callback){
        if (inf == null){
            if (callback!=null){
                callback.onAPICallback(null, APIConstants.ERROR_GAME_INF_EMPTY);
            }
            return;
        }

        com.yd.yunapp.gameboxlib.GameInfo game = inf.getLibGameInfo();
        getLibManager().applyCloudDevice(game, playQueue, new com.yd.yunapp.gameboxlib.APICallback<com.yd.yunapp.gameboxlib.DeviceControl>() {
            @Override
            public void onAPICallback(com.yd.yunapp.gameboxlib.DeviceControl deviceControl, int i) {

                if (callback!=null){
                    DeviceControl control = null;
                    if (deviceControl!=null) {
                        control = new DeviceControl(deviceControl, inf);
                    }
                    callback.onAPICallback(control, i);
                }
            }
        });

    }

    /**
     * 加入队列，获取队列状态
     * @param info 游戏信息
     * @param checkInterval 队列检测时间间隔，单位秒
     * @param callback 回调
     * @return
     */
    public boolean joinQueue(GameInfo info, int checkInterval, final APICallback<QueueRankInfo> callback) {
        if (info == null){
            return false;
        }
        return getLibManager().joinQueue(info.getLibGameInfo(),checkInterval,new com.yd.yunapp.gameboxlib.APICallback<com.yd.yunapp.gameboxlib.QueueRankInfo>(){
            @Override
            public void onAPICallback(com.yd.yunapp.gameboxlib.QueueRankInfo queueRankInfo, int code) {
                if (callback!=null){
                    QueueRankInfo inf = null;
                    if (queueRankInfo!=null){
                        inf = new QueueRankInfo(queueRankInfo);
                    }
                    callback.onAPICallback(inf, code);
                }
            }
        });
    }

    /**
     * 游戏列表获取，游戏列表支持分页获取。
     * @param page
     * @param limit
     * @return
     */
    public List<GameInfo> queryGameList(int page, int limit) {
        List<GameInfo> list = null;
        List<com.yd.yunapp.gameboxlib.GameInfo> gameInfoArr = getLibManager().queryGameList(page,limit);
        if (gameInfoArr!=null && gameInfoArr.size()>0){
            list = new ArrayList<>();
            for (int i = 0; i < gameInfoArr.size(); i++) {
                list.add(new GameInfo(gameInfoArr.get(i)));
            }
        }
        return list;
    }

    /**
     * 根据gid获取运营平台配置的游戏
     * @param gid
     * @return
     */
    public GameInfo queryGame(int gid) {
        GameInfo inf = null;
        com.yd.yunapp.gameboxlib.GameInfo gameInfo = getLibManager().queryGame(gid);
        if (gameInfo !=null){
            inf = new GameInfo(gameInfo);
        }
        return inf;
    }

    /**
     * 根据包名获取运营平台配置的游戏列表
     * @param pkg
     * @return
     */
    public List<GameInfo> queryGames(String pkg) {
        List<GameInfo> list = null;
        List<com.yd.yunapp.gameboxlib.GameInfo> gameInfoArr = getLibManager().queryGames(pkg);
        if (gameInfoArr!=null && gameInfoArr.size()>0){
            list = new ArrayList<>();
            for (int i = 0; i < gameInfoArr.size(); i++) {
                list.add(new GameInfo(gameInfoArr.get(i)));
            }
        }
        return list;
    }

    /**
     * 更新单个游戏信息
     * @param game
     * @return
     */
    public GameInfo updateGameInfo(@NonNull GameInfo game) {
        com.yd.yunapp.gameboxlib.GameInfo gameInfo = getLibManager().updateGameInfo(game.getLibGameInfo());
        if (gameInfo!=null){
            return new GameInfo(gameInfo);
        }
        return null;
    }

    public void setSubChannel(String channel){
        getLibManager().setSubChannel(channel);
    }

    /**
     * 设置会员等级
     * @param level
     */
    public void setUserLevel(UserLevel level) {
        getLibManager().setMemberLevel(UserLevel.getMemberLevel(level));
    }

    /**
     * 获取当前会员等级
     * @return
     */
    public UserLevel getUserLevel() {
        return UserLevel.getUserLevel(getLibManager().getMemberLevel());
    }

    /**
     * 队列加速
     * @param accCount 加速前进数量
     */
    public void accelerateQueue(int accCount) {
        getLibManager().accelerateQueue(accCount);
    }

    /**
     * 退出队列,如果退出试玩，一定要调用此接口，否则肯会内存泄漏
     */
    public void exitQueue() {
        getLibManager().exitQueue();
    }

    /**
     * 设置联运帐号用户唯一标识
     * @param uid
     */
    public void setUniqueId(String uid){
        this.mUniqueId = uid;
    }

    /**
     * 获取当前联运帐号唯一标识
     * @return
     */
    public String getUniqueId(){
        return this.mUniqueId;
    }
}
