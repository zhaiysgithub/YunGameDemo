package com.yd.yunapp.gamebox;

import android.app.Application;

import com.yd.yunapp.gamebox.permission.PermissionHandler;

import kptech.game.kit.APIConstants;
import kptech.game.kit.GameBoxManager;


public class DemoApplication extends Application {

//    // app key. 查看地址： http://yunapp-console.bj.bcebos.com/sandbox_new/#/deviceGroups
//    private static final String YOUR_AK = "DRKV6H9ZAUn4fM2xEQok5dNb";
//    // app secret
//    private static final String YOUR_SK = "XdOHcNixh29GA0KkRQBjUDvPmLSWVs1TCaIegZ5q";

    // app key. 查看地址： http://yunapp-console.bj.bcebos.com/sandbox_new/#/deviceGroups
    private static final String GAME_AK = "xPLHWwUCDBiVa4QrZyT2g73M";
    // app secret
    private static final String GAME_SK = "HtQ4S2I8EnAupa39vrlWYiqKCyFDzNBZV1hdUgPs";

    // 渠道值，自定义
    private static final String YOUR_CHANNEL = "test";
    private PermissionHandler mPermissionHandler;

    private static final String APPKEY = "2OL7hDplsNG3SLS-bacc1a1395641317";

    @Override
    public void onCreate() {
        super.onCreate();
        //debug,发布版本时要关闭
        GameBoxManager.setDebug(true);
        // 配置ak sk 渠道
        GameBoxManager.getInstance(this).init(this, APPKEY);

        // 配置 子渠道 （可选）
        GameBoxManager.getInstance(this).setSubChannel("demo_test");
         GameBoxManager.getInstance(this).addDeviceMockInfo(APIConstants.MOCK_ANDROID_ID, "1212121212121212");
        // GameBoxManager.getInstance(this).addDeviceMockInfo(APIConstants.MOCK_IMEI, "3434343434343434");
        mPermissionHandler = new PermissionHandlerImpl();
    }

    public PermissionHandler getPermissionHandler() {
        return mPermissionHandler;
    }
}
