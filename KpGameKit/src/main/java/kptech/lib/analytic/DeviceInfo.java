package kptech.lib.analytic;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import kptech.lib.constants.SharedKeys;
import kptech.lib.constants.Urls;
import kptech.game.kit.utils.DeviceIdUtil;
import kptech.game.kit.utils.DeviceUtils;
import kptech.game.kit.utils.ProferencesUtils;
import kptech.game.kit.utils.StringUtil;

public class DeviceInfo {

    private static String mDeviceId = null;
    public static boolean hasDeviceId(Context context){
        String dev = null;
        try {
            dev = ProferencesUtils.getString(context, SharedKeys.KEY_EVENT_DEVICE_ID, null);
        }catch (Exception e){
        }
        return dev != null;
    }

    public static String getUserId(Context context){
        try {
            String userId = ProferencesUtils.getString(context, SharedKeys.KEY_EVENT_USERID, null);
            if (userId == null || "".equals(userId)){
                String uniqueID = UUID.randomUUID().toString();
                userId = StringUtil.getMD5(uniqueID);
                ProferencesUtils.setString(context, SharedKeys.KEY_EVENT_USERID, userId);
            }
            return userId;
        }catch (Exception e){
        }
        return "";
    }

    public static String getDeviceId(Context context){
        try {
            if (mDeviceId == null){
                mDeviceId = DeviceIdUtil.getDeviceId(context);
            }
        }catch (Exception e){}
        return mDeviceId;
    }

    public static Map<String,Object> getDeviceHardInfo(Context context){
        String imei = DeviceUtils.getIMEI(context);
        String androidId = DeviceUtils.getAndroidId(context);
        String phoneBrand = DeviceUtils.getDeviceBrand();
        String phoneType = DeviceUtils.getDeviceModel();
        String screenSize = DeviceUtils.getPhysicsScreenSize(context)+"";

        HashMap<String,Object> map = new HashMap<>();
        map.put("imei", imei);
        map.put("sysid", androidId);
        map.put("phonebrand", phoneBrand);
        map.put("phonetype", phoneType);
        map.put("screensize", screenSize);

        return map;
    }

    public static void sendDeviceInfo(final Context context, String corpKey) {
        final String deviceId = DeviceIdUtil.getDeviceId(context);
        String userId = getUserId(context);
        String imei = DeviceUtils.getIMEI(context);
        String androidId = DeviceUtils.getAndroidId(context);
        String phoneBrand = DeviceUtils.getDeviceBrand();
        String phoneType = DeviceUtils.getDeviceModel();
        String phoneOS = "android";
        String screenSize = DeviceUtils.getPhysicsScreenSize(context)+"";
        String systemVer = DeviceUtils.getBuildVersion();
        String systemLevel = DeviceUtils.getBuildLevel()+"";

        StringBuilder sb = new StringBuilder();
        try {
            sb.append("clientid=" + corpKey);
            sb.append("&userid=" + userId);
            sb.append("&deviceid=" + deviceId);
            sb.append("&imei=" +  imei);
            sb.append("&sysid=" + androidId);
            sb.append("&phone=" + phoneOS);
            sb.append("&phonebrand=" + phoneBrand);
            sb.append("&phonetype=" + phoneType);
            sb.append("&screensize=" + screenSize);
            sb.append("&sysversion=" + systemVer);
            sb.append("&syslevel=" + systemLevel);
        }catch (Exception e){
        }

        new AsyncTask<String,Void,Void>(){
            @Override
            protected Void doInBackground(String... params) {
                String ret = request(params[0]);
                try {
                    if (ret!=null){
                        JSONObject jsonObject = new JSONObject(ret);
                        int c = jsonObject.getInt("c");
                        if (c == 200){
                            //保存数据
                            ProferencesUtils.setString(context, SharedKeys.KEY_EVENT_DEVICE_ID, deviceId);
                        }
                    }
                }catch (Exception e){
                }
                return null;
            }
        }.execute(sb.toString());

    }

    //post请求
    private static String request(String params) {

        String str = Urls.EVENT_RUL + "/appuserlog.php";
        try {
            URL url = new URL(str);
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("POST");//post 请求
            postConnection.setConnectTimeout(1000*5);
            postConnection.setReadTimeout(1000*5);
            postConnection.setDoInput(true);//允许从服务端读取数据
            postConnection.setDoOutput(true);//允许写入

            OutputStream outputStream = postConnection.getOutputStream();
            outputStream.write(params.getBytes());//把参数发送过去.
            outputStream.flush();

            final StringBuffer buffer = new StringBuffer();
            int code = postConnection.getResponseCode();
            if (code == 200) {//成功
                InputStream inputStream = postConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;//一行一行的读取
                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line);//把一行数据拼接到buffer里
                }
                return buffer.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }



}
