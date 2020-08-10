package com.yd.yunapp.gamebox.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kuaipan.game.demo.R;

import kptech.game.kit.APIConstants;
import kptech.game.kit.DeviceControl;

public class FloatMenuView extends FrameLayout implements View.OnClickListener {

    private static final String TAG = "FloatMenuView";

    private FloatingBarView mMenuIcon;
    private Button mHDBtn;
    private Button mOrdianryBtn;
    private Button mLsBtn;
    private Button mAutoBtn;
    private TextView mPlayBtn;
    private TextView mExitBtn;
    private TextView mAduioBtn;

    private Dialog mMenuDialog;
    private View mMenuDialogContentView;
    private DeviceControl mDeviceControl;
    private boolean mAudioSwitch = true;

    public FloatMenuView(@NonNull Context context) {
        super(context);
        initView();
    }

    public FloatMenuView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.cloud_phone_float_menu, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMenuIcon = (FloatingBarView) findViewById(R.id.menu_icon);
        mMenuIcon.initIcons(new int[] {120, 180}, new int[] {R.color.gb_floating_color_green,
                R.color.gb_floating_color_yellow, R.color.gb_floating_color_red});

        mMenuDialog = new Dialog(getContext(), R.style.MyTheme_CustomDialog_MenuDialog);
        mMenuDialogContentView = mMenuDialog.getLayoutInflater().inflate(R.layout.cloud_phone_float_menu_panel,
                null, false);
        mMenuDialog.setContentView(mMenuDialogContentView);
        mMenuDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mMenuIcon.setVisibility(VISIBLE);
            }
        });
        mMenuDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mMenuIcon.setVisibility(GONE);
            }
        });
        mMenuDialog.setCanceledOnTouchOutside(true);

        mHDBtn = (Button) mMenuDialogContentView.findViewById(R.id.clarity_hd);
        mOrdianryBtn = (Button) mMenuDialogContentView.findViewById(R.id.clarity_ordianry);
        mLsBtn = (Button) mMenuDialogContentView.findViewById(R.id.clarity_ls);
        mAutoBtn = (Button) mMenuDialogContentView.findViewById(R.id.clarity_auto);
        mPlayBtn = (TextView) mMenuDialogContentView.findViewById(R.id.play);
        mExitBtn = (TextView) mMenuDialogContentView.findViewById(R.id.exit_play);
        mAduioBtn = (TextView) mMenuDialogContentView.findViewById(R.id.audio);

        mAduioBtn.setOnClickListener(this);
        mExitBtn.setOnClickListener(this);
        mPlayBtn.setOnClickListener(this);
        mMenuIcon.setOnClickListener(this);
        mHDBtn.setOnClickListener(this);
        mOrdianryBtn.setOnClickListener(this);
        mLsBtn.setOnClickListener(this);
        mAutoBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (mMenuIcon == view) {
            mMenuDialog.show();
        } else if (mAutoBtn == view) {
            setSelectGradeLevel(APIConstants.DEVICE_VIDEO_QUALITY_AUTO);
            mMenuDialog.dismiss();
        } else if (mLsBtn == view) {
            setSelectGradeLevel(APIConstants.DEVICE_VIDEO_QUALITY_LS);
            mMenuDialog.dismiss();
        } else if (mHDBtn == view) {
            setSelectGradeLevel(APIConstants.DEVICE_VIDEO_QUALITY_HD);
            mMenuDialog.dismiss();
        } else if (mOrdianryBtn == view) {
            setSelectGradeLevel(APIConstants.DEVICE_VIDEO_QUALITY_ORDINARY);
            mMenuDialog.dismiss();
        } else if (mPlayBtn == view) {
            mMenuDialog.dismiss();
        } else if (mExitBtn == view) {
            mMenuDialog.dismiss();
            ((Activity) getContext()).finish();
        } else if (mAduioBtn == view) {
            mMenuDialog.dismiss();
            mAudioSwitch = !mAudioSwitch;
            mDeviceControl.setAudioSwitch(mAudioSwitch);
        }
    }

    public void setDeviceControl(DeviceControl deviceControl) {
        mDeviceControl = deviceControl;
    }

    private void setSelectGradeLevel(String level) {
        if (level == APIConstants.DEVICE_VIDEO_QUALITY_AUTO) {
            applyBtnStyle(mAutoBtn, R.drawable.blue_btn);
            applyBtnStyle(mHDBtn, R.drawable.translucent_btn);
            applyBtnStyle(mOrdianryBtn, R.drawable.translucent_btn);
            applyBtnStyle(mLsBtn, R.drawable.translucent_btn);
        } else if (level == APIConstants.DEVICE_VIDEO_QUALITY_LS) {
            applyBtnStyle(mLsBtn, R.drawable.blue_btn);
            applyBtnStyle(mHDBtn, R.drawable.translucent_btn);
            applyBtnStyle(mOrdianryBtn, R.drawable.translucent_btn);
            applyBtnStyle(mAutoBtn, R.drawable.translucent_btn);
        } else if (level == APIConstants.DEVICE_VIDEO_QUALITY_HD) {
            applyBtnStyle(mHDBtn, R.drawable.blue_btn);
            applyBtnStyle(mAutoBtn, R.drawable.translucent_btn);
            applyBtnStyle(mOrdianryBtn, R.drawable.translucent_btn);
            applyBtnStyle(mLsBtn, R.drawable.translucent_btn);

        } else if (level == APIConstants.DEVICE_VIDEO_QUALITY_ORDINARY) {
            applyBtnStyle(mOrdianryBtn, R.drawable.blue_btn);
            applyBtnStyle(mHDBtn, R.drawable.translucent_btn);
            applyBtnStyle(mAutoBtn, R.drawable.translucent_btn);
            applyBtnStyle(mLsBtn, R.drawable.translucent_btn);
        }
        if (mDeviceControl != null) {
            mDeviceControl.switchQuality(level);
        }
    }

    private void applyBtnStyle(Button button, int bg) {
        button.setBackgroundResource(bg);
    }

    public void dismissMenuDialog() {
        if (mMenuDialog != null && mMenuDialog.isShowing()) {
            mMenuDialog.dismiss();
        }
    }

    public void onPingChanged(int ping) {
        if (mMenuIcon != null) {
            mMenuIcon.onPingChanged(ping);
        }
    }
}
