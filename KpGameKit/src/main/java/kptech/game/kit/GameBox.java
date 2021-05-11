package kptech.game.kit;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;


import org.xutils.x;

import kptech.game.kit.activity.GamePlay;
import kptech.game.kit.callback.OnAuthCallback;
import kptech.game.kit.manager.UserCertificationManager;
import kptech.game.kit.utils.Logger;

public class GameBox {
//    private static final Logger logger = new Logger("GameBox") ;

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
    public void playGame(Activity activity, GameInfo gameInfo, Params params){
        if (activity==null || gameInfo==null){
            Logger.error("GameBox", "playGame error, activity:" + activity + ", gameInfo:" + gameInfo );
            return;
        }

//        try {
//            //判断本地是否已经安装
//            PackageManager packageManager = activity.getPackageManager();
//            Intent intent = packageManager.getLaunchIntentForPackage(gameInfo.pkgName);
//            if(intent!=null){
//                Logger.info("GameBox", "本地已安装游戏："+gameInfo.pkgName);
//                activity.startActivity(intent);
//                return;
//            }
//        }catch (Exception e){
//            Logger.error("GameBox", "获取本地游戏，error:" + e.getMessage());
//        }

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
    public void startLogin(final Activity context, final GameInfo gameInfo, String phoneNum, final OnAuthCallback callback){

        if (callback == null){
            return;
        }
        boolean shouldAuth = UserCertificationManager.getInstance().shouldLoginAuthByPhone(context, gameInfo.pkgName, phoneNum);
        if (shouldAuth){
            startCertification(context, "", "", phoneNum, gameInfo, new OnAuthCallback() {
                @Override
                public void onCerSuccess() {
                    callback.onCerSuccess();
                    playGame(context, gameInfo);
                }

                @Override
                public void onCerError(int code, String errorStr) {
                    callback.onCerError(code, errorStr);
                }
            });
        } else {
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
    public void startCertification(final Activity context, String userName, String userIdCard, String userPhone,
                                   final GameInfo gameInfo, final OnAuthCallback callback){
        if(callback == null){
            return;
        }
        UserCertificationManager.getInstance().startAuthLoginGame(context, gameInfo.pkgName
                , userName, userIdCard, userPhone, new OnAuthCallback() {
                    @Override
                    public void onCerSuccess() {

                        callback.onCerSuccess();
                        GameBox.getInstance().playGame(context, gameInfo);
                    }

                    @Override
                    public void onCerError(int erroCode , String errorStr) {
                        callback.onCerError(erroCode, errorStr);
                    }
                });
    }
}
