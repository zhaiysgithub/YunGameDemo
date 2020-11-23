package kptech.game.kit.data;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

public class RequestLoginTask extends AsyncTask<String,Void,String> {
    public interface ICallback{
        void onResult(HashMap<String, Object> map);
    }

    private ICallback mCallback;
    public RequestLoginTask(ICallback callback){
        mCallback = callback;
    }

    private String mCmd = null;
    private String uninqueId = null;
    @Override
    protected String doInBackground(String... args) {
        String ret = null;
        mCmd = args[0];
        try {
            if ("uid".equals(mCmd)){
                uninqueId = args[2];
                ret = requestUidLogin(args[1], args[2]);
            }else if ("kp".equals(mCmd)){
                ret = requestKpLogin(args[1], args[2],args[3]);
            }
        }catch (Exception e){

        }

        return ret;
    }

    @Override
    protected void onPostExecute(String str) {
        if (mCallback!=null){
            HashMap<String,Object> map = new HashMap();
            if (str == null){
                map.put("error","登录参数出错");
                mCallback.onResult(map);
                return;
            }
            try{
                JSONObject jsonObject = new JSONObject(str);
                int c = jsonObject.getInt("c");

                if (c == 200){
                    JSONObject dObj = jsonObject.getJSONObject("d");
                    if (dObj != null){
                        Iterator<String> keys =  dObj.keys();
                        while (keys.hasNext()){
                            String k = keys.next();
                            map.put(k, dObj.get(k)) ;
                        }
                    }
                }else {
                    String m = jsonObject.getString("m");
                    map.put("error",m);
                }

            }catch (Exception e){
                map.put("error",e.getMessage());
            }
            mCallback.onResult(map);
        }
    }

    //post请求
    private String requestUidLogin(String mCorpId, String userId) {

        String str = "https://auth-dev.kuaipantech.com/api/client/user/login";
        try {
            URL url = new URL(str);
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("POST");//post 请求
            postConnection.setConnectTimeout(1000*10);
            postConnection.setReadTimeout(1000*10);
            postConnection.setDoInput(true);//允许从服务端读取数据
            postConnection.setDoOutput(true);//允许写入
//            postConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");//以表单形式传递参数
            String postParms = "corpKey="+mCorpId+"&globalUserId="+userId+"&client_id=1585036969212529&client_secret=n9Yja2ybFgqNSNIua3ykqV83zq2mrXVDPmRmikqV";
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

    //post请求
    private String requestKpLogin(String mCorpId, String phone, String psw) {

        String str = "https://auth-dev.kuaipantech.com/api/user/login";
        try {
            URL url = new URL(str);
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("POST");//post 请求
            postConnection.setConnectTimeout(1000*5);
            postConnection.setReadTimeout(1000*5);
            postConnection.setDoInput(true);//允许从服务端读取数据
            postConnection.setDoOutput(true);//允许写入
//            postConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");//以表单形式传递参数
            String postParms = "corpKey="+mCorpId+"&phone="+phone+"&password="+psw+"&client_id=1585036969212529&client_secret=n9Yja2ybFgqNSNIua3ykqV83zq2mrXVDPmRmikqV&app_key=dc02bd00184e4a9381d2eb3fe4a529b2";
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
