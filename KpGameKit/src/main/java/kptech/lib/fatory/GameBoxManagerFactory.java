package kptech.lib.fatory;

import android.app.Application;

import com.kptach.lib.inter.game.IGameBoxManager;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import kptech.game.kit.GameInfo;
import kptech.game.kit.utils.Logger;


public class GameBoxManagerFactory {
    private static final String TAG = "GameBoxManagerFactory";
    private static IGameBoxManager bd2Manager = null;
    private static IGameBoxManager rfManager = null;
    private static IGameBoxManager passv3Manager = null;

    private static byte[] lock = new byte[0];

    public static IGameBoxManager getGameBoxManager(GameInfo.SdkType sdkType, Application app, HashMap<String,Object> params){
        synchronized (lock){
            IGameBoxManager instance = null;
            try {
                if (isSupportPassV3()){
                    if (passv3Manager == null) {
                        passv3Manager = (IGameBoxManager) newInstance("kptech.game.kit.pass.PassV3", null, null);
                    }
                    instance = passv3Manager;
                }else {
                    if (sdkType == GameInfo.SdkType.BD || sdkType == GameInfo.SdkType.DEFAULT){
                        if (bd2Manager == null){
                            bd2Manager = (IGameBoxManager) newInstance("com.kptach.lib.game.baidu.BdGameBoxManager", null, null);
                        }
                        instance = bd2Manager;
                    }/*else if (sdkType == GameInfo.SdkType.REDF){
                        if (rfManager == null) {
                            rfManager = (IGameBoxManager) newInstance("com.kptach.lib.game.redfinger.RedGameBoxManager", null, null);
                        }
                        instance = rfManager;
                    }*/
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            if (instance != null){
                instance.initLib(app, params, null);
            }
            return instance;
        }
    }

    private static Object newInstance(String className, Class[] argsClass, Object[] args) throws Exception {
        Class newoneClass = Class.forName(className);
        Constructor cons = newoneClass.getConstructor(argsClass);
        return cons.newInstance(args);
    }

    public static boolean isSupportPassV3(){
        try {
            Class.forName("kptech.game.kit.pass.PassV3");
            return true;
        }catch (Exception e){
            Logger.info(TAG, "isSupportPassV3 " + false);
        }
        return false;
    }

}
