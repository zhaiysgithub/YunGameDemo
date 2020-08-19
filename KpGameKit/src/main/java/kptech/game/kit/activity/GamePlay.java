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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kptech.game.kit.APICallback;
import kptech.game.kit.APIConstants;
import kptech.game.kit.DeviceControl;
import kptech.game.kit.GameBox;
import kptech.game.kit.GameBoxManager;
import kptech.game.kit.GameInfo;
import kptech.game.kit.R;
import kptech.game.kit.activity.hardware.HardwareManager;
import kptech.game.kit.ad.IAdCallback;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.view.FloatMenuView;


public class GamePlay extends Activity implements APICallback<String>, DeviceControl.PlayListener, View.OnClickListener {

    public static final String EXTRA_CORPID = "extra.corpid";
    public static final String EXTRA_GAME = "extra.game";
    public static final String EXTRA_TIMEOUT = "extra.timeout";

    private Logger logger = new Logger("GamePlay");

    private static final int MSG_SHOW_ERROR = 1;

    private TextView mLoadingText;
    private FrameLayout mVideoContainer;
    private FloatMenuView mMenuView;
    private FrameLayout mLoadingLL;
    private ViewGroup mErrorLL;
    private TextView mErrorText;
    private ImageView mGameIcon;
    private Button mErrorBtn;

    private HardwareManager mHardwareManager;

    private long mBackClickTime;
    private DeviceControl mDeviceControl;

    private GameInfo mGameInfo;
    private String mCorpID;

