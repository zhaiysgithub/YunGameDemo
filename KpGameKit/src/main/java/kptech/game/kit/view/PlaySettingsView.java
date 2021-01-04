package kptech.game.kit.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import kptech.game.kit.R;
import kptech.game.kit.utils.DensityUtil;

public class PlaySettingsView extends LinearLayout {

    private ViewGroup mLayout;
    public PlaySettingsView(Context context) {
        super(context);
        initView();
    }

    public PlaySettingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        View view = inflate(getContext(), R.layout.kp_view_play_settings, this);
        mLayout = view.findViewById(R.id.layout);
        updateLayout();
    }

    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        updateLayout();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLayout();
    }

    private int curOrientation = 0;
    private void updateLayout(){
        if (curOrientation == this.getResources().getConfiguration().orientation){
            return;
        }
        curOrientation = this.getResources().getConfiguration().orientation;


        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            //横屏
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(DensityUtil.dip2px(getContext(), 300), ViewGroup.LayoutParams.MATCH_PARENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            mLayout.setLayoutParams(lp);
        }
        else if (this.getResources().getConfiguration().orientation ==Configuration.ORIENTATION_PORTRAIT) {
            //竖屏
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(getContext(), 300));
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            mLayout.setLayoutParams(lp);
        }
    }
}
