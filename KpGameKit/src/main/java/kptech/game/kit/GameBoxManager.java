package kptech.game.kit;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.bun.miitmdid.core.JLibrary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kptech.game.kit.ad.AdManager;
import kptech.game.kit.analytic.Event;
import kptech.game.kit.analytic.EventCode;
import kptech.game.kit.analytic.MobclickAgent;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.data.RequestAppInfoTask;
import kptech.game.kit.data.RequestTask;
import kptech.game.kit.msg.MsgManager;
import kptech.game.kit.utils.DeviceUtils;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.MiitHelper;
import kptech.game.kit.utils.ProferencesUtils;
import kptech.game.kit.utils.StringUtil;


public class GameBoxManager {

    private static final Logger logger = new Logger("GameBoxManager") ;

    private static Application mApplication = null;

    public static String mCorpID = "";

    private Context context;
    private static volatile GameBoxManager box = null;
    private com.yd.yunapp.gameboxlib.GameBoxManager mLibManager;
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
    public static void setAppKey(String appKey){
        if (!StringUtil.isEmpty(appKey)){
            mCorpID = appKey;
        }
    }

    private GameBoxManager(Context context){
        this.context = context;
        this.mLibManager = com.yd.yunapp.gameboxlib.GameBoxManager.getInstance(context);
    }

    public boolean isGameBoxManagerInited(){
        return this.isLibInited;
    }

    public synchronized void init(@NonNull Application application, String appKey, APICallback<String> callback){
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

//        try {
//            JLibrary.InitEntry(application);
//            new MiitHelper(new MiitHelper.AppIdsUpdater() {
//                @Override
//                public void OnIdsAvalid(@NonNull String ids) {
//                    logger.info("oaid: "+ids);
//                }
//            }).getDeviceIds(mApplication);
//        }catch (Exception e){
//            logger.error(e.getMessage());
//        }


        try {
            //统计事件初始化
            Event.init(application, mCorpID);

            //发送打点事件
            Event event = Event.getEvent(EventCode.DATA_SDK_INIT_START);
            event.setExt(getDeviceInfo(application));
            MobclickAgent.sendEvent(event);
        }catch (Exception e){
            logger.error(e.getMessage());
        }


        //发送请求
        InitHandler mHandler = new InitHandler();
        mHandler.setCallback(callback);
        mHandler.sendEmptyMessage(1);

    }

    private HashMap getDeviceInfo(Context context){
        HashMap<String,Object> params = new HashMap<>();
        try {
            params.put("imei", DeviceUtils.getDeviceId(context));
            params.put("ip", DeviceUtils.getIPAddress(context));
            params.put("phoneBrand", DeviceUtils.getPhoneBrand());
            params.put("phoneType", DeviceUtils.getPhoneModel());
            params.put("screenSize", DeviceUtils.getPhysicsScreenSize(context));
            params.put("appVer", DeviceUtils.getVersionName(context));
            params.put("sdkVer", BuildConfig.VERSION_NAME);
            params.put("androidLevel", DeviceUtils.getBuildLevel());
            params.put("androidVer", DeviceUtils.getBuildVersion());
        }catch (Exception e){
        }
        return params;
    }


