package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import kptech.game.kit.GameInfo;


public abstract class PlayAuthPageView extends FrameLayout {

    public PlayAuthPageView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public PlayAuthPageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        removeAllViews();
        View authView = getAuthView();
        if (authView != null) {
            addView(authView);
        } else {
            int authViewId = getAuthViewId();
            if (authViewId > 0) {
                inflate(context, authViewId, this);
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        initView();
    }

    protected abstract void initView();

    protected abstract View getAuthView();

    protected abstract int getAuthViewId();

    public void setGameInfo(GameInfo gameInfo){

    }
}
