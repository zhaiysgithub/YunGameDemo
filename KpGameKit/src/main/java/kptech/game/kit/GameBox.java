package kptech.game.kit;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;


import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.xutils.x;

import kptech.game.kit.activity.GamePlay;
import kptech.game.kit.callback.OnAuthCallback;
import kptech.game.kit.manager.UserAuthManager;
import kptech.game.kit.utils.Logger;
import kptech.lib.analytic.Event;
import kptech.lib.analytic.EventCode;
import kptech.lib.analytic.MobclickAgent;

public class GameBox {

    private static volatile GameBox box = null;

    public Application mApplication = null;
    public String appKey = null;

//    public static RefWatcher sRefWatcher;

    public static void init(@NonNull Application application, String appKey) {
//        sRefWatcher = LeakCanary.install(application);

        if (box == null) {
            synchronized(GameBoxManager.class) {
                if (box == null) {
                    box = new GameBox(application, appKey);
                }
            }
        }
    }

    public static GameBox getInstance() {
        return box;
    }

    private GameBox(Application application, String appKey){
        x.Ext.init(application);
//        x.Ext.setDebug(BuildConfig.DEBUG); //输出debug日志，开启会影响性能
        this.mApplication = application;
        this.appKey = appKey;
        GameBoxManager.setAppKey(appKey);
    }
    public void playGame(Activity activity, GameInfo gameInfo){
        this.playGame(activity,gameInfo,null);
    }

    public void playGame(Activity activity,GameInfo gameInfo,String guid, String token, String phone){
        if (gameInfo != null){
            Map<String,String> paramMap = new HashMap<>();
            paramMap.put("guid",guid);
            paramMap.put("token",token);
            paramMap.put("phone",phone);
            paramMap.put("platform",appKey);
            String paramJson = new JSONObject(paramMap).toString();

            Params params = new Params();
            params.put(ParamKey.GAME_AUTH_UNION_GID, paramJson);
            this.playGame(activity, gameInfo,params);
        }
    }
    public void playGame(Activity activity, GameInfo gameInfo, Params params){
        if (activity==null || gameInfo==null){
            Logger.error("GameBox", "playGame error, activity:" + activity + ", gameInfo:" + gameInfo );
            return;
        }

        try {
            //判断本地是否已经安装
            // 优先级 ： 本地游戏最高，其次是微包，
            String packageName = activity.getApplicationInfo().packageName;
            String gamePkgName = gameInfo.pkgName; //游戏包名
            String weiBaoPkgName = gamePkgName + ".kpmini"; //微包包名
            PackageManager packageManager = activity.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(gamePkgName);
            if (!packageName.equals(weiBaoPkgName)){ //非微包
                if(intent != null){
                    //启动原游戏包
                    Logger.info("GameBox", "本地已安装游戏："+gameInfo.pkgName);
                    activity.startActivity(intent);
                    return;
                }else {
                    //启动微包
                    Intent weiBaoIntent = packageManager.getLaunchIntentForPackage(weiBaoPkgName);
                    if (weiBaoIntent != null){
                        Logger.info("GameBox", "本地微包游戏："+ weiBaoPkgName);
                        activity.startActivity(weiBaoIntent);
                        return;
                    }
                }
            }else {
                //启动原游戏包
                if(intent != null){
                    //启动原游戏包
                    Logger.info("GameBox", "本地已安装游戏："+gameInfo.pkgName);
                    activity.startActivity(intent);
                    return;
                }
            }
        }catch (Exception e){
            Logger.error("GameBox", "获取本地游戏，error:" + e.getMessage());
        }

        Logger.info("GameBox", "启动云游戏，gameInfo:" + gameInfo.toString());

        //启动云游戏
        Intent intent = new Intent(activity, GamePlay.class);
        intent.putExtra(GamePlay.EXTRA_CORPID, this.appKey);
        intent.putExtra(GamePlay.EXTRA_GAME, gameInfo);
        intent.putExtra(GamePlay.EXTRA_PARAMS, params);

        activity.startActivity(intent);
    }

    /**
     * 开始用户游戏登录
     * @param context Activity 对象
     * @param gameInfo GameInfo 对象
     * @param phoneNum 手机号
     * @param callback 回调
     */
    private void startLogin(final Activity context, final GameInfo gameInfo, String phoneNum, final OnAuthCallback callback){

        if (callback == null){
            return;
        }

        try {
            //统计事件初始化
            Event.init(context.getApplication(), appKey);

        }catch (Exception e){
            Logger.error("GameBoxManager",e.getMessage());
        }

        boolean shouldAuth = UserAuthManager.getInstance().shouldLoginAuthByPhone(context, gameInfo.pkgName, phoneNum);
        if (shouldAuth){

            startCertification(context, "", "", phoneNum, gameInfo, new OnAuthCallback() {
                @Override
                public void onCerSuccess(String gid, String token) {
                    callback.onCerSuccess(gid, token);
                    playGame(context, gameInfo);
                }

                @Override
                public void onCerError(int code, String errorStr) {
                    callback.onCerError(code, errorStr);
                }
            });
        } else {
            String gidValue = UserAuthManager.getInstance().getGidValue();
            String tokenValue = UserAuthManager.getInstance().getTokenValue();
            callback.onCerSuccess(gidValue, tokenValue);

            Map<String,Object> map = new HashMap<>();
            map.put("phone",phoneNum);
            map.put("platform","realname_auth");
            mobSendEvent(EventCode.DATA_REALNAME_AUTH_ENTER, gameInfo.pkgName, map);

            playGame(context, gameInfo);
        }
    }

    /**
     * 开始执行认证
     * @param context Activity 对象
     * @param userName 用户姓名
     * @param userIdCard 用户身份信息
     * @param userPhone 用户手机号
     * @param gameInfo 游戏信息
     */
    public void startCertification(final Activity context, String userName, String userIdCard, final String userPhone,
                                   final GameInfo gameInfo, final OnAuthCallback callback){
        if(callback == null){
            return;
        }

        Map<String,Object> map = new HashMap<>();
        map.put("phone",userPhone);
        mobSendEvent(EventCode.DATA_REALNAME_AUTH_START, gameInfo.pkgName, map);

        UserAuthManager.getInstance().startAuthLoginGame(context, gameInfo.pkgName
                , userName, userIdCard, userPhone, new OnAuthCallback() {
                    @Override
                    public void onCerSuccess(String gid, String token) {

                        callback.onCerSuccess(gid, token);

                        Map<String,Object> eventMap = new HashMap<>();
                        eventMap.put("phone",userPhone);
                        eventMap.put("guid",gid);
                        eventMap.put("platform","realname_auth");
                        mobSendEvent(EventCode.DATA_REALNAME_AUTH_SUCCESS, gameInfo.pkgName, eventMap);

                        playGame(context, gameInfo);
                    }

                    @Override
                    public void onCerError(int erroCode , String errorStr) {
                        callback.onCerError(erroCode, errorStr);

                        Map<String,Object> eventMap = new HashMap<>();
                        eventMap.put("phone",userPhone);
                        eventMap.put("errorcode",erroCode);
                        mobSendEvent(EventCode.DATA_REALNAME_AUTH_FAILED, gameInfo.pkgName, eventMap);
                    }
                });
    }

    /**
     * 统计打点
     */
    private void mobSendEvent(String event, String gamePkgName, Map<String,Object> map){

        try {
            MobclickAgent.sendEvent(Event.getEvent(event, gamePkgName, map));
        } catch (Exception e){
            Logger.error("GameBox", e.getMessage());
        }
    }
}
