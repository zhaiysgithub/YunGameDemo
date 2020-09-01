package kptech.game.kit.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import kptech.game.kit.R;

/**
 * Created by yyf on 2016/9/6.
 * <p>
 * public void drawCircle (float cx, float cy, float radius, Paint paint)
 * cx：圆心的x坐标。
 * cy：圆心的y坐标。
 * radius：圆的半径。
 * paint：绘制时所使用的画笔。
 */
public class LoadingRoundImageView extends View {
    private Paint circleRing;
    private String TAG = "RoundImageView";
    private float strokeWidth = 3;
    private float paddingWidth = 3;
    private float angle = 0;
    private int r = 0;//半径
    private float smallBallWidth = 5;
    private float width = 0;
    private ValueAnimator animator;
    private Bitmap bitmaps;  //中心图片
    private boolean isDrawPoint = false;

    public LoadingRoundImageView(Context context) {
        this(context, null);
    }

    public LoadingRoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingRoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray mTypeArray=context.obtainStyledAttributes(attrs,R.styleable.LoadingRoundImageView);
        width=mTypeArray.getDimension(R.styleable.LoadingRoundImageView_outer_circle_width,150);
        smallBallWidth = mTypeArray.getDimension(R.styleable.LoadingRoundImageView_dot_width,150);
        strokeWidth = mTypeArray.getDimension(R.styleable.LoadingRoundImageView_border_width,3);
        paddingWidth = smallBallWidth;
        initDrawable();
    }

    public void initDrawable() {
        circleRing = new Paint();
        bitmaps = BitmapFactory.decodeResource(getResources(), R.mipmap.loading_icon);
    }

    public void setImageResource(int resId) {
        bitmaps = BitmapFactory.decodeResource(getResources(), resId);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        circleRing.setColor(Color.parseColor("#e9e9e9"));
        circleRing.setStyle(Paint.Style.STROKE);
        circleRing.setAntiAlias(true);
        circleRing.setStrokeWidth(strokeWidth);

        /**********  中心图片  **********/
        r = (int) (width / 2);

//        canvas.drawBitmap(bitmaps, r / 2 + 5, r / 2 + 5, null);

        /*************  外层圆环  ***********/
        canvas.drawCircle(r + 5 + paddingWidth , r + 5 + paddingWidth, r, circleRing);

        /********** 外层点   *****************/
        circleRing.setColor(Color.parseColor("#ff2c55"));
        circleRing.setStyle(Paint.Style.FILL);
        circleRing.setStrokeWidth(strokeWidth);
        MathDisc(canvas);
    }

    /********
     * 计算圆弧点上的坐标
     * 公式:Math.sin(x)      x 的正玄值。返回值在 -1.0 到 1.0 之间；
     * Math.cos(x)    x 的余弦值。返回的是 -1.0 到 1.0 之间的数；
     * <p>
     * X指的是弧度  因此
     * <p>
     * 30° 角度 的弧度 = 2*PI/360*30
     * <p>
     * 圆上每个点的X坐标=a + Math.sin(2*Math.PI / 360) * r
     * Y坐标=b + Math.cos(2*Math.PI / 360) * r ；
     **************/
    private void MathDisc(Canvas canvas) { //angle 角度
        try {
            if (isDrawPoint) {
                float hudu = (float) ((2 * Math.PI / 360) * (angle));   //  360/8=45,即45度(这个随个人设置)
                float X = (float) (r + 5 + paddingWidth + Math.sin(hudu) * r);    //  r+5 是圆形中心的坐标X   即定位left 的值
                float Y = (float) (r + 5 + paddingWidth - Math.cos(hudu) * r);    //  r+5 是圆形中心的坐标Y   即定位top 的值
                canvas.drawCircle(X, Y, smallBallWidth, circleRing);
                canvas.restore();
            }
        }catch (Exception e){

        }

    }

    /***
     * 是否绘制小圆点
     */
    public boolean isDrawPoint() {
        return isDrawPoint;
    }

    public void setDrawPoint(boolean drawPoint) {
        isDrawPoint = drawPoint;
    }


    //属性动画
    public void startAnimation() {
        setDrawPoint(true);
        animator = ValueAnimator.ofFloat(0, 1.0f);
        //动画时长，让进度条在CountDown时间内正好从0-360走完，
        animator.setDuration(2000);
        animator.setInterpolator(new LinearInterpolator());//匀速
        animator.setRepeatCount(-1);//表示不循环，-1表示无限循环
        //值从0-1.0F 的动画，动画时长为countdownTime，ValueAnimator没有跟任何的控件相关联，那也正好说明ValueAnimator只是对值做动画运算，而不是针对控件的，我们需要监听ValueAnimator的动画过程来自己对控件做操作
        //添加监听器,监听动画过程中值的实时变化(animation.getAnimatedValue()得到的值就是0-1.0)
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                /**
                 * 这里我们已经知道ValueAnimator只是对值做动画运算，而不是针对控件的，因为我们设置的区间值为0-1.0f
                 * 所以animation.getAnimatedValue()得到的值也是在[0.0-1.0]区间，而我们在画进度条弧度时，设置的当前角度为360*currentAngle，
                 * 因此，当我们的区间值变为1.0的时候弧度刚好转了360度
                 */
                angle = 360 * (float) animation.getAnimatedValue();
                invalidate();//实时刷新view，这样我们的进度条弧度就动起来了
            }
        });
        //开启动画
        animator.start();
    }

    public void stopAnimation() {
        animator.cancel();
        animator = null;
    }
}