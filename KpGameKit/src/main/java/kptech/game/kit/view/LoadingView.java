package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import kptech.game.kit.R;

public class LoadingView extends FrameLayout {
    private ProgressBar mLoadingPb;
    private TextView mLoadingText;
    private ImageView mIconImg;

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }
    private void initView() {
        inflate(getContext(), R.layout.kp_view_loading, this);
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
        mIconImg = findViewById(R.id.icon);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        try {
            if (visibility == VISIBLE){
                iv.startAnimation();
            }else {
                iv.stopAnimation();
            }
        }catch (Exception e){
            e.printStackTrace();
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
        try {
            iv.stopAnimation();
        }catch (Exception e){
            e.printStackTrace();
        }
        iv = null;
    }

    public void setProgress(int mPro) {
//        mLoadingPb.setProgress(mPro);
    }

    public void setText(String text) {
        mLoadingText.setText(text);
    }

    public void setIconImageResource(int res){
        mIconImg.setImageResource(res);
    }
}
