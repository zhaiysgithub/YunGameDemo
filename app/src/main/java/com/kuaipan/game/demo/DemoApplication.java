package com.kuaipan.game.demo;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import kptech.game.kit.GameBoxManager;


public class DemoApplication extends Application {

    // app key. 查看地址： http://yunapp-console.bj.bcebos.com/sandbox_new/#/deviceGroups
    private static final String YOUR_AK = "TOphL4quGn1a7dVRisS5ywU0";
    // app secret
    private static final String YOUR_SK = "foeGZYkV4NOICn9Qpuq507ElvagTMHybhrKPLX6S";


    // 渠道值，自定义
    private static final String YOUR_CHANNEL = "test";

    @Override
    public void onCreate() {
        super.onCreate();
        // 配置ak sk 渠道
        GameBoxManager.init(this, YOUR_AK, YOUR_SK, YOUR_CHANNEL);

        GameBoxManager.getInstance(this).setDebug(true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        MultiDex.install(base);
    }

}
