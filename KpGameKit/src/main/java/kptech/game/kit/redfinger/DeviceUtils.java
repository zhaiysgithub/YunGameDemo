package kptech.game.kit.redfinger;

import android.os.Build;
import android.os.Process;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DeviceUtils {

    public static boolean isSupportArm64() {
        boolean ret = false;
        if (Build.VERSION.SDK_INT < 21) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader("/proc/cpuinfo"));
                ret = bufferedReader.readLine().contains("aarch64");
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (Build.SUPPORTED_64_BIT_ABIS.length > 0) {
            ret = true;
        }
        Log.i("DeviceUtils", "isSupportArm64: " + ret);
        return ret;
    }

    public static boolean is64Bit() {
        if (Build.VERSION.SDK_INT >= 23) {
            return Process.is64Bit();
        }
        if (!isSupportArm64()) {
            return false;
        }
        String property = System.getProperty("os.arch");
        Log.i("DeviceUtils", "processIs64Bit arc: " + property);
        if (property == null || !property.contains("64")) {
            return false;
        }
        return true;
    }
}
