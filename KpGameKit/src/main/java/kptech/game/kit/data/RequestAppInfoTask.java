package kptech.game.kit.data;

import android.os.AsyncTask;
import android.os.Handler;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class RequestAppInfoTask extends AsyncTask<String,Void,String> {
    public interface ICallback{
        void onResult(HashMap<String,String> map);
    }

    private ICallback mCallback;
    public RequestAppInfoTask(ICallback callback){
        mCallback = callback;
    }

    @Override
    protected String doInBackground(String... args) {
        String str = requestAppInfo(args[0], args[1]);
        return str;
    }

    @Override
    protected void onPostExecute(String str) {
        if (mCallback!=null){
            HashMap<String,String> map = new HashMap();
            try{
                JSONObject jsonObject = new JSONObject(str);
                map.put("adkey", jsonObject.getString("adkey"));
                map.put("adtoken", jsonObject.getString("adtoken"));
                map.put("adtype", jsonObject.getString("adtype"));
                map.put("adcode", jsonObject.getString("adcode"));
            }catch (Exception e){

            }
            mCallback.onResult(map);
        }
    }

    //post请求
    private String requestAppInfo(String key, String secret) {

        String str = "http://bd.open.kuaipantech.com/index.php";
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append("?appKey="+key);
            sb.append("&appSecret="+secret);
            URL url = new URL(sb.toString());
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("GET");//post 请求
            postConnection.setConnectTimeout(1000*5);
            postConnection.setReadTimeout(1000*5);
            postConnection.setDoInput(true);//允许从服务端读取数据
            postConnection.setDoOutput(true);//允许写入
//            postConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");//以表单形式传递参数
//            String postParms = "name=1702C2019&password=12345&verifyCode=8888";
            OutputStream outputStream = postConnection.getOutputStream();
//            outputStream.write(postParms.getBytes());//把参数发送过去.
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
