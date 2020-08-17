package kptech.game.kit.activity.permission;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kptech.game.kit.utils.Logger;

public class PermissionHelper {
    private static final boolean DEBUG = true;
    private static final String TAG = "PermissionHelper";
    private static Logger logger = new Logger(TAG);

    private static final Map<Integer, PermissionCallbackWrapper> sCallbackMap = new HashMap<>();

    private static PermissionHandler  mPermissionHandler = new PermissionHandlerImpl();


    private static List<String> checkPermissions(@NonNull Context context, @NonNull String... permissions) {
        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            PermissionHandler handler = mPermissionHandler;//((DemoApplication) context.getApplicationContext()).getPermissionHandler();
            boolean hasGranted;

            if (handler != null) {
                hasGranted = handler.hasPermission(context, permission);
            } else {
                hasGranted = PermissionUtils.hasPermission(context, permission);
            }

            if (DEBUG) {
                logger.info("checkPermission: " + permission + ", result: " + hasGranted);
            }

            if (!hasGranted) {
                permissionList.add(permission);
            }
        }

        return permissionList;
    }

    public static boolean hasPermissions(@NonNull Context context, @NonNull String... permissions) {
        List<String> result = checkPermissions(context, permissions);
        if (result != null && result.size() > 0) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean somePermissionPermanentlyDenied(Activity activity,
                                                          @NonNull String[] perms, @NonNull int[] grants) {
        List<String> granted = new ArrayList<>();
        List<String> denied = new ArrayList<>();
        for (int i = 0; i < perms.length; i++) {
            String perm = perms[i];
            if (grants[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(perm);
            } else {
                denied.add(perm);
            }
        }

        return somePermissionPermanentlyDenied(activity, denied);
    }

    public static boolean somePermissionPermanentlyDenied(Activity activity, @NonNull List<String> perms) {
        for (String deniedPermission : perms) {
            if (permissionPermanentlyDenied(activity, deniedPermission)) {
                logger.info("somePermissionPermanentlyDenied() perm = "+ deniedPermission);
                return true;
            }
        }

        return false;
    }

    public static boolean permissionPermanentlyDenied(Activity activity, String perms) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return !activity.shouldShowRequestPermissionRationale(perms);
        } else {
            return true;
        }
    }

    /**
     * 使用默认的实现时，需要在Activity中实现 onRequestPermissionsResult
     *
     * @param activity
     * @param permissions
     * @param callback
     */
    public static void requestPermission(Activity activity, String[] permissions,
                                         final PermissionHandler.PermissionCallback callback) {
        logger.info("requestPermission permission: " + Arrays.asList(permissions));

        if (callback == null) {
            return;
        }

        PermissionHandler handler = mPermissionHandler;//((DemoApplication) activity.getApplicationContext()).getPermissionHandler();
        PermissionCallbackWrapper callbackWrapper = callback != null ?
                new PermissionCallbackWrapper(callback) : null;
        if (handler != null) {
            handler.requestPermissions(activity, permissions, callbackWrapper);
        } else {
            int requestCode = getRequestCode(callback);
            sCallbackMap.put(requestCode, callbackWrapper);
            PermissionUtils.requestPermissions(activity, requestCode, permissions);
        }
    }

    public static void showPermissionGuideDialog(Activity activity, String[] permissions, String msg) {
        PermissionHandler handler = mPermissionHandler;//((DemoApplication) activity.getApplicationContext()).getPermissionHandler();
        if (handler != null) {
            handler.showPermissionDialog(activity, permissions, msg);
        } else {
            logger.info("show guide dialog");
            // showDefaultPermissionGuideDialog(activity, permissions, msg);
        }
    }

    private static class PermissionCallbackWrapper extends CallbackWrapper<PermissionHandler.PermissionCallback>
            implements PermissionHandler.PermissionCallback {

        public PermissionCallbackWrapper(PermissionHandler.PermissionCallback receiver) {
            super(receiver);
        }

        @Override
        public void onPermissionResult(String[] perms, int[] grants) {
            PermissionHandler.PermissionCallback callback = fetchReceiver();
            if (callback != null) {
                if (DEBUG) {
                    List<Integer> grantsList = new ArrayList<Integer>();
                    for (int i : grants) {
                        grantsList.add(i);
                    }
                    logger.info("PermissionCallback.onPermissionResult() perms = " + (perms != null ? Arrays.asList(perms) : "null") + ", grants = " + (grants != null ? grantsList : "null"));
                }

                callback.onPermissionResult(perms, grants);
            }
        }
    }

    private static int getRequestCode(PermissionHandler.PermissionCallback callback) {
        return callback.hashCode() & 0x000000ff;
    }

    public static boolean isPermissionsGranted(String[] strings, int[] ints) {
        if (strings != null && ints != null && strings.length > 0 && ints.length > 0) {
            for (int i = 0; i < strings.length; i++) {
                if (DEBUG) {
                    logger.info("requestPermission " + strings[i] + ": " + ints[i]);
                }
                if (ints[i] == PackageManager.PERMISSION_DENIED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void onRequestPermissionsResult(int requestCode,
                                                  String[] permissions, int[] grantResults) {
        if (DEBUG) {
            logger.info("onRequestPermissionsResult() requestCode = " + requestCode);
        }
        PermissionCallbackWrapper callbackWrapper = sCallbackMap.get(requestCode);
        if (callbackWrapper != null) {
            try {
                callbackWrapper.onPermissionResult(permissions, grantResults);
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            } finally {
                sCallbackMap.remove(requestCode);
            }
        }
    }

}
