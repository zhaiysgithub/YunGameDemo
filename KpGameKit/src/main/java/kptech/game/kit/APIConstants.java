package kptech.game.kit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class APIConstants {
    public static final int APPLY_DEVICE_SUCCESS = 1000;
    public static final int CONNECT_DEVICE_SUCCESS = 1001;
    public static final int RECONNECT_DEVICE_SUCCESS = 1002;
    public static final int WAITING_QUEUE = 1003;
    public static final int QUEUE_UPDATE = 1004;
    public static final int QUEUE_ACCELERATE_BEFORE = 1005;
    public static final int QUEUE_ACCELERATE_AFTER = 1006;
    public static final int QUEUE_SUCCESS = 1007;
    public static final int QUEUE_EXIT = 1008;
    public static final int QUEUE_FAILED = 1009;
    public static final int QUEUE_NO_QUEUE = 1010;
    public static final int ERROR_API_CALL_ERROR = -1000;
    public static final int ERROR_NO_DEVICE = -1001;
    public static final int ERROR_NETWORK_ERROR = -1002;
    public static final int ERROR_DEVICE_EXPIRED = -1003;
    public static final int ERROR_DEVICE_OTHER_ERROR = -1004;
    public static final int ERROR_OTHER_DEVICE_RUNNING = -1005;
    public static final int ERROR_SDK_INIT_ERROR = -1006;
    public static final int ERROR_APP_QUERY_ERROR = -1007;
    public static final int ERROR_WAITING_QUEUE = -1008;
    public static final int ERROR_OTHER_DEVICE_WAITING = -1009;
    public static final int ERROR_DEVICE_TOKEN_VALID_FAILED = -1010;

    public static final int ERROR_GAME_INF_EMPTY = -2001;
    public static final int ERROR_DEVICE_CANCEL = -2002;
    public static final int ERROR_AD_FAILE = -2003;

    public static final String DEVICE_VIDEO_QUALITY_HD = "GRADE_LEVEL_HD";
    public static final String DEVICE_VIDEO_QUALITY_ORDINARY = "GRADE_LEVEL_ORDINARY";
    public static final String DEVICE_VIDEO_QUALITY_HS = "GRADE_LEVEL_HS";
    public static final String DEVICE_VIDEO_QUALITY_LS = "GRADE_LEVEL_LS";
    public static final String DEVICE_VIDEO_QUALITY_AUTO = "GRADE_LEVEL_AUTO";


    public APIConstants() {
    }

    @Retention(RetentionPolicy.CLASS)
    public @interface VideoQuality {
    }

}
