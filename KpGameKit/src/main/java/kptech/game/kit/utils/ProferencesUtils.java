package kptech.game.kit.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ProferencesUtils {
    private static SharedPreferences instance(Context context){
        return context.getSharedPreferences("kpgameboxkit", Context.MODE_PRIVATE);
    }

    public static void setString(Context context, String key, String value){
        instance(context).edit().putString(key,value).commit();
    }
    public static String getString(Context context, String key, String def){
        return instance(context).getString(key, def);
    }
    public static void setLong(Context context, String key, long value){
        instance(context).edit().putLong(key,value).commit();
    }
    public static long getLong(Context context, String key, long num){
        return instance(context).getLong(key,num);
    }
    public static void setInt(Context context, String key, int value){
        instance(context).edit().putInt(key,value).commit();
    }
    public static int getIng(Context context, String key, int num){
        return instance(context).getInt(key,num);
    }
    public static void setBoolean(Context context, String key, boolean value){
        instance(context).edit().putBoolean(key, value).apply();
    }
    public static boolean getBoolean(Context context, String key, boolean defaultValue){
        return instance(context).getBoolean(key,defaultValue);
    }

    public static void remove(Context context, String key){
        instance(context).edit().remove(key).commit();
    }
}
