package kptech.game.kit.activity.permission;

import android.app.Activity;
import android.content.Context;

/**
 * 权限处理器
 * 涉及的业务： 基础权限，云游戏使用到权限。
 */
public interface PermissionHandler {

    /**
     * 权限检测
     * @param context context
     * @param permission 权限
     * @return 是否授予
     */
    boolean hasPermission(Context context, String permission);

    /**
     * 申请权限
     * @param activity activity
     * @param permissions 多个权限
     * @param callback 申请结果回调
     */
    void requestPermissions(Activity activity, String[] permissions, PermissionCallback callback);

    /**
     * 申请权限引导弹窗，会在权限被拒绝时弹出。
     * @param activity activity
     * @param permissions 多个权限
     * @param msg 权限请求说明文案
     */
    void showPermissionDialog(Activity activity, String[] permissions, String msg);

    /**
     * 权限申请回调
     */
    interface PermissionCallback {
        /**
         * 申请结果
         * @param perms 多个权限
         * @param grants 请求结果。PackageManager.PERMISSION_GRANTED或PackageManager.PERMISSION_DENIED
         */
        void onPermissionResult(String[] perms, int[] grants);
    }
}
