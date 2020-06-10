package com.kuaipan.game.demo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.kuaipan.game.demo.BuildConfig;


public class NetworkUtils {
    public static final int NET_TYPE_NONE = -1;
    // Don't touch! The following network types are defined by Server.
    public static final int NET_TYPE_WIFI = 1;
    public static final int NET_TYPE_2G = 2;
    public static final int NET_TYPE_3G = 3;
    public static final int NET_TYPE_MOBILE = 4;
    public static final int NET_TYPE_4G = 5;
    private static final String TAG = "NetworkUtils";
    private static final boolean DEBUG = BuildConfig.DEBUG;
    /** Same to {@link ConnectivityManager#TYPE_WIMAX} (API 8) */
    private static final int CM_TYPE_WIMAX = 6;
    /** Same to {@link ConnectivityManager#TYPE_ETHERNET} (API 13) */
    private static final int CM_TYPE_ETHERNET = 9;
    /** Same to {@link ConnectivityManager#TYPE_MOBILE_MMS} (API 8) */
    private static final int CM_TYPE_MOBILE_MMS = 2;
    /** Same to {@link ConnectivityManager#TYPE_BLUETOOTH} (API 8) */
    private static final int CM_TYPE_BLUETOOTH = 7;
    /** Same to {@link TelephonyManager#NETWORK_TYPE_EVDO_B} (API 9) 5 Mbps */
    private static final int TM_NETWORK_TYPE_EVDO_B = 12;
    /** Same to {@link TelephonyManager#NETWORK_TYPE_LTE} (API 11) 10+ Mbps */
    private static final int TM_NETWORK_TYPE_LTE = 13;
    /** Same to {@link TelephonyManager#NETWORK_TYPE_EHRPD} (API 11) 1~2 Mbps */
    private static final int TM_NETWORK_TYPE_EHRPD = 14;
    /** Same to {@link TelephonyManager#NETWORK_TYPE_HSPAP} (API 13) 10~20 Mbps */
    private static final int TM_NETWORK_TYPE_HSPAP = 15;
    private static ConnectivityManager sCM;

    private static ConnectivityManager getConnectivityManager(Context cxt) {
        if (sCM == null) {
            sCM = (ConnectivityManager) cxt.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
        }
        return sCM;
    }

    /**
     * @param cxt
     * @return One of values {@link #NET_TYPE_WIFI}, {@link #NET_TYPE_2G},
     * {@link #NET_TYPE_3G}, {@link #NET_TYPE_4G} or {@link #NET_TYPE_NONE}
     */
    public static int getNetworkType(Context cxt) {
        ConnectivityManager connMgr = getConnectivityManager(cxt);
        if (connMgr == null) {
            return NET_TYPE_NONE;
        }
        NetworkInfo netInfo = null;
        try {
            netInfo = connMgr.getActiveNetworkInfo();
        } catch (Exception e) {
        }
        if (netInfo != null) {
            int type = netInfo.getType();
            int subType = netInfo.getSubtype();
            if (type == ConnectivityManager.TYPE_WIFI
                    || type == CM_TYPE_WIMAX
                    || type == CM_TYPE_ETHERNET) {
                return NET_TYPE_WIFI;
            } else if (type == ConnectivityManager.TYPE_MOBILE
                    /*
                     * this patch for fix in some devices type when apn connected, report type is
                     * TYPE_BLUETOOTH and has subtype.  tested on CoolPad 7260+
                     */
                    || (type == CM_TYPE_BLUETOOTH && subType > 0)
                    ) {
                if (subType == TelephonyManager.NETWORK_TYPE_UMTS
                        || subType == TelephonyManager.NETWORK_TYPE_EVDO_0
                        || subType == TelephonyManager.NETWORK_TYPE_EVDO_A
                        || subType == TelephonyManager.NETWORK_TYPE_HSDPA
                        || subType == TelephonyManager.NETWORK_TYPE_HSUPA
                        || subType == TelephonyManager.NETWORK_TYPE_HSPA
                        || subType == TM_NETWORK_TYPE_EVDO_B
                        || subType == TM_NETWORK_TYPE_EHRPD
                        || subType == TM_NETWORK_TYPE_HSPAP) {
                    return NET_TYPE_3G;
                } else if (subType == TM_NETWORK_TYPE_LTE) {
                    return NET_TYPE_4G;
                }
                return NET_TYPE_2G; // Take other data types as 2G
            } else if (type == CM_TYPE_MOBILE_MMS || type == CM_TYPE_BLUETOOTH) {
                // when mms and bluetooth, don't recognize as mobile
                return NET_TYPE_NONE;
            }
            return NET_TYPE_2G; // Take unknown networks as 2G
        }
        return NET_TYPE_NONE;
    }

    /**
     * @param cxt
     * @return One of the values {@link #NET_TYPE_NONE} or {@link #NET_TYPE_WIFI} or {@link #NET_TYPE_MOBILE}
     */
    public static int getSimpleNetworkType(Context cxt) {
        ConnectivityManager connMgr = getConnectivityManager(cxt);
        if (connMgr == null) {
            return NET_TYPE_NONE;
        }
        NetworkInfo netInfo = null;
        try {
            netInfo = connMgr.getActiveNetworkInfo();
        } catch (Exception e) {
        }
        if (netInfo != null) {
            int type = netInfo.getType();
            if (type == ConnectivityManager.TYPE_WIFI || type == CM_TYPE_WIMAX
                    || type == CM_TYPE_ETHERNET) {
                return NET_TYPE_WIFI;
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                return NET_TYPE_MOBILE;
            } else if (type == CM_TYPE_MOBILE_MMS || type == CM_TYPE_BLUETOOTH) {
                return NET_TYPE_NONE;
            } else {
                // Take unknown networks as mobile network
                return NET_TYPE_MOBILE;
            }
        }
        return NET_TYPE_NONE;
    }

    /**
     * Check if there is an active network connection
     */
    public static boolean isNetworkAvaialble(Context ctx) {
        ConnectivityManager connMgr = getConnectivityManager(ctx);
        if (connMgr == null) {
            return false;
        }
        NetworkInfo network = null;
        try {
            network = connMgr.getActiveNetworkInfo();
        } catch (Exception e) {
        }
        return network != null && network.isConnected();
    }


    private static boolean isNetworkMobile(Context ctx) {
        ConnectivityManager cm = getConnectivityManager(ctx);
        if (cm == null) {
            return false;
        }
        NetworkInfo networkInfo = null;
        try {
            networkInfo = cm.getActiveNetworkInfo();
        } catch (Exception e) {
        }
        if (networkInfo != null) {
            return networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        }
        return false;
    }
}
