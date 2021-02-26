package kptech.lib.data;

public interface IRequestCallback<T> {
    void onResult(T obj, int code);
}
