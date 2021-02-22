package com.kptach.lib.inter.game;

public interface IGameCallback<T> {
    void onGameCallback(T msg, int code);
}
