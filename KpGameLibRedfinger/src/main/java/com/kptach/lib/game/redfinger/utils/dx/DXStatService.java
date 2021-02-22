package com.kptach.lib.game.redfinger.utils.dx;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* compiled from: DXStatService */
public final class DXStatService {
    private static Map<String, String> map;
    private static String[] pKeys = {"pkg", "ie", "lc", "model", "tk", "v", "vn"};

    private static String a(Context context, List<String> list) {
        String ret;
        synchronized (DXStatService.class) {
            List<String> a3 = cleanList(list);
            HashMap hashMap = new HashMap();
            addMapInf(context);
            HashMap hashMap2 = new HashMap(map);
            putMap("ntt", DXBMobileInfo.getNetNameType(context), hashMap2);
            for (String str : a3) {
                hashMap2.remove(str);
            }
            for (Object str2 : hashMap2.keySet()) {
                DXBNetworkState.putMap((String) str2, (String) hashMap2.get(str2), hashMap);
            }
            ret = DXBNetworkState.toUrlEncodeString(hashMap, "UTF-8");
        }
        return ret;
    }

    public static String b(Context context) {
        return a(context, new ArrayList());
    }

    @SuppressLint("WrongConstant")
    private static void addMapInf(Context context) {
        if (map == null) {
            map = new HashMap();
            putMap("pkg", context.getPackageName(), map);
            putMap("h", DXBMobileInfo.d(context), map);
            putMap("w", DXBMobileInfo.m(context), map);
            putMap("v", String.valueOf(DXBPackageUtils.a(context)), map);
            putMap("vn", DXBPackageUtils.getVersionName(context), map);
            putMap("model", DXBMobileInfo.j(context), map);
            putMap("vendor", DXBMobileInfo.h(context), map);
            putMap("ie", DXBMobileInfo.e(context), map);
            putMap("sdk", DXBMobileInfo.a(context), map);
            putMap("dpi", DXBMobileInfo.c(context), map);
            putMap("tk", TokenManager.c(context), map);
            putMap("locale", DXBMobileInfo.g(context), map);
            putMap("signmd5", a(context), map);
        }
        if (Build.VERSION.SDK_INT < 23 || context.checkSelfPermission("android.permission.READ_PHONE_STATE") == 0) {
            String f = DXBMobileInfo.f(context);
            if (!map.containsKey("is")) {
                putMap("is", f, map);
            }
            if (!TextUtils.isEmpty(f) && !map.containsKey("op")) {
                putMap("op", DXBMobileInfo.b(context), map);
            }
        }
        if (!map.containsKey("lc")) {
            putMap("lc", LcService.a(context), map);
        }
    }

    private static String a(String str) {
        long j = 0;
        int i = 8;
        if (str == null || str.length() < 32) {
            return "-1";
        }
        String substring = str.substring(8, 24);
        int i2 = 0;
        long j2 = 0;
        while (i2 < 8) {
            int i3 = i2 + 1;
            j2 = (j2 * 16) + ((long) Integer.parseInt(substring.substring(i2, i3), 16));
            i2 = i3;
        }
        while (i < substring.length()) {
            int i4 = i + 1;
            j = (j * 16) + ((long) Integer.parseInt(substring.substring(i, i4), 16));
            i = i4;
        }
        return String.valueOf((j2 + j) & 4294967295L);
    }

    @SuppressLint("WrongConstant")
    private static String a(Context context) {
        try {
            return a(DXBHashUtils.a(context.getPackageManager().getPackageInfo(context.getPackageName(), 64).signatures[0].toCharsString()));
        } catch (Exception e) {
            return null;
        }
    }

    private static List<String> cleanList(List<String> list) {
        String[] strArr = pKeys;
        for (String str : strArr) {
            if (list.contains(str)) {
                list.remove(str);
            }
        }
        return list;
    }

    private static Map<String, String> putMap(String str, String str2, Map<String, String> map) {
        if (!TextUtils.isEmpty(str2)) {
            map.put(str, str2);
        }
        return map;
    }
}
