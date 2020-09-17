package kptech.game.kit.ad.loader;

import android.app.Activity;

import com.zad.sdk.Oapi.ZadSdkApi;
import com.zad.sdk.Oapi.callback.ZadRewardAdObserver;
import com.zad.sdk.Oapi.work.ZadRewardWorker;

import java.util.HashMap;

import kptech.game.kit.ad.AdLoader;
import kptech.game.kit.analytic.Event;
import kptech.game.kit.analytic.EventCode;
import kptech.game.kit.analytic.MobclickAgent;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;

public class RewardAdLoader implements IAdLoader{

    private final Logger logger = new Logger("RewardAdLoader") ;

    private ZadRewardWorker mRewardWorker;
    private String mRewardAdCode;

    private IAdLoaderCallback mCallback;

    private String mPkgName;
    public void setPkgName(String pkgName) {
        this.mPkgName = pkgName;
    }

    public RewardAdLoader(String adCode){
        this.mRewardAdCode = adCode;
    }

    @Override
    public void destory() {

    }

    @Override
    public void loadAd(Activity activity) {
        try {
            //发送打点事件
            HashMap ext = new HashMap<>();
            ext.put("adCode", this.mRewardAdCode);
            MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_LOADING, mPkgName, ext));
        }catch (Exception e){}

        //加载广告
        mRewardWorker = ZadSdkApi.getRewardAdWorker(activity, new MyZadRewardAdObserver(activity), mRewardAdCode);
        if (mRewardWorker != null) {
            mRewardWorker.requestProviderAd();
        }

    }

    @Override
    public void showAd() {
        if (mRewardWorker != null) {
            mRewardWorker.showRewardAd();
        }
    }

    @Override
    public void setLoaderCallback(IAdLoaderCallback callback) {
        mCallback = callback;
    }

    /**  各个类型广告的回调  **/
    private class MyZadRewardAdObserver extends ZadRewardAdObserver{
        private boolean mRewardVerify = false;
        private Activity mActivity;

        public MyZadRewardAdObserver(Activity activity){
            this.mActivity = activity;
        }

        @Override
        public void onAdClosed(String posId, String info) {
            logger.info("onAdClosed(),   posId = " + posId + ", info = " + info);

            if (mCallback!=null){
                if (mRewardVerify) {
                    mCallback.onAdClose(true);
                }else {
                    mCallback.onAdClose(false);
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

            if (this.mActivity != null){
                //保存到缓存中
                ProferencesUtils.setInt(mActivity, SharedKeys.KEY_AD_REWARD_VERIFY_FLAG, 1);
            }

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
            mRewardVerify = true;

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

            if (mCallback!=null){
                mCallback.onAdReady();
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

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("rewardAdCode", mRewardAdCode);
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_EMPTY, mPkgName, ext));
            }catch (Exception e){}

            if (mCallback!=null){
                mCallback.onAdFail();
            }
        }
    };

}
