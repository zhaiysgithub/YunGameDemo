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

    /**
     * 更新下载状态
     */
    void updateDownloadStatus(int status,String url);

    /**
     * 更新下载进度
     */
    void updateDownloadProgress(long total, long current, String url);
}
