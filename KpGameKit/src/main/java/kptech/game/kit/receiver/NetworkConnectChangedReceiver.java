package kptech.game.kit.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import kptech.game.kit.utils.Logger;

public class NetworkConnectChangedReceiver extends BroadcastReceiver {

    private final OnNetworkChangeListener mListener;
    private int changCount;

    public NetworkConnectChangedReceiver(OnNetworkChangeListener listener) {
        this.mListener = listener;
        changCount = 0;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
         /*if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {// 监听wifi的打开与关闭，与wifi的连接无关
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
//            Logger.info("", "wifiState:" + wifiState);
           switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    break;
            }
        }*/
            // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                //获取联网状态的NetworkInfo对象
                NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (info != null) {
                    //如果当前的网络连接成功并且网络连接可用
                    if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                        if (info.getType() == ConnectivityManager.TYPE_WIFI || info.getType() == ConnectivityManager.TYPE_MOBILE) {
//                        Logger.info("", getConnectionType(info.getType()) + "连上,changeCount=" + changCount);
                            int netType = info.getType();
                            boolean isMobileNet = getConnectionType(netType);
                            if (mListener != null) {
                                changCount++;
                                if (changCount > 3) {
                                    changCount = 3;
                                }

                                //是否是WIFI环境切换到移动环境
                                boolean isWifiSwitchToMobileNet = (isMobileNet && changCount > 1);

                                mListener.onNetworkChanged(netType, isMobileNet, isWifiSwitchToMobileNet);
                            }
                        }
                    } else {
//                    Logger.info("", getConnectionType(info.getType()) + "断开");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean getConnectionType(int type) {
        String connType = "";
        boolean isMobileNet = false;
        if (type == ConnectivityManager.TYPE_MOBILE) {
            connType = "移动网络数据";
            isMobileNet = true;
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            connType = "WIFI网络";
            isMobileNet = false;
        }
        return isMobileNet;
    }


    public interface OnNetworkChangeListener {

        /**
         * 网络发生变化
         *
         * @param type                    网络类型
         * @param isMobileNet             是否是移动网络
         * @param isWifiChangeToMobileNet 是否是从WIFI切换到移动网络
         */
        void onNetworkChanged(int type, boolean isMobileNet, boolean isWifiChangeToMobileNet);
    }
}
