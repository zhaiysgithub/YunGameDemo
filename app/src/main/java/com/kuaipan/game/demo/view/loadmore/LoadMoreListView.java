package com.kuaipan.game.demo.view.loadmore;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.kuaipan.game.demo.R;

import java.lang.reflect.Constructor;


public class LoadMoreListView extends ListView {

    /**
     * 加载更多UI
     */
    ILoadMoreView mLoadMoreView;
    /**
     * 加载更多方式，默认滑动到底部加载更多
     */
    LoadMoreMode mLoadMoreMode = LoadMoreMode.SCROLL;
    /**
     * 是否可以加载跟多
     */
    boolean mHasMoreData = true;
    /**
     * 加载更多lock
     */
    private boolean mLoadMoreLock;
    /**
     * 是否加载失败
     */
    private boolean mHasLoadFail;
    /**
     * 加载更多事件回调
     */
    private OnLoadMoreListener mOnLoadMoreListener;
    /**
     * 没有更多了是否隐藏loadmoreview
     */
    private boolean mNoLoadMoreHideView;
    private boolean mAddLoadMoreFooterFlag;
    private int mLoadMoreViewBottom;
    private int mLoadMoreViewTop;
    private int mLoadMoreViewLeft;
    private int mLoadMoreViewRight;
    private boolean mLoadMoreViewShowState;
    /**
     * 刷新数据时停止滑动,避免出现数组下标越界问题
     */
    private DataSetObserver mDataObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_CANCEL, 0, 0, 0));
        }

        @Override
        public void onInvalidated() {
        }
    };

    public LoadMoreListView(Context context) {
        super(context);
        init(context, null);
    }

    public LoadMoreListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LoadMoreListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadMoreListView);

        if (a.hasValue(R.styleable.LoadMoreListView_loadMoreMode)) {
            mLoadMoreMode = LoadMoreMode.mapIntToValue(a.getInt(R.styleable.LoadMoreListView_loadMoreMode, 0x01));
        } else {
            mLoadMoreMode = LoadMoreMode.SCROLL;
        }

        if (a.hasValue(R.styleable.LoadMoreListView_noLoadMoreHideView)) {
            mNoLoadMoreHideView = a.getBoolean(R.styleable.LoadMoreListView_noLoadMoreHideView, false);
        } else {
            mNoLoadMoreHideView = false;
        }

        if (a.hasValue(R.styleable.LoadMoreListView_loadMoreView)) {
            try {
                String loadMoreViewName = a.getString(R.styleable.LoadMoreListView_loadMoreView);
                Class clazz = Class.forName(loadMoreViewName);
                Constructor c = clazz.getConstructor(Context.class);
                ILoadMoreView loadMoreView = (ILoadMoreView) c.newInstance(context);
                mLoadMoreView = loadMoreView;
            } catch (Exception e) {
                mLoadMoreView = new DefaultLoadMoreView(getContext());
            }
        } else {
            mLoadMoreView = new DefaultLoadMoreView(getContext());
        }

        setOnScrollListener(new ListViewOnScrollListener());
        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        showNoMoreUI();
        hideLoadMoreView();
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (!mAddLoadMoreFooterFlag) {
            mAddLoadMoreFooterFlag = true;
            addFooterView(mLoadMoreView.getFooterView());
        }
        super.setAdapter(adapter);
        if (adapter instanceof BaseAdapter) {
            try {
                adapter.unregisterDataSetObserver(mDataObserver);
            } catch (Exception e) {
                // 无需处理
            }
            adapter.registerDataSetObserver(mDataObserver);
        }
    }

    private void onScorllBootom() {
        if (mHasMoreData && mLoadMoreMode == LoadMoreMode.SCROLL) {
            executeLoadMore();
        }
    }

    /**
     * 设置LoadMoreView(需要在setAdapter之前)
     * @param loadMoreView
     */
    public void setLoadMoreView(ILoadMoreView loadMoreView) {
//        if (mLoadMoreView != null) {
//            try {
//                removeFooterView(mLoadMoreView.getFooterView());
//                mAddLoadMoreFooterFlag = false;
//            } catch (Exception e){}
//        }

        mLoadMoreView = loadMoreView;
        mLoadMoreView.getFooterView().setOnClickListener(new OnMoreViewClickListener());
    }

    /**
     * 设置加载更多模式
     * @param mode
     */
    public void setLoadMoreMode(LoadMoreMode mode) {
        mLoadMoreMode = mode;
    }

    /**
     * 设置没有更多数据了，是否隐藏fooler view
     * @param hide
     */
    public void setNoLoadMoreHideView(boolean hide) {
        this.mNoLoadMoreHideView = hide;
    }

    /**
     * 没有很多了
     */
    void showNoMoreUI() {
        mLoadMoreLock = false;
        mLoadMoreView.showNoMore();
    }

    /**
     * 显示失败UI
     */
    public void showFailUI() {
        mHasLoadFail = true;
        mLoadMoreLock = false;
        mLoadMoreView.showFail();
    }

    /**
     * 显示默认UI
     */
    void showNormalUI() {
        mLoadMoreLock = false;
        mLoadMoreView.showNormal();
    }

    /**
     * 显示加载中UI
     */
    void showLoadingUI() {
        mHasLoadFail = false;
        mLoadMoreView.showLoading();
    }

    /**
     * 是否有更多
     * @param hasLoadMore
     */
    public void setHasMoreData(boolean hasLoadMore) {
        mHasMoreData = hasLoadMore;
        if (!mHasMoreData) {
            showNoMoreUI();
            if (mNoLoadMoreHideView && mLoadMoreViewShowState) {
                hideLoadMoreView();
            }
        } else {
            if (!mLoadMoreViewShowState) {
                showLoadMoreView();
            }
            showNormalUI();
        }
    }

    /**
     * 设置加载更多事件回调
     * @param loadMoreListener
     */
    public void setOnLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.mOnLoadMoreListener = loadMoreListener;
    }

    /**
     * 完成加载更多
     */
    public void onLoadMoreComplete() {
        if (mHasLoadFail) {
            showFailUI();
        } else if (mHasMoreData) {
            showNormalUI();
        }
    }

    /**
     * 加载更多
     */
    void executeLoadMore() {
        if (!mLoadMoreLock && mHasMoreData) {
            if (mOnLoadMoreListener != null) {
                mOnLoadMoreListener.loadMore();
            }
            // 上锁
            mLoadMoreLock = true;
            showLoadingUI();
        }
    }

    private void hideLoadMoreView() {
        mLoadMoreViewShowState = false;
        mLoadMoreViewBottom = mLoadMoreView.getFooterView().getPaddingBottom();
        mLoadMoreViewTop = mLoadMoreView.getFooterView().getPaddingTop();
        mLoadMoreViewLeft = mLoadMoreView.getFooterView().getPaddingLeft();
        mLoadMoreViewRight = mLoadMoreView.getFooterView().getPaddingRight();
        mLoadMoreView.getFooterView().setVisibility(View.GONE);
        mLoadMoreView.getFooterView().setPadding(0, -mLoadMoreView.getFooterView().getHeight(), 0, 0);
    }

    private void showLoadMoreView() {
        mLoadMoreViewShowState = true;
        mLoadMoreView.getFooterView().setVisibility(View.VISIBLE);
        mLoadMoreView.getFooterView().setPadding(mLoadMoreViewLeft, mLoadMoreViewTop, mLoadMoreViewRight,
                mLoadMoreViewBottom);
    }

    public interface OnLoadMoreListener {
        void loadMore();
    }

    /**
     * 点击more view加载更多
     */
    class OnMoreViewClickListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            if (mHasMoreData) {
                executeLoadMore();
            }
        }
    }

    /**
     * 滚动到底部自动加载更多数据
     */
    private class ListViewOnScrollListener implements OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView listView, int scrollState) {
            // 如果滚动到最后一行
            if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
                    && listView.getLastVisiblePosition() + 1 == listView.getCount()) {
                onScorllBootom();
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }
    }


}