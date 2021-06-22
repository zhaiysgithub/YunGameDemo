package com.kptach.lib.game.huawei;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HWTaskHelper {

    private static final int BUFFER_SIZE = 1024;

    private HWTaskHelper() {
    }

    private static class HWTaskHelperHolder {
        private static final HWTaskHelper helper = new HWTaskHelper();
    }

    public static HWTaskHelper instance() {
        return HWTaskHelperHolder.helper;
    }

    public void getAppSoInfo(String corpKey, String sdkVersion, int soVersion, String cpuInfo
            , TaskCallback listener) {

        if (listener == null) {
            return;
        }

        String result = requestAppInfo(corpKey, sdkVersion, soVersion, cpuInfo);
        if (result == null || result.isEmpty()){
            listener.onFaile(-3, "request so info result empty");
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(result);
            int c = jsonObject.getInt("c");
            if (c == 0) {
                JSONObject dObj = jsonObject.getJSONObject("d");
                String md5 = dObj.optString("md5");
                String dataUrl = dObj.optString("data");
//                int version = dObj.optInt("version");
                listener.onSucces(dataUrl, md5);
            } else {
                listener.onFaile(-1, result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onFaile(-2, e.getMessage());
        }

    }

    private String requestAppInfo(String corpKey, String sdkVersion, int soVersion, String cpuInfo) {
        String requestUrl = HWFileUtils.soInfoTestUrl;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        InputStreamReader isr = null;

        try {
            String appendUrl = requestUrl +
                    "?corpkey=" + corpKey +
                    "&cpuInfo=" + cpuInfo +
                    "&version=" + soVersion +
                    "&sdkVersion=" + sdkVersion;

            URL url = new URL(appendUrl);
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("GET");
            postConnection.setConnectTimeout(1000 * 10);
            postConnection.setReadTimeout(1000 * 10);
//            postConnection.setDoInput(true);
            postConnection.setRequestProperty("Content-type", "application/json");


            final StringBuilder buffer = new StringBuilder();
            int code = postConnection.getResponseCode();

            HWCloudGameUtils.info("requestAppInfo:code = " + code);
            if (code == 200) {
                inputStream = postConnection.getInputStream();
                isr = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(isr);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line);
                }
                return buffer.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";

    }

    /**
     * 执行下载zip并解压的流程
     */
    public void startDownloadLibZip(String sUrl, File file, DownloadCallback callback) {
        if (callback == null){
            return;
        }
        if (sUrl == null || sUrl.isEmpty()){
            callback.onFailed("url is empty");
            return;
        }

        if (file == null || !file.exists()){
            callback.onFailed("file == null || !file.exists()");
            return;
        }

        InputStream in = null;
        FileOutputStream out = null;

        try {
            URL url = new URL(sUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
//            conn.setDoOutput(false);
            //超时时间
            conn.setConnectTimeout(10 * 1000);
            conn.setReadTimeout(10 * 1000);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            //防止屏蔽程序抓取而返回403错误
//            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            conn.connect();
            out = new FileOutputStream(file);
            int responseCode = conn.getResponseCode();
            if (responseCode == 200){
                in = conn.getInputStream();
                int byteread;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((byteread = in.read(buffer)) != -1) {
                    out.write(buffer, 0, byteread);
                }
                out.flush();
            }
            callback.onSuccess(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailed(e.getMessage());
        } finally {
            try {
                if (in != null){
                    in.close();
                }
                if (out != null){
                    out.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public interface TaskCallback {

        void onSucces(String libUrl, String md5);

        void onFaile(int code, String errMsg);
    }


    public interface DownloadCallback {

        void onSuccess(String filePath);

        void onFailed(String errorMsg);
    }
}
