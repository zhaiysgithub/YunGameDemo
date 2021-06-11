package com.kptach.lib.game.huawei;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;



public class HWCloudGameUtils {

    private static final String TAG = "kpgamekit.hw.";

    private static boolean isDebug = false;
    public static void setDebug(boolean debug){
        isDebug = debug;
    }

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
