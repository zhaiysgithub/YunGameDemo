package kptech.lib.fatory;

import android.app.Application;

//import com.kptach.lib.game.baidu.BdGameBoxManager;
//import com.kptach.lib.game.bdsdk.BDSdkGameBoxManager;
//import com.kptach.lib.game.huawei.HWGameBoxManager;
//import com.kptach.lib.game.redfinger.RedGameBoxManager;
import com.kptach.lib.inter.game.IGameBoxManager;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import kptech.game.kit.BuildConfig;
import kptech.game.kit.GameInfo;

public class GameBoxManagerFactory {
    private static IGameBoxManager bd2Manager = null;
    private static IGameBoxManager bd3Manager = null;
    private static IGameBoxManager rfManager = null;
    private static IGameBoxManager hwManager = null;

    private static byte[] lock = new byte[0];

    public static IGameBoxManager getGameBoxManager(GameInfo.SdkType sdkType, Application app, HashMap<String,Object> params){
        synchronized (lock){
            IGameBoxManager instance = null;
            try {
                if (sdkType == GameInfo.SdkType.BD || sdkType == GameInfo.SdkType.DEFAULT){
                    if (BuildConfig.useSDK2){
                        if (bd2Manager == null){
                            bd2Manager = (IGameBoxManager) newInstance("com.kptach.lib.game.baidu.BdGameBoxManager", null, null);
                        }
                        instance = bd2Manager;
                    }else {
                        //3.0 SDK
                        if (bd3Manager == null){
                            bd3Manager = (IGameBoxManager) newInstance("com.kptach.lib.game.bdsdk.BDSdkGameBoxManager", null, null);
                        }
                        instance = bd3Manager;
                    }
                }else if (sdkType == GameInfo.SdkType.REDF){
                    if (rfManager == null) {
                        rfManager = (IGameBoxManager) newInstance("com.kptach.lib.game.redfinger.RedGameBoxManager", null, null);
                    }
                    instance = rfManager;
                } else if(sdkType == GameInfo.SdkType.HW) {
                    if (hwManager == null){
                        hwManager = (IGameBoxManager) newInstance("com.kptach.lib.game.huawei.HWGameBoxManager", null,null);
                    }
                    instance = hwManager;
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

}
