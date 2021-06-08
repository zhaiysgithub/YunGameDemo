package com.kptach.lib.game.bdsdk.play;

public interface IVideoListener {
    void onResolutionChange(int i, int i2);
    void onEncodeChange(int i);
    void onFPSChange(int i);
    void onBitrateChange(int i);
    void onQualityChange(int i);
    void onMaxIdrChange(int i);
}
