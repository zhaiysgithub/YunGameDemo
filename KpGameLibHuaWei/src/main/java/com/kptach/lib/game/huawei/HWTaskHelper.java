package com.kptach.lib.game.huawei;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HWTaskHelper {

    private static final int BUFFER_SIZE = 4096; // 8k ~ 32K

    private HWTaskHelper() {
    }

    private static class HWTaskHelperHolder {
        private static final HWTaskHelper helper = new HWTaskHelper();
    }

    public static HWTaskHelper instance() {
        return HWTaskHelperHolder.helper;
    }

    public void getAppSoInfo(String corpKey, String sdkVersion, String cpuInfo
            , TaskCallback listener) {

        if (listener == null) {
            return;
        }

        String result = requestAppInfo(corpKey, sdkVersion, cpuInfo);
        int code;
        try {
            JSONObject jsonObject = new JSONObject(result);
            int c = jsonObject.getInt("c");
            if (c == 0) {
                JSONObject dObj = jsonObject.getJSONObject("d");
                String md5 = dObj.optString("md5");
                String url = dObj.optString("libUrl");
                String soVersion = dObj.optString("soVersion");

                listener.onSucces(url, md5, soVersion);
            } else {
                String errorMsg = jsonObject.getString("m");
                code = -1;
                listener.onFaile(code, errorMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            code = -2;
            String errorMsg = e.getMessage();
            listener.onFaile(code, errorMsg);
        }

    }

    private String requestAppInfo(String corpKey, String sdkVersion, String cpuInfo) {
        String requestUrl = HWLoadLibHelper.checkVerionUrl;
        HWCloudGameUtils.info("requestAppInfo:" + requestUrl);
        OutputStream outputStream = null;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        InputStreamReader isr = null;

        try {
            String appendUrl = requestUrl +
                    "?corpKey=" + corpKey +
                    "&version=" + sdkVersion;

            URL url = new URL(appendUrl);
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("POST");
            postConnection.setConnectTimeout(1000 * 10);
            postConnection.setReadTimeout(1000 * 10);
            postConnection.setDoInput(true);
            postConnection.setRequestProperty("Content-type", "application/json");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("cpuInfo",cpuInfo);
            String dataStr = jsonObject.toString();
            outputStream = postConnection.getOutputStream();
            outputStream.write(dataStr.getBytes());
            outputStream.flush();

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
                if (outputStream != null) {
                    outputStream.close();
                }
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
     * 执行下载流程
     */
    public void startDownloadLibZip(String sUrl, File file, DownloadCallback callback) {
        if (callback == null){
            return;
        }
        if (sUrl == null || sUrl.isEmpty()){
            callback.onSuccess("url is empty");
            return;
        }

        if (file == null || !file.exists()){
            callback.onSuccess("file == null || !file.exists()");
            return;
        }

        InputStream in = null;
        FileOutputStream out = null;

        try {
            URL url = new URL(sUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setDoOutput(false);
            //超时时间
            conn.setConnectTimeout(10 * 1000);
            conn.setReadTimeout(10 * 1000);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            //防止屏蔽程序抓取而返回403错误
//            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            conn.connect();

            in = conn.getInputStream();
            out = new FileOutputStream(file,false);

            int byteread;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((byteread = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteread);
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

        void onSucces(String libUrl, String md5, String soVersion);

        void onFaile(int code, String errMsg);
    }


    public interface DownloadCallback {

        void onSuccess(String filePath);

        void onFailed(String errorMsg);
    }
}
