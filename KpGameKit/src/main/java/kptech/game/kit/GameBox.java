package kptech.game.kit;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import kptech.game.kit.activity.GamePlay;
import kptech.game.kit.utils.Logger;

public class GameBox {
    private static final Logger logger = new Logger("GameBox") ;

    private static volatile GameBox box = null;

    public Application mApplication = null;
    public String appKey = null;

    private GameDownloader mDownloader = null;

    public static void init(@NonNull Application application, String appKey) {
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
            logger.error("playGame error, activity:" + activity + ", gameInfo:" + gameInfo );
            return;
        }

        try {
            //判断本地是否已经安装
            PackageManager packageManager = activity.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(gameInfo.pkgName);
            if(intent!=null){
                logger.info("本地已安装游戏："+gameInfo.pkgName);
                activity.startActivity(intent);
                return;
            }
        }catch (Exception e){
            logger.error("获取本地游戏，error:" + e.getMessage());
        }

        logger.info("启动云游戏，gameInfo:" + gameInfo.toString());

        //启动云游戏
        Intent intent = new Intent(activity, GamePlay.class);
        intent.putExtra(GamePlay.EXTRA_CORPID, this.appKey);
        intent.putExtra(GamePlay.EXTRA_GAME, gameInfo);
        intent.putExtra(GamePlay.EXTRA_PARAMS, params);

        activity.startActivity(intent);
    }

//    public void playGame(Activity activity, int gid, String pkgName){
//        this.playGame(activity,gid,pkgName,null, 0);
//    }
//
//    public void playGame(Activity activity, int gid, String pkgName, String downUrl){
//        this.playGame(activity,gid,pkgName,downUrl, 0);
//    }
//    public void playGame(Activity activity, int gid, String pkgName, String downUrl, int showAd){
//        GameInfo info = new GameInfo();
//        info.gid = gid;
//        info.pkgName = pkgName;
//        info.downloadUrl = downUrl;
//        info.showAd = showAd;
//        this.playGame(activity,info);
//    }


    public void setGameDownloader(GameDownloader downloader){
        logger.info("setGameDownloader :" + downloader);
        this.mDownloader = downloader;
    }

    public GameDownloader getGameDownloader(){
        logger.info("getGameDownloader :" + this.mDownloader);
        return this.mDownloader;
    }

}
