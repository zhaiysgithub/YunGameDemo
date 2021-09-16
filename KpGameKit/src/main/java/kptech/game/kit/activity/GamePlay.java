package kptech.game.kit.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kptech.game.kit.APICallback;
import kptech.game.kit.APIConstants;
import kptech.game.kit.IDeviceControl;
import kptech.game.kit.GameBoxManager;
import kptech.game.kit.GameInfo;
import kptech.game.kit.ParamKey;
import kptech.game.kit.Params;
import kptech.game.kit.R;
import kptech.game.kit.activity.hardware.HardwareManager;
import kptech.game.kit.download.DownloadTask;
import kptech.game.kit.manager.UserAuthManager;
import kptech.game.kit.utils.AppUtils;
import kptech.game.kit.receiver.KPGameReceiver;
import kptech.game.kit.view.FloatRecordView;
import kptech.game.kit.view.PlayStatusLayout;
import kptech.lib.analytic.Event;
import kptech.lib.analytic.EventCode;
import kptech.lib.analytic.MobclickAgent;
import kptech.game.kit.env.Env;
import kptech.lib.constants.SharedKeys;
import kptech.lib.data.AccountTask;
import kptech.lib.data.IRequestCallback;
import kptech.lib.data.RequestGameExitListTask;
import kptech.lib.data.RequestGameInfoTask;
import kptech.game.kit.msg.BaseMsgReceiver;
import kptech.game.kit.utils.DensityUtil;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.MD5Util;
import kptech.game.kit.utils.ProferencesUtils;
import kptech.game.kit.utils.StringUtil;
import kptech.game.kit.view.FloatDownView;
import kptech.game.kit.view.FloatMenuView;


public class GamePlay extends Activity implements APICallback<String>, IDeviceControl.PlayListener {

    public static final String EXTRA_CORPID = "extra.corpid";
    public static final String EXTRA_GAME = "extra.game";
    //    public static final String EXTRA_TIMEOUT = "extra.timeout";
    public static final String EXTRA_PARAMS = "extra.params";
    public static final String EXTRA_MINI_VERSION = "extra.mini.version";
    private static final int mRequestCode = 9002;

    private static final String TAG = "GamePlay";

    private static final int MSG_SHOW_ERROR = 1;
    private static final int MSG_RELOAD_GAME = 2;
    private static final int MSG_SHOW_AUTH = 3;
    private static final int MSG_GAME_EXIT = 4;

    private ViewGroup mContentView;
    private FrameLayout mVideoContainer;
    private FloatMenuView mMenuView;

    private FloatRecordView mRecordView;

    private PlayStatusLayout mPlayStatueView;

    private FloatDownView mFloatDownView;

    private HardwareManager mHardwareManager;

    private IDeviceControl mDeviceControl;

    private GameInfo mGameInfo;
    private String mCorpID;

    private long fontTimeout = 5 * 60;
    private long backTimeout = 3 * 60;

    private int mErrorCode = -1;
    private String mErrorMsg = null;
    private String miniPkgVersion;

    private Params mCustParams;

    private boolean mEnableExitGameAlert = false;
    private List<GameInfo> mExitGameList = null;

    public static String mUnionUUID = null;
    private String mAuthUnionAk;
    private String mAuthUnionSign;
    private String mAuthUnionTS;
    //暂存游戏声音开关的变量
    private boolean gameVoiceSwitchValue = false;
    //游戏是否正在运行
    private boolean gameRunSuccess = false;
    //退出弹窗
    private ExitDialog exitDialog;
    //游戏推荐退出弹窗
    private ExitGameListDialog exitGameListDialog;

    private MsgReceiver mMsgReceiver;
    //游戏相关的 broadcastReceiver
    private KPGameReceiver mKpGameReceiver;


    private final Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            if (isFinishing()) {
                return;
            }
            switch (msg.what) {
                case MSG_SHOW_ERROR:
                    showError((String) msg.obj);
                    break;
                case MSG_RELOAD_GAME:
                    reloadGame();
                    break;
                case MSG_SHOW_AUTH:
                    if (mPlayStatueView != null) {
                        mPlayStatueView.showUserAuthView(mCorpID, mUnionUUID);
                    }
                    break;
                case MSG_GAME_EXIT:
                    onBackPressed();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        GameBox.sRefWatcher.watch(this);
        if (Env.isTestEnv()) {
            Toast.makeText(this, "Env test !!!", Toast.LENGTH_LONG).show();
        }

        setFullScreen();
        @SuppressLint("InflateParams")
        View rootView = getLayoutInflater().inflate(R.layout.kp_activity_game_play, null);
        setContentView(rootView);

        mCorpID = getIntent().getStringExtra(EXTRA_CORPID);
        mGameInfo = getIntent().getParcelableExtra(EXTRA_GAME);
        if (getIntent().hasExtra(EXTRA_MINI_VERSION)){
            miniPkgVersion = getIntent().getStringExtra(EXTRA_MINI_VERSION);
        }
        if (getIntent().hasExtra(EXTRA_PARAMS)) {
            try {
                mCustParams = (Params) getIntent().getSerializableExtra(EXTRA_PARAMS);
            } catch (Exception e) {
            }
        }
        if (mCustParams == null) {
            mCustParams = new Params();
        }

        fontTimeout = mCustParams.get(ParamKey.GAME_OPT_TIMEOUT_FONT, 5 * 60);
        backTimeout = mCustParams.get(ParamKey.GAME_OPT_TIMEOUT_BACK, 3 * 60);

        mEnableExitGameAlert = mCustParams.get(ParamKey.GAME_OPT_EXIT_GAMELIST, true);

        mUnionUUID = mCustParams.get(ParamKey.GAME_AUTH_UNION_UUID, null);
        mAuthUnionAk = mCustParams.get(ParamKey.GAME_AUTH_UNION_AK,"");
        mAuthUnionSign = mCustParams.get(ParamKey.GAME_AUTH_UNION_SIGN, "");
        mAuthUnionTS = mCustParams.get(ParamKey.GAME_AUTH_UNION_TS, "");
        GameBoxManager.getInstance().setUniqueId(mUnionUUID);

        String guidJson = mCustParams.get(ParamKey.GAME_AUTH_UNION_GID,null);
        if (guidJson != null){
            UserAuthManager.getInstance().cachePlatUserInfo(this, mGameInfo.pkgName, guidJson);
        }else {
            UserAuthManager.getInstance().clearPlatUserInfo(this,mGameInfo.pkgName);
        }

        initView(rootView);
        mMsgReceiver = new MsgReceiver(this);
        mHardwareManager = new HardwareManager(this);

        try {
            //统计事件初始化
            Event.init(getApplication(), mCorpID);

            //重置traceId
            Event.resetBaseTraceId();

            //发送打点事件
            Event event = Event.getEvent(EventCode.DATA_ACTIVITY_PLAYGAME_ONCREATE, mGameInfo != null ? mGameInfo.pkgName : "");
            Event cloneEvent = (Event) event.clone();
            cloneEvent.traceId = Event.getBaseTraceId();
            MobclickAgent.sendEvent(cloneEvent);
        } catch (Exception e) {
        }

        //未获取到游戏信息
        if (mCorpID == null || mGameInfo == null) {
            mHandler.sendMessage(Message.obtain(mHandler, MSG_SHOW_ERROR, "获取游戏信息失败"));
            return;
        }
        doGameReceiver();

        checkAndRequestPermission();

        Logger.info("GamePlay", "Activity Process，pid:" + android.os.Process.myPid());

        bindDownloadService(true);

    }

