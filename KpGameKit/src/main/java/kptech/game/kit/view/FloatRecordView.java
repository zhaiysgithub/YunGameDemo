package kptech.game.kit.view;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import kptech.game.kit.R;
//import kptech.game.kit.dialog.RecordPublishPopup;
import kptech.game.kit.dialog.view.Loading;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.StringUtil;
import kptech.game.kit.utils.TToast;
import kptech.lib.analytic.DeviceInfo;
import kptech.lib.conf.RecordScreenConfig;
import kptech.lib.data.RequestRecordScreen;

public class FloatRecordView extends FrameLayout implements View.OnClickListener {
    private static final String TAG =  FloatRecordView.class.getSimpleName();

    private TextView stateText;
    private TextView timeText;
    private TextView finishTimeText;
    private ImageView pauseBtn;

    private RecordView mRecordView;
    private RecordPublishView mPublishView;

    private int maxTimeLen = 300;
    private int minTimeLen = 5;

    public FloatRecordView(Context context) {
        super(context);
        initView();
    }

    public FloatRecordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.kp_view_record_float, this);
        mFinishLayout  = findViewById(R.id.finishLayout);
        mRecordView = findViewById(R.id.recordView);
        mPublishView = findViewById(R.id.publishView);
        mPublishView.setOnPublishListener(new RecordPublishView.OnPublishListener() {
            @Override
            public void onPublish(String title) {
                publish(title);
            }
        });

        pauseBtn = mRecordView.findViewById(R.id.playpause);
        pauseBtn.setOnClickListener(this);
        mRecordView.findViewById(R.id.stop).setOnClickListener(this);
        stateText = mRecordView.findViewById(R.id.state_text);
        timeText = mRecordView.findViewById(R.id.time_text);

        finishTimeText = findViewById(R.id.finish_time);
        findViewById(R.id.finish_remove).setOnClickListener(this);
        findViewById(R.id.finish_publish).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.playpause) {
            pauseresume();
        } else if (id == R.id.stop) {
            stopRecord();
        } else if (id == R.id.finish_remove) {
            hideFinishLayout();
            TToast.showCenterToast(getContext(), "视频已删除", Toast.LENGTH_SHORT);
        } else if (id == R.id.finish_publish) {
            hideFinishLayout();

            mPublishView.show();

            upload();

//            RecordPublishPopup popup = new RecordPublishPopup(getContext());
//            popup.setOnPublishListener(new RecordPublishPopup.OnPublishListener() {
//                @Override
//                public void onPublish(String title) {
//                    publish(title);
//                }
//            });
//            popup.show();
        }
    }

    private static final int STATE_IDLE = 0;
    private static final int STATE_PLAYING = 1;
    private static final int STATE_PAUSED = 2;

    private String mPadcode;
    private String mPkgName;
    private String mGameName;
    private String mCorpKey;
    public void setCorpKey(String key){
        this.mCorpKey = key;
    }

    public void startRecord(String padcode, String pkgName, String gameName){
        if (state != STATE_IDLE){
            return;
        }
        if (StringUtil.isEmpty(padcode) || StringUtil.isEmpty(pkgName)){
            return;
        }

        //获取录屏配置
        RecordScreenConfig mConfig = RecordScreenConfig.getConfig(getContext());
        if (mConfig != null){
            minTimeLen = mConfig.minTimeLen;
            maxTimeLen = mConfig.maxTimeLen;
        }

        this.setVisibility(VISIBLE);
        timeText.setText("00:00");
        stateText.setText("录制中");
        second = 0;

        if (mHandler == null){
            mHandler = new MyHandler(this);
        }
        this.mPadcode = padcode;
        this.mPkgName = pkgName;
        this.mGameName = gameName;
        request(RequestRecordScreen.ACTION_RECORD_START);
    }

    public void stopRecord(){

        state = STATE_IDLE;
        updateState(state);

        if (second > minTimeLen) {
            request(RequestRecordScreen.ACTION_RECORD_STOP);
        }

        showFinishLayout();
    }

    public void upload(){
        request(RequestRecordScreen.ACTION_RECORD_UPLOAD);
    }

    public void publish(String title){
        request(RequestRecordScreen.ACTION_RECORD_PUBLISH, title);
    }

    public void pauseresume(){
        if (state == STATE_IDLE){
            return;
        }

        if (state == STATE_PLAYING){
            request(RequestRecordScreen.ACTION_RECORD_PAUSE);
        }else if (state == STATE_PAUSED){
            request(RequestRecordScreen.ACTION_RECORD_RESUME);
        }
    }
    private MyHandler mHandler;

    public void reset() {

    }

    private static class MyHandler extends Handler {
        WeakReference<FloatRecordView> ref = null;
        public MyHandler(FloatRecordView view) {
            super(Looper.myLooper());
            ref = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 1:
                   int state = (int) msg.obj;
                   if (ref != null && ref.get() != null){
                       ref.get().updateState(state);
                   }
                   break;
                case 2:
                    if (ref != null && ref.get() != null){
                        ref.get().updateTime();
                    }
                    break;
            }
        }
    }

    private int second = 0;

    private void updateTime(){
        if (state !=  STATE_PLAYING) {
            return;
        }

        second++;

        if (second >= maxTimeLen) {
            stopRecord();
            return;
        }

        timeText.setText(getTimeStr(second));

        mHandler.sendEmptyMessageDelayed(2, 1000);
    }

    private String getTimeStr(int second){
        String time = "";
        int min =  second / 60;
        if (min < 10){
            time += "0" + min;
        }else {
            time += min;
        }
        time += ":";
        int sec = second % 60;
        if (sec < 10){
            time += "0" + sec;
        }else {
            time += sec;
        }
        return time;
    }

    private void updateState(int state){

        switch (state){
            case STATE_IDLE:
                stateText.setText("开始");
                pauseBtn.setImageResource(R.drawable.kp_ic_record_play);
                break;
            case STATE_PLAYING:
                stateText.setText("录制中");
                pauseBtn.setImageResource(R.drawable.kp_ic_record_pause);
                mHandler.sendEmptyMessageDelayed(2, 1000);
                mRecordView.setVisibility(VISIBLE);
                break;
            case STATE_PAUSED:
                stateText.setText("已暂停");
                pauseBtn.setImageResource(R.drawable.kp_ic_record_play);
                break;
            default:
                return;
        }
        this.state  = state;
    }

    private int state = STATE_IDLE;
    private boolean sending = false;
    private Loading mLoading = null;
    private synchronized void request(int action){
        request(action, null);
    }
    private synchronized void request(final int action, String title){
        try {
            if (sending){
                return;
            }
            if (getContext()==null || ! (getContext() instanceof Activity)){
                return;
            }
            final Activity activity = (Activity) getContext();
            if (activity.isFinishing()){
                return;
            }
            sending = true;
            if (action == RequestRecordScreen.ACTION_RECORD_START){
                if (mLoading == null){
                    mLoading = Loading.build(getContext());
                }
                mLoading.show();
            }

//            String[]  params  = (title != null) ? new String[]{mPadcode, mPkgName, title} : new String[]{mPadcode, mPkgName};

            HashMap data = new HashMap();
            if (action == RequestRecordScreen.ACTION_RECORD_PUBLISH){
                data.put("appname", mGameName);
                data.put("pkgname", mPkgName);
                data.put("title",title);
                data.put("content", title);
                data.put("uid", DeviceInfo.getUserId(getContext()));
                data.put("deviceid", DeviceInfo.getDeviceId(getContext()));
                data.put("corpkey", mCorpKey);
            }

            new RequestRecordScreen(getContext(), action)
                    .setCorpKey(mCorpKey)
                    .setCallback(new RequestRecordScreen.ICallback() {
                        @Override
                        public void onSuccess() {
                            sending = false;
                            if(activity.isFinishing()){
                                return;
                            }
                            if (mLoading != null){
                                mLoading.dismiss();
                                mLoading = null;
                            }

                            int state = -1;
                            if (action == RequestRecordScreen.ACTION_RECORD_START){
                                state = STATE_PLAYING;
                                TToast.showCenterToast(getContext(), "录屏开始\n最长可录制5分钟内容", Toast.LENGTH_LONG);
                            }else if (action == RequestRecordScreen.ACTION_RECORD_RESUME){
                                state = STATE_PLAYING;
                            }else if (action  == RequestRecordScreen.ACTION_RECORD_PAUSE){
                                state = STATE_PAUSED;
                            }else if (action == RequestRecordScreen.ACTION_RECORD_STOP){

                            }else if (action == RequestRecordScreen.ACTION_RECORD_PUBLISH){
                                TToast.showCenterToast(getContext(), "视频发布完成", Toast.LENGTH_SHORT);
                            }
                            if (state != -1){
                                updateState(state);
                            }

//                            switch (action){
//                                case RequestRecordScreen.ACTION_RECORD_START:
//                                    //显示弹窗
//                                    state = STATE_PLAYING;
//                                    break;
//                                case RequestRecordScreen.ACTION_RECORD_PAUSE:
//                                    state = STATE_PAUSED;
//                                    break;
//                                case RequestRecordScreen.ACTION_RECORD_RESUME:
//                                    state = STATE_PLAYING;
//                                    break;
//                            }
//
//                            if (state != -1){
//                                Message msg = Message.obtain();
//                                msg.what = 1;
//                                msg.obj = state;
//                                mHandler.sendMessage(msg);
//                            }
                        }

                        @Override
                        public void onError(String err) {
                            sending = false;
                            if(activity.isFinishing()){
                                return;
                            }
                            if (mLoading != null){
                                mLoading.dismiss();
                                mLoading = null;
                            }
                            TToast.showCenterToast(getContext(), err, Toast.LENGTH_LONG);
                        }
                    })
                    .execute(mPadcode, mPkgName, data);
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }
    }

    private ViewGroup mFinishLayout;

    private void showFinishLayout() {
        if (mFinishLayout.getVisibility() == View.VISIBLE){
            return;
        }

        mRecordView.setVisibility(GONE);

        if (second < minTimeLen) {
            TToast.showCenterToast(getContext(), "录屏时间太短", Toast.LENGTH_LONG);
            return;
        }

        finishTimeText.setText("视频时长：" + getTimeStr(second));

        mFinishLayout.setVisibility(VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.kp_view_enter_bottom);
        mFinishLayout.startAnimation(animation);
    }

    private void hideFinishLayout(){
        if (mFinishLayout.getVisibility() != View.VISIBLE){
            return;
        }

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.kp_view_exit_bottom);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mFinishLayout.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        mFinishLayout.startAnimation(animation);
    }



}
