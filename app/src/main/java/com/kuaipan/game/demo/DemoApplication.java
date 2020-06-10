package com.kuaipan.game.demo;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.yd.yunapp.gameboxlib.GameBoxManager;

public class DemoApplication extends Application {

    // app key. 查看地址： http://yunapp-console.bj.bcebos.com/sandbox_new/#/deviceGroups
    private static final String YOUR_AK = "xPLHWwUCDBiVa4QrZyT2g73M";
    // app secret
    private static final String YOUR_SK = "HtQ4S2I8EnAupa39vrlWYiqKCyFDzNBZV1hdUgPs";


    // 渠道值，自定义
    private static final String YOUR_CHANNEL = "test";

    @Override
    public void onCreate() {
        super.onCreate();
        // 配置ak sk 渠道
        GameBoxManager.getInstance(this).init(YOUR_AK, YOUR_SK, YOUR_CHANNEL);

        // 配置 子渠道 （可选）
        GameBoxManager.getInstance(this).setSubChannel("demo_test");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        MultiDex.install(base);
    }

}
