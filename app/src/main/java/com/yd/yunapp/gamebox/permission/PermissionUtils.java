package com.yd.yunapp.gamebox.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.AppOpsManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.yd.yunapp.gamebox.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hupianpian on 17-9-25.
 */

public class PermissionUtils {
    private static final String TAG = "PermissionUtils";

    public static boolean hasPermission(@NonNull Context context, @NonNull String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        for (String permission : permissions) {
            String op = AppOpsManagerCompat.permissionToOp(permission);
            if (TextUtils.isEmpty(op)) {
                continue;
            }
            int result = AppOpsManagerCompat.noteProxyOp(context, op, context.getPackageName());
            if (result == AppOpsManagerCompat.MODE_IGNORED) {
                return false;
            }
            result = ContextCompat.checkSelfPermission(context, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static String[] getDeniedPermissions(Context context, @NonNull String... permissions) {
        List<String> deniedList = new ArrayList<>(1);
        for (String permission : permissions) {
            if (!hasPermission(context, permission)) {
                deniedList.add(permission);
            }
        }

        return deniedList.toArray(new String[deniedList.size()]);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void requestPermissions(Object o, int requestCode, String... permissions) {
        if (o instanceof Activity) {
            ActivityCompat.requestPermissions(((Activity) o), permissions, requestCode);
        } else if (o instanceof Fragment) {
            ((Fragment) o).requestPermissions(permissions, requestCode);
        } else if (o instanceof android.app.Fragment) {
            ((android.app.Fragment) o).requestPermissions(permissions, requestCode);
            Logger.e(TAG, "The " + o.getClass().getName() + " is not support " + "requestPermissions()");
        }
    }

    public static boolean shouldShowRationalePermissions(Object o, String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }

        boolean rationale = false;
        for (String permission : permissions) {
            if (o instanceof Activity) {
                rationale = ActivityCompat.shouldShowRequestPermissionRationale((Activity) o, permission);
            } else if (o instanceof Fragment) {
                rationale = ((Fragment) o).shouldShowRequestPermissionRationale(permission);
            } else if (o instanceof android.app.Fragment) {
                rationale = ((android.app.Fragment) o).shouldShowRequestPermissionRationale(permission);
            }
            if (rationale) {
                return true;
            }
        }
        return false;
    }

    public static Context getContext(Object o) {
        if (o instanceof Activity) {
            return (Activity) o;
        } else if (o instanceof Fragment) {
            return ((Fragment) o).getActivity();
        } else if (o instanceof android.app.Fragment) {
            return ((android.app.Fragment) o).getActivity();
        }

        throw new IllegalArgumentException("The " + o.getClass().getName() + " is not support.");
    }
}
