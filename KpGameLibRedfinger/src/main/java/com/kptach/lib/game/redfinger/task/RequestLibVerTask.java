package com.kptach.lib.game.redfinger.task;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import com.kptach.lib.game.redfinger.utils.Logger;

public class RequestLibVerTask extends AsyncTask<String,Void,HashMap> {
    private static final String TAG = "RequestDeviceTask";

    public interface ICallback{
        void onSucces(String libUrl, String md5);
        void onFaile(int code, String err);
    }

    private String mSdkUrl;
    private String mSdkVer;

    private ICallback mCallback;

    public RequestLibVerTask setCallback(ICallback callback) {
        this.mCallback = callback;
        return this;
    }

    public RequestLibVerTask setSdkUrl(String url){
        this.mSdkUrl = url;
        return this;
    }

    public RequestLibVerTask setSdkVer(String ver) {
        this.mSdkVer = ver;
        return this;
    }

    @Override
    protected HashMap doInBackground(String... args) {
        HashMap<String,String> ret = new HashMap();
        int code = 0;
        String err = null;
        try {
            String corpKey = args[0];
            String libVer = args[1];

            String str = requestAppInfo(corpKey, libVer);

            JSONObject jsonObject = new JSONObject(str);
            int c = jsonObject.getInt("c");

            if (c == 0){
                JSONObject dObj = jsonObject.getJSONObject("d");
                Logger.info(TAG,"device connect success: " + dObj.toString());

                String md5 = dObj.optString("md5");
                String url = dObj.optString("libUrl");

                code = 0;
                ret.put("md5", md5);
                ret.put("libUrl", url);
            }else {
                String m = jsonObject.getString("m");
                Logger.error(TAG,"device connect faile:" + m);
                JSONObject json = new JSONObject();
                code = -1;
                err = json.toString();
            }
        }catch (Exception e){
            Logger.error(TAG,"device connect error:" + e.getMessage());
            code = -2;
            err = e.getMessage();
        }

        ret.put("code", code+"");
        if (err!=null){
            ret.put("err", err);
        }

        return ret;
    }

    @Override
    protected void onPostExecute(HashMap ret) {
        if(mCallback!=null){
            if (ret == null){
                mCallback.onFaile(-3, "接口返回数据错误");
                return;
            }
            int code = Integer.parseInt(ret.get("code").toString()) ;

            String md5 = ret.containsKey("md5") ? ret.get("md5").toString() : null;
            String libUrl = ret.containsKey("libUrl") ? ret.get("libUrl").toString() : null;
            if (md5 != null || libUrl != null){
                mCallback.onSucces(libUrl, md5);
                return;
            }

            String err = ret.containsKey("err") ? ret.get("err").toString() : "";
            mCallback.onFaile(code, err);
        }
    }

    //获取扩展配置信息
    private String requestAppInfo(String corpKey, String libVersion) {

        String str = mSdkUrl;
        Logger.info(TAG,"device connect :" + str);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append("?corpKey=" + corpKey);
            sb.append("&version=" + mSdkVer);
            URL url = new URL(sb.toString());
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("POST");//post 请求
            postConnection.setConnectTimeout(1000*10);
            postConnection.setReadTimeout(1000*10);
            postConnection.setDoInput(true);//允许从服务端读取数据
            postConnection.setRequestProperty("Content-type", "application/json");//以表单形式传递参数

            HashMap map = new HashMap();
            map.put("libVersion", libVersion);
            String dataStr = new JSONObject(map).toString();
            OutputStream outputStream = postConnection.getOutputStream();
            outputStream.write(dataStr.getBytes());//把参数发送过去.
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
                Logger.error(TAG,"appInfo response code:" + code + "msg:" + msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }




}
