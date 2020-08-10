package kptech.game.kit.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.zad.sdk.Oapi.ZadSdkApi;
import com.zad.sdk.Oapi.callback.ZadInterstitialAdObserver;
import com.zad.sdk.Oapi.callback.ZadRewardAdObserver;
import com.zad.sdk.Oapi.work.ZadInterstitialWorker;
import com.zad.sdk.Oapi.work.ZadRewardWorker;

import kptech.game.kit.R;
import kptech.game.kit.ad.AdManager;
import kptech.game.kit.utils.Logger;

public class AdRemindDialog extends AlertDialog implements View.OnClickListener {
    private static final Logger logger = new Logger("AdRemindDialog") ;

    private Activity mActivity;
    private ZadRewardWorker mRewardWorker;
    private String mAdCode;
    private String mExtAdCode = "ZM_SDKAD_1_00065";
    private IRemindDialogCallback mCallback;
    public interface IRemindDialogCallback {
        void onSubmit();
        void onCancel();
    }

    public AdRemindDialog setAdCode(String adCode) {
        this.mAdCode = adCode;
        return this;
    }

    public AdRemindDialog setCallback(IRemindDialogCallback callback) {
        this.mCallback = callback;
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
                mCallback.onCancel();
            }
        } else if (i == R.id.submit) {
            dismiss();
            //加载广告
            loadRewardAd();
        }
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (mCallback!=null){
                mCallback.onSubmit();
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
        //加载广告
        mRewardWorker = ZadSdkApi.getRewardAdWorker(this.mActivity, new MyRewardObserver(), this.mAdCode);
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
            if (!mRewardVerify){
                if (mCallback!=null){
                    mCallback.onCancel();
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
        }

        @Override
        public void onPlayComplete(String posId, String info) {
            logger.info("onPlayComplete(),   posId = " + posId + ", info = " + info);
        }

        @Override
        public void onAdShow(String posId, String info) {
            logger.info("onAdShow(),   posId = " + posId + ", info = " + info);
        }

        @Override
        public void onAdClick(String posId, String info) {
            logger.info("onAdClick(),   posId = " + posId + ", info = " + info);
        }

        @Override
        public void onAdReady(String posId, int count, String info) {
            logger.info("onAdReady(),   count = " + count + ", info = " + info);
            if (mRewardWorker != null) mRewardWorker.showRewardAd();
        }

        @Override
        public void onAdEmpty(String posId, String info) {
            logger.error( "onAdEmpty, posId = " + posId + ", info = " + info);

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
            AdPopupWindow pop = new AdPopupWindow(activity, code);
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
