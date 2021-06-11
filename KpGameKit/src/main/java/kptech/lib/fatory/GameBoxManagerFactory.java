package kptech.lib.fatory;

import android.app.Application;

import com.kptach.lib.game.baidu.BdGameBoxManager;
import com.kptach.lib.game.bdsdk.BDSdkGameBoxManager;
import com.kptach.lib.game.huawei.HWGameBoxManager;
import com.kptach.lib.game.redfinger.RedGameBoxManager;
import com.kptach.lib.inter.game.IGameBoxManager;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import kptech.game.kit.GameInfo;

public class GameBoxManagerFactory {
    private static IGameBoxManager bdManager = null;
    private static IGameBoxManager rfManager = null;
    private static IGameBoxManager hwManager = null;

    private static byte[] lock = new byte[0];

    public static IGameBoxManager getGameBoxManager(GameInfo.SdkType sdkType, Application app, HashMap<String,Object> params){
        synchronized (lock){
            IGameBoxManager instance = null;
            try {
                if (sdkType == GameInfo.SdkType.BD || sdkType == GameInfo.SdkType.DEFAULT){
                    if (bdManager == null) {
                        bdManager = (IGameBoxManager) newInstance(BDSdkGameBoxManager.class.getName(), null, null);
                    }
                    instance = bdManager;
                }else if (sdkType == GameInfo.SdkType.REDF){
                    if (rfManager == null) {
                        rfManager = (IGameBoxManager) newInstance(RedGameBoxManager.class.getName(), null, null);
                    }
                    instance = rfManager;
                } else if(sdkType == GameInfo.SdkType.HW) {
                    if (hwManager == null){
                        hwManager = (IGameBoxManager) newInstance(HWGameBoxManager.class.getName(), null,null);
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
