package com.yd.yunapp.gamebox.hardware;

import android.app.Activity;

import com.yd.yunapp.gamebox.hardware.sampler.CameraSampler;
import com.yd.yunapp.gamebox.hardware.sampler.LocationSampler;
import com.yd.yunapp.gamebox.hardware.sampler.MicSampler;
import com.yd.yunapp.gamebox.hardware.sampler.Sampler;
import com.yd.yunapp.gamebox.hardware.sampler.SamplingCallback;
import com.yd.yunapp.gamebox.hardware.sampler.SensorSampler;
import com.yd.yunapp.gamebox.permission.PermissionHelper;
import com.yd.yunapp.gamebox.utils.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import kptech.game.kit.IDeviceControl;
import kptech.game.kit.SensorConstants;

/**
 * Created by zhouzhiyong on 19-5-13.
 * Project: GameBox
 */
public class HardwareManager implements SamplingCallback {

    private static final String TAG = "HardwareManager";
    private static final boolean DEBUG = true;

    private IDeviceControl mDeviceControl;
    private Activity mActivity;
    private Map<Integer, Sampler> mSamplers;

    public HardwareManager(Activity context) {
        mActivity = context;
        mSamplers = new HashMap<>();
    }

    public IDeviceControl getDeviceControl() {
        return mDeviceControl;
    }

    public void setDeviceControl(IDeviceControl mDeviceControl) {
        this.mDeviceControl = mDeviceControl;
    }

    public void registerHardwareState(int id, int state) {
        if (DEBUG) {
            Logger.d(TAG, "registerHardwareState id = " + id + "  state = " + state);
        }
        Sampler sampler = mSamplers.get(id);
        if (sampler == null) {
            sampler = createSampler(id);
            if (sampler == null) {
                return;
            }
            mSamplers.put(id, sampler);
            sampler.onStart();
        }
        if (sampler.getState() != state) {
            if (state == SensorConstants.SENSOR_ENABLE) {
                if (checkPermission(sampler)) {
                    sampler.onResume();
                } else {
                    sampler.requestPermission();
                }
            } else if (state == SensorConstants.SENSOR_DISABLE) {
                sampler.onPause();
            }
        }
        sampler.setState(state);
    }

    private void releaseSampler(int id) {
        Sampler sampler = mSamplers.get(id);
        if (sampler != null) {
            sampler.onStop();
        }
    }

    public void release() {
        if (DEBUG) {
            Logger.d(TAG, "release all sampler");
        }
        Set<Integer> keys = mSamplers.keySet();
        for (Integer id : keys) {
            releaseSampler(id);
        }
        mSamplers.clear();
        mDeviceControl = null;
    }

    private Sampler createSampler(int id) {
        Sampler sampler = null;
        switch (id) {
            case SensorConstants.HARDWARE_ID_MIC:
                sampler = new MicSampler(mActivity, this);
                break;
            case SensorConstants.HARDWARE_ID_ACCELEROMETER:
            case SensorConstants.HARDWARE_ID_PRESSURE:
            case SensorConstants.HARDWARE_ID_GRAVITY:
            case SensorConstants.HARDWARE_ID_GYROSCOPE:
            case SensorConstants.HARDWARE_ID_MAGNETOMETER:
                sampler = new SensorSampler(mActivity, this, id);
                break;
            case SensorConstants.HARDWARE_ID_VIDEO_BACK:
                sampler = new CameraSampler(mActivity, this);
                break;
            case SensorConstants.HARDWARE_ID_LOCATION:
                sampler = new LocationSampler(mActivity, this);
                break;
            default:
                break;
        }
        return sampler;
    }

    private boolean checkPermission(Sampler sampler) {
        return PermissionHelper.hasPermissions(mActivity, sampler.getRequestPermission());
    }

    @Override
    public void onSamplerData(int sensor, int type, byte[] data) {
        if (mDeviceControl != null) {
            mDeviceControl.sendSensorInputData(sensor, type, data);
        }
    }

    @Override
    public void onSensorSamplerData(int sensor, int sensorType, float... data) {
        if (mDeviceControl != null) {
            mDeviceControl.sendSensorInputData(sensor, sensorType, data);
        }
    }
}
