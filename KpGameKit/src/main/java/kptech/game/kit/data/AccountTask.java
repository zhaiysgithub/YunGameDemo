package kptech.game.kit.data;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import kptech.game.kit.analytic.DeviceInfo;
import kptech.game.kit.constants.Urls;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.MD5Util;

public class AccountTask extends AsyncTask<Object, Void, Map<String,Object>> {

    private static final String TAG = "AccountTask" ;

    public interface ICallback{
        void onResult(Map<String, Object> map);
    }

    public static final String ACTION_LOGIN_PHONE = "1";
    public static final String ACTION_LOGIN_PASSWORD = "2";
    public static final String ACTION_REGIST = "3";
    public static final String ACTION_UPDATE_PWD = "4";
    public static final String ACTION_SENDSMS = "5";
    public static final String ACTION_CHECKLOGIN = "6";
    public static final String ACTION_PAY_ORDER = "7";
    public static final String ACTION_LOGIN_CHANNEL_UUID = "8";
    public static final String ACTION_AUTH_CHANNEL_UUID = "9";

    public static final String SENDSMS_TYPE_PHONELOGIN = "4";
    public static final String SENDSMS_TYPE_REGIST = "1";
    public static final String SENDSMS_TYPE_UPDATEPSW = "2";

    private String mAction;
    private String mCorpKey;
    private ICallback mCallback;
    private String mDeviceId;

    private Context mContext;
    public AccountTask(Context context, String action){
        this.mContext = context;
        this.mAction = action;
        mDeviceId = DeviceInfo.getDeviceId(context);
    }

    public AccountTask setCallback(ICallback callback) {
        this.mCallback = callback;
        return this;
    }

    public AccountTask setCorpKey(String mCorpKey) {
        this.mCorpKey = mCorpKey;
        return this;
    }

    @Override
    protected Map doInBackground(Object... params) {
        Map<String, Object> ret = null;
        if (mAction == ACTION_LOGIN_PASSWORD){
            ret = doLoginPwd(params);
        }else if (mAction == ACTION_LOGIN_PHONE){
            ret = doLoginPhone(params);
        }else if (mAction == ACTION_REGIST){
            ret = doRegist(params);
        }else if (mAction == ACTION_UPDATE_PWD){
            ret = doUpdatePwd(params);
        }else if (mAction == ACTION_SENDSMS){
            ret = doSendSms(params);
        }else if (mAction == ACTION_PAY_ORDER){
            ret = doPayOrder(params);
        }else if (mAction == ACTION_LOGIN_CHANNEL_UUID){
            ret = doLoginChannel(params);
        }else if (mAction == ACTION_AUTH_CHANNEL_UUID){
            ret = doAuthChannelUser(params);
        }
        return ret;
    }

    @Override
    protected void onPostExecute(Map<String, Object> map) {
        if (mCallback != null){
            mCallback.onResult(map);
        }
    }

    private Map<String, Object> doLoginPwd(Object... params){
        Map<String,Object> p = new HashMap<>();
        p.put("func", "login");
        p.put("phone", params[0]);
        p.put("password", params[1]);
        p.put("deviceid", mDeviceId);
        return request(p);
    }

    private Map<String, Object> doLoginPhone(Object... params){
        Map<String,Object> p = new HashMap<>();
        p.put("func", "smscodelogin" );
        p.put("phone", params[0]);
        p.put("smsCode", params[1]);
        p.put("smsCodeId", params[2]);
        p.put("deviceid", mDeviceId);
        return request(p);
    }

    private Map<String, Object> doLoginChannel(Object... params){
        Map<String,Object> p = new HashMap<>();
        p.put("func", "thirdpartlogin");
        p.put("usersign", params[0]);    //用户id
        p.put("deviceid", mDeviceId);
        p.put("corpkey", mCorpKey);    //用户id
        return request(p);
    }

    private Map<String,Object> doSendSms(Object... params){
        Map<String,Object> p = new HashMap<>();
        p.put("func", "sendsms");
        p.put("phone", params[0]);
        p.put("action", params[1]);
        return request(p);
    }

    private Map<String,Object> doRegist(Object... params){
        Map<String,Object> p = new HashMap<>();
        p.put("func", "register");
        p.put("phone", params[0]);
        p.put("smsCode", params[1]);
        p.put("smsCodeId", params[2]);
        p.put("password", params[3]);
        p.put("deviceid", mDeviceId);
        return request(p);
    }

