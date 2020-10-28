package kptech.game.kit.ad;

import android.app.Application;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import kptech.game.kit.ad.inter.IAdLoader;

public class AdLoaderFactory {

    public static final String AD_TYPE_REWARD = "reward";
    public static final String AD_TYPE_FEED = "feed";

    public static IAdLoader createrAdLoader(String adtype, String adcode){
        IAdLoader obj = null;
        try {
            if (AdLoaderFactory.AD_TYPE_REWARD.equals(adtype)){
                obj = (IAdLoader) newInstance("com.kptach.game.ad.loader.RewardAdLoader", new Class[]{String.class}, new Object[]{adcode});
            }else if (AdLoaderFactory.AD_TYPE_FEED.equals(adtype)){
                obj = (IAdLoader) newInstance("com.kptach.game.ad.loader.FeedAdLoader", new Class[]{String.class}, new Object[]{adcode});
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return obj;
    }

    public static boolean init(Application application, String appkey ,String appToken){
        try {
            invokeStaticMethod("com.zad.sdk.Oapi.ZadSdkApi","init",
                    new Class[]{Application.class, String.class, String.class},
                    new Object[]{application, appkey, appToken});
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static Object invokeStaticMethod(String className, String methodName, Class[] argsClass,
                                     Object[] args) throws Exception {
        Class ownerClass = Class.forName(className);

        Method method = ownerClass.getMethod(methodName,argsClass);

        return method.invoke(null, args);
    }

    public static Object newInstance(String className, Class[] argsClass, Object[] args) throws Exception {
        Class newoneClass = Class.forName(className);

        Constructor cons = newoneClass.getConstructor(argsClass);

        return cons.newInstance(args);

    }
}
