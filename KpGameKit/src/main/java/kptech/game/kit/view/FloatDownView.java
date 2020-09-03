package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import kptech.game.kit.GameDownloader;
import kptech.game.kit.R;

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
        inflate(getContext(), R.layout.view_float_down, this);
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
            case GameDownloader.STATUS_STARTED:
                mDownBtn.setProgress(0,"下载中...");
                break;
            case GameDownloader.STATUS_ERROR:
                mDownBtn.setProgress(0, "下载出错");
                break;
            case GameDownloader.STATUS_FINISHED:
                mDownBtn.setProgress(0, "下载完成");
                break;
            default:
                mDownBtn.setProgress(0, "边玩边下");
        }
    }
}
