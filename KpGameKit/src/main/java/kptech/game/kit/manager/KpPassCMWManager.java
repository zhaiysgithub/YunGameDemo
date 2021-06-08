package kptech.game.kit.manager;

import android.app.Application;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kptech.game.kit.PassConstants;
import kptech.game.kit.callback.PassCMWCallback;
import kptech.game.kit.model.PassDeviceResponseBean;
import kptech.game.kit.utils.JsonUtils;
import kptech.game.kit.utils.Logger;
import kptech.lib.analytic.DeviceInfo;
import kptech.lib.constants.Urls;

public class KpPassCMWManager {

    private static final String TAG = "KpPassCMWManager";
    private static final int requestMaxCount = 3;
    private int requestPassCount = 0;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    public static final String defaultErrorMsg = "服务异常，请稍后再试";

    private KpPassCMWManager() {
    }

    private static class PassCMWHolder {
        private static final KpPassCMWManager INSTANCE = new KpPassCMWManager();
    }

    public static KpPassCMWManager instance() {
        return PassCMWHolder.INSTANCE;
    }

    public void startRequestPassCMW(final Application context, final String corpKey, final String pkgName, final PassCMWCallback callback) {

        requestPassCount++;
        final String passParams = createPassParams(corpKey, pkgName);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                requestDevicePost(passParams, new PassCMWCallback() {
                    @Override
                    public void onSuccess(PassDeviceResponseBean result) {
                        requestPassCount = 0;
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        if (errorCode != -2 && requestPassCount <= requestMaxCount) {
                            startRequestPassCMW(context, corpKey, pkgName, callback);
                        } else {
                            requestPassCount = 0;
                            callback.onError(errorCode, errorMsg);
                        }
                    }
                });
            }
        });
    }

    private void requestDevicePost(String jsonStr, PassCMWCallback callback) {
        if (jsonStr == null || jsonStr.isEmpty()) {
            Logger.error(TAG, "jsonStr is null or empty");
            callback.onError(-2, defaultErrorMsg);
            return;
        }
        String url = Urls.getRequestDeviceUrl(Urls.URL_REQUEST_DEVICE);
        BufferedReader reader = null;
        OutputStream writeStream = null;
        InputStreamReader isr = null;
        try {
            URL connURL = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) connURL.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("accept", "application/json");
            byte[] writeBytes = jsonStr.getBytes();
            conn.setRequestProperty("Content-Length", String.valueOf(writeBytes.length));
            writeStream = conn.getOutputStream();
            writeStream.write(writeBytes);
            writeStream.flush();

            int responseCode = conn.getResponseCode();
            Logger.info(TAG, "conn:" + responseCode);
            if (responseCode == 200) {
                isr = new InputStreamReader(conn.getInputStream());
                reader = new BufferedReader(isr);
                String result = reader.readLine();
                Logger.info(TAG, "result=" + result);

                if (result != null && !result.isEmpty()) {
                    PassDeviceResponseBean responseBean = passJsonStrToBean(result);
                    callback.onSuccess(responseBean);
                } else {
                    callback.onError(responseCode, defaultErrorMsg);
                }
            } else {
                callback.onError(responseCode, defaultErrorMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError(-1, defaultErrorMsg);
        } finally {
            try {
                if (writeStream != null) {
                    writeStream.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError(-1, defaultErrorMsg);
            }
        }

    }


    private String createPassParams(String corpKey, String pkgName) {

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("corpKey", corpKey);
            jsonObject.put("pkgName", pkgName);
            jsonObject.put("clntType", "ANDR");
            jsonObject.put("protocol", "ws");//  wss -- 加密方式 ws -- 非加密方式
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            jsonObject.put("uuid", uuid);
            jsonObject.put("ts", System.currentTimeMillis());
            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private PassDeviceResponseBean passJsonStrToBean(String jsonStr) {
        PassDeviceResponseBean responseBean = new PassDeviceResponseBean();
        try {
            JSONObject jsonResult = new JSONObject(jsonStr);
            responseBean.code = JsonUtils.optInt(jsonResult, "code");
            responseBean.msg = JsonUtils.optString(jsonResult, "msg");
            responseBean.ts = JsonUtils.optLong(jsonResult, "ts");
            JSONObject passDataJson = JsonUtils.optObject(jsonResult, "data");

            PassDeviceResponseBean.PassData passData = new PassDeviceResponseBean.PassData();
            if (passDataJson != null) {
                passData.iaas = JsonUtils.optString(passDataJson, "iaas");
                passData.deviceid = JsonUtils.optString(passDataJson, "deviceid");
                passData.devicetype = JsonUtils.optString(passDataJson, "devicetype");
                passData.devicenum = JsonUtils.optString(passDataJson, "devicenum");
                JSONObject resourceJson = JsonUtils.optObject(passDataJson, "resource");
                if (resourceJson != null){
                    passData.resource = resourceJson.toString();
                } else {
                    passData.resource = "";
                }


                /*PassDeviceResponseBean.DeviceResource deviceResource = new PassDeviceResponseBean.DeviceResource();
                if (resourceJson != null) {
                    deviceResource.deviceNum = JsonUtils.optString(resourceJson, "deviceNum");
                    deviceResource.phoneIp = JsonUtils.optString(resourceJson, "phoneIp");
                    deviceResource.domain = JsonUtils.optString(resourceJson, "domain");
                    deviceResource.serverIp = JsonUtils.optString(resourceJson, "serverIp");
                    deviceResource.accessPort = JsonUtils.optString(resourceJson, "accessPort");
                    deviceResource.publicIp = JsonUtils.optString(resourceJson, "publicIp");
                    deviceResource.sessionId = JsonUtils.optString(resourceJson, "sessionId");
                    deviceResource.listenPort = JsonUtils.optString(resourceJson, "listenPort");
                }*/
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return responseBean;
    }

    public String getErrorText(int code){
        String errorText = "";
        switch (code){
            case PassConstants.PASS_CODE_ERROR_CORPKEY:
            case PassConstants.PASS_CODE_ERROR_AUTH:
                errorText = "初始化游戏失败";
                break;
            case PassConstants.PASS_CODE_ERROR_PKGNAME:
            case PassConstants.PASS_CODE_ERROR_APP:
                errorText = "未获取到游戏信息";
                break;
            case PassConstants.PASS_CODE_ERROR_DEVICEBUSY:
                errorText = "试玩人数过多，请稍后再试";
                break;
            case PassConstants.PASS_CODE_ERROR_DEFAULT:
                errorText = "操作失败,请联系管理员";
                break;
            default:
                errorText = defaultErrorMsg;
                break;
        }

        return errorText;
    }

}
