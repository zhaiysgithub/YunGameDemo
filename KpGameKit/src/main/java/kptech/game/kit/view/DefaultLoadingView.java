package kptech.game.kit.view;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

import kptech.game.kit.GameInfo;
import kptech.game.kit.R;
import kptech.game.kit.utils.DensityUtil;
import kptech.game.kit.utils.Logger;

public class DefaultLoadingView extends LoadingPageView {

    private ProgressBar mLoadingPb;
    private TextView mLoadingText;
    private ImageView mIconImg;
    private TextView mNameText;
    private ViewGroup mBottomLL;

    private int textPos = 0;
    private static final String[] bottomTextArr = new String[]{
            "提示：请关闭手机旋转设置，体验会更好",
            "提示：游戏加载不消耗流量哦",
            "提示：网络延迟过高，请切换手机网络",
    };


    public DefaultLoadingView(Context context) {
        super(context);
    }

    public DefaultLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void inflateView() {
        View view = inflate(getContext(), R.layout.kp_view_default_loading, this);

        mIconImg = view.findViewById(R.id.game_icon);
        mNameText = view.findViewById(R.id.game_name);
        mLoadingText = view.findViewById(R.id.loading_txt);
        mLoadingPb = view.findViewById(R.id.loading_pb);
        mLoadingPb.setMax(pbMax);
        mBottomLL = view.findViewById(R.id.bottom_ll);
    }

    @Override
    protected void updateChildProgress(int progress) {
        mLoadingPb.setProgress(progress);
    }

    @Override
    protected void updateChildText() {
        int arrLength = bottomTextArr.length;
        int pos = (textPos + 1) % arrLength;
        mLoadingText.setText(bottomTextArr[pos]);
        textPos = pos;
    }

    @Override
    protected void setLoadingInfo(GameInfo gameInfo) {
        mNameText.setText(gameInfo.name);
        try {
            if (mIconResId > 0){
                //显示本地icon
                Picasso.with(getContext()).load(mIconResId).into(mIconImg);
            }else {
                String iconUrl = gameInfo.iconUrl;
                if (iconUrl != null && !iconUrl.isEmpty()) {
                    Picasso.with(getContext()).load(iconUrl).into(mIconImg);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onConfigChanged(Configuration newConfig) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mBottomLL.getLayoutParams();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            lp.bottomMargin = DensityUtil.dip2px(getContext(), 20);
        }else {
            lp.bottomMargin = DensityUtil.dip2px(getContext(), 94);
        }
        mBottomLL.setLayoutParams(lp);
    }

    public void updateLoadingText(String text) {
        mLoadingText.setText(text);
    }

}
