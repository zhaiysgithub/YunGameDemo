package com.kptach.lib.inter.game;

public interface IPlayDataListener {

    /**
     * 网络延时反馈
     * @param ping 网络延时，单位ms
     */
    void onPingUpdate(int ping);

    /**
     * 无操作超时回调
     * @param type 类型。1为后台，2为前台
     * @param timeout 超时时长，单位s
     * @return 返回true表示消耗了事件，sdk不处理超时逻辑；返回false表示未消耗事件，在倒计时结束后，sdk会停止试玩
     */
    boolean onNoOpsTimeout(int type, long timeout);

    /**
     * 流信息
     * @param fps  帧率
     * @param bitrate  码率
     */
    void onSteamInfo(int fps, long bitrate);
}
