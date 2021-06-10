package com.kptach.lib.game.bdsdk.utils;

import android.util.Log;

public class Logger {

    private static boolean isDebug = false;
    public static void setDebug(boolean debug){
        isDebug = debug;
    }

    private static String TAG = "kpgamekit.bdsdk.";

    public static void info(String tag, String msg){
        if (isDebug){
            Log.i(TAG + tag, msg);
        }
    }

    public static void error(String tag, String err){
        if (err != null){
            Log.e(TAG, err);
        }
    }

}
