package kptech.game.kit.ad;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import kptech.game.kit.ad.inter.IAdLoader;
import kptech.game.kit.ad.inter.IAdLoaderCallback;
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

    public static boolean adEnable = false;


    public static String rewardCode = null;
    public static String extCode = null;
    public static String feedCode = null;

    public static boolean init(Application application){
        boolean ret = false;


//        if (AdLoaderFactory.init(application, "ZM_appSDK_00029", "MadXeXeJf7zNzBIH")){
//            AdManager.adEnable = true;
//        }
//
//        rewardCode = "ZM_SDKAD_1_00066";
//        feedCode = "ZM_SDKAD_1_00107";

        if (AdLoaderFactory.init(application, "ZM_appSDK_00038", "NMjBe5rjxwCboi5q")){
            AdManager.adEnable = true;
        }

        rewardCode = "ZM_SDKAD_1_00119";
        feedCode = "ZM_SDKAD_1_00121";


//        //判断广告是否开启
//        String adEnable = ProferencesUtils.getString(application, SharedKeys.KEY_GAME_APP_ADENABLE,null);
//
//        HashMap ext = new HashMap();
//        ext.put("adEnable", adEnable);
//
//        String err = null;
//
//        if (adEnable!=null && "1".equals(adEnable)){
//            String adJson = ProferencesUtils.getString(application, SharedKeys.KEY_GAME_APP_ADJSON,null);
//            if (adJson!=null){
//                try {
//                    JSONObject adObj = new JSONObject(adJson);
//                    String appKey = adObj.getString("appKey");
//                    String appToken = adObj.getString("appToken");
////                    ZadSdkApi.init(application, appKey, appToken);
//
//                    if (AdLoaderFactory.init(application, appKey, appToken)){
//                        AdManager.adEnable = true;
//                    }
//
//                    ret = true;
//
//                    JSONArray gameStartArr = adObj.getJSONArray("gameStart");
//                    try {
//                        for (int i = 0; i < gameStartArr.length(); i++) {
//                            JSONObject gameObj = gameStartArr.getJSONObject(i);
//                            String type = gameObj.getString("adType");
//                            String code = gameObj.getString("adCode");
//                            if ("reward".equals(type)) {
//                                rewardCode = code;
//                            } else if ("interstitial".equals(type)) {
//                                extCode = code;
//                            }else if("feed".equals(type)){
//                                feedCode = code;
//                            }
//                        }
//                    }catch (Exception e){
//                        err = e.getMessage();
//                    }
//
//                    ext.put("appKey", appKey);
//                    ext.put("appToken", appToken);
//                    if (gameStartArr!=null){
//                        ext.put("gameStartArr", gameStartArr.toString());
//                    }
//
//                }catch (Exception e){
//                    logger.error(e.getMessage());
//                    err = e.getMessage();
//                }
//            }
//        }else {
//            AdManager.adEnable = false;
//        }
//
//        try {
//            //发送打点事件
//            Event event = Event.getEvent(ret ? EventCode.DATA_AD_INIT_OK : EventCode.DATA_AD_INIT_FAILED);
//            if (err!=null){
//                event.setErrMsg(err);
//            }
//            event.setExt(ext);
//            MobclickAgent.sendEvent(event);
//        }catch (Exception e){}

        return ret;
    }

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
        //判断是否打开广告
        if (!AdManager.adEnable){
            return;
        }

        if (AdManager.rewardCode != null){
            loadAd(AdLoaderFactory.AD_TYPE_REWARD, AdManager.rewardCode);
        }else if (AdManager.feedCode != null){
            loadAd(AdLoaderFactory.AD_TYPE_FEED, AdManager.feedCode);
        }
    }

    private synchronized void loadAd(String type, String code){
        try {
            if (mLoader != null){
                mLoader.destory();
            }

            mLoader = AdLoaderFactory.createrAdLoader(type, code);

            if (mLoader != null){
                try {
                    //发送打点事件
                    String eventCode = EventCode.DATA_AD_REWARD_LOADING;
                    if ((AdLoaderFactory.AD_TYPE_FEED).equals(type)){
                        eventCode = EventCode.DATA_AD_FEED_LOADING;
                    }
                    HashMap ext = new HashMap<>();
                    ext.put("adCode", code);
                    MobclickAgent.sendEvent(Event.getEvent(eventCode, mPackageName, ext));
                }catch (Exception e){}

                loadState = LOAD_STATE_START;
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
        private boolean mRewardVerify = false;

        @Override
        public void onAdClosed(IAdLoader loader, String posId, String info) {
            if (mHandler!=null){
                mHandler.sendEmptyMessage(mRewardVerify ? CB_AD_PASSED : CB_AD_CANCELED);
            }

            try {
                //发送打点事件
                String eventCode = EventCode.DATA_AD_REWARD_CLOSED;
                if (loader!=null && loader.getAdType().equals(AdLoaderFactory.AD_TYPE_FEED)){
                    eventCode = EventCode.DATA_AD_FEED_CLOSED;
                }
                HashMap ext = new HashMap<>();
                ext.put("posId", posId);
                ext.put("info", info);
                ext.put("rewardVerify", mRewardVerify);
                MobclickAgent.sendEvent(Event.getEvent(eventCode, mPackageName, ext));
            }catch (Exception e){}
        }

        @Override
        public void onRewardVerify(IAdLoader loader, String posId, boolean rewardVerify, int rewardAmount, String rewardName) {
            mRewardVerify = rewardVerify;

            if (mActivity != null){
                //保存到缓存中
                ProferencesUtils.setInt(mActivity, SharedKeys.KEY_AD_REWARD_VERIFY_FLAG, 1);
            }

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("posId", posId);
                ext.put("rewardVerify", rewardVerify);
                ext.put("rewardName", rewardName);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_VERIFY, mPackageName, ext));
            }catch (Exception e){}
        }

        @Override
        public void onPlayComplete(IAdLoader loader, String posId, String info) {
            mRewardVerify = true;

            try {
                //发送打点事件
                HashMap ext = new HashMap<>();
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_REWARD_PLAYCOMPLETE, mPackageName, ext));
            }catch (Exception e){}
        }

        @Override
        public void onAdShow(IAdLoader loader, String posId, String info) {
            if (loader!=null && loader.getAdType().equals(AdLoaderFactory.AD_TYPE_FEED)){
                mRewardVerify = true;
            }

            try {
                //发送打点事件
                String eventCode = EventCode.DATA_AD_REWARD_DISPLAY;
                if (loader!=null && loader.getAdType().equals(AdLoaderFactory.AD_TYPE_FEED)){
                    eventCode = EventCode.DATA_AD_FEED_DISPLAY;
                }
                HashMap ext = new HashMap<>();
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(eventCode, mPackageName, ext));
            }catch (Exception e){}
        }

        @Override
        public void onAdClick(IAdLoader loader, String posId, String info) {
            try {
                //发送打点事件
                String eventCode = EventCode.DATA_AD_REWARD_CLICK;
                if (loader!=null && loader.getAdType().equals(AdLoaderFactory.AD_TYPE_FEED)){
                    eventCode = EventCode.DATA_AD_FEED_CLICK;
                }
                HashMap ext = new HashMap<>();
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(eventCode, mPackageName, ext));
            }catch (Exception e){}
        }

        @Override
        public void onAdReady(IAdLoader loader, String posId, int count, String info) {

            try {
                //发送打点事件
                String eventCode = EventCode.DATA_AD_REWARD_READY;
                if (loader!=null && loader.getAdType().equals(AdLoaderFactory.AD_TYPE_FEED)){
                    eventCode = EventCode.DATA_AD_FEED_READY;
                }
                HashMap ext = new HashMap<>();
                ext.put("posId", posId);
                ext.put("count", count);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(eventCode, mPackageName, ext));
            }catch (Exception e){}


            loadState = LOAD_STATE_SUCCESS;
            if (waitingShow){
                //显示广告
                if (mLoader != null){
                    mLoader.showAd();
                }
            }

        }

        @Override
        public void onAdEmpty(IAdLoader loader, String posId, String info) {

            try {
                //发送打点事件
                String eventCode = EventCode.DATA_AD_REWARD_EMPTY;
                if (loader!=null && loader.getAdType().equals(AdLoaderFactory.AD_TYPE_FEED)){
                    eventCode = EventCode.DATA_AD_FEED_EMPTY;
                }
                HashMap ext = new HashMap<>();
                ext.put("posId", posId);
                ext.put("info", info);
                MobclickAgent.sendEvent(Event.getEvent(eventCode, mPackageName, null, info, ext));
            }catch (Exception e){}

            //判断是否要加载另一种
            if (loader!=null && loader.getAdType().equals(AdLoaderFactory.AD_TYPE_REWARD)){
                if (AdManager.feedCode != null){
                    loadAd(AdLoaderFactory.AD_TYPE_FEED, AdManager.feedCode);
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
        if (loadState == LOAD_STATE_SUCCESS && mLoader != null){
            //显示广告
            mLoader.showAd();
        }else if (loadState == LOAD_STATE_FAILED && mHandler!=null){
            //广告加载失败
            mHandler.sendEmptyMessage(CB_AD_FAILED);
        }else if (loadState == LOAD_STATE_START){
            //广告加载中，什么都不做

        }else if (loadState == LOAD_STATE_NONE) {
            //广告未加载,开始加载
            prepareAd();
        }else {
            if (mHandler!=null){
                mHandler.sendEmptyMessage(CB_AD_FAILED);
            }
        }
    }

    public void loadGameAd(IAdCallback adCallback){
        this.mAdCallback = adCallback;
        //判断是否打开广告
        if (!AdManager.adEnable){
            if (mHandler!=null){
                mHandler.sendEmptyMessage(CB_AD_PASSED);
            }
            return;
        }

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
                    try {
                        //发送打点事件
                        MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_DIALOG_CANCEL, mPackageName));
                    }catch (Exception e){}

                    //取消按钮
                    if (mHandler!=null){
                        mHandler.sendEmptyMessage(CB_AD_CANCELED);
                    }
                }
            });
            dialog.setOnSubmitListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {
                        //发送打点事件
                        MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_AD_DIALOG_SUBMIT, mPackageName));
                    }catch (Exception e){}

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

