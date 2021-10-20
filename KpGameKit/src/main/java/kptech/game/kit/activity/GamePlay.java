package kptech.game.kit.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONObject;
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
import kptech.game.kit.callback.IGameObservable;
import kptech.game.kit.callback.IPlayStateListener;
import kptech.game.kit.callback.SimpleGameObservable;
import kptech.game.kit.manager.FastRepeatClickManager;
import kptech.game.kit.manager.KpGameDownloadManger;
import kptech.game.kit.manager.KpGameManager;
import kptech.game.kit.manager.UserAuthManager;
import kptech.game.kit.receiver.KPGameReceiver;
import kptech.game.kit.utils.GameUtils;
import kptech.game.kit.utils.NetUtils;
import kptech.game.kit.view.FloatRecordView;
import kptech.game.kit.view.PlayStatusLayout;
import kptech.game.kit.view.ToastNetDialog;
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
    private static final int MSG_APPLYING_ERROR = 5;

    private ViewGroup mContentView;
    private FrameLayout mVideoContainer;
    private FloatMenuView mMenuView;

    private FloatRecordView mRecordView;

    private PlayStatusLayout mPlayStatueView;

    private FloatDownView mFloatDownView;
    //透明蒙层
    private View mTransparentLayer;

    private HardwareManager mHardwareManager;

    private IDeviceControl mDeviceControl;

    private GameInfo mGameInfo;
    //游戏的下载地址
    private String mGameDownUrl;
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
    //蒙层是否显示
    private boolean mFrontLayerVis;
    //下载按钮是否显示
    private boolean mDownloadWidVis;
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
    private KpGameDownloadManger downloadManger;
    private IPlayStateListener mPlayStateListener;
    private boolean connIsWifi;
    //处理playSuccess执行多次的问题
    private boolean isFirstWifiToggleDown = true;
    private ToastNetDialog mNetTipDialog;

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            if (isFinishing()) {
                return;
            }
            switch (msg.what) {
                case MSG_SHOW_ERROR:
                    showError(true,(String) msg.obj,false);
                    break;
                case MSG_APPLYING_ERROR: //申请设备中出错
                    showError(true,(String) msg.obj,true);
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

        if (Env.isTestEnv()) {
            Toast.makeText(this, "Env test !!!", Toast.LENGTH_LONG).show();
        }

        setFullScreen();
        @SuppressLint("InflateParams")
        View rootView = getLayoutInflater().inflate(R.layout.kp_activity_game_play, null);
        setContentView(rootView);

        KpGameManager.instance().addObservable(mGameObserver);

        mCorpID = getIntent().getStringExtra(EXTRA_CORPID);
        mGameInfo = getIntent().getParcelableExtra(EXTRA_GAME);
        if (mGameInfo != null){
            mGameDownUrl = mGameInfo.downloadUrl;
        }
        setOrientation();
        if (getIntent().hasExtra(EXTRA_MINI_VERSION)){
            miniPkgVersion = getIntent().getStringExtra(EXTRA_MINI_VERSION);
        }
        if (getIntent().hasExtra(EXTRA_PARAMS)) {
            try {
                mCustParams = (Params) getIntent().getSerializableExtra(EXTRA_PARAMS);
            } catch (Exception e) {
                e.printStackTrace();
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
        mFrontLayerVis = mCustParams.get(ParamKey.GAME_OPT_LAYER_FRONT,false);
        mDownloadWidVis = mCustParams.get(ParamKey.GAME_DOWNLOAD_WID_ENABLE, true);
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
            e.printStackTrace();
        }

        //未获取到游戏信息
        if (mCorpID == null || mGameInfo == null) {
            mHandler.sendMessage(Message.obtain(mHandler, MSG_SHOW_ERROR, "获取游戏信息失败"));
            return;
        }
        initDownloadConfig();
        doGameReceiver();
        checkAndRequestPermission();

        Logger.info("GamePlay", "Activity Process，pid:" + android.os.Process.myPid());

    }

    private void initDownloadConfig() {
        mPlayStateListener = GameBoxManager.getInstance().getStateListener();
        downloadManger = KpGameDownloadManger.instance();
        downloadManger.setGameInfo(mGameInfo);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void setOrientation(){
        if (mGameInfo != null){
            int gameOrientation = mGameInfo.gameOrientation;
            if (gameOrientation == 0){
                if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }else if (gameOrientation == 1){
                if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        }
    }

    private void initView(View rootView) {

        mContentView = findViewById(R.id.content_view);

        mPlayStatueView = new PlayStatusLayout.Builder(this)
                .setGameInfo(mGameInfo)
                .create();
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
                        e.printStackTrace();
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

                if (GameInfo.GAME_DOWNLOADTYPE_SILENT.equals(mGameInfo.downloadType)){
                    //静默下载  显示边玩边下弹窗
                    if (downloadManger != null){
                        downloadManger.showPlayWhenDownDialog(GamePlay.this,connIsWifi);
                    }
                }else {
                    //通知下载
                    toggleDownload();
                }
            }
        });

        mTransparentLayer = findViewById(R.id.view_transparent_layer);
        if (mFrontLayerVis){
            mTransparentLayer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (FastRepeatClickManager.getInstance().isFastDoubleClick(v.getId())){
                        return;
                    }
                    mTransparentLayer.setVisibility(View.GONE);
                    //点击蒙层开始执行下载
                    toggleDownload();
                }
            });
        }else{
            mTransparentLayer.setOnClickListener(null);
        }
    }

    /**
     * 处理下载逻辑
     */
    private synchronized void toggleDownload() {
        //验证权限
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_REQUEST_DOWNLOAD_PERMISSION);
                return;
            }
        }
        if (downloadManger != null){
            // TODO 配置限速值
            /*String downConfigPkgStr = ProferencesUtils.getString(GamePlay.this, SharedKeys.KEY_GAME_DOWN_PKG, "");
            String gamePkg = mGameInfo.pkgName;
            String downConfigDataBufStr = ProferencesUtils.getString(GamePlay.this,SharedKeys.KEY_GAME_DOWN_DATABUF,"");
            GameUtils.setDownSpeedLimit(gamePkg,downConfigPkgStr,downConfigDataBufStr);*/

            int state = downloadManger.getDownloadState(mGameDownUrl);
            switch (state){
                case KpGameDownloadManger.STATE_STARTED:
                    //暂停
                    downloadManger.stopDownload(mGameDownUrl);
                    break;
                case KpGameDownloadManger.STATE_FINISHED:
                    //显示弹窗执行安装
                    downloadManger.showPlayWhenDownDialog(GamePlay.this,connIsWifi);
                    break;
                case KpGameDownloadManger.STATE_STOPPED:
                case KpGameDownloadManger.STATE_WAITING:  //初次开始下载或者当前暂停状态点击开始下载
                    downloadManger.initDownload(GamePlay.this);
                    break;
                case KpGameDownloadManger.STATE_ERROR:
                    //删除原文件重新下载
                    downloadManger.delErrorFile(mGameDownUrl);
                    downloadManger.initDownload(GamePlay.this);
                    break;
                case KpGameDownloadManger.STATE_NONE:
                    Logger.error(TAG,"下载出错，任务状态为NONE");
                    break;
            }

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
                            mHandler.sendMessage(Message.obtain(mHandler, MSG_APPLYING_ERROR, "初始化游戏失败,请稍后再试"));
                        }
                        //对外接口
                        if (mPlayStateListener != null){
                            int notifyCode = code == 1 ? APIConstants.CODE_SDK_INIT_SUCCESS : APIConstants.CODE_SDK_INIT_FAIL;
                            mPlayStateListener.onNotify(notifyCode, msg);
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
            if (mPlayStateListener != null){
                mPlayStateListener.onNotify(APIConstants.CODE_GAME_GET_INFO, "获取游戏信息...");
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

                            mGameDownUrl = game.downloadUrl;
                        }
                    } catch (Exception e) {
                        Logger.error("GamePlay", e.getMessage());
                    }

                    if (mPlayStatueView != null) {
                        mPlayStatueView.setGameInfo(mGameInfo);
                    }

                    //判断是否需要显示授权界面
                    if (mGameInfo.kpUnionGame == 1) {
                        String cachedAuthId = ProferencesUtils.getString(GamePlay.this, SharedKeys.KEY_AUTH_ID, "");
                        if (mUnionUUID == null || mUnionUUID.isEmpty()){
                            if (!cachedAuthId.isEmpty()){
                                ProferencesUtils.remove(GamePlay.this,SharedKeys.KEY_GAME_USER_LOGIN_DATA_PRE);
                                ProferencesUtils.setString(GamePlay.this, SharedKeys.KEY_AUTH_ID,"");
                            }
                        }else {
                            String uuidMd5Value = MD5Util.md5(mUnionUUID);
                            if(!cachedAuthId.equals(uuidMd5Value)){
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
            if (mPlayStateListener != null){
                mPlayStateListener.onNotify(APIConstants.CODE_DEVICE_START_CONN, "正在连接设备...");
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
                                //对外提供状态接口
                                if (mPlayStateListener != null){
                                    mPlayStateListener.onNotify(code, "申请设备成功");
                                }
                            } else {
                                Logger.error("GamePlay", "申请试玩设备失败,code = " + code);

                                if (mDeviceControl != null) {
                                    mDeviceControl.stopGame();
                                }

                                GamePlay.this.mErrorCode = code;
                                GamePlay.this.mErrorMsg = getErrorText(code);
                                mHandler.sendMessage(Message.obtain(mHandler, MSG_APPLYING_ERROR, getErrorText(code)));
                                //对外提供状态接口
                                if (mPlayStateListener != null){
                                    mPlayStateListener.onNotify(code, mErrorMsg);
                                }
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
        Logger.info("GamePlay", "gameOnAPICallback, code = " + code + ", apiResult = " + msg);
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
            connIsWifi = NetUtils.isWiFi(GamePlay.this);

            gameRunSuccess = true;
            mPlayStatueView.setStatus(PlayStatusLayout.STATUS_LOADING_FINISHED, "游戏启动完成！");
            mPlayStatueView.setVisibility(View.GONE);

            mVideoContainer.setVisibility(View.VISIBLE);
            mMenuView.setVisibility(View.VISIBLE);
            mMenuView.setDeviceControl(mDeviceControl);
            mTransparentLayer.setVisibility(mFrontLayerVis ? View.VISIBLE : View.GONE);

            //显示下载按钮
            if (mGameInfo != null && mGameInfo.enableDownload == 1 && !StringUtil.isEmpty(mGameDownUrl) && mDownloadWidVis) {
                mFloatDownView.setVisibility(View.VISIBLE);
                mFloatDownView.startTimeoutLayout();
            }else {
                mFloatDownView.setVisibility(View.GONE);
            }
            requestExitGameList();
            try {
                getWindow().getDecorView().setSystemUiVisibility(getSystemUi());
            } catch (Exception e) {
                e.printStackTrace();
            }

            //已经下载的进度
            int progress = downloadManger.getDownloadProgress(mGameDownUrl);
            //当前游戏的下载状态
            int state = downloadManger.getDownloadState(mGameDownUrl);
            boolean continueDownload = (progress > 2 && progress < 100); //大于2%小于100% （继续下载）
            if (state == KpGameDownloadManger.STATE_FINISHED){
                downloadManger.showPlayWhenDownDialog(GamePlay.this,connIsWifi);
            }else {
                if (continueDownload){
                    if (mFloatDownView != null){
                        mFloatDownView.setProgress(progress,"" + progress + "%");
                    }
                    toggleDownload();
                }
            }
            //wifi情况下的静默下载
            if (isFirstWifiToggleDown && connIsWifi && GameInfo.GAME_DOWNLOADTYPE_SILENT.equals(mGameInfo.downloadType)){
                isFirstWifiToggleDown = false;
                downloadManger.setSpeedLimitEnable(false);

                if (state != KpGameDownloadManger.STATE_FINISHED){
                    showCenterToast();
                }
                if (!continueDownload){ //从头自动下载
                    toggleDownload();
                }
            }

        } catch (Exception e) {
            gameRunSuccess = false;
            Logger.error(TAG, e.getMessage());
        }
    }

    private void showCenterToast(){
        try{
            if (mNetTipDialog == null){
                mNetTipDialog = new ToastNetDialog(GamePlay.this);
            }
            mNetTipDialog.showDialog();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void exitPlay() {
        setResult(RESULT_OK, getIntent());
        finish();
    }

    /**
     * 显示错识页面
     * @param isShowLoadingError  是否显示loading的错误页面
     * @param err  错误信息
     * @param isApplyDeviceError  是否是申请设备成功
     */
    private void showError(boolean isShowLoadingError, String err, boolean isApplyDeviceError) {
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
            int status;
            if (isShowLoadingError){
                if (isApplyDeviceError){
                    status = PlayStatusLayout.STATUS_LOADING_ERROR_STOP;
                }else {
                    status = PlayStatusLayout.STATUS_GAME_RUNNING_ERROR;
                }
            }else {
                status = PlayStatusLayout.STATUS_ERROR;
            }
            mPlayStatueView.setStatus(status, err);
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }

    /**
     * 重试加载游戏
     */
    private void reloadGame() {
        try {
            isFirstWifiToggleDown = true;
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

    private final IGameObservable mGameObserver = new SimpleGameObservable(){

        @Override
        public void onBackListener(boolean isExit) {
            if (isExit){
                if (GamePlay.this.isFinishing()){
                    return;
                }
                exitPlay();
            }
        }

        @Override
        public void onReloadListener() {
            try {
                if (GamePlay.this.isFinishing()){
                    return;
                }
                mHandler.sendEmptyMessage(MSG_RELOAD_GAME);

                //发送打点事件
                Event event = Event.getEvent(EventCode.DATA_ACTIVITY_PLAYERROR_RELOAD, mGameInfo != null ? mGameInfo.pkgName : "");
                event.setErrMsg(GamePlay.this.mErrorMsg);
                if (mDeviceControl != null) {
                    event.setPadcode(mDeviceControl.getPadcode());
                }
                HashMap<String,Object> ext = new HashMap<>();
                ext.put("code", mErrorCode);
                ext.put("msg", mErrorMsg);
                event.setExt(ext);
                MobclickAgent.sendEvent(event);
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        @Override
        public void onCopyInfoListener(String info) {
            if (GamePlay.this.isFinishing() || mDeviceControl == null){
                return;
            }
            String copyStr = mDeviceControl.getDeviceInfo();
            if (info != null && info.length() > 0){
                copyStr = (copyStr + ";" + info);
            }
            StringUtil.copy(GamePlay.this,copyStr);
        }

        @Override
        public void onDownloadListener() {
            if (mGameInfo != null){
                if (GamePlay.this.isFinishing()){
                    return;
                }
                //授权或者错误页面点击下载
                toggleDownload();
            }
        }

        @Override
        public void onAuthListener(boolean isAuthPass) {

            if (!isAuthPass){
                //发送打点事件
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_ACTIVITY_USERAUTH_CANCEL, mGameInfo != null ? mGameInfo.pkgName : ""));

                exitPlay();
                return;
            }

            if (mPlayStatueView != null){
                mPlayStatueView.hideUserAuthView();
            }
            try{
                //发送打点事件
                MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_ACTIVITY_USERAUTH_APPROVE, mGameInfo != null ? mGameInfo.pkgName : ""));
            }catch (Exception e){
                e.printStackTrace();
            }

            try{
                if (GamePlay.this.isFinishing()){
                    return;
                }
                //调用接口发送授权数据
                new AccountTask(GamePlay.this, AccountTask.ACTION_AUTH_GT_API)
                        .setCorpKey(mCorpID)
                        .setCallback(new AccountTask.ICallback() {
                            @Override
                            public void onResult(Map<String, Object> map) {
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
                                        String unionMd5Value = MD5Util.md5(mUnionUUID);
                                        ProferencesUtils.setString(GamePlay.this,SharedKeys.KEY_AUTH_ID, unionMd5Value);
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

                                        if (mUnionUUID != null && mUnionUUID.length() > 0){
                                            map.put("uninqueId",mUnionUUID);
                                        }

                                        JSONObject obj = new JSONObject(map);
                                        String cacheKey = SharedKeys.KEY_GAME_USER_LOGIN_DATA_PRE;
                                        ProferencesUtils.setString(GamePlay.this, cacheKey, obj.toString());
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                startCloudPhone();
                            }
                        })
                        .execute(mAuthUnionAk, mUnionUUID, mCorpID, mAuthUnionTS, mAuthUnionSign);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void updateDownloadStatus(int status, String url) {
            Logger.info(TAG,"updateDownloadStatus:" + status);
            setDownloadStatus(status,url);
        }

        @Override
        public void updateDownloadProgress(long total, long current, String url) {
            double precent = Double.parseDouble(current + "") / Double.parseDouble(total + "");
            int progress = (int) (precent * 100);
            setDownloadProgress(progress, "" + progress + "%",url);
        }
    };

    @Override
    protected void onDestroy() {
        if (mNetTipDialog != null){
            mNetTipDialog.cancel();
            mNetTipDialog = null;
        }
        if (mKpGameReceiver != null){
            unregisterReceiver(mKpGameReceiver);
            mKpGameReceiver = null;
        }
        if (mGameInfo != null && mGameDownUrl != null && downloadManger != null){
            downloadManger.destroyService(GamePlay.this,mGameDownUrl);
        }
        KpGameManager.instance().clearObservable();
        super.onDestroy();

        try {
            if (mDeviceControl != null) {
                mDeviceControl.stopGame();
            }
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
                e.printStackTrace();
            }
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
                    if (mDeviceControl != null){
                        mDeviceControl.stopGame();
                    }
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
            if (downloadManger != null){
                downloadManger.dismissPlayDownDialog();
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
    private long lastBufSize;

    @Override
    public void onPingUpdate(int ping) {
        if (!isFinishing() && mMenuView != null) {
            mMenuView.onPingChanged(ping);

            //动态调整下载限速
            if (downloadManger == null || mGameDownUrl == null || mGameDownUrl.isEmpty()){
                return;
            }
            boolean speedLimitEnable = downloadManger.isSpeedLimitEnable();
            if (!speedLimitEnable){  //不限速
                downloadManger.setSpeedPerSecond(-1);
                return;
            }
            long bufByPing = GameUtils.getBufByPing(ping);
            lastBufSize = bufByPing;
            downloadManger.setSpeedPerSecond(bufByPing);
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

        List<String> lackedPermission = new ArrayList<>();
        if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        /*if (!(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }*/

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
                //授权后执行下载
                toggleDownload();
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
                e.printStackTrace();
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
                        e.printStackTrace();
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
                        e.printStackTrace();
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
                        e.printStackTrace();
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
     */
    private int getExitShowNum() {
        //判断次数
        int num = 0;
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            final String today = sdf.format(new Date());
            String str = ProferencesUtils.getString(this, KEY_EXIT_NUM, null);
            JSONObject obj = new JSONObject(str);
            if (obj.has(today)) {
                num = obj.getInt(today);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return num;
    }

    /**
     * 记录挽留窗显示次数
     */
    private void addExitShowNum() {
        try {
            @SuppressLint("SimpleDateFormat")
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
                            e.printStackTrace();
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

    private void setDownloadStatus(int status, String url) {
        if (url == null || url.isEmpty()){
            return;
        }
        try{
            if (mGameInfo != null && url.equals(mGameDownUrl)) {
                if (downloadManger != null){
                    downloadManger.notifyDownloadState(status);
                }

                if (mPlayStatueView != null && mPlayStatueView.isShown()) {
                    mPlayStatueView.setDownloadStatus(status);
                }

                if (mFloatDownView != null && mFloatDownView.isShown()) {
                    mFloatDownView.setDownloadStatus(status);
                }

            /*if (status == KpGameDownloadManger.STATE_FINISHED && downloadManger != null){
                downloadManger.doInstallApk(GamePlay.this, mGameInfo.pkgName);
            }*/
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void setDownloadProgress(int progress, String text, String url) {
        if (url == null || url.isEmpty()){
            return;
        }
        if (mGameInfo != null && url.equals(mGameDownUrl)) {
            if (mPlayStatueView != null/* && mPlayStatueView.isShown()*/) {
                mPlayStatueView.setProgress(progress, text);
            }

            if (mFloatDownView != null/* && mFloatDownView.isShown()*/) {
                mFloatDownView.setProgress(progress, text);
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
