package kptech.game.kit;

public interface APICallback<T> {
    void onAPICallback(T msg, int code);
}
