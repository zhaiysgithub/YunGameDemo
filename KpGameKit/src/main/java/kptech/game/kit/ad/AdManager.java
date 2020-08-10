package kptech.game.kit.ad;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.zad.sdk.Oapi.ZadSdkApi;
import com.zad.sdk.Oapi.callback.ZadRewardAdObserver;
import com.zad.sdk.Oapi.work.ZadRewardWorker;

public class AdManager {


    private static final String TAG = "AdManager";


    public static void init(final Application application, String appKey, String appToken){

        //调用接口获取广告信息,加载完成后初始化广告
        ZadSdkApi.init(application, appKey, appToken);

    }

}
