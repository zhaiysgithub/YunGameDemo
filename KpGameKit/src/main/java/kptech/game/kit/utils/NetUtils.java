package kptech.game.kit.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtils {

    /**
     * 判断网络连接是否打开,包括移动数据连接
     *
     * @param context 上下文
     * @return 是否联网
     */
    public static boolean isNetworkAvailable(Context context) {
        boolean netstate = false;
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {

            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            for (NetworkInfo networkInfo : info) {

                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {

                    netstate = true;
                    break;
                }
            }
        }
        return netstate;
    }


    /**
     * 只是判断WIFI
     *
     * @param context 上下文
     * @return 是否打开Wifi
     */
    public static boolean isWiFi(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo != null){
            NetworkInfo.State wifi = networkInfo.getState();
            return wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING;
        }
        return false;
    }
}
