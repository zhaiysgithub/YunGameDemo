package com.kptach.lib.inter.game;

public interface IPlayScreenListener {

    /**
     * 分辨率
     */
    void onVideoSizeChange(int width, int height);

    /**
     * 屏幕方向
     */
    void onOrientationChange (int orientation);
}
