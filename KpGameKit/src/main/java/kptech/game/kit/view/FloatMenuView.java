package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import kptech.game.kit.IDeviceControl;
import kptech.game.kit.R;

public class FloatMenuView extends FrameLayout implements View.OnClickListener {

    private static final String TAG = "FloatMenuView";

    private FloatingBarView mMenuIcon;
    private PlaySettingsView mSettingsView;

//    private Button mHDBtn;
//    private Button mOrdianryBtn;
//    private Button mLsBtn;
//    private Button mAutoBtn;
//    private TextView mPlayBtn;
//    private TextView mExitBtn;
//    private TextView mAduioBtn;
//    private TextView mResizeBtn;

//    private Dialog mMenuDialog;
//    private View mMenuDialogContentView;
//    private DeviceControl mDeviceControl;
//    private boolean mAudioSwitch = true;
    public boolean mVideoScale = true;
//
//    private int systemUiVisibility = -1;
//
    private VideoResizeListener mResizeClickListener;

    public interface VideoResizeListener{
        void onVideoResize(boolean scale);
    }

    public void setResizeClickListener(VideoResizeListener listener) {
        this.mResizeClickListener = listener;
    }

    private OnClickListener mExitListener;
    public void setOnExitClickListener(View.OnClickListener listener) {
        mExitListener = listener;
    }

    private OnClickListener mSetListener;
    public void setOnSettingsClickListener(View.OnClickListener listener) {
        mSetListener = listener;
    }

    private OnClickListener mRecordListener;
    public void setOnRecordClickListener(View.OnClickListener listener) {
        mRecordListener = listener;
    }

    public FloatMenuView(Context context) {
        super(context);
        initView();
    }

    public FloatMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.kp_view_float_menu, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMenuIcon = (FloatingBarView) findViewById(R.id.menu_icon);
        mMenuIcon.initIcons(new int[] {120, 180}, new int[] {R.color.float_menu_color_green,
                R.color.float_menu_color_yellow, R.color.float_menu_color_red});
        mMenuIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsView.show();
            }
        });

        mSettingsView = findViewById(R.id.setting_view);
        mSettingsView.setOnShowListener(new PlaySettingsView.OnShowListener() {
            @Override
            public void onShow() {
//                mMenuView.setVisibility(View.GONE);
                mMenuIcon.setVisibility(GONE);
            }
        });
        mSettingsView.setOnDismissListener(new PlaySettingsView.OnDismissListener() {
            @Override
            public void onDismiss() {
//                mMenuView.setVisibility(View.VISIBLE);
                mMenuIcon.setVisibility(VISIBLE);
            }
        });
        mSettingsView.setOnVideoScaleListener(new PlaySettingsView.OnVideoScaleListener() {
            @Override
            public void onScale(boolean scale) {
//                resizeVideoContainer(scale);
                if (mResizeClickListener!=null){
                    mResizeClickListener.onVideoResize(scale);
                }
            }
        });
        mSettingsView.setOnExitListener(new PlaySettingsView.OnExitListener() {
            @Override
            public void onExit() {
//                onBackPressed();
                if (mExitListener!=null){
                    mExitListener.onClick(null);
                }
            }
        });
        mSettingsView.setOnRecordListener(new PlaySettingsView.OnRecordListener() {
            @Override
            public void onRecord() {
                if (mRecordListener!=null){
                    mRecordListener.onClick(null);
                }
            }
        });

//        mMenuDialog = new Dialog(getContext(), R.style.MyTheme_CustomDialog);
//        mMenuDialogContentView = mMenuDialog.getLayoutInflater().inflate(R.layout.kp_dialog_float_menu_panel,
//                null, false);
//        mMenuDialog.setContentView(mMenuDialogContentView);
//        mMenuDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                mMenuIcon.setVisibility(VISIBLE);
//
//                if (systemUiVisibility != -1){
//                    try {
//                        Context ctx = getContext();
//                        if (ctx instanceof Activity){
//                            Activity activity = (Activity) ctx;
//                            activity.getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
//                        }
//                    }catch (Exception e){}
//                }
//            }
//        });
//        mMenuDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                mMenuIcon.setVisibility(GONE);
//            }
//        });
//        mMenuDialog.setCanceledOnTouchOutside(true);
//
//        mHDBtn = (Button) mMenuDialogContentView.findViewById(R.id.clarity_hd);
//        mOrdianryBtn = (Button) mMenuDialogContentView.findViewById(R.id.clarity_ordianry);
//        mLsBtn = (Button) mMenuDialogContentView.findViewById(R.id.clarity_ls);
//        mAutoBtn = (Button) mMenuDialogContentView.findViewById(R.id.clarity_auto);
//        mPlayBtn = (TextView) mMenuDialogContentView.findViewById(R.id.play);
//        mExitBtn = (TextView) mMenuDialogContentView.findViewById(R.id.exit_play);
//        mAduioBtn = (TextView) mMenuDialogContentView.findViewById(R.id.audio);
//        mResizeBtn = (TextView) mMenuDialogContentView.findViewById(R.id.resize);
//
//        mAduioBtn.setOnClickListener(this);
//        mExitBtn.setOnClickListener(this);
//        mPlayBtn.setOnClickListener(this);
//        mMenuIcon.setOnClickListener(this);
//        mHDBtn.setOnClickListener(this);
//        mOrdianryBtn.setOnClickListener(this);
//        mLsBtn.setOnClickListener(this);
//        mAutoBtn.setOnClickListener(this);
//        mResizeBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
//        if (mMenuIcon == view) {
//            systemUiVisibility = -1;
//            try {
//                Context ctx = getContext();
//                if (ctx instanceof Activity){
//                    Activity activity = (Activity) ctx;
//                    systemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
//                }
//            }catch (Exception e){}
//            mMenuDialog.show();
//        }
//        else if (mAutoBtn == view) {
//            setSelectGradeLevel(APIConstants.DEVICE_VIDEO_QUALITY_AUTO);
//            mMenuDialog.dismiss();
//        } else if (mLsBtn == view) {
//            setSelectGradeLevel(APIConstants.DEVICE_VIDEO_QUALITY_LS);
//            mMenuDialog.dismiss();
//        } else if (mHDBtn == view) {
//            setSelectGradeLevel(APIConstants.DEVICE_VIDEO_QUALITY_HD);
//            mMenuDialog.dismiss();
//        } else if (mOrdianryBtn == view) {
//            setSelectGradeLevel(APIConstants.DEVICE_VIDEO_QUALITY_ORDINARY);
//            mMenuDialog.dismiss();
//        } else if (mPlayBtn == view) {
//            mMenuDialog.dismiss();
//        } else if (mExitBtn == view) {
//            mMenuDialog.dismiss();
//            if (mExitListener!=null){
//                mExitListener.onClick(view);
//            }
//        } else if (mAduioBtn == view) {
//            mMenuDialog.dismiss();
//            mAudioSwitch = !mAudioSwitch;
//            mDeviceControl.setAudioSwitch(mAudioSwitch);
//            setSoundStyle(mAudioSwitch);
//        } else if (mResizeBtn == view){
//            mMenuDialog.dismiss();
//            mVideoScale = !mVideoScale;
//            if (mResizeClickListener!=null){
//                mResizeClickListener.onVideoResize(mVideoScale);
//            }
//            setResizeStyle(mVideoScale);
//        }
    }