    private long fontTimeout = 5 * 60;
    private long backTimeout = 3 * 60;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (isFinishing()) {
                return;
            }
            switch (msg.what) {
                case MSG_SHOW_ERROR:
                    showError((String) msg.obj);
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
        long[] times = getIntent().getLongArrayExtra(EXTRA_TIMEOUT);
        if (times!=null && times.length == 2){
            fontTimeout = times[0] > 60 ? times[0] : 5 * 60;
            backTimeout = times[1] > 60 ? times[1] : 3 * 60;
        }

        initView();
        mHardwareManager = new HardwareManager(this);

        //未获取到游戏信息
        if (mCorpID == null || mGameInfo == null){
            mHandler.sendMessage(Message.obtain(mHandler, MSG_SHOW_ERROR, "获取游戏信息失败"));
            return;
        }

        checkAndRequestPermission();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_back){
            finish();
        }else if(view.getId() == R.id.btn_down){
            if (view.getTag()!=null && view.getTag() instanceof String){
                String tag = (String) view.getTag();
                if ("reload".equals(tag)){
                    //重试逻辑
                    reloadGame();

                }else if("down".equals(tag)){
                    //下载逻辑
                    Toast.makeText(this, "未获取到下载地址", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void initView() {
        mLoadingLL = (FrameLayout) findViewById(R.id.loading_ll);
        mLoadingText = (TextView) findViewById(R.id.loading_txt);
        mMenuView = (FloatMenuView) findViewById(R.id.float_menu);
        mVideoContainer = (FrameLayout) findViewById(R.id.play_container);
        mErrorLL = findViewById(R.id.error_ll);
        mErrorText = findViewById(R.id.error_text);
        mGameIcon = findViewById(R.id.game_icon);
        mErrorBtn = findViewById(R.id.btn_down);
        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.btn_down).setOnClickListener(this);
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
                        mHandler.sendMessage(Message.obtain(mHandler, MSG_SHOW_ERROR, "初始化游戏失败,请稍后再试"));
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
                                if (mDeviceControl != null) {
                                    mDeviceControl.stopGame();
                                }
                            }
                        }else {
                            logger.error("申请试玩设备失败,code = " + code);
//                            exitPlay();
                            if (mDeviceControl != null) {
                                mDeviceControl.stopGame();
                            }

                            mHandler.sendMessage(Message.obtain(mHandler, MSG_SHOW_ERROR, getErrorText(code)));
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

        //设置前后台无操作超时时间
        mDeviceControl.setNoOpsTimeout(fontTimeout, backTimeout);
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
            if (mDeviceControl!=null){
                mDeviceControl.setPlayListener(null);
                mDeviceControl.stopGame();
            }

            logger.info(msg);

            //取消游戏
            if (code == APIConstants.ERROR_GAME_CANCEL){
                exitPlay();
                return;
            }

            mHandler.sendMessage(Message.obtain(mHandler, MSG_SHOW_ERROR, getErrorText(code)));

        }
    }

    private void playSuccess() {
        mLoadingLL.setVisibility(View.GONE);
        mVideoContainer.setVisibility(View.VISIBLE);
        mMenuView.setVisibility(View.VISIBLE);
        mMenuView.setDeviceControl(mDeviceControl);
    }

    private void exitPlay() {
        setResult(RESULT_OK, getIntent());
        finish();
    }

    private void showError(String err){
        mLoadingLL.setVisibility(View.GONE);
        mVideoContainer.setVisibility(View.GONE);
        mMenuView.setVisibility(View.GONE);
        mErrorLL.setVisibility(View.VISIBLE);
        mErrorText.setText(err);
        if (this.mGameInfo!=null){
            mErrorBtn.setVisibility(View.VISIBLE);
            if (this.mGameInfo.iconUrl!=null){
                Picasso.with(this).load(this.mGameInfo.iconUrl).into(mGameIcon);
            }
            if (this.mGameInfo.downloadUrl != null){
                //显示下载按钮
                mErrorBtn.setTag("down");
                mErrorBtn.setText("下载游戏直接玩");
            }else {
                //显示重试按钮
                mErrorBtn.setTag("reload");
                mErrorBtn.setText("重新加载游戏");
            }
        }else {
            //隐藏按钮
            mErrorBtn.setVisibility(View.GONE);
            mErrorBtn.setTag("hide");
        }

    }

    private void reloadGame(){
        mLoadingLL.setVisibility(View.VISIBLE);
        mVideoContainer.setVisibility(View.GONE);
        mMenuView.setVisibility(View.GONE);
        mErrorLL.setVisibility(View.GONE);

        checkAndRequestPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDeviceControl != null) {
            mDeviceControl.stopGame();
        }
        GameBoxManager.getInstance(GamePlay.this).exitQueue();
        mMenuView.dismissMenuDialog();
    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - mBackClickTime) > 3000) {
            mBackClickTime = System.currentTimeMillis();
            Toast.makeText(this, "再按一次退出游戏", Toast.LENGTH_SHORT).show();
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
        logger.error("onNoOpsTimeout() type = " + type + ", timeout = " + timeout);

        exitPlay();
        Toast.makeText(this, String.format("[%s]无操作超时 %ds 退出！", type == 1 ? "后台" : "前台", timeout), Toast.LENGTH_LONG).show();

        return true;
    }

    @Override
    public void onScreenChange(int orientation) {
        logger.error("onScreenChange() orientation = " + orientation);
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

    private String getErrorText(int code){
        String error = null;
        switch (code) {
            case APIConstants.ERROR_GAME_INF_EMPTY:
                error = "未获取到游戏信息";
                break;
            case APIConstants.ERROR_SDK_INIT_ERROR:
                error = "初始化游戏失败";
                break;
            case APIConstants.ERROR_API_CALL_ERROR:
                error = "调用服务出错，请稍后再试";
                break;
            case APIConstants.ERROR_NO_DEVICE:
            case APIConstants.WAITING_QUEUE:
                error = "试玩人数过多，请稍后再试";
                break;
            case APIConstants.ERROR_NETWORK_ERROR:
                error = "网络错误，请检查网络后再试";
                break;
            default:
                error = "服务异常，请稍后再试";
                break;
        }

        return error;
    }
}
