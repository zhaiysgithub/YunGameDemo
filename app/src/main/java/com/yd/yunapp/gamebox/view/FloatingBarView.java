package com.yd.yunapp.gamebox.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.kuaipan.game.demo.R;

/**
 * Created by lxy on 18-5-21.
 */

public class FloatingBarView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "FloatingBarView";
    private static final boolean DEBUG = true;

    private int[] mThresValues;
    private int[] mTextColorResIds;

    private LinearLayout mFloatingView;
    private ImageView mIcon;
    private TextView mText;

    private int mLastPing;

    boolean isMove = false;
    private float[] mTemp = new float[]{
            0f, 0f
    };
    private float mSlop;

    private DrawingThread mDrawingThread;

    public FloatingBarView(@NonNull Context context) {
        super(context);
    }

    public FloatingBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        mFloatingView = (LinearLayout) inflate(getContext(), R.layout.gb_floating_view_layout, null);
        mIcon = (ImageView) mFloatingView.findViewById(R.id.floating_icon);
        mText = (TextView) mFloatingView.findViewById(R.id.floating_text);
        mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        mIcon.setVisibility(View.VISIBLE);
        getHolder().addCallback(this);
        setZOrderMediaOverlay(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    public void initIcons(int[] thresValues, int[] colorResIds) {
        if (thresValues == null || colorResIds == null) {
            throw new IllegalStateException("error init FloatingBarView");
        }

        int thresLen = thresValues.length;
        int stateLen = colorResIds.length;

        if (thresLen != stateLen - 1) {
            throw new IllegalStateException("error init FloatingBarView");
        }

        mThresValues = thresValues;
        mTextColorResIds = colorResIds;
    }

    public void onPingChanged(int ping) {
        if (ping != mLastPing) {
            mLastPing = ping;
            onChangedInternal();
        }
    }

    private void onChangedInternal() {
        for (int i = 0; i < mThresValues.length; i++) {
            if (mLastPing < mThresValues[i]) {
                updateView(mTextColorResIds[i]);
                return;
            }
        }

        updateView(mTextColorResIds[mTextColorResIds.length - 1]);
    }

    private void updateView(int color) {
        mText.setText(mLastPing + "ms");
        mText.setTextColor(getContext().getResources().getColor(color));
        mIcon.setImageResource(R.mipmap.gb_float_menu_icon);

        if (mDrawingThread != null) {
            mDrawingThread.draw();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int eventAction = event.getAction();
        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
                mTemp[0] = event.getX();
                mTemp[1] = event.getY();
                isMove = false;
                break;
            case MotionEvent.ACTION_UP:
                if (isMove) {
                    int viewX = (int) (getLeft() + getTranslationX() + event.getX());
                    int viewY = (int) (getTop() + getTranslationY() + event.getY());
                    int viewWidth = getWidth();
                    int viewHeight = getHeight();
                    int parentWidth = ((View) getParent()).getWidth();
                    int parentHeight = ((View) getParent()).getHeight();

                    int targetX = viewX - (int) event.getX();
                    int targetY = viewY - (int) event.getY();

                    int xDistance = Math.min(Math.abs(viewX - 0), Math.abs(parentWidth - viewX));
                    int yDistance = Math.min(Math.abs(viewY - 0), Math.abs(parentHeight - viewY));
                    if (xDistance < yDistance) {
                        if (viewX < parentWidth / 2) {
                            targetX = 0;
                        } else {
                            targetX = parentWidth - viewWidth;
                        }

                        if (targetY > parentHeight - viewHeight) {
                            targetY = parentHeight - viewHeight;
                        } else if (targetY < 0) {
                            targetY = 0;
                        }
                    } else {
                        if (viewY < parentHeight / 2) {
                            targetY = 0;
                        } else {
                            targetY = parentHeight - viewHeight;
                        }

                        if (targetX > parentWidth - viewWidth) {
                            targetX = parentWidth - viewWidth;
                        } else if (targetX < 0) {
                            targetX = 0;
                        }
                    }

                    setX(targetX);
                    setY(targetY);
                } else {
                    performClick();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isMove || !pointInView(this, event.getX(), event.getY())) {
                    isMove = true;
                    int diffX = (int) (event.getX() - mTemp[0]);
                    int diffY = (int) (event.getY() - mTemp[1]);
                    setTranslationX(diffX + getTranslationX());
                    setTranslationY(diffY + getTranslationY());
                }
                break;
            default:
                break;
        }
        return true;
    }

    private boolean pointInView(View view, float localX, float localY) {
        return localX >= -mSlop && localY >= -mSlop
                && localX < ((view.getRight() - view.getLeft()) + mSlop)
                && localY < ((view.getBottom() - view.getTop()) + mSlop);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mDrawingThread = new DrawingThread(holder);
        mDrawingThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mDrawingThread != null) {
            mDrawingThread.quit();
            mDrawingThread = null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // mFloatingView的大小不可变，务必使SurfaceView与mFloatingView大小保持一致！
        mFloatingView.measure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mFloatingView.getMeasuredWidth(), mFloatingView.getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mFloatingView.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    private class DrawingThread extends HandlerThread implements Handler.Callback {
        private static final int MSG_DRAW = 100;

        private SurfaceHolder mDrawingSurface;
        private volatile Handler mReceiver;

        public DrawingThread(SurfaceHolder holder) {
            super("DrawingThread");
            mDrawingSurface = holder;
        }

        @Override
        protected void onLooperPrepared() {
            mReceiver = new Handler(getLooper(), this);
            draw();
        }

        @Override
        public boolean quit() {
            // Clear all messages before dying
            mReceiver.removeCallbacksAndMessages(null);
            return super.quit();
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DRAW:
                    doDraw();
                    break;
            }
            return true;
        }

        private void doDraw() {

            Canvas canvas = null;
            try {
                canvas = mDrawingSurface.lockCanvas();
                if (canvas != null) {
                    Paint paint = new Paint();
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                    canvas.drawPaint(paint);
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

                    mFloatingView.draw(canvas);
                }
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            } finally {
                if (canvas != null) {
                    mDrawingSurface.unlockCanvasAndPost(canvas);
                }
            }
        }

        public void draw() {
            if (mReceiver != null) {
                mReceiver.sendEmptyMessage(MSG_DRAW);
            }
        }
    }
}
