package kptech.game.kit;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import kptech.cloud.kit.msg.Messager;
import kptech.game.kit.msg.MsgManager;
import kptech.game.kit.utils.Logger;


public class GameBoxManager {
    private static final String TAG = "GameManager";

    private Context context;
    private static volatile GameBoxManager box = null;
    private com.yd.yunapp.gameboxlib.GameBoxManager manager;
    private Messager mManager;
    private Application mApplication;
    private String mUniqueId;

    private GameBoxManager(Context context){
        manager = com.yd.yunapp.gameboxlib.GameBoxManager.getInstance(context);
        this.context = context;
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

    public static void setDebug(Context context){
        com.yd.yunapp.gameboxlib.GameBoxManager.getInstance(context).setDebug(true);
    }

    public static void init(Application application, String appKey, String appSecret, String appChannel){
        MsgManager.init(application);
        com.yd.yunapp.gameboxlib.GameBoxManager.getInstance(application).init(appKey,appSecret,appChannel);
    }

    public void setDebug(boolean debug){
        if (manager!=null){
            manager.setDebug(true);
        }
        Logger.setLogEnable(debug);
    }

    public void applyCloudDevice(@NonNull final GameInfo inf, @NonNull final APICallback<DeviceControl> callback){
        this.applyCloudDevice(inf, false, callback);
    }

    public void applyCloudDevice(@NonNull final GameInfo inf, final boolean playQueue, @NonNull final APICallback<DeviceControl> callback){


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

    public void applyCloudDeviceWithToken(String token, @NonNull final APICallback<DeviceControl> callback){
        if (token == null){
            if (callback!=null){
                callback.onAPICallback(null, APIConstants.ERROR_DEVICE_TOKEN_VALID_FAILED);
            }
            return;
        }

        manager.applyCloudDeviceWithToken(token, new com.yd.yunapp.gameboxlib.APICallback<com.yd.yunapp.gameboxlib.DeviceControl>() {
            @Override
            public void onAPICallback(com.yd.yunapp.gameboxlib.DeviceControl deviceControl, int i) {
                if (callback!=null){
                    DeviceControl control = null;
                    if (deviceControl!=null) {
                        control = new DeviceControl(deviceControl);
                    }
                    callback.onAPICallback(control, i);
                }
            }
        });
    }

    public List<GameInfo> queryGameList(int page, int limit) {
        List<GameInfo> list = null;
        List<com.yd.yunapp.gameboxlib.GameInfo> gameInfoArr = manager.queryGameList(page,limit);
        if (gameInfoArr!=null && gameInfoArr.size()>0){
            list = new ArrayList<>();
            for (int i = 0; i < gameInfoArr.size(); i++) {
                list.add(new GameInfo(gameInfoArr.get(i)));
            }
        }
        return list;
    }

    public GameInfo queryGame(int var1) {
        GameInfo inf = null;
        com.yd.yunapp.gameboxlib.GameInfo gameInfo = manager.queryGame(var1);
        if (gameInfo !=null){
            inf = new GameInfo(gameInfo);
        }
        return inf;
    }

    public List<GameInfo> queryGames(String var1) {
        List<GameInfo> list = null;
        List<com.yd.yunapp.gameboxlib.GameInfo> gameInfoArr = manager.queryGames(var1);
        if (gameInfoArr!=null && gameInfoArr.size()>0){
            list = new ArrayList<>();
            for (int i = 0; i < gameInfoArr.size(); i++) {
                list.add(new GameInfo(gameInfoArr.get(i)));
            }
        }
        return list;
    }

    public GameInfo updateGameInfo(@NonNull GameInfo game) {
        com.yd.yunapp.gameboxlib.GameInfo gameInfo = manager.updateGameInfo(game.getLibGameInfo());
        if (gameInfo!=null){
            return new GameInfo(gameInfo);
        }
        return null;
    }

    public boolean joinQueue(GameInfo info, int var2, final APICallback<QueueRankInfo> callback) {
        if (info == null){
            return false;
        }
        return manager.joinQueue(info.getLibGameInfo(),var2,new com.yd.yunapp.gameboxlib.APICallback<com.yd.yunapp.gameboxlib.QueueRankInfo>(){
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

    public void setSubChannel(String channel){
        manager.setSubChannel(channel);
    }

    public void setUserLevel(UserLevel level) {
        manager.setMemberLevel(UserLevel.getMemberLevel(level));
    }

    public UserLevel getUserLevel() {
        return UserLevel.getUserLevel(manager.getMemberLevel());
    }

    public void accelerateQueue(int accCount) {
        manager.accelerateQueue(accCount);
    }

    public void exitQueue() {
        manager.exitQueue();
    }

    public void setUniqueId(String uid){
        this.mUniqueId = uid;
    }

    public String getUniqueId(){
        return this.mUniqueId;
    }
}
