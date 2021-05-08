package com.yd.yunapp.gamebox;

import android.app.Application;

import com.yd.yunapp.gamebox.permission.PermissionHandler;


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

        //debug,发布版本时要关闭
        GameBoxManager.setDebug(true);

        mPermissionHandler = new PermissionHandlerImpl();
    }

    public PermissionHandler getPermissionHandler() {
        return mPermissionHandler;
    }
}
