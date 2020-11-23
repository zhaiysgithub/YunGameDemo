package kptech.game.kit.data;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PublicKey;
import java.util.HashMap;

public class RequestPhoneCodeTask extends AsyncTask<String,Void,String> {
    public interface ICallback{
        void onResult(HashMap<String, Object> map);
    }

    public static final String ACTION_REGIST = "1";
    public static final String ACTION_FORGET = "2";

    private ICallback mCallback;
    private String mCorpId;
    private String mPhone;
    private String mAction;

    public RequestPhoneCodeTask(String corpId, String action, String phone,  ICallback callback){
        mCallback = callback;
        mCorpId = corpId;
        mPhone = phone;
        mAction = action;
    }

    @Override
    protected String doInBackground(String... args) {
        return requestPhoneCode();
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
                   if (dObj.has("smsCodeId")){
                       map.put("smsCodeId", dObj.get("smsCodeId"));
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
    private String requestPhoneCode() {
        String str = "https://auth-dev.kuaipantech.com/api/sendsms";
        try {
            URL url = new URL(str);
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("POST");//post 请求
            postConnection.setConnectTimeout(1000*10);
            postConnection.setReadTimeout(1000*10);
            postConnection.setDoInput(true);//允许从服务端读取数据
            postConnection.setDoOutput(true);//允许写入
            String postParms = "corpKey="+mCorpId+"&phone="+mPhone+"&action="+mAction+"&client_id=1597450043952963&client_secret=Z4zQA6sTlOEsBAXRijNo9fC8cDecfM3OA7pfg4O9&app_key=dc02bd00184e4a9381d2eb3fe4a529b2";
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
