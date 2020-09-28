package kptech.game.kit.ad;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;

import com.zad.sdk.Oapi.ZadSdkApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import kptech.game.kit.ad.loader.FeedAdLoader;
import kptech.game.kit.ad.loader.IAdLoader;
import kptech.game.kit.ad.loader.IAdLoaderCallback;
import kptech.game.kit.ad.loader.RewardAdLoader;
import kptech.game.kit.analytic.Event;
import kptech.game.kit.analytic.EventCode;
import kptech.game.kit.analytic.MobclickAgent;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;

public class AdManager {
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

    public static boolean adEnable = true;
    public static String rewardCode = null;
    public static String extCode = null;
    public static String feedCode = null;
    public static boolean init(Application application){
        boolean ret = false;

        //判断广告是否开启
        String adEnable = ProferencesUtils.getString(application, SharedKeys.KEY_GAME_APP_ADENABLE,null);

        HashMap ext = new HashMap();
        ext.put("adEnable", adEnable);

        String err = null;

        if (adEnable!=null && "1".equals(adEnable)){
            String adJson = ProferencesUtils.getString(application, SharedKeys.KEY_GAME_APP_ADJSON,null);
            if (adJson!=null){
                try {
                    JSONObject adObj = new JSONObject(adJson);
                    String appKey = adObj.getString("appKey");
                    String appToken = adObj.getString("appToken");
                    ZadSdkApi.init(application, appKey, appToken);
                    ret = true;
                    AdManager.adEnable = true;

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
                            }else if("feed".equals(type)){
                                feedCode = code;
                            }
                        }
                    }catch (Exception e){
                        err = e.getMessage();
                    }

                    ext.put("appKey", appKey);
                    ext.put("appToken", appToken);
                    if (gameStartArr!=null){
                        ext.put("gameStartArr", gameStartArr.toString());
                    }

                }catch (Exception e){
                    logger.error(e.getMessage());
                    err = e.getMessage();
                }
            }
        }else {
            AdManager.adEnable = false;
        }

        try {
            //发送打点事件
            Event event = Event.getEvent(ret ? EventCode.DATA_AD_INIT_OK : EventCode.DATA_AD_INIT_FAILED);
            if (err!=null){
                event.setErrMsg(err);
            }
            event.setExt(ext);
            MobclickAgent.sendEvent(event);
        }catch (Exception e){}

        return ret;
    }


    private static final String AD_TYPE_REWARD = "reward";
    private static final String AD_TYPE_FEED = "feed";

    private static final int LOAD_STATE_NONE = 0;
    private static final int LOAD_STATE_START = 1;
    private static final int LOAD_STATE_SUCCESS = 2;
    private static final int LOAD_STATE_FAILED = 3;

    private Activity mActivity;
    private IAdCallback mAdCallback;

    private int loadState = LOAD_STATE_NONE; //0未加载，1加载中，2加载成功，3加载失败
    private boolean waitingShow = false;

    private String mPackageName = null;

    private IAdLoader mLoader = null;

    public AdManager(Activity activity){
        this.mActivity = activity;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    public void prepareAd(){
        String adType = null;
        if (AdManager.rewardCode != null){
            adType = AD_TYPE_REWARD;
        }else if (AdManager.feedCode != null){
            adType = AD_TYPE_FEED;
        }

        loadAd(adType);
    }

    private synchronized void loadAd(String type){
        try {
            if (mLoader != null){
                mLoader.destory();
            }

            if (type == AD_TYPE_REWARD){
                mLoader =  new RewardAdLoader(AdManager.rewardCode);
            }else if (type == AD_TYPE_FEED){
                mLoader = new FeedAdLoader(AdManager.feedCode);
            }

            if (mLoader != null){
                loadState = LOAD_STATE_START;
                mLoader.setPkgName(mPackageName);
                mLoader.setLoaderCallback(mAdLoaderCallback);
                mLoader.loadAd(mActivity);
                return;
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        loadState = LOAD_STATE_FAILED;
        //加载失败
        if (mHandler!=null){
            mHandler.sendEmptyMessage(CB_AD_FAILED);
        }
    }

    private IAdLoaderCallback mAdLoaderCallback = new IAdLoaderCallback() {

        @Override
        public void onAdReady() {
            loadState = LOAD_STATE_SUCCESS;

            if (waitingShow){
                //显示广告
                if (mLoader != null){
                    mLoader.showAd();
                }
            }
        }

        @Override
        public void onAdClose(boolean b) {
            if (mHandler!=null){
                mHandler.sendEmptyMessage(b ? CB_AD_PASSED : CB_AD_CANCELED);
            }
        }

        @Override
        public void onAdFail() {
            //判断是否要加载另一种
            if (mLoader != null && mLoader instanceof RewardAdLoader){
                if (AdManager.feedCode != null){
                    loadAd(AD_TYPE_FEED);
                    return;
                }
            }

            loadState = LOAD_STATE_FAILED;
            //加载失败
            if (waitingShow){
                if (mHandler!=null){
                    mHandler.sendEmptyMessage(CB_AD_FAILED);
                }
            }

        }
    };

    private synchronized void showAd(){
        waitingShow = true;
        if (loadState == 2){
            //显示广告
            if (mLoader != null){
                mLoader.showAd();
            }
        }else if (loadState == 3){
            //广告加载失败
            if (mHandler!=null){
                mHandler.sendEmptyMessage(CB_AD_FAILED);
            }
        }else if (loadState == 1){
            //广告加载中，什么都不做

        }else {
            //广告未加载,开始加载
            prepareAd();
        }
    }

    public void loadGameAd(IAdCallback adCallback){
        this.mAdCallback = adCallback;

        try {
            //判断是否已经看过广告了
            int adVerify = ProferencesUtils.getIng(mActivity, SharedKeys.KEY_AD_REWARD_VERIFY_FLAG, 0);
            if (adVerify > 0) {
                if (mHandler!=null){
                    mHandler.sendEmptyMessage(CB_AD_PASSED);
                }
                return;
            }

            //判断广告是否已经加载失败，加载失败后就不弹出弹窗
            if (loadState == LOAD_STATE_FAILED){
                if (mHandler!=null){
                    mHandler.sendEmptyMessage(CB_AD_FAILED);
                }
                return;
            }

            //加载广告
            if (mHandler!=null){
                mHandler.sendEmptyMessage(CB_AD_LOADING);
            }

            //显示广告弹窗
            showAdRemindDialog();

        }catch (Exception e){
            logger.error("showAd error:" + e.getMessage());

            if (mHandler!=null){
                mHandler.sendEmptyMessage(CB_AD_FAILED);
            }
        }
    }

    //显示广告弹窗
    private void showAdRemindDialog() {
        try {
            AdRemindDialog dialog = new AdRemindDialog(mActivity);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        try {
                            //发送打点事件
                            MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_DIALOG_CANCEL, mPackageName));
                        }catch (Exception e){}
                    }
                });
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        try {
                            //发送打点事件
                            MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_DIALOG_DISPLAY, mPackageName));
                        }catch (Exception e){}
                    }
                });

            dialog.setOnCancelListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //取消按钮
                        if (mHandler!=null){
                            mHandler.sendEmptyMessage(CB_AD_CANCELED);
                        }
                    }
                });
            dialog.setOnSubmitListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //显示广告按钮
                    showAd();
                }
            });
            dialog.show();
        }catch (Exception e){
            throw e;
        }
    }

    public void destory() {
        try {
            mAdCallback = null;
        }catch (Exception e){
            logger.error("destory error:"+e.getMessage());
        }
    }


    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 0){
                return;
            }
            //取消按钮
            if (mAdCallback!=null){
                mAdCallback.onAdCallback(null, msg.what);
            }
        }
    };

}
