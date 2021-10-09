package kptech.game.kit.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kptech.kputils.x;

import kptech.game.kit.GameInfo;
import kptech.game.kit.R;
import kptech.game.kit.utils.DensityUtil;

public class XiaoYuLoadingPage extends LoadingPageView {

    private RoundImageView mRoundIcon;
    private TextView mTvGameName;
    private TextView mTvProValue;
    private LoadingSeekBar mLoadingSeekbar;
    private TextView mLoadingText;

    private int textPos = 0;
    private final String[] textArr = new String[]{
            "提示：请关闭手机旋转设置，体验会更好",
            "提示：游戏加载不消耗流量哦",
            "提示：网络延迟过高，请切换手机网络",
    };

    public XiaoYuLoadingPage(@NonNull Context context) {
        super(context);
    }

    public XiaoYuLoadingPage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public XiaoYuLoadingPage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void inflateView() {
        View view = inflate(getContext(), R.layout.view_loading_xiaoyu, this);
        TextView mLoadingTitle = view.findViewById(R.id.tvLoadingTitle);
        mRoundIcon = view.findViewById(R.id.iconGame);
        mTvGameName = view.findViewById(R.id.tvGameName);
        mTvProValue = view.findViewById(R.id.tvProgressValue);
        mLoadingSeekbar = view.findViewById(R.id.loadingSeekbar);
        mLoadingText = view.findViewById(R.id.tvLoadingText);
        mLoadingTitle.setText("游戏试玩");
        mLoadingSeekbar.setMax(pbMax);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void updateChildProgress(int progress) {
        mLoadingSeekbar.setProgress(progress);
        int value = progress * 100 / pbMax;
        mTvProValue.setText(value + "%");
    }

    @Override
    protected void updateChildText() {
        /*int arrLength = textArr.length;
        int pos = (textPos + 1) % arrLength;
        mLoadingText.setText(textArr[pos]);
        textPos = pos;*/
    }

    @Override
    protected void setLoadingInfo(GameInfo gameInfo) {
        try {
            mTvGameName.setText(gameInfo.name);
            String iconUrl = gameInfo.iconUrl;
            int localResId = gameInfo.localResId;
            if (localResId > 0){
                mRoundIcon.setImageResource(localResId);
            }else {
                if (iconUrl != null && !iconUrl.isEmpty()) {
                    x.image().bind(mRoundIcon,iconUrl);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onConfigChanged(Configuration newConfig) {

        LinearLayout.LayoutParams roundIconLp = (LinearLayout.LayoutParams) mRoundIcon.getLayoutParams();
        LinearLayout.LayoutParams tvProValueLp = (LinearLayout.LayoutParams) mTvProValue.getLayoutParams();
        LinearLayout.LayoutParams seekbarLp = (LinearLayout.LayoutParams) mLoadingSeekbar.getLayoutParams();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            roundIconLp.topMargin = DensityUtil.dip2px(getContext(), 30);
            tvProValueLp.topMargin = DensityUtil.dip2px(getContext(), 15);
            seekbarLp.topMargin = DensityUtil.dip2px(getContext(), 15);
        } else {
            roundIconLp.topMargin = DensityUtil.dip2px(getContext(), 80);
            tvProValueLp.topMargin = DensityUtil.dip2px(getContext(), 35);
            seekbarLp.topMargin = DensityUtil.dip2px(getContext(), 35);
        }

        mRoundIcon.setLayoutParams(roundIconLp);
        mTvProValue.setLayoutParams(tvProValueLp);
        mLoadingSeekbar.setLayoutParams(seekbarLp);
    }

    @Override
    protected void updateLoadingText(String msg) {
//        mLoadingText.setText(msg);
    }
}
