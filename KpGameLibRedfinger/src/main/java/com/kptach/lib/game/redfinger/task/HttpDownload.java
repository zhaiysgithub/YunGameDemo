package com.kptach.lib.game.redfinger.task;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.kptach.lib.game.redfinger.utils.Logger;

public class HttpDownload extends Thread {

    private static final String TAG = HttpDownload.class.getSimpleName();

    public interface ICallback{
        void onSuccess(String file);
        void onFailed();
    }

    private static final int BUFFER_SIZE = 4096; // 8k ~ 32K

    private String mFilePath;
    private String mUrl;

    private ICallback mCallback;

    public void setCallback(ICallback callback) {
        this.mCallback = callback;
    }

    public HttpDownload(String url, String filePath){
        this.mUrl = url;
        this.mFilePath = filePath;
    }

    @Override
    public void run() {
        int ret = download();
        if (ret == 1){
            if (mCallback != null){
                mCallback.onSuccess(this.mFilePath);
            }
        }else {
            if (mCallback != null){
                mCallback.onFailed();
            }
        }
    }

    private int download(){
        int ret = 0;
        final String urlStr = mUrl;
        InputStream in = null;
        FileOutputStream out = null;
        try {
            Logger.info(TAG, "url: " + urlStr);

            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            File file = new File(mFilePath);
            if (!file.exists()){
                file.createNewFile();
            }

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setReadTimeout(10 * 1000);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            urlConnection.connect();

            in = urlConnection.getInputStream();
            out = new FileOutputStream(file,false);

            int byteread = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((byteread = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteread);
            }
            ret = 1;
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
            ret = -1;
        } finally {
            close(out);
            close(in);
        }
        return ret;
    }

    public static void close(Closeable obj) {
        if (obj != null) {
            try {
                obj.close();
            } catch (IOException e) {
                Logger.error("StreamUtil", "io流关闭异常 ");
            }
        }
    }
}