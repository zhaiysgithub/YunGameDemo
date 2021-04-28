package kptech.game.kit.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
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
import java.util.Map;

import kptech.game.kit.R;
//import kptech.game.kit.dialog.RecordPublishPopup;
import kptech.game.kit.dialog.view.Loading;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.StringUtil;
import kptech.game.kit.utils.TToast;
import kptech.lib.analytic.DeviceInfo;
import kptech.lib.analytic.Event;
import kptech.lib.analytic.EventCode;
import kptech.lib.analytic.MobclickAgent;
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

    private String mCoverImg;

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
            try {
                //发送打点事件
                Event event = Event.getEvent(EventCode.DATA_RECORD_CLICK_STOPBTN, mPkgName );
                event.setPadcode(mPadcode);
                MobclickAgent.sendEvent(event);
            }catch (Exception e){
            }

            stopRecord();
        } else if (id == R.id.finish_remove) {
            try {
                //发送打点事件
                Event event = Event.getEvent(EventCode.DATA_RECORD_FINISHED_CALBTN, mPkgName );
                event.setPadcode(mPadcode);
                MobclickAgent.sendEvent(event);
            }catch (Exception e){
            }

            hideFinishLayout();
            TToast.showCenterToast(getContext(), "视频已删除", Toast.LENGTH_SHORT);
        } else if (id == R.id.finish_publish) {

            try {
                //发送打点事件
                Event event = Event.getEvent(EventCode.DATA_RECORD_FINISHED_SUBBTN, mPkgName );
                event.setPadcode(mPadcode);
                MobclickAgent.sendEvent(event);
            }catch (Exception e){
            }

            hideFinishLayout();

            mPublishView.show(mPkgName,mPadcode,mCoverImg);

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

        //判断是否已录制完成
        if (mFinishLayout.getVisibility() == VISIBLE || mPublishView.getVisibility() == VISIBLE){
            TToast.showCenterToast(getContext(), "有未发布的视频，请发布后再录制", Toast.LENGTH_SHORT);
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

            Configuration conf = getResources().getConfiguration();
            int orientation = 0;
            if (conf.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //横屏
                orientation = 2;
            } else if (conf.orientation == Configuration.ORIENTATION_PORTRAIT) {
                //竖屏
                orientation = 1;
            }

//            String[]  params  = (title != null) ? new String[]{mPadcode, mPkgName, title} : new String[]{mPadcode, mPkgName};

            HashMap data = new HashMap();
            if (action == RequestRecordScreen.ACTION_RECORD_PUBLISH){
                data.put("title",title);
                data.put("content", title);
            }

            HashMap clientInfo = new HashMap();
            clientInfo.put("appname", mGameName);
            clientInfo.put("pkgname", mPkgName);
            clientInfo.put("uid", DeviceInfo.getUserId(getContext()));
            clientInfo.put("deviceid", DeviceInfo.getDeviceId(getContext()));
            clientInfo.put("corpkey", mCorpKey);
            clientInfo.put("traceid", Event.getTraceId());
            clientInfo.put("padcode", mPadcode);
            clientInfo.put("orientation", orientation);


            try {
                String eventCode = "";
                switch (action){
                    case RequestRecordScreen.ACTION_RECORD_START:
                        eventCode = EventCode.DATA_RECORD_API_START;
                        break;
                    case RequestRecordScreen.ACTION_RECORD_PAUSE:
                        eventCode = EventCode.DATA_RECORD_API_PAUSE;
                        break;
                    case RequestRecordScreen.ACTION_RECORD_RESUME:
                        eventCode = EventCode.DATA_RECORD_API_RESUME;
                        break;
                    case RequestRecordScreen.ACTION_RECORD_STOP:
                        eventCode = EventCode.DATA_RECORD_API_STOP;
                        break;
                    case RequestRecordScreen.ACTION_RECORD_UPLOAD:
                        eventCode = EventCode.DATA_RECORD_API_UPLOAD;
                        break;
                    case RequestRecordScreen.ACTION_RECORD_PUBLISH:
                        eventCode = EventCode.DATA_RECORD_API_PUBLISH;
                        break;
                }
                //发送打点事件
                if (eventCode != null){
                    Event event = Event.getEvent(eventCode, mPkgName );
                    event.setPadcode(mPadcode);
                    MobclickAgent.sendEvent(event);
                }
            }catch (Exception e){
            }

            new RequestRecordScreen(getContext(), action)
                    .setCorpKey(mCorpKey)
                    .setCallback(new RequestRecordScreen.ICallback() {
                        @Override
                        public void onSuccess(Map ret) {
                            sending = false;
                            if(activity.isFinishing()){
                                return;
                            }
                            if (mLoading != null){
                                mLoading.dismiss();
                                mLoading = null;
                            }

                            try {
                                //发送打点事件
                                Event event = Event.getEvent(EventCode.DATA_RECORD_API_SUCCESS, mPkgName );
                                event.setPadcode(mPadcode);
                                MobclickAgent.sendEvent(event);
                            }catch (Exception e){
                            }

                            try {
                                if (ret!=null && ret.containsKey("coverimg")){
                                    mCoverImg = ret.get("coverimg").toString();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            int state = -1;
                            if (action == RequestRecordScreen.ACTION_RECORD_START){
                                state = STATE_PLAYING;
                                TToast.showCenterToast(getContext(), "录屏开始\n最长可录制5分钟内容", Toast.LENGTH_SHORT);
                            }else if (action == RequestRecordScreen.ACTION_RECORD_RESUME){
                                state = STATE_PLAYING;
                            }else if (action  == RequestRecordScreen.ACTION_RECORD_PAUSE){
                                state = STATE_PAUSED;
                            }else if (action == RequestRecordScreen.ACTION_RECORD_STOP){

                            }else if (action == RequestRecordScreen.ACTION_RECORD_PUBLISH){
                                try {
                                    //发送打点事件
                                    Event event = Event.getEvent(EventCode.DATA_RECORD_PUBLISH_SUCCTOAST, mPkgName );
                                    event.setPadcode(mPadcode);
                                    MobclickAgent.sendEvent(event);
                                }catch (Exception e){
                                }

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
                            try {
                                //发送打点事件
                                Event event = Event.getEvent(EventCode.DATA_RECORD_API_ERR, mPkgName );
                                event.setErrMsg(err);
                                event.setPadcode(mPadcode);
                                MobclickAgent.sendEvent(event);
                            }catch (Exception e){
                            }

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
                    .execute(mPadcode, mPkgName, data, clientInfo);
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
            try {
                //发送打点事件
                Event event = Event.getEvent(EventCode.DATA_RECORD_MINTIME_DISPLAY, mPkgName );
                event.setPadcode(mPadcode);
                MobclickAgent.sendEvent(event);
            }catch (Exception e){
            }

            TToast.showCenterToast(getContext(), "录制时长少于"+minTimeLen+"秒\n无法发布视频", Toast.LENGTH_LONG);
            return;
        }

        finishTimeText.setText("视频时长：" + getTimeStr(second));

        mFinishLayout.setVisibility(VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.kp_view_enter_bottom);
        mFinishLayout.startAnimation(animation);

        try {
            //发送打点事件
            Event event = Event.getEvent(EventCode.DATA_RECORD_FINISHED_DISPLAY, mPkgName );
            event.setPadcode(mPadcode);
            MobclickAgent.sendEvent(event);
        }catch (Exception e){
        }
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

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
