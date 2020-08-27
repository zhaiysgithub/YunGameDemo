package kptech.game.kit;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import kptech.game.kit.activity.GamePlay;
import kptech.game.kit.analytic.Event;
import kptech.game.kit.analytic.EventCode;
import kptech.game.kit.analytic.MobclickAgent;
import kptech.game.kit.utils.Logger;

public class GameBox {
    private static final Logger logger = new Logger("GameBox") ;

    private static volatile GameBox box = null;

    public Application mApplication = null;
    public String appKey = null;

    private long[] noOpsTimeout = null;

    public static GameBox getInstance(@NonNull Application application, String appKey) {
        if (box == null) {
            synchronized(GameBoxManager.class) {
                if (box == null) {
                    box = new GameBox(application, appKey);
                }
            }
        }
        return box;
    }

    private GameBox(Application application, String appKey){
        this.mApplication = application;
        this.appKey = appKey;
    }

    public void playGame(Activity activity, GameInfo gameInfo){
        if (activity==null || gameInfo==null){
            logger.error("playGame error, activity:" + activity + ", gameInfo:" + gameInfo );
            return;
        }

        //

        //初始化事件基本信息
        Event.createBaseEvent(activity, appKey);

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
        if (noOpsTimeout != null){
            intent.putExtra(GamePlay.EXTRA_TIMEOUT, noOpsTimeout);
        }
        activity.startActivity(intent);

    }

    public void playGame(Activity activity, int gid, String pkgName){
        this.playGame(activity,gid,pkgName,null, 0);
    }

    public void playGame(Activity activity, int gid, String pkgName, String downUrl){
        this.playGame(activity,gid,pkgName,downUrl, 0);
    }

    public void playGame(Activity activity, int gid, String pkgName, String downUrl, int showAd){
        GameInfo info = new GameInfo();
        info.gid = gid;
        info.pkgName = pkgName;
        info.downloadUrl = downUrl;
        info.showAd = showAd;
        this.playGame(activity,info);
    }

    /**
     * 设置无操作超时,
     * @param font 前台超时，单位s
     * @param back 后台超时，单位s
     */
    public void setNoOpsTimeout(long font, long back){
        if (font > 60 && back > 60){
            noOpsTimeout = new long[]{font, back};
        }
    }

}
