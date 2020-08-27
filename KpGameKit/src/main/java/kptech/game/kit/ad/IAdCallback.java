package kptech.game.kit.ad;

public interface IAdCallback<T> {
    void onAdCallback(T msg, int code);
}
