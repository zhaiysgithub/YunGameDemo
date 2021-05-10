package com.yd.yunapp.gamebox;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kuaipan.game.demo.BuildConfig;
import com.kuaipan.game.demo.R;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import kptech.game.kit.GameBox;
import kptech.game.kit.GameBoxManager;
import kptech.game.kit.GameInfo;
import kptech.game.kit.ParamKey;
import kptech.game.kit.Params;
import kptech.game.kit.callback.UserCertificationCallback;
import kptech.game.kit.env.Env;
import kptech.game.kit.manager.UserCertificationManager;

public class MainActivity extends AppCompatActivity {
    //测试appID
    private static String APP_ID = "";
    //快盘 "2OLuBOnPAGt12hN-64219e8c44e0efda";
    //网易："2OCYlwVwzqZ2R8m-d27d6a9c5c675a3b";

    ArrayList<GameInfo> mGameInfos;
    GameAdapter mGameAdapter;
    private EditText mGidText;
    private EditText mPkgText;
//    boolean enableAd = false;


    SharedPreferences mSp = null;

    public void startGame(View v){
        GameInfo info = new GameInfo();
        int gid = 0;
        try {
            String gidTest = mGidText.getText().toString();
            gid = Integer.parseInt(gidTest);
        }catch (Exception e){
        }
        if (gid == 0){
            Toast.makeText(this, "gid错误", Toast.LENGTH_SHORT).show();
            return;
        }

        info.gid = gid;
        String pkgText = mPkgText.getText().toString();
        info.pkgName = pkgText;
        info.name = "网易游戏联运Demo";

        runGame(info);
    }