    private class InitHandler extends Handler{
        public InitHandler(){
            super(Looper.getMainLooper());
        }
        private int requestCount = 0;
        private APICallback<String> callback;
        private void setCallback(APICallback<String> callback){
            this.callback = callback;
        }
        @Override
        public void handleMessage(@NonNull final Message msg) {
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
                                    InitHandler.this.sendEmptyMessage(2);
                                }else {
                                    //重试2次
                                    if (requestCount > 2){
                                        logger.error("retry request appinfo");
                                        //获取数据失败，重试一次
                                        InitHandler.this.sendEmptyMessage(1);
                                    }else {
                                        logger.error(" request appinfo faile");
                                        //使用本地缓存，初始化
                                        InitHandler.this.sendEmptyMessage(2);
                                    }
                                }
                            }
                        }).execute(mCorpID);
                        break;

                    case 2:
                        int initState = 0;

                        //初始化游戏信息
                        if (GameBoxManager.getInstance(mApplication).initLibManager()){
                            logger.info("gamebox initialized");
                            initState = 1;
                        }else {
                            logger.error("gamebox init failure");
                        }

                        //初始化广告信息
                        if (AdManager.init(mApplication)) {
                            logger.info("ad initialized");
                        }else {
                            logger.error("ad init failure");
                        }

                        //初始化通讯
                        MsgManager.init(mApplication);

                        //回调初始化
                        if (this.callback != null){
                            this.callback.onAPICallback("", initState);
                        }

                        break;
                }
            }catch (Exception e){
                logger.error(e.getMessage());
            }
        }
    }

    private static String tmpAK;
    private static String tmpSK;
    private static String tmpCH;

    public static void setAppInfo(String ak, String sk, String ch){
        tmpAK = ak;
        tmpSK = sk;
        tmpCH = ch;
    }

    /**
     * 初始化gameBox
     */
    private boolean initLibManager(){

        boolean ret = false;
        String ak = tmpAK!=null ? tmpAK : ProferencesUtils.getString(context, SharedKeys.KEY_GAME_APP_KEY,null);
        String sk = tmpSK!= null ? tmpSK : ProferencesUtils.getString(context, SharedKeys.KEY_GAME_APP_SECRET,null);
        String ch = tmpCH!= null ? tmpCH : ProferencesUtils.getString(context, SharedKeys.KEY_GAME_APP_CHANNEL,null);
        if (ak!=null && sk != null){
            mLibManager.setDebug(mDebug);

            //初始化游戏
            mLibManager.init(ak, sk, ch);
            isLibInited = true;

            ret = true;
        }

        try {
            //发送打点事件
            Event event = Event.getEvent(ret ? EventCode.DATA_SDK_INIT_OK : EventCode.DATA_SDK_INIT_FAILED);
            HashMap ext = new HashMap();
            ext.put("ak",ak);
            ext.put("sk",sk);
            ext.put("ch",ch);
            event.setExt(ext);
            MobclickAgent.sendEvent(event);
        }catch (Exception e){}

        return ret;
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
    public void applyCloudDevice(@NonNull Activity activity,  @NonNull final GameInfo inf, @NonNull final APICallback<DeviceControl> callback){
        this.applyCloudDevice(activity, inf, false, callback);
    }

    /**
     * 申请游戏的云设备
     * @param inf 游戏信息
     * @param playQueue 设备不足时，是否进入队列等待，默认false不等待。
     * @param callback 申请设备成功则返回状态码：APIConstants.API_CALL_SUCCESS和DeviceControl用于控制设备，否则返回对应错误码。
     */
    public void applyCloudDevice(@NonNull Activity activity, @NonNull final GameInfo inf, final boolean playQueue, @NonNull final APICallback<DeviceControl> callback){
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

        //获取设备id, 发送到云手机，用来解决风控问题
        if (inf.addMockInfo == 1){
            //297ebd358f8d1d5f,  //864131034311009 //VM010127052028
            //DeviceInfo{id=0, status=0, deviceId='VM010127052028', token='{"webControlList":[{"webControlCode":"XA-WEBSOCKET-CONTROL-41","webControlInfoList":[{"controlIp":"xian.cloud-control.top","controlPort":9741}]}],"controlList":[{"controlCode":"XA-USER-CONTROL-41","controlInfoList":[{"controlIp":"xian.cloud-control.top","controlPort":9641}]}],"padList":[{"controlCode":"XA-USER-CONTROL-41","padCode":"VM010127052028","padStatus":"1","padType":"0","videoCode":"GZ-TEST-USER-VIDEO-01"}],"videoList":[{"videoCode":"GZ-TEST-USER-VIDEO-01","videoInfoList":[{"videoUrl":"rtmp://117.48.196.66:110/live","videoProtocol":"2","videoDomain":"live","videoPort":110,"videoContext":"1"},{"videoUrl":"rtmp://117.48.196.66:1936/live","videoProtocol":"","videoDomain":"","videoPort":-1,"videoContext":""}]}],"wssWebControlList":[{"wssWebControlInfoList":[{"controlIp":"xian.cloud-control.top","controlPort":9841}],"wssWebControlCode":"XA-WSS-CONTROL-41"}],"webRtcControlList":[{"webRtcControlInfoList":[{"controlIp":"10.3.98.1","controlPort":9641}],"controlCode":"XA-USER-CONTROL-41","gateway":{"gatewayWssPort":8191,"gatewayIp":"xian.cloud-control.top","gatewayPort":8190}}],"sessionId":"b6d822fcc481462ead6c57741bf6d3f0","userId":11357855}', type=0, usedTime=0, totalTime=86400, gop=50, bitRate=3600, compressionType=VPU, maxDescentFrame=1, maxFrameRate=30, minDescentFrame=1, minFrameRate=20, picQuality=GRADE_LEVEL_HD, resolution=LEVEL_720_1280, sound=true, queueInfo=null}
            try {
                String ANDROID_ID = Settings.System.getString(mApplication.getContentResolver(), Settings.System.ANDROID_ID);
                String Imei = DeviceUtils.getIMEI(mApplication);

                manager.addDeviceMockInfo(com.yd.yunapp.gameboxlib.APIConstants.MOCK_IMEI, Imei);
                manager.addDeviceMockInfo(com.yd.yunapp.gameboxlib.APIConstants.MOCK_ANDROID_ID, ANDROID_ID);
            }catch (Exception e){
            }
        }


        try {
            //发送打点事件
            MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_DEVICE_APPLY_START, inf.pkgName));
        }catch (Exception e){}


        //预加载广告
        final AdManager adManager = AdManager.adEnable ? new AdManager(activity) : null;
        if (adManager!=null){
            adManager.setPackageName(inf.pkgName);
            adManager.prepareAd();
        }

        //申请云手机
        com.yd.yunapp.gameboxlib.GameInfo game = inf.getLibGameInfo();
        manager.applyCloudDevice(game, playQueue, new com.yd.yunapp.gameboxlib.APICallback<com.yd.yunapp.gameboxlib.DeviceControl>() {
            @Override
            public void onAPICallback(com.yd.yunapp.gameboxlib.DeviceControl deviceControl, int code) {

                DeviceControl control = null;
                if (deviceControl!=null) {
                    control = new DeviceControl(deviceControl, inf);
                    control.setAdManager(adManager);
                }

                //回调方法
                if (callback!=null){
                    callback.onAPICallback(control, code);
                }

                try {
                    //发送打点事件
                    Event event = Event.getEvent(EventCode.getDeviceEventCode(code), inf.pkgName);
                    if (control != null){
                        event.setPadcode(control.getPadcode());
                    }
                    event.setErrMsg(""+code);
                    HashMap ext = new HashMap<>();
                    ext.put("code", code);
                    event.setExt(ext);
                    MobclickAgent.sendEvent(event);
                }catch (Exception e){}

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
        if (mCorpID != null && !"".equals(mCorpID)){
            List<GameInfo> list = RequestTask.queryGameList(mCorpID, page, limit);
            return list;
        }

        com.yd.yunapp.gameboxlib.GameBoxManager manager = getLibManager();
        if (manager == null){
            //等待1秒
            try {
                Thread.sleep(1000);
                manager = getLibManager();
            }catch (Exception e){}
        }

        if (manager == null){
            return null;
        }

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
