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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kuaipan.game.demo.R;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import kptech.game.kit.BuildConfig;
import kptech.game.kit.GameBox;
import kptech.game.kit.GameBoxManager;
import kptech.game.kit.GameDownloader;
import kptech.game.kit.GameInfo;
import kptech.game.kit.ParamKey;
import kptech.game.kit.Params;
import kptech.game.kit.activity.ExitDialog;
import kptech.game.kit.activity.ExitGameListDialog;
import kptech.game.kit.analytic.DeviceInfo;

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

    static final boolean wy = false;//BuildConfig.APPLICATION_ID.equals("com.netease.cloudmusic");

    final String jidou = "2OV3w2Cabzl2Dw8-eb8758a7b094d246";//"2OV3sQEr3Dm1zY1-e5cffa0d176cc004";


    final String corpId = wy ? "2OCYlwVwzqZ2R8m-d27d6a9c5c675a3b" : BuildConfig.DEBUG ?  "2OQCrVnJuES1AVO-ac995a9fef8adcdb" : "2OPhcwdOhFq2uXl-1bcef9c0bf0a668a";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal_main);
        mGameInfos = new LinkedHashMap<>();
        initView();
        GameBox.init(getApplication(),corpId);



        String str = DeviceInfo.getDeviceId(this);

        Log.e("MainActivity",str);

//        GameBoxManager.getInstance(getApplication()).init(getApplication(), corpId, null);

