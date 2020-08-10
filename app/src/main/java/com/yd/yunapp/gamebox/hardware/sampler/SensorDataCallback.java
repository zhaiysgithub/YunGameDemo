package com.yd.yunapp.gamebox.hardware.sampler;

/**
 * Created by zhouzhiyong on 19-5-14.
 * Project: GameBox
 */
public interface SensorDataCallback<T> {

    void sampling(int type, T data);
}
