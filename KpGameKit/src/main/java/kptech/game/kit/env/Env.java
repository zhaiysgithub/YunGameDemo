package kptech.game.kit.env;

import android.content.Context;

import kptech.game.kit.BuildConfig;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;
import kptech.lib.constants.SharedKeys;

public class Env {
    private static final String TAG = Env.class.getSimpleName();

    private static final boolean mDebug = BuildConfig.DEBUG;

    private static int mEnvType;

    public static boolean isTestEnv(){
        //TODO PASS3.0未部署到线上环境，暂时使用测试环境测试
        /*try {
            return mDebug || mEnvType == ENV_DEBUG;
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }
        return false;*/
        return true;
    }

    public static final int ENV_DEBUG = 2;
    public static final int ENV_RELEASE = 1;

    public static void setEnv(Context context, int env){
        try {
            if (env == ENV_DEBUG){
                ProferencesUtils.setInt(context, SharedKeys.KEY_ENV_KEY, ENV_DEBUG);
            }else if (env == ENV_RELEASE){
                ProferencesUtils.setInt(context, SharedKeys.KEY_ENV_KEY, ENV_RELEASE);
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }
    }

    public static void init(Context context){
        try {
            mEnvType = ProferencesUtils.getIng(context, SharedKeys.KEY_ENV_KEY, ENV_RELEASE);
            return;
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }
        mEnvType = ENV_RELEASE;
    }
}
