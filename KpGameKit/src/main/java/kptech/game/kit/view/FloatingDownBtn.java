package kptech.game.kit.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

import kptech.game.kit.R;
import kptech.game.kit.manager.KpGameDownloadManger;
import kptech.game.kit.utils.DensityUtil;


/**
 * Created by lxy on 18-5-21.
 */

public class FloatingDownBtn extends FrameLayout {

    boolean isMove = false;
    private float[] mTemp = new float[]{
            0f, 0f
    };
    private float mSlop;

    private ProgressBar mProgressBar;
    private TextView mTextView;
    private ImageView mSmallImg;

    private OnClickListener mListener;
    //下载状态
    private int mDownStatus;

    public FloatingDownBtn(Context context) {
        super(context);
    }

    public FloatingDownBtn(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        View view = inflate(getContext(), R.layout.kp_view_floating_downbtn, this);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mSmallImg.isShown()){
                    //显示大图
                    toggleLayout();
                    return;
                }

                if (mListener != null){
                    mListener.onClick(view);
                }

//                Toast.makeText(getContext(), "下载", Toast.LENGTH_SHORT).show();
//
//                mHandler.sendEmptyMessageDelayed(1, 1000);
            }
        });

        mTextView = view.findViewById(R.id.floating_text);
        mProgressBar = view.findViewById(R.id.pb_progressbar);
        mSmallImg = view.findViewById(R.id.down_img);
    }

    public void toggleLayout(){
        LayoutParams lp = (LayoutParams) getLayoutParams();
        int preWidth = lp.width;
        float targetX = getX();

        if (mSmallImg.isShown()){
            lastOptTime = System.currentTimeMillis();
            startTimeout();

            //显示大按钮
            mSmallImg.setVisibility(GONE);
            mTextView.setVisibility(VISIBLE);
            mProgressBar.setVisibility(VISIBLE);
            lp.width = DensityUtil.dip2px(getContext(),90);
            lp.height = DensityUtil.dip2px(getContext(),30);
        }else {
            if (mDownStatus <= KpGameDownloadManger.STATE_STARTED){
                return;
            }
            stopTimeout();

            //显示小按钮
            mSmallImg.setVisibility(VISIBLE);
            mTextView.setVisibility(GONE);
            mProgressBar.setVisibility(GONE);
            lp.width = DensityUtil.dip2px(getContext(),30);
            lp.height = DensityUtil.dip2px(getContext(),30);
        }

        FloatingDownBtn.this.setLayoutParams(lp);

        if (targetX < (((View) getParent()).getWidth() / 2)){
            targetX += lp.width - preWidth;
            setX(targetX);
        }
    }

    public synchronized void startTimeout(){
        if (mTimeHandler!=null && mTimeHandler.hasMessages(1)){
            return;
        }

        if (mTimeHandler==null) {
            mTimeHandler = new MyHandler(this);
        }

        lastOptTime = System.currentTimeMillis() - 300;
        mTimeHandler.sendEmptyMessageDelayed(1, 15 * 1000);
    }

    public synchronized void stopTimeout(){
        if (mTimeHandler!=null){
            mTimeHandler.removeMessages(1);
            mTimeHandler = null;
        }
    }

    public void setOnDownListener(OnClickListener listener){
        mListener = listener;
    }

    public void setProgress(int num, String text){
        mProgressBar.setProgress(num);
        mTextView.setText(text);
    }

    public void setDownloadStatus(int status){
        this.mDownStatus = status;
    }

    private boolean mEnableTimeout = true;
    public void setEnableTimeout(boolean enableTimeout){
        if (mEnableTimeout == enableTimeout) {
            return;
        }

        this.mEnableTimeout = enableTimeout;
        if (this.mEnableTimeout){
            startTimeout();
        }else {
            stopTimeout();
        }
    }

    private MyHandler mTimeHandler;
    private long lastOptTime = 0;
    private class MyHandler extends Handler {
        private WeakReference<FloatingDownBtn> ref;

        protected MyHandler(FloatingDownBtn obj) {
            super(Looper.getMainLooper());
            ref = new WeakReference<>(obj);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            if (ref == null || ref.get() == null || !ref.get().isShown()){
                return;
            }

            if (!ref.get().mEnableTimeout){
                return;
            }

            long timeout = System.currentTimeMillis() - lastOptTime;
            if (timeout > 15 * 1000) {
                ref.get().toggleLayout();
            }else{
                if (ref.get().mTimeHandler!=null){
                    ref.get().mTimeHandler.sendEmptyMessageDelayed(1, 15 * 1000 - timeout);
                }
            }
        }
    }

//    private int pro = 0;
//    private Handler mHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            ++pro;
//            if (pro > 100){
//                pro = 100;
//            }
//            mProgressBar.setProgress(pro);
//
//            if (pro < 100){
//                mHandler.sendEmptyMessageDelayed(1, 300);
//            }
//        }
//    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        lastOptTime = System.currentTimeMillis();
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


}
