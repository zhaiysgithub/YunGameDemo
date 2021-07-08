package kptech.game.kit.view;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import kptech.game.kit.GameInfo;
import kptech.game.kit.utils.Logger;


public abstract class LoadingPageView extends FrameLayout {

    public static final int pbMax = 10000;
    protected static final int WHAT_UPDATE_PROGRESS = 0;
    protected static final int WHAT_UPDATE_Text = 1;
    private Handler mHandler;
    private boolean mPausePro;
    private int mPro = 0;
    public int mIconResId = 0;

    public LoadingPageView(@NonNull Context context) {
        super(context);
        initView();
    }

    public LoadingPageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LoadingPageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    private void initView() {
        mIconResId = 0;
        mHandler = new LoadingHandler(this);
        inflateView();
    }

    public void setPausePro(boolean pausePro) {
        this.mPausePro = pausePro;
    }

    protected abstract void inflateView();

    protected abstract void updateChildProgress(int progress);

    protected void updateChildText(){}

    protected abstract void setLoadingInfo(GameInfo gameInfo);

    protected abstract void onConfigChanged(Configuration newConfig);

    protected void updateLoadingText(String msg){

    }

    /**
     * 设置本地的图片资源
     */
    public void setLocalResIcon(int resId){
        this.mIconResId = resId;
    }

    private void updateText(){
        try {
            updateChildText();
            if (isShown()){
                mHandler.sendEmptyMessageDelayed(1, 4000);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateProgress(int progress){
        updateChildProgress(progress);

        //延时更新进度
        if (!mPausePro && mPro < pbMax){
            if (mPro < (int) (pbMax * 0.85) && mPro >= (int) (pbMax * 0.70)){
                mHandler.sendMessageDelayed(Message.obtain(mHandler, 0, (int)(mPro + ((pbMax - mPro) * 0.05))), 150);
            }else if (mPro < (int) (pbMax * 0.95) && mPro >= (int) (pbMax * 0.85)){
                mHandler.sendMessageDelayed(Message.obtain(mHandler, 0, (int)(mPro + ((pbMax - mPro) * 0.1))), 3000);
            }else if (mPro >= (int) (pbMax * 0.95)){
                mHandler.sendMessageDelayed(Message.obtain(mHandler, 0, (int)(mPro + 50)), 3000);
            }else {
                //计算延时时间
                mHandler.sendEmptyMessageDelayed(0, 100);
            }

        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == GONE) {
            mHandler.removeMessages(WHAT_UPDATE_PROGRESS);
            mHandler.removeMessages(WHAT_UPDATE_Text);
            mPro = 0;
            mPausePro = true;
            updateProgress(0);
        } else {
            mPausePro = false;
            mHandler.sendEmptyMessageDelayed(1, 4000);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onConfigChanged(newConfig);
    }

    public void setProgressStatus(int status){
        Logger.info("LoadingView", "status: "+status);
        int pro = mPro;
        switch (status){
            case PlayStatusLayout.STATUS_LOADING:
                pro = 0;
                break;
            case PlayStatusLayout.STATUS_LOADING_INIT:
                pro = (int) (pbMax * 0.01);
                break;
//            case PlayStatusLayout.STATUS_LOADING_GET_GAMEINFO:
//                pro = (int) (pbMax * 0.10);
//                break;
            case PlayStatusLayout.STATUS_LOADING_CONNECT_DEVICE:
                pro = (int) (pbMax * 0.15);
                break;
            case PlayStatusLayout.STATUS_LOADING_RECOVER_GAMEINFO:
                pro = (int) (pbMax * 0.30);
                break;
            case PlayStatusLayout.STATUS_LOADING_AD_PAUSE:
            case PlayStatusLayout.STATUS_LOADING_START_GAME:
                pro = (int) (pbMax * 0.60);
                break;
//            case PlayStatusLayout.STATUS_LOADING_START_GAME:
//                pro = (int) (pbMax * 0.70);
//                break;
            case PlayStatusLayout.STATUS_LOADING_FINISHED:
                pro = (int) (pbMax * 0.98);
                break;
        }
        mPro = pro;
        mHandler.sendMessage(Message.obtain(mHandler, 0, pro));

    }



    private static class LoadingHandler extends Handler {

        private int sPro = 0;
        private WeakReference<LoadingPageView> ref;

        private LoadingHandler(LoadingPageView view) {
            super(Looper.getMainLooper());
            ref = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (ref == null || ref.get() == null) {
                return;
            }

            if (!ref.get().isShown()) {
                return;
            }
            int what = msg.what;
            if (what == 0) {
                if (msg.obj != null) {
                    int obj = (int) msg.obj;
                    if (obj > sPro) {
                        sPro = obj;
                    }
                } else {
                    int last = pbMax - sPro; //剩余长度

                    int in;
                    if (last < 200) {
                        in = 0;
                    } else if (last < 1000) {
                        in = 2;
                    } else if (last < 3000) {
                        in = 20;
                    } else {
                        in = last / 100;
                    }
                    sPro += in;
                }

                if (sPro > pbMax) {
                    sPro = pbMax;
                }
                ref.get().updateProgress(sPro);
            } else if (msg.what == 1) {
                ref.get().updateText();
            }
        }
    }

}
