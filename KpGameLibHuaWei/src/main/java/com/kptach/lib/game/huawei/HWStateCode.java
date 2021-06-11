package com.kptach.lib.game.huawei;

public class HWStateCode {

    public static final int code_connecting = 256;
    public static final int code_connect_success = 512;
    public static final int code_server_unreachable = 769;
    public static final int code_resource_inusing = 770;
    public static final int code_verifying = 1024;
    public static final int code_verify_success = 1280;
    public static final int code_verify_parameter_missing = 1537;
    public static final int code_verify_parameter_invalid = 1538;
    // 1539,1540,1541,1542,2307,3331，4865
    public static final int code_server_inner_error = 1539;
    public static final int code_game_starting = 1792;
    public static final int code_game_start_success = 2048;
    public static final int code_game_package_notexist = 2305;
    public static final int code_game_start_failed = 2306;
    public static final int code_start_parameter_missing = 2308;
    public static final int code_connect_lost = 2560;
    public static final int code_reconnection = 2816;
    public static final int code_reconnecting_success = 3072;
    public static final int code_reconnect_parameter_invalid = 3329;
    public static final int code_reconnect_server_unreachable = 3330;
    //可用时间到达
    public static final int code_available_time_usedup = 3584;
    //未操作超时
    public static final int code_notouch_timeout = 3840;
    //切换后台超时
    public static final int code_switch_background_timeout = 4096;
    public static final int code_decode_error = 4352;
    public static final int code_engine_start_failed = 4353;
    //切换后台
    public static final int code_switch_backgroud = 4608;
    //切到前台
    public static final int code_switch_foregroud = 5120;
    //游戏退出
    public static final int code_game_exit = 5888;
    //分辨率设置成功
    public static final int code_set_resolution_success = 6400;
    //分辨率设置失败
    public static final int code_set_resolution_error = 8193;
    //无效操作
    public static final int code_invalid_operation = 65535;

    //错误的code 数组
    public static final int[] errorCodeArray = {796, 770, 1539, 1540, 1541, 1542, 2305, 2306, 2307
            , 2308, 2560, 3329, 3330, 3331, 4352, 4353, 4865};
}
