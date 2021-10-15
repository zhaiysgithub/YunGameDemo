package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import kptech.game.kit.GameBoxManager;
import kptech.game.kit.GameInfo;
import kptech.game.kit.utils.AnimationUtil;
import kptech.game.kit.utils.Logger;
import kptech.lib.analytic.Event;
import kptech.lib.analytic.EventCode;
import kptech.lib.analytic.MobclickAgent;

public class PlayStatusLayout extends FrameLayout {
    private static final String TAG = PlayStatusLayout.class.getSimpleName();

    private static final int viewid_loading = 0x1001;
    private static final int viewid_error = 0x1002;
    private static final int viewid_auth = 0x1003;

    public static final int STATUS_ERROR = -1;

    public static final int STATUS_LOADING = 100;
    public static final int STATUS_LOADING_INIT = 101;
    public static final int STATUS_LOADING_GET_GAMEINFO = 102;
    public static final int STATUS_LOADING_CONNECT_DEVICE = 103;
    public static final int STATUS_LOADING_AD = 104;
    public static final int STATUS_LOADING_AD_PAUSE = 105;
    public static final int STATUS_LOADING_RECOVER_GAMEINFO = 106;
    public static final int STATUS_LOADING_START_GAME = 107;
    public static final int STATUS_LOADING_ERROR_STOP = 108;
    //游戏中错误
    public static final int STATUS_GAME_RUNNING_ERROR = 109;
    public static final int STATUS_LOADING_FINISHED = 110;

//    private LoadingPageView mLoadingView;
    //微包定制的loading页面
    private GameMiniLoadingView mLoadingView;
    private PlayErrorPageView mErrorView;
    private PlayAuthPageView mAuthView;

    private String pkgName;
    private GameInfo mGameInfo;

    public void setDownloadStatus(int status) {
        if (mErrorView != null){
            mErrorView.setDownloadStatus(status);
        }
    }

    public void setProgress(int progress, String text) {
        if (mErrorView != null){
            mErrorView.setProgress(progress, text);
        }
    }

    public PlayStatusLayout(Context context) {
        super(context);

    }

    public PlayStatusLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setGameInfo(GameInfo info){
        this.mGameInfo = info;
        if (info != null){
            pkgName = info.pkgName;

            if (mLoadingView != null){
                mLoadingView.setLoadingInfo(info);
            }
            if (mErrorView != null){
                mErrorView.setGameInfo(info);
            }
            if (mAuthView != null){
                mAuthView.setGameInfo(info);
            }
        }
    }

    private void initView() {

        mLoadingView = findViewById(viewid_loading);
        mErrorView = findViewById(viewid_error);
        mAuthView = findViewById(viewid_auth);

        mAuthView.onFinishInflate();
        mErrorView.onFinishInflate();
        mLoadingView.onFinishInflate();

        mLoadingView.setOnMiniLoadListener(mMiniLoadListener);
    }

    private final GameMiniLoadingView.OnMiniLoadListener mMiniLoadListener = new GameMiniLoadingView.OnMiniLoadListener() {
        @Override
        public void onDownloadClick(String msg) {
            //显示下载页面开始执行下载
            setStatus(STATUS_ERROR, msg);

        }

        @Override
        public void onGameRetryClick() {
            if (mLoadingView != null){
                mLoadingView.updateMiniDialogView(false, "",true);
            }
            GameBoxManager.getInstance().setOnReloadClick();
        }
    };

