package kptech.game.kit;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.kptach.lib.inter.game.IGameBoxManager;
import com.kptach.lib.inter.game.IGameCallback;

import kptech.game.kit.callback.PassCMWCallback;
import kptech.game.kit.manager.KpPassCMWManager;
import kptech.game.kit.model.PassDeviceResponseBean;
import kptech.game.kit.msg.mqtt.MsgSuper;
import kptech.game.kit.view.LoadingPageView;
import kptech.lib.ad.AdManager;
import kptech.lib.analytic.DeviceInfo;
import kptech.lib.analytic.Event;
import kptech.lib.analytic.EventCode;
import kptech.lib.analytic.MobclickAgent;
import kptech.game.kit.env.Env;
import kptech.lib.constants.SharedKeys;
import kptech.lib.constants.Urls;
import kptech.lib.data.RequestAppInfoTask;
import kptech.lib.data.RequestTask;
import kptech.lib.fatory.GameBoxManagerFactory;
import kptech.game.kit.utils.DeviceUtils;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.MillisecondsDuration;
import kptech.game.kit.utils.ProferencesUtils;
import com.kptach.lib.inter.game.APIConstants;


public class GameBoxManager {

    private static final String TAG = "GameBoxManager";
    private static Application mApplication = null;
    public static String mCorpID = "";
    private static volatile GameBoxManager box = null;
    private String mUniqueId;
    private MillisecondsDuration mTimeDuration;
    private boolean isInited = false;
    private boolean devLoading = false;
    private boolean mShowCustomerLoadingView;
    private LoadingPageView mCustomerLoadingView;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static boolean mDebug = false;
    public static void setDebug(boolean debug){
        mDebug = debug;

        //Logger
        if (!debug){
            Logger.setLevel(Logger.LEVEL_INFO);
        }

        //Messager
        MsgSuper.getInstance().setDebug(debug);
    }

    public static GameBoxManager getInstance() {
        if (box == null) {
            synchronized(GameBoxManager.class) {
                if (box == null) {
                    box = new GameBoxManager();
                }
            }
        }
        return box;
    }

    private GameBoxManager(){
//        this.mLibManager = com.yd.yunapp.gameboxlib.GameBoxManager.getInstance(context);
    }


    public void setLoadingView(boolean isShow,LoadingPageView loadingView){
        mShowCustomerLoadingView = isShow;
        mCustomerLoadingView = loadingView;
    }

    public static void setAppKey(String appKey) {
        mCorpID = appKey;
    }

    public boolean isGameBoxManagerInited(){
        return this.isInited;
    }

    public synchronized void init(Application application, String appKey, APICallback<String> callback){
        //判断已经初始化完成
        if (isGameBoxManagerInited()){
            if (callback != null){
                callback.onAPICallback("", 1);
            }
            return;
        }

        //环境
        Env.init(application);

        mApplication = application;
        mCorpID = appKey;
        if (mApplication == null){
            Logger.error("GameBoxManager","Init application is null");
            if (callback != null){
                callback.onAPICallback("Application is null", APIConstants.ERROR_GAME_INIT);
            }
            return;
        }
        if (mCorpID==null || "".equals(mCorpID.trim())){
            Logger.error("GameBoxManager","Init appKey is null");
            //回调初始化
            if (callback != null){
                callback.onAPICallback("CorID is null", APIConstants.ERROR_GAME_INIT);
            }
            return;
        }

        //初始化设备信息，并发送
        if (!DeviceInfo.hasDeviceId(mApplication)){
            DeviceInfo.sendDeviceInfo(mApplication, mCorpID);
        }

        try {
            //统计事件初始化
            Event.init(application, mCorpID);
        }catch (Exception e){
            Logger.error("GameBoxManager",e.getMessage());
        }

        mTimeDuration = new MillisecondsDuration();
        try {
            //发送打点事件
            Event event = Event.getEvent(EventCode.DATA_SDK_INIT_START);
            event.setExt(getDeviceInfo(application));
            MobclickAgent.sendEvent(event);

            Logger.info("GameBoxManager","deviceId: " + DeviceInfo.getDeviceId(mApplication));

        }catch (Exception e){
            Logger.error("GameBoxManager",e.getMessage());
        }

        //发送请求
        InitHandler mHandler = new InitHandler();
        mHandler.setCallback(callback);
        mHandler.sendEmptyMessage(1);

    }

