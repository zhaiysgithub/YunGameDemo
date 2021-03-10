package kptech.lib.conf;

import android.content.Context;

import org.json.JSONObject;

import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;
import kptech.lib.constants.SharedKeys;

public class RecordScreenConfig {
    private static final String TAG = RecordScreenConfig.class.getSimpleName();

    private static final int CONF_RECORD_SCREEN_MAX_TIME_LEN = 300;
    private static final int CONF_RECORD_SCREEN_MIN_TIME_LEN = 5;

    public boolean disable;
    public int maxTimeLen;
    public int minTimeLen;

    public static RecordScreenConfig getConfig(Context context){
        RecordScreenConfig config = getDefaultConfig();
        try {
            if (context != null){
                String str = ProferencesUtils.getString(context, SharedKeys.KEY_GAME_RECORD_SCREEN_CONF, null);
                if (str != null){
                    try {
                        JSONObject json = new JSONObject(str);
                        int max = json.optInt("maxTimeLen");
                        int min = json.optInt("minTimeLen");
                        int disable = json.optInt("disable");
                        if (disable == 1){
                            config.disable = true;
                        }else {
                            config.disable = false;
                        }
                        if (max > 0){
                            config.maxTimeLen = max;
                        }
                        if (min > 0){
                            config.minTimeLen = min;
                        }
                    }catch (Exception e){
                        Logger.error(TAG, e.getMessage());
                    }
                }
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }

        return config;
    }

    private static RecordScreenConfig getDefaultConfig(){
        RecordScreenConfig config = new RecordScreenConfig();
        config.disable = false;
        config.maxTimeLen = CONF_RECORD_SCREEN_MAX_TIME_LEN;
        config.minTimeLen = CONF_RECORD_SCREEN_MIN_TIME_LEN;
        return config;
    }

}
