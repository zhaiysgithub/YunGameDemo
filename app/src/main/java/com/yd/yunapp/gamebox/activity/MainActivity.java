package com.yd.yunapp.gamebox.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.widget.AbsListView;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.kuaipan.game.demo.BuildConfig;
import com.kuaipan.game.demo.R;
import com.yd.yunapp.gamebox.SettingsActivity;
import com.yd.yunapp.gamebox.TestXiaoYuBean;
import com.yd.yunapp.gamebox.model.MainModel;
import com.yd.yunapp.gamebox.utils.AppUtils;
import com.yd.yunapp.gamebox.view.CustomerLoadingView;

import org.xutils.x;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import kptech.game.kit.GameBox;
import kptech.game.kit.GameBoxManager;
import kptech.game.kit.GameInfo;
import kptech.game.kit.ParamKey;
import kptech.game.kit.Params;
import kptech.game.kit.env.Env;

public class MainActivity extends AppCompatActivity {

    private ArrayList<GameInfo> mGameInfos;
    private GameAdapter mGameAdapter;
    private EditText mGidText;
    private EditText mPkgText;
    private MainModel mainModel;
    private SharedPreferences mSp = null;
    private String mCorpKey;
    private SwipeRefreshLayout mRefreshLayout;
    private View mLayoutProgress;
    private String userSignValue;



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Env.init(this);

        mPkgText = findViewById(R.id.pkg);
        mGidText = findViewById(R.id.gid);
        mRefreshLayout = findViewById(R.id.refreshLayout);
        mLayoutProgress = findViewById(R.id.layoutProgress);

        x.Ext.init(getApplication());
        x.Ext.setDebug(BuildConfig.DEBUG); //输出debug日志，开启会影响性能

        mainModel = new MainModel(this);
        setTitle(mainModel.getTitleStr());

        mRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        mSp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enableInputCorpKey = mSp.getBoolean("inputCorpKey",false);
        if (enableInputCorpKey){
            mCorpKey = mSp.getString("editCorpKey","");
        }else {
            //测试appID
            mCorpKey = mSp.getString("corpKey", null);
//            APP_ID = "2VeV4QHgtjh2H7E-40cf9808ad9c3d5b";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(mainModel.getVersionName());
        sb.append("\n");
        TextView coprKey = findViewById(R.id.corpkey);
        if (mCorpKey == null) {
            sb.append("请配置CorpKey");
            coprKey.setText(sb.toString());
            coprKey.setTextColor(Color.RED);
        } else {
            sb.append((Env.isTestEnv() ? "测试环境" : "正式环境"));
            sb.append("\n CorpKey:");
            sb.append(mCorpKey);
            coprKey.setText(sb.toString());
        }

        //打印log信息，正式版本需要关闭
        GameBoxManager.setDebug(BuildConfig.DEBUG);
        GameBoxManager.setAppKey(mCorpKey);

        mGameInfos = new ArrayList<>();
        ListView mGameList = findViewById(R.id.game_list);
        mGameAdapter = new GameAdapter(mGameInfos);
        mGameList.setAdapter(mGameAdapter);

        GameBox.init(getApplication(), mCorpKey);

        loadGame();
        mGameList.setOnScrollListener(mListViewScrollListener);
        mRefreshLayout.setOnRefreshListener(mRefreshListener);
    }

