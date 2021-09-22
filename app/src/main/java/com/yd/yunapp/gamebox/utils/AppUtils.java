package com.yd.yunapp.gamebox.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class AppUtils {

    public static final String SIGN_SK = "990911";
    public static final String SIGN_AK = "test_13390kp";

    public static final String SIGN_SK_BD_YOUTH = "30ad7b36364ca133d5d496e74d18cc97";
    public static final String SIGN_AK_BD_YOUTH = "08afbd881c7832dd013a8061675ce407";

    /**
     * 获取当前本地apk的版本
     */
    public static String getVersionName(Context mContext) {
        String versionName = "1.0.0";
        try {
            versionName = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "V" + versionName;
    }

    /**
     * 获取应用程序名称
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取验签值
     */
    public static String getMd5Value(String openId,String corpKey,String time,String sk){

        /*try {
            String str = "corpkey=" + corpKey + "ts=" + time + "usersign=" + openId + SIGN_SK;

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";*/

        String str = "corpkey=" + corpKey + "ts=" + time + "usersign=" + openId + sk;
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            StringBuilder sBuilder = new StringBuilder();
            for (byte value : digest) {
                final int b = value & 255;
                if (b < 16) {
                    sBuilder.append('0');
                }
                sBuilder.append(Integer.toHexString(b));
            }
            return sBuilder.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

}
