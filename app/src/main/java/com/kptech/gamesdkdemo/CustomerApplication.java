package com.kptech.gamesdkdemo;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import org.xutils.x;

import kptech.game.kit.GameBox;
import kptech.game.kit.GameBoxManager;

public class CustomerApplication extends Application {

    public static final String appKeyErrorMsg = "appKey 不能为空";
    public static Application appContext;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        //控制台是否输出
        GameBoxManager.setDebug(true);
        String appKey = Enviroment.getInstance().getmCropKey();
        //初始化
        if (!appKey.isEmpty()) {
            GameBox.init(this, appKey);
        }

    }
}
