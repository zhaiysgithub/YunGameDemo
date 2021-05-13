package kptech.lib.conf;

import android.content.Context;

import org.json.JSONObject;

import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;
import kptech.lib.constants.SharedKeys;

public class Config {
    private static final String TAG = Config.class.getSimpleName();

    public static void saveConfig(Context mContext, JSONObject dObj){
        if (dObj == null){
            return;
        }

        try {
            //获取支付配置信息
            String payJson = dObj.has("payConf") ? dObj.getString("payConf") : null;
            if (payJson != null){
                ProferencesUtils.setString(mContext, SharedKeys.KEY_PAY_CONF, payJson);
            } else{
                ProferencesUtils.remove(mContext, SharedKeys.KEY_PAY_CONF);
            }
        }catch (Exception e) {
            Logger.error(TAG, "支付配置信息: " + e.getMessage());
        }

        try {
            //挽留弹窗次数
            String exitAlertNum = dObj.has("detentionNum") ? dObj.getString("detentionNum") : null;
            if (exitAlertNum != null){
                try {
                    ProferencesUtils.setInt(mContext, SharedKeys.KEY_GAME_EXITALERTCOUNT_CONF, Integer.parseInt(exitAlertNum));
                }catch (Exception e){}
            }else {
                ProferencesUtils.remove(mContext, SharedKeys.KEY_GAME_EXITALERTCOUNT_CONF);
            }
        }catch (Exception e) {
            Logger.error(TAG, "挽留弹窗配置信息 Error: " + e.getMessage());
        }


        try {
            //websocket URL
            String wsurl = dObj.has("wsurl") ? dObj.getString("wsurl") : null;
            if (wsurl != null){
                ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_WSURL, wsurl);

            }
        }catch (Exception e){
            Logger.error(TAG, "WebSocket Error: " + e.getMessage());
        }

        try {
            //mockSleepTime
            String mockSleepTime = dObj.has("mockSleepTime") ? dObj.getString("mockSleepTime") : null;
            if (mockSleepTime != null){
                ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_MOCK_SLEEPTIME, mockSleepTime);
            }else {
                ProferencesUtils.remove(mContext, SharedKeys.KEY_GAME_MOCK_SLEEPTIME);
            }
        }catch (Exception e){
            Logger.error(TAG, "一键新机配置信息 Error: " + e.getMessage());
        }

        try {
            //录屏功能时长
            String recordScreen = dObj.has("recordScreen") ? dObj.getString("recordScreen") : "";
            ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_RECORD_SCREEN_CONF, recordScreen);
        }catch (Exception e){
            Logger.error(TAG, "录屏配置信息 Error: " + e.getMessage());
        }

        try {
            //默认菜单 “云手机控制-后退” 显示
            String menuItemCon = dObj.has("menuItemControlShow") ? dObj.getString("menuItemControlShow") : "true";
            ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_MENUITEM_CONTROL, menuItemCon);
        } catch (Exception e) {
            Logger.error(TAG, "云手机控制后退 Error: " + e.getMessage());
        }
    }

}