    public void startGame(View v) {
        if (mCorpKey == null || mCorpKey.isEmpty()){
            Toast.makeText(MainActivity.this,"请配置CorpKey",Toast.LENGTH_SHORT).show();
            return;
        }
        boolean useSDK2 = kptech.game.kit.BuildConfig.useSDK2;
        GameInfo info = new GameInfo();
        if (useSDK2) {
            int gid = 0;
            try {
                String gidTest = mGidText.getText().toString();
                gid = Integer.parseInt(gidTest);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (gid == 0) {
                Toast.makeText(this, "gid错误", Toast.LENGTH_SHORT).show();
                return;
            }

            info.gid = gid;
            info.pkgName = mPkgText.getText().toString();
            info.name = "网易游戏联运Demo";

        } else {

            String pkgName = mPkgText.getText().toString();
            if (pkgName.isEmpty()){
                Toast.makeText(this, "包名不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            info.pkgName = pkgName;
        }
        runGame(info);
    }

    public void runGame(GameInfo game) {
        Params params = new Params();

        boolean enableAuth = mSp.getBoolean("enableAuth", false);
        String unionId = mSp.getString("unionId", null);

        if (enableAuth && unionId != null) {
            userSignValue = "test_" + unionId;
            params.put(ParamKey.GAME_AUTH_UNION_UUID, userSignValue);
        }

        String fontStr = mSp.getString("fontTimeout", null);
        if (fontStr != null) {
            try {
                int fontTimeout = Integer.parseInt(fontStr);

                if (fontTimeout > 0) {
                    params.put(ParamKey.GAME_OPT_TIMEOUT_FONT, fontTimeout);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String backStr = mSp.getString("backTimeout", null);
        if (backStr != null) {
            try {
                int backTimeout = Integer.parseInt(backStr);
                if (backTimeout > 0) {
                    params.put(ParamKey.GAME_OPT_TIMEOUT_BACK, backTimeout);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        boolean enableAd = mSp.getBoolean("enableAd", false);
        if (enableAd) {
            game.showAd = GameInfo.GAME_AD_SHOW_ON;
        } else {
            game.showAd = GameInfo.GAME_AD_SHOW_AUTO;
        }
        String akSign = AppUtils.SIGN_AK;
        String skSign = AppUtils.SIGN_SK;

        String timeStr = String.valueOf(System.currentTimeMillis());
        params.put(ParamKey.GAME_AUTH_UNION_AK,akSign);
        params.put(ParamKey.GAME_AUTH_UNION_TS,timeStr);
        String signValue = AppUtils.getMd5Value(userSignValue, mCorpKey, timeStr,skSign);
        params.put(ParamKey.GAME_AUTH_UNION_SIGN,signValue);
        //测试环境
//        game.gameEnvValue = 0;
//        game.errorShowDownUi = 1;

        boolean useCustomerLoadingView = mSp.getBoolean("enableCustomerLoadign",false);
        GameBoxManager.getInstance().setLoadingView(useCustomerLoadingView,new CustomerLoadingView(this));
        boolean enableGidLogin = mSp.getBoolean("enableGidLogin", false);
        if (enableGidLogin) {
            //使用 GID 登录游戏
            mainModel.loginByGid(game);
        } else {
            //启动游戏
            GameBox.getInstance().playGame(MainActivity.this, game, params);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivityForResult(new Intent(this, SettingsActivity.class), 101);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == 102) {
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    public class GameAdapter extends BaseAdapter {

        private final List<GameInfo> mData = new ArrayList<>();

        public GameAdapter(List<GameInfo> data) {
            mData.addAll(data);
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
            GameViewHodler holder;
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
            String iconUrl = game.iconUrl;
            if (iconUrl != null && !iconUrl.isEmpty()) {
                Glide.with(holder.icon).load(iconUrl).placeholder(R.mipmap.ico_default).into(holder.icon);
            } else {
                Glide.with(holder.icon).load(R.mipmap.ico_default).into(holder.icon);
            }

            holder.playBtn.setOnClickListener(view -> runGame(game));
        }

        class GameViewHodler extends RecyclerView.ViewHolder {
            TextView name;
            TextView playCount;
            ImageView icon;
            Button playBtn;

            public GameViewHodler(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.game_name);
                playCount = itemView.findViewById(R.id.play_count);
                icon = itemView.findViewById(R.id.game_icon);
                playBtn = itemView.findViewById(R.id.play_btn);
            }
        }
    }

    private void loadGame() {

        mLayoutProgress.setVisibility(View.VISIBLE);
        new Thread(() -> {
            List<GameInfo> result = GameBoxManager.getInstance().queryGameList(0, 50);
            mGameInfos.clear();
            if (result != null && result.size() > 0) {
                mGameInfos.addAll(result);
            }
            mHandler.sendEmptyMessage(0);
        }).start();
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            mLayoutProgress.setVisibility(View.GONE);
            mRefreshLayout.setRefreshing(false);
            mGameAdapter.refresh(new ArrayList<>(mGameInfos));
        }
    };

    private void startShowDialog() {
        sendBroadCast();
    }


    private void sendBroadCast() {
        Intent intent = new Intent();
        intent.setAction("KP_Cloud_Game_Play_StartActivity");
        intent.putExtra("className","com.yd.yunapp.gamebox.activity.TransDialogActivity");

        Bundle bundle = new Bundle();
        bundle.putString("abc","123");
        TestXiaoYuBean xiaoYuBean = new TestXiaoYuBean();
        xiaoYuBean.code = "000";
        xiaoYuBean.msg = "xiaoyu";
        bundle.putSerializable("xiaoyuBundle",xiaoYuBean);
        intent.putExtra("bundleData",bundle);

        sendBroadcast(intent);
    }


    private final SwipeRefreshLayout.OnRefreshListener mRefreshListener = this::loadGame;

    private final AbsListView.OnScrollListener mListViewScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (view != null){
                View firstView = view.getChildAt(firstVisibleItem);
                mRefreshLayout.setEnabled(firstVisibleItem == 0 && (firstView == null || firstView.getTop() == 0));
            }
        }
    };
}
