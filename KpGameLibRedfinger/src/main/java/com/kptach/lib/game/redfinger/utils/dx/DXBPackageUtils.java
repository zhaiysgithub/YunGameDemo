package com.kptach.lib.game.redfinger.utils.dx;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/* compiled from: DXBPackageUtils */
public class DXBPackageUtils {
    public static int a(Context context) {
        return getVersionCode(context, context.getPackageName());
    }

    public static String getVersionName(Context context) {
        try {
            PackageInfo a = getPackageInfo(context, context.getPackageName());
            if (a != null) {
                return a.versionName;
            }
        } catch (Exception e) {
        }
        return "";
    }

    public static int getVersionCode(Context context, String str) {
        try {
            PackageInfo a = getPackageInfo(context, str);
            if (a != null) {
                return a.versionCode;
            }
        } catch (Exception e) {
        }
        return -1;
    }

    private static PackageInfo getPackageInfo(Context context, String str) {
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                return packageManager.getPackageInfo(str, 0);
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return null;
    }

}