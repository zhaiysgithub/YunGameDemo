package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import kptech.game.kit.GameInfo;

public abstract class PlayErrorPageView extends FrameLayout {

    protected Context mContext;
    private View mRootView;

    public PlayErrorPageView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public PlayErrorPageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView(mRootView);
    }

    private void init(Context context) {
        this.mContext = context;
        removeAllViews();
        try{
            View errorView = getErrorView();
            if (errorView != null){
                mRootView = errorView;
            }else {
                int errorLayoutRes = getErrorLayoutRes();
                mRootView = inflate(context, errorLayoutRes, this);
            }
            addView(errorView);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    protected abstract void initView(View view);

    protected abstract View getErrorView();

    protected abstract int getErrorLayoutRes();

    protected void setGameInfo(GameInfo gameInfo){}

    protected void setErrorText(String error){}

    protected void setProgress(int num, String text){}

    protected void setDownloadStatus(int status){}


}
