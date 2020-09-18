package com.yd.yunapp.gamebox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kuaipan.game.demo.R;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import kptech.game.kit.APIConstants;
import kptech.game.kit.GameBox;
import kptech.game.kit.GameBoxManager;
import kptech.game.kit.GameDownloader;
import kptech.game.kit.GameInfo;
import kptech.game.kit.activity.GamePlay;
import kptech.game.kit.ad.AdManager;
import kptech.game.kit.ad.IAdCallback;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.utils.DensityUtil;
import kptech.game.kit.utils.ProferencesUtils;

public class HorizontalHomeActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int PLAY_GAME_REQUEST = 100;
    private static final int MSG_REFRESH_LIST = 1;
    private static final int MSG_SHOW_ERROR = 2;
    private static final int MSG_SHOW_NODATA = 3;
    private static final int MSG_SHOW_EMPTY_DATA = 4;
    private RecyclerView mGameList;
    private LinearLayout mLoadingView;
    private LinearLayout mNetworkErrorView;
    private TextView mTips;
    private FrameLayout mLoadingContainar;
    private HorizontalGameAdapter mGameAdapter;
    private LinkedHashMap<Integer, GameInfo> mGameInfos;

    private GameDownloader mGameDownloader;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            HorizontalHomeActivity.this.handleMessage(msg);
        }
    };

    final String corpId = "2OCYlwVwzqZ2R8m-d27d6a9c5c675a3b";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal_main);
        mGameInfos = new LinkedHashMap<>();
        initView();
        GameBox.init(getApplication(),corpId);
//
//        if (!GameBoxManager.getInstance(this).isGameBoxManagerInited()){
//            GameBoxManager.getInstance(this).init(getApplication(), corpId, new IAdCallback<String>() {
//                @Override
//                public void onAdCallback(String msg, int code) {
//                    if (code == 1){
//
//                    }else {
//                        //初始化失败，退出页面
//                        Toast.makeText(HorizontalHomeActivity.this,"初始化游戏失败", Toast.LENGTH_LONG).show();
//                    }
//                }
//            });
//        }
//
//        //下载类
//        mGameDownloader = new GameDownloader() {
//            @Override
//            public boolean start(final GameInfo gameInfo) {
//                new Thread(){
//                    @Override
//                    public void run() {
//                        download(gameInfo.downloadUrl, gameInfo);
//
//                    }
//                }.start();
//
//                //处理开始下载方法
//                return true;
//            }
//
//            @Override
//            public void stop(GameInfo gameInfo) {
//                //处理停止下载
//                downloadStop();
//            }
//
//        };
//
        gameBox = GameBox.getInstance();
