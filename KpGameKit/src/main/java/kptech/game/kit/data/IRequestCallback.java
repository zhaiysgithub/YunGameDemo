package kptech.game.kit.data;

public interface IRequestCallback<T> {
    void onResult(T obj, int code);
}
