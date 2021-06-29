package kptech.game.kit.pass.manager;

import org.json.JSONObject;

public class JsonUtils {

    public static String optString(JSONObject json, String key) {
        if (json.isNull(key)) {
            return null;
        } else {
            return json.optString(key);
        }
    }

    public static int optInt(JSONObject json, String key){
        if (json.isNull(key)){
            return -1;
        }else{
            return json.optInt(key,-1);
        }
    }

    public static long optLong(JSONObject json, String key){
        if (json.isNull(key)){
            return 0;
        }else{
            return json.optLong(key);
        }
    }

    public static boolean optBoolean(JSONObject json, String key){
        if (json.isNull(key)){
            return false;
        }else{
            return json.optBoolean(key,false);
        }
    }

}
