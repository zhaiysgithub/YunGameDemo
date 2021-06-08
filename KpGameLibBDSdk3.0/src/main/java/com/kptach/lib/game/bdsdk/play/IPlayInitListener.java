package com.kptach.lib.game.bdsdk.play;

public interface IPlayInitListener {
    void success();
    void failed(int code, String err);
}
