package kptech.game.kit.activity.permission;

import android.app.Activity;
import android.content.Context;

public class PermissionHandlerImpl implements PermissionHandler {
    @Override public boolean hasPermission(Context context, String permission) {
        return true;
    }

    @Override public void requestPermissions(Activity activity, String[] permissions, PermissionCallback callback) {

    }

    @Override public void showPermissionDialog(Activity activity, String[] permissions, String msg) {

    }
}
