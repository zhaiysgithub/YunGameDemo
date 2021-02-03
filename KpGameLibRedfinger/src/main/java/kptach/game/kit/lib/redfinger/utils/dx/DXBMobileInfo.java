package kptach.game.kit.lib.redfinger.utils.dx;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/* compiled from: DXBMobileInfo */
@SuppressLint("WrongConstant")
public class DXBMobileInfo {
    public static String a(String str) {
        try {
            Class<?> cls = Class.forName("android.os.SystemProperties");
            return (String) cls.getMethod("get", String.class).invoke(cls, str);
        } catch (Exception e) {
            return "";
        }
    }

    public static String b() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(String.format("/system/bin/cat /sys/block/mmcblk%d/device/cid", Integer.valueOf(i))).getInputStream()));
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null || readLine.length() <= 0) {
                        break;
                    }
                    sb.append(readLine).append(" ");
                }
            } catch (Exception e) {
            }
        }
        return sb.toString();
    }

    public static String c(Context context) {
        try {
            DisplayMetrics i = i(context);
            if (i != null) {
                return Integer.toString(i.densityDpi);
            }
        } catch (Exception e) {
        }
        return "";
    }

    public static String d(Context context) {
        try {
            DisplayMetrics i = i(context);
            if (i != null) {
                return String.valueOf(i.heightPixels);
            }
        } catch (Exception e) {
        }
        return "";
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static String e(Context context) {
        try {
            if (Build.VERSION.SDK_INT < 23 || context.checkSelfPermission("android.permission.READ_PHONE_STATE") == 0) {
                return ((TelephonyManager) context.getSystemService("phone")).getDeviceId();
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }


    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static String f(Context context) {
        try {
            if (Build.VERSION.SDK_INT < 23 || context.checkSelfPermission("android.permission.READ_PHONE_STATE") == 0) {
                return ((TelephonyManager) context.getSystemService("phone")).getSubscriberId();
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    public static String g(Context context) {
        try {
            return context.getResources().getConfiguration().locale.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String h(Context context) {
        try {
            return Build.MANUFACTURER;
        } catch (Exception e) {
            return "";
        }
    }

    private static DisplayMetrics i(Context context) {
        try {
            Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            defaultDisplay.getMetrics(displayMetrics);
            return displayMetrics;
        } catch (Exception e) {
            return null;
        }
    }

    public static String j(Context context) {
        try {
            return Build.MODEL;
        } catch (Exception e) {
            return "";
        }
    }

    @SuppressLint("MissingPermission")
    public static String getNetNameType(Context context) {
        try {
             NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getApplicationContext().getSystemService("connectivity")).getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                String typeName = activeNetworkInfo.getTypeName();
                return "mobile".equals(typeName.toLowerCase()) ? activeNetworkInfo.getSubtypeName() : typeName;
            }
        } catch (Exception e) {
        }
        return "none";
    }

    public static String l(Context context) {
        try {
            String a = a("ro.serialno");
            return !TextUtils.isEmpty(a) ? a : a("ro.hw.dxos.SN");
        } catch (Exception e) {
            return "";
        }
    }

    public static String m(Context context) {
        try {
            DisplayMetrics i = i(context);
            if (i != null) {
                return String.valueOf(i.widthPixels);
            }
        } catch (Exception e) {
        }
        return "";
    }

    public static String n(Context context) {
        try {
            WifiInfo connectionInfo = ((WifiManager) context.getSystemService("wifi")).getConnectionInfo();
            if (connectionInfo != null) {
                return connectionInfo.getMacAddress();
            }
            return null;
        } catch (Exception e) {
            return "";
        }
    }

    public static String a() {
        String readLine = "unknown";
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("/system/bin/cat /proc/meminfo").getInputStream()));
            do {
                readLine = bufferedReader.readLine();
                if (readLine != null && readLine.length() > 0) {
                }
                return "unknown";
            } while (!readLine.startsWith("MemFree:"));

        } catch (Exception e) {
        }
        return readLine;
    }

    public static String a(Context context) {
        try {
            return String.valueOf(Build.VERSION.SDK_INT);
        } catch (Exception e) {
            return "";
        }
    }

    public static String b(Context context) {
        try {
            return ((TelephonyManager) context.getSystemService("phone")).getNetworkOperator();
        } catch (Exception e) {
            return "";
        }
    }
}