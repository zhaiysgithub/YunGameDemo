package com.yd.yunapp.gamebox.hardware.sampler;

public interface SamplingCallback {
    void onSamplerData(int sensor, int type, byte[] data);

    void onSensorSamplerData(int sensor, int sensorType, float... data);
}
