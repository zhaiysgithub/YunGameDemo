package kptech.game.kit.pass.manager;

import android.app.Application;

import com.kptach.lib.inter.game.IGameBoxManager;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public class GameBoxManagerFactory {
    private static IGameBoxManager bd2Manager = null;
    private static IGameBoxManager bd3Manager = null;
    private static IGameBoxManager rfManager = null;
    private static IGameBoxManager hwManager = null;

    private static byte[] lock = new byte[0];

    public static IGameBoxManager getGameBoxManager(String sdkType, Application app, HashMap<String,Object> params){
        synchronized (lock){
            IGameBoxManager instance = null;
            try {
                if ("BD".equals(sdkType)){
                    if (bd3Manager == null){
                        bd3Manager = (IGameBoxManager) newInstance("com.kptach.lib.game.bdsdk.BDSdkGameBoxManager", null, null);
                    }
                    instance = bd3Manager;
                } else if("HW".equals(sdkType)) {
                    if (hwManager == null){
                        hwManager = (IGameBoxManager) newInstance("com.kptach.lib.game.huawei.HWGameBoxManager", null,null);
                    }
                    instance = hwManager;
                }
            }catch (Exception e){
                e.printStackTrace();
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
