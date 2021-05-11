package kptech.game.kit.manager;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import kptech.game.kit.APIConstants;
import kptech.game.kit.GameBoxManager;
import kptech.game.kit.callback.OnAuthCallback;
import kptech.game.kit.utils.AppUtils;
import kptech.game.kit.utils.ProferencesUtils;
import kptech.lib.constants.SharedKeys;
import kptech.lib.data.AccountTask;

/**
 * 用户认证相关管理类
 */
public class UserCertificationManager {

    private static final String ERROR_SET_PKGNAME = "请输入正确的游戏包名";
    private static final String ERROR_SET_CROPKEY = "请先申请并设置appId";
    private static final String ERRORN_USERNAME_MSG = "请输入正确的用户名";
    private static final String ERRORN_USERID_MSG = "请输入正确的身份证信息";
    private static final String ERRORN_PHONENUM_MSG = "请输入正确的手机号";
    private static final String ERRORN_DEFAULT = "登录失败";
    private static final String ERRORN_NOT_AUTH = "该用户未认证";

    private Gson mGson;
    private String guid;

    private UserCertificationManager() {

    }

    private static class UserCertificationHolder {
        private static final UserCertificationManager INSTANCE = new UserCertificationManager();
    }

    public static UserCertificationManager getInstance() {
        return UserCertificationHolder.INSTANCE;
    }

    private Gson createGson() {
        if (mGson == null) {
            mGson = new GsonBuilder().enableComplexMapKeySerialization().create();
        }
        return mGson;
    }

    private String getSPCacheKey(String pkgName) {
        return SharedKeys.KEY_GAME_USER_LOGIN_DATA_PRE + pkgName;
    }

    /**
     * 是否需要认证
     *
     * @return true 需要认证
     * false 不需要认证
     */
    public boolean shouldLoginAuthByPhone(Context context, String pkgName, String phoneNum) {
        return shouldLoginAuth(context, pkgName, phoneNum, "");
    }

    /**
     * 通过 uninqueId 认证
     *
     */
    public boolean shouldLoginAuthById(Context context, String pkgName, String uninqueId) {
        return shouldLoginAuth(context, pkgName, "", uninqueId);
    }

    public boolean shouldLoginAuth(Context context, String pkgName, String phoneNum, String uninqueId) {
        Map<String, Object> cacheDataMap = getCacheData(context, pkgName);

        if (uninqueId.isEmpty()) {
            String spPhone = cacheDataMap.containsKey("phone") ? cacheDataMap.get("phone").toString() : "";
            if (spPhone == null || spPhone.isEmpty() || !spPhone.equals(phoneNum)) {
                return true;
            }
            guid = cacheDataMap.containsKey("guid") ? cacheDataMap.get("guid").toString() : "";
            if (guid == null || guid.isEmpty()) {
                return true;
            }
            String token = cacheDataMap.containsKey("token") ? cacheDataMap.get("token").toString() : "";
            return token == null || token.isEmpty();

        } else {

            String cacheUninqueId = cacheDataMap.containsKey("uninqueId") ? cacheDataMap.get("uninqueId").toString() : "";
            return !cacheUninqueId.equals(uninqueId);

        }
    }

    /**
     * 联运游戏
     * 开始执行游戏认证
     */
    public void startAuthLoginGame(final Context context, final String pkgName,
                                   String userName, String userIdCardNum, String userPhone,
                                   @NonNull final OnAuthCallback callback) {
        startAuthLoginGame(context, pkgName, userName, userIdCardNum, userPhone, "", callback);
    }

    /**
     * 开始执行游戏认证
     */
    public void startAuthLoginGame(final Context context, final String pkgName,
                                   String userName, String userIdCardNum, final String userPhone,
                                   final String uninqueId, @NonNull final OnAuthCallback callback) {
        if (FastRepeatClickManager.getInstance().isFastDoubleClick() || context == null) {
            return;
        }
        String cropKey = GameBoxManager.mCorpID;
        if (cropKey.isEmpty()) {
            callback.onCerError(APIConstants.ERROR_AUTH_PARAMS, ERROR_SET_CROPKEY);
        } else if (pkgName == null || pkgName.isEmpty()) {
            callback.onCerError(APIConstants.ERROR_AUTH_PARAMS, ERROR_SET_PKGNAME);
        } else if (!AppUtils.phoneNumSimpleCheck(userPhone)) {
            callback.onCerError(APIConstants.ERROR_AUTH_PARAMS, ERRORN_PHONENUM_MSG);
        } else {
            try {
                // AccountTask 执行请求回调
                new AccountTask(context, AccountTask.ACTION_AUTH_THIRD_USER)
                        .setCorpKey(cropKey)
                        .setPkgName(pkgName)
                        .setCallback(new AccountTask.ICallback() {
                            @Override
                            public void onResult(Map<String, Object> map) {
                                if (map == null || map.size() == 0) {
                                    callback.onCerError(APIConstants.ERROR_AUTH_FAIL, ERRORN_DEFAULT);
                                } else if (map.containsKey("error")) {
                                    String errorMsg = map.get("error").toString();
                                    if (errorMsg != null && !errorMsg.isEmpty() && !errorMsg.equals("null")) {
                                        callback.onCerError(APIConstants.ERROR_AUTH_FAIL, errorMsg);
                                    } else {
                                        callback.onCerError(APIConstants.ERROR_AUTH_FAIL, ERRORN_DEFAULT);
                                    }
                                } else {
                                    if (map.containsKey("access_token")) {
                                        Object at = map.get("access_token");
                                        map.put("token", at);
                                        map.remove("access_token");
                                    }
                                    String guidValue = "";
                                    if (map.containsKey("guid") && map.containsKey("token")){
                                        guidValue = (String) map.get("guid");
                                        String tokenValue = (String) map.get("token");
                                        if (guidValue == null || guidValue.isEmpty() || tokenValue == null || tokenValue.isEmpty()){
                                            callback.onCerError(APIConstants.PHONE_NOT_AUTH,ERRORN_NOT_AUTH);
                                            return;
                                        }
                                    }else{
                                        callback.onCerError(APIConstants.PHONE_NOT_AUTH,ERRORN_NOT_AUTH);
                                        return;
                                    }
                                    if (uninqueId != null && uninqueId.length() > 0) {
                                        map.put("uninqueId", uninqueId);
                                    }
                                    if (userPhone.length() > 0) {
                                        map.put("phone", userPhone);
                                    }
                                    Gson gson = createGson();
                                    String jsonStr = gson.toJson(map);
                                    ProferencesUtils.setString(context, getSPCacheKey(pkgName), jsonStr);
                                    callback.onCerSuccess(guidValue);
                                }

                            }
                        }).execute(userIdCardNum, userName, userPhone, pkgName);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onCerError(APIConstants.ERROR_AUTH_FAIL, e.getMessage());
            }
        }
    }


    private Map<String, Object> getCacheData(Context context, String pkgName) {

        Map<String, Object> map = new HashMap<>();
        String cacheKey = getSPCacheKey(pkgName);
        try {
            String jsonStr = ProferencesUtils.getString(context, cacheKey, null);
            if (jsonStr != null && !jsonStr.isEmpty()) {
                Gson gson = createGson();

                Type mapType = new TypeToken<Map<String, Object>>() {
                }.getType();

                map = gson.fromJson(jsonStr, mapType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    public String getGuid() {
        return guid;
    }
}
