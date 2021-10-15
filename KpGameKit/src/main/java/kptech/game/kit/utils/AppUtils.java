package kptech.game.kit.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;

public class AppUtils {

    /**
     * 用来判断服务是否运行.
     *
     * @param context
     * @param className 判断的服务名字
     * @return true 在运行 false 不在运行
     */
    public static boolean isServiceRunning(Context context, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList
                = activityManager.getRunningServices(30);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }


    /**
     * 简单校验手机号码
     */
    public static boolean phoneNumSimpleCheck(String phoneNum){

        return phoneNum != null && phoneNum.length() == 11;
    }

    /**
     * 判断Apk包是否可用
     */
    public static boolean getUninatllApkInfo(Context context, String filePath) {
        boolean result = false;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
            if (info != null) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取文件的md5值
     */
    public static String getMD5(File file) {
        if (file.isFile()) {
            MessageDigest digest;
            FileInputStream in = null;
            byte[] buffer = new byte[1024];
            try {
                digest = MessageDigest.getInstance("MD5");
                in = new FileInputStream(file);
                int len;
                while ((len = in.read(buffer, 0, 1024)) != -1) {
                    digest.update(buffer, 0, len);
                }
                return toHex(digest.digest());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close(in);
            }
        }
        return null;
    }

    private static String toHex(byte[] md) {
        char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        int j = md.length;
        char[] str = new char[j * 2];

        for (int i = 0; i < j; ++i) {
            byte byte0 = md[i];
            str[2 * i] = hexDigits[byte0 >>> 4 & 15];
            str[i * 2 + 1] = hexDigits[byte0 & 15];
        }

        return new String(str);
    }

    private static void close(Closeable obj) {
        if (obj != null) {
            try {
                obj.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isScreenPortrait(Context context){
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

}
