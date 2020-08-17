package kptech.game.kit.activity.hardware;

import android.app.Activity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import kptech.game.kit.DeviceControl;
import kptech.game.kit.SensorConstants;
import kptech.game.kit.activity.hardware.sampler.CameraSampler;
import kptech.game.kit.activity.hardware.sampler.LocationSampler;
import kptech.game.kit.activity.hardware.sampler.MicSampler;
import kptech.game.kit.activity.hardware.sampler.Sampler;
import kptech.game.kit.activity.hardware.sampler.SamplingCallback;
import kptech.game.kit.activity.hardware.sampler.SensorSampler;
import kptech.game.kit.activity.permission.PermissionHelper;
import kptech.game.kit.utils.Logger;

/**
 * Created by zhouzhiyong on 19-5-13.
 * Project: GameBox
 */
public class HardwareManager implements SamplingCallback {

    private static final String TAG = "HardwareManager";
    private static final boolean DEBUG = true;

    private Logger logger = new Logger("HardwareManager");

    private DeviceControl mDeviceControl;
    private Activity mActivity;
    private Map<Integer, Sampler> mSamplers;

    public HardwareManager(Activity context) {
        mActivity = context;
        mSamplers = new HashMap<>();
    }

    public DeviceControl getDeviceControl() {
        return mDeviceControl;
    }

    public void setDeviceControl(DeviceControl mDeviceControl) {
        this.mDeviceControl = mDeviceControl;
    }

    public void registerHardwareState(int id, int state) {
        if (DEBUG) {
            logger.info("registerHardwareState id = " + id + "  state = " + state);
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
            logger.info("release all sampler");
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
            case SensorConstants.HARDWARE_ID_VIDEO:
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