    /**
     * 显示授权界面
     */
    public void showUserAuthView(String unionUUID, String corpId){
        try {
            mLoadingView.setPausePro(true);

            if (mAuthView.getVisibility() == View.VISIBLE){
                return;
            }


            mAuthView.setGameInfo(mGameInfo);
            mAuthView.setAnimation(AnimationUtil.moveToViewLocation());
            mAuthView.setVisibility(View.VISIBLE);

            try {
                //发送打点事件
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_ACTIVITY_USERAUTH_DISPLAY, pkgName ));
            }catch (Exception e){
                e.printStackTrace();
            }
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }
    }

    /**
     * 隐藏授权界面
     */
    public void hideUserAuthView(){
        if (mAuthView.getVisibility() != View.VISIBLE){
            return;
        }
        mAuthView.setAnimation(AnimationUtil.moveToViewBottom());
        mAuthView.setVisibility(View.GONE);

        mLoadingView.setPausePro(false);
    }

    public void setStatus(int status, String msg) {
        if (status  == STATUS_ERROR){
            if (mErrorView != null){
                if (!mErrorView.isShown()){
                    mErrorView.setVisibility(VISIBLE);
                }

                mErrorView.setErrorText(msg);
            }
            if (mLoadingView != null && mLoadingView.isShown()){
                mLoadingView.setVisibility(GONE);
            }
        }else if (status >=  STATUS_LOADING && status <= STATUS_LOADING_FINISHED){
            if (mLoadingView != null){
                if (!mLoadingView.isShown()){
                    mLoadingView.setVisibility(VISIBLE);
                }

                if (status == STATUS_LOADING_AD_PAUSE){
                    mLoadingView.setPausePro(true);
                }else if (status == STATUS_LOADING_START_GAME){
                    mLoadingView.setPausePro(false);
                }else if(status == STATUS_LOADING_ERROR_STOP){
                    mLoadingView.setPausePro(true);
                    mLoadingView.updateMiniDialogView(true, msg,true);
                }else if(status == STATUS_GAME_RUNNING_ERROR){
                    mLoadingView.setPausePro(true);
                    mLoadingView.updateMiniDialogView(true, msg,false);
                }

                mLoadingView.updateLoadingText(msg);
                mLoadingView.setProgressStatus(status);
            }
            if (mErrorView != null && mErrorView.isShown()){
                mErrorView.setVisibility(GONE);
            }
        }

    }

    public void destory() {
        removeAllViews();
    }

    public static class Builder {

        private final Context context;
        private View loadingView;
        private View errorView;
        private View authView;
        private GameInfo gameInfo;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setLoadingView(View view){
            this.loadingView = view;
            return this;
        }

        public Builder setErrorView(View view){
            this.errorView = view;
            return this;
        }

        public Builder setAuthView(View view){
            this.authView = view;
            return this;
        }

        public Builder setGameInfo(GameInfo info){
            this.gameInfo = info;
            return this;
        }

        public PlayStatusLayout create(){

            if (loadingView == null){
                /*final LoadingPageView customerLoadignView = GameBoxManager.getInstance().getCustomerLoadingView();
                if (customerLoadignView != null){
                    loadingView = customerLoadignView;
                }else{
                    loadingView = new LoadingDefaultView(context);
                }*/
                loadingView = new GameMiniLoadingView(context);
            }
            if (errorView == null) {
                final PlayErrorPageView cusErrorView = GameBoxManager.getInstance().getCusErrorView();
                if (cusErrorView != null){
                    errorView = cusErrorView;
                }else {
                    errorView = new PlayErrorDefaultView(context);
                }
            }
            if (authView == null) {
                final PlayAuthPageView cusAuthView = GameBoxManager.getInstance().getCusAuthView();
                if (cusAuthView != null){
                    authView = cusAuthView;
                }else {
                    authView = new UserAuthView(context);
                }
            }

            loadingView.setId(viewid_loading);
            loadingView.setVisibility(VISIBLE);
            errorView.setId(viewid_error);
            errorView.setVisibility(GONE);
            authView.setId(viewid_auth);
            authView.setVisibility(GONE);

            PlayStatusLayout layout = new PlayStatusLayout(context);
            layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            layout.addView(loadingView);
            layout.addView(errorView);
            layout.addView(authView);
            layout.initView();
            layout.setGameInfo(gameInfo);
            return  layout;

        }
    }
}
