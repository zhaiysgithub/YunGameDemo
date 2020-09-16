package kptech.game.kit.ad.loader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.zad.sdk.Oapi.ZadSdkApi;
import com.zad.sdk.Oapi.bean.ZadFeedDataAdBean;
import com.zad.sdk.Oapi.callback.ZadFeedDataAdObserver;
import com.zad.sdk.Oapi.work.ZadFeedDataWorker;
import com.zad.sdk.Oapi.work.ZadRewardWorker;

import java.util.HashMap;
import java.util.List;

import kptech.game.kit.R;
import kptech.game.kit.ad.view.AdFeedDialog;
import kptech.game.kit.ad.view.AdFeedPopup;
import kptech.game.kit.ad.view.AdPopupWindow;
import kptech.game.kit.ad.view.TestPopupWindow;
import kptech.game.kit.analytic.Event;
import kptech.game.kit.analytic.EventCode;
import kptech.game.kit.analytic.MobclickAgent;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;

public class FeedAdLoader implements IAdLoader {
    private final Logger logger = new Logger("RewardAdLoader") ;

    private String mAdCode;
    private ZadFeedDataWorker mFeedWorker;
    private IAdLoaderCallback mCallback;

    private ZadFeedDataAdBean adBean;

    private Activity mActivity;

    private String mPkgName;
    public void setPkgName(String pkgName) {
        this.mPkgName = pkgName;
    }

    public FeedAdLoader(String adCode){
        this.mAdCode = adCode;
    }

    @Override
    public void destory() {

    }

    @Override
    public void loadAd(Activity activity) {
        mActivity = activity;

        try {
            //发送打点事件
            HashMap ext = new HashMap<>();
            ext.put("adCode", mAdCode);
            MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_FEED_LOADING, mPkgName, ext));
        }catch (Exception e){}

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


    private class MyFeedDataObser extends ZadFeedDataAdObserver {

        @Override
        public void onAdShow(String posId, String info) {
            logger.info("onAdShow,   posId = " + posId + ", info = " + info);
            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_FEED_DISPLAY, mPkgName, ext));
            }catch (Exception e){}
        }

        @Override
        public void onAdClick(String posId, String info) {
            logger.info("onAdClick,   posId = " + posId + ", info = " + info);

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_FEED_CLICK, mPkgName, ext));
            }catch (Exception e){}
        }

        @Override
        public void onAdReady(String posId, int count, String info) {
            logger.info("onAdReady,   count = " + count + ", info = " + info);

            if (mFeedWorker != null){
                List<ZadFeedDataAdBean> mAdBeanNewList = mFeedWorker.getAdBeans();
                if (mAdBeanNewList != null && mAdBeanNewList.size() > 0){
                    adBean = mAdBeanNewList.get(0);
                }
            }

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("posId", posId);
                ext.put("count", count);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_FEED_READY, mPkgName, ext));
            }catch (Exception e){}

            if (mCallback!=null){
                mCallback.onAdReady();
            }
        }

        @Override
        public void onAdEmpty(String posId, String info) {
            logger.error( "onAdEmpty, posId = " + posId + ", info = " + info);

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_FEED_EMPTY, mPkgName, ext));
            }catch (Exception e){}

            if (mCallback!=null){
                mCallback.onAdFail();
            }
        }

    }

    private void showFeedAd(){
        //广告view加载失败
        if (adBean == null || mActivity==null || mActivity.isFinishing()){
            if (mCallback!=null){
                mCallback.onAdFail();
            }
            return;
        }

//        int[] a = new int[0];
//        TestPopupWindow pop = new TestPopupWindow(mActivity,a,adBean);
//        pop.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);

//        Dialog mMenuDialog = new AdFeedDialog(mActivity, adBean);
//        mMenuDialog.show();



        try {
//            PopupWindow pop = new PopupWindow(mActivity);
//            TestLayout layout = new TestLayout(mActivity, adBean);
//            pop.setContentView(layout);
//            // 设置SelectPicPopupWindow弹出窗体的宽
//            pop.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
//            // 设置SelectPicPopupWindow弹出窗体的高
//            pop.setHeight(WindowManager.LayoutParams.MATCH_PARENT);//屏幕的高
            PopupWindow pop = new AdFeedPopup(mActivity, adBean);
            pop.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {

                    try {
                        //发送打点事件
                        HashMap ext = new HashMap<>();
                        MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_FEED_CLOSED, mPkgName, ext));
                    }catch (Exception e){}


                    if (mCallback!=null){
                        mCallback.onAdClose();
                    }
                }
            });
            pop.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
            return;
        }catch (Exception e){
            logger.error(e.getMessage());
        }

        if (mCallback!=null){
            mCallback.onAdFail();
        }
    }
}
