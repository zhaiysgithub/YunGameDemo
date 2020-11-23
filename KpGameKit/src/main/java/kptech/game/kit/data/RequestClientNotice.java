package kptech.game.kit.data;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kptech.game.kit.BuildConfig;

public class RequestClientNotice extends AsyncTask<String,Void,Void> {
    public interface ICallback{
        void onResult(String ret);
    }

    private ICallback mCallback;
    public RequestClientNotice setCallback(ICallback callback){
        this.mCallback = callback;
        return this;
    }


    @Override
    protected Void doInBackground(String... params) {
        String ret = "";
        try {
            HashMap<String,Object> p = new HashMap<>();
            p.put("action", "usevm"); //string，固定值
            p.put("padcode", params[0]);
            p.put("pkgname", params[1]);
            p.put("uid", params[2]);
            p.put("type", 1); //int 终端类型，1 -- Android SDK， 2 -- iOS SDK，3 -- H5 SDK
            p.put("tm", new Date().getTime()); //long int 本地时间戳
            HashMap m = new HashMap<String,Object>();
            m.put("corpkey",params[3]);
            p.put("clientinfo", m);//Object 客户端的相关信息，key-value值，例如：agent："xxxx",corpkey:"xxxx"

            ret = request(p);
        }catch (Exception e){
        }

        if (mCallback != null){
            mCallback.onResult(ret);
        }

        return null;
    }

    //post请求
    private String request(Map pMap) {
        String str = "http://kpsdkapi.kuaipantech.com/KpWebSDKApi/KpCloudCtrlsys/index.php";
        try {
            URL url = new URL(str);
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("POST");//post 请求
            postConnection.setConnectTimeout(1000*10);
            postConnection.setReadTimeout(1000*10);
            postConnection.setDoInput(true);//允许从服务端读取数据
            postConnection.setDoOutput(true);//允许写入

//            HashMap<String,Object> map = new HashMap<>();
//            map.put("f", "clientnotice");
//            map.put("p", pMap);
//            map.put("v", "1.0.1");
//            map.put("token", "");

            JSONObject obj = new JSONObject(pMap);
            String postParms = "f=clientnotice" +
                    "&p=" + obj.toString() +
                    "&v=1.0.1" +
                    "&token=" +
                    "&version=" + BuildConfig.VERSION_NAME;
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
                return buffer.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