//        GameBoxManager.setAppInfo("qpGwICisRHSMLv6jmoBKP9cU", "vfwBDe7YrVGLK4R89zphxCUba13cPTtM2dyOnIHu", "aa");
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
//        gameBox = GameBox.getInstance();
//        gameBox.setGameDownloader(mGameDownloader);
//
        IntentFilter filter =new  IntentFilter();
        filter.addAction("KpTech_Game_Kit_DownLoad_Start_Action");
        filter.addAction("KpTech_Game_Kit_DownLoad_Stop_Action");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("KpTech_Game_Kit_DownLoad_Start_Action")){
                    final GameInfo gameInfo = intent.getParcelableExtra("extra.game");
                    new Thread(){
                        @Override
                        public void run() {
                            download(gameInfo.downloadUrl, gameInfo);
                        }
                    }.start();
                }else  if (intent.getAction().equals("KpTech_Game_Kit_DownLoad_Stop_Action")){
//                    final GameInfo gameInfo = intent.getParcelableExtra("extra.game");
                    downloadStop();
                }

            }
        }, filter);
    }


    String access_token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxNTg1MDM2OTY5MjEyNTI5IiwianRpIjoiMzAyY2FhYmIzODU3MTRiMmU2ODIzOTYwMzk5YjcxYTI5ZDU2Y2M2MzUyNTI1Mzk3YjA0NDQwYjZlNjdmOTc3ZDgxYjUwZmU4YWQ0NWIwN2EiLCJpYXQiOjE2MDIyMjgwNTEsIm5iZiI6MTYwMjIyODA1MSwiZXhwIjoxNjEwMTc2ODUxLCJzdWIiOiIzNDgzZWEyNjc5NTc0MjliYTA1OTBmOWNmZTRiYTk4ZCIsInNjb3BlcyI6WyJnZXRVc2VySW5mbyIsImxvZ2luT3V0IiwidXBkYXRlUGFzc3dvcmQiXX0.jCiMgLKd-Pi0Y-EbPfLd9XPZSRJ8Tre6_8Vn0fnhHf9KixiS0AB9GL30oQvDpLqwHrIanCDswgty_7D3n_vsCkygfjmXeAqOTxUC4iEF_swZkOFdvocjyTXfyIV8IEGmoDy3o6kH_mLX11iJ4eyuii6CoValgCxWRB5aH_ByighSbBp0PfNmND7BLQjIo_6fPjK11r1cEbulFmpYuSmbwcu2XsuKjCfRXdXiG_lTzLHi9_UYUxwIwdTHYDxmAkW159IB0FWhnR0r6RzHWyNlIhR794EqurDkRKKGfs49WxMbf7MNQNS9gcoiS4yt--T5uzBrt0MV28ZkRy5gBQSRO6mRcimOEnulCRolmpBZvHbwXPUYVulUBAmQSiKTlQE-SYUzstK5UtqDnyvGPmtUCz7z9Dw36IR9LRS_ksnj58agCJQpssg_ZK6LDXx0NQksTL46hqI4KBpB8-kAPCce5a_t_q_XSDladnpXZYSYtn1VMYA5YNLeiSC_bWSXgffJfqLMXhryfpUaMOxnxlRt9L9dCiokZz8GSzR8_fJwCyKZzbmDo_FgCp4-9OOqS_jsX6Vlw9qeTZUoOLDTbW2Jp7gcFDZwFj_AYOSDFx_v2Lt3";
    String guid = "3483ea267957429ba0590f9cfe4ba98d";
    String global_id = "3483ea267957429ba0590f9cfe4ba98d";
    //启动云游戏
    public void startGame(View view){

//        GameInfo info = new GameInfo();
//        info.gid = 3273;
//        info.pkgName = "com.kptach.cpgame.demo";
//        info.name = "植物";
//        info.iconUrl = "http://kp.you121.top/api/image/20200119133131vpiulx.png";
//        info.showAd = GameInfo.GAME_AD_SHOW_ON;
//        list.add(info);

//        ExitGameListDialog dialog = new ExitGameListDialog(this, list);
//
//        dialog.show();


//        new RequestGameExitListTask(this)
//                .setRequestCallback(new IRequestCallback<List<GameInfo>>() {
//                    @Override
//                    public void onResult(List<GameInfo> list, int code) {
//                        Log.i("AA", list.toString());
//                    }
//                })
//                .execute(corpId, "51");

//        ProferencesUtils.setInt(this, SharedKeys.KEY_GAME_EXITALERTCOUNT_CONF, 3);


//        showExitGameListDialog();

//        AccountActivity mLoginDialog = new AccountActivity(this, corpId, "aaa", "bbb");
//        mLoginDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialogInterface) {
//
//            }
//        });
//            mLoginDialog.setCallback(new LoginDialog.OnLoginListener() {
//                @Override
//                public void onLoginSuccess(HashMap<String, Object> map) {
//                    //缓存数据
//                    cacheLoginData(map);
//
//                    //回调
//                    if (mCallback!=null){
//                        mCallback.onLogin(1, "", map);
//                    }
//                }
//            });
//        mLoginDialog.show();

//        new RequestClientNotice()
//                .execute("VM110110110","com.tencent.YiRen","h51038462",corpId);

//        String str = DeviceIdUtil.getDeviceId(this);
//        Log.i("HorizontalHomeActivity", str);
//
//        //初始化设备信息，并发送
//        if (!DeviceInfo.hasDeviceId(this)){
//            DeviceInfo.sendDeviceInfo(this,corpId);
//        }

//        startActivity(new Intent(this,TestActivity.class));

//        LoginDialog mLoginDialog = new LoginDialog(this, corpId);
//        mLoginDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialogInterface) {
//
//            }
//        });
//        mLoginDialog.setCallback(new LoginDialog.OnLoginListener() {
//            @Override
//            public void onLoginSuccess(HashMap<String, Object> map) {
//                Log.i("", map.toString());
//            }
//        });
//        mLoginDialog.show();
//
//        PayDialog mPayDialog = new PayDialog(this);
//        mPayDialog.cp_orderid = "test111";
//        mPayDialog.guid = guid;
//        mPayDialog.gameId = "3075";
//        mPayDialog.gameName = "MT2";
//        mPayDialog.gamePkg = "com.mt2.kp";
//        mPayDialog.corpKey = corpId;
//        mPayDialog.productcode = "test111";
//        mPayDialog.productname = "test商品1001";
//        mPayDialog.productprice = "81";
//        mPayDialog.phone = "13811571397";
//        mPayDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialogInterface) {
//
//            }
//        });
//        mPayDialog.setCallback(new PayDialog.ICallback() {
//            @Override
//            public void onResult(int ret, String msg) {
//
//            }
//        });
//        mPayDialog.show();

//        GameInfo info = new GameInfo();
//        info.gid = 3273;
//        info.pkgName = "com.kptach.cpgame.demo";
//        info.name = "植物";
//        info.iconUrl = "http://kp.you121.top/api/image/20200119133131vpiulx.png";
//        info.showAd = GameInfo.GAME_AD_SHOW_ON;

        GameInfo info = new GameInfo();

        info.iconUrl = "http://kp.you121.top/api/image/20200119133131vpiulx.png";
        info.showAd = GameInfo.GAME_AD_SHOW_ON;
//        info.downloadUrl = "https://down.qq.com/qqweb/QQ_1/android_apk/AndroidQQ_8.4.5.4745_537065283.apk";

        info.gid = 3439;
        info.pkgName = "com.kptach.test";

//        info.gid = 3437;
//        info.pkgName = "com.kptach.test";
        info.name = "cloudTest";

//        info.gid = 3104;
//        info.pkgName = "com.netease.tom";
//        info.name = "猫和老鼠";

//        info.gid = 3427;
//        info.pkgName = "com.netease.dwrg";
//        info.name = "第五人格";

//        info.gid = 3427;
//        info.pkgName = "com.netease.dwrg";
//        info.name = "第五人格";


//        info.gid = 3135;
//        info.pkgName = "com.pwrd.mjjsl.guopan";
//        info.name = "梦间集天鹅座";

        Params params = new Params();
//        params.put(ParamKey.ACTIVITY_LOADING_ICON, 111);
//        params.put(ParamKey.GAME_OPT_TIMEOUT_FONT, 5 * 60);
//        params.put(ParamKey.GAME_OPT_TIMEOUT_BACK, 3 * 60);
        GameBox.getInstance().playGame(HorizontalHomeActivity.this,info,params);
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
game.ext = new HashMap<>();
game.ext.put("version","aa");
game.ext.put("md5","bb");
                //                game.showAd = GameInfo.GAME_AD_SHOW_OFF;
//                GameBox box = GameBox.getInstance(getApplication(),corpId);
//                gameBox.setGameDownloader(mGameDownloader);


                Params params = new Params();
//                params.put(ParamKey.GAME_AUTH_UNION_UUID, "test0001");
                GameBox.getInstance().playGame(HorizontalHomeActivity.this, game, params);
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
                    GameBoxManager.getInstance().updateGameInfo(info);
                    mGameInfos.put(info.gid, info);
                    mHandler.sendEmptyMessage(MSG_REFRESH_LIST);
                }
            }.start();
        }
    }

    private void loadGame() {
//        GameBoxManager.setAppKey("2OCYlwVwzqZ2R8m-d27d6a9c5c675a3b");
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<GameInfo> result = null;
//                if (wy){
                    result = GameBoxManager.getInstance()
                            .queryGameList(mGameInfos.size(), 50);
//                }else {
////                    String tmp_data = "[{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d1921\",\"gid\":1921,\"iconUrl\":\"\",\"name\":\"QQ\",\"pkgName\":\"com.tencent.mobileqq\",\"playCount\":99,\"showAd\":0,\"size\":96018622,\"totalTime\":86400,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2497\",\"gid\":2497,\"iconUrl\":\"\",\"name\":\"明日之后\",\"pkgName\":\"com.netease.mrzh.guopan\",\"playCount\":60,\"showAd\":2,\"size\":2070011001,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2647\",\"gid\":2647,\"iconUrl\":\"\",\"name\":\"网易云音乐\",\"pkgName\":\"com.netease.cloudmusic\",\"playCount\":96,\"showAd\":2,\"size\":83116311,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2652\",\"gid\":2652,\"iconUrl\":\"\",\"name\":\"战神传奇\",\"pkgName\":\"com.zqgame.zscq.kuaipan\",\"playCount\":69,\"showAd\":2,\"size\":246827618,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2760\",\"gid\":2760,\"iconUrl\":\"\",\"name\":\"万象物语\",\"pkgName\":\"com.ilongyuan.sdorica.guopan\",\"playCount\":99,\"showAd\":2,\"size\":228779724,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2761\",\"gid\":2761,\"iconUrl\":\"\",\"name\":\"模拟城市：我是市长\",\"pkgName\":\"com.ea.simcitymobile.guopan\",\"playCount\":80,\"showAd\":1,\"size\":155105034,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2762\",\"gid\":2762,\"iconUrl\":\"\",\"name\":\"迷失岛2：时间的灰烬\",\"pkgName\":\"com.isoland2.lilithgames.guopan\",\"playCount\":99,\"showAd\":1,\"size\":280453188,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2764\",\"gid\":2764,\"iconUrl\":\"\",\"name\":\"梦间集：天鹅座\",\"pkgName\":\"com.pwrd.mjjsl.guopan\",\"playCount\":91,\"showAd\":1,\"size\":1416160752,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2809\",\"gid\":2809,\"iconUrl\":\"\",\"name\":\"传奇盛世2 \",\"pkgName\":\"com.sanjiu.cqss2.guopan\",\"playCount\":53,\"showAd\":1,\"size\":558109309,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2814\",\"gid\":2814,\"iconUrl\":\"\",\"name\":\"葫芦兄弟\",\"pkgName\":\"com.wk.hlxd.wanme\",\"playCount\":68,\"showAd\":1,\"size\":495070658,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2821\",\"gid\":2821,\"iconUrl\":\"\",\"name\":\"战歌竞技场\",\"pkgName\":\"com.tencent.hjzqgame\",\"playCount\":97,\"showAd\":1,\"size\":1009615840,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2823\",\"gid\":2823,\"iconUrl\":\"\",\"name\":\"疯狂原始人（爱奇艺测试通信sdk）\",\"pkgName\":\"com.skymoons.croods.iqiyiyyx\",\"playCount\":78,\"showAd\":1,\"size\":771857656,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2827\",\"gid\":2827,\"iconUrl\":\"\",\"name\":\"爱奇艺测试\",\"pkgName\":\"com.iqiyigame.sdk.demo\",\"playCount\":61,\"showAd\":1,\"size\":11441875,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2851\",\"gid\":2851,\"iconUrl\":\"\",\"name\":\"命运之刃（守护女神）\",\"pkgName\":\"com.bt.myzr.guopan\",\"playCount\":50,\"showAd\":0,\"size\":5757725,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2855\",\"gid\":2855,\"iconUrl\":\"\",\"name\":\"陌陌\",\"pkgName\":\"com.immomo.momo\",\"playCount\":61,\"showAd\":0,\"size\":84801024,\"totalTime\":604800,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"http://yunapp-ws-sandbox.baidu.com/api/v1/app/download?appid\\u003d2856\",\"gid\":2856,\"iconUrl\":\"\",\"name\":\"花椒直播\",\"pkgName\":\"com.huajiao\",\"playCount\":81,\"showAd\":0,\"size\":67583573,\"totalTime\":604800,\"usedTime\":0}]";
//                                        String tmp_data = "[{\"addMockInfo\":0,\"downloadUrl\":\"\",\"gid\":3006,\"iconUrl\":\"\",\"name\":\"街球艺术\",\"pkgName\":\"com.zqgame.jqys.kuaipan\",\"playCount\":99,\"showAd\":0,\"size\":96018622,\"totalTime\":86400,\"usedTime\":0},{\"addMockInfo\":0,\"downloadUrl\":\"\",\"gid\":2847,\"iconUrl\":\"\",\"name\":\"葫芦娃\",\"pkgName\":\"com.beiyu.huluw\",\"playCount\":99,\"showAd\":0,\"size\":96018622,\"totalTime\":86400,\"usedTime\":0}]";
//                    Gson gson = new Gson();
//                    Type listType = new TypeToken<List<GameInfo>>() {
//                    }.getType();
//                    result = gson.fromJson(tmp_data, listType);
//                }

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
