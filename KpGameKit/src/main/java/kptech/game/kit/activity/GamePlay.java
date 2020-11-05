package kptech.game.kit.activity;

import android.Manifest;
import android.app.Activity;import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kptech.game.kit.APICallback;
import kptech.game.kit.APIConstants;
import kptech.game.kit.DeviceControl;
import kptech.game.kit.GameBoxManager;
import kptech.game.kit.GameDownloader;
import kptech.game.kit.GameInfo;
import kptech.game.kit.ParamKey;
import kptech.game.kit.Params;
import kptech.game.kit.R;
import kptech.game.kit.activity.hardware.HardwareManager;
import kptech.game.kit.analytic.DeviceInfo;
import kptech.game.kit.analytic.Event;
import kptech.game.kit.analytic.EventCode;
import kptech.game.kit.analytic.MobclickAgent;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.data.AccountTask;
import kptech.game.kit.data.IRequestCallback;
import kptech.game.kit.data.RequestGameExitListTask;
import kptech.game.kit.data.RequestGameInfoTask;
import kptech.game.kit.utils.AnimationUtil;
import kptech.game.kit.utils.DensityUtil;
import kptech.game.kit.utils.DeviceUtils;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.MD5Util;
import kptech.game.kit.utils.ProferencesUtils;
import kptech.game.kit.utils.StringUtil;
import kptech.game.kit.view.FloatDownView;
import kptech.game.kit.view.FloatMenuView;
import kptech.game.kit.view.LoadingView;
import kptech.game.kit.view.PlayErrorView;
import kptech.game.kit.view.UserAuthView;


public class GamePlay extends Activity implements APICallback<String>, DeviceControl.PlayListener{

    public static final String EXTRA_CORPID = "extra.corpid";
    public static final String EXTRA_GAME = "extra.game";
//    public static final String EXTRA_TIMEOUT = "extra.timeout";
    public static final String EXTRA_PARAMS = "extra.params";

    private Logger logger = new Logger("GamePlay");

    private static final int MSG_SHOW_ERROR = 1;
    private static final int MSG_RELOAD_GAME = 2;
    private static final int MSG_SHOW_AUTH = 3;

    private ViewGroup mContentView;
    private FrameLayout mVideoContainer;
    private FloatMenuView mMenuView;

    private LoadingView mLoadingView;
    private PlayErrorView mErrorView;
    private FloatDownView mFloatDownView;
    private UserAuthView mUserAuthView;

    private HardwareManager mHardwareManager;

    private long mBackClickTime;
    private DeviceControl mDeviceControl;

    private GameInfo mGameInfo;
    private String mCorpID;

    private long fontTimeout = 5 * 60;
    private long backTimeout = 3 * 60;

    private int mErrorCode = -1;
    private String mErrorMsg = null;

//    private boolean mVideoContainerScale = false;

//    private int mPro = 0;
//    private boolean mPuasePro = false;

//    private GameDownloader mGameDownloader;
//    private GameBox mGameBox;

    private Params mCustParams;

    private boolean mEnableExitGameAlert = false;
    private List<GameInfo> mExitGameList = null;

    private String mUnionUUID = null;

    private int systemUi = -1;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {

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
                   showUserAuthView();
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
        if (getIntent().hasExtra(EXTRA_PARAMS)){
            try {
                mCustParams = (Params) getIntent().getSerializableExtra(EXTRA_PARAMS);
            }catch (Exception e){}
        }
        if (mCustParams == null){
            mCustParams = new Params();
        }

        fontTimeout = mCustParams.get(ParamKey.GAME_OPT_TIMEOUT_FONT,5 * 60);
        backTimeout = mCustParams.get(ParamKey.GAME_OPT_TIMEOUT_BACK,3 * 60);

        mEnableExitGameAlert = mCustParams.get(ParamKey.GAME_OPT_EXIT_GAMELIST, true);

        mUnionUUID = mCustParams.get(ParamKey.GAME_AUTH_UNION_UUID, null);
        GameBoxManager.getInstance(this).setUniqueId(mUnionUUID);

        initView();
        mHardwareManager = new HardwareManager(this);

        try {
            //统计事件初始化
            Event.init(getApplication(), mCorpID);

            //发送打点事件
            MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_ACTIVITY_PLAYGAME_ONCREATE, mGameInfo!=null ? mGameInfo.pkgName : "" ));
        }catch (Exception e){
        }

        //未获取到游戏信息
        if (mCorpID == null || mGameInfo == null){
            mHandler.sendMessage(Message.obtain(mHandler, MSG_SHOW_ERROR, "获取游戏信息失败"));
            return;
        }

        checkAndRequestPermission();

