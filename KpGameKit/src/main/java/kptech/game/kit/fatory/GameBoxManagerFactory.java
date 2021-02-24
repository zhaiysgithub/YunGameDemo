package kptech.game.kit.fatory;

import android.app.Application;

import com.kptach.lib.game.baidu.BdGameBoxManager;
import com.kptach.lib.game.redfinger.RedGameBoxManager;
import com.kptach.lib.inter.game.IGameBoxManager;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public class GameBoxManagerFactory {
    private static IGameBoxManager bdManager = null;
    private static IGameBoxManager rfManager = null;

    private static byte[] lock = new byte[0];

    public static IGameBoxManager getGameBoxManager(int sdkType, Application app, HashMap params){
        synchronized (lock){
            IGameBoxManager instance = null;
            try {
                if (sdkType == 1 ){
                    if (bdManager == null) {
                        bdManager = (IGameBoxManager) newInstance(BdGameBoxManager.class.getName(), null, null);
                    }
                    instance = bdManager;
                }else {
                    if (rfManager == null) {
                        rfManager = (IGameBoxManager) newInstance(RedGameBoxManager.class.getName(), null, null);
                    }
                    instance = rfManager;
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
