package com.kptach.lib.game.redfinger.utils.dx;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/* compiled from: LcService */
public class LcService {
    private static int a = 0;
    private static String b = "";

    public static boolean a(int i) {
        return i == 0 || 1 == i || 2 == i || 3 == i || 4 == i;
    }

    public static boolean a(String str) {
        if (a != 3 || TextUtils.isEmpty(str)) {
            return false;
        }
        b = str;
        return true;
    }

    public static boolean b(int i) {
        if (!a(i)) {
            return false;
        }
        a = i;
        return true;
    }

    private static String c(Context context) {
        BufferedReader bufferedReader;
        String str = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(context.getResources().getAssets().open("lc.txt")));
            try {
                String readLine = bufferedReader.readLine();
                if (readLine != null) {
                    readLine = readLine.trim();
                }
                if (readLine.length() != 0) {
                    str = readLine;
                }
                DXBCloseUtils.closeable(bufferedReader);
            } catch (Exception e) {
                DXBCloseUtils.closeable(bufferedReader);
                return str;
            } catch (Throwable th) {
                th = th;
                DXBCloseUtils.closeable(bufferedReader);
                throw th;
            }
        } catch (Exception e2) {
            bufferedReader = null;
            DXBCloseUtils.closeable(bufferedReader);
            return str;
        } catch (Throwable th2) {
            bufferedReader = null;
            DXBCloseUtils.closeable(bufferedReader);
        }
        return str;
    }

    @SuppressLint("WrongConstant")
    private static String d(Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(context.getPackageName(), 128).metaData.getString("LC");
        } catch (Throwable th) {
            return null;
        }
    }

    private static String b(Context context) {
        int i = a;
        if (i == 0) {
            return c(context);
        }
        if (1 == i) {
            return d(context);
        }
        if (2 == i) {
            return a();
        }
        return 4 == i ? b() : "";
    }

    public static String a(Context context) {
        if (TextUtils.isEmpty(b)) {
            b = b(context);
        }
        return b;
    }

    private static String a() {
        byte[] a2 = DXBFileUtils.a("/system/etc/dianxinos/ota/lc");
        return (a2 == null || a2.length <= 0) ? "" : new String(a2).trim();
    }

    private static String b() {
        return DXBMobileInfo.a("ro.dianxinos.os.lc");
    }

}