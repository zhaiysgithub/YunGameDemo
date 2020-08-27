package kptech.game.kit.ad;

import android.app.Activity;
import android.app.Application;
import android.view.View;

import com.zad.sdk.Oapi.ZadSdkApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import kptech.game.kit.GameInfo;
import kptech.game.kit.analytic.Event;
import kptech.game.kit.analytic.EventCode;
import kptech.game.kit.analytic.MobclickAgent;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.data.IRequestCallback;
import kptech.game.kit.data.RequestGameInfoTask;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;
import kptech.game.kit.ad.view.AdRemindDialog;

public class AdManager implements IAdCallback {
    private static final Logger logger = new Logger("AdManager") ;

    //取消显示
    public static final int CB_AD_CANCELED = 2;

    //广告显示完成
    public static final int CB_AD_PASSED = 3;
    //广告关闭
    public static final int CB_AD_DISABLED = 4;
    //广告显示失败
    public static final int CB_AD_FAILED = 5;

    //显示弹窗
    public static final int CB_AD_LOADING = 6;

    public static String rewardCode = null;
    public static String extCode = null;
    public static boolean init(Application application){
        boolean ret = false;

        //判断广告是否开启
        String adEnable = ProferencesUtils.getString(application, SharedKeys.KEY_GAME_APP_ADENABLE,null);

        HashMap ext = new HashMap();
        ext.put("adEnable", adEnable);

        if (adEnable!=null && "1".equals(adEnable)){
            String adJson = ProferencesUtils.getString(application, SharedKeys.KEY_GAME_APP_ADJSON,null);
            if (adJson!=null){
                try {
                    JSONObject adObj = new JSONObject(adJson);
                    String appKey = adObj.getString("appKey");
                    String appToken = adObj.getString("appToken");
                    ZadSdkApi.init(application, appKey, appToken);
                    ret = true;

                    JSONArray gameStartArr = adObj.getJSONArray("gameStart");
                    try {
                        for (int i = 0; i < gameStartArr.length(); i++) {
                            JSONObject gameObj = gameStartArr.getJSONObject(i);
                            String type = gameObj.getString("adType");
                            String code = gameObj.getString("adCode");
                            if ("reward".equals(type)) {
                                rewardCode = code;
                            } else if ("interstitial".equals(type)) {
                                extCode = code;
                            }
                        }
                    }catch (Exception e){
                    }

                    ext.put("appKey", adEnable);
                    ext.put("appToken", adEnable);
                    if (gameStartArr!=null){
                        ext.put("gameStartArr", gameStartArr.toString());
                    }

                }catch (Exception e){
                    logger.error(e.getMessage());
                }
            }
        }

        try {
            //发送打点事件
            Event event = Event.getEvent(ret ? EventCode.DATA_AD_INIT_OK : EventCode.DATA_AD_INIT_FAILED);
            event.setExt(ext);
            MobclickAgent.sendEvent(event);
        }catch (Exception e){}

        return ret;
    }

    private IAdCallback mAdCallback;
    public void setAdCallback(IAdCallback adCallback) {
        this.mAdCallback = adCallback;
    }

    private AdLoader mAdLoader;
    public void setAdLoader(AdLoader adLoader) {
        this.mAdLoader = adLoader;
    }

    private Activity mActivity;
    public AdManager(Activity activity){
        this.mActivity = activity;
    }

    public void loadGameAd(String corpId, final GameInfo gameInfo){
        try {
            //手动设置为不显示广告
            if (gameInfo.showAd == GameInfo.GAME_AD_SHOW_OFF){
                if (mAdCallback!=null){
                    mAdCallback.onAdCallback("", CB_AD_DISABLED);
                }
                return;
            }

            //没有广告信息时不显示广告
            if (mAdLoader == null){
                if (mAdCallback!=null){
                    mAdCallback.onAdCallback("", CB_AD_FAILED);
                }
                return;
            }

            //判断是否已经看过广告了
            int adVerify = ProferencesUtils.getIng(mActivity, SharedKeys.KEY_AD_REWARD_VERIFY_FLAG, 0);
            if (adVerify > 0) {
                if (mAdCallback!=null){
                    mAdCallback.onAdCallback("", CB_AD_PASSED);
                }
                return;
            }

            //加载广告
            if (mAdCallback!=null){
                mAdCallback.onAdCallback("", CB_AD_LOADING);
            }

            //直接显示广告，不用请求服务器
            if (gameInfo.showAd == GameInfo.GAME_AD_SHOW_ON){
                //显示广告弹窗
                showAdRemindDialog(gameInfo.pkgName);
                return;
            }

            //请求网络获取广告显示
            new RequestGameInfoTask(mActivity).setRequestCallback(new IRequestCallback<GameInfo>() {
                @Override
                public void onResult(GameInfo game, int code) {
                    if (game!=null && game.showAd == 1){
                        //显示广告弹窗
                        showAdRemindDialog(gameInfo.pkgName);
                    }else {
                        //广告关闭
                        if (mAdCallback!=null){
                            mAdCallback.onAdCallback("", CB_AD_DISABLED);
                        }
                    }
                }
            }).execute(corpId, gameInfo.pkgName);

            return;
        }catch (Exception e){
            logger.error("showAd error:" + e.getMessage());
        }

        if (mAdCallback!=null){
            mAdCallback.onAdCallback("", CB_AD_FAILED);
        }
    }


    //显示广告弹窗
    private void showAdRemindDialog(final String pkgName) {
        try {
            //检测是否已经加载到广告
//            if(mAdLoader.getRewardState() == AdLoader.AdLoadState.RewardFailed){
//                //如果加载失败，则不弹出窗口
//
//            }

            new AdRemindDialog(mActivity)
                    .setOnCancelListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //显示广告按钮
                            if (mAdLoader!=null){
                                mAdLoader.setAdCallback(AdManager.this);
                                mAdLoader.setPackageName(pkgName);
                                mAdLoader.showAd();
                            }
                        }
                    })
                    .setOnSubmitListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //取消按钮
                            if (mAdCallback!=null){
                                mAdCallback.onAdCallback(null, CB_AD_CANCELED);
                            }
                        }
                    })
                    .show();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void onAdCallback(Object msg, int code) {
        switch (code){
            case AdLoader.ADSTATE_CLOSE:
                //用户取消，不进入游戏
                if (mAdCallback!=null){
                    mAdCallback.onAdCallback("", CB_AD_CANCELED);
                }
                break;
            case AdLoader.ADSTATE_FAILED:
                if (mAdCallback!=null){
                    mAdCallback.onAdCallback("", CB_AD_FAILED);
                }
                break;
            case AdLoader.ADSTATE_VERIFY:
                if (mAdCallback!=null){
                    mAdCallback.onAdCallback("", CB_AD_PASSED);
                }
                break;
            default:
                if (mAdCallback!=null){
                    mAdCallback.onAdCallback("", CB_AD_PASSED);
                }
                break;
        }
    }

    public void closeAd() {
        //关闭广告

    }

    public void destory() {
        try {
            if (mAdLoader!=null){
                mAdLoader.destory();
            }
            mAdCallback = null;
            mAdLoader = null;
            mActivity = null;
        }catch (Exception e){
            logger.error("destory error:"+e.getMessage());
        }
    }


}
