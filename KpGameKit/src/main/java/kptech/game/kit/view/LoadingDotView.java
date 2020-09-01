package kptech.game.kit.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import kptech.game.kit.R;

public class LoadingDotView extends RelativeLayout {


    public LoadingDotView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void initView() {
        inflate(getContext(), R.layout.view_loading, this);
        initProgressDots();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

    }


    //用于循环动画的handler
    public Handler animaLoopHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            if(ANIMA_LOOP == msg.what){
//                hasOneFinishCount = 0;
//                hasTwoFinishCount = 0;
//                startRotationAnimOneSet();
//                //Log.d(TAG, "1handleMessage() called with: msg = [" + msg + "]");
//            }
        }
    };

    /**
     * 初始化小圆点
     */
    private void initProgressDots() {
        removeAllViews();
        ImageView iv = new ImageView(getContext());
        iv.setBackgroundResource(R.drawable.loading_dot);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        addView(iv,params);

    }

    /**
     * 开启小圆点旋转动画1啊
     */
    private void startRotationAnimOne(final ImageView iv, final float fromDegrees, final float toDegrees) {
        //以View的父控件中心点作为旋转轴，创建旋转度的动画
        int pivotXType = Animation.ABSOLUTE;
        float pivotXValue = 0;
        int pivotYType = Animation.ABSOLUTE;
        float pivotYValue = 15;//父控件高度的一半
        Animation animation = new RotateAnimation(
                fromDegrees, toDegrees,
                pivotXType, pivotXValue,
                pivotYType, pivotYValue
        );
        //设置动画持续时间
        animation.setDuration(1000);
        //animation.setFillAfter(true);
        //animation.setRepeatCount(10);
        //通过View的startAnimation方法将动画立即应用到View上
        iv.startAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //startRotationAnimTwo(iv, toDegrees, 360);
//                hasOneFinishCount++;
//                if(DOT_COUNT == hasOneFinishCount){
//                    startRotationAnimTwoSet();
//                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

}
