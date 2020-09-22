package com.yd.yunapp.gamebox;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;

import kptech.game.kit.BuildConfig;
import kptech.game.kit.GameBox;
import kptech.game.kit.GameInfo;

public class TestActivity extends Activity {
    final String corpId = BuildConfig.DEBUG ?  "2OQCrVnJuES1AVO-ac995a9fef8adcdb" : "2OPhcwdOhFq2uXl-1bcef9c0bf0a668a";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button btn = new Button(this);
        btn.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        btn.setText("Start");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(v);
            }
        });
        setContentView(btn);

        GameBox.init(getApplication(),corpId);

    }

    //启动云游戏
    public void startGame(View view) {

        GameInfo info = new GameInfo();
        info.gid = 1893;
        info.pkgName = "com.netease.tom.guopan";
        info.name = "猫和老鼠";
        info.iconUrl = "http://kp.you121.top/api/image/20200119133131vpiulx.png";
        info.showAd = GameInfo.GAME_AD_SHOW_ON;
        info.downloadUrl = "https://down.qq.com/qqweb/QQ_1/android_apk/AndroidQQ_8.4.5.4745_537065283.apk";

        GameBox.getInstance().playGame(this, info);
    }
}
