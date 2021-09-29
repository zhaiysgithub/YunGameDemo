package kptech.game.kit.callback;

public interface IGameObservable {

    /**
     * 返回回调
     */
    void onBackListener(boolean isExit);

    /**
     * 重新加载回调
     */
    void onReloadListener();

    /**
     * 下载回调
     */
    void onDownloadListener();

    /**
     * 复制文本回调
     */
    void onCopyInfoListener(String info);

    /**
     * 授权回调
     */
    void onAuthListener(boolean isAuthPass);
}
