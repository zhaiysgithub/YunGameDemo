package kptech.lib.ad;

public interface IAdCallback<T> {
    void onAdCallback(T msg, int code);
}
