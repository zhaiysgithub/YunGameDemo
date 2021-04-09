package kptech.game.kit.view;

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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

import kptech.game.kit.APIConstants;
import kptech.game.kit.BuildConfig;
import kptech.game.kit.GameBoxManager;
import kptech.game.kit.IDeviceControl;
import kptech.game.kit.R;
import kptech.game.kit.utils.DensityUtil;
import kptech.game.kit.utils.StringUtil;
import kptech.game.kit.env.Env;
import kptech.lib.conf.RecordScreenConfig;

public class PlaySettingsView extends LinearLayout {

    private IDeviceControl mDeviceControl;
    private int mSize = 0;

    private ViewGroup mLayout;
    private RadioGroup mVideoSizeGroup;
    private RadioGroup mVideoQualityGroup;
    private CheckBox mAudioCheckbox;

    private ViewGroup mRecordItem;
    private TextView mPadcodeTv;

    private String mVideoQuality;
    private boolean mAudioSwitch;
    public boolean mVideoScale = true;

    long verTime;
    int verClickCount;

    public interface OnDismissListener {
        void onDismiss();
    }

    public interface OnShowListener {
        void onShow();
    }

    public interface OnVideoScaleListener {
        void onScale(boolean scale);
    }

    public interface OnExitListener {
        void onExit();
    }

    public interface OnRecordListener{
        void onRecord();
    }

    private OnDismissListener mOnDismissListener;
    public void setOnDismissListener (OnDismissListener listener){
        this.mOnDismissListener = listener;
    }

    private OnShowListener mOnShowListener;
    public void setOnShowListener (OnShowListener listener){
        this.mOnShowListener = listener;
    }

    private OnVideoScaleListener mOnVideoScaleListener;
    public void setOnVideoScaleListener(OnVideoScaleListener listener) {
        mOnVideoScaleListener = listener;
    }

    private OnExitListener mOnExitListener;
    public void setOnExitListener(OnExitListener listener) {
        mOnExitListener = listener;
    }

    private OnRecordListener mOnRecordListener;
    public void setOnRecordListener(OnRecordListener listener){
        mOnRecordListener = listener;
    }

    public PlaySettingsView(Context context) {
        super(context);
        initView();
    }

    public PlaySettingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void setDeviceControl(IDeviceControl deviceControl) {
        mDeviceControl = deviceControl;
        if (mDeviceControl != null){
            initAudioEnable();
            initVideoQuality();

            if (Env.isTestEnv()){
                try {
                    mPadcodeTv.setText(deviceControl.getPadcode());
                }catch (Exception e){}
            }else {
                mPadcodeTv.setVisibility(GONE);
            }


            //获取录屏配置
            RecordScreenConfig config = RecordScreenConfig.getConfig(getContext());
            if (config != null && config.disable){
                mRecordItem.setVisibility(GONE);
            }else {
                mRecordItem.setVisibility(VISIBLE);
            }

        }
    }

    private void initAudioEnable(){
        mAudioSwitch = mDeviceControl.isSoundEnable();
        if (mAudioCheckbox.isEnabled() != mAudioSwitch){
            mAudioCheckbox.setEnabled(mAudioSwitch);
        }
    }

    private void initVideoQuality(){
        if (mVideoQuality == null && mDeviceControl != null){
            mVideoQuality = mDeviceControl.getVideoQuality();
            if (mVideoQuality != null){
                if (mVideoQuality.equals(APIConstants.DEVICE_VIDEO_QUALITY_AUTO)) {
                    mVideoQualityGroup.check(R.id.video_quality_auto);
                } else if (mVideoQuality.equals(APIConstants.DEVICE_VIDEO_QUALITY_LS)) {
                    mVideoQualityGroup.check(R.id.video_quality_ls);
                } else if (mVideoQuality.equals(APIConstants.DEVICE_VIDEO_QUALITY_HD)) {
                    mVideoQualityGroup.check(R.id.video_quality_hd);
                } else if (mVideoQuality.equals(APIConstants.DEVICE_VIDEO_QUALITY_ORDINARY)) {
                    mVideoQualityGroup.check(R.id.video_quality_ordinary);
                }
            }
        }
    }

