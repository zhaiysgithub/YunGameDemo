package kptech.game.kit.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kptech.game.kit.APICallback;
import kptech.game.kit.APIConstants;
import kptech.game.kit.DeviceControl;
import kptech.game.kit.GameBox;
import kptech.game.kit.GameBoxManager;
import kptech.game.kit.GameInfo;
import kptech.game.kit.QueueRankInfo;
import kptech.game.kit.R;
import kptech.game.kit.activity.hardware.HardwareManager;
import kptech.game.kit.ad.IAdCallback;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.view.CountDownRingView;
import kptech.game.kit.view.FloatMenuView;


public class GamePlay extends Activity implements APICallback<String>,
        View.OnClickListener, DeviceControl.PlayListener {

    public static final String EXTRA_CORPID = "extra.corpid";
    public static final String EXTRA_GAME = "extra.game";

    private static final String TAG = "GameRunningActivity";

    private Logger logger = new Logger(TAG);

    private static final int MSG_TIME_REMIND = 2;
    private static final int MSG_TIME_REMIND_DISAPPEAR = 3;
    private static final int MSG_TIME_UP = 4;
    private static final int MSG_REMIND_PLAYING_DOWNLOAD = 5;

    private TextView mLoadingText;
    private FrameLayout mVideoContainer;
    private FloatMenuView mMenuView;
    private TextView mTimeoutWarning;
    private FrameLayout mLoadingLL;

//    private View mPlayingDownloadArea;
//    private Dialog mDownloadDialog;
    private CountDownRingView mCountView;
    private HardwareManager mHardwareManager;

    private long mBackClickTime;
    private DeviceControl mDeviceControl;
    private GameInfo mGameInfo;
    private String mCorpID;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isFinishing()) {
                return;
            }
            switch (msg.what) {
                case MSG_TIME_UP:
                    if (mDeviceControl != null) {
                        mDeviceControl.stopGame();
                    }
//                    showDownloadDialog();
                    break;
                case MSG_TIME_REMIND:
                    mCountView.setVisibility(View.VISIBLE);
                    mCountView.setCount(msg.arg1);
                    mCountView.start();
                    showTimeoutWarning(msg.arg1);
                    break;
                case MSG_TIME_REMIND_DISAPPEAR:
                    disappearTimeoutWarning();
                    break;
                case MSG_REMIND_PLAYING_DOWNLOAD:
//                    mPlayingDownloadArea.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_game_play);
        mCorpID = getIntent().getStringExtra(EXTRA_CORPID);
        mGameInfo = getIntent().getParcelableExtra(EXTRA_GAME);

        initView();
        mHardwareManager = new HardwareManager(this);

        //未获取到游戏信息
        if (mCorpID == null || mGameInfo == null){
//            Toast.makeText(GamePlay.this,"获取游戏信息失败", Toast.LENGTH_LONG).show();
            logger.error("获取游戏信息失败");
            finish();
            return;
        }

        checkAndRequestPermission();
    }

    @SuppressLint("WrongViewCast")
    private void initView() {
        mLoadingLL = (FrameLayout) findViewById(R.id.loading_ll);
        mMenuView = (FloatMenuView) findViewById(R.id.float_menu);
        mLoadingText = (TextView) findViewById(R.id.loading_txt);
        mVideoContainer = (FrameLayout) findViewById(R.id.play_container);
        mTimeoutWarning = (TextView) findViewById(R.id.timeout_tips);
//        mPlayingDownloadArea = findViewById(R.id.playing_download_area);
        mCountView = (CountDownRingView) findViewById(R.id.time_count);
        findViewById(R.id.playing_download_close).setOnClickListener(this);
        findViewById(R.id.playing_download).setOnClickListener(this);
    }

    private void checkInitCloudPhoneSDK(){
        //判断是否已经初始化
        if (!GameBoxManager.getInstance(this).isGameBoxManagerInited()){
            //初始化
            mLoadingText.setText("正在初始化云手机");
            GameBoxManager.getInstance(this).init(getApplication(), this.mCorpID, new IAdCallback<String>() {
                @Override
                public void onCallback(String msg, int code) {
                    if (code == 1){
                        startCloudPhone();
                    }else {
                        //初始化失败，退出页面
//                        Toast.makeText(GamePlay.this,"初始化游戏失败", Toast.LENGTH_LONG).show();
                        logger.error("初始化游戏失败,code = " + code + ", msg = " + msg);
                        exitPlay();
                    }
                }
            });
            return;
        }

        //启动云手机
        startCloudPhone();
    }

    private void startCloudPhone() {
        mLoadingText.setText("正在加载云手机");
        GameBoxManager.getInstance(this).applyCloudDevice(mGameInfo, false, new APICallback<DeviceControl>() {
            @Override
            public void onAPICallback(DeviceControl deviceControl, final int code) {
                mDeviceControl = deviceControl;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (code == APIConstants.APPLY_DEVICE_SUCCESS) {
                            if (!isFinishing()) {
                                startGame();
                            } else {
                                // 如果界面推出之后才收到回调，请调用这个方法
                                mDeviceControl.stopGame();
                            }
                        }else {
                            logger.error("申请试玩设备失败,code = " + code);
                            exitPlay();
                        }
                    }
                });
            }
        });

    }

    private void startGame() {
        mDeviceControl.registerSensorSamplerListener(new DeviceControl.SensorSamplerListener() {
            @Override
            public void onSensorSamper(int sensor, int state) {
//                if (sensor < 210) {
//                    return;
//                }
                logger.info("onSensorSamper = " + sensor + "  state = " + state);
                mHardwareManager.registerHardwareState(sensor, state);
            }
        });
        mHardwareManager.setDeviceControl(mDeviceControl);
        mDeviceControl.startGame(GamePlay.this, R.id.play_container, GamePlay.this);
        // setRemind();
        setNoOpsTimeout();
    }

    private void setNoOpsTimeout() {
        mDeviceControl.setNoOpsTimeout(5 * 60, 3 * 60);
    }

    private void setRemind() {
        Message message = mHandler.obtainMessage(MSG_TIME_REMIND);
        if (mGameInfo.getEffectTime() > 30) {
            message.arg1 = 30;
            mHandler.sendMessageDelayed(message, (mGameInfo.getEffectTime() - 30) * 1000);
        } else if (mGameInfo.getEffectTime() > 2) {
            message.arg1 = mGameInfo.getEffectTime();
            mHandler.sendMessage(message);
        }

        // 发送消息，20s后弹出变下边玩按钮
        // mHandler.sendEmptyMessageDelayed(MSG_REMIND_PLAYING_DOWNLOAD, 20 * 1000);
        // 发送消息，游戏结束后弹出下载弹窗
        mHandler.sendEmptyMessageDelayed(MSG_TIME_UP, mGameInfo.getEffectTime() * 1000);
    }

    private void playSuccess() {
        mLoadingLL.setVisibility(View.GONE);
        mVideoContainer.setVisibility(View.VISIBLE);
        mMenuView.setVisibility(View.VISIBLE);
        mMenuView.setDeviceControl(mDeviceControl);
    }

    @Override
    public void onAPICallback(String msg, int code) {
        if (msg != null) {
            logger.info("apiResult = " + msg);
        }
        if (code == APIConstants.CONNECT_DEVICE_SUCCESS || code == APIConstants.RECONNECT_DEVICE_SUCCESS) {
            mDeviceControl.setPlayListener(this);
            playSuccess();
        } else {
            mDeviceControl.setPlayListener(null);
            exitPlay();

            logger.info(msg);
//            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }

    private void exitPlay() {
        setResult(RESULT_OK, getIntent());
        finish();
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.playing_download:
//                downloadApp(mGameInfo.pkgName);
//                mPlayingDownloadArea.setVisibility(View.GONE);
//                break;
//            case R.id.playing_download_close:
//                mPlayingDownloadArea.setVisibility(View.GONE);
//                break;
//            default:
//                break;
//        }
    }

    private void downloadApp(String pkgName) {
        //TODO 添加下载逻辑
    }

    private void showTimeoutWarning(int second) {
        mTimeoutWarning.setText(getString(R.string.game_play_time_remind, second));
        Animator warningIn = ObjectAnimator.ofFloat(mTimeoutWarning, "alpha", 0f, 1f);
        warningIn.setDuration(200);
        warningIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mTimeoutWarning.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mHandler.sendEmptyMessageDelayed(MSG_TIME_REMIND_DISAPPEAR, 2000);
            }
        });
        warningIn.start();
    }

    private void disappearTimeoutWarning() {
        Animator warningOut = ObjectAnimator.ofFloat(mTimeoutWarning, "alpha", 1f, 0f);
        warningOut.setDuration(200);
        warningOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mTimeoutWarning.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mTimeoutWarning.setVisibility(View.GONE);
            }
        });

        warningOut.start();
    }