//
//    private void setResizeStyle(boolean scale){
//        mResizeBtn.setText(scale ? "全屏显示画面":"按比例显示画面");
//    }
//
//    private void fullScreenImmersive(View view) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
//            view.setSystemUiVisibility(uiOptions);
//        }
//    }
//
    public void setDeviceControl(IDeviceControl deviceControl) {
        mSettingsView.setDeviceControl(deviceControl);
//        mDeviceControl = deviceControl;
//
//        if (mDeviceControl!=null){
//            String videoLevel = mDeviceControl.getVideoQuality();
//            if (!StringUtil.isEmpty(videoLevel)){
//                setSelectGradeLevel(videoLevel);
//            }
//            mAudioSwitch = mDeviceControl.isSoundEnable();
//            setSoundStyle(mAudioSwitch);
//        }
    }
//
//    private void setSoundStyle(boolean sound){
//        mAduioBtn.setText(sound ? "关闭声音":"打开声音");
//    }
//
//    private void setSelectGradeLevel(String level) {
//        if (level == APIConstants.DEVICE_VIDEO_QUALITY_AUTO) {
//            applyBtnStyle(mAutoBtn, R.drawable.kp_blue_btn);
//            applyBtnStyle(mHDBtn, R.drawable.kp_translucent_btn);
//            applyBtnStyle(mOrdianryBtn, R.drawable.kp_translucent_btn);
//            applyBtnStyle(mLsBtn, R.drawable.kp_translucent_btn);
//        } else if (level == APIConstants.DEVICE_VIDEO_QUALITY_LS) {
//            applyBtnStyle(mLsBtn, R.drawable.kp_blue_btn);
//            applyBtnStyle(mHDBtn, R.drawable.kp_translucent_btn);
//            applyBtnStyle(mOrdianryBtn, R.drawable.kp_translucent_btn);
//            applyBtnStyle(mAutoBtn, R.drawable.kp_translucent_btn);
//        } else if (level == APIConstants.DEVICE_VIDEO_QUALITY_HD) {
//            applyBtnStyle(mHDBtn, R.drawable.kp_blue_btn);
//            applyBtnStyle(mAutoBtn, R.drawable.kp_translucent_btn);
//            applyBtnStyle(mOrdianryBtn, R.drawable.kp_translucent_btn);
//            applyBtnStyle(mLsBtn, R.drawable.kp_translucent_btn);
//        } else if (level == APIConstants.DEVICE_VIDEO_QUALITY_ORDINARY) {
//            applyBtnStyle(mOrdianryBtn, R.drawable.kp_blue_btn);
//            applyBtnStyle(mHDBtn, R.drawable.kp_translucent_btn);
//            applyBtnStyle(mAutoBtn, R.drawable.kp_translucent_btn);
//            applyBtnStyle(mLsBtn, R.drawable.kp_translucent_btn);
//        }
//        if (mDeviceControl != null) {
//            mDeviceControl.switchQuality(level);
//        }
//    }
//
//    private void applyBtnStyle(Button button, int bg) {
//        button.setBackgroundResource(bg);
//    }

    public void dismissMenuDialog() {
//        if (mMenuDialog != null && mMenuDialog.isShowing()) {
//            mMenuDialog.dismiss();
//        }
        mSettingsView.dismiss();
    }

    public void onPingChanged(int ping) {
        if (mMenuIcon != null) {
            mMenuIcon.onPingChanged(ping);
        }
    }
}
