package kptech.game.kit.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import kptech.game.kit.GameInfo;
import kptech.game.kit.R;
import kptech.game.kit.manager.FastRepeatClickManager;
import kptech.game.kit.utils.DensityUtil;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.StringUtil;

/**
 * 微包定制的loading （横屏定制）
 */
public class GameMiniLoadingView extends LoadingPageView{

    private ImageView miniProTitle;
    private LoadingSeekBar miniLoadSeekbar;
    private TextView miniProValue;
    private TextView miniLoadDesc;
    private LinearLayout miniLayoutLoadError;
    private RelativeLayout mLayoutMiniProgress;
    private ProgressBar miniProDown;
    private TextView miniTvDown;
    private FrameLayout miniLayoutDown;
//    private Drawable miniThumbDefault,miniThumbError;
    private Drawable miniProDrawableDefault,miniProDrawableError;
    private RelativeLayout.LayoutParams miniProValueParams;
    private int pxSeekbarWidth,pxProTextMarginRight;
    private OnMiniLoadListener miniListener;
    //错误信息
    private String mErrorMsg;


    public GameMiniLoadingView(@NonNull Context context) {
        super(context);
    }

    public GameMiniLoadingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void inflateView() {
        View view = inflate(getContext(), R.layout.view_gamemini_loading, this);
        miniProTitle = view.findViewById(R.id.ivMiniProTitle);
        miniLoadSeekbar = view.findViewById(R.id.seekbarMiniLoading);
        miniProValue = view.findViewById(R.id.tvMiniProValue);
        miniLoadDesc = view.findViewById(R.id.tvMiniLoadDesc);
        miniLayoutLoadError = view.findViewById(R.id.layoutMiniLoadError);
        mLayoutMiniProgress = view.findViewById(R.id.layoutMiniProgress);
        miniLayoutDown = view.findViewById(R.id.layoutMiniDown);
        miniProDown = view.findViewById(R.id.proMiniDown);
        miniTvDown = view.findViewById(R.id.tvMiniDown);
        TextView miniTvRetry = view.findViewById(R.id.tvMiniRetry);

        miniLoadSeekbar.setMax(pbMax);
        miniLayoutLoadError.setVisibility(View.GONE);
        pxSeekbarWidth = DensityUtil.dip2px(getContext(),320);
        pxProTextMarginRight = DensityUtil.dip2px(getContext(),32);
        miniProValueParams = (RelativeLayout.LayoutParams) miniProValue.getLayoutParams();

//        miniThumbDefault = ContextCompat.getDrawable(getContext(),R.mipmap.mini_ic_load_rock_run);
//        miniThumbError = ContextCompat.getDrawable(getContext(),R.mipmap.mini_ic_load_rock_error);
        miniProDrawableDefault = ContextCompat.getDrawable(getContext(),R.drawable.mini_seekbar_loading_running);
        miniProDrawableError = ContextCompat.getDrawable(getContext(),R.drawable.mini_seekbar_loading_error);

        miniLayoutDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastRepeatClickManager.getInstance().isFastDoubleClick(v.getId())){
                    return;
                }
                //全速下载
                if (miniListener != null){
                    miniListener.onDownloadClick(mErrorMsg);
                }
            }
        });

        miniTvRetry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastRepeatClickManager.getInstance().isFastDoubleClick(v.getId())){
                    return;
                }
                setPausePro(false);
                //重试
                if (miniListener != null){
                    miniListener.onGameRetryClick();
                }
            }
        });
    }

    /**
     * 更新view
     * @param isError 是否发生了错误
     * @param errorMsg 错误信息
     * @param showProgress 是否显示进度条
     */
    public void updateMiniDialogView(boolean isError,String errorMsg, boolean showProgress){
        mLayoutMiniProgress.setVisibility(showProgress ? View.VISIBLE : View.GONE);
        if (!isError){
            miniLayoutLoadError.setVisibility(View.GONE);
            miniProTitle.setImageResource(R.mipmap.mini_ic_load_entering);
//            miniLoadSeekbar.setThumb(miniThumbDefault);
            miniLoadSeekbar.setProgressDrawable(miniProDrawableDefault);
            miniLoadDesc.setText("游戏启动中，请耐心等待～");
        }else {
            this.mErrorMsg = errorMsg;
            miniLayoutLoadError.setVisibility(View.VISIBLE);
            miniProTitle.setImageResource(R.mipmap.mini_ic_load_retry);
//            miniLoadSeekbar.setThumb(miniThumbError);
            miniLoadSeekbar.setProgressDrawable(miniProDrawableError);
            if (showProgress){
                miniLoadDesc.setText("游戏加载失败，再给一次机会嘛，拜托拜托～");
            }else {
                miniLoadDesc.setText(errorMsg);
            }
        }
    }

    /**
     * 更新下载的进度
     */
    public void setDownloadProgress(int progress,String text){
        miniTvDown.setText(text);
        miniProDown.setProgress(progress);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void updateChildProgress(int progress) {

        miniLoadSeekbar.setProgress(progress);
        int showValue = progress * 100 / pbMax;
        miniProValue.setText(showValue + "%");

        float proPercent = (float)progress / (float)pbMax;
        int curProWidth = (int) (pxSeekbarWidth * proPercent);
        miniProValueParams.leftMargin = curProWidth - pxProTextMarginRight;
        miniProValue.setLayoutParams(miniProValueParams);
    }

    @Override
    protected void setLoadingInfo(GameInfo gameInfo) {
        if (gameInfo == null){
            return;
        }
        String downloadUrl = gameInfo.downloadUrl;
        int enableDownload = gameInfo.enableDownload;
        if (enableDownload == 1 && !StringUtil.isEmpty(downloadUrl)){
            miniLayoutDown.setVisibility(View.VISIBLE);
        }else {
            miniLayoutDown.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onConfigChanged(Configuration newConfig) {

    }

    public void setOnMiniLoadListener(OnMiniLoadListener listener){
        this.miniListener = listener;
    }


    public interface OnMiniLoadListener{

        void onDownloadClick(String msg);

        void onGameRetryClick();
    }
}
