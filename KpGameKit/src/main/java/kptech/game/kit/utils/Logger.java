package kptech.game.kit.utils;

import android.content.Context;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Logger {

    public static int LEVEL_ERROR = 1;
    public static int LEVEL_INFO = 3;
    public static int LEVEL_DEBUG = 7;

    private static int level = LEVEL_ERROR;

    public static void setLevel(int l){
        level = l;
    }

    private static final String TAG = "kpgamekit.";

    public static void info(String tag, String msg){
        if (( level & LEVEL_INFO ) > 0 ){
            Log.i(TAG + tag, msg);
        }
    }

    public static void error(String tag, String err){
        if (( level & LEVEL_ERROR ) > 0 ){
            Log.e(TAG + tag, err);
        }

    }

    public static void error(String tag, Throwable ex){
        if (( level & LEVEL_ERROR ) > 0 && ex != null){
            ex.printStackTrace();
        }
    }

    public static void error(String tag, String err, Throwable ex){
        if (err != null){
            Log.e(TAG + tag, err);
        }
        if (( level & LEVEL_ERROR ) > 0 && ex != null){
            ex.printStackTrace();
        }
    }

    public static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try{
            throwable.printStackTrace(pw);
            return sw.toString();
        } finally {
            pw.close();
        }
    }

//
////    private static boolean isDebug = false;
////    public static void setDebug(boolean debug){
////        isDebug = debug;
////    }
//
//    private static boolean enable = false;
//
//    public static void se(Context context,  boolean b){
//
//    }
//
//
//
//    private static String TAG = "kpgamekit.";
////    private boolean mEnableLog;
//
////    public Logger(String tag){
////        this.TAG += tag;
////        setEnableLog(isDebug);
////    }
////
////    public Logger(String tag, boolean enable){
////        this.TAG += tag;
////        this.mEnableLog = enable;
////    }
//
//    public static void info(String tag, String msg){
//        if (enable){
//            Log.i(TAG + tag, msg);
//        }
//    }
//
//    public static void error(String tag, String err){
//        if (err != null){
//            Log.e(TAG, err);
//        }
//    }

//    public void setEnableLog(boolean enable){
//        this.mEnableLog = enable;
//    }
}
