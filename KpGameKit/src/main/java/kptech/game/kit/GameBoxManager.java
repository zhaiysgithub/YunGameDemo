package kptech.game.kit;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import kptech.cloud.kit.msg.Messager;
import kptech.game.kit.ad.AdManager;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.data.RequestAppInfoTask;
import kptech.game.kit.data.RequestTask;
import kptech.game.kit.msg.MsgManager;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;


public class GameBoxManager {
    private static final Logger logger = new Logger("GameBoxManager") ;

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

    public static String mCorpID = "";

    private Context context;
    private static volatile GameBoxManager box = null;
    private com.yd.yunapp.gameboxlib.GameBoxManager mLibManager;
    private Messager mManager;
    private String mUniqueId;

    private InitHandler mHandler = new InitHandler();;

    private boolean isLibInited = false;

    private static boolean mDebug = false;
    public static void setDebug(boolean debug){
        mDebug = debug;

        //Logger
        Logger.setDebug(debug);
        //Messager
        MsgManager.setDebug(debug);
    }

    public static void init(@NonNull Application application, String appKey){
        mApplication = application;
        mCorpID = appKey;
        if (mApplication == null){
            logger.error("Init application is null");
            return;
        }
        if (mCorpID==null || "".equals(mCorpID.trim())){
            logger.error("Init appKey is null");
            return;
        }

        //发送请求
        InitHandler handler = getInstance(application).mHandler;
        handler.requestCount = 0;
        handler.sendEmptyMessage(1);
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

    private class InitHandler extends Handler{
        public InitHandler(){
            super(Looper.getMainLooper());
        }

        public int requestCount = 0;
        @Override
        public void handleMessage(@NonNull Message msg) {
            try {
                switch (msg.what){
                    case 1:
                        logger.info("gamebox request config corpId: "+ mCorpID);
                        requestCount++;
                        //发送请求获取配置信息
                        new RequestAppInfoTask(mApplication, new RequestAppInfoTask.ICallback() {
                            @Override
                            public void onResult(boolean ret) {
                                if (ret){
                                    //获取数据成功，初始化
                                    mHandler.sendEmptyMessage(2);
                                }else {
                                    //重试2次
                                    if (requestCount > 2){
                                        logger.error("retry request appinfo");
                                        //获取数据失败，重试一次
                                        mHandler.sendEmptyMessage(1);
                                    }else {
                                        logger.error(" request appinfo faile");

                                        //使用本地缓存，初始化
                                        mHandler.sendEmptyMessage(2);
                                    }
                                }
                            }
                        }).execute(mCorpID);
                        break;

                    case 2:

                        //初始化游戏信息
                        if (GameBoxManager.getInstance(mApplication).initLibManager()){
                            logger.info("gamebox initialized");
                        }else {
                            logger.error("gamebox init failure");
                        }

                        //初始化广告信息
                        if (AdManager.getInstance().init(mApplication)) {
                            logger.info("ad initialized");
                        }else {
                            logger.error("ad init failure");
                        }

                        //初始化通讯
                        MsgManager.init(mApplication);

//                        logger.error("init finished");

                        break;
                }
            }catch (Exception e){
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * 初始化gameBox
     */
    private boolean initLibManager(){

        String ak = ProferencesUtils.getString(context, SharedKeys.KEY_GAME_APP_KEY,null);
        String sk = ProferencesUtils.getString(context, SharedKeys.KEY_GAME_APP_SECRET,null);
        String ch = ProferencesUtils.getString(context, SharedKeys.KEY_GAME_APP_CHANNEL,null);
        if (ak!=null && sk != null){
            mLibManager.setDebug(mDebug);

            //初始化游戏
            mLibManager.init(ak, sk, ch);
            isLibInited = true;
            return true;
        }

        return false;
    }

    private com.yd.yunapp.gameboxlib.GameBoxManager getLibManager(){
        //处理未已初始化
        if (!isLibInited){
            logger.error("gamebox not initialized");
            return null;
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

        com.yd.yunapp.gameboxlib.GameBoxManager manager = getLibManager();
        if (manager == null){
            return;
        }

        com.yd.yunapp.gameboxlib.GameInfo game = inf.getLibGameInfo();
        manager.applyCloudDevice(game, playQueue, new com.yd.yunapp.gameboxlib.APICallback<com.yd.yunapp.gameboxlib.DeviceControl>() {
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
        com.yd.yunapp.gameboxlib.GameBoxManager manager = getLibManager();
        if (manager == null){
            return false;
        }
        return manager.joinQueue(info.getLibGameInfo(),checkInterval,new com.yd.yunapp.gameboxlib.APICallback<com.yd.yunapp.gameboxlib.QueueRankInfo>(){
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
        List<GameInfo> list = RequestTask.queryGameList(mCorpID, page, limit);
//        com.yd.yunapp.gameboxlib.GameBoxManager manager = getLibManager();
//        if (manager == null){
//            return null;
//        }
//        List<GameInfo> list = null;
//        List<com.yd.yunapp.gameboxlib.GameInfo> gameInfoArr = getLibManager().queryGameList(page,limit);
//        if (gameInfoArr!=null && gameInfoArr.size()>0){
//            list = new ArrayList<>();
//            for (int i = 0; i < gameInfoArr.size(); i++) {
//                list.add(new GameInfo(gameInfoArr.get(i)));
//            }
//        }
        return list;
    }

    /**
     * 根据gid获取运营平台配置的游戏
     * @param gid
     * @return
     */
    public GameInfo queryGame(int gid) {
        com.yd.yunapp.gameboxlib.GameBoxManager manager = getLibManager();
        if (manager == null){
            return null;
        }

        GameInfo inf = null;
        com.yd.yunapp.gameboxlib.GameInfo gameInfo = manager.queryGame(gid);
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
        com.yd.yunapp.gameboxlib.GameBoxManager manager = getLibManager();
        if (manager == null){
            return null;
        }

        List<GameInfo> list = null;
        List<com.yd.yunapp.gameboxlib.GameInfo> gameInfoArr = manager.queryGames(pkg);
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
        com.yd.yunapp.gameboxlib.GameBoxManager manager = getLibManager();
        if (manager == null){
            return null;
        }

        com.yd.yunapp.gameboxlib.GameInfo gameInfo = manager.updateGameInfo(game.getLibGameInfo());
        if (gameInfo!=null){
            return new GameInfo(gameInfo);
        }
        return null;
    }

    public void setSubChannel(String channel){
        com.yd.yunapp.gameboxlib.GameBoxManager manager = getLibManager();
        if (manager == null){
            return;
        }

        manager.setSubChannel(channel);
    }

    /**
     * 设置会员等级
     * @param level
     */
    public void setUserLevel(UserLevel level) {
        com.yd.yunapp.gameboxlib.GameBoxManager manager = getLibManager();
        if (manager == null){
            return;
        }

        manager.setMemberLevel(UserLevel.getMemberLevel(level));
    }

    /**
     * 获取当前会员等级
     * @return
     */
    public UserLevel getUserLevel() {
        com.yd.yunapp.gameboxlib.GameBoxManager manager = getLibManager();
        if (manager == null){
            return UserLevel.NORMAL;
        }
        return UserLevel.getUserLevel(manager.getMemberLevel());
    }

    /**
     * 队列加速
     * @param accCount 加速前进数量
     */
    public void accelerateQueue(int accCount) {
        com.yd.yunapp.gameboxlib.GameBoxManager manager = getLibManager();
        if (manager == null){
            return ;
        }
        manager.accelerateQueue(accCount);
    }

    /**
     * 退出队列,如果退出试玩，一定要调用此接口，否则肯会内存泄漏
     */
    public void exitQueue() {
        com.yd.yunapp.gameboxlib.GameBoxManager manager = getLibManager();
        if (manager == null){
            return ;
        }
        manager.exitQueue();
    }

    /**
     * 设置云手机参数信息，需要在申请设备之前进行设置
     *
     * @param key   目前支持imei，androidid
     * @param value
     */
    public void addDeviceMockInfo(@APIConstants.MockInfo String key, String value) {
        com.yd.yunapp.gameboxlib.GameBoxManager manager = getLibManager();
        if (manager == null){
            return ;
        }
        manager.addDeviceMockInfo(key, value);
    }

    /**
     * 移除云手机参数信息
     *
     * @param key 目前支持imei，androidid设置
     */
    public void removeDeviceMockInfo(@APIConstants.MockInfo String key) {
        com.yd.yunapp.gameboxlib.GameBoxManager manager = getLibManager();
        if (manager == null){
            return ;
        }
        manager.removeDeviceMockInfo(key);
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
