package kptech.game.kit.ad.loader;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zad.sdk.Oapi.ZadSdkApi;
import com.zad.sdk.Oapi.bean.ZadFeedDataAdBean;
import com.zad.sdk.Oapi.callback.ZadFeedDataAdObserver;
import com.zad.sdk.Oapi.work.ZadFeedDataWorker;

import java.util.HashMap;
import java.util.List;

import kptech.game.kit.ad.view.AdFeedPopup;
import kptach.game.kit.inter.ad.IAdLoader;
import kptach.game.kit.inter.ad.IAdLoaderCallback;


public class FeedAdLoader implements IAdLoader {
//    private final Logger logger = new Logger("FeedAdLoader") ;

    private String mAdCode;
    private ZadFeedDataWorker mFeedWorker;
    private IAdLoaderCallback mCallback;

    private ZadFeedDataAdBean adBean;

    private Activity mActivity;

    public FeedAdLoader(String adCode){
        this.mAdCode = adCode;
    }

    @Override
    public void destory() {
    }

    @Override
    public String getAdType() {
        return "feed";
    }

    @Override
    public String getAdCode() {
        return mAdCode;
    }

    @Override
    public void loadAd(Activity activity) {
        mActivity = activity;

//        try {
//            //发送打点事件
//            HashMap ext = new HashMap<>();
//            ext.put("adCode", mAdCode);
//            MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_FEED_LOADING, mPkgName, ext));
//        }catch (Exception e){}

        //加载广告
        mFeedWorker = ZadSdkApi.getFeedDataAdWorker(activity, new MyFeedDataObser(), mAdCode);
        if (mFeedWorker != null) {
            mFeedWorker.requestProviderAd();
//            mFeedWorker.setRequestCount(1, 280);     // 影响请求个数；影响点击区域高度。（多数情况不允许指定）会影响小米
        }
    }

    @Override
    public void showAd() {
        showFeedAd();
    }

    @Override
    public void setLoaderCallback(IAdLoaderCallback callback) {
        mCallback = callback;
    }

    private boolean adClicked = false;
    private class MyFeedDataObser extends ZadFeedDataAdObserver {

        @Override
        public void onAdShow(String posId, String info) {
            if (mCallback != null){
                mCallback.onAdShow(FeedAdLoader.this, posId, info);
            }

//            logger.info("onAdShow,   posId = " + posId + ", info = " + info);
//            try {
//                //发送打点事件
//                HashMap ext = new HashMap<>();
//                ext.put("posId", posId);
//                ext.put("info", info);
//                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_FEED_DISPLAY, mPkgName, ext));
//            }catch (Exception e){}
        }

        @Override
        public void onAdClick(String posId, String info) {
//            logger.info("onAdClick,   posId = " + posId + ", info = " + info);
            adClicked = true;

            if (mCallback != null){
                mCallback.onAdClick(FeedAdLoader.this, posId, info);
            }

//            try {
//                //发送打点事件
//                HashMap ext = new HashMap<>();
//                ext.put("posId", posId);
//                ext.put("info", info);
//                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_FEED_CLICK, mPkgName, ext));
//            }catch (Exception e){}
        }

        @Override
        public void onAdReady(String posId, int count, String info) {
//            logger.info("onAdReady,   count = " + count + ", info = " + info);

            if (mFeedWorker != null){
                List<ZadFeedDataAdBean> mAdBeanNewList = mFeedWorker.getAdBeans();
                if (mAdBeanNewList != null && mAdBeanNewList.size() > 0){
                    adBean = mAdBeanNewList.get(0);
                }
            }

//            try {
//                //发送打点事件
//                HashMap ext = new HashMap<>();
//                ext.put("posId", posId);
//                ext.put("count", count);
//                ext.put("info", info);
//                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_FEED_READY, mPkgName, ext));
//            }catch (Exception e){}

            if (mCallback != null){
                mCallback.onAdReady(FeedAdLoader.this, posId, count, info);
            }
        }

        @Override
        public void onAdEmpty(String posId, String info) {
            if (mCallback != null){
                mCallback.onAdEmpty(FeedAdLoader.this, posId, info);
            }

//            logger.error( "onAdEmpty, posId = " + posId + ", info = " + info);
//
//            try {
//                //发送打点事件
//                HashMap ext = new HashMap<>();
//                ext.put("posId", posId);
//                ext.put("info", info);
//                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_FEED_EMPTY, mPkgName,null, info, ext));
//            }catch (Exception e){}
//
//            if (mCallback!=null){
//                mCallback.onAdFail();
//            }
        }

    }

    private PopupWindow mAdPopupWindow;
    private void showFeedAd(){
        //广告view加载失败
        if (adBean == null || mActivity==null || mActivity.isFinishing()){
//            if (mCallback!=null){
//                mCallback.onAdFail();
//            }

            if (mCallback != null){
                mCallback.onAdEmpty(FeedAdLoader.this, mAdCode, "show feed ad empty");
            }
            return;
        }

        String err = "";
        try {
            mAdPopupWindow = new AdFeedPopup(mActivity, adBean);
            mAdPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
//                    try {
//                        //发送打点事件
//                        HashMap ext = new HashMap<>();
//                        MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_FEED_CLOSED, mPkgName, ext));
//                    }catch (Exception e){}



                    if (mCallback!=null){
                        mCallback.onAdClosed(FeedAdLoader.this, mAdCode, "close");
                    }

                    try {
                        if (mActivityCallback!=null){
                            mActivity.getApplication().unregisterActivityLifecycleCallbacks(mActivityCallback);
                            mActivityCallback = null;
                        }
                    }catch (Exception e){}

                }
            });

            mAdPopupWindow.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);

            try {
                //增加actvity生命周期监听
                mActivityCallback = new PopupActivityLifecycleCallbacks();
                mActivity.getApplication().registerActivityLifecycleCallbacks(mActivityCallback);
            }catch (Exception e){}

            return;
        }catch (Exception e){
//            logger.error(e.getMessage());
            err = e.getMessage();
        }

//        if (mCallback!=null){
//            mCallback.onAdFail();
//        }

        if (mCallback != null){
            mCallback.onAdEmpty(FeedAdLoader.this, mAdCode, "show feed ad error:" + err);
        }
    }

    private PopupActivityLifecycleCallbacks mActivityCallback;
    private class PopupActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {

        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            if (adClicked && activity==mActivity && mAdPopupWindow!=null && mAdPopupWindow.isShowing()){
                mAdPopupWindow.dismiss();
            }
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            if (adClicked && mAdPopupWindow!=null && mAdPopupWindow.isShowing()){
//                mAdPopupWindow.dismiss();
            }
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {

        }
    }
}
