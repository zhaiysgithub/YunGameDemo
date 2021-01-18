package kptech.game.kit.redfinger.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mci.commonplaysdk.MCISdkView;
import com.mci.commonplaysdk.PlayMCISdkManager;
import com.mci.commonplaysdk.PlaySdkCallbackInterface;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

import kptech.game.kit.APICallback;
import kptech.game.kit.APIConstants;
import kptech.game.kit.GameInfo;
import kptech.game.kit.IDeviceControl;
import kptech.game.kit.redfinger.PlaySDKManager;

public class PlayFragment extends Fragment {
    private static final String TAG = "PlayFragment";

//    private FrameLayout rootLayout;
    private MCISdkView mMCISdkView;

//    private APICallback<String> mCallback;
//    private PlayMCISdkManager mPlayMCISdkManager;
//    private IDeviceControl.PlayListener mPlayListener;

    private PlaySDKManager mPlaySDKManager;

//    private int mApiLevel = 2;
//    private int mUseSSL = 0;
//    private String mDeviceInfo;
//    private GameInfo mGameInfo;

    private BackstageTimer backstageTimer;
    private DownstageTimer downstageTimer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootLayout = new FrameLayout(getActivity().getApplicationContext());
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mMCISdkView = new MCISdkView(getActivity());
        mMCISdkView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rootLayout.addView(mMCISdkView);

        this.mPlaySDKManager = PlaySDKManager.getInstance();

        this.downstageTimer = new DownstageTimer(PlaySDKManager.fontTime, 1000);
        this.backstageTimer = new BackstageTimer(PlaySDKManager.backTime, 1000);

        connect();

        return rootLayout;
    }

    private void connect(){
        if (mPlaySDKManager != null){
            mPlaySDKManager.start(getActivity(), mMCISdkView);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlaySDKManager != null){
            mPlaySDKManager.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlaySDKManager != null){
            mPlaySDKManager.pause();
        }
    }

    @Override
    public void onDestroy() {
        if (mPlaySDKManager != null){
            mPlaySDKManager.stop();
            mPlaySDKManager.destory();
        }
        mPlaySDKManager = null;

        DownstageTimer downstageTimer2 = this.downstageTimer;
        if (downstageTimer2 != null) {
            downstageTimer2.cancel();
        }
        BackstageTimer backstageTimer2 = this.backstageTimer;
        if (backstageTimer2 != null) {
            backstageTimer2.cancel();
        }

        super.onDestroy();
    }

    private void resetTimer(boolean z) {
        if (z) {
            DownstageTimer downstageTimer2 = this.downstageTimer;
            if (downstageTimer2 != null) {
                downstageTimer2.cancel();
                this.downstageTimer.start();
            }
            BackstageTimer backstageTimer2 = this.backstageTimer;
            if (backstageTimer2 != null) {
                backstageTimer2.cancel();
                return;
            }
            return;
        }
        DownstageTimer downstageTimer3 = this.downstageTimer;
        if (downstageTimer3 != null) {
            downstageTimer3.cancel();
        }
        BackstageTimer backstageTimer3 = this.backstageTimer;
        if (backstageTimer3 != null) {
            backstageTimer3.start();
        }
    }

    /* access modifiers changed from: protected */
    public class BackstageTimer extends CountDownTimer {
        public BackstageTimer(long j, long j2) {
            super(j, j2);
        }

        public void onFinish() {
//            PlayFragment.this.stopPlay();
//            if (PlayFragment.this.playCallback != null) {
//                PlayFragment.this.playCallback.a(1, t.j);
//            }
        }

        public void onTick(long j) {
        }
    }

    /* access modifiers changed from: protected */
    public class DownstageTimer extends CountDownTimer {
        public DownstageTimer(long j, long j2) {
            super(j, j2);
        }

        public void onFinish() {
//            PlayFragment.this.countDownLayout.setVisibility(8);
//            v.d("ControlTimer onFinish");
//            PlayFragment.this.stopPlay();
//            if (PlayFragment.this.playCallback != null) {
//                PlayFragment.this.playCallback.a(2, t.k);
//            }
        }

        public void onTick(long j) {
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
