package kptech.game.kit.download;

import android.os.Handler;
import android.os.Message;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;

import kptech.game.kit.utils.Logger;

public class XutilDownload {
    private static final String TAG = "XutilDownload";

    private String mFilePath;
    private String mUrl;
    private Handler mHandler;
    private Callback.Cancelable cancelable;
    private int mRetryCount = 3;
    private boolean cancel = false;
    private String mFileName;

    public void cancel(){
        if (cancelable!=null){
            cancelable.cancel();
        }
        cancel = true;
    }

    public void setFilePath(String filePath, String fileName){
        this.mFilePath = filePath;
        this.mFileName = fileName;
    }

    public void setUrl(String url){
        this.mUrl = url;
//        this.mFileName = url.substring(url.lastIndexOf("/") + 1, url.length());
    }

    public void setHandler(Handler handler){
        this.mHandler = handler;
    }

    public void start() {
        this.download();
    }

    /**
     * 下载文件
     */
    private void download() {
        if (cancel) {
            return;
        }


        RequestParams requestParams = new RequestParams(mUrl);

        requestParams.setSaveFilePath(mFilePath);
        /**自动为文件命名**/
        requestParams.setAutoRename(false);
        /**自动为文件断点续传**/
        requestParams.setAutoResume(true);

        cancelable = x.http().get(requestParams, new Callback.ProgressCallback<File>() {
            @Override
            public void onSuccess(File result) {
                Logger.info(TAG, "下载完成");
                Logger.info(TAG, "result=" + result.getPath());

                // 下载完成
                if (mHandler!=null){
                    String[] obj = new String[]{mFileName, result.getPath()};
                    mHandler.sendMessage(Message.obtain(mHandler, HttpDownload.DOWNLOAD_MSG_SUCCESS, obj));
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Logger.error(TAG,  ex.getMessage());
                if (mRetryCount <= 0){
                    if(mHandler!=null){
                        String[] obj = new String[]{mFileName, ex.getMessage()};
                        mHandler.sendMessage(Message.obtain(mHandler, HttpDownload.DOWNLOAD_MSG_FAIL,obj));
                    }
                }else {
                    retryHandler.sendMessageDelayed(Message.obtain(retryHandler,1), 2000);
                }
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Logger.info(TAG, "下载已取消");
                if (mHandler!=null){
                    String[] obj = new String[]{mFileName};
                    mHandler.sendMessage(Message.obtain(mHandler, HttpDownload.DOWNLOAD_MSG_PUASE, obj));
                }
            }

            @Override
            public void onFinished() {

            }

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                if (mRetryCount == 3){
                    if (mHandler!=null){
                        String[] obj = new String[]{mFileName};
                        mHandler.sendMessage(Message.obtain(mHandler, HttpDownload.DOWNLOAD_MSG_START, obj));
                    }
                }
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                Logger.info(TAG, "total=" + total + "--" + "current=" + current);

                if (mHandler!=null){
                    long[] arr = new long[2];
                    arr[0] = total;
                    arr[1] = current;
                    mHandler.sendMessage(Message.obtain(mHandler, HttpDownload.DOWNLOAD_MSG_PROGRESS, arr));
                }
            }
        });
    }

    Handler retryHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    mRetryCount--;
                    download();
                    break;
            }
        }
    };

}
