package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;


import kptech.game.kit.R;
import kptech.game.kit.manager.KpGameDownloadManger;

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
            mDownBtn.setProgress(num, "下载" + text);
        }
    }

    public void setDownloadStatus(int status){
        switch (status){
            case KpGameDownloadManger.STATE_STARTED:
//                mDownBtn.setProgress(0,"下载中...");
//                mDownBtn.setEnableTimeout(false);
                break;
            case KpGameDownloadManger.STATE_ERROR:
                mDownBtn.setProgress(0, "下载出错");
                mDownBtn.setEnableTimeout(false);
                break;
            case KpGameDownloadManger.STATE_FINISHED:
                mDownBtn.setProgress(0, "立即安装");
                mDownBtn.setEnableTimeout(false);
                break;
            case KpGameDownloadManger.STATE_STOPPED:
//                mDownBtn.setProgress(0, "已暂停");
//                mDownBtn.setEnableTimeout(false);
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
