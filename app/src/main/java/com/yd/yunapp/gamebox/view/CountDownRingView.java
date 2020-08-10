package com.yd.yunapp.gamebox.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.kuaipan.game.demo.R;

import java.util.Timer;
import java.util.TimerTask;


@SuppressLint("AppCompatCustomView")
public class CountDownRingView extends TextView {

    private Timer mTimer;
    private Paint mRingPaint;
    private RectF mRingRect;
    private Paint mBackgroundPaint;
    private ValueAnimator mValueAnimator;

    private float mAngel = 0f;
    private volatile int mCountDownSecond = 0;
    private int mRingColor = 0xFFFFFF;
    private int mRingWidth = 3;
    private int mBackgroundColor = -1;
    private int mCenterX;
    private int mCenterY;
    private float mBackgroundRadius;

    public CountDownRingView(Context context) {
        super(context);
        init();
    }

    public CountDownRingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CountDownRingView);
        mRingColor = a.getColor(R.styleable.CountDownRingView_circleColor, 0xFFFFFF);
        mRingWidth = a.getDimensionPixelOffset(R.styleable.CountDownRingView_circleWidth, 3);
        mBackgroundColor = a.getColor(R.styleable.CountDownRingView_backgroundColor, -1);
        a.recycle();
        init();
    }


    private void init() {
        mRingPaint = new Paint();
        mRingPaint.setColor(mRingColor);
        mRingPaint.setAntiAlias(true);
        mRingPaint.setStrokeWidth(mRingWidth);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingRect = new RectF();
        if (mBackgroundColor != -1) {
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(mBackgroundColor);
            mBackgroundPaint.setAntiAlias(true);
            mBackgroundPaint.setStyle(Paint.Style.FILL);
        }
    }

    public void setCount(int second) {
        mCountDownSecond = second;
    }

    public void start() {
        mAngel = 360f;
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
        }

        mValueAnimator = ValueAnimator.ofFloat(360f, 0f);
        mValueAnimator.setDuration(mCountDownSecond * 1000);
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAngel = (Float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mValueAnimator.start();

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCountDownSecond > 0) {
                            setText(mCountDownSecond + "S");
                            mCountDownSecond--;
                            if (mCountDownSecond == 0) {
                                mTimer.cancel();
                            }
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    public void cancelCount() {
        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
            mTimer.cancel();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int halfCircleWidth = mRingWidth >> 1;
        int left = getPaddingLeft() + halfCircleWidth;
        int top = getPaddingTop() + halfCircleWidth;
        int right = getMeasuredWidth() - getPaddingRight() - halfCircleWidth;
        int bottom = getMeasuredHeight() - getPaddingBottom() - halfCircleWidth;
        mRingRect.set(left, top, right, bottom);
        mCenterX = getMeasuredWidth() >> 1;
        mCenterY = getMeasuredHeight() >> 1;
        mBackgroundRadius = mCenterX - getPaddingLeft();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBackgroundPaint != null) {
            canvas.drawCircle(mCenterX, mCenterY, mBackgroundRadius, mBackgroundPaint);
        }
        super.onDraw(canvas);
        canvas.drawArc(mRingRect, 270 - mAngel, mAngel, false, mRingPaint);
    }
}
