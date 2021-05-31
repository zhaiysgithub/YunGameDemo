package kptech.game.kit.utils;

import kptech.game.kit.BuildConfig;

/**
 * 版本渠道控制
 */
public class VersionUtils {

    private static final String CHANNEL_MARK_DEFAULT = "1";
    private static final String CHANNEL_MARK_WANGYI = "2";
    private static final String CHANNEL_MARK_XIAOYU = "3";

    public static boolean isXiaoYuChannel(){

        boolean ret = false;
        try{
            String versionName = BuildConfig.VERSION_NAME;
            String[] splitArray = versionName.split("\\.");
            if (splitArray.length == 4){
                String channelMark = splitArray[2];
                ret = CHANNEL_MARK_XIAOYU.equals(channelMark);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

}
