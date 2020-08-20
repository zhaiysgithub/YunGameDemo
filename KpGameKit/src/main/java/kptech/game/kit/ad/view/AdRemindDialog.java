package kptech.game.kit.ad.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.zad.sdk.Oapi.ZadSdkApi;
import com.zad.sdk.Oapi.callback.ZadRewardAdObserver;
import com.zad.sdk.Oapi.work.ZadRewardWorker;

import java.util.HashMap;

import kptech.game.kit.GameInfo;
import kptech.game.kit.R;
import kptech.game.kit.ad.AdInfo;
import kptech.game.kit.ad.AdManager;
import kptech.game.kit.ad.IAdCallback;
import kptech.game.kit.analytic.Event;
import kptech.game.kit.analytic.EventCode;
import kptech.game.kit.analytic.MobclickAgent;
import kptech.game.kit.utils.Logger;

public class AdRemindDialog extends AlertDialog implements View.OnClickListener {
    private static final Logger logger = new Logger("AdRemindDialog") ;

    private Activity mActivity;
    private ZadRewardWorker mRewardWorker;
    private IAdCallback<String> mCallback;

    private String mRewardAdCode;
    private String mExtAdCode;
    private GameInfo mGameInfo;
    private String mGamePkg;

    public AdRemindDialog setRewardAdCode(String adCode){
        this.mRewardAdCode = adCode;
        return this;
    }

    public AdRemindDialog setExtAdCode(String adCode){
        this.mExtAdCode = adCode;
        return this;
    }

    public AdRemindDialog setCallback(IAdCallback callback) {
        this.mCallback = callback;
        return this;
    }

    public AdRemindDialog setGameInfo(GameInfo gameInfo){
        this.mGameInfo = gameInfo;
        if (gameInfo != null){
            mGamePkg = gameInfo.pkgName;
        }else {
            mGamePkg = null;
        }
        return this;
    }

    public AdRemindDialog(Activity context) {
        super(context, R.style.RemindDialog);
        this.mActivity = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ad_remind_dialog);
        initView();
        this.setCanceledOnTouchOutside(false);


        try {
            //发送打点事件
            Event event = Event.getEvent(EventCode.DATA_AD_DIALOG_DISPLAY);
            if (this.mGameInfo!=null){
                event.setGamePkg(this.mGameInfo.pkgName);
            }
            HashMap ext = new HashMap<>();
            ext.put("rewardAdCode", this.mRewardAdCode);
            ext.put("extAdCode", this.mExtAdCode);
            event.setExt(ext);
            MobclickAgent.sendEvent(event);
        }catch (Exception e){}
    }

    public void initView() {
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.submit).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.cancel) {
            dismiss();
            if (mCallback!=null){
                mCallback.onCallback("cancel", 0);
            }
            try {
                //发送打点事件
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_DIALOG_CANCEL, mGamePkg));
            }catch (Exception e){}
        } else if (i == R.id.submit) {
            dismiss();
            //加载广告
            loadRewardAd();
        }
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (mCallback!=null){
                mCallback.onCallback("success", 1);
            }
        }
    };

    private synchronized void submit(){
        if (mHandler.hasMessages(1)){
            return;
        }
        //延时1秒钟调用
        mHandler.sendEmptyMessageDelayed(1, 1000);
    }

    /**
     * 加载激励视频广告
     */
    private void loadRewardAd(){
        try {
            //发送打点事件
            HashMap ext = new HashMap<>();
            ext.put("rewardAdCode", this.mRewardAdCode);
            MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_LOADING, mGamePkg, ext));
        }catch (Exception e){}


        //加载广告
        mRewardWorker = ZadSdkApi.getRewardAdWorker(this.mActivity, new MyRewardObserver(), this.mRewardAdCode);
        if (mRewardWorker != null) {
            mRewardWorker.requestProviderAd();
        }
    }


    /**  各个类型广告的回调  **/
    public class MyRewardObserver extends ZadRewardAdObserver {
        private boolean mRewardVerify = false;
        public MyRewardObserver(){
        }

        @Override
        public void onAdClosed(String posId, String info) {
            logger.info("onAdClosed(),   posId = " + posId + ", info = " + info);
            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("rewardAdCode", mRewardAdCode);
                ext.put("posId", posId);
                ext.put("rewardVerify", mRewardVerify);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_CLOSED, mGamePkg, ext));
            }catch (Exception e){}

            if (!mRewardVerify){
                if (mCallback!=null){
                    mCallback.onCallback(" ad closed ", 0);
                }
                return;
            }
            //进入游戏
            submit();
        }

        @Override
        public void onRewardVerify(String posId, boolean rewardVerify, int rewardAmount, String rewardName) {
            logger.info("onRewardVerify(),   rewardVerify = " + rewardVerify);
            mRewardVerify = rewardVerify;

            Toast.makeText(mActivity, "您已获得一次试玩机会", Toast.LENGTH_LONG).show();

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("rewardAdCode", mRewardAdCode);
                ext.put("posId", posId);
                ext.put("rewardVerify", rewardVerify);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_VERIFY, mGamePkg, ext));
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
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_PLAYCOMPLETE, mGamePkg, ext));
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
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_DISPLAY, mGamePkg, ext));
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
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_CLICK, mGamePkg, ext));
            }catch (Exception e){}
        }

        @Override
        public void onAdReady(String posId, int count, String info) {
            logger.info("onAdReady(),   count = " + count + ", info = " + info);
            if (mRewardWorker != null) mRewardWorker.showRewardAd();

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("rewardAdCode", mRewardAdCode);
                ext.put("posId", posId);
                ext.put("count", count);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_READY, mGamePkg, ext));
            }catch (Exception e){}
        }

        @Override
        public void onAdEmpty(String posId, String info) {
            logger.error( "onAdEmpty, posId = " + posId + ", info = " + info);
//            Toast.makeText(mActivity, "未获取到激励视频", Toast.LENGTH_LONG).show();

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("rewardAdCode", mRewardAdCode);
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_EMPTY, mGamePkg, ext));
            }catch (Exception e){}


            //没有广告，加截另一种
            if (showPopupAd(mActivity, mExtAdCode)) {
                return;
            }

            //进入游戏
            submit();
        }
    }


    private boolean showPopupAd(Activity activity, String code){
        if (activity == null || code==null){
            return false;
        }

        try {
            AdPopupWindow pop = new AdPopupWindow(activity, code, mGamePkg);
            pop.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    //进入游戏
                    submit();
                }
            });
            pop.showAtLocation(activity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
            return true;
        }catch (Exception e){
            logger.error(e.getMessage());
        }
        return false;
    }
}
