package kptech.game.kit.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

import kptech.game.kit.R;
import kptech.game.kit.activity.GamePlay;

public class FloatDownView extends FrameLayout {

    private FloatingDownBtn mDownBtn;
    public FloatDownView(Context context) {
        super(context);
        initView();
    }

    public FloatDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.kp_view_float_down, this);
        mDownBtn = findViewById(R.id.down_btn);
    }

    public void setOnDownListener(OnClickListener listener){
        if (mDownBtn!=null){
            mDownBtn.setOnDownListener(listener);
        }
    }

    public void setProgress(int num, String text){
        if (mDownBtn!=null){
            mDownBtn.setProgress(num, text);
        }
    }

    public void setDownloadStatus(int status){
        switch (status){
            case GamePlay.STATUS_STARTED:
                mDownBtn.setProgress(0,"下载中...");
                mDownBtn.setEnableTimeout(false);
                break;
            case GamePlay.STATUS_ERROR:
                mDownBtn.setProgress(0, "下载出错");
                mDownBtn.setEnableTimeout(false);
                break;
            case GamePlay.STATUS_FINISHED:
                mDownBtn.setProgress(0, "下载完成");
                mDownBtn.setEnableTimeout(false);
                break;
            default:
                mDownBtn.setProgress(0, "边玩边下");
                mDownBtn.setEnableTimeout(true);
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
    }

    public void startTimeoutLayout() {
        mDownBtn.startTimeout();
    }

//    public void enableView(){
//        if (visibility == VISIBLE){
//            mDownBtn.startTimeout();
//        }else {
//            mDownBtn.stopTimeout();
//        }
//    }
}
