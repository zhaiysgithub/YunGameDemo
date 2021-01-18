package kptech.game.kit.redfinger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class KpHttp {

    public static RequestThread createRequestThread(UrlSign swSign, int timeout, OnResponseListener listener) {
        if (listener == null) {
            return null;
        } else {
            UrlSign var3 = new UrlSign();

//            var3.a(var0.getAppListURL(), var0.getAppKey(), var0.getAuthVer(), var0.getAppSecret(), var0.getDesKey(), "{}");
            RequestThread thread = new RequestThread(swSign, timeout, listener);
            thread.start();
            return thread;
        }
    }

    public static void connectDevice(String corpKey, String pkgName, OnResponseListener listener){
        UrlSign sign = new UrlSign();
        RequestThread thread = new RequestThread(sign, 15000, listener);
        thread.start();
    }


    public static Result http(UrlSign urlSign, int timeout) {
        if (urlSign == null) {
            return null;
        } else {
            Result ret = new Result();
            try {
                URL url = new URL(urlSign.getUrl());
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setReadTimeout(timeout);
                conn.setConnectTimeout(timeout);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("accept", "application/json");
                String params = urlSign.getParams();
                if (params != null && !"".equals(params.trim())) {
                    byte[] bytes = params.getBytes();
                    conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
                    OutputStream out = conn.getOutputStream();
                    out.write(bytes);
                    out.flush();
                    out.close();
                }

                int code = conn.getResponseCode();
                if (200 == code) {
                    ret.result = 0;
                    ret.content = readStream(conn.getInputStream());
                } else {
                    ret.result = code;
                    ret.content = readStream(conn.getErrorStream());
                }
            } catch (Exception e) {
                e.printStackTrace();
                ret.result = -100;
                ret.content = e.toString();
            }

            return ret;
        }
    }

    private static String readStream(InputStream in) {
        InputStreamReader inputReader = new InputStreamReader(in);
        BufferedReader bufferReader = new BufferedReader(inputReader);
        StringBuilder builder = new StringBuilder();
        String line = null;

        try {
            while((line = bufferReader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bufferReader = null;

            try {
                inputReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputReader = null;

            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }

    public static class Result {
        public int result = 0;
        public String content = "";

        public Result() {
        }
    }

    public static class UrlSign {

        public String url;
        public String params;

        public String getUrl() {
//            return "https://api.omsys.kuaipantech.com/kp/api/paas/connect";
            return url;
        }

//        public String getParams() {
//            String postParms = "corpKey=2OdIzZFuIkW3Zzu-f7fc6f9e08f98506"+"&pkgName=com.netease.sky"+"&uuid=1585036969212529&channel=BD";
//
//            return postParms;
//        }

        public String getParams(){
            return params;
//            JSONObject obj = new JSONObject();
//            try {
//                obj.put("corpKey", "2OQZnY8bpQ82o4E-62f6341bd2e1e3cf");
//                obj.put("pkgName", "com.netease.dwrg");
//                obj.put("uuid", "1585036969212529");
//                obj.put("channel", "BD");
//                obj.put("protocol", "ws");
//                obj.put("isWebRtc", 0);
//            }catch (Exception supportPlayQueue){
//            }
//            return obj.toString();
        }
    }

    public static class RequestThread extends Thread {
        private final UrlSign urlSign;
        private final OnResponseListener listener;
        private final int timeout;
        private final byte[] lock = new byte[0];
        private boolean isCancel = false;

        public RequestThread(UrlSign var1, int var2, OnResponseListener var3) {
            this.urlSign = var1;
            this.listener = var3;
            this.timeout = var2;
        }

        public void run() {
            Result ret = http(this.urlSign, this.timeout);
            synchronized(this.lock) {
                if (!this.isCancel) {
                    this.listener.onResponse(ret.result, ret.content);
                }

            }
        }

        public void cancel() {
            synchronized(this.lock) {
                this.isCancel = true;
            }
        }
    }

    public interface OnResponseListener {
        void onResponse(int code, String content);
    }

    public interface SWSign {
        String getAppListURL();

        String getConnectURL();

        String getDisconnectURL();

        String getDevicesListURL();

        String getAppKey();

        String getAuthVer();

        String getAppSecret();

        String getDesKey();
    }
}
