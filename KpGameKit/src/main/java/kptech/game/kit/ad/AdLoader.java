package kptech.game.kit.ad;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import com.zad.sdk.Oapi.ZadSdkApi;
import com.zad.sdk.Oapi.callback.ZadInterstitialAdObserver;
import com.zad.sdk.Oapi.callback.ZadRewardAdObserver;
import com.zad.sdk.Oapi.work.ZadInterstitialWorker;
import com.zad.sdk.Oapi.work.ZadRewardWorker;

import java.util.HashMap;

import kptech.game.kit.ad.view.AdPopupWindow;
import kptech.game.kit.analytic.Event;
import kptech.game.kit.analytic.EventCode;
import kptech.game.kit.analytic.MobclickAgent;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;

public class AdLoader {

    public static final int ADSTATE_VERIFY = 1;
    public static final int ADSTATE_CLOSE = 2;
    public static final int ADSTATE_FAILED = 3;

    public AdLoadState getRewardState() {
        return mAdLoadState;
    }

    public enum AdLoadState{
        unload,
        RewardReady,
        RewardFailed,
        RewardClosed,
    }

    private static final Logger logger = new Logger("AdLoader") ;

    private ZadInterstitialWorker mInterstitialWorker;
    private ZadRewardWorker mRewardWorker;

    private Activity mActivity;
    private String mRewardAdCode;
    private String mExtAdCode;

    private AdLoadState mAdLoadState = AdLoadState.unload;  //0未加载，1加载中，2 加载成功，3加载失败
    private boolean mExtAdReady = false;


    private boolean loading = false;
    private boolean extLoading = false;

    private boolean wattingShowAd = false;

    private IAdCallback<String> mAdCallback;
    public void setAdCallback(IAdCallback<String> callback){
        this.mAdCallback = callback;
    }

    private String mPkgName;
    public void setPackageName(String pkgName){
        this.mPkgName = pkgName;
    }

    public AdLoader(Activity activity){
        this.mActivity = activity;
    }

    public AdLoader(Activity activity, String rewardAdCode){
        this.mActivity = activity;
        this.mRewardAdCode = rewardAdCode;
    }

    public void setRewardAdCode(String rewardAdCode) {
        this.mRewardAdCode = rewardAdCode;
    }

    public void setExtAdCode(String extAdCode) {
        this.mExtAdCode = extAdCode;
    }

    public void destory(){
        mRewardWorker = null;
        mActivity = null;
        mAdCallback = null;
    }

    public void loadAd(){
        //判断是否已经加载过,不需要重新加载
        if (mRewardWorker!=null && loading) {
            return;
        }

        wattingShowAd = false;
        loadReward();
    }

    public void showAd(){
        wattingShowAd = true;

        //判断如果不存在 ，则加载
        if (mRewardWorker != null && mAdLoadState == AdLoadState.RewardReady) {
            mRewardWorker.showRewardAd();
            return;
        }

        //激励视频加载失败，显示插屏广告
        if (mAdLoadState == AdLoadState.RewardFailed){
            if (mAdCallback!=null){
                mAdCallback.onAdCallback(null, ADSTATE_FAILED);
            }
//
////            //显示广告
////            if (mExtAdReady && mInterstitialWorker!=null){
////                if (extAdView != null){
////                    showExtAd(extAdView);
////                }else {
////                    mInterstitialWorker.getAdBeans();
////                }
////            }
//
//            loadExtAd();
            return;
        }


        loadReward();
    }

