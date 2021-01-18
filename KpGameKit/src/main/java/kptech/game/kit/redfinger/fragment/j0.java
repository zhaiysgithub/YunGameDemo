package kptech.game.kit.redfinger.fragment;

import android.os.Build;

/* compiled from: Utils */
public class j0 {
    private static int a = 0;
    private static int b = 1;
    public static final String[] c = {"IP903H-54U4, IP903H_54U4, Hisense", "IP903H-05U1, IP903H, Hisense"};

    public static void a(int i) {
        a = i;
    }

    public static int b() {
        return a;
    }

    public static int c() {
        return b;
    }

    public static boolean a() {
        String str = Build.DEVICE;
        String str2 = Build.MODEL;
        String str3 = Build.BRAND;
        int i = 0;
        while (true) {
            String[] strArr = c;
            if (i >= strArr.length) {
                return false;
            }
            String[] split = strArr[i].split(", ");
            if (split[0].equals(str) && split[1].equals(str2) && split[2].equals(str3)) {
                return true;
            }
            i++;
        }
    }

    public static void b(int i) {
        v.a("setVideoOrientation orientation: " + i);
        b = i;
    }
}
