package kptech.game.kit.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.airbnb.lottie.LottieAnimationView;

import kptech.game.kit.GameInfo;
import kptech.game.kit.R;
import kptech.game.kit.utils.DensityUtil;

public class BdYonthLoadingView extends LoadingPageView {

    private LottieAnimationView mLoadingAnimationView;
    private LoadingSeekBar mLoadingSeekBar;
    private OnLoadingCallback mCallback;


    private LinearLayout.LayoutParams lottieViewParams;
    //竖屏的时候需要移动的总距离
    private int verTotalMoveDistance;
    //横屏的时候需要移动的总距离
    private int horTotalMoveDistance;
    //距离左侧距离
    private int viewLeftMargin;
    //当前是否是横屏
    private boolean currentIsLandspace;

    public BdYonthLoadingView(Context context) {
        super(context);
    }

    public BdYonthLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void inflateView() {
        View view = inflate(getContext(), R.layout.kp_bdyouth_loading, this);

        int screenWidth = DensityUtil.getScreenWidth(getContext());
        int screenHeight = DensityUtil.getScreenHeight(getContext());
        //视图的大小
        int viewSize = DensityUtil.dip2px(getContext(), 70);
        viewLeftMargin = DensityUtil.dip2px(getContext(),45);
        int seekBarLeftMargin = DensityUtil.dip2px(getContext(),90);
        //视图右侧距离屏幕左侧的距离
        int removedDistance = seekBarLeftMargin * 2 + viewSize/2;
        verTotalMoveDistance = screenWidth - removedDistance;
        horTotalMoveDistance = screenHeight - removedDistance;

        mLoadingAnimationView = view.findViewById(R.id.loading_animationView);
        mLoadingSeekBar = view.findViewById(R.id.loadingSeekbar);
        mLoadingSeekBar.setMax(pbMax);
        lottieViewParams = (LinearLayout.LayoutParams) mLoadingAnimationView.getLayoutParams();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void updateChildProgress(int progress) {

        mLoadingSeekBar.setProgress(progress);
        float value = (float) progress / (float) pbMax;
        float moveDistance;
        //移动距离
        if (currentIsLandspace){ //横屏
            moveDistance = horTotalMoveDistance * value;
        }else {//竖屏
            moveDistance = verTotalMoveDistance * value;
        }
        lottieViewParams.leftMargin = (int) (viewLeftMargin + moveDistance);
        mLoadingAnimationView.setLayoutParams(lottieViewParams);
    }


    @Override
    protected void updateChildText() {

    }

    @Override
    protected void setLoadingInfo(GameInfo gameInfo) {

    }

    @Override
    protected void onConfigChanged(Configuration newConfig) {
        currentIsLandspace = (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    public void updateLoadingText(String text) {

    }

    public void setOnLoadingCallback(OnLoadingCallback callback) {
        this.mCallback = callback;
    }

    public interface OnLoadingCallback {

        void onBackClick();
    }

}