//        mGameBox = GameBox.getInstance();
//        if (mGameBox!=null){
//            mGameDownloader = mGameBox.getGameDownloader();
//        }
//        if (mGameDownloader != null){
//            mGameDownloader.addCallback(this);
//        }
//        logger.info("mGameDownloader " + mGameDownloader);

        logger.info("Activity Process，pid:" + android.os.Process.myPid());

        //注册下载
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("Cloud_Music_Cloud_Game_DownLoad_Start");
        intentFilter.addAction("Cloud_Music_Cloud_Game_DownLoad_Fail");
        intentFilter.addAction("Cloud_Music_Cloud_Game_DownLoad_Stop");

        registerReceiver(mDownloadReceiver, intentFilter);


    }

    private void initView() {

        mContentView = findViewById(R.id.content_view);

        mLoadingView = findViewById(R.id.loading_view);

        int iconRes = mCustParams.get(ParamKey.ACTIVITY_LOADING_ICON,-1);
        if (iconRes > 0){
            try {
                getResources().getResourceTypeName(iconRes);
                mLoadingView.setIconImageResource(iconRes);
            }catch (Exception e){
                mLoadingView.setIconImageResource(R.mipmap.loading_icon);
            }
        }

        mMenuView = (FloatMenuView) findViewById(R.id.float_menu);
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

        mVideoContainer = (FrameLayout) findViewById(R.id.play_container);

        mErrorView = findViewById(R.id.error_view);
        mErrorView.setGameInfo(mGameInfo);
        mErrorView.setOnBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mErrorView.setOnRetryListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //重新加载游戏
                mHandler.sendEmptyMessage(MSG_RELOAD_GAME);

                try {
                    //发送打点事件
                    Event event = Event.getEvent(EventCode.DATA_ACTIVITY_PLAYERROR_RELOAD, mGameInfo!=null ? mGameInfo.pkgName : "" );
                    event.setErrMsg(GamePlay.this.mErrorMsg);
                    if (mDeviceControl!=null){
                        event.setPadcode(mDeviceControl.getPadcode());
                    }
                    HashMap ext = new HashMap();
                    ext.put("code", mErrorCode);
                    ext.put("msg", mErrorMsg);
                    event.setExt(ext);
                    MobclickAgent.sendEvent(event);
                }catch (Exception e){
                }
            }
        });
        mErrorView.setOnDownListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                downloadApk(view);

            }
        });

        mFloatDownView = findViewById(R.id.float_down);
        mFloatDownView.setOnDownListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                downloadApk(view);

            }
        });

        mUserAuthView = findViewById(R.id.auth_view);
        mUserAuthView.setOnAuthListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    //发送打点事件
                    MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_ACTIVITY_USERAUTH_APPROVE, mGameInfo!=null ? mGameInfo.pkgName : "" ));
                }catch (Exception e){
                }

                //调用接口发送授权数据
                new AccountTask(GamePlay.this, AccountTask.ACTION_AUTH_CHANNEL_UUID)
                        .setCorpKey(mCorpID)
                        .setCallback(new AccountTask.ICallback() {
                            @Override
                            public void onResult(Map<String, Object> map) {
                                //保存数据
                                String key = MD5Util.md5(mUnionUUID + mGameInfo.pkgName);
                                ProferencesUtils.setInt(GamePlay.this, key, 1);
                            }
                        })
                        .execute(mUnionUUID, mGameInfo.pkgName);

                startCloudPhone();
                hideUserAuthView();
            }
        });
        mUserAuthView.setOnBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitPlay();

                try {
                    //发送打点事件
                    MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_ACTIVITY_USERAUTH_CANCEL, mGameInfo!=null ? mGameInfo.pkgName : "" ));
                }catch (Exception e){
                }
            }
        });
    }

    /**
     * 显示授权界面
     */
    private void showUserAuthView(){
        if (mUserAuthView.getVisibility() == View.VISIBLE){
            return;
        }
        mUserAuthView.setInfo(mGameInfo.name, mGameInfo.iconUrl);
        mUserAuthView.setAnimation(AnimationUtil.moveToViewLocation());
        mUserAuthView.setVisibility(View.VISIBLE);

        try {
            //发送打点事件
            MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_ACTIVITY_USERAUTH_DISPLAY, mGameInfo!=null ? mGameInfo.pkgName : "" ));
        }catch (Exception e){
        }
    }

    /**
     * 隐藏授权界面
     */
    private void hideUserAuthView(){
        if (mUserAuthView.getVisibility() != View.VISIBLE){
            return;
        }
        mUserAuthView.setAnimation(AnimationUtil.moveToViewBottom());
        mUserAuthView.setVisibility(View.GONE);
    }

    private void downloadApk(View view){
        //判断网络状态
        if (DeviceUtils.getNetworkType(this) == ConnectivityManager.TYPE_MOBILE){
            Toast.makeText(this,"您当前正在使用数据流量。", Toast.LENGTH_SHORT).show();
        }

        boolean stop = false;
        if (mDownloadStatus == GameDownloader.STATUS_STARTED){
            //发送下载广播
            Intent intent = new Intent("KpTech_Game_Kit_DownLoad_Stop_Action");
            intent.putExtra(EXTRA_GAME, mGameInfo);
            GamePlay.this.sendBroadcast(intent);
            stop = true;
        }else {
            //发送下载广播
            Intent intent = new Intent("KpTech_Game_Kit_DownLoad_Start_Action");
            intent.putExtra(EXTRA_GAME, mGameInfo);
            GamePlay.this.sendBroadcast(intent);
            stop = false;
        }

        //发送打点数据
        try {
            Event event = null;
            if (view.getId() == R.id.error_down_layout){
                //错误界面点的下载
                String eventCode = stop ? EventCode.DATA_ACTIVITY_PLAYERROR_DOWNLOADSTOP : EventCode.DATA_ACTIVITY_PLAYERROR_DOWNLOAD;
                event = Event.getEvent(eventCode, mGameInfo!=null ? mGameInfo.pkgName : "");
                event.setErrMsg("code:"+mErrorCode+",err:"+mErrorMsg);

            }else if (view.getId() == R.id.down_btn){
                //边玩边下按钮
                String eventCode = stop ? EventCode.DATA_ACTIVITY_PLAYGAME_DOWNLOADSTOP : EventCode.DATA_ACTIVITY_PLAYGAME_DOWNLOAD;
                event = Event.getEvent(eventCode, mGameInfo!=null ? mGameInfo.pkgName : "");
            }
            if (event!=null){
                if (mDeviceControl!=null){
                    event.setPadcode(mDeviceControl.getPadcode());
                }
                MobclickAgent.sendEvent(event);
            }
        }catch (Exception e) {
            logger.error(e.getMessage());
        }
    }