    public void runGame(GameInfo game){
        Params params = new Params();

        boolean enableAuth = mSp.getBoolean("enableAuth", false);
        String unionId = mSp.getString("unionId", null);

        if (enableAuth && unionId != null){
            params.put(ParamKey.GAME_AUTH_UNION_UUID, "test_" + unionId);
        }

        String fontStr = mSp.getString("fontTimeout", null);
        if (fontStr!=null){
            try {
                int fontTimeout = Integer.parseInt(fontStr);

                if (fontTimeout > 0){
                    params.put(ParamKey.GAME_OPT_TIMEOUT_FONT, fontTimeout);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        String backStr = mSp.getString("backTimeout", null);
        if (backStr!=null){
            try {
                int backTimeout = Integer.parseInt(backStr);
                if (backTimeout > 0) {
                    params.put(ParamKey.GAME_OPT_TIMEOUT_BACK, backTimeout);
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        boolean enableAd = mSp.getBoolean("enableAd", false);
        if (enableAd){
            game.showAd = GameInfo.GAME_AD_SHOW_ON;
        }else {
            game.showAd = GameInfo.GAME_AD_SHOW_AUTO;
        }

        boolean shouldLoginAuth = UserCertificationManager.getInstance().shouldLoginAuthByPhone(getApplication(), "15711485499",game.pkgName);
        if (shouldLoginAuth){
            UserCertificationDialog mCertificationDialog = new UserCertificationDialog(MainActivity.this);
            mCertificationDialog.setOnCallback(new UserCertificationDialog.OnUserCerificationCallbck() {
                @Override
                public void onUserCancel() {
                    toggleSoftInput();
                }

                @Override
                public void onUserConfirm(String userName, String userIdCard, String userPhone) {
                    startUserAuth(userName,userIdCard,userPhone,game,params);
                    toggleSoftInput();
                }
            });
            mCertificationDialog.show();
            mHandler.postDelayed(this::toggleSoftInput,200);
        }else{
            GameBox.getInstance().playGame(MainActivity.this, game, params);
        }

        //启动游戏
//        GameBox.getInstance().playGame(MainActivity.this, game, params);
    }

    private void startUserAuth(String userName, String userIdCard,String userPhone, GameInfo gameInfo,Params params){
        try {
//            String userName = "丁文杰";
//            String userIdCard = "340203198007129355";
//            String userPhone = "15711485499";
            UserCertificationManager.getInstance().startAuthLoginGame(getApplication(), gameInfo.pkgName
                    , userName, userIdCard, userPhone, new UserCertificationCallback() {
                        @Override
                        public void onCerSuccess() {
                            if (!MainActivity.this.isFinishing()){
                                Toast.makeText(getApplication(),"认证成功",Toast.LENGTH_SHORT).show();
                                GameBox.getInstance().playGame(MainActivity.this, gameInfo, params);
                            }
                        }

                        @Override
                        public void onCerError(String errorStr) {
                            if (!MainActivity.this.isFinishing()){
                                Toast.makeText(MainActivity.this,errorStr,Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Env.init(this);

        mPkgText = findViewById(R.id.pkg);
        mGidText = findViewById(R.id.gid);

        x.Ext.init(getApplication());
        x.Ext.setDebug(BuildConfig.DEBUG); //输出debug日志，开启会影响性能

        mSp = PreferenceManager.getDefaultSharedPreferences(this);
        APP_ID = mSp.getString("corpKey", null);

        TextView coprKey = findViewById(R.id.corpkey);
        if (APP_ID == null){
            coprKey.setText("请配置CorpKey");
            coprKey.setTextColor(Color.RED);
        }else {
            coprKey.setText((Env.isTestEnv() ? "测试环境":"正式环境") + "\n CorpKey: " + APP_ID);
        }

        //打印log信息，正式版本需要关闭
        GameBoxManager.setDebug(BuildConfig.DEBUG);
        GameBoxManager.setAppKey(APP_ID);

        mGameInfos = new ArrayList<>();


        ListView mGameList = (ListView) findViewById(R.id.game_list);
        mGameAdapter = new GameAdapter(this, mGameInfos);
        mGameList.setAdapter(mGameAdapter);

        GameBox.init(getApplication(), APP_ID);

        //下载类
//        mGameDownloader = new GameDownloader() {
//            @Override
//            public boolean start(final GameInfo inf) {
//                new Thread(){
//                    @Override
//                    public void run() {
//                        download(inf.downloadUrl, inf);
//                    }
//                }.start();
//
//                //处理开始下载方法
//                return true;
//            }
//
//            @Override
//            public void stop(GameInfo inf) {
//                //处理停止下载
//                downloadStop();
//            }
//
//        };

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


        loadGame();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == R.id.settings){
            startActivityForResult(new Intent(this, SettingsActivity.class), 101);
//        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == 101 && resultCode == 102){
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    private Callback.Cancelable cancelable;
    private String mFilePath;
    //    private String mFileName;
    private boolean cancel = false;
    private void download(String url, final GameInfo game) {
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

//                if (mGameDownloader!=null){
//                    mGameDownloader.onStatusChanged(GameDownloader.STATUS_FINISHED, null, game);
//                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
//                if (mGameDownloader!=null){
//                    mGameDownloader.onStatusChanged(GameDownloader.STATUS_ERROR, ex.getMessage(), game);
//                }

                sendBroadcast(new Intent("Cloud_Music_Cloud_Game_DownLoad_Fail"));
            }

            @Override
            public void onCancelled(CancelledException cex) {
//                if (mGameDownloader!=null){
//                    mGameDownloader.onStatusChanged(GameDownloader.STATUS_CANCEL, null, game);
//                }
                sendBroadcast(new Intent("Cloud_Music_Cloud_Game_DownLoad_Stop"));
            }

            @Override
            public void onFinished() {

            }

            @Override
            public void onWaiting() {
//                if (mGameDownloader!=null){
//                    mGameDownloader.onStatusChanged(GameDownloader.STATUS_WAITTING, null, game);
//                }
            }

            @Override
            public void onStarted() {
//                if (mGameDownloader!=null){
//                    mGameDownloader.onStatusChanged(GameDownloader.STATUS_STARTED, null, game);
//                }
                sendBroadcast(new Intent("Cloud_Music_Cloud_Game_DownLoad_Start"));
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

//                if (mGameDownloader!=null){
//                    mGameDownloader.onProgresss(current, total, game);
//                }
            }
        });
    }

    private void downloadStop(){
        if (cancelable!=null && !cancelable.isCancelled()){
            cancelable.cancel();
        }
    }

    public class GameAdapter extends BaseAdapter {

        private List<GameInfo> mData = new ArrayList<>();
        private Activity mActivity;

        public GameAdapter(Activity activity, List<GameInfo> data) {
            mData.addAll(data);
            mActivity = activity;
        }

        public void refresh(final Collection<GameInfo> data) {
            mData.clear();
            mData.addAll(data);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            GameViewHodler holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_list_item, parent, false);
                holder = new GameViewHodler(convertView);
            } else {
                holder = (GameViewHodler) convertView.getTag();
            }
            convertView.setTag(holder);
            onBindViewHolder(holder, position);
            return convertView;
        }

        public void onBindViewHolder(GameViewHodler holder, final int position) {
            final GameInfo game = mData.get(position);
            holder.name.setText(game.name);
            holder.playBtn.setText("开始试玩");
            Glide.with(holder.icon).load(game.iconUrl).into(holder.icon);
            holder.playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    runGame(game);
                }
            });
        }

        class GameViewHodler extends RecyclerView.ViewHolder {
            TextView name;
            TextView playCount;
            ImageView icon;
            Button playBtn;

            public GameViewHodler(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.game_name);
                playCount = (TextView) itemView.findViewById(R.id.play_count);
                icon = (ImageView) itemView.findViewById(R.id.game_icon);
                playBtn = (Button) itemView.findViewById(R.id.play_btn);
            }
        }
    }

    private void loadGame() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<GameInfo> result = GameBoxManager.getInstance().queryGameList(0, 50);

                if (result != null && result.size() > 0) {
                    for (GameInfo info : result) {
                        mGameInfos.add(info);
                    }
                    mHandler.sendEmptyMessage(0);
                }
            }
        }).start();
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            mGameAdapter.refresh(new ArrayList<GameInfo>(mGameInfos));
        }
    };

    private void toggleSoftInput(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
