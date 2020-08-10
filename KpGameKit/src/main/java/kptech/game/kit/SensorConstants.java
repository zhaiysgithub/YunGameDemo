package kptech.game.kit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SensorConstants {

    public static final int HARDWARE_ID_LOCATION = com.yd.yunapp.gameboxlib.SensorConstants.HARDWARE_ID_LOCATION;
    public static final int HARDWARE_ID_ACCELEROMETER = com.yd.yunapp.gameboxlib.SensorConstants.HARDWARE_ID_ACCELEROMETER;
    public static final int HARDWARE_ID_PRESSURE = com.yd.yunapp.gameboxlib.SensorConstants.HARDWARE_ID_PRESSURE;
    public static final int HARDWARE_ID_GYROSCOPE = com.yd.yunapp.gameboxlib.SensorConstants.HARDWARE_ID_GYROSCOPE;
    public static final int HARDWARE_ID_MAGNETOMETER = com.yd.yunapp.gameboxlib.SensorConstants.HARDWARE_ID_MAGNETOMETER;
    public static final int HARDWARE_ID_MIC = com.yd.yunapp.gameboxlib.SensorConstants.HARDWARE_ID_MIC;
    public static final int HARDWARE_ID_VIDEO = com.yd.yunapp.gameboxlib.SensorConstants.HARDWARE_ID_VIDEO;
    public static final int HARDWARE_ID_GRAVITY = com.yd.yunapp.gameboxlib.SensorConstants.HARDWARE_ID_GRAVITY;
    public static final int CAMERA_VIDEO_TYPE_SPS = com.yd.yunapp.gameboxlib.SensorConstants.CAMERA_VIDEO_TYPE_SPS;
    public static final int CAMERA_VIDEO_TYPE_PPS = com.yd.yunapp.gameboxlib.SensorConstants.CAMERA_VIDEO_TYPE_PPS;
    public static final int CAMERA_VIDEO_TYPE_IFRAME = com.yd.yunapp.gameboxlib.SensorConstants.CAMERA_VIDEO_TYPE_IFRAME;
    public static final int CAMERA_VIDEO_TYPE_PFRAME = com.yd.yunapp.gameboxlib.SensorConstants.CAMERA_VIDEO_TYPE_PFRAME;
    public static final int AUDIO_TYPE_ACC_SPECIAL_DATA = com.yd.yunapp.gameboxlib.SensorConstants.AUDIO_TYPE_ACC_SPECIAL_DATA;
    public static final int AUDIO_TYPE_ACC_FRAME = com.yd.yunapp.gameboxlib.SensorConstants.AUDIO_TYPE_ACC_FRAME;
    public static final int SENSOR_ENABLE = com.yd.yunapp.gameboxlib.SensorConstants.SENSOR_ENABLE;
    public static final int SENSOR_DISABLE = com.yd.yunapp.gameboxlib.SensorConstants.SENSOR_DISABLE;
    public static final int SENSOR_TYPE_GRAVITY = com.yd.yunapp.gameboxlib.SensorConstants.SENSOR_TYPE_GRAVITY;
    public static final int SENSOR_TYPE_MAGNETOMETER = com.yd.yunapp.gameboxlib.SensorConstants.SENSOR_TYPE_MAGNETOMETER;
    public static final int SENSOR_TYPE_GYROSCOPE = com.yd.yunapp.gameboxlib.SensorConstants.SENSOR_TYPE_GYROSCOPE;
    public static final int SENSOR_TYPE_PRESSURE = com.yd.yunapp.gameboxlib.SensorConstants.SENSOR_TYPE_PRESSURE;
    public static final int SENSOR_TYPE_ACCELEROMETER = com.yd.yunapp.gameboxlib.SensorConstants.SENSOR_TYPE_ACCELEROMETER;

    public SensorConstants() {
    }

    @Retention(RetentionPolicy.CLASS)
    public @interface SensorType {
    }

    @Retention(RetentionPolicy.CLASS)
    public @interface SensorState {
    }

    @Retention(RetentionPolicy.CLASS)
    public @interface AudioType {
    }

    @Retention(RetentionPolicy.CLASS)
    public @interface CameraVideoType {
    }

    @Retention(RetentionPolicy.CLASS)
    public @interface CloudPhoneSensorId {
    }

}
