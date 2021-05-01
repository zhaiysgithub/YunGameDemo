package com.kptech.gamesdkdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import kptech.game.kit.GameInfo;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView mTextView,mTvNoData;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private MainModel mMainModel;
    private MainAdapter mMainAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.tvAppId);
        mRecyclerView = findViewById(R.id.recyclerView);
        mProgressBar = findViewById(R.id.progressBar);
        mTvNoData = findViewById(R.id.tvNoData);

        mMainModel = new MainModel(this, callback);
        setTitle(mMainModel.getTitleStr());

        initView();
        mTvNoData.setOnClickListener(mNoDataClick);
        getGameInfos();
    }

    private void getGameInfos(){
        String appKey = Enviroment.getInstance().getmCropKey();
        if (!appKey.isEmpty()) {
            mProgressBar.setVisibility(View.VISIBLE);
            mMainModel.getGameInfos();
        } else {
            Toast.makeText(this, CustomerApplication.appKeyErrorMsg,Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.GONE);
            mTvNoData.setVisibility(View.VISIBLE);
        }
    }

    private void initView() {
        mTextView.setText(mMainModel.getAppId());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mMainAdapter = new MainAdapter(this);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mMainAdapter);
        mMainAdapter.setOnItemClickListener(mItemListener);
    }


    private final MainModel.MainCallback callback = new MainModel.MainCallback() {
        @Override
        public void onNext(@NonNull List<GameInfo> gameInfos) {
            if (gameInfos != null && gameInfos.size() > 0) {
                mMainAdapter.addAll(gameInfos);
            }
        }

        @Override
        public void onError(@NonNull Throwable e) {
            Log.e(TAG, "onError:" + e.getMessage());
            mProgressBar.setVisibility(View.GONE);
            mTvNoData.setVisibility(View.VISIBLE);
        }

        @Override
        public void onComplete() {
            mProgressBar.setVisibility(View.GONE);
            mTvNoData.setVisibility(View.GONE);
        }
    };

    private final OnItemCallback mItemListener = new OnItemCallback() {
        @Override
        public void onItemClickListener(GameInfo gameInfo) {

        }

        @Override
        public void onItemPlayClick(GameInfo gameInfo) {
            if (gameInfo != null) {
                mMainModel.startGame(gameInfo);
            }
        }
    };

    private final View.OnClickListener mNoDataClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mTvNoData.setVisibility(View.GONE);
            getGameInfos();
        }
    };
}