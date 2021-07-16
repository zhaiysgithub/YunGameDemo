package com.kptach.lib.inter.game;

public interface IPlayDataListener {

    /**
     * 无操作超时回调
     * @param type 类型。1为后台，2为前台
     * @param timeout 超时时长，单位s
     * @return 返回true表示消耗了事件，sdk不处理超时逻辑；返回false表示未消耗事件，在倒计时结束后，sdk会停止试玩
     */
    boolean onNoOpsTimeout(int type, long timeout);

    /**
     * 流信息
     * {"refresh_fps":"30","refresh_ping":“10”,"refresh_bitrate":"10000000"}
     */
    void onSteamInfo(String streamData);
}