//    private void downloadApk(boolean error){
////        Toast.makeText(GamePlay.this,"FloatDownView Clicked 1", Toast.LENGTH_SHORT).show();
//        logger.info("downloadApk downloader:" + mGameDownloader + ",game："+mGameInfo);
//        if (mGameInfo!=null && !StringUtil.isEmpty(mGameInfo.downloadUrl)){
//            //下载逻辑
//            if (mGameDownloader == null){
//                if (mGameBox==null){
//                    mGameBox = GameBox.getInstance();
//                }
//                if (mGameBox!=null){
//                    mGameDownloader = mGameBox.getGameDownloader();
//                }
//            }
//
//            //判断下载中，则暂停
//            if (mGameDownloader != null){
////                Toast.makeText(GamePlay.this,"FloatDownView Clicked 2", Toast.LENGTH_SHORT).show();
//                if (mDownloadStatus == GameDownloader.STATUS_STARTED){
//                    logger.info("downloadApk stop");
//                    //点击停止
//                    mGameDownloader.stop(mGameInfo);
//                }else if(mDownloadStatus == GameDownloader.STATUS_FINISHED){
//                    //点击安装
//
//                }else{
////                    Toast.makeText(GamePlay.this,"FloatDownView Clicked 3", Toast.LENGTH_SHORT).show();
//
//
//
//                    //点击下载
//                    boolean b = mGameDownloader.start(mGameInfo);
//                    if (!b){
//                        Toast.makeText(this,"下载失败", Toast.LENGTH_LONG).show();
//                    }
//                    logger.info("downloadApk start");
//
//                    try {
//                        if (error){
//                            //错误界面发送事件
//                            Event event = Event.getEvent(EventCode.DATA_ACTIVITY_PLAYERROR_DOWNLOAD, mGameInfo!=null ? mGameInfo.pkgName : "" );
//                            event.setErrMsg(GamePlay.this.mErrorMsg);
//                            if (mDeviceControl!=null){
//                                event.setPadcode(mDeviceControl.getPadcode());
//                            }
//                            HashMap ext = new HashMap();
//                            ext.put("code", mErrorCode);
//                            ext.put("msg", mErrorMsg);
//                            event.setExt(ext);
//                            MobclickAgent.sendEvent(event);
//                        }else {
//                            //游戏界面发送
//                            Event event = Event.getEvent(EventCode.DATA_ACTIVITY_PLAYGAME_DOWNLOAD, mGameInfo!=null ? mGameInfo.pkgName : "" );
//                            if (mDeviceControl!=null){
//                                event.setPadcode(mDeviceControl.getPadcode());
//                            }
//                            MobclickAgent.sendEvent(event);
//                        }
//                    }catch (Exception e){
//                    }
//
//                }
//            }else {
//                logger.info("downloadApk error: gameDownloader null");
////                Toast.makeText(GamePlay.this,"FloatDownView Clicked 4", Toast.LENGTH_SHORT).show();
//            }
//        }else {
//            //下载逻辑
//            Toast.makeText(GamePlay.this, "未获取到下载地址", Toast.LENGTH_SHORT).show();
//        }
//    }


    private void checkInitCloudPhoneSDK(){
        //判断是否已经初始化
        if (!GameBoxManager.getInstance(this).isGameBoxManagerInited()){
            //初始化
            mLoadingView.setText("正在设备初始化...");
//            mHandler.sendMessage(Message.obtain(mHandler, PROGRESS_BAR_, 0));
            GameBoxManager.getInstance(this).init(getApplication(), this.mCorpID, new APICallback<String>() {
                @Override
                public void onAPICallback(String msg, int code) {
                    if (code == 1){
                        getGameInfo();
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
        getGameInfo();
    }

    private void getGameInfo(){
        mLoadingView.setText("获取游戏信息...");
        new RequestGameInfoTask(this).setRequestCallback(new IRequestCallback<GameInfo>() {
            @Override
            public void onResult(GameInfo game, int code) {
                try {
                    if (game!=null){
                        //处理广告显示
                        if (mGameInfo.showAd != GameInfo.GAME_AD_SHOW_AUTO){
                            game.showAd = mGameInfo.showAd;
                        }
                        mGameInfo = game;
                    }
                }catch (Exception e){
                    logger.error(e.getMessage());
                }

                //判断是否需要显示授权界面
                if (mGameInfo.kpUnionGame == 1 && mUnionUUID!=null){
                    String key = MD5Util.md5(mUnionUUID + mGameInfo.pkgName);
                    int auth = ProferencesUtils.getIng(GamePlay.this, key, 0);
                    if (auth == 0){
                        mHandler.sendEmptyMessage(MSG_SHOW_AUTH);
                        return;
                    }
                }
                //启动游戏
                startCloudPhone();

            }
        }).execute(mCorpID, mGameInfo.pkgName);

    }

    private void startCloudPhone() {
        mLoadingView.setText("正在连接设备...");
        GameBoxManager.getInstance(this).applyCloudDevice(this, mGameInfo, false, new APICallback<DeviceControl>() {
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

    }


    private void startGame() {
        //获取云手机分辨率，按比例显示画面
        initVideoSize();

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
//        if (msg != null) {
            logger.info("gameOnAPICallback, code = "+code+", apiResult = " + msg);
//        }
        if (code == APIConstants.AD_LOADING){
            mLoadingView.setText("正在加载广告...");
        }else if (code == APIConstants.GAME_LOADING){
            mLoadingView.setText("正在加载游戏...");
        }else if (code == APIConstants.RECOVER_DATA_LOADING) {
            mLoadingView.setText("初始游戏数据...");
        }else if (code == APIConstants.CONNECT_DEVICE_SUCCESS || code == APIConstants.RECONNECT_DEVICE_SUCCESS) {
            this.mErrorMsg = null;
            mDeviceControl.setPlayListener(this);
            playSuccess();
        } else if(code == com.yd.yunapp.gameboxlib.APIConstants.RELEASE_SUCCESS){
            if (mDeviceControl!=null){
                mDeviceControl.setPlayListener(null);
            }

            if (mChangeGame){
                mChangeGame = false;
                mHandler.sendEmptyMessage(MSG_RELOAD_GAME);
            }


        }else if(code < 0){
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

            this.mErrorCode = code;
            this.mErrorMsg = msg != null ? msg : getErrorText(code);
            mHandler.sendMessage(Message.obtain(mHandler, MSG_SHOW_ERROR, getErrorText(code)));

        }
    }

    private void playSuccess() {
        mLoadingView.setVisibility(View.GONE);

        mVideoContainer.setVisibility(View.VISIBLE);
        mMenuView.setVisibility(View.VISIBLE);
        mMenuView.setDeviceControl(mDeviceControl);

        //显示下载按钮
        if (mGameInfo!=null && !StringUtil.isEmpty(mGameInfo.downloadUrl)){
            mFloatDownView.setVisibility(View.VISIBLE);
        }

        requestExitGameList();
    }

    private void exitPlay() {
        setResult(RESULT_OK, getIntent());
        finish();
    }

    /**
     * 显示错识页面
     * @param err
     */
    private void showError(String err){
        mLoadingView.setVisibility(View.GONE);
        mVideoContainer.setVisibility(View.GONE);
        mMenuView.setVisibility(View.GONE);
        mFloatDownView.setVisibility(View.GONE);

        mErrorView.setGameInfo(mGameInfo);
        mErrorView.setVisibility(View.VISIBLE);
        mErrorView.setErrorText(err);

    }

    /**
     * 重试加载游戏
     */
    private void reloadGame(){
        mLoadingView.setVisibility(View.VISIBLE);
        mVideoContainer.setVisibility(View.GONE);
        mMenuView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.GONE);
        mFloatDownView.setVisibility(View.GONE);

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

        try{
            //发送打点事件
            MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_ACTIVITY_PLAYGAME_DESTORY, mGameInfo!=null ? mGameInfo.pkgName : "" ));
        }catch (Exception e){
        }

//        if (mGameDownloader!=null){
//            mGameDownloader.removeCallback(this);
//        }

        try{
            unregisterReceiver(mDownloadReceiver);
            mDownloadReceiver = null;
        }catch (Exception e) {

        }

    }

//    @Override
//    public void onBackPressed() {
//        if ((System.currentTimeMillis() - mBackClickTime) > 3000) {
//            //弹出挽留窗口,从挽留窗口启动的，不弹界面
//            if (!showExitGameListDialog(this)){
//                mBackClickTime = System.currentTimeMillis();
//                //退出
//                Toast.makeText(this, "再按一次退出游戏", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            exitPlay();
//        }
//    }

    @Override
    public void onBackPressed() {
        if (mDeviceControl==null || mDeviceControl.isReleased()){
            exitPlay();
            return;
        }
        //弹出挽留窗口
        if (showExitGameListDialog(this)){
            return;
        }
        //弹出退出窗口
        if (showExitDialog()){
            return;
        }

        exitPlay();

    }

    private boolean showExitDialog() {
        ExitDialog dialog = new ExitDialog(this);
        dialog.setOnExitListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitPlay();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (systemUi != -1){
                    GamePlay.this.getWindow().getDecorView().setSystemUiVisibility(systemUi);
                    systemUi = -1;
                }
            }
        });
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                systemUi = GamePlay.this.getWindow().getDecorView().getSystemUiVisibility();
            }
        });
        dialog.show();
        return true;
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
        Toast.makeText(this, String.format("[%s]无操作超时 %ds 退出！", type == 1 ? "后台" : "前台", timeout/1000), Toast.LENGTH_LONG).show();

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
        if (requestCode != CODE_REQUEST_PERMISSION) return;

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

