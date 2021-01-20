package kptach.game.kit.lib.redfinger.play;

public interface IPlayInitListener {
    void success();
    void failed(int code, String err);
}
