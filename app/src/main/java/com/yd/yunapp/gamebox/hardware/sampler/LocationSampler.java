package com.yd.yunapp.gamebox.hardware.sampler;

import android.app.Activity;

import kptech.game.kit.SensorConstants;


/**
 * Created by zhouzhiyong on 20-4-14.
 * Project: GameBoxSDK
 */
public class LocationSampler extends Sampler {


    public LocationSampler(Activity context, SamplingCallback callback) {
        super(context, callback);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

        callback.onSensorSamplerData(SensorConstants.HARDWARE_ID_LOCATION,
                SensorConstants.HARDWARE_ID_LOCATION,
                (float) 108.5229, // longitude
                (float) 38.4166, // latitude
                0.0f, // altitude
                0.0f, // floor
                0.0f, // horizontalaccuracy
                0.0f, // verticalaccuracy
                0.0f, // speed
                0.0f // direction
        );

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public String[] getRequestPermission() {
        return new String[0];
    }

    @Override
    public void waitGrantPermission() {

    }
}
