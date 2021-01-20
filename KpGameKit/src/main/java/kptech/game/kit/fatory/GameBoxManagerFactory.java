package kptech.game.kit.fatory;

import java.lang.reflect.Constructor;

import kptach.game.kit.inter.game.IGameBoxManager;

public class GameBoxManagerFactory {
    private static IGameBoxManager bdManager = null;
    private static IGameBoxManager rfManager = null;

    private static byte[] lock = new byte[0];

    public static IGameBoxManager getGameBoxManager(int sdkType){
        synchronized (lock){
            IGameBoxManager instance = null;
            try {
                if (sdkType == 1 ){
                    if (bdManager == null) {
                        bdManager = (IGameBoxManager) newInstance("", null, null);
                    }
                    instance = bdManager;
                }else {
                    if (rfManager == null) {
                        rfManager = (IGameBoxManager) newInstance("kptach.game.kit.lib.redfinger.RedGameBoxManager", null, null);
                    }
                    instance = rfManager;
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