    private synchronized void loadReward(){
        if (loading){
            return;
        }

        //加载广告
        mRewardWorker = ZadSdkApi.getRewardAdWorker(mActivity, mRewardObserver, mRewardAdCode);
        if (mRewardWorker != null) {
            mRewardWorker.requestProviderAd();
            loading = true;
        }

        try {
            //发送打点事件
            HashMap ext = new HashMap<>();
            ext.put("rewardAdCode", this.mRewardAdCode);
            MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_LOADING, mPkgName, ext));
        }catch (Exception e){}
    }



    /**  各个类型广告的回调  **/
    private ZadRewardAdObserver mRewardObserver = new ZadRewardAdObserver() {
        private boolean mRewardVerify = false;

        @Override
        public void onAdClosed(String posId, String info) {
            logger.info("onAdClosed(),   posId = " + posId + ", info = " + info);

            mAdLoadState = AdLoadState.RewardClosed;

            if (mAdCallback!=null){
                if (mRewardVerify) {
                    mAdCallback.onAdCallback(null, ADSTATE_VERIFY);
                }else {
                    mAdCallback.onAdCallback(null, ADSTATE_CLOSE);
                }
            }

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("rewardAdCode", mRewardAdCode);
                ext.put("posId", posId);
                ext.put("rewardVerify", mRewardVerify);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_CLOSED, mPkgName, ext));
            }catch (Exception e){}

        }

        @Override
        public void onRewardVerify(String posId, boolean rewardVerify, int rewardAmount, String rewardName) {
            logger.info("onRewardVerify(),   rewardVerify = " + rewardVerify);
            mRewardVerify = rewardVerify;

            //保存到缓存中
            ProferencesUtils.setInt(mActivity, SharedKeys.KEY_AD_REWARD_VERIFY_FLAG, 1);

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("rewardAdCode", mRewardAdCode);
                ext.put("posId", posId);
                ext.put("rewardVerify", rewardVerify);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_VERIFY, mPkgName, ext));
            }catch (Exception e){}
        }

        @Override
        public void onPlayComplete(String posId, String info) {
            logger.info("onPlayComplete(),   posId = " + posId + ", info = " + info);

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("rewardAdCode", mRewardAdCode);
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_PLAYCOMPLETE, mPkgName, ext));
            }catch (Exception e){}
        }

        @Override
        public void onAdShow(String posId, String info) {
            logger.info("onAdShow(),   posId = " + posId + ", info = " + info);
            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("rewardAdCode", mRewardAdCode);
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_DISPLAY, mPkgName, ext));
            }catch (Exception e){}
        }

        @Override
        public void onAdClick(String posId, String info) {
            logger.info("onAdClick(),   posId = " + posId + ", info = " + info);

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("rewardAdCode", mRewardAdCode);
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_CLICK, mPkgName, ext));
            }catch (Exception e){}
        }

        @Override
        public void onAdReady(String posId, int count, String info) {
            logger.info("onAdReady(),   count = " + count + ", info = " + info);

            loading = false;
            mAdLoadState = AdLoadState.RewardReady;

            if (mRewardWorker != null && wattingShowAd) {
                mRewardWorker.showRewardAd();
            }

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("rewardAdCode", mRewardAdCode);
                ext.put("posId", posId);
                ext.put("count", count);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_READY, mPkgName, ext));
            }catch (Exception e){}
        }

        @Override
        public void onAdEmpty(String posId, String info) {
            logger.error( "onAdEmpty, posId = " + posId + ", info = " + info);

            loading = false;
            mAdLoadState = AdLoadState.RewardFailed;

            if(wattingShowAd) {
                if (mAdCallback!=null){
                    mAdCallback.onAdCallback(info, ADSTATE_FAILED);
                }
            }

//            loadExtAd();

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("rewardAdCode", mRewardAdCode);
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_EMPTY, mPkgName, ext));
            }catch (Exception e){}
        }
    };

    private synchronized void loadExtAd(){
        if (extLoading){
            return;
        }

        //加载插屏广告
        mInterstitialWorker = ZadSdkApi.getInterstitialAdWorker(this.mActivity, mZadObserver, mExtAdCode);
        if (mInterstitialWorker != null) {
            mInterstitialWorker.requestProviderAd();
            extLoading = true;
        }

        try {
            //发送打点事件
            HashMap ext = new HashMap<>();
            ext.put("extAdCode", this.mExtAdCode);
            MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_LOADING, mPkgName, ext));
        }catch (Exception e){}
    }

    private View extAdView = null;
    private ZadInterstitialAdObserver mZadObserver = new ZadInterstitialAdObserver(){

        @Override
        public void onClose(String posId, String info) {
            logger.info("onClose, posId = " + posId + ", info = " + info);

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("extAdCode", mExtAdCode);
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_CLOSED, mPkgName, ext));
            }catch (Exception e){}
        }

        @Override
        public void onAdShow(String posId, String info) {
            logger.info("onAdShow : " + info);

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("extAdCode", mExtAdCode);
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_DISPLAY, mPkgName, ext));
            }catch (Exception e){}
        }

        @Override
        public void onAdClick(String posId, String info) {
            logger.info("onAdClick : " + info);
            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("extAdCode", mExtAdCode);
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_CLOSED, mPkgName, ext));
            }catch (Exception e){}
        }

        @Override
        public void onAdReady(String posId, int count, String info) {
            logger.info("onAdReady(), count = " + count + ", info = " + info);
            extLoading = false;
            mExtAdReady = true;

            if (wattingShowAd){
                if (count <= 0){
                    mInterstitialWorker.getAdBeans();
                }else {
                    //显示到popup
                    extAdView = mInterstitialWorker.getAdBeans().get(0).getAdView();
                    showExtAd(extAdView);
                }
            }

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("extAdCode", mExtAdCode);
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_READY, mPkgName, ext));
            }catch (Exception e){}
        }


        @Override
        public void onAdEmpty(String posId, String info) {
            logger.error("onAdEmpty, posId = " + posId + ", info = " + info);
            extLoading = false;

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("extAdCode", mExtAdCode);
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_EMPTY, mPkgName, ext));
            }catch (Exception e){}
        }
    };

    private void showExtAd(View adView){
        //广告view加载失败
        if (adView == null){
            return;
        }

        try {
            AdPopupWindow pop = new AdPopupWindow(mActivity, adView);
            pop.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                }
            });
            pop.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }
}
