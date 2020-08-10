package com.yd.yunapp.gamebox;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kuaipan.game.demo.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import kptech.game.kit.GameBoxManager;
import kptech.game.kit.GameInfo;

public class HorizontalHomeActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int PLAY_GAME_REQUEST = 100;
    private static final int MSG_REFRESH_LIST = 1;
    private static final int MSG_SHOW_ERROR = 2;
    private static final int MSG_SHOW_NODATA = 3;
    private static final int MSG_SHOW_EMPTY_DATA = 4;
    private RecyclerView mGameList;
    private LinearLayout mLoadingView;
    private LinearLayout mNetworkErrorView;
    private TextView mTips;
    private FrameLayout mLoadingContainar;
    private HorizontalGameAdapter mGameAdapter;
    private LinkedHashMap<Integer, GameInfo> mGameInfos;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            HorizontalHomeActivity.this.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal_main);
        mGameInfos = new LinkedHashMap<>();
        initView();
    }

    private void initView() {
        mLoadingContainar = (FrameLayout) findViewById(R.id.loading_containar);
        mNetworkErrorView = (LinearLayout) findViewById(R.id.fail_ll);
        mLoadingView = (LinearLayout) findViewById(R.id.loading_ll);
        mTips = (TextView) findViewById(R.id.network_error_tips);
        mNetworkErrorView.setOnClickListener(this);
        mGameList = (RecyclerView) findViewById(R.id.game_list);
        mGameList.setHasFixedSize(true);
        if (mGameInfos.size() == 0) {
            mLoadingContainar.setVisibility(View.VISIBLE);
        } else {
            mLoadingContainar.setVisibility(View.GONE);
        }
        mGameAdapter = new HorizontalGameAdapter(this);
        mGameAdapter.setOnItemClickListener(new HorizontalGameAdapter.OnItemClickListener() {
            @Override public void onItemClick(View view, int pos) {
                Intent intent = new Intent(HorizontalHomeActivity.this, GameRunningActivity.class);
                intent.putExtra(GameRunningActivity.EXTRA_GAME, (GameInfo) mGameAdapter.getItem(pos));
                HorizontalHomeActivity.this.startActivityForResult(intent, HomeActivity.PLAY_GAME_REQUEST);
            }
        });
        mGameList.setAdapter(mGameAdapter);
        loadGame();
    }


    private void loadMoreData() {
        loadGame();
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_REFRESH_LIST:
                mGameAdapter.refresh(new ArrayList<GameInfo>(mGameInfos.values()));
                mTips.setVisibility(View.GONE);
                mLoadingContainar.setVisibility(View.GONE);
                break;
            case MSG_SHOW_ERROR:
                mTips.setVisibility(View.VISIBLE);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mTips.setVisibility(View.GONE);
                    }
                }, 3000);
                break;
            case MSG_SHOW_EMPTY_DATA:
                mLoadingView.setVisibility(View.GONE);
                mLoadingContainar.setVisibility(View.VISIBLE);
                mNetworkErrorView.setVisibility(View.VISIBLE);
                break;
            case MSG_SHOW_NODATA:
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mNetworkErrorView) {
            mNetworkErrorView.setVisibility(View.GONE);
            mLoadingView.setVisibility(View.VISIBLE);
            loadGame();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLAY_GAME_REQUEST && resultCode == RESULT_OK) {
            final GameInfo info = data.getParcelableExtra(GameRunningActivity.EXTRA_GAME);
            new Thread() {
                @Override
                public void run() {
                    // 试玩结束后更新游戏信息
                    GameBoxManager.getInstance(HorizontalHomeActivity.this).updateGameInfo(info);
                    mGameInfos.put(info.gid, info);
                    mHandler.sendEmptyMessage(MSG_REFRESH_LIST);
                }
            }.start();
        }
    }

    private void loadGame() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<GameInfo> result = GameBoxManager.getInstance(HorizontalHomeActivity.this)
                        .queryGameList(mGameInfos.size(), 50);
                if (result != null && result.size() > 0) {
                    for (GameInfo info : result) {
                        mGameInfos.put(info.gid, info);
                    }
                    mHandler.sendEmptyMessage(MSG_REFRESH_LIST);
                } else if (result != null && result.size() == 0 && mGameInfos.size() > 0) {
                    mHandler.sendEmptyMessageDelayed(MSG_SHOW_NODATA, 1000);
                } else if (mGameInfos.size() == 0) {
//                    if (!NetworkUtils.isNetworkAvaialble(MainActivity.this)) {
//                        mHandler.sendEmptyMessage(MSG_SHOW_ERROR);
//                    }
                    mHandler.sendEmptyMessage(MSG_SHOW_EMPTY_DATA);
                } else {
                    mHandler.sendEmptyMessageDelayed(MSG_SHOW_ERROR, 1000);
                }
            }
        }).start();
    }

    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            ((GridLayoutManager) mGameList.getLayoutManager()).setSpanCount(3);
        } else if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_USER) {
            ((GridLayoutManager) mGameList.getLayoutManager()).setSpanCount(5);
        }
    }
}