    private void initView(View rootView) {

        mContentView = findViewById(R.id.content_view);

        mPlayStatueView = new PlayStatusLayout.Builder(this)
                .setGameInfo(mGameInfo)
                .create();
        mPlayStatueView.setCallback(new PlayStatusCallback(GamePlay.this));
        ((ViewGroup) rootView).addView(mPlayStatueView, 0);

        mMenuView = findViewById(R.id.float_menu);
        mMenuView.setPkgVersion(miniPkgVersion);
        mMenuView.setResizeClickListener(new FloatMenuView.VideoResizeListener() {
            @Override
            public void onVideoResize(boolean scale) {
                resizeVideoContainer(scale);
            }
        });
        mMenuView.setOnExitClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mMenuView.setOnRecordClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDeviceControl != null) {
                    String padcode = mDeviceControl.getPadcode();

                    try {
                        //发送打点事件
                        Event event = Event.getEvent(EventCode.DATA_RECORD_CLICK_STARTBTN, mGameInfo != null ? mGameInfo.pkgName : "");
                        if (mDeviceControl != null) {
                            event.setPadcode(padcode);
                        }
                        MobclickAgent.sendEvent(event);
                    } catch (Exception e) {
                    }


                    mRecordView.startRecord(padcode, mGameInfo.pkgName, mGameInfo.name);
                }

            }
        });

        mRecordView = findViewById(R.id.float_record_view);
        mRecordView.setCorpKey(mCorpID);

        mVideoContainer = findViewById(R.id.play_container);

        mFloatDownView = findViewById(R.id.float_down);
        mFloatDownView.setOnDownListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //普通下载
                toggleDownload(mGameInfo);
            }
        });
    }

    private synchronized void toggleDownload(GameInfo gameInfo) {

        if (mDownloadStatus == DownloadTask.STATUS_STARTED) {
            //判断是否是当前游戏
            if (mDownloadId != gameInfo.gid) {
                Toast.makeText(this, "其他游戏在下载中，请稍后在试", Toast.LENGTH_SHORT).show();
                return;
            }
            stopDownload();
        } else {
            startDownlad();
        }
    }

    /**
     * 初始化SDK
     */
    private void checkInitCloudPhoneSDK() {
        try {
            //判断是否已经初始化
            if (!GameBoxManager.getInstance().isGameBoxManagerInited()) {
                //初始化
                if (!isFinishing() && mPlayStatueView != null) {
                    mPlayStatueView.setStatus(PlayStatusLayout.STATUS_LOADING_INIT, "正在设备初始化...");
                }

                GameBoxManager.getInstance().init(getApplication(), this.mCorpID, new APICallback<String>() {
                    @Override
                    public void onAPICallback(String msg, int code) {
                        if (code == 1) {
                            getGameInfo();
                        } else {
                            //初始化失败，退出页面
                            Logger.error("GamePlay", "初始化游戏失败,code = " + code + ", msg = " + msg);
                            mHandler.sendMessage(Message.obtain(mHandler, MSG_SHOW_ERROR, "初始化游戏失败,请稍后再试"));
                        }
                    }
                });
                return;
            }

        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }

        //启动云手机
        getGameInfo();
    }

    private void getGameInfo() {
        try {
            if (!isFinishing() && mPlayStatueView != null) {
                mPlayStatueView.setStatus(PlayStatusLayout.STATUS_LOADING_GET_GAMEINFO, "获取游戏信息...");
            }

            new RequestGameInfoTask(this).setRequestCallback(new IRequestCallback<GameInfo>() {
                @Override
                public void onResult(GameInfo game, int code) {
                    try {
                        if (game != null) {
                            mGameInfo.gid = game.gid;
                            mGameInfo.pkgName = game.pkgName;
                            mGameInfo.name = game.name;
                            mGameInfo.iconUrl = game.iconUrl;
                            mGameInfo.coverUrl = game.coverUrl;
                            mGameInfo.downloadUrl = game.downloadUrl;
                            mGameInfo.playCount = game.playCount;
                            mGameInfo.totalTime = game.totalTime;
                            mGameInfo.usedTime = game.usedTime;
                            mGameInfo.kpGameId = game.kpGameId;
                            mGameInfo.enableDownload = game.enableDownload;
                            mGameInfo.mockSleepTime = game.mockSleepTime;
                            mGameInfo.kpUnionGame = game.kpUnionGame;
                            mGameInfo.recoverCloudData = game.recoverCloudData;
                            mGameInfo.enterRemind = game.enterRemind;
                            mGameInfo.exitRemind = game.exitRemind;
                            mGameInfo.useSDK = mGameInfo.useSDK != GameInfo.SdkType.DEFAULT ? mGameInfo.useSDK : game.useSDK;
                            //处理广告显示
                            if (mGameInfo.showAd == GameInfo.GAME_AD_SHOW_AUTO) {
                                mGameInfo.showAd = game.showAd;
                            }
                            if (mGameInfo.ext == null || mGameInfo.ext.size() <= 0) {
                                mGameInfo.ext = new HashMap<>();
                                if (game.ext != null) {
                                    mGameInfo.ext.putAll(game.ext);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Logger.error("GamePlay", e.getMessage());
                    }

                    if (mPlayStatueView != null) {
                        mPlayStatueView.setGameInfo(mGameInfo);
                    }

                    //判断是否需要显示授权界面
                    if (mGameInfo.kpUnionGame == 1) {
                        if (mUnionUUID == null || mUnionUUID.isEmpty()){
                            ProferencesUtils.setString(GamePlay.this, SharedKeys.KEY_AUTH_ID,"");
                        }else {
                            String authIdValue = ProferencesUtils.getString(GamePlay.this, SharedKeys.KEY_AUTH_ID, "");
                            if(!mUnionUUID.equals(authIdValue)){
                                ProferencesUtils.remove(GamePlay.this,SharedKeys.KEY_GAME_USER_LOGIN_DATA_PRE);
                                mHandler.sendEmptyMessage(MSG_SHOW_AUTH);
                                return;
                            }
                        }
                    }

                    //启动游戏
                    startCloudPhone();
                }
            }).execute(mCorpID, mGameInfo.pkgName);
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }

    /**
     * 申请云设备
     */
    private void startCloudPhone() {
        try {
            if (this.isFinishing()) {
                return;
            }

            if (mPlayStatueView != null) {
                mPlayStatueView.setStatus(PlayStatusLayout.STATUS_LOADING_CONNECT_DEVICE, "正在连接设备...");
            }

            GameBoxManager.getInstance().applyCloudDevice(this, mGameInfo, new APICallback<IDeviceControl>() {
                @Override
                public void onAPICallback(IDeviceControl deviceControl, final int code) {
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
                            } else {
                                Logger.error("GamePlay", "申请试玩设备失败,code = " + code);

                                if (mDeviceControl != null) {
                                    mDeviceControl.stopGame();
                                }

                                GamePlay.this.mErrorCode = code;
                                GamePlay.this.mErrorMsg = getErrorText(code);
                                mHandler.sendMessage(Message.obtain(mHandler, MSG_SHOW_ERROR, getErrorText(code)));
                            }
                        }
                    });
                }
            });

        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }


    private void startGame() {
        try {
            //获取云手机分辨率，按比例显示画面
            initVideoSize();

            mDeviceControl.registerSensorSamplerListener(new IDeviceControl.SensorSamplerListener() {
                @Override
                public void onSensorSamper(int sensor, int state) {
//                if (sensor < 210) {
//                    return;
//                }
                    Logger.info("GamePlay", "onSensorSamper = " + sensor + "  state = " + state);
                    mHardwareManager.registerHardwareState(sensor, state);
                }
            });
            mHardwareManager.setDeviceControl(mDeviceControl);

            mDeviceControl.startGame(GamePlay.this, R.id.play_container, GamePlay.this);

            //设置前后台无操作超时时间
            mDeviceControl.setNoOpsTimeout(fontTimeout, backTimeout);

        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }

    @Override
    public void onAPICallback(String msg, int code) {
//        if (msg != null) {
        Logger.info("GamePlay", "gameOnAPICallback, code = " + code + ", apiResult = " + msg);
//        }
        try {
            if (code == APIConstants.AD_LOADING) {
                if (!isFinishing() && mPlayStatueView != null) {
                    mPlayStatueView.setStatus(PlayStatusLayout.STATUS_LOADING_AD, "正在加载广告...");
                }
            } else if (code == APIConstants.GAME_LOADING) {
                if (!isFinishing() && mPlayStatueView != null) {
                    mPlayStatueView.setStatus(PlayStatusLayout.STATUS_LOADING_START_GAME, "正在加载游戏...");
                }
            } else if (code == APIConstants.RECOVER_DATA_LOADING) {
                if (!isFinishing() && mPlayStatueView != null) {
                    mPlayStatueView.setStatus(PlayStatusLayout.STATUS_LOADING_RECOVER_GAMEINFO, "初始游戏数据...");
                }
            } else if (code == APIConstants.CONNECT_DEVICE_SUCCESS || code == APIConstants.RECONNECT_DEVICE_SUCCESS) {
                this.mErrorMsg = null;

//                doGameReceiver();
                mDeviceControl.setPlayListener(this);
                mDeviceControl.setMessageReceiver(mMsgReceiver);
                playSuccess();
            } else if (code == APIConstants.RELEASE_SUCCESS) {
//                if (mDeviceControl!=null){
//                    mDeviceControl.removerListener();
//                }

                if (mChangeGame) {
                    mChangeGame = false;
                    mHandler.sendEmptyMessage(MSG_RELOAD_GAME);
                }

            } else if (code == APIConstants.ERROR_NETWORK_ERROR) {
                //网络错误，弹出提示窗口
                if (mDeviceControl != null) {
                    mDeviceControl.stopGame();
                }

                showTimeoutDialog("网络不稳定，请检查网络配置。");

            } else {
                if (mDeviceControl != null) {
                    mDeviceControl.stopGame();
                }

                Logger.error("GamePlay", msg);

                //取消游戏
                if (code == APIConstants.ERROR_GAME_CANCEL) {
                    exitPlay();
                    return;
                }

                this.mErrorCode = code;
                this.mErrorMsg = msg != null ? msg : getErrorText(code);
                mHandler.sendMessage(Message.obtain(mHandler, MSG_SHOW_ERROR, getErrorText(code)));

            }

        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }

    private void playSuccess() {
        try {
            gameRunSuccess = true;
            mPlayStatueView.setStatus(PlayStatusLayout.STATUS_LOADING_FINISHED, "游戏启动完成！");
            mPlayStatueView.setVisibility(View.GONE);

            mVideoContainer.setVisibility(View.VISIBLE);
            mMenuView.setVisibility(View.VISIBLE);
            mMenuView.setDeviceControl(mDeviceControl);

            //显示下载按钮
            if (mGameInfo != null && mGameInfo.enableDownload == 1 && !StringUtil.isEmpty(mGameInfo.downloadUrl)) {
                mFloatDownView.setVisibility(View.VISIBLE);
                mFloatDownView.startTimeoutLayout();
            }

            requestExitGameList();

            try {
                getWindow().getDecorView().setSystemUiVisibility(getSystemUi());
            } catch (Exception e) {
            }

        } catch (Exception e) {
            gameRunSuccess = false;
            Logger.error(TAG, e.getMessage());
        }
    }

    private void exitPlay() {
        setResult(RESULT_OK, getIntent());
        finish();
    }

    /**
     * 显示错识页面
     *
     * @param err
     */
    private void showError(String err) {
        try {
            if (isFinishing()) {
                return;
            }

            mVideoContainer.setVisibility(View.GONE);
            mMenuView.setVisibility(View.GONE);
            mFloatDownView.setVisibility(View.GONE);

            mRecordView.reset();
            mRecordView.setVisibility(View.GONE);

            mPlayStatueView.setVisibility(View.VISIBLE);
            mPlayStatueView.setStatus(PlayStatusLayout.STATUS_ERROR, err);
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }

    /**
     * 重试加载游戏
     */
    private void reloadGame() {
        try {
            mVideoContainer.removeAllViews();
            mVideoContainer.setVisibility(View.GONE);
            mMenuView.setVisibility(View.GONE);
            mFloatDownView.setVisibility(View.GONE);

            mPlayStatueView.setVisibility(View.VISIBLE);
            mPlayStatueView.setStatus(PlayStatusLayout.STATUS_LOADING, "加载云游戏");

            mRecordView.reset();
            mRecordView.setVisibility(View.GONE);

            checkAndRequestPermission();

        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从 home 键返回到游戏中是处理声音问题
        if (gameRunSuccess && mDeviceControl != null && gameVoiceSwitchValue){
            mDeviceControl.setAudioSwitch(true);
        }
        if (mDeviceControl != null){
            mDeviceControl.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDeviceControl != null){
            mDeviceControl.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 按下 home 按键暂时关闭游戏声音
        if (gameRunSuccess && mDeviceControl != null){
            gameVoiceSwitchValue = mDeviceControl.isSoundEnable();
            if (gameVoiceSwitchValue){
                mDeviceControl.setAudioSwitch(false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mKpGameReceiver != null){
            unregisterReceiver(mKpGameReceiver);
            mKpGameReceiver = null;
        }
        super.onDestroy();

        try {
            if (mDeviceControl != null) {
                mDeviceControl.stopGame();
            }
//            GameBoxManager.getInstance().exitQueue();
            gameVoiceSwitchValue = false;
            mRecordView = null;
            mMenuView.dismissMenuDialog();

            try {
                //发送打点事件
                Event event = Event.getEvent(EventCode.DATA_ACTIVITY_PLAYGAME_DESTORY, mGameInfo != null ? mGameInfo.pkgName : "");
                Event cloneEvent = (Event) event.clone();
                cloneEvent.traceId = Event.getBaseTraceId();
                MobclickAgent.sendEvent(cloneEvent);
            } catch (Exception e) {
            }

//        if (mGameDownloader!=null){
//            mGameDownloader.removeCallback(this);
//        }

            if (mExitGameList != null) {
                mExitGameList.clear();
                mExitGameList = null;
            }

            if (mPlayStatueView != null) {
                mPlayStatueView.destory();
                mPlayStatueView = null;
            }

            if (mVideoContainer != null) {
                mVideoContainer.removeAllViews();
            }

            if (mHardwareManager != null) {
                mHardwareManager.release();
                mHardwareManager = null;
            }

            unbindDownloadService();
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (mDeviceControl == null || mDeviceControl.isReleased()) {
                exitPlay();
                return;
            }
            //弹出挽留窗口
            if (showExitGameListDialog(this)) {
                return;
            }
            //弹出退出窗口
            if (showExitDialog()) {
                return;
            }

        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }

        exitPlay();
    }

    private boolean showExitDialog() {
        try {
            exitDialog = new ExitDialog(this);
            if (mGameInfo != null) {
                exitDialog.setText(mGameInfo.exitRemind);
            }
            exitDialog.setOnExitListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    exitPlay();
                }
            });
            exitDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    GamePlay.this.getWindow().getDecorView().setSystemUiVisibility(getSystemUi());
                }
            });
            exitDialog.show();
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
            return false;
        }
        return true;
    }

    private boolean showTimeoutDialog(String msg) {
        try {
            //关闭其他弹窗
            if (exitDialog != null && exitDialog.isShowing()){
                exitDialog.dismiss();
            }
            if (exitGameListDialog != null && exitGameListDialog.isShowing()){
                exitGameListDialog.dismiss();
            }
            TimeoutDialog dialog = new TimeoutDialog(this);
            dialog.setTitle(msg);
            dialog.setOnExitListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    exitPlay();
                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    GamePlay.this.getWindow().getDecorView().setSystemUiVisibility(getSystemUi());
//                }
                }
            });
            dialog.setOnReloadListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHandler.sendEmptyMessage(MSG_RELOAD_GAME);
                }
            });
            dialog.show();
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
        return true;
    }

    @Override
    public void onPingUpdate(int ping) {
        if (!isFinishing() && mMenuView != null) {
            mMenuView.onPingChanged(ping);
        }
    }

    @Override
    public boolean onNoOpsTimeout(int type, long timeout) {
        Logger.info("GamePlay", "onNoOpsTimeout() type = " + type + ", timeout = " + timeout);

        gameRunSuccess = false;
        gameVoiceSwitchValue = false;
        //前台未操作超时
//        if (type == 2) {
            showTimeoutDialog("您长时间未操作，游戏已释放。");
            if (mDeviceControl != null) {
                mDeviceControl.stopGame();
            }
            return true;
//        }

//        exitPlay();
//        Toast.makeText(this, String.format("[%s]无操作超时 %ds 退出！", type == 1 ? "后台" : "前台", timeout / 1000), Toast.LENGTH_LONG).show();

//        return true;
    }

    @Override
    public void onScreenChange(int orientation) {
        Logger.info("GamePlay", "onScreenChange() orientation = " + orientation);
    }


    private void setFullScreen() {
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            this.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    getWindow().getDecorView().setSystemUiVisibility(getSystemUi());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getSystemUi() {
        return View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    }

    private static final int CODE_REQUEST_PERMISSION = 1024;
    private static final int CODE_REQUEST_DOWNLOAD_PERMISSION = 1025;

    private void checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT < 23) {
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
            checkInitCloudPhoneSDK();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CODE_REQUEST_PERMISSION) {
            checkInitCloudPhoneSDK();
        } else if (requestCode == CODE_REQUEST_DOWNLOAD_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownlad();
            }
        }
    }

    private String getErrorText(int code) {
        String error;
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
            case APIConstants.ERROR_DEVICE_EXPIRED:
            case APIConstants.ERROR_DEVICE_OTHER_ERROR:
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

    private String getPkgName() {
        if (mGameInfo != null) {
            return mGameInfo.pkgName;
        }
        return "";
    }

    private int mDownloadStatus;
    private int mDownloadId;
    int resizeWidth = 0;
    int resizeHeight = 0;

    /**
     * 初始化显示画面比例尺寸
     */
    private void initVideoSize() {
        try {
            if (mDeviceControl == null) {
                return;
            }
            //获取云手机分辨率，按比例显示画面
            int[] size = mDeviceControl.getVideoSize();
            if (size != null && size.length == 2) {
                //视频尺寸
                int vw = size[0];
                int vh = size[1];

                //屏幕尺寸
                int sw = mContentView.getWidth();
                int sh = mContentView.getHeight();

                if (sw <= 0 || sh <= 0) {
                    sw = DensityUtil.getScreenWidth(this);
                    sh = DensityUtil.getScreenHeight(this);
                }

                //处理横竖屏
                int screenWidth = sh < sw ? sh : sw;
                int screenHeight = sh < sw ? sw : sh;
                int videoWidth = vh < vw ? vh : vw;
                int videoHeight = vh < vw ? vw : vh;

                //宽高比
                float videoScale = (float) videoWidth / (float) videoHeight;
                float screenScale = (float) screenWidth / (float) screenHeight;

                float widthScale = (float) videoWidth / (float) screenWidth;
                float heightScale = (float) videoHeight / (float) screenHeight;

                if (widthScale < heightScale) {
                    resizeHeight = screenHeight;
                    resizeWidth = (int) (screenHeight * videoScale);
                } else {
                    resizeWidth = screenWidth;
                    resizeHeight = (int) (screenWidth / videoScale);
                }
            }

            resizeVideoContainer(mMenuView.mVideoScale);
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }

    }


    /**
     * 修改显示画面比例
     *
     * @param scale
     */
    private synchronized void resizeVideoContainer(boolean scale) {
        try {
            if (scale) {
                Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
                int ori = mConfiguration.orientation; //获取屏幕方向
                if (resizeWidth > 0 && resizeHeight > 0) {
                    if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                        //横屏
                        ViewGroup.LayoutParams lp = mVideoContainer.getLayoutParams();
                        lp.width = resizeHeight;
                        lp.height = resizeWidth;
                        mVideoContainer.setLayoutParams(lp);
                    } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
                        //竖屏
                        ViewGroup.LayoutParams lp = mVideoContainer.getLayoutParams();
                        lp.width = resizeWidth;
                        lp.height = resizeHeight;
                        mVideoContainer.setLayoutParams(lp);
                    }
                }
            } else {
                //全屏
                ViewGroup.LayoutParams lp = mVideoContainer.getLayoutParams();
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                mVideoContainer.setLayoutParams(lp);
            }
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resizeVideoContainer(mMenuView.mVideoScale);
    }


    /**
     * 切换游戏
     */
    private boolean mChangeGame = false;

    private void changeGame(GameInfo game) {
        try {
            if (game == null || game.gid == 0) {
                return;
            }

            try {
                //发送打点事件
                Event event = Event.getEvent(EventCode.DATA_DIALOG_EXITLIST_CHANGEGAME);
                event.setGamePkg(game.pkgName);
                MobclickAgent.sendEvent(event);
            } catch (Exception e) {
            }

            //关闭换留窗显示
            mEnableExitGameAlert = false;
            //删除数据
            mExitGameList = null;

            mChangeGame = true;

            //更换游戏信息
            mGameInfo = game;

            //关闭当前游戏
            if (mDeviceControl != null && !mDeviceControl.isReleased()) {
                mDeviceControl.stopGame();
            } else {
                mHandler.sendEmptyMessage(MSG_RELOAD_GAME);
            }
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }

    /**
     * 显示挽留窗口
     *
     * @param activity
     * @return
     */
    public boolean showExitGameListDialog(Activity activity) {
        if (mExitGameList == null || mExitGameList.size() <= 0) {
            return false;
        }

        //获取总数
        int mExitAlertCount = ProferencesUtils.getIng(this, SharedKeys.KEY_GAME_EXITALERTCOUNT_CONF, 0);

        int num = getExitShowNum();

        //超过显示数量,不显示
        if (num >= mExitAlertCount) {
            return false;
        }

        try {
            exitGameListDialog = new ExitGameListDialog(activity, mExitGameList, mGameInfo.exitRemind);
            exitGameListDialog.setCallback(new ExitGameListDialog.ICallback() {
                @Override
                public void onGameItem(GameInfo gameInfo) {
                    exitGameListDialog.dismiss();
                    changeGame(gameInfo);
                }

                @Override
                public void onExit() {
                    exitGameListDialog.dismiss();
                    exitPlay();

                    try {
                        //发送打点事件
                        Event event = Event.getEvent(EventCode.DATA_DIALOG_EXITLIST_EXITBTN, mGameInfo.pkgName);
                        MobclickAgent.sendEvent(event);
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onClose() {
                    exitGameListDialog.dismiss();

                    try {
                        //发送打点事件
                        Event event = Event.getEvent(EventCode.DATA_DIALOG_EXITLIST_CANCELBTN, mGameInfo.pkgName);
                        MobclickAgent.sendEvent(event);
                    } catch (Exception e) {
                    }
                }
            });
            exitGameListDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    //增加显示数量
                    addExitShowNum();

                    try {
                        //发送打点事件
                        Event event = Event.getEvent(EventCode.DATA_DIALOG_EXITLIST_DISPLAY, mGameInfo.pkgName);
                        MobclickAgent.sendEvent(event);
                    } catch (Exception e) {
                    }
                }
            });
            exitGameListDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    GamePlay.this.getWindow().getDecorView().setSystemUiVisibility(getSystemUi());
                }
            });

            exitGameListDialog.show();

            return true;
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }

        return false;
    }


    private static final String KEY_EXIT_NUM = "kp_game_exit_dialog_num";

    /**
     * 获取挽留窗显示次数
     *
     * @return
     */
    private int getExitShowNum() {
        //判断次数
        int num = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            final String today = sdf.format(new Date());
            String str = ProferencesUtils.getString(this, KEY_EXIT_NUM, null);
            JSONObject obj = new JSONObject(str);
            if (obj.has(today)) {
                num = obj.getInt(today);
            }

        } catch (Exception e) {
        }
        return num;
    }

    /**
     * 记录挽留窗显示次数
     */
    private void addExitShowNum() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            final String today = sdf.format(new Date());
            int num = getExitShowNum();
            HashMap<String, Object> map = new HashMap<>();
            map.put(today, num + 1);
            ProferencesUtils.setString(this, KEY_EXIT_NUM, new JSONObject(map).toString());
        } catch (Exception e) {
            Logger.error("GamePlay", e.getMessage());
        }
    }

    /**
     * 请求挽留窗游戏数据
     */
    private void requestExitGameList() {
        try {
            if (mExitGameList != null) {
                return;
            }

            //判断是否要显示挽留窗
            if (!mEnableExitGameAlert) {
                mExitGameList = null;
                return;
            }

            //获取总数
            int mExitAlertCount = ProferencesUtils.getIng(this, SharedKeys.KEY_GAME_EXITALERTCOUNT_CONF, 0);

            int num = getExitShowNum();

            //超过显示数量,不显示
            if (num >= mExitAlertCount) {
                return;
            }

            //获取数据
            new RequestGameExitListTask(this)
                    .setRequestCallback(new IRequestCallback<List<GameInfo>>() {
                        @Override
                        public void onResult(List<GameInfo> list, int code) {
                            if (list != null && list.size() > 0) {
                                mExitGameList = new ArrayList<>();
                                mExitGameList.addAll(list);
                            }
                        }
                    })
                    .execute(mCorpID, mGameInfo.kpGameId);
        } catch (Exception e) {
            Logger.error("GamePlay", e.getMessage());
        }
    }

    private static class MsgReceiver extends BaseMsgReceiver {
        WeakReference<GamePlay> mRef = null;

        public MsgReceiver(GamePlay activity) {
            mRef = new WeakReference<>(activity);
        }

        @Override
        public void onMessageReceived(String msg) {
            if (msg == null) {
                return;
            }
            if (mRef == null || mRef.get() == null || mRef.get().isFinishing()) {
                return;
            }

            try {
                JSONObject obj = new JSONObject(msg);
                if (obj.has("channel")) {
                    String ch = obj.getString("channel");
                    if ("netease".equals(ch)) {
                        Object data = obj.get("data");
                        //发送广播
                        String str = data == null ? "" : data.toString();
                        Intent intent = new Intent("KpTech_Game_Kit_NetEase_Msg_Received");
                        intent.putExtra("data", str);
                        mRef.get().sendBroadcast(intent);

                        try {
                            //发送打点事件
                            Event event = Event.getEvent(EventCode.DATA_ACTIVITY_ONMESSAGE_NETEASE, mRef.get().getPkgName());
                            HashMap<String, Object> ext = new HashMap<>();
                            ext.put("data", str);
                            event.setExt(ext);
                            MobclickAgent.sendEvent(event);
                        } catch (Exception e) {
                        }
                    }
                }

            } catch (Exception e) {
                Logger.error(TAG, e.getMessage());
            }
        }

        @Override
        public void onMessageReceived(String event, Map<String, Object> params) {
            if (mRef == null || mRef.get() == null || mRef.get().isFinishing()) {
                return;
            }

            try {
                //退出游戏事件
                if (event.equals(BaseMsgReceiver.EVENT_EXIT)) {
                    mRef.get().mHandler.sendEmptyMessage(MSG_GAME_EXIT);
                }
            } catch (Exception e) {
                Logger.error(TAG, e.getMessage());
            }

        }
    }

    // --------------------------------- 下载功能 -----------------------------
    public void startDownlad() {
        //验证权限
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_REQUEST_DOWNLOAD_PERMISSION);
                return;
            }
        }

        Intent intent = new Intent(this, DownloadTask.class);
        intent.putExtra("action", "start");
        intent.putExtra(DownloadTask.EXTRA_GAME, mGameInfo);
        //android8.0以上通过startForegroundService启动service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        bindDownloadService(false);
    }

    private void stopDownload() {
        Intent intent = new Intent(this, DownloadTask.class);
        intent.putExtra("action", "stop");
        intent.putExtra(DownloadTask.EXTRA_GAME, mGameInfo);
        //android8.0以上通过startForegroundService启动service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private DownloadServiceConnection connection;

    private synchronized void bindDownloadService(boolean check) {
        if (check && !AppUtils.isServiceRunning(this, DownloadTask.class.getName())) {
            return;
        }

        if (connection != null) {
            return;
        }

        //判断是否是当前游戏
        connection = new DownloadServiceConnection(this);
        bindService(new Intent(this, DownloadTask.class), connection, Context.BIND_AUTO_CREATE);
    }

    private void unbindDownloadService() {
        try {
            if (connection != null) {
                unbindService(connection);
                connection = null;
            }
        } catch (Exception e) {
            Logger.error("GamePlay", e);
        }
    }

    private static class DownloadServiceConnection implements ServiceConnection {
        WeakReference<GamePlay> ref;

        private DownloadServiceConnection(GamePlay activity) {
            ref = new WeakReference<>(activity);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            DownloadTask.DownloadBinder downloadBinder = (DownloadTask.DownloadBinder) service;
            DownloadTask mService = downloadBinder.getService();
            if (mService.isDownloading()) {
                if (ref != null && ref.get() != null) {
                    ref.get().updateDownloadStatus(DownloadTask.STATUS_STARTED, mService.getId());
                }
            }

            mService.setDataCallback(new DownloadTask.DataCallback() {
                @Override
                public void onStart(int id) {
                    Logger.info("GamePlay", "onStart: " + id);
                    if (ref != null && ref.get() != null) {
                        ref.get().updateDownloadStatus(DownloadTask.STATUS_STARTED, id);
                    }
                }

                @Override
                public void onPause(int id) {
                    if (ref != null && ref.get() != null) {
                        ref.get().updateDownloadStatus(DownloadTask.STATUS_STOPED, id);
                    }
                }

                @Override
                public void onSuccess(String filePath, int id) {
                    Logger.info("GamePlay", "onSuccess: " + filePath);
                    if (ref != null && ref.get() != null) {
                        ref.get().updateDownloadStatus(DownloadTask.STATUS_FINISHED, id);
                        ref.get().unbindDownloadService();
                    }
                }

                @Override
                public void onFail(String err, int id) {
                    Logger.info("GamePlay", "onFail: " + err);
                    if (ref != null && ref.get() != null) {
                        ref.get().updateDownloadStatus(DownloadTask.STATUS_ERROR, id);
                    }
                }

                @Override
                public void onProgress(long total, long current, int id) {
                    double precent = Double.parseDouble(current + "") / Double.parseDouble(total + "");
                    int progress = (int) (precent * 100);
                    Logger.info("GamePlay", "onDownloadProgress: " + progress + "%");
                    if (ref != null && ref.get() != null) {
                        ref.get().updateDownloadProgress(progress, "" + progress + "%", id);
                    }
                }

                @Override
                public void onStopService() {
                    Logger.info("GamePlay", "onStopService");
                    if (ref != null && ref.get() != null) {
                        ref.get().unbindDownloadService();
                    }
                }

                @Override
                public void onInstallApkError(final String filePath, String msg) {
                    if (ref == null || ref.get() == null) {
                        return;
                    }
                    //Apk包解析错误，弹出提示窗口
                    AlertDialog dialog = new AlertDialog.Builder(ref.get())
                            .setTitle("安装包解析失败，删除后重新下载！")
                            .setCancelable(true)
                            .setPositiveButton("重新下载", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    //删除文件
                                    File file = new File(filePath);
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                    //重新下载
                                    ref.get().startDownlad();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    dialog.show();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private void updateDownloadStatus(int status, int gid) {
        mDownloadStatus = status;
        mDownloadId = gid;

        if (mGameInfo != null && gid == mGameInfo.gid) {
            if (mPlayStatueView != null && mPlayStatueView.isShown()) {
                mPlayStatueView.setDownloadStatus(status);
            }

            if (mFloatDownView != null && mFloatDownView.isShown()) {
                mFloatDownView.setDownloadStatus(status);
            }
        }
    }

    private void updateDownloadProgress(int progress, String text, int gid) {
        if (mGameInfo != null && gid == mGameInfo.gid) {
            if (mPlayStatueView != null && mPlayStatueView.isShown()) {
                mPlayStatueView.setProgress(progress, text);
            }

            if (mFloatDownView != null && mFloatDownView.isShown()) {
                mFloatDownView.setProgress(progress, text);
            }
        }
    }

    // 普通 loading 页面
    private class PlayStatusCallback implements PlayStatusLayout.ICallback {

        WeakReference<GamePlay> ref = null;

        public PlayStatusCallback(GamePlay play) {
            ref = new WeakReference<>(play);
        }

        @Override
        public void onClickFinish() {
            try {
                if (ref != null && ref.get() != null) {
                    ref.get().exitPlay();
                }
            } catch (Exception e) {
                Logger.error(TAG, e);
            }
        }

        @Override
        public void onClickReloadGame() {
            try {
                if (ref != null && ref.get() != null) {
                    ref.get().mHandler.sendEmptyMessage(MSG_RELOAD_GAME);
                }

                try {
                    //发送打点事件
                    Event event = Event.getEvent(EventCode.DATA_ACTIVITY_PLAYERROR_RELOAD, mGameInfo != null ? mGameInfo.pkgName : "");
                    event.setErrMsg(GamePlay.this.mErrorMsg);
                    if (mDeviceControl != null) {
                        event.setPadcode(mDeviceControl.getPadcode());
                    }
                    HashMap ext = new HashMap();
                    ext.put("code", mErrorCode);
                    ext.put("msg", mErrorMsg);
                    event.setExt(ext);
                    MobclickAgent.sendEvent(event);
                } catch (Exception e) {
                }
            } catch (Exception e) {
                Logger.error(TAG, e);
            }
        }

        @Override
        public void onClickDownloading() {
            try {
                if (ref != null && ref.get() != null) {
                    ref.get().toggleDownload(mGameInfo);
                }
            } catch (Exception e) {
                Logger.error(TAG, e);
            }
        }

        @Override
        public void onClickAuthPass() {
            try {
                try {
                    //发送打点事件
                    MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_ACTIVITY_USERAUTH_APPROVE, mGameInfo != null ? mGameInfo.pkgName : ""));
                } catch (Exception e) {
                    Logger.error(TAG, "onClickAuthPass:" + e.getMessage());
                }

                //调用接口发送授权数据
                new AccountTask(GamePlay.this, AccountTask.ACTION_AUTH_GT_API)
                        .setCorpKey(mCorpID)
                        .setCallback(new AccountTask.ICallback() {
                            @Override
                            public void onResult(Map<String, Object> map) {
                                ProferencesUtils.setString(GamePlay.this,SharedKeys.KEY_AUTH_ID,mUnionUUID);
                                //保存数据
                                String errMsg = "";
                                if (map == null || map.size() <= 0){
                                    errMsg = "登录失败";
                                }else if (map.containsKey("error")){
                                    errMsg = map.get("error").toString();
                                }
                                try{
                                    boolean noError = (errMsg == null || errMsg.isEmpty());
                                    if (map != null && noError){

                                        if (map.containsKey("guid")){
                                            Object guid = map.get("guid");
                                            if (guid != null){
                                                Event.setGuid(guid+"");
                                            }
                                        }
                                        if (map.containsKey("access_token")){
                                            Object at =  map.get("access_token");
                                            map.put("token", at);
                                            map.remove("access_token");
                                        }
                                        if (map.containsKey("phone")){
                                            Object phone = map.get("phone");
                                            map.put("userphone",phone);
                                        }
                                        JSONObject obj = new JSONObject(map);
                                        String cacheKey = SharedKeys.KEY_GAME_USER_LOGIN_DATA_PRE;
                                        ProferencesUtils.setString(GamePlay.this, cacheKey, obj.toString());
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                if (ref != null && ref.get() != null) {
                                    ref.get().startCloudPhone();
                                }
                            }
                        })
                        .execute(mAuthUnionAk, mUnionUUID, mCorpID, mAuthUnionTS, mAuthUnionSign);;




            } catch (Exception e) {
                Logger.error(TAG, e);
            }
        }

        @Override
        public void onClickAuthReject() {
            try {
                try {
                    //发送打点事件
                    MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_ACTIVITY_USERAUTH_CANCEL, mGameInfo != null ? mGameInfo.pkgName : ""));
                } catch (Exception e) {
                }

                if (ref != null && ref.get() != null) {
                    ref.get().exitPlay();
                }
            } catch (Exception e) {
                Logger.error(TAG, "onClickAuthReject:" + e.getMessage());
            }
        }

        @Override
        public void onClickCopyInf() {
            if (mDeviceControl != null){
                String info = mDeviceControl.getDeviceInfo();
                StringUtil.copy(GamePlay.this, info);
                Toast.makeText(GamePlay.this, "info", Toast.LENGTH_SHORT).show();
            }

        }
    }

    /**
     * 注册广播并发送广播
     */
    private void doGameReceiver(){
        if (mKpGameReceiver != null){
            return;
        }
        registerGameReceiver();
        // 发送广播
        /*Intent intent = new Intent();
        intent.setAction(KPGameReceiver.ACTION);
        String randomValue = System.currentTimeMillis() + "";
        mKpGameReceiver.setRandomValue(randomValue);
        intent.putExtra(KPGameReceiver.RANDOM_KEY,randomValue);
        sendBroadcast(intent);*/
    }

    private void registerGameReceiver() {
        mKpGameReceiver = new KPGameReceiver();
        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(KPGameReceiver.ACTION);
        intentFilter.addAction(KPGameReceiver.ACTION_STARTACTIVITY);
        registerReceiver(mKpGameReceiver,intentFilter);

        mKpGameReceiver.setCallback(new KPGameReceiver.OnKpGameReceiverCallback() {
            @Override
            public void onExitGame() {
                Logger.info(TAG,"onExitGame");
                try{
                    MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_BROADCAST_EXIT_GAME, mGameInfo!=null ? mGameInfo.pkgName : ""));
                }catch (Exception e){
                    Logger.error(TAG, "DATA_BROADCAST_EXIT_GAME:" + e.getMessage());
                }
                exitPlay();
            }

            @Override
            public void onStartActivity(Intent intent) {
                GamePlay.this.startActivityForResult(intent,mRequestCode);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == mRequestCode && resultCode == 9001){
            exitPlay();
        }
    }


}