    private void initView() {
        View view = inflate(getContext(), R.layout.kp_view_play_settings, this);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mLayout = view.findViewById(R.id.layout);
        mLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                lastOptTime = System.currentTimeMillis();
            }
        });

        view.findViewById(R.id.record_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mOnRecordListener!=null){
                    mOnRecordListener.onRecord();
                }
            }
        });

        TextView sdkVer = view.findViewById(R.id.sdk_ver);
        sdkVer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                long timeNew = System.currentTimeMillis();
                if ((timeNew - verTime) < 1000){
                    verClickCount += 1;
                }else {
                    verClickCount = 1;
                }

                verTime = timeNew;
                if (verClickCount == 8){
                    //打开log
                    GameBoxManager.setDebug(true);
                    Toast.makeText(getContext(), "log", Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (Env.isTestEnv()){
            sdkVer.setText("Version: "+BuildConfig.VERSION_NAME+"_"+BuildConfig.VERSION_CODE);
        }else {
            sdkVer.setText("    ");
        }

        int height = DensityUtil.getScreenHeight(getContext());
        int width = DensityUtil.getScreenWidth(getContext());
        mSize = height > width ? height / 2 : width / 2;

        updateLayout();

        mVideoSizeGroup = view.findViewById(R.id.video_size_group);
        if (mVideoScale) {
            mVideoSizeGroup.check(R.id.video_size_scale);
        }else {
            mVideoSizeGroup.check(R.id.video_size_full);
        }

        mVideoSizeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                lastOptTime = System.currentTimeMillis();

                if (mOnVideoScaleListener!=null){
                    mVideoScale = checkedId==R.id.video_size_scale ? true : false;
                    mOnVideoScaleListener.onScale(mVideoScale);
                }
            }
        });
        mVideoQualityGroup = view.findViewById(R.id.video_quality_group);
        mVideoQualityGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                lastOptTime = System.currentTimeMillis();

                String level = null;
                if (checkedId == R.id.video_quality_auto) {
                    level = APIConstants.DEVICE_VIDEO_QUALITY_AUTO;
                }else if (checkedId == R.id.video_quality_ordinary){
                    level = APIConstants.DEVICE_VIDEO_QUALITY_ORDINARY;
                }else if (checkedId == R.id.video_quality_ls){
                    level = APIConstants.DEVICE_VIDEO_QUALITY_LS;
                }else if (checkedId == R.id.video_quality_hd){
                    level = APIConstants.DEVICE_VIDEO_QUALITY_HD;
                }
                if (level != null){
                    setSelectQualityLevel(level);
                }
            }
        });
        mAudioCheckbox = view.findViewById(R.id.sound_checkbox);
        mAudioCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                lastOptTime = System.currentTimeMillis();

                mAudioSwitch = isChecked;
                if (mDeviceControl != null){
                    mDeviceControl.setAudioSwitch(mAudioSwitch);
                }
            }
        });
        Button exitBtn = view.findViewById(R.id.exit_btn);
        exitBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mOnExitListener != null){
                    mOnExitListener.onExit();
                }
            }
        });
        exitBtn.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    if (mDeviceControl != null){
                        String info = mDeviceControl.getDeviceInfo();
                        StringUtil.copy(getContext(), info);
                        if (Env.isTestEnv()){
                            Toast.makeText(getContext(), "info", Toast.LENGTH_SHORT).show();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }
        });
        view.findViewById(R.id.send_back_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mDeviceControl != null){
                    mDeviceControl.sendPadKey(APIConstants.PAD_KEY_BACK);
                }

            }
        });
        mRecordItem = findViewById(R.id.record_item);
        mPadcodeTv = findViewById(R.id.tv_padcode);
        mPadcodeTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mDeviceControl != null){
                        String info = mDeviceControl.getPadcode();
                        StringUtil.copy(getContext(), info);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void setSelectQualityLevel(String level) {
        if (level == null){
            return;
        }
        if (mDeviceControl != null) {
            mDeviceControl.switchQuality(level);
            mVideoQuality = level;
        }
    }

    public void show(){
        enter();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
    }

    public void dismiss(){
        bottomExit();

        if (mHandler != null){
            mHandler.removeMessages(1);
            mHandler = null;
        }
    }

    private long lastOptTime;

    private long getLastOptTime(){
        return lastOptTime;
    }

    private TimeoutHandler mHandler;
    private void enter() {
        if (this.getVisibility() == View.VISIBLE){
            return;
        }

        this.setVisibility(VISIBLE);
        if (mOnShowListener != null){
            mOnShowListener.onShow();
        }

        lastOptTime = System.currentTimeMillis();
        if (mHandler == null){
            mHandler = new TimeoutHandler(this);
        }
        mHandler.sendEmptyMessageDelayed(1, 5000);

        int animRes = 0;
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            animRes = R.anim.kp_view_enter_left;
        }else {
            animRes = R.anim.kp_view_enter_bottom;
        }

        Animation animation = AnimationUtils.loadAnimation(getContext(), animRes);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        this.startAnimation(animation);
    }

    private void bottomExit(){
        if (this.getVisibility() != View.VISIBLE){
            return;
        }

        int animRes = 0;
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            animRes = R.anim.kp_view_exit_left;
        }else {
            animRes = R.anim.kp_view_exit_bottom;
        }

        Animation animation = AnimationUtils.loadAnimation(getContext(), animRes);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setVisibility(GONE);
                if (mOnDismissListener != null){
                    mOnDismissListener.onDismiss();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        this.startAnimation(animation);
    }

    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        updateLayout();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLayout();
    }

    private int curOrientation = 0;
    private void updateLayout(){
        if (curOrientation == this.getResources().getConfiguration().orientation){
            return;
        }
        curOrientation = this.getResources().getConfiguration().orientation;


        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            if (mSize < DensityUtil.dip2px(getContext(),360)){
                mSize = DensityUtil.dip2px(getContext(),360);
            }
            //横屏
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mSize, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            mLayout.setLayoutParams(lp);
            mLayout.setBackgroundResource(R.drawable.kp_view_settings_left_bg);
        }
        else if (this.getResources().getConfiguration().orientation ==Configuration.ORIENTATION_PORTRAIT) {
            if (mSize < DensityUtil.dip2px(getContext(),320)) {
                mSize = DensityUtil.dip2px(getContext(),320);
            }
            //竖屏
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mSize);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            mLayout.setLayoutParams(lp);
            mLayout.setBackgroundResource(R.drawable.kp_view_settings_bottom_bg);
        }
    }

    private static class TimeoutHandler extends Handler{
        WeakReference<PlaySettingsView> ref = null;
        private TimeoutHandler(PlaySettingsView obj) {
            super(Looper.getMainLooper());
            ref = new WeakReference<>(obj);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (ref == null || ref.get() == null){
                return;
            }
            if (!ref.get().isShown()){
                return;
            }
            long optTime = ref.get().getLastOptTime();
            long curTime = System.currentTimeMillis();
            if ((curTime - optTime) < 10000){
                if (ref.get().mHandler!=null){
                    ref.get().mHandler.sendEmptyMessageDelayed(1, 1000);
                }
            }else {
                ref.get().dismiss();
            }
        }
    }
}
