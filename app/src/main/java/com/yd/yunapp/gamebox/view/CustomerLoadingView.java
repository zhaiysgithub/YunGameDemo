package com.yd.yunapp.gamebox.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.bumptech.glide.Glide;
import com.kuaipan.game.demo.R;

import kptech.game.kit.GameInfo;
import kptech.game.kit.utils.DensityUtil;
import kptech.game.kit.view.LoadingPageView;

/**
 * 自定义 loadingView
 */
public class CustomerLoadingView extends LoadingPageView {

    private AppCompatImageView mImageView;
    private TextView mTvGameName;
    private TextView mProgressValue;
    private AppCompatSeekBar mSeekBar;
    private TextView mLoadingText;

    public CustomerLoadingView(@NonNull Context context) {
        super(context);
    }

    public CustomerLoadingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomerLoadingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void inflateView() {
        View view = inflate(getContext(), R.layout.view_customer_loading, this);
        TextView mLoadingTitle = view.findViewById(R.id.tvLoadingTitle);
        mImageView = view.findViewById(R.id.iconGame);
        mTvGameName = view.findViewById(R.id.tvGameName);
        mProgressValue = view.findViewById(R.id.tvProgressValue);
        mSeekBar = view.findViewById(R.id.loadingSeekbar);
        mLoadingText = view.findViewById(R.id.tvLoadingText);

        mLoadingTitle.setText("测试自定义loadingView");
        mSeekBar.setMax(pbMax);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void updateChildProgress(int progress) {
        mSeekBar.setProgress(progress);
        int value = progress * 100 / pbMax;
        mProgressValue.setText(value + "%");
    }

    @Override
    protected void setLoadingInfo(GameInfo gameInfo) {
        mTvGameName.setText(gameInfo.name);
        String iconUrl = gameInfo.iconUrl;
        if (iconUrl != null && !iconUrl.isEmpty()) {
            Glide.with(getContext()).load(iconUrl).placeholder(R.mipmap.ico_default).into(mImageView);
        }
    }

    @Override
    protected void onConfigChanged(Configuration newConfig) {
        LinearLayout.LayoutParams roundIconLp = (LinearLayout.LayoutParams) mImageView.getLayoutParams();
        LinearLayout.LayoutParams tvProValueLp = (LinearLayout.LayoutParams) mProgressValue.getLayoutParams();
        LinearLayout.LayoutParams seekbarLp = (LinearLayout.LayoutParams) mSeekBar.getLayoutParams();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            roundIconLp.topMargin = DensityUtil.dip2px(getContext(), 30);
            tvProValueLp.topMargin = DensityUtil.dip2px(getContext(), 15);
            seekbarLp.topMargin = DensityUtil.dip2px(getContext(), 15);
        } else {
            roundIconLp.topMargin = DensityUtil.dip2px(getContext(), 80);
            tvProValueLp.topMargin = DensityUtil.dip2px(getContext(), 35);
            seekbarLp.topMargin = DensityUtil.dip2px(getContext(), 35);
        }

        mImageView.setLayoutParams(roundIconLp);
        mProgressValue.setLayoutParams(tvProValueLp);
        mSeekBar.setLayoutParams(seekbarLp);
    }

    @Override
    protected void updateLoadingText(String msg) {
        mLoadingText.setText(msg);
    }
}