//        gameBox.setGameDownloader(mGameDownloader);
//
//        IntentFilter filter =new  IntentFilter();
//        filter.addAction("KpTech_Game_Kit_DownLoad_Start_Action");
//        filter.addAction("KpTech_Game_Kit_DownLoad_Stop_Action");
//        registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (intent.getAction().equals("KpTech_Game_Kit_DownLoad_Start_Action")){
//                    final GameInfo gameInfo = intent.getParcelableExtra("extra.game");
//                    new Thread(){
//                        @Override
//                        public void run() {
//                            download(gameInfo.downloadUrl, gameInfo);
//                        }
//                    }.start();
//                }else  if (intent.getAction().equals("KpTech_Game_Kit_DownLoad_Stop_Action")){
////                    final GameInfo gameInfo = intent.getParcelableExtra("extra.game");
//                    downloadStop();
//                }
//
//            }
//        }, filter);
    }

    GameBox gameBox;
    //启动云游戏
    public void startGame(View view){

        GameInfo info = new GameInfo();
        info.gid = 1893;
        info.pkgName = "com.netease.tom.guopan";
        info.name = "猫和老鼠";
        info.iconUrl = "http://kp.you121.top/api/image/20200119133131vpiulx.png";
        info.showAd = GameInfo.GAME_AD_SHOW_ON;
        info.downloadUrl = "https://down.qq.com/qqweb/QQ_1/android_apk/AndroidQQ_8.4.5.4745_537065283.apk";

        gameBox.playGame(HorizontalHomeActivity.this,info);

        //预加载广告
//        final AdManager adManager = AdManager.adEnable ? new AdManager(this) : null;
//        if (adManager!=null){
//            adManager.setPackageName(info.pkgName);
//            adManager.prepareAd();
//        }
//
//        adManager.loadGameAd(corpId, info, new IAdCallback() {
//            @Override
//            public void onAdCallback(Object msg, int code) {
//                Log.i("HorizontalHomeActivity", ""+code);
//                //成功连接游戏,删除激励广告标记
//                int adVerify = ProferencesUtils.getIng(HorizontalHomeActivity.this, SharedKeys.KEY_AD_REWARD_VERIFY_FLAG, 0);
//                if (adVerify > 0){
//                    ProferencesUtils.setInt(HorizontalHomeActivity.this, SharedKeys.KEY_AD_REWARD_VERIFY_FLAG, 0);
//                }
//            }
//        });
    }

    private void initView() {
        mLoadingContainar = (FrameLayout) findViewById(R.id.loading_containar);
        mNetworkErrorView = (LinearLayout) findViewById(R.id.fail_ll);
        mLoadingView = (LinearLayout) findViewById(R.id.loading_ll);
        mTips = (TextView) findViewById(R.id.network_error_tips);
        mNetworkErrorView.setOnClickListener(this);
        mGameList = (RecyclerView) findViewById(R.id.game_list);
        mGameList.setHasFixedSize(true);
        if (mGameInfos.size() == 0) {
            mLoadingContainar.setVisibility(View.VISIBLE);
        } else {
            mLoadingContainar.setVisibility(View.GONE);
        }
        mGameAdapter = new HorizontalGameAdapter(this);
        mGameAdapter.setOnItemClickListener(new HorizontalGameAdapter.OnItemClickListener() {
            @Override public void onItemClick(View view, int pos) {
                GameInfo game = (GameInfo) mGameAdapter.getItem(pos);
                game.downloadUrl = "https://down.qq.com/qqweb/QQ_1/android_apk/AndroidQQ_8.4.5.4745_537065283.apk";
//                GameBox box = GameBox.getInstance(getApplication(),corpId);
//                gameBox.setGameDownloader(mGameDownloader);
                gameBox.playGame(HorizontalHomeActivity.this, game);
//                Intent intent = new Intent(HorizontalHomeActivity.this, GamePlay.class);
//                intent.putExtra(GamePlay.EXTRA_GAME, (GameInfo) mGameAdapter.getItem(pos));
//                HorizontalHomeActivity.this.startActivityForResult(intent, HomeActivity.PLAY_GAME_REQUEST);

//                GameInfo info = new GameInfo();
//                info.gid = 1893;
//                info.pkgName = "com.netease.tom.guopan";
//                info.name = "猫和老鼠";
//                info.iconUrl = "http://kp.you121.top/api/image/20200119133131vpiulx.png";
//                GameBox.getInstance(getApplication(),"2OL7hDplsNG3SLS-bacc1a1395641317")
//                        .playGame(HorizontalHomeActivity.this,info);
            }
        });
        mGameList.setAdapter(mGameAdapter);
        loadGame();
    }


    private void loadMoreData() {
        loadGame();
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_REFRESH_LIST:
                mGameAdapter.refresh(new ArrayList<GameInfo>(mGameInfos.values()));
                mTips.setVisibility(View.GONE);
                mLoadingContainar.setVisibility(View.GONE);
                break;
            case MSG_SHOW_ERROR:
                mTips.setVisibility(View.VISIBLE);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mTips.setVisibility(View.GONE);
                    }
                }, 3000);
                break;
            case MSG_SHOW_EMPTY_DATA:
                mLoadingView.setVisibility(View.GONE);
                mLoadingContainar.setVisibility(View.VISIBLE);
                mNetworkErrorView.setVisibility(View.VISIBLE);
                break;
            case MSG_SHOW_NODATA:
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mNetworkErrorView) {
            mNetworkErrorView.setVisibility(View.GONE);
            mLoadingView.setVisibility(View.VISIBLE);
            loadGame();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLAY_GAME_REQUEST && resultCode == RESULT_OK) {
            final GameInfo info = data.getParcelableExtra(GameRunningActivity.EXTRA_GAME);
            new Thread() {
                @Override
                public void run() {
                    // 试玩结束后更新游戏信息
                    GameBoxManager.getInstance(HorizontalHomeActivity.this).updateGameInfo(info);
                    mGameInfos.put(info.gid, info);
                    mHandler.sendEmptyMessage(MSG_REFRESH_LIST);
                }
            }.start();
        }
    }

    private void loadGame() {
        GameBoxManager.setAppKey("2OCYlwVwzqZ2R8m-d27d6a9c5c675a3b");
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<GameInfo> result = GameBoxManager.getInstance(HorizontalHomeActivity.this)
                        .queryGameList(mGameInfos.size(), 50);

//                String tmp_data = "[{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d1921\",\"gid\":1921,\"iconUrl\":\"\",\"name\":\"QQ\",\"pkgName\":\"com.tencent.mobileqq\",\"playCount\":99,\"showAd\":0,\"size\":96018622,\"totalTime\":86400,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2497\",\"gid\":2497,\"iconUrl\":\"\",\"name\":\"明日之后\",\"pkgName\":\"com.netease.mrzh.guopan\",\"playCount\":60,\"showAd\":2,\"size\":2070011001,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2647\",\"gid\":2647,\"iconUrl\":\"\",\"name\":\"网易云音乐\",\"pkgName\":\"com.netease.cloudmusic\",\"playCount\":96,\"showAd\":2,\"size\":83116311,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2652\",\"gid\":2652,\"iconUrl\":\"\",\"name\":\"战神传奇\",\"pkgName\":\"com.zqgame.zscq.kuaipan\",\"playCount\":69,\"showAd\":2,\"size\":246827618,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2760\",\"gid\":2760,\"iconUrl\":\"\",\"name\":\"万象物语\",\"pkgName\":\"com.ilongyuan.sdorica.guopan\",\"playCount\":99,\"showAd\":2,\"size\":228779724,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2761\",\"gid\":2761,\"iconUrl\":\"\",\"name\":\"模拟城市：我是市长\",\"pkgName\":\"com.ea.simcitymobile.guopan\",\"playCount\":80,\"showAd\":1,\"size\":155105034,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2762\",\"gid\":2762,\"iconUrl\":\"\",\"name\":\"迷失岛2：时间的灰烬\",\"pkgName\":\"com.isoland2.lilithgames.guopan\",\"playCount\":99,\"showAd\":1,\"size\":280453188,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2764\",\"gid\":2764,\"iconUrl\":\"\",\"name\":\"梦间集：天鹅座\",\"pkgName\":\"com.pwrd.mjjsl.guopan\",\"playCount\":91,\"showAd\":1,\"size\":1416160752,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2809\",\"gid\":2809,\"iconUrl\":\"\",\"name\":\"传奇盛世2 \",\"pkgName\":\"com.sanjiu.cqss2.guopan\",\"playCount\":53,\"showAd\":1,\"size\":558109309,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2814\",\"gid\":2814,\"iconUrl\":\"\",\"name\":\"葫芦兄弟\",\"pkgName\":\"com.wk.hlxd.wanme\",\"playCount\":68,\"showAd\":1,\"size\":495070658,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2821\",\"gid\":2821,\"iconUrl\":\"\",\"name\":\"战歌竞技场\",\"pkgName\":\"com.tencent.hjzqgame\",\"playCount\":97,\"showAd\":1,\"size\":1009615840,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2823\",\"gid\":2823,\"iconUrl\":\"\",\"name\":\"疯狂原始人（爱奇艺测试通信sdk）\",\"pkgName\":\"com.skymoons.croods.iqiyiyyx\",\"playCount\":78,\"showAd\":1,\"size\":771857656,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2827\",\"gid\":2827,\"iconUrl\":\"\",\"name\":\"爱奇艺测试\",\"pkgName\":\"com.iqiyigame.sdk.demo\",\"playCount\":61,\"showAd\":1,\"size\":11441875,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2851\",\"gid\":2851,\"iconUrl\":\"\",\"name\":\"命运之刃（守护女神）\",\"pkgName\":\"com.bt.myzr.guopan\",\"playCount\":50,\"showAd\":0,\"size\":5757725,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2855\",\"gid\":2855,\"iconUrl\":\"\",\"name\":\"陌陌\",\"pkgName\":\"com.immomo.momo\",\"playCount\":61,\"showAd\":0,\"size\":84801024,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2856\",\"gid\":2856,\"iconUrl\":\"\",\"name\":\"花椒直播\",\"pkgName\":\"com.huajiao\",\"playCount\":81,\"showAd\":0,\"size\":67583573,\"totalTime\":604800,\"usedTime\":0}]";
//                Gson gson = new Gson();
//                Type listType = new TypeToken<List<GameInfo>>() {
//                }.getType();
//                List<GameInfo> result = gson.fromJson(tmp_data, listType);

                if (result != null && result.size() > 0) {
                    for (GameInfo info : result) {
                        mGameInfos.put(info.gid, info);
                    }
                    mHandler.sendEmptyMessage(MSG_REFRESH_LIST);
                } else if (result != null && result.size() == 0 && mGameInfos.size() > 0) {
                    mHandler.sendEmptyMessageDelayed(MSG_SHOW_NODATA, 1000);
                } else if (mGameInfos.size() == 0) {
//                    if (!NetworkUtils.isNetworkAvaialble(MainActivity.this)) {
//                        mHandler.sendEmptyMessage(MSG_SHOW_ERROR);
//                    }
                    mHandler.sendEmptyMessage(MSG_SHOW_EMPTY_DATA);
                } else {
                    mHandler.sendEmptyMessageDelayed(MSG_SHOW_ERROR, 1000);
                }
            }
        }).start();
    }

    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            ((GridLayoutManager) mGameList.getLayoutManager()).setSpanCount(3);
        } else if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_USER) {
            ((GridLayoutManager) mGameList.getLayoutManager()).setSpanCount(5);
        }
    }

    private void downloadStop(){
        if (cancelable!=null && !cancelable.isCancelled()){
            cancelable.cancel();
        }
    }

    private Callback.Cancelable cancelable;
    private String mFilePath;