    private Map<String,Object> doUpdatePwd(Object... params){
        Map<String,Object> p = new HashMap<>();
        p.put("func", "forgetpassword");
        p.put("phone", params[0]);
        p.put("smsCode", params[1]);
        p.put("smsCodeId", params[2]);
        p.put("password", params[3]);
        return request(p);
    }

    private Map<String,Object> doPayOrder(Object... params){
        Map<String,Object> p = new HashMap<>();
        p.put("func", "maketrade");
        p.put("guid", params[0]);    //用户id
        p.put("paytype", params[1]);    //用户id
        p.put("cpinfo", params[2]);

        p.put("offcode", "nooff");
        p.put("gameid", params[3]);
        p.put("gamepackage", params[4]);
        p.put("gamename","");
        p.put("clientid", mCorpKey);

        return request(p);
    }

    private Map<String,Object> doAuthChannelUser(Object... params){
        Map<String,Object> p = new HashMap<>();
        p.put("func", "appuserauth");
        p.put("corpkey", mCorpKey);
        p.put("deviceid", mDeviceId);
        p.put("usersign", params[0]);
        p.put("package", params[1]);
        p.put("ext", DeviceInfo.getDeviceHardInfo(mContext));
        return request(p);
    }

    //获取扩展配置信息
    private Map request(Map<String, Object> params) {
        Map<String,Object> ret = new HashMap<>();

        try {
            URL url = new URL(Urls.HTTP_URL);
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("POST");//post 请求
            postConnection.setConnectTimeout(1000*10);
            postConnection.setReadTimeout(1000*10);
            postConnection.setDoInput(true);//允许从服务端读取数据

            params.put("ctime", new Date().getTime());
            String sign = buildHttpSign(params, mCorpKey);
            String dataStr = new JSONObject(params).toString();

            String postParms = "data="+dataStr+"&key="+mCorpKey+"&sign="+sign;
            Logger.info(TAG,"req: " + url + "?" + postParms);

            OutputStream outputStream = postConnection.getOutputStream();
            outputStream.write(postParms.getBytes());//把参数发送过去.
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
                String retStr = buffer.toString();

                Logger.info(TAG,"resp:" + retStr);

                JSONObject jsonObject = new JSONObject(retStr);
                int c = jsonObject.getInt("c");
                if (c == 200){
                    JSONObject dObj = jsonObject.getJSONObject("d");
                    if (dObj!=null){
                        Iterator<String> keys = dObj.keys();
                        while (keys.hasNext()){
                            String k = keys.next();
                            ret.put(k, dObj.get(k));
                        }
                    }
                } else {
                    String m = jsonObject.getString("m");
                    Logger.error(TAG,"resp msg:" + m);
                    ret.put("error", m);
                }
            }else {
                String msg = null;
                try {
                    InputStream inputStream = postConnection.getErrorStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line = null;//一行一行的读取
                    while ((line = bufferedReader.readLine()) != null) {
                        buffer.append(line);//把一行数据拼接到buffer里
                    }
                    msg = buffer.toString();
                }catch (Exception e){}
                Logger.error(TAG,"resp code:" + code + ", msg:" + msg);

                ret.put("error", msg);
            }

        } catch (Exception e) {
            Logger.error(TAG,"request exception: " + e.getMessage());
            ret.put("error", e.getMessage());
        }
        return ret;
    }

    /**
     * 签名工具方法
     *
     * @param reqMap
     * @return
     */
    private static String buildHttpSign(Map<String, Object> reqMap, String signKey) {
        TreeMap<String, Object> signMap = new TreeMap<String, Object>(reqMap);
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : signMap.entrySet()) {
            if ("sign".equals(entry.getKey()) || "signType".equals(entry.getKey())) {
                continue;
            }
            if (entry.getValue() != null) {
                Object val = null;
                try {
                    if (entry.getValue() instanceof Map){
                        val = new JSONObject((Map)entry.getValue()).toString();
                    }
                }catch (Exception e){}
                if (val == null){
                    val = entry.getValue();
                }
                stringBuilder.append(entry.getKey()).append("=").append(val);
            }
        }
        stringBuilder.append(signKey);
        String signSrc = stringBuilder.toString().replaceAll("&", "");//剔除参数中含有的'&'符号
        return MD5Util.md5(signSrc).toLowerCase();
    }

}
