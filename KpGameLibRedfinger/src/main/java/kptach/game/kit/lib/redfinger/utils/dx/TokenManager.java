package kptach.game.kit.lib.redfinger.utils.dx;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import java.security.MessageDigest;

/* compiled from: TokenManager */
public final class TokenManager {
    private static String a = "";

    private static boolean a(Context context, String str) {
        ContentResolver contentResolver = context.getContentResolver();
        if (Build.VERSION.SDK_INT > 22) {
            return false;
        }
        try {
            return Settings.System.putString(contentResolver, "android.{F46B117B-CBC7-4ac2-8F3C-43C1649DC760}", str);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean b(Context context, String str) {
        SharedPreferences.Editor edit = context.getSharedPreferences("utils", 0).edit();
        edit.putString("tm", str);
        return edit.commit();
    }

    public static String c(Context context) {
        boolean z;
        boolean z2;
        if (c(a) || b(a)) {
            String e = e(context);
            String d = d(context);
            if (b(e)) {
                if (c(d)) {
                    String a2 = a(context);
                    a = a2;
                    if (b(a2)) {
                        a = b(context);
                        z = true;
                        z2 = true;
                    }
                } else if (b(d)) {
                    String a3 = a(context);
                    a = a3;
                    if (b(a3)) {
                        a = b(context);
                        z = true;
                        z2 = true;
                    }
                } else {
                    a = d;
                    z = false;
                    z2 = true;
                }
                z = true;
                z2 = true;
            } else {
                a = e;
                if (b(d)) {
                    z = true;
                    z2 = false;
                } else {
                    z = false;
                    z2 = false;
                }
            }
            if (z2) {
                synchronized (TokenManager.class) {
                    b(context, a);
                }
            }
            if (z) {
                synchronized (TokenManager.class) {
                    a(context, a);
                }
            }
        }
        return a;
    }

    private static String d(Context context) {
        if (Build.VERSION.SDK_INT <= 22) {
            try {
                return Settings.System.getString(context.getContentResolver(), "android.{F46B117B-CBC7-4ac2-8F3C-43C1649DC760}");
            } catch (Exception e) {
            }
        }
        return null;
    }

    private static String e(Context context) {
        return context.getSharedPreferences("utils", 0).getString("tm", "");
    }

    private static String a(Context context) {
        String e = DXBMobileInfo.e(context);
        String f = DXBMobileInfo.f(context);
        String b = DXBMobileInfo.b();
        if (!TextUtils.isEmpty(f)) {
            f = f.replaceAll("\\s*ro.cdma.home.operator.alpha=", "cdma=");
        }
        boolean z = true;
        if (TextUtils.isEmpty(e) || e.length() <= 12) {
            z = false;
        }
        if (TextUtils.isEmpty(f) || f.length() <= 12) {
            z = false;
        }
        if (TextUtils.isEmpty(b) || b.length() <= 32) {
            z = false;
        } else if (b.length() > 128) {
            b = b.substring(0, 128);
        }
        if (z) {
            return a(e + "_" + f + "_" + b);
        }
        return "";
    }

    private static String b(Context context) {
        String e = DXBMobileInfo.e(context);
        String l = DXBMobileInfo.l(context);
        String n = DXBMobileInfo.n(context);
        String b = DXBMobileInfo.b();
        return a(e + "_" + n + "_" + l + "_" + System.currentTimeMillis() + "_" + b + "_" + DXBMobileInfo.a());
    }

    private static boolean b(String str) {
        return str == null || str.length() <= 5;
    }

    private static String a(String str) {
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            instance.update(str.getBytes("UTF-8"));
            return new String(Base64.d(instance.digest()), "UTF-8");
        } catch (Exception e) {
            return str;
        }
    }

    private static boolean c(String str) {
        return TextUtils.isEmpty(str);
    }
}
