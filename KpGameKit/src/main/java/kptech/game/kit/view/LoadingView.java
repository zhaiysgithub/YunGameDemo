package kptech.game.kit.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import kptech.game.kit.R;

public class LoadingView extends FrameLayout {
    private ProgressBar mLoadingPb;
    private TextView mLoadingText;

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }
    private void initView() {
        inflate(getContext(), R.layout.view_loading, this);
    }


    private LoadingRoundImageView iv;
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        iv = findViewById(R.id.loading_dot);
//        iv.setDrawPoint(true);

//        iv.startAnimation();

        mLoadingText = (TextView) findViewById(R.id.loading_txt);
//        mLoadingPb = findViewById(R.id.loading_pb);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE){
            iv.startAnimation();
        }else {
            iv.stopAnimation();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
//        iv.startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        iv.stopAnimation();
    }

    public void setProgress(int mPro) {
//        mLoadingPb.setProgress(mPro);
    }

    public void setText(String text) {
        mLoadingText.setText(text);
    }
}