//    private int mDownloadStatus;
//    @Override
//    public void onDownloadStatusChanged(int status, String msg, GameInfo game) {
//        logger.info("onDownloadStatusChanged: " + status + ", game:" + game);
//
//        if (game != null && game.gid != mGameInfo.gid){
//            return;
//        }
//        mDownloadStatus = status;
//        switch (status){
//            case GameDownloader.STATUS_STARTED:
//                setDownProgress(0,"下载中...", true);
//                break;
//            case GameDownloader.STATUS_PAUSED:
//                setDownProgress(0,"已暂停，点击继续", false);
//                break;
//            case GameDownloader.STATUS_FINISHED:
//                setDownProgress(100,"下载完成", true);
//                break;
//            case GameDownloader.STATUS_STOPED:
//            case GameDownloader.STATUS_CANCEL:
//                setDownProgress(0,"已停止，点击下载", false);
//                break;
//            case GameDownloader.STATUS_WAITTING:
//                setDownProgress(0,"等待下载", false);
//                break;
//            case GameDownloader.STATUS_ERROR:
//                setDownProgress(0,"下载出错", false);
//                break;
//        }
//    }
//
//    @Override
//    public void onDownloadProgress(long total, long current, GameInfo game) {
//        logger.info("onDownloadProgress: " + current + ", game:" + game);
//        if (game != null && game.gid != mGameInfo.gid){
//            return;
//        }
//
//        if (mDownloadStatus != GameDownloader.STATUS_STARTED){
//            return;
//        }
//
//        float prcent = (float) total / (float)current;
//        if (prcent > 1){
//            prcent = 1;
//        }else if(prcent < 0){
//            prcent = 0;
//        }
//
//        int num = (int)(prcent * 100);
//
//        logger.info("onDownloadProgress: "+num+"%");
//        setDownProgress(num, ""+num+"%", true);
//    }
//
//    private void setDownProgress(int progress, String text, boolean downing){
//        if (mErrorView!=null && mErrorView.isShown()){
//            mErrorView.setProgress(progress, text);
//        }
//
//        if (mFloatDownView!=null && mFloatDownView.isShown()){
//            if (downing){
//                mFloatDownView.setProgress(progress, text);
//            }else {
//                mFloatDownView.setProgress(progress, "边玩边下");
//            }
//        }
//    }

    private BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("Cloud_Music_Cloud_Game_DownLoad_Start")){
                //开始下载
                updateDownStatus(GameDownloader.STATUS_STARTED);

                try{
                    //发送打点事件
                    MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_ACTIVITY_RECEIVE_DOWNLOADSTART,  mGameInfo!=null ? mGameInfo.pkgName : ""));
                }catch (Exception e){
                }

            }else if (intent.getAction().equals("Cloud_Music_Cloud_Game_DownLoad_Fail")){
                //下载失败
                updateDownStatus(GameDownloader.STATUS_ERROR);

                try{
                    //发送打点事件
                    MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_ACTIVITY_RECEIVE_DOWNLOADERROR,  mGameInfo!=null ? mGameInfo.pkgName : ""));
                }catch (Exception e){
                }

            }else if (intent.getAction().equals("Cloud_Music_Cloud_Game_DownLoad_Stop")){
                //停止下载
                updateDownStatus(GameDownloader.STATUS_STOPED);

                try{
                    //发送打点事件
                    MobclickAgent.sendEvent(Event.getEvent(EventCode.DATA_ACTIVITY_RECEIVE_DOWNLOADSTOP,  mGameInfo!=null ? mGameInfo.pkgName : ""));
                }catch (Exception e){
                }
            }
        }
    };

    private int mDownloadStatus;
    private void updateDownStatus(int status){
        mDownloadStatus = status;

        if (status == GameDownloader.STATUS_STARTED){
            Toast.makeText(this,"下载中...", Toast.LENGTH_LONG).show();
        }else if (status == GameDownloader.STATUS_ERROR){
            Toast.makeText(this,"下载失败", Toast.LENGTH_LONG).show();
        }else if (status == GameDownloader.STATUS_STOPED){
            Toast.makeText(this,"停止下载", Toast.LENGTH_LONG).show();
        }

        if (mErrorView!=null && mErrorView.isShown()){
            mErrorView.setDownloadStatus(status);
        }

        if (mFloatDownView!=null && mFloatDownView.isShown()){
            mFloatDownView.setDownloadStatus(status);
        }
    }


    int resizeWidth = 0;
    int resizeHeight = 0;

    /**
     * 初始化显示画面比例尺寸
     */
    private void initVideoSize(){
        if (mDeviceControl == null){
            return;
        }
        //获取云手机分辨率，按比例显示画面
        int[] size = mDeviceControl.getVideoSize();
        if (size!=null && size.length==2){
            //视频尺寸
            int vw = size[0];
            int vh = size[1];

            //屏幕尺寸
            int sw = mContentView.getWidth();
            int sh = mContentView.getHeight();

            if ( sw<=0 || sh<=0 ){
                sw = DensityUtil.getScreenWidth(this);
                sh = DensityUtil.getScreenHeight(this);
            }

            //处理横竖屏
            int screenWidth = sh < sw ? sh : sw;
            int screenHeight = sh < sw ? sw : sh;
            int videoWidth = vh < vw ? vh : vw;
            int videoHeight = vh < vw ? vw : vh;

            //宽高比
            float videoScale = (float)videoWidth/(float)videoHeight;
            float screenScale = (float)screenWidth/(float)screenHeight;

            float widthScale = (float)videoWidth/(float)screenWidth;
            float heightScale = (float)videoHeight/(float)screenHeight;

            if (widthScale < heightScale){
                resizeHeight = screenHeight;
                resizeWidth = (int)(screenHeight * videoScale);
            }else {
                resizeWidth = screenWidth;
                resizeHeight = (int)(screenWidth / videoScale);
            }
        }

        resizeVideoContainer(mMenuView.mVideoScale);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resizeVideoContainer(mMenuView.mVideoScale);
    }

    /**
     * 修改显示画面比例
     * @param scale
     */
    private synchronized void resizeVideoContainer(boolean scale){
        if (scale){
            Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
            int ori = mConfiguration.orientation; //获取屏幕方向
            if (resizeWidth > 0 && resizeHeight > 0){
                if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                    //横屏
                    ViewGroup.LayoutParams lp =  mVideoContainer.getLayoutParams();
                    lp.width = resizeHeight;
                    lp.height = resizeWidth;
                    mVideoContainer.setLayoutParams(lp);
                } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
                    //竖屏
                    ViewGroup.LayoutParams lp =  mVideoContainer.getLayoutParams();
                    lp.width = resizeWidth;
                    lp.height = resizeHeight;
                    mVideoContainer.setLayoutParams(lp);
                }
            }
        }else {
            //全屏
            ViewGroup.LayoutParams lp =  mVideoContainer.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoContainer.setLayoutParams(lp);
        }
    }

    /**
     * 切换游戏
     */
    private boolean mChangeGame = false;
    private void changeGame(GameInfo game){
        if (game == null || game.gid == 0){
            return;
        }

        try{
            //发送打点事件
            Event event = Event.getEvent(EventCode.DATA_DIALOG_EXITLIST_CHANGEGAME);
            event.setGamePkg(game.pkgName);
            MobclickAgent.sendEvent(event);
        }catch (Exception e){
        }

        //关闭换留窗显示
        mEnableExitGameAlert = false;
        //删除数据
        mExitGameList = null;

        mChangeGame = true;

        //更换游戏信息
        mGameInfo = game;

        //关闭当前游戏
        if (mDeviceControl != null && !mDeviceControl.isReleased()){
            mDeviceControl.stopGame();
        }else {
            mHandler.sendEmptyMessage(MSG_RELOAD_GAME);
        }
    }

    /**
     * 显示挽留窗口
     * @param activity
     * @return
     */
    public boolean showExitGameListDialog(Activity activity){
        if (mExitGameList==null || mExitGameList.size()<=0){
            return false;
        }

        try {

            final ExitGameListDialog dialog = new ExitGameListDialog(activity, mExitGameList);
            dialog.setCallback(new ExitGameListDialog.ICallback() {
                @Override
                public void onGameItem(GameInfo gameInfo) {
                    dialog.dismiss();
                    changeGame(gameInfo);
                }

                @Override
                public void onExit() {
                    dialog.dismiss();
                    exitPlay();

                    try{
                        //发送打点事件
                        Event event = Event.getEvent(EventCode.DATA_DIALOG_EXITLIST_EXITBTN, mGameInfo.pkgName);
                        MobclickAgent.sendEvent(event);
                    }catch (Exception e){
                    }
                }

                @Override
                public void onClose(){
                    dialog.dismiss();

                    try{
                        //发送打点事件
                        Event event = Event.getEvent(EventCode.DATA_DIALOG_EXITLIST_CANCELBTN, mGameInfo.pkgName);
                        MobclickAgent.sendEvent(event);
                    }catch (Exception e){
                    }
                }
            });
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    systemUi = GamePlay.this.getWindow().getDecorView().getSystemUiVisibility();

                    //增加显示数量
                    addExitShowNum();

                    try{
                        //发送打点事件
                        Event event = Event.getEvent(EventCode.DATA_DIALOG_EXITLIST_DISPLAY, mGameInfo.pkgName);
                        MobclickAgent.sendEvent(event);
                    }catch (Exception e){
                    }
                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    if (systemUi != -1){
                        GamePlay.this.getWindow().getDecorView().setSystemUiVisibility(systemUi);
                        systemUi = -1;
                    }
                }
            });

            dialog.show();

            return true;
        }catch (Exception e){

        }

        return false;
    }


    private static final String KEY_EXIT_NUM = "kp_game_exit_dialog_num";

    /**
     * 获取挽留窗显示次数
     * @return
     */
    private int getExitShowNum(){
        //判断次数
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final String today = sdf.format(new Date());
        int num = 0;
        try {
            String str = ProferencesUtils.getString(this, KEY_EXIT_NUM, null);
            JSONObject obj = new JSONObject(str);
            if (obj.has(today)){
                num = obj.getInt(today);
            }

        }catch (Exception e){}
        return num;
    }

    /**
     * 记录挽留窗显示次数
     */
    private void addExitShowNum(){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            final String today = sdf.format(new Date());
            int num = getExitShowNum();
            HashMap<String,Object> map = new HashMap<>();
            map.put(today, num+1);
            ProferencesUtils.setString(this, KEY_EXIT_NUM, new JSONObject(map).toString());
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    /**
     * 请求挽留窗游戏数据
     */
    private void requestExitGameList() {
        try {
            if (mExitGameList!=null){
                return;
            }

            //判断是否要显示挽留窗
            if (!mEnableExitGameAlert){
                mExitGameList = null;
                return;
            }

            //获取总数
            int mExitAlertCount = ProferencesUtils.getIng(this, SharedKeys.KEY_GAME_EXITALERTCOUNT_CONF, 0);

            int num = getExitShowNum();

            //超过显示数量,不显示
            if (num >= mExitAlertCount){
                return ;
            }

            //获取数据
            new RequestGameExitListTask(this)
                    .setRequestCallback(new IRequestCallback<List<GameInfo>>() {
                        @Override
                        public void onResult(List<GameInfo> list, int code) {
                            if (list!=null && list.size() > 0){
                                mExitGameList = new ArrayList<>();
                                mExitGameList.addAll(list);
                            }
                        }
                    })
                    .execute(mCorpID, mGameInfo.kpGameId);
        }catch (Exception e){
            logger.error(e.getMessage());
        }

    }
}
