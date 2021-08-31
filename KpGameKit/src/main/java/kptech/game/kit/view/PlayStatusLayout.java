package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import kptech.game.kit.APIConstants;
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
    public static final int STATUS_LOADING_FINISHED = 108;

    private BdYonthLoadingView mLoadingView;
    private BdYonthErrorView mErrorView;
    private BdYonthAuthView mAuthView;

    private String iconUrl;
    private String downUrl;
    private String pkgName;
    private String gameName;
    private int mErrorCode;

    public void setDownloadStatus(int status) {
        /*if (mErrorView != null){
            mErrorView.setDownloadStatus(status);
        }*/
    }

    public void setProgress(int progress, String text) {
        /*if (mErrorView != null){
            mErrorView.setProgress(progress, text);
        }*/
    }

    public interface ICallback{
        void onClickFinish();
        void onClickReloadGame();
        void onClickDownloading();
        void onClickAuthPass();
        void onClickAuthReject();
        void onClickCopyInf();
    }

    private ICallback mCallback;
    public void setCallback(ICallback callback){
        this.mCallback = callback;
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
//        this.mGameInfo = info;
        if (info != null){
            iconUrl = info.iconUrl;
            downUrl = info.enableDownload == 1 ? info.downloadUrl : null;
            gameName = info.name;
            pkgName = info.pkgName;

            if (mLoadingView != null){
                mLoadingView.setLoadingInfo(info);
            }
            if (mErrorView != null){
                mErrorView.setGameInfo(info);
            }
            if (mAuthView != null){
                mAuthView.setGameInfo(iconUrl, gameName);
            }
        }
    }

    private void initView() {

        mLoadingView = findViewById(viewid_loading);
        mErrorView = findViewById(viewid_error);
        mAuthView = findViewById(viewid_auth);

        mLoadingView.setOnLoadingCallback(new BdYonthLoadingView.OnLoadingCallback() {
            @Override
            public void onBackClick() {
                if (mCallback != null){
                    mCallback.onClickFinish();
                }
            }
        });

        mErrorView.setOnErrorCallback(new BdYonthErrorView.OnErrorCallback() {
            @Override
            public void onBackClick() {
                if (mCallback != null){
                    mCallback.onClickFinish();
                }
            }

            @Override
            public void onReloadGame() {
                if (mCallback != null){
                    mCallback.onClickReloadGame();
                }
            }

            @Override
            public void onDownloadGame() {
                if (mCallback != null){
                    mCallback.onClickDownloading();
                }
            }

            @Override
            public void onCopyDeviceInf() {
                if (mCallback != null){
                    mCallback.onClickCopyInf();
                }
            }
        });

        mAuthView.setOnAuthCallback(new BdYonthAuthView.OnAuthCallback() {
            @Override
            public void onBackListener() {
                if (mCallback != null){
                    mCallback.onClickAuthReject();
                }
            }

            @Override
            public void onAuthListener() {
                if(mCallback != null){
                    mCallback.onClickAuthPass();
                }
                hideUserAuthView();
            }
        });

        mAuthView.onFinishInflate();
        mErrorView.onFinishInflate();
        mLoadingView.onFinishInflate();
    }

//    private String mUnionUUID;
//    private String mCorpID;
    /**
     * 显示授权界面
     */
    public void showUserAuthView(String unionUUID, String corpId){
        try {
            mLoadingView.setPausePro(true);

            if (mAuthView.getVisibility() == View.VISIBLE){
                return;
            }

//            mUnionUUID = unionUUID;
//            mCorpID = corpId;

            mAuthView.setGameInfo(iconUrl, gameName);
            mAuthView.setAnimation(AnimationUtil.moveToViewLocation());
            mAuthView.setVisibility(View.VISIBLE);

            try {
                //发送打点事件
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_ACTIVITY_USERAUTH_DISPLAY, pkgName ));
            }catch (Exception e){
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

    public void setErrorCode(int errorCode){
        this.mErrorCode = errorCode;
    }

    public void setStatus(int status, String msg) {
        if (status  == STATUS_ERROR){
            if (mErrorView != null){
                if (!mErrorView.isShown()){
                    mErrorView.setVisibility(VISIBLE);
                }
                int errorStatus = BdYonthErrorView.STATUS_LOAD_ERROR;
                if (mErrorCode == APIConstants.ERROR_NO_DEVICE){
                    errorStatus = BdYonthErrorView.STATUS_NO_DEVICE;
                }else if(mErrorCode == APIConstants.ERROR_API_CALL_ERROR){
                    errorStatus = BdYonthErrorView.STATUS_PAGE_ERROR;
                }else if(mErrorCode == APIConstants.ERROR_DEVICE_EXPIRED ||
                        mErrorCode == APIConstants.ERROR_DEVICE_OTHER_ERROR){
                    errorStatus = BdYonthErrorView.STATUS_DEVICE_OFFLINE;
                }
                mErrorView.updateErrorStatus(errorStatus,msg);
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

        private Context context;
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
                loadingView = new BdYonthLoadingView(context);
            }
            if (errorView == null) {
                errorView = new BdYonthErrorView(context);
            }
            if (authView == null) {
                authView = new BdYonthAuthView(context);
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
