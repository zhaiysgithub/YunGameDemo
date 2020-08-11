package kptech.game.kit.ad;

public interface IAdCallback<T> {
    void onCallback(T msg, int code);
}
