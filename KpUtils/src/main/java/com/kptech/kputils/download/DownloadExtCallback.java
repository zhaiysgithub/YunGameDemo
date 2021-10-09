package com.kptech.kputils.download;

import java.io.File;

public interface DownloadExtCallback {

    /**
     * 开始下载
     */
    void onStarted(String url);

    /**
     * 暂停下载
     */
    void onPaused(String url);

    /**
     * 下载进度回调
     */
    void onProgress(long total, long current, String url);

    /**
     * 下载完成
     */
    void onSuccess(File result, String url);

    /**
     * 下载错误
     */
    void onError(String error, String url);

    /**
     * 下载取消
     */
    void onCancelled(String msg, String url);
}