//    private String mFileName;
    private boolean cancel = false;
    private void download(String url, final GameInfo gameInfo) {
        if (cancel) {
            return;
        }

        File dir = getExternalFilesDir("download");
        if (!dir.exists()){
            dir.mkdir();
        }
        String apkName = url.substring(url.lastIndexOf("/") + 1, url.length());
        File file = new File(dir,apkName);
        mFilePath = file.getPath();


        RequestParams requestParams = new RequestParams(url);
        requestParams.setSaveFilePath(mFilePath);
        /**自动为文件命名**/
        requestParams.setAutoRename(false);
        /**自动为文件断点续传**/
        requestParams.setAutoResume(true);

        cancelable = x.http().get(requestParams, new Callback.ProgressCallback<File>() {
            @Override
            public void onSuccess(File result) {

                if (mGameDownloader!=null){
                    mGameDownloader.onStatusChanged(GameDownloader.STATUS_FINISHED, null, gameInfo);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

                if (mGameDownloader!=null){
                    mGameDownloader.onStatusChanged(GameDownloader.STATUS_ERROR, ex.getMessage(), gameInfo);
                }

                sendBroadcast(new Intent("Cloud_Music_Cloud_Game_DownLoad_Fail"));
            }

            @Override
            public void onCancelled(CancelledException cex) {
                if (mGameDownloader!=null){
                    mGameDownloader.onStatusChanged(GameDownloader.STATUS_CANCEL, null, gameInfo);
                }
                sendBroadcast(new Intent("Cloud_Music_Cloud_Game_DownLoad_Stop"));
            }

            @Override
            public void onFinished() {

            }

            @Override
            public void onWaiting() {
                if (mGameDownloader!=null){
                    mGameDownloader.onStatusChanged(GameDownloader.STATUS_WAITTING, null, gameInfo);
                }
            }

            @Override
            public void onStarted() {
                if (mGameDownloader!=null){
                    mGameDownloader.onStatusChanged(GameDownloader.STATUS_STARTED, null, gameInfo);
                }

                sendBroadcast(new Intent("Cloud_Music_Cloud_Game_DownLoad_Start"));
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                if (mGameDownloader!=null){
                    mGameDownloader.onProgresss(current, total, gameInfo);
                }
            }
        });
    }
}
