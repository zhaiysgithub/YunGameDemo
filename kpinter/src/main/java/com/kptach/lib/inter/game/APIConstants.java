package com.kptach.lib.inter.game;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class APIConstants {


    //申请设备成功
    public static final int APPLY_DEVICE_SUCCESS = 1000;
    //申请设备失败
    public static final int ERROR_APPLY_DEVICE = 1001;
    //连接设备成功
    public static final int CONNECT_DEVICE_SUCCESS = 1002;
    //重连设备成功
    public static final int RECONNECT_DEVICE_SUCCESS = 1003;
    //连接设备失败
    public static final int ERROR_CONNECT_DEVICE = 1004;
    //释放设备
    public static final int RELEASE_SUCCESS = 1005;
    //设备繁忙 (无设备)
    public static final int ERROR_DEVICE_BUSY = 1006;
    //网络错误
    public static final int ERROR_NETWORK = 1007;
    //SDK初始化失败
    public static final int ERROR_SDK_INIT = 1008;
    //游戏信息错误
    public static final int ERROR_GAME_INFO = 1009;
    //游戏被取消启动
    public static final int ERROR_GAME_CANCLED = 1010;
    //广告加载中
    public static final int AD_LOADING = 1011;
    //游戏加载中
    public static final int GAME_LOADING = 1012;
    //云手机数据恢复中
    public static final int RECOVER_DATA_LOADING = 1013;
    //可用时间到达
    public static final int TIMEOUT_AVAILABLE_TIME = 1014;
    //游戏帧率数据
    public static final int DATA_FPS_GAME = 1015;
    //网络延迟数据
    public static final int DATA_NETWORK_LATENCY = 1016;
    //切换游戏分辨率成功
    public static final int SWITCH_GAME_RESOLUTION_SUCCESS = 1017;
    //切换游戏分辨率失败
    public static final int SWITCH_GAME_RESOLUTION_ERROR = 1018;
    //游戏数据内部错误
    public static final int ERROR_SDK_INNER = 1019;
    //调用API错误
    public static final int ERROR_CALL_API = 1020;
    //认证失败
    public static final int ERROR_AUTH = 1021;

    //其他错误联系管理员
    public static final int ERROR_OTHER = 65535;



    public static final String DEVICE_VIDEO_QUALITY_HD = "GRADE_LEVEL_HD";
    public static final String DEVICE_VIDEO_QUALITY_ORDINARY = "GRADE_LEVEL_ORDINARY";
    public static final String DEVICE_VIDEO_QUALITY_HS = "GRADE_LEVEL_HS";
    public static final String DEVICE_VIDEO_QUALITY_LS = "GRADE_LEVEL_LS";
    public static final String DEVICE_VIDEO_QUALITY_AUTO = "GRADE_LEVEL_AUTO";

    public static final String MOCK_IMEI = "imei";
    public static final String MOCK_ANDROID_ID = "androidId";
    public static final String MOCK_WIFIMAC = "wifimac";
    public static final String MOCK_SERIALNO = "serialno";
    public static final String MOCK_BASEBAND = "baseband";
    public static final String MOCK_BOARD = "board";
    public static final String MOCK_DISPLAYID = "displayId";
    public static final String MOCK_DEVICE = "device";
    public static final String MOCK_FINGERPRINT = "fingerprint";
    public static final String MOCK_PRODUCTNAME = "productName";
    public static final String MOCK_BUILDID = "buildId";
    public static final String MOCK_BUILDHOST = "buildHost";
    public static final String MOCK_BOOTLOADER = "bootloader";
    public static final String MOCK_BUILDTAGS = "buildTags";
    public static final String MOCK_BUILDTYPE = "buildType";
    public static final String MOCK_BUILDVERSIONINC = "buildVersionInc";
    public static final String MOCK_BUILDDATEUTC = "buildDateUtc";
    public static final String MOCK_BUILDDESCRIPTION = "buildDescription";
    public static final String MOCK_WIFINAME = "wifiname";
    public static final String MOCK_BSSID = "bssid";

    public static final int AV_DATA_TYPE_VIDEO = 0;
    public static final int AV_DATA_TYPE_AUDIO = 1;

    public static final int AV_ENCODE_TYPE_IDR_FRAME = 0;
    public static final int AV_ENCODE_TYPE_P_FRAME = 1;
    public static final int AV_ENCODE_TYPE_PPS_FRAME = 2;
    public static final int AV_ENCODE_TYPE_SPS_FRAME = 3;

    public static final int PAD_KEY_HOME = 1;//GamePadKey.GAMEPAD_HOME.getKey();
    public static final int PAD_KEY_BACK = 2;//GamePadKey.GAMEPAD_BACK.getKey();

    public APIConstants() {
    }

    @Retention(RetentionPolicy.CLASS)
    public @interface MockInfo {
    }

    @Retention(RetentionPolicy.CLASS)
    public @interface VideoQuality {
    }

}
