package kptech.game.kit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SensorConstants {

    public static final int HARDWARE_ID_LOCATION = 201;
    public static final int HARDWARE_ID_ACCELEROMETER = 202;
    public static final int HARDWARE_ID_PRESSURE = 203;
    public static final int HARDWARE_ID_GYROSCOPE = 204;
    public static final int HARDWARE_ID_MAGNETOMETER = 205;
    public static final int HARDWARE_ID_MIC = 211;
    public static final int HARDWARE_ID_VIDEO_BACK = 212;
    public static final int HARDWARE_ID_VIDEO_FRONT = 199;
    public static final int HARDWARE_ID_GRAVITY = 213;
    public static final int CAMERA_VIDEO_TYPE_SPS = 0;
    public static final int CAMERA_VIDEO_TYPE_PPS = 1;
    public static final int CAMERA_VIDEO_TYPE_IFRAME = 2;
    public static final int CAMERA_VIDEO_TYPE_PFRAME = 3;
    public static final int AUDIO_TYPE_ACC_SPECIAL_DATA = 0;
    public static final int AUDIO_TYPE_ACC_FRAME = 1;
    public static final int SENSOR_ENABLE = 1;
    public static final int SENSOR_DISABLE = 0;
    public static final int SENSOR_TYPE_GRAVITY = 0;
    public static final int SENSOR_TYPE_MAGNETOMETER = 1;
    public static final int SENSOR_TYPE_GYROSCOPE = 2;
    public static final int SENSOR_TYPE_PRESSURE = 3;
    public static final int SENSOR_TYPE_ACCELEROMETER = 4;

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
