package com.kptach.lib.game.bdsdk.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.kptach.lib.game.bdsdk.play.KpPlaySDKManager;
import com.kptach.lib.game.bdsdk.utils.Logger;
import com.mci.play.MCISdkView;

public class PlayFragment extends Fragment {
    private static final String TAG = "PlayFragment";

//    private FrameLayout rootLayout;
    private MCISdkView mMCISdkView;

//    private APICallback<String> mCallback;
//    private PlayMCISdkManager mPlayMCISdkManager;
//    private IDeviceControl.PlayListener mPlayListener;

    private KpPlaySDKManager mPlaySDKManager;

//    private int mApiLevel = 2;
//    private int mUseSSL = 0;
//    private String mDeviceInfo;
//    private GameInfo mGameInfo;

    private BackstageTimer backstageTimer;
    private DownstageTimer downstageTimer;

    private boolean isResumedStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mDeviceInfo = getArguments().getString("deviceInfo");
//        mGameInfo = getArguments().getParcelable("gameInfo");

//        if (mPlaySDKManager!=null){
//            mPlaySDKManager.start(getActivity(), this.mMCISdkView);
//        }
    }

//    public void setCallback(APICallback<String> callback){
//        this.mCallback = callback;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        ViewGroup rootLayout = new FrameLayout(getActivity().getApplicationContext());
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mMCISdkView = new MCISdkView(getActivity());
        mMCISdkView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rootLayout.addView(mMCISdkView);
        mMCISdkView.getSwDisplay().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Logger.info(TAG, "onTouch");
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    resetTimer(isResumedStatus);
                }
                return false;
            }
        });

        this.mPlaySDKManager = KpPlaySDKManager.getInstance();

        this.downstageTimer = new DownstageTimer(KpPlaySDKManager.fontTime, 1000);
        this.backstageTimer = new BackstageTimer(KpPlaySDKManager.backTime, 1000);

        connect();

        return rootLayout;
    }

    private void connect(){
        if (mPlaySDKManager != null){
            mPlaySDKManager.start(getActivity(), mMCISdkView);
        }
    }

    private void stopPlay(){
        if (mPlaySDKManager != null) {
            mPlaySDKManager.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.isResumedStatus = true;
        if (mPlaySDKManager != null){
            mPlaySDKManager.resume();
        }

        resetTimer(this.isResumedStatus);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.isResumedStatus = false;
        if (mPlaySDKManager != null){
            mPlaySDKManager.pause();
        }

        resetTimer(this.isResumedStatus);
        if (getActivity() != null && getActivity().isFinishing()) {
            stopPlay();
        }
    }

    @Override
    public void onDestroy() {
        if (mPlaySDKManager != null){
            mPlaySDKManager.destory();
        }
        mPlaySDKManager = null;

        if (this.downstageTimer != null) {
            this.downstageTimer.cancel();
        }
        if (this.backstageTimer != null) {
            this.backstageTimer.cancel();
        }

        super.onDestroy();
    }

    private void resetTimer(boolean font) {
        if (font) {
            if (this.downstageTimer != null) {
                this.downstageTimer.cancel();
                this.downstageTimer.start();
            }
            if (this.backstageTimer != null) {
                this.backstageTimer.cancel();
                return;
            }
            return;
        }
        if (this.downstageTimer != null) {
            this.downstageTimer.cancel();
        }
        if (this.backstageTimer != null) {
            this.backstageTimer.start();
        }
    }

    /* access modifiers changed from: protected */
    public class BackstageTimer extends CountDownTimer {
        public BackstageTimer(long j, long j2) {
            super(j, j2);
        }

        public void onFinish() {
            if (PlayFragment.this.mPlaySDKManager != null) {
                PlayFragment.this.mPlaySDKManager.onNoOpsTimeout(1, KpPlaySDKManager.backTime);
            }
            Logger.info(TAG, "BackstageTimer onFinish");
        }

        public void onTick(long j) {
            Logger.info(TAG, "BackstageTimer ontick:"+j);
        }
    }


    /* access modifiers changed from: protected */
    public class DownstageTimer extends CountDownTimer {
        private long lastAliveTime = 0;

        public DownstageTimer(long j, long j2) {
            super(j, j2);
            lastAliveTime = System.currentTimeMillis();
        }

        public void onFinish() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (PlayFragment.this.mPlaySDKManager != null) {
                        PlayFragment.this.mPlaySDKManager.onNoOpsTimeout(2, KpPlaySDKManager.fontTime);
                    }
                }
            });

            Logger.info(TAG, "DownstageTimer onFinish");
        }

        public void onTick(long j) {
            Logger.info(TAG, "DownstageTimer ontick:"+j);
            //如果超过5分钟没有操作，发送一个消息，保持游戏继续
            if ((System.currentTimeMillis() - lastAliveTime) > 4 * 60 * 1000) {
                lastAliveTime = System.currentTimeMillis();
                PlayFragment.this.sendAliveMsg();
            }
        }
    }

    private void sendAliveMsg(){
        Logger.info(TAG, "sendAliveMsg");
        if (mPlaySDKManager != null){
            mPlaySDKManager.sendLocationData(
                    (float) 108.5229, // longitude
                    (float) 38.4166, // latitude
                    0.0f, // altitude
                    0.0f, // floor
                    0.0f, // horizontalaccuracy
                    0.0f, // verticalaccuracy
                    0.0f, // speed
                    0.0f // direction
            );
        }
    }

//    private void releaseData(){
//        if (mPlayMCISdkManager != null){
//            mPlayMCISdkManager.release();
//            mPlayMCISdkManager = null;
//        }
//        mCallback = null;
//        mPlayListener = null;
//        mMCISdkView = null;
//        mGameInfo = null;
//    }

//    public void stopPlay(){
//        if (mPlayMCISdkManager != null){
//            mPlayMCISdkManager.stop();
//        }
//    }
//
//    private void startPlay(){
//
//        //初始化SDK
//        mPlayMCISdkManager = new PlayMCISdkManager(getActivity(), false);
//
//        int ret = 0;
//
//        //5、set game parameters
//        ret = mPlayMCISdkManager.setParams(this.mDeviceInfo, this.mGameInfo.pkgName, mApiLevel, mUseSSL, mMCISdkView, new PlayFragment.InnerPlayListener(this));
//        if (ret != 0) {
//            //设置参数错误，返回
//            return;
//        }
//
//        //6、start game
//        ret = mPlayMCISdkManager.start();
//        if (ret != 0) {
//            //开始游戏失败，返回
//            return;
//        }
//    }

//    public void setPlayListener(IDeviceControl.PlayListener listener) {
//        this.mPlayListener = listener;
//    }
//
//    public void resetValue() {
//    }
//
//    public void Connect() {
//        startPlay();
//    }


}
