package kptech.game.kit.utils;

import android.util.Log;

public class Logger {

    private static boolean isDebug = false;
    public static void setDebug(boolean debug){
        isDebug = debug;
    }

    private static String TAG = "kpgamekit.";
//    private boolean mEnableLog;

//    public Logger(String tag){
//        this.TAG += tag;
//        setEnableLog(isDebug);
//    }
//
//    public Logger(String tag, boolean enable){
//        this.TAG += tag;
//        this.mEnableLog = enable;
//    }

    public static void info(String tag, String msg){
        if (isDebug){
            Log.i(TAG + tag, msg);
        }
    }

    public static void error(String tag, String err){
        Log.e(TAG, err);
    }

//    public void setEnableLog(boolean enable){
//        this.mEnableLog = enable;
//    }
}
