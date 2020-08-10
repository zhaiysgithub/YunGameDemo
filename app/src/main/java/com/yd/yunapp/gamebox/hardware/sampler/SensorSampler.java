package com.yd.yunapp.gamebox.hardware.sampler;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import kptech.game.kit.SensorConstants;


/**
 * Created by zhouzhiyong on 19-5-13.
 * Project: GameBox
 */
public class SensorSampler extends Sampler implements SensorEventListener {

    public static final String TAG = "SensorSampler";
    private SensorManager mSensorManager;
    // 对应红手指激活Sensor采样接口的id
    private int mSensorId;
    // 对应红手指发送传感器数据接口的id
    private int mRedfingerSensorId;
    private Sensor mSensor;

    public SensorSampler(Activity context, SamplingCallback callback, int id) {
        super(context, callback);
        mSensorId = id;
    }

    @Override
    public void onStart() {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(getSensorType());
    }

    @Override
    public void onResume() {
        if (mSensor != null) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            if (DEBUG) {
                Log.d(TAG, "not support this sensor, type = " + mSensorId);
            }
        }
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onStop() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public String[] getRequestPermission() {
        return new String[0];
    }

    @Override
    public void waitGrantPermission() {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float x = event.values[0];
        final float y = event.values[1];
        final float z = event.values[2];
        if (callback != null) {
            callback.onSensorSamplerData(mSensorId, mRedfingerSensorId, x, y, z);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private int getSensorType() {
        switch (mSensorId) {
            case SensorConstants.HARDWARE_ID_ACCELEROMETER:
                // 加速度传感器 202
                mRedfingerSensorId = SensorConstants.SENSOR_TYPE_ACCELEROMETER;
                return Sensor.TYPE_ACCELEROMETER;
            case SensorConstants.HARDWARE_ID_PRESSURE:
                // 压力传感器 203
                mRedfingerSensorId = SensorConstants.SENSOR_TYPE_PRESSURE;
                return Sensor.TYPE_PRESSURE;
            case SensorConstants.HARDWARE_ID_GRAVITY:
                // 重力传感器 213
                mRedfingerSensorId = SensorConstants.SENSOR_TYPE_GRAVITY;
                return Sensor.TYPE_GRAVITY;
            case SensorConstants.HARDWARE_ID_GYROSCOPE:
                // 陀螺仪 204
                mRedfingerSensorId = SensorConstants.SENSOR_TYPE_GYROSCOPE;
                return Sensor.TYPE_GYROSCOPE;
            case SensorConstants.HARDWARE_ID_MAGNETOMETER:
                // 磁力传感器 205
                mRedfingerSensorId = SensorConstants.SENSOR_TYPE_MAGNETOMETER;
                return Sensor.TYPE_MAGNETIC_FIELD;
            default:
                break;
        }
        return -1;
    }

}
