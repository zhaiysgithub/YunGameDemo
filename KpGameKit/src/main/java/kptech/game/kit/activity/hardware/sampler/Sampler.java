package kptech.game.kit.activity.hardware.sampler;

import android.app.Activity;

import kptech.game.kit.SensorConstants;
import kptech.game.kit.activity.permission.PermissionHandler;
import kptech.game.kit.activity.permission.PermissionHelper;

/**
 * Created by zhouzhiyong on 19-5-13.
 * Project: GameBox
 */
public abstract class Sampler implements PermissionHandler.PermissionCallback {

    static final boolean DEBUG = true;
    Activity mContext;
    int mState = -1;
    SamplingCallback callback;

    public Sampler(Activity context, SamplingCallback callback) {
        mContext = context;
        this.callback = callback;
    }

    public abstract void onStart();

    public abstract void onResume();

    public abstract void onPause();

    public abstract void onStop();

    public abstract String[] getRequestPermission();

    public abstract void waitGrantPermission();

    public void requestPermission() {
        waitGrantPermission();
        PermissionHelper.requestPermission(mContext, getRequestPermission(), this);
    }

    public void onPermissionsGrantEnd(boolean grant) {

    }

    public void setState(int state) {
        mState = state;
    }

    public int getState() {
        return mState;
    }

    @Override
    public void onPermissionResult(String[] perms, int[] grants) {
        boolean isGranted = PermissionHelper.isPermissionsGranted(perms, grants);
        if (isGranted) {
            if (mState == SensorConstants.SENSOR_DISABLE) {
                onPause();
            } else if (mState == SensorConstants.SENSOR_ENABLE) {
                onResume();
            }
            onPermissionsGrantEnd(true);
        } else {
            onPause();
            onPermissionsGrantEnd(false);
        }
    }
}
