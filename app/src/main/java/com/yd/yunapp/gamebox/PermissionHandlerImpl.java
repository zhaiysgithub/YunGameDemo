package com.yd.yunapp.gamebox;

import android.app.Activity;
import android.content.Context;

import com.yd.yunapp.gamebox.permission.PermissionHandler;

public class PermissionHandlerImpl implements PermissionHandler {
    @Override public boolean hasPermission(Context context, String permission) {
        return true;
    }

    @Override public void requestPermissions(Activity activity, String[] permissions, PermissionCallback callback) {

    }

    @Override public void showPermissionDialog(Activity activity, String[] permissions, String msg) {

    }
}
