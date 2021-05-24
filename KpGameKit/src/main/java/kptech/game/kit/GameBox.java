package kptech.game.kit;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;


import kptech.game.kit.activity.GamePlay;
import kptech.game.kit.utils.Logger;

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

        String miniPkgVersion = "";
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
                miniPkgVersion = packageManager.getPackageInfo(weiBaoPkgName,0).versionName;
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
        if (!miniPkgVersion.isEmpty()){
            intent.putExtra(GamePlay.EXTRA_MINI_VERSION,miniPkgVersion);
        }

        activity.startActivity(intent);
    }

}
