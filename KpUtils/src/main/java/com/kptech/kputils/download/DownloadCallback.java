package com.kptech.kputils.download;

import com.kptech.kputils.common.Callback;
import com.kptech.kputils.common.util.LogUtil;
import com.kptech.kputils.ex.DbException;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by wyouflf on 15/11/10.
 */
/*package*/ class DownloadCallback implements
        Callback.CommonCallback<File>,
        Callback.ProgressCallback<File>,
        Callback.Cancelable {

    private DownloadInfo downloadInfo;
    private String downloadUrl;
    private WeakReference<DownloadViewHolder> viewHolderRef;
    private DownloadManager downloadManager;
    private boolean cancelled = false;
    private Cancelable cancelable;
    private DownloadExtCallback mExtCallback;

    public DownloadCallback(DownloadViewHolder viewHolder, DownloadExtCallback callback) {
        this.switchViewHolder(viewHolder);
        this.mExtCallback = callback;
    }

    public boolean switchViewHolder(DownloadViewHolder viewHolder) {
        if (viewHolder == null) return false;

        synchronized (DownloadCallback.class) {
            if (downloadInfo != null) {
                if (this.isStopped()) {
                    return false;
                }
            }
            this.downloadInfo = viewHolder.getDownloadInfo();
            this.viewHolderRef = new WeakReference<>(viewHolder);
            if(downloadInfo != null){
                downloadUrl = downloadInfo.getUrl();
            }
        }
        return true;
    }

    public void setDownloadManager(DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
    }

    public void setCancelable(Cancelable cancelable) {
        this.cancelable = cancelable;
    }

    private DownloadViewHolder getViewHolder() {
        if (viewHolderRef == null) return null;
        DownloadViewHolder viewHolder = viewHolderRef.get();
        if (viewHolder != null) {
            DownloadInfo downloadInfo = viewHolder.getDownloadInfo();
            if (this.downloadInfo != null && this.downloadInfo.equals(downloadInfo)) {
                return viewHolder;
            }
        }
        return null;
    }

    @Override
    public void onWaiting() {
        try {
            downloadInfo.setState(DownloadState.WAITING);
            downloadManager.updateDownloadInfo(downloadInfo);
        } catch (DbException ex) {
            LogUtil.e(ex.getMessage(), ex);
        }
        DownloadViewHolder viewHolder = this.getViewHolder();
        if (viewHolder != null) {
            viewHolder.onWaiting();
        }
    }

    @Override
    public void onStarted() {
        try {
            downloadInfo.setState(DownloadState.STARTED);
            downloadManager.updateDownloadInfo(downloadInfo);
            if (mExtCallback != null){
                mExtCallback.onStarted(downloadUrl);
            }
        } catch (DbException ex) {
            LogUtil.e(ex.getMessage(), ex);
        }
        DownloadViewHolder viewHolder = this.getViewHolder();
        if (viewHolder != null) {
            viewHolder.onStarted();
        }
    }

    @Override
    public void onLoading(long total, long current, boolean isDownloading) {
        if (isDownloading) {
            try {
                downloadInfo.setState(DownloadState.STARTED);
                if (total > 0) {
                    downloadInfo.setFileLength(total);
                    downloadInfo.setProgress((int) (current * 100 / total));
                }
                downloadManager.updateDownloadInfo(downloadInfo);
                if (mExtCallback != null){
                    mExtCallback.onProgress(total,current,downloadUrl);
                }
            } catch (DbException ex) {
                LogUtil.e(ex.getMessage(), ex);
            }
            DownloadViewHolder viewHolder = this.getViewHolder();
            if (viewHolder != null) {
                viewHolder.onLoading(total, current);
            }
        }else {
            if (mExtCallback != null){
                mExtCallback.onPaused(downloadUrl);
            }
        }
    }

    @Override
    public void onSuccess(File result) {
        synchronized (DownloadCallback.class) {
            try {
                downloadInfo.setState(DownloadState.FINISHED);
                downloadManager.updateDownloadInfo(downloadInfo);
                if (mExtCallback != null){
                    mExtCallback.onSuccess(result,downloadUrl);
                }
            } catch (DbException ex) {
                LogUtil.e(ex.getMessage(), ex);
            }
            DownloadViewHolder viewHolder = this.getViewHolder();
            if (viewHolder != null) {
                viewHolder.onSuccess(result);
            }
        }
    }

    @Override
    public void onError(Throwable ex, boolean isOnCallback) {
        synchronized (DownloadCallback.class) {
            try {
                downloadInfo.setState(DownloadState.ERROR);
                downloadManager.updateDownloadInfo(downloadInfo);
                if (mExtCallback != null){
                    mExtCallback.onError(ex != null ? ex.getMessage() : "" , downloadUrl);
                }
            } catch (DbException e) {
                LogUtil.e(e.getMessage(), e);
            }
            DownloadViewHolder viewHolder = this.getViewHolder();
            if (viewHolder != null) {
                viewHolder.onError(ex, isOnCallback);
            }
        }
    }

    @Override
    public void onCancelled(CancelledException cex) {
        synchronized (DownloadCallback.class) {
            try {
                downloadInfo.setState(DownloadState.STOPPED);
                downloadManager.updateDownloadInfo(downloadInfo);
                if (mExtCallback != null){
                    mExtCallback.onCancelled(cex != null ? cex.getMessage() : "" , downloadUrl);
                }
            } catch (DbException ex) {
                LogUtil.e(ex.getMessage(), ex);
            }
            DownloadViewHolder viewHolder = this.getViewHolder();
            if (viewHolder != null) {
                viewHolder.onCancelled(cex);
            }
        }
    }

    @Override
    public void onFinished() {
        cancelled = false;
    }

    private boolean isStopped() {
        DownloadState state = downloadInfo.getState();
        return isCancelled() || state.value() > DownloadState.STARTED.value();
    }

    @Override
    public void cancel() {
        cancelled = true;
        if (cancelable != null) {
            cancelable.cancel();
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
