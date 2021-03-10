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

import kptech.game.kit.BuildConfig;
import kptech.game.kit.utils.Logger;
import kptech.lib.analytic.DeviceInfo;
import kptech.lib.constants.Urls;

public class RequestRecordScreen extends AsyncTask<Object,Void,Map> {
    public interface ICallback{
        void onSuccess();
        void onError(String err);
    }
    private static final String TAG = RequestRecordScreen.class.getSimpleName();

    public static final int ACTION_RECORD_START = 1;
    public static final int ACTION_RECORD_PAUSE = 2;
    public static final int ACTION_RECORD_RESUME = 3;
    public static final int ACTION_RECORD_STOP = 4;
    public static final int ACTION_RECORD_UPLOAD = 5;
    public static final int ACTION_RECORD_PUBLISH = 6;

    private Context mContext;
    private int mAction;
    private String mCorpKey;
    private String mDeviceId;
    private ICallback mCallback;

    public RequestRecordScreen setCallback(ICallback callback){
        this.mCallback = callback;
        return this;
    }

    public RequestRecordScreen(Context context, int action){
        this.mContext = context;
        this.mAction = action;
        mDeviceId = DeviceInfo.getDeviceId(context);
    }

    public RequestRecordScreen setCorpKey(String mCorpKey) {
        this.mCorpKey = mCorpKey;
        return this;
    }

    private Map<String, Object> getRecordScreenParams(Object... params){

        Map<String,Object> p = new HashMap<>();

        switch (mAction){
            case ACTION_RECORD_START:
                p.put("action", "rec");
                break;
            case ACTION_RECORD_PAUSE:
                p.put("action", "pause");
                break;
            case ACTION_RECORD_RESUME:
                p.put("action", "resume");
                break;
            case ACTION_RECORD_STOP:
                p.put("action", "stop");
                break;
            case ACTION_RECORD_UPLOAD:
                p.put("action", "upload");
                break;
            case ACTION_RECORD_PUBLISH:
                p.put("action", "save");
                break;
        }

        p.put("padcode", params[0]);
        p.put("pkgname", params[1]);
        p.put("uid", DeviceInfo.getDeviceId(mContext));
        p.put("type", 1); //int 终端类型，1 -- Android SDK， 2 -- iOS SDK，3 -- H5 SDK
        p.put("tm", new Date().getTime()); //long int 本地时间戳
        HashMap m = new HashMap<String,Object>();
        m.put("corpkey",mCorpKey);
        p.put("clientinfo", new HashMap<>());//Object 客户端的相关信息，key-value值，例如：agent："xxxx",corpkey:"xxxx"

        p.put("data", params[2]);

        Map<String,Object> ret = new HashMap<>();
        ret.put("f", "APP_RECORDER");
        ret.put("p", new JSONObject(p).toString());
        ret.put("token","");
        ret.put("v", BuildConfig.VERSION_NAME);

        return ret;
    }

    @Override
    protected Map doInBackground(Object... params) {
        Map<String,Object> ret  = null;
        try {
            Map p = getRecordScreenParams(params);
            ret = request(p);
        }catch (Exception e){
            e.printStackTrace();
        }

        return ret;
    }

    @Override
    protected void onPostExecute(Map ret) {
        if (ret != null && !ret.containsKey("error")){
            if (mCallback != null){
                mCallback.onSuccess();
            }
        }else {
            String err = (String) ret.get("error");
            if (mCallback != null){
                mCallback.onError(err);
            }
        }
    }

    //post请求
    private Map request(Map map) {
        Map<String,Object> ret = new HashMap<>();
        try {
            URL url = new URL(Urls.URL_RECORD_SCREEN);
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("POST");//post 请求
            postConnection.setConnectTimeout(1000*10);
            postConnection.setReadTimeout(1000*10);
            postConnection.setDoInput(true);//允许从服务端读取数据
            postConnection.setDoOutput(true);//允许写入
            postConnection.setRequestProperty("Content-type", "application/json");//以表单形式传递参数

            JSONObject obj = new JSONObject(map);
            OutputStream outputStream = postConnection.getOutputStream();
            outputStream.write(obj.toString().getBytes());//把参数发送过去.
            outputStream.flush();

            Logger.info(TAG,"req:" + obj.toString());

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
                if (c == 0){
                    return ret;
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
            e.printStackTrace();
        }
        return ret;
    }
}
