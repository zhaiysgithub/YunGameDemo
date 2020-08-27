package kptech.game.kit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class APIConstants {
    public static final int APPLY_DEVICE_SUCCESS = com.yd.yunapp.gameboxlib.APIConstants.APPLY_DEVICE_SUCCESS;
    public static final int CONNECT_DEVICE_SUCCESS = com.yd.yunapp.gameboxlib.APIConstants.CONNECT_DEVICE_SUCCESS;
    public static final int RECONNECT_DEVICE_SUCCESS = com.yd.yunapp.gameboxlib.APIConstants.RECONNECT_DEVICE_SUCCESS;
    public static final int WAITING_QUEUE = com.yd.yunapp.gameboxlib.APIConstants.WAITING_QUEUE;

    /** 队列更新 */
    public static final int QUEUE_UPDATE = com.yd.yunapp.gameboxlib.APIConstants.QUEUE_UPDATE;
    /** 队列加速前 */
    public static final int QUEUE_ACCELERATE_BEFORE = com.yd.yunapp.gameboxlib.APIConstants.QUEUE_ACCELERATE_BEFORE;
    /** 队列加速后 */
    public static final int QUEUE_ACCELERATE_AFTER = com.yd.yunapp.gameboxlib.APIConstants.QUEUE_ACCELERATE_AFTER;
    /** 排队成功 */
    public static final int QUEUE_SUCCESS = com.yd.yunapp.gameboxlib.APIConstants.QUEUE_SUCCESS;
    /** 队列退出 */
    public static final int QUEUE_EXIT = com.yd.yunapp.gameboxlib.APIConstants.QUEUE_EXIT;
    /** 排队失败 */
    public static final int QUEUE_FAILED = com.yd.yunapp.gameboxlib.APIConstants.QUEUE_FAILED;
    /** 不存在队列，直接申请*/
    public static final int QUEUE_NO_QUEUE = com.yd.yunapp.gameboxlib.APIConstants.QUEUE_NO_QUEUE;

    public static final int RELEASE_SUCCESS = com.yd.yunapp.gameboxlib.APIConstants.RELEASE_SUCCESS;

    public static final int ERROR_API_CALL_ERROR = com.yd.yunapp.gameboxlib.APIConstants.ERROR_API_CALL_ERROR;
    public static final int ERROR_NO_DEVICE = com.yd.yunapp.gameboxlib.APIConstants.ERROR_NO_DEVICE;
    public static final int ERROR_NETWORK_ERROR = com.yd.yunapp.gameboxlib.APIConstants.ERROR_NETWORK_ERROR;
    public static final int ERROR_DEVICE_EXPIRED = com.yd.yunapp.gameboxlib.APIConstants.ERROR_DEVICE_EXPIRED;
    public static final int ERROR_DEVICE_OTHER_ERROR = com.yd.yunapp.gameboxlib.APIConstants.ERROR_DEVICE_OTHER_ERROR;
    public static final int ERROR_OTHER_DEVICE_RUNNING = com.yd.yunapp.gameboxlib.APIConstants.ERROR_OTHER_DEVICE_RUNNING;
    public static final int ERROR_SDK_INIT_ERROR = com.yd.yunapp.gameboxlib.APIConstants.ERROR_SDK_INIT_ERROR;
    public static final int ERROR_APP_QUERY_ERROR = com.yd.yunapp.gameboxlib.APIConstants.ERROR_APP_QUERY_ERROR;
    public static final int ERROR_WAITING_QUEUE = com.yd.yunapp.gameboxlib.APIConstants.ERROR_WAITING_QUEUE;
    public static final int ERROR_OTHER_DEVICE_WAITING = com.yd.yunapp.gameboxlib.APIConstants.ERROR_OTHER_DEVICE_WAITING;
    public static final int ERROR_DEVICE_TOKEN_VALID_FAILED = com.yd.yunapp.gameboxlib.APIConstants.ERROR_DEVICE_TOKEN_VALID_FAILED;

    public static final int ERROR_GAME_INF_EMPTY = -2001;
    public static final int ERROR_GAME_CANCEL = -2002;
    public static final int ERROR_AD_FAILE = -2003;
    public static final int AD_LOADING = 11001;
    public static final int AD_FINISHED = 11002;

    public static final String DEVICE_VIDEO_QUALITY_HD = com.yd.yunapp.gameboxlib.APIConstants.DEVICE_VIDEO_QUALITY_HD;
    public static final String DEVICE_VIDEO_QUALITY_ORDINARY = com.yd.yunapp.gameboxlib.APIConstants.DEVICE_VIDEO_QUALITY_ORDINARY;
    public static final String DEVICE_VIDEO_QUALITY_HS = com.yd.yunapp.gameboxlib.APIConstants.DEVICE_VIDEO_QUALITY_HS;
    public static final String DEVICE_VIDEO_QUALITY_LS = com.yd.yunapp.gameboxlib.APIConstants.DEVICE_VIDEO_QUALITY_LS;
    public static final String DEVICE_VIDEO_QUALITY_AUTO = com.yd.yunapp.gameboxlib.APIConstants.DEVICE_VIDEO_QUALITY_AUTO;
    public static final String MOCK_IMEI = com.yd.yunapp.gameboxlib.APIConstants.MOCK_IMEI;
    public static final String MOCK_ANDROID_ID = com.yd.yunapp.gameboxlib.APIConstants.MOCK_ANDROID_ID;


    public APIConstants() {
    }

    @Retention(RetentionPolicy.CLASS)
    public @interface MockInfo {
    }

    @Retention(RetentionPolicy.CLASS)
    public @interface VideoQuality {
    }

}
