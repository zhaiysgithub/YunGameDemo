package kptech.game.kit.ad;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.zad.sdk.Oapi.ZadSdkApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kptech.game.kit.GameInfo;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.data.IRequestCallback;
import kptech.game.kit.data.RequestGameInfoTask;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;
import kptech.game.kit.ad.view.AdRemindDialog;

public class AdManager {
    private static final Logger logger = new Logger("AdManager") ;

    private static volatile AdManager mAdManager = null;
    public static AdManager getInstance() {
        if (mAdManager == null) {
            synchronized(AdManager.class) {
                if (mAdManager == null) {
                    mAdManager = new AdManager();
                }
            }
        }
        return mAdManager;
    }

    private AdManager(){
    }

    public boolean init(Application application){
        //判断广告是否开启
        String adEnable = ProferencesUtils.getString(application, SharedKeys.KEY_GAME_APP_ADENABLE,null);
        if (adEnable!=null && "1".equals(adEnable)){
            String adJson = ProferencesUtils.getString(application, SharedKeys.KEY_GAME_APP_ADJSON,null);
            if (adJson!=null){
                try {
                    JSONObject adObj = new JSONObject(adJson);
                    String appKey = adObj.getString("appKey");
                    String appToken = adObj.getString("appToken");
                    ZadSdkApi.init(application, appKey, appToken);

//                JSONArray gameStartArr = adObj.getJSONArray("gameStart");
                    return true;
                }catch (Exception e){
                    logger.error(e.getMessage());
                }
            }
        }
        return false;
    }


    public List<AdInfo> getGameStartAds(Context context){
        List<AdInfo> list = null;
        //判断广告是否开启
        String adEnable = ProferencesUtils.getString(context, SharedKeys.KEY_GAME_APP_ADENABLE,null);
        if (adEnable!=null && "1".equals(adEnable)){
            String adJson = ProferencesUtils.getString(context, SharedKeys.KEY_GAME_APP_ADJSON,null);
            if (adJson!=null){
                list = new ArrayList<>();
                try {
                    JSONObject adObj = new JSONObject(adJson);
                    JSONArray gameStartArr = adObj.getJSONArray("gameStart");
                    for (int i = 0; i < gameStartArr.length(); i++) {
                        AdInfo ad = new AdInfo();
                        JSONObject gameObj = gameStartArr.getJSONObject(i);
                        ad.type = gameObj.getString("adType");
                        ad.code = gameObj.getString("adCode");
                        list.add(ad);
                    }
                }catch (Exception e){
                    logger.error(e.getMessage());
                }
            }
        }

        return list;
    }


    public boolean showGameStartAd(final Activity activity, String corpId, GameInfo gameInfo, final IAdCallback<String> callback){

        //手动设置为不显示广告
        if (gameInfo.showAd == GameInfo.GAME_AD_SHOW_OFF){
            return false;
        }

        //获取本地广告配置信息
        List<AdInfo> ads = getGameStartAds(activity);
        String rewardCode = null;
        String extCode = null;
        if (ads != null && ads.size() > 0) {
            for (int i = 0; i < ads.size(); i++) {
                AdInfo ad = ads.get(i);
                if ("reward".equals(ad.type)) {
                    rewardCode = ad.code;
                } else if ("interstitial".equals(ad.type)) {
                    extCode = ad.code;
                }
            }
        }

        //未获取到广告
        if (rewardCode==null && extCode==null){
            return false;
        }

        //直接显示广告，不用请求服务器
        if (gameInfo.showAd == GameInfo.GAME_AD_SHOW_ON){
            //显示广告弹窗
            showAdRemindDialog(activity, rewardCode, extCode, callback);
            return true;
        }

        //请求网络获取广告显示
        final String finalRewardCode = rewardCode;
        final String finalExtCode = extCode;
        new RequestGameInfoTask(activity).setRequestCallback(new IRequestCallback<GameInfo>() {
            @Override
            public void onResult(GameInfo game, int code) {
                if (game!=null && game.showAd == 1){
                    //显示广告弹窗
                    showAdRemindDialog(activity, finalRewardCode, finalExtCode, callback);
                }else {
                    if (callback!=null){
                        callback.onCallback("", 1);
                    }
                }
            }
        }).execute(corpId, gameInfo.pkgName);
        return true;
    }

    //显示广告弹窗
    private void showAdRemindDialog(Activity activity, String rewardCode, String extCode, final IAdCallback<String> callback){
        new AdRemindDialog(activity)
                .setRewardAdCode(rewardCode)
                .setExtAdCode(extCode)
                .setCallback(new IAdCallback<String>() {
                    @Override
                    public void onCallback(String msg, int code) {
                        if (callback!=null){
                            callback.onCallback(msg, code);
                        }
                    }
                })
                .show();
    }
}
