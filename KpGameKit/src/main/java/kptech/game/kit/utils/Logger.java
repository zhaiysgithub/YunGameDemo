package kptech.game.kit.utils;

import android.util.Log;

public class Logger {

    private static boolean isDebug = false;
    public static void setDebug(boolean debug){
        isDebug = debug;
    }

    private String TAG = "kpgamekit.";
    private boolean mEnableLog;
    public Logger(String tag){
        this.TAG += tag;
        setEnableLog(isDebug);
    }

    public Logger(String tag, boolean enable){
        this.TAG += tag;
        this.mEnableLog = enable;
    }

    public void info(String msg){
        if (this.mEnableLog){
            Log.i(TAG, msg);
        }
    }

    public void error(String err){
        Log.e(TAG, err);
    }

    public void setEnableLog(boolean enable){
        this.mEnableLog = enable;
    }
}
