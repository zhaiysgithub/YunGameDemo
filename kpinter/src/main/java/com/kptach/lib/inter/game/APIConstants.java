package com.kptach.lib.inter.game;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class APIConstants {

    //游戏初始化错误
    public static final int ERROR_GAME_INIT = 100;
    //调用API错误
    public static final int ERROR_CALL_API = 101;
    //认证失败
    public static final int ERROR_AUTH = 102;
    //申请设备成功
    public static final int APPLY_DEVICE_SUCCESS = 1000;
    //申请设别失败
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
    //设备过期
    public static final int ERROR_DEVICE_EXPIRED = 1008;
    //初始化游戏失败 (启动游戏失败)
    public static final int ERROR_SDK_INIT = 1009;
    //无该游戏信息
    public static final int ERROR_GAME_INFO = 1010;
    //游戏被取消启动
    public static final int ERROR_GAME_CANCLED = 1011;
    //启动游戏成功
    public static final int GAME_SDK_INIT_SUCCESS = 1012;
    //广告加载中
    public static final int AD_LOADING = 1013;
    //游戏加载中
    public static final int GAME_LOADING = 1014;
    //云手机数据恢复中
    public static final int RECOVER_DATA_LOADING = 1015;
    //可用时间到达
    public static final int TIMEOUT_AVAILABLE_TIME = 1016;
    //长时间未操作
    public static final int TIMEOUT_NO_OPS = 1017;
    //游戏退出
    public static final int GAME_EXIT_SUCCESS = 1018;
    //游戏帧率数据
    public static final int DATA_FPS_GAME = 1019;
    //网络延迟数据
    public static final int DATA_NETWORK_LATENCY = 1020;
    //切换游戏分辨率成功
    public static final int SWITCH_GAME_RESOLUTION_SUCCESS = 1021;
    //切换游戏分辨率失败
    public static final int SWITCH_GAME_RESOLUTION_ERROR = 1022;
    //游戏数据内部错误
    public static final int ERROR_GAME_INNER = 1023;
    //游戏开始连接成功 (处理游戏开始连接到游戏启动成功这段时间黑屏问题)
    public static final int GAME_START_CONNECT = 1024;
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
