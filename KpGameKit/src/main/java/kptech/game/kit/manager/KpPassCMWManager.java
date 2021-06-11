package kptech.game.kit.manager;

import com.kptach.lib.inter.game.APIConstants;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kptech.game.kit.PassConstants;
import kptech.game.kit.callback.PassCMWCallback;
import kptech.game.kit.model.PassDeviceResponseBean;
import kptech.game.kit.utils.JsonUtils;
import kptech.game.kit.utils.Logger;
import kptech.lib.constants.Urls;

public class KpPassCMWManager {

    private static final String TAG = "KpPassCMWManager";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    public static final String defaultErrorMsg = "服务异常，请稍后再试";

    private KpPassCMWManager() {
    }

    private static class PassCMWHolder {
        private static final KpPassCMWManager INSTANCE = new KpPassCMWManager();
    }

    public static KpPassCMWManager instance() {
        return PassCMWHolder.INSTANCE;
    }

    public void startRequestPassCMW(final String corpKey, final String pkgName, final PassCMWCallback callback) {
        final String passParams = createPassParams(corpKey, pkgName);
        executeRunnable(passParams,callback);
    }

    private void executeRunnable(final String passParams, final PassCMWCallback callback){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                requestDevicePost(passParams, new PassCMWCallback() {
                    @Override
                    public void onSuccess(PassDeviceResponseBean result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        callback.onError(errorCode, errorMsg);
                    }
                });
            }
        });
    }

    private void requestDevicePost(String jsonStr, PassCMWCallback callback) {
        if (jsonStr == null || jsonStr.isEmpty()) {
            Logger.error(TAG, "jsonStr is null or empty");
            callback.onError(APIConstants.ERROR_CALL_API, defaultErrorMsg);
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

                    PassDeviceResponseBean responseBean = new PassDeviceResponseBean();
                    JSONObject jsonResult = new JSONObject(result);
                    responseBean.code = JsonUtils.optInt(jsonResult, "code");
                    responseBean.msg = JsonUtils.optString(jsonResult, "msg");
                    responseBean.ts = JsonUtils.optLong(jsonResult, "ts");

                    PassDeviceResponseBean.PassData passData = new PassDeviceResponseBean.PassData();
                    if (jsonResult.has("data")){
                        Object dataObject = jsonResult.get("data");
                        JSONObject passDataJson = new JSONObject(dataObject.toString());
                        passData.iaas = JsonUtils.optString(passDataJson, "iaas");
                        passData.deviceid = JsonUtils.optString(passDataJson, "deviceid");
                        passData.devicetype = JsonUtils.optString(passDataJson, "devicetype");
                        passData.devicenum = JsonUtils.optString(passDataJson, "devicenum");
                        passData.direction = JsonUtils.optInt(passDataJson, "direction");
                        passData.resource = passDataJson.get("resource");
                    }
                    responseBean.data = passData;

                    callback.onSuccess(responseBean);
                } else {
                    callback.onError(responseCode, defaultErrorMsg);
                }
            } else {
                callback.onError(responseCode, defaultErrorMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError(APIConstants.ERROR_APPLY_DEVICE, e.getMessage());
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
                callback.onError(APIConstants.ERROR_OTHER, defaultErrorMsg + ";" + e.getMessage());
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


    public int getErrorCode(int applyDeviceCode){
        int apiErrorCode;
        switch (applyDeviceCode){
            case PassConstants.PASS_CODE_ERROR_CORPKEY:
            case PassConstants.PASS_CODE_ERROR_PKGNAME:
            case PassConstants.PASS_CODE_ERROR_AUTH:
            case PassConstants.PASS_CODE_ERROR_APP:
            case PassConstants.PASS_CODE_ERROR_DEVICENO:
                apiErrorCode = APIConstants.ERROR_GAME_INIT;
                break;
            case PassConstants.PASS_CODE_ERROR_DEVICEBUSY:
                apiErrorCode = APIConstants.ERROR_DEVICE_BUSY;
                break;
            case PassConstants.PASS_CODE_ERROR_DEFAULT:
                apiErrorCode = APIConstants.ERROR_OTHER;
                break;
            default:
                apiErrorCode = APIConstants.ERROR_APPLY_DEVICE;
                break;
        }
        return apiErrorCode;
    }
}