//    private void showDownloadDialog() {
//        if (isFinishing()) {
//            return;
//        }
//
//        if (mDownloadDialog != null && mDownloadDialog.isShowing()) {
//            mDownloadDialog.dismiss();
//        }
//        mDownloadDialog = new Dialog(this, R.style.MyTheme_CustomDialog);
//        View contentView = mDownloadDialog.getLayoutInflater().inflate(R.layout.dialog_download, null, false);
//        mDownloadDialog.setContentView(contentView);
//        Glide.with(contentView.findViewById(R.id.app_icon)).load(mGameInfo.iconUrl)
//                .into((ImageView) contentView.findViewById(R.id.app_icon));
//        ((TextView) contentView.findViewById(R.id.app_name)).setText(mGameInfo.name);
//        ((TextView) contentView.findViewById(R.id.play_count)).setText(getString(R.string.game_launch_count,
//                UnitFormatUtils.formatPerson(this, mGameInfo.playCount)));
//        ((TextView) contentView.findViewById(R.id.message)).setText(getString(R.string.game_end_play_dialog_tips,
//                mGameInfo.totalTime / 60));
//        Button okBtn = (Button) contentView.findViewById(R.id.download_btn);
//        okBtn.setText(
//                getString(R.string.game_end_play_dialog_download, UnitFormatUtils.formatBytes(mGameInfo.size, false)));
//        okBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                downloadApp(mGameInfo.pkgName);
//            }
//        });
//        contentView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!GameRunningActivity.this.isFinishing()) {
//                    exitPlay();
//                }
//            }
//        });
//        mDownloadDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                exitPlay();
//            }
//        });
//        mDownloadDialog.setCanceledOnTouchOutside(false);
//        mDownloadDialog.show();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDeviceControl != null) {
            mDeviceControl.stopGame();
        }
        GameBoxManager.getInstance(GamePlay.this).exitQueue();
        mMenuView.dismissMenuDialog();
        if (mHardwareManager != null) {
            mHardwareManager.release();
        }
    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - mBackClickTime) > 3000) {
            mBackClickTime = System.currentTimeMillis();
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
        } else {
            exitPlay();
        }
    }

    @Override
    public void onPingUpdate(int ping) {
        if (mMenuView != null) {
            mMenuView.onPingChanged(ping);
        }
    }

    @Override
    public boolean onNoOpsTimeout(int type, long timeout) {
        Log.d(TAG, "onNoOpsTimeout() type = " + type + ", timeout = " + timeout);
        exitPlay();
        Toast.makeText(this, String.format("[%s]无操作超时 %ds 退出！", type == 1 ? "后台" : "前台", timeout), Toast.LENGTH_LONG).show();
        return true;
    }

    @Override
    public void onScreenChange(int orientation) {
        Log.d(TAG, "onScreenChange() orientation = " + orientation);
    }


    private void setFullScreen() {
        if (Build.VERSION.SDK_INT < 14) {
            return;
        }
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            if (Build.VERSION.SDK_INT < 16) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            this.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                }
            });
        } catch (Exception e) {
            Log.w("PlayActivity", e);
        }
    }


    private static final int CODE_REQUEST_PERMISSION = 1024;
    private void checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT < 23) {
//            startCloudPhone();
            checkInitCloudPhoneSDK();
            return;
        }

        List<String> lackedPermission = new ArrayList();
        if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!(checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (lackedPermission.size() != 0) {
            String[] requestPermissions = new String[lackedPermission.size()];
            lackedPermission.toArray(requestPermissions);
            requestPermissions(requestPermissions, CODE_REQUEST_PERMISSION);
        } else {
//            startCloudPhone();
            checkInitCloudPhoneSDK();
        }
    }

    private boolean hasAllPermissionsGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != CODE_REQUEST_PERMISSION) return;

//        startCloudPhone();
        checkInitCloudPhoneSDK();
    }
}
