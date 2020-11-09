package com.yd.yunapp.gamebox;

import android.app.Application;

import com.kuaipan.game.demo.BuildConfig;
import com.yd.yunapp.gamebox.permission.PermissionHandler;

import org.xutils.x;

import kptech.game.kit.APIConstants;
import kptech.game.kit.GameBox;
import kptech.game.kit.GameBoxManager;


public class DemoApplication extends Application {

//    // app key. 查看地址： http://yunapp-console.bj.bcebos.com/sandbox_new/#/deviceGroups
//    private static final String YOUR_AK = "DRKV6H9ZAUn4fM2xEQok5dNb";
//    // app secret
//    private static final String YOUR_SK = "XdOHcNixh29GA0KkRQBjUDvPmLSWVs1TCaIegZ5q";

//    // app key. 查看地址： http://yunapp-console.bj.bcebos.com/sandbox_new/#/deviceGroups
//    private static final String GAME_AK = "xPLHWwUCDBiVa4QrZyT2g73M";
//    // app secret
//    private static final String GAME_SK = "HtQ4S2I8EnAupa39vrlWYiqKCyFDzNBZV1hdUgPs";

    // app key. 查看地址： http://yunapp-console.bj.bcebos.com/sandbox_new/#/deviceGroups
    private static final String YOUR_AK = "iaUGSL3QZYqMwRch0F2rmzBg";
    // app secret
    private static final String YOUR_SK = "y9GeE8hVd3xlYSWKRLwanATUk7oz5qtI21rMsNQB";


    // 渠道值，自定义
    private static final String YOUR_CHANNEL = "test";
    private PermissionHandler mPermissionHandler;

    private static final String APPKEY = "2OCYlwVwzqZ2R8m-d27d6a9c5c675a3b";

    @Override
    public void onCreate() {
        super.onCreate();

//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        GameBox.init(this, "2OQCrVnJuES1AVO-ac995a9fef8adcdb");

        //debug,发布版本时要关闭
        GameBoxManager.setDebug(true);
        // 配置ak sk 渠道
//        GameBoxManager.setAppInfo(YOUR_AK, YOUR_SK, YOUR_CHANNEL);
//        GameBoxManager.getInstance(this).init(this,null,null);

//        GameBoxManager.setAppKey(APPKEY);

        // 配置 子渠道 （可选）
//        GameBoxManager.getInstance(this).setSubChannel("demo_test");
//         GameBoxManager.getInstance(this).addDeviceMockInfo(APIConstants.MOCK_ANDROID_ID, "1212121212121212");
        // GameBoxManager.getInstance(this).addDeviceMockInfo(APIConstants.MOCK_IMEI, "3434343434343434");
        mPermissionHandler = new PermissionHandlerImpl();

        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG); //输出debug日志，开启会影响性能
    }

    public PermissionHandler getPermissionHandler() {
        return mPermissionHandler;
    }
}
