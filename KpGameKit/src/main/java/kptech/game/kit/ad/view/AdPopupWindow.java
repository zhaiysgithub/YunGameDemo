package kptech.game.kit.ad.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.zad.sdk.Oapi.ZadSdkApi;
import com.zad.sdk.Oapi.callback.ZadInterstitialAdObserver;
import com.zad.sdk.Oapi.work.ZadInterstitialWorker;

import java.util.HashMap;

import kptech.game.kit.R;
import kptech.game.kit.analytic.Event;
import kptech.game.kit.analytic.EventCode;
import kptech.game.kit.analytic.MobclickAgent;
import kptech.game.kit.utils.Logger;

public class AdPopupWindow extends PopupWindow  {
    private static final Logger logger = new Logger("AdRemindDialog") ;

    private Activity mActivity;
    private ViewGroup mLayout;
    private LayoutInflater inflater;
    private String mAdCode;
    private String mGamePkg;

    public AdPopupWindow(Activity activity, String code, String gamePkg) {//, int position,
        super(activity);
        this.mActivity = activity;
        this.mAdCode = code;
        this.mGamePkg = gamePkg;

        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.ad_popup_layout, null);
        this.setContentView(view);

        mLayout = view.findViewById(R.id.layout);


        //sdk > 21 解决 标题栏没有办法遮罩的问题
        this.setClippingEnabled(false);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(WindowManager.LayoutParams.MATCH_PARENT);//屏幕的高
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
//        this.setAnimationStyle(R.style.AnimationWindow);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xff000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
//        mLayout.setOnTouchListener(new View.OnTouchListener() {
//
//            public boolean onTouch(View v, MotionEvent event) {
//
////                int height = mMenuView.findViewById(R.id.pop_layout2).getTop();
////                int y = (int) event.getY();
////                if (event.getAction() == MotionEvent.ACTION_UP) {
////                    if (y < height) {
////                        dismiss();
////                    }
////                }
//                return true;
//            }
//        });

        //
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);

        //加载广告
        loadInterstitialAd();
    }

    private ZadInterstitialWorker mInterstitialWorker;

    /**
     * 加载插屏广告
     */
    private void loadInterstitialAd() {
        try {
            //发送打点事件
            HashMap ext = new HashMap<>();
            ext.put("extAdCode", this.mAdCode);
            MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_LOADING, mGamePkg, ext));
        }catch (Exception e){}

        mInterstitialWorker = ZadSdkApi.getInterstitialAdWorker(this.mActivity, new MyInterObserver(), mAdCode);
        if (mInterstitialWorker != null) mInterstitialWorker.requestProviderAd();
    }

    class MyInterObserver extends ZadInterstitialAdObserver {

        @Override
        public void onAdShow(String posId, String info) {
            logger.info("onAdShow : " + info);
            Toast.makeText(mActivity, "您已获得一次试玩机会", Toast.LENGTH_LONG).show();

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("extAdCode", mAdCode);
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_DISPLAY, mGamePkg, ext));
            }catch (Exception e){}
        }

        @Override
        public void onAdClick(String posId, String info) {
            logger.info("onAdClick : " + info);

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("extAdCode", mAdCode);
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_CLOSED, mGamePkg, ext));
            }catch (Exception e){}
        }

        @Override
        public void onAdReady(String posId, int count, String info) {
            logger.info("onAdReady(), count = " + count + ", info = " + info);
            if (count <= 0) {
                mInterstitialWorker.getAdBeans();
            } else {
                mLayout.removeAllViews();
                mLayout.addView(mInterstitialWorker.getAdBeans().get(0).getAdView());
            }

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("extAdCode", mAdCode);
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_READY, mGamePkg, ext));
            }catch (Exception e){}
        }

        @Override
        public void onClose(String posId, String info) {
            logger.info("onClose, posId = " + posId + ", info = " + info);

            dismiss();

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("extAdCode", mAdCode);
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_CLOSED, mGamePkg, ext));
            }catch (Exception e){}
        }

        @Override
        public void onAdEmpty(String posId, String info) {
            logger.error("onAdEmpty, posId = " + posId + ", info = " + info);
//            Toast.makeText(mActivity, "未获取到插屏广告", Toast.LENGTH_LONG).show();
            dismiss();

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("extAdCode", mAdCode);
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_EMPTY, mGamePkg, ext));
            }catch (Exception e){}
        }
    }


}