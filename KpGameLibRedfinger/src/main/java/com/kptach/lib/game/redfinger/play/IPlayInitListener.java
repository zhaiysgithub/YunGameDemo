package com.kptach.lib.game.redfinger.play;

public interface IPlayInitListener {
    void success();
    void failed(int code, String err);
}
