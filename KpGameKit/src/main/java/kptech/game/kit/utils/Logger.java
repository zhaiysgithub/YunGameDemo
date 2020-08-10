package kptech.game.kit.utils;

import android.util.Log;

public class Logger {
    private String TAG = "kpgamekit.";
    private static boolean isDebug = false;
    public Logger(String tag){
        this.TAG += tag;
    }

    public static void setDebug(boolean debug){
        isDebug = debug;
    }

    public void info(String msg){
        if (isDebug){
            Log.i(TAG, msg);
        }
    }

    public void error(String err){
        Log.e(TAG, err);
    }
}
