package kptech.game.kit.download;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpDownload extends  Thread {

    public static final int DOWNLOAD_MSG_START = 1;
    public static final int DOWNLOAD_MSG_PUASE = 2;
    public static final int DOWNLOAD_MSG_SUCCESS = 3;
    public static final int DOWNLOAD_MSG_FAIL = 4;
    public static final int DOWNLOAD_MSG_PROGRESS = 5;

    private static final int BUFFER_SIZE = 10 * 1024; // 8k ~ 32K
    private boolean mCancel = false;
    private Handler mHandler;
    private Context mContext;

    private String mFilePath;
    private String mUrl;
    private String mFileName;

    private int retryCount = 3;

    public HttpDownload(Context context){
        this.mContext = context;
    }

    public void setFilePath(String filePath){
        this.mFilePath = filePath;
    }

    public void setUrl(String url){
        this.mUrl = url;
        this.mFileName = url.substring(url.lastIndexOf("/") + 1, url.length());
    }

    public void setHandler(Handler handler){
        this.mHandler = handler;
    }

    @Override
    public void run() {
        if (mUrl == null || "".equals(mUrl)){
            if(mHandler!=null){
                String[] obj = new String[]{mFileName, "Url为空"};
                mHandler.sendMessage(Message.obtain(mHandler, DOWNLOAD_MSG_FAIL,obj));
            }
            return;
        }
        if (mFilePath == null || "".equals(mFilePath)) {
            if(mHandler!=null){
                String[] obj = new String[]{mFileName, "FilePath为空"};
                mHandler.sendMessage(Message.obtain(mHandler, DOWNLOAD_MSG_FAIL,obj));
            }
            return;
        }

        if (mHandler!=null){
            String[] obj = new String[]{mFileName};
            mHandler.sendMessage(Message.obtain(mHandler, DOWNLOAD_MSG_START,obj));
        }

        //重试3次
        while (true){

            int ret = download();

            if (ret == -1 && retryCount > 0){
                try {
                    Thread.sleep(2 * 1000);
                }catch (Exception e){
                }

                retryCount--;
                continue;
            }

            if (ret == 0 || retryCount<=0 || mCancel){
                break;
            }

        }

        mCancel = false;
    }

    public void cancel(){
        this.mCancel = true;
    }

    private int download(){
        int ret = 0;

        final String urlStr = mUrl;
        InputStream in = null;
        FileOutputStream out = null;
        try {

            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//            String apkName = urlStr.substring(urlStr.lastIndexOf("/") + 1, urlStr.length());
            File file = new File(mFilePath);
            if (!file.exists()){
                file.createNewFile();
            }
//            File apkFile = new File(dir, apkName);
            //判断文件大小
            long bytesum = 0;
            if (file.exists()) {
                bytesum = file.length();
                // 设置断点续传的开始位置
                urlConnection.setRequestProperty("Range", "bytes=" + bytesum + "-");
            }

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setReadTimeout(10 * 1000);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            urlConnection.connect();
            long bytetotal = urlConnection.getContentLength();
            //获取总大小
            try {
                String s = urlConnection.getHeaderField("Content-Range");
                if (s!=null){
                    String[] strs = s.split("/");
                    int t = Integer.parseInt(strs[1]);
                    if (t > 0){
                        bytetotal = t;
                    }
                }
            }catch (Exception e){
            }

            //保存当前文件的大小
//            ProferencesUtils.setLong(mContext, file.getName(), bytetotal);

            if (bytesum < bytetotal){

                int byteread = 0;
                in = urlConnection.getInputStream();

                out = new FileOutputStream(file,true);
                byte[] buffer = new byte[BUFFER_SIZE];

                int oldProgress = 0;

                while ((byteread = in.read(buffer)) != -1) {
                    bytesum += byteread;
                    out.write(buffer, 0, byteread);

                    int progress = (int) (bytesum * 1000 / bytetotal);
                    // 如果进度与之前进度相等，则不更新，如果更新太频繁，否则会造成界面卡顿
                    if (progress != oldProgress) {
                        //updateProgress(bytetotal,bytesum);
                        Log.i("HttpDownload: ",Thread.currentThread().getName());
                        if (mHandler!=null){
                            long[] arr = new long[2];
                            arr[0] = bytetotal;
                            arr[1] = bytesum;
                            mHandler.sendMessage(Message.obtain(mHandler, DOWNLOAD_MSG_PROGRESS, arr));
                        }
                    }
                    oldProgress = progress;

                    if (mCancel) {
                        if (mHandler!=null){
                            String[] obj = new String[]{mFileName};
                            mHandler.sendMessage(Message.obtain(mHandler, DOWNLOAD_MSG_PUASE,obj));
                        }
                        break;
                    }
                }
            }

            if (bytesum >= bytetotal){
                // 下载完成
                if (mHandler!=null){
                    String[] obj = new String[]{mFileName, file.getPath()};
                    mHandler.sendMessage(Message.obtain(mHandler, DOWNLOAD_MSG_SUCCESS,obj));
                }
            }

        } catch (Exception e) {
            Log.e("DownloadTask", e.getMessage());
            if (retryCount == 0){
                if(mHandler!=null){
                    String[] obj = new String[]{mFileName, e.getMessage()};
                    mHandler.sendMessage(Message.obtain(mHandler, DOWNLOAD_MSG_FAIL,obj));
                }
            }
            ret = -1;

        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {

                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {

                }
            }

        }

        return ret;
    }
}