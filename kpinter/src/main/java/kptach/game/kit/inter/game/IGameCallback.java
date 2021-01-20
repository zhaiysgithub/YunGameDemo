package kptach.game.kit.inter.game;

public interface IGameCallback<T> {
    void onGameCallback(T msg, int code);
}
