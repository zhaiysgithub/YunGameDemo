package kptech.game.kit.utils;

import android.util.Log;

public class Logger {
    //设为false关闭日志
    private static boolean LOG_ENABLE = false;

    public static void setLogEnable(boolean b){
        LOG_ENABLE = b;
    }

    public static void i(String tag, String msg){
        if (LOG_ENABLE){
            Log.i(tag, msg);
        }
    }
    public static void v(String tag, String msg){
        if (LOG_ENABLE){
            Log.v(tag, msg);
        }
    }
    public static void d(String tag, String msg){
        if (LOG_ENABLE){
            Log.d(tag, msg);
        }
    }
    public static void w(String tag, String msg){
        if (LOG_ENABLE){
            Log.w(tag, msg);
        }
    }
    public static void e(String tag, String msg){
        if (LOG_ENABLE){
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable t){
        if (LOG_ENABLE){
            Log.e(tag,msg,t);
        }
    }
}
