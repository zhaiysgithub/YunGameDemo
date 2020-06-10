package com.kuaipan.game.demo.view.loadmore;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kuaipan.game.demo.R;


public class DefaultLoadMoreView extends FrameLayout implements ILoadMoreView {

    private View mContentView;
    private ProgressBar mProgressBar;
    private TextView mTextView;

    public DefaultLoadMoreView(@NonNull Context context) {
        super(context);
        initView();
    }

    public DefaultLoadMoreView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.loading_more, this);
        mContentView = findViewById(R.id.loadmore_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mTextView = (TextView) findViewById(R.id.text);
        setClickable(false);
    }

    @Override
    public void showNormal() {
//        mContentView.setVisibility(GONE);
        mTextView.setVisibility(GONE);
        mProgressBar.setVisibility(GONE);
    }


    @Override
    public void showNoMore() {
        mContentView.setVisibility(VISIBLE);
        mProgressBar.setVisibility(GONE);
        mTextView.setVisibility(VISIBLE);
        mTextView.setText("没有更多数据了");
    }

    @Override
    public void showLoading() {
        mContentView.setVisibility(VISIBLE);
        mProgressBar.setVisibility(VISIBLE);
        mTextView.setVisibility(VISIBLE);
        mTextView.setText("正在加载更多...");
    }

    @Override
    public void showFail() {

    }

    @Override
    public View getFooterView() {
        return this;
    }
}
