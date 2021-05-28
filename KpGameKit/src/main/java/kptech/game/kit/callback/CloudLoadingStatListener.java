package kptech.game.kit.callback;

public interface CloudLoadingStatListener {

    /**
     * 进入云游戏前加载的回调
     * @param stateCode 状态码
     * @param progress  进度
     * @param msg   提示信息
     */
    void onLoadingStat(int stateCode, int progress, String msg);
}
