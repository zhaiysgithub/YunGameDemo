package kptech.lib.data;

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

import kptech.game.kit.BuildConfig;
import kptech.lib.analytic.DeviceInfo;
import kptech.lib.constants.Urls;
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
    //三方认证登录
    public static final String ACTION_AUTH_THIRD_USER = "10";
    //检测 guid , token 有效性
    public static final String ACTION_AUTH_PLAT_EFFECT = "11";
    public static final String ACTION_AUTH_GT_API = "12";

    public static final String SENDSMS_TYPE_PHONELOGIN = "4";
    public static final String SENDSMS_TYPE_REGIST = "1";
    public static final String SENDSMS_TYPE_UPDATEPSW = "2";

    private String mAction;
    private String mCorpKey;
    private String mPkgName;
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

    public AccountTask setPkgName(String pkgName) {
        this.mPkgName = pkgName;
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
        }else if (mAction.equals(ACTION_AUTH_THIRD_USER)){
            ret = doLoginAuthByThird(params);
        }else if(mAction.equals(ACTION_AUTH_PLAT_EFFECT)){
            ret = doCheckPlatUserValid(params);
        }else if(mAction.equals(ACTION_AUTH_GT_API)){
            ret = doAuthGtApi(params);
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
        p.put("package", mPkgName);
        return request(p);
    }

    private Map<String, Object> doLoginPhone(Object... params){
        Map<String,Object> p = new HashMap<>();
        p.put("func", "smscodelogin" );
        p.put("phone", params[0]);
        p.put("smsCode", params[1]);
        p.put("smsCodeId", params[2]);
        p.put("deviceid", mDeviceId);
        p.put("package", mPkgName);
        //大账号测试
        p.put("corpKey",mCorpKey);
        p.put("usersign",params[3]);
        return request(p);
    }

    private Map<String, Object> doLoginChannel(Object... params){
        Map<String,Object> p = new HashMap<>();
        p.put("func", "thirdpartlogin");
        p.put("usersign", params[0]);    //用户id
        p.put("deviceid", mDeviceId);
        p.put("corpkey", mCorpKey);    //用户id
        p.put("package", mPkgName);
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
        p.put("package", mPkgName);
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
        p.put("datatype", BuildConfig.DEBUG ? "debug" : "");
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

    /**
     * 三方登录认证的接口
     */
    private Map<String,Object> doLoginAuthByThird(Object... params){
        Map<String,Object> map = new HashMap<>();
        if (params.length < 4){
            return map;
        }
        map.put("func", "thirduser");
        map.put("corpkey", mCorpKey);
        map.put("deviceid", mDeviceId);
        map.put("idnum", params[0]);
        map.put("username", params[1]);
        map.put("phone", params[2]);
        map.put("package",params[3]);
        return request(map,Urls.HTTP_URL_CLIENTUSER);
    }

    private Map<String,Object> doCheckPlatUserValid(Object... params){
        Map<String,Object> map = new HashMap<>();
        if (params.length < 3){
            return map;
        }
        map.put("func", "checkuser");
        map.put("corpkey", mCorpKey);
        map.put("deviceid", mDeviceId);
        map.put("guid", params[0]);
        map.put("token", params[1]);
        map.put("phone", params[2]);
        return request(map, Urls.HTTP_PLAT_KPUSER);
    }

    /**
     * 验签
     */
    private Map<String,Object> doAuthGtApi(Object... params){
        Map<String,Object> map = new HashMap<>();
        try{
            if (params == null || params.length < 5){
                return map;
            }
            map.put("f", "authlogin");
            map.put("ak",params[0]);

            JSONObject pJson = new JSONObject();
            pJson.put("usersign",params[1]); //用户唯一标识
            pJson.put("corpkey",params[2]);

            map.put("p",pJson);
            map.put("ts",params[3]); //客户端的时间值
            map.put("sign",params[4]); //客户端验签值

        }catch (Exception e){
            e.printStackTrace();
        }

        return requestStream(map,Urls.URL_GT_API);
    }



    private Map<String,Object> request(Map<String, Object> params){
        return request(params,Urls.HTTP_URL);
    }

    //获取扩展配置信息
    private Map<String,Object> request(Map<String, Object> params,String sepcUrl) {
        Map<String,Object> ret = new HashMap<>();

        try {
            URL url = new URL(sepcUrl);
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


    private Map<String,Object> requestStream(Map<String, Object> params,String sepcUrl){
        Map<String,Object> ret = new HashMap<>();

        HttpURLConnection postConnection = null;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        try{
            URL url = new URL(sepcUrl);
            postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("POST");
            postConnection.setConnectTimeout(1000*10);
            postConnection.setReadTimeout(1000*10);
            postConnection.setDoInput(true);
            postConnection.setDoOutput(true);

            String postParms = new JSONObject(params).toString();

            OutputStream outputStream = postConnection.getOutputStream();
            outputStream.write(postParms.getBytes());//把参数发送过去.
            outputStream.flush();

            final StringBuilder buffer = new StringBuilder();
            int code = postConnection.getResponseCode();
            if (code == 200){
                inputStream = postConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;//一行一行的读取
                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line);//把一行数据拼接到buffer里
                }
                String retStr = buffer.toString();

                Logger.info(TAG,"resp:" + retStr);

                JSONObject jsonObject = new JSONObject(retStr);
                int c = jsonObject.getInt("c");
                if (c == 200){
                    JSONObject dObj = jsonObject.getJSONObject("d");
                    Iterator<String> keys = dObj.keys();
                    while (keys.hasNext()){
                        String k = keys.next();
                        ret.put(k, dObj.get(k));
                    }
                } else {
                    String m = jsonObject.getString("m");
                    Logger.error(TAG,"resp msg:" + m);
                    ret.put("error", m);
                }
            }else {
                String msg = null;
                try {
                    inputStream = postConnection.getErrorStream();
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;//一行一行的读取
                    while ((line = bufferedReader.readLine()) != null) {
                        buffer.append(line);//把一行数据拼接到buffer里
                    }
                    msg = buffer.toString();
                }catch (Exception e){
                    e.printStackTrace();
                }
                Logger.error(TAG,"resp code:" + code + ", msg:" + msg);

                ret.put("error", msg);
            }

        }catch (Exception e){
            e.printStackTrace();
            ret.put("error", e.getMessage());
        }finally {
            try {
                if(bufferedReader != null){
                    bufferedReader.close();
                }
                if (inputStream != null){
                    inputStream.close();
                }
                if (postConnection != null){
                    postConnection.disconnect();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
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