    private HashMap<String,Object> getDeviceInfo(Context context){
        HashMap<String,Object> params = new HashMap<>();
        try {
            params.put("appVer", DeviceUtils.getVersionName(context));
            params.put("sdkVer", BuildConfig.VERSION_NAME);
            int netType = DeviceUtils.getNetworkType(context);
            String netStr = "unkwon";
            switch (netType){
                case ConnectivityManager.TYPE_MOBILE:
                    netStr = "mobile";
                    break;
                case ConnectivityManager.TYPE_WIFI:
                    netStr = "wifi";
                    break;
            }
            params.put("nettype", netStr);
            params.put("deviceId", DeviceInfo.getDeviceId(context));
        }catch (Exception e){
            e.printStackTrace();
        }
        return params;
    }

    public String getLogFilePath() {
        return "";
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
                        Logger.info("GameBoxManager","gamebox request config corpId: "+ mCorpID);
                        requestCount++;
                        //发送请求获取配置信息
                        new RequestAppInfoTask(mApplication, new RequestAppInfoTask.ICallback() {
                            @Override
                            public void onResult(boolean ret) {
                                if (ret){
                                    //获取数据成功，初始化
                                    GameBoxManager.InitHandler.this.sendEmptyMessage(2);
                                }else {
                                    //重试2次
                                    if (requestCount < 3){
                                        Logger.error("GameBoxManager","retry request appinfo");
                                        //获取数据失败，重试一次
                                        GameBoxManager.InitHandler.this.sendEmptyMessage(1);
                                    }else {
                                        Logger.error("GameBoxManager"," request appinfo faile");
                                        //使用本地缓存，初始化
                                        GameBoxManager.InitHandler.this.sendEmptyMessage(2);
                                    }
                                }
                            }
                        }).execute(mCorpID);
                        break;

                    case 2:
                        int initState = 0;

                        //初始化游戏信息
                        if (initLibManager()){
                            Logger.info("GameBoxManager","gamebox initialized");
                            initState = 1;
                        }else {
                            Logger.error("GameBoxManager","gamebox init failure");
                        }

                        //初始化广告信息
                        if (AdManager.init(mApplication)) {
                            Logger.info("GameBoxManager","ad initialized");
                        }

                        //初始化通讯
                        MsgSuper.getInstance().init(mApplication, mCorpID);

                        //回调初始化
                        if (this.callback != null){
                            this.callback.onAPICallback("", initState);
                        }

                        break;

                    case 3:
                        //继续申请设备

                        break;
                }
            }catch (Exception e){
                Logger.error("GameBoxManager", e.getMessage());
            }
        }
    }

    private static String AK;
    private static String SK;
    private static String CH;

    public static void setAppInfo(String ak, String sk, String ch){
        AK = ak;
        SK = sk;
        CH = ch;
    }

    /**
     * 初始化gameBox
     */
    private boolean initLibManager(){

        if (AK == null || SK == null){
            AK = ProferencesUtils.getString(mApplication, SharedKeys.KEY_GAME_APP_KEY,null);
            SK = ProferencesUtils.getString(mApplication, SharedKeys.KEY_GAME_APP_SECRET,null);
        }

        boolean ret = true;
        try {
            //发送打点事件
            Event event = Event.getEvent(ret ? EventCode.DATA_SDK_INIT_OK : EventCode.DATA_SDK_INIT_FAILED);
            HashMap<String,Object> ext = new HashMap<>();
            ext.put("ak",AK);
            ext.put("sk",SK);
            event.setExt(ext);
            MobclickAgent.sendEvent(event);
        }catch (Exception e){
            e.printStackTrace();
        }


        try {
            //发送事件耗时统计
            long useTm = mTimeDuration.duration();
            HashMap<String,Object> data = new HashMap<>();
            data.put("state", ret ? "ok" : "failed");
            data.put("stime", mTimeDuration.getSavedTime());
            data.put("etime", mTimeDuration.getCurentTime());
            Event event = Event.getTMEvent(EventCode.DATA_TMDATA_SDKINIT_END, useTm, data);
            MobclickAgent.sendTMEvent(event);

            mTimeDuration = new MillisecondsDuration();

        }catch (Exception e){
            e.printStackTrace();
        }

        isInited = true;

        return true;
    }

    /**
     * 申请游戏的云设备
     * @param inf 游戏信息
     * @param callback 申请设备成功则返回状态码：APIConstants.API_CALL_SUCCESS和DeviceControl用于控制设备，否则返回对应错误码。
     */
    public synchronized void applyCloudDevice(final Activity activity, final GameInfo inf,final APICallback<IDeviceControl> callback){
        if (devLoading){
            return;
        }
        if (activity == null || activity.isFinishing()) {
            return;
        }

        if (inf == null){
            if (callback != null){
                callback.onAPICallback(null, APIConstants.ERROR_GAME_INFO);
            }
            return;
        }

        try {
            //重置打点数据
            Event.resetTrackIdFromBase();

            HashMap<String,Object> ext = new HashMap<>();
            ext.put("gid", inf.gid);
            ext.put("useSDK", inf.useSDK.name());

            //发送打点事件
            MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_DEVICE_APPLY_START, inf.pkgName, ext));

            if (mTimeDuration == null) {
                mTimeDuration = new MillisecondsDuration();
            }

            //发送事件耗时统计
            long useTm = mTimeDuration.duration();
            HashMap<String,Object> data = new HashMap<>();
            data.put("stime", mTimeDuration.getSavedTime());
            data.put("etime", mTimeDuration.getCurentTime());
            Event event = Event.getTMEvent(EventCode.DATA_TMDATA_DEVICE_START, useTm, data);
            event.setGamePkg(inf.pkgName);
            MobclickAgent.sendTMEvent(event);

            mTimeDuration = new MillisecondsDuration();
        }catch (Exception e){
            e.printStackTrace();
        }
        devLoading = true;

        boolean useSDK2 = BuildConfig.useSDK2;
        if (useSDK2){
            applyDeviceBy2(activity, inf, callback);
        } else {
            applyDeviceBy3(activity, inf, callback);
        }
    }

    /**
     * 使用SDK2.0打包
     */
    private void applyDeviceBy2(final Activity activity, final GameInfo info, final APICallback<IDeviceControl> callback){
        HashMap<String,Object> sdkParams = new HashMap<>();
        sdkParams.put(IGameBoxManager.PARAMS_KEY_DEBUG, mDebug);
        sdkParams.put(IGameBoxManager.PARAMS_KEY_CORPID, mCorpID);
        sdkParams.put(IGameBoxManager.PARAMS_KEY_USERID, DeviceInfo.getUserId(mApplication));
        sdkParams.put(IGameBoxManager.PARAMS_KEY_SDKURL, Urls.GET_DEVICE_CONNECT);
        sdkParams.put(IGameBoxManager.PARAMS_KEY_SDKVER, BuildConfig.VERSION_NAME);
        sdkParams.put(IGameBoxManager.PARAMS_KEY_BD_AK, AK);
        sdkParams.put(IGameBoxManager.PARAMS_KEY_BD_SK, SK);

        IGameBoxManager gameBoxManager = GameBoxManagerFactory.getGameBoxManager(info.useSDK, mApplication, sdkParams);
        gameBoxManager.applyCloudDevice(activity, info.toJsonString(), new IGameCallback<com.kptach.lib.inter.game.IDeviceControl>() {
            @Override
            public void onGameCallback(com.kptach.lib.inter.game.IDeviceControl innerControl, int code) {
                dealApplyDeviceCallback(innerControl, code, info, callback);
            }
        });
    }

    /**
     * 使用SDK3.0打包
     */
    private void applyDeviceBy3(final Activity activity, final GameInfo info, final APICallback<IDeviceControl> callback){

        KpPassCMWManager.instance().startRequestPassCMW(mCorpID, info.pkgName, new PassCMWCallback() {
            @Override
            public void onSuccess(PassDeviceResponseBean result) {
                if (result == null){
                    devLoading=false;
                    callback.onAPICallback(null, APIConstants.ERROR_APPLY_DEVICE);
                    return;
                }
                Logger.info("KpPassCMWManager","result = " + result.toString());
                int code = result.code;
                if(code == PassConstants.PASS_CODE_SUCCESS){
                    initDeviceControl(activity, result.data
                            , info, callback);
                    return;
                }
                devLoading = false;
                int erroCode = KpPassCMWManager.instance().getErrorCode(code);
                callback.onAPICallback(null, erroCode);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                devLoading = false;
                Logger.error("GamePlay", "申请设备接口失败,code = " + errorCode + "; errorMsg = " + errorMsg);
                callback.onAPICallback(null, APIConstants.ERROR_APPLY_DEVICE);
            }
        });
    }

    //启动游戏创建deviceControl
    private void initDeviceControl(@NonNull Activity activity, PassDeviceResponseBean.PassData data
            ,@NonNull final GameInfo inf
            , @NonNull final APICallback<IDeviceControl> callback) {

        HashMap<String,Object> sdkParams = new HashMap<>();
        sdkParams.put("resource",data.resource);
        sdkParams.put("direction",data.direction);
        sdkParams.put(IGameBoxManager.PARAMS_KEY_DEBUG, BuildConfig.DEBUG);
        sdkParams.put("deviceid",data.deviceid);
        String iaas = data.iaas;
        if (iaas.equals("BD")){
            inf.useSDK = GameInfo.SdkType.BD;
        }else if(iaas.equals("HW")){
            inf.useSDK = GameInfo.SdkType.HW;
            sdkParams.put("corpKey",mCorpID);
            sdkParams.put("sdkVersion", BuildConfig.VERSION_NAME);
        }

        IGameBoxManager gameBoxManager = GameBoxManagerFactory.getGameBoxManager(inf.useSDK, activity.getApplication(),sdkParams);
        gameBoxManager.createDeviceControl(activity, inf.toJsonString(), sdkParams, new IGameCallback<com.kptach.lib.inter.game.IDeviceControl>(){

            @Override
            public void onGameCallback(com.kptach.lib.inter.game.IDeviceControl innerControl, int code) {
                dealApplyDeviceCallback(innerControl, code, inf, callback);
            }
        });
    }

    /**
     * 处理申请设备后的逻辑
     */
    private void dealApplyDeviceCallback(com.kptach.lib.inter.game.IDeviceControl innerControl, int code
            , GameInfo inf, final APICallback<IDeviceControl> callback){
        devLoading = false;

        DeviceControl control = null;
        if (innerControl != null){
            control = new DeviceControl(innerControl, inf);
            control.setCorpKey(mCorpID);
        }

        try {
            //发送打点事件
            Event event = Event.getEvent(EventCode.getDeviceEventCode(code), inf.pkgName);
            if (control != null){
                event.setPadcode(control.getPadcode());
            }
            event.setErrMsg(""+code);
            HashMap<String,Object> ext = new HashMap<>();
            ext.put("code", code);
            if(innerControl!=null && innerControl.getSdkType()!=null){
                ext.put("useSDK", innerControl.getSdkType().toString());
            }
            event.setExt(ext);
            MobclickAgent.sendEvent(event);
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            //发送事件耗时统计
            long useTm = mTimeDuration.duration();
            HashMap<String,Object> data = new HashMap<>();
            data.put("stime", mTimeDuration.getSavedTime());
            data.put("etime", mTimeDuration.getCurentTime());
            data.put("state", code == APIConstants.APPLY_DEVICE_SUCCESS ? "ok" : "failed");
            data.put("code", code);
            Event event = Event.getTMEvent(EventCode.DATA_TMDATA_DEVICE_END, useTm, data);
            event.setGamePkg(inf.pkgName);
            if (control != null){
                event.setPadcode(control.getPadcode());
            }
            MobclickAgent.sendTMEvent(event);

            mTimeDuration = null;
        }catch (Exception e){
            e.printStackTrace();
        }

        //回调方法
        callback.onAPICallback(control, code);
    }

    /**
     * 游戏列表获取，游戏列表支持分页获取。
     * @param page
     * @param limit
     * @return
     */
    public List<GameInfo> queryGameList(int page, int limit) {
        if (mCorpID != null && !"".equals(mCorpID)){
            List<GameInfo> list = null;
            if (BuildConfig.useSDK2){
                list = RequestTask.queryGameList(mCorpID, page, limit);
            }else {
                list = RequestTask.queryGameListByPass3(mCorpID, page, limit);
            }

            return list;
        }
        return null;
    }

    /**
     * 根据gid获取运营平台配置的游戏
     * @param gid
     * @return
     */
    public GameInfo queryGame(int gid) {
//        com.yd.yunapp.gameboxlib.GameBoxManager manager = getLibManager();
//        if (manager == null){
//            return null;
//        }
//
//        GameInfo inf = null;
//        com.yd.yunapp.gameboxlib.GameInfo gameInfo = manager.queryGame(gid);
//        if (gameInfo !=null){
//            inf = new GameInfo(gameInfo);
//        }
//        return inf;
        return null;
    }

    /**
     * 根据包名获取运营平台配置的游戏列表
     * @param pkg
     * @return
     */
    public List<GameInfo> queryGames(String pkg) {
//        com.yd.yunapp.gameboxlib.GameBoxManager manager = getLibManager();
//        if (manager == null){
//            return null;
//        }
//
//        List<GameInfo> list = null;
//        List<com.yd.yunapp.gameboxlib.GameInfo> gameInfoArr = manager.queryGames(pkg);
//        if (gameInfoArr!=null && gameInfoArr.size()>0){
//            list = new ArrayList<>();
//            for (int i = 0; i < gameInfoArr.size(); i++) {
//                list.add(new GameInfo(gameInfoArr.get(i)));
//            }
//        }
//        return list;
        return null;
    }

    /**
     * 设置联运帐号用户唯一标识
     */
    public void setUniqueId(String uid){
        this.mUniqueId = uid;
    }

    public void setDevLoading(boolean devLoading) {
        this.devLoading = devLoading;
    }

    /**
     * 获取当前联运帐号唯一标识
     */
    public String getUniqueId(){
        return this.mUniqueId;
    }

    public boolean ismShowCustomerLoadingView() {
        return mShowCustomerLoadingView;
    }

    public LoadingPageView getmCustomerLoadingView() {
        return mCustomerLoadingView;
    }
}
