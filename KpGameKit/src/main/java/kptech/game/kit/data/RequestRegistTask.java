package kptech.game.kit.data;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RequestRegistTask extends AsyncTask<String,Void,String> {
    public interface ICallback{
        void onResult(HashMap<String, Object> map);
    }

    public static final String ACTION_REGIST = "1";
    public static final String ACTION_FORGET = "2";

    private ICallback mCallback;
    private String mCorpId;
    private String mPhone;
    private String mAction;
    private String mPsw;
    private String mSmsCode;
    private String mSmsCodeId;


    public RequestRegistTask(String corpId, String action, String phone, String psw, String code, String smsCodeId, ICallback callback){
        mCallback = callback;
        mCorpId = corpId;
        mPhone = phone;
        mAction = action;
        mPsw = psw;
        mSmsCode = code;
        mSmsCodeId = smsCodeId;
    }

    @Override
    protected String doInBackground(String... args) {
        return requestRegist();
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
            str = "{\"c\":200,\"m\":\"OK\",\"d\":{\"token_type\":\"Bearer\",\"expires_in\":7862400,\"access_token\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxNTg1MDM2OTY5MjEyNTI5IiwianRpIjoiMzkzYTIwOGQ0ZjI1NTVjZTllOTNlZjZlMjNkM2Q4ZTBjM2ZkNDhiNTQ4ZTBiY2ZmZjU1N2JlMjdlN2UzYmY5ZDI3NDk0Yzg1ZjkyNjg5YjciLCJpYXQiOjE2MDE0NTA0ODQsIm5iZiI6MTYwMTQ1MDQ4NCwiZXhwIjoxNjA5MzEyODg0LCJzdWIiOiJlZjc1NWRlNGFmMjI0NjQ3YTIxMWMwNzM2NzkyZDBhMSIsInNjb3BlcyI6WyJnZXRVc2VySW5mbyIsImxvZ2luT3V0IiwidXBkYXRlUGFzc3dvcmQiXX0.SFP_4NNuSTBpCo64sgm6VAQUO7fIi6tO7FXIRbtHb5A1wdqsdgDpoMayD69jpI-CxC3ztccO8J1bOo6nvJGQz01GOCcEtQotv8yzVAqCrUiSavS2oW4AiO0SffxP5U6hLI79JXXaqTyagoXA96dYk7yobOdZZUpI2EV0fT0DXwfi7cT0j_mSC_5mP_4bnp-rRR-nhLUbtLsmapNnkIQOfgnPpjgyD3K_N1N2fxFOLaNhvHfrJOv1jeNy5A2IZwvQ577Im2Oka0rIt9m8TSI_37NblZr5LWL90NGw-nGXQRs5vfWHXLBLuONdDK0YveReoBjX1btUi7ACHro4gEZrd0TjxS9BWfYasrdhlzVlTHmF8MG87KIEJir9vjJWjKWSj8DfJWl8gVPdxOgfIjZJRS3pKqVPrtzTHJ8Y1kulXORTlf6yQKCsbr7QY3f6Pv5tPK6CRBw54KRL_SdkoKyEgmRmZz1iMyIjlw3SXgZ7wAmN-3jqmT2AVAdLSyOL3-SPjGYW73bwNFh8iY88k43A9-2BS9PrHWweyJAuxTdGl-dayHyLxphGL8CWdG47LVZK1SF0PvDJR1giT9jvM97bmy-nmfwH_rLrjAj7EGwf5TBm_y64GJ7kWTBJhjG3Utw-SKyItyIH6ktywEPuBQfDerC_0AaY3pUxxDH0nhtjL6U\",\"refresh_token\":\"def502009a34f14126f18437dae535db7f42e3e6c6a7001cc75b95030100d61539c061a96f51c449e3552490cb744f5c3468edc50327422d71b44e86e01da65f154a8afa782f3068d220b94f0ca67c9ec73b4d1a1c0275424c3f46744b1b294c05408be7cd8746c419313071815d266aa7c8eb4bf89620e64267f3a71de8a1dae4e0d795f1891586be20b1e1c6ddfc411f8ca4ab878887ccc8646171cb1850c4912297e7d4c930e43d75885c17b25a66a2cdcab1e14168bf711ba40df809f7de9695c709c0ecc7b0ca603b7f6c8bcfcd0f1455352b7ccc1fd908cc658512fa341b38562c6531fe46d63700ef1df60ed16d054951b87180d57a0e18ea0bd283ce6389d47b239d37428030acdcd9f758b29a64833197b2604ca83bda4979c80cbffcfd5e8c0b1f2e4245abf2b07397d54e5422942f10687132ad4012cd47932c99ad8bae6d47ee255b7f024d2f556215700f25bf69db1f8dead87ff008fd3abf02152a4c85a711ca3d0d9a958dd047d1f49b1d11d673bfbcb9ff2dd375db98862201dd346b09de4c6697dd63172b9225d4a949e16062295e6ce9c4d2e922e0d71ddf67e436b4efe8de346f0579b1a80c036f80681c81583afa14b1\",\"redirect\":\"https:\\/\\/auth-dev.kuaipantech.com\\/redirect\\/kpWebLoginRedirect\",\"phone\":\"13811571396\",\"guid\":\"ef755de4af224647a211c0736792d0a1\"}}";
            try{
                JSONObject jsonObject = new JSONObject(str);
                int c = jsonObject.getInt("c");

                if (c == 200){
                    JSONObject dObj = jsonObject.getJSONObject("d");
                    Iterator<String> keys = dObj.keys();
                    while (keys.hasNext()){
                        String k = keys.next();
                        Object v = dObj.get(k);
                        map.put(k, v);
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
    private String requestRegist() {
//        https://auth-dev.kuaipantech.com/api/user/register?phone=&password=&smsCode=&smsCodeId=&client_id=1597450043952963&client_secret=Z4zQA6sTlOEsBAXRijNo9fC8cDecfM3OA7pfg4O9
//        https://auth-dev.kuaipantech.com/api/forgetpassword?phone=&password=&smsCode=&smsCodeId&client_id=1597450043952963&client_secret=Z4zQA6sTlOEsBAXRijNo9fC8cDecfM3OA7pfg4O9
//        String str = "https://auth-dev.kuaipantech.com/api/user/register";

        String str = mAction == ACTION_FORGET ? "https://auth-dev.kuaipantech.com/api/forgetpassword" : "https://auth-dev.kuaipantech.com/api/user/register";
        try {
            URL url = new URL(str);
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("POST");//post 请求
            postConnection.setConnectTimeout(1000*5);
            postConnection.setReadTimeout(1000*5);
            postConnection.setDoInput(true);//允许从服务端读取数据
            postConnection.setDoOutput(true);//允许写入

            String postParms = "corpKey=" + mCorpId +
                    "&phone=" + mPhone +
                    "&password=" + mPsw +
                    "&smsCode=" + mSmsCode +
                    "&smsCodeId[]=" + mSmsCodeId +  //php接收数组形式a参数为数组,a[]=a&a[]=b
                    "&client_id=1597450043952963&client_secret=Z4zQA6sTlOEsBAXRijNo9fC8cDecfM3OA7pfg4O9&app_key=dc02bd00184e4a9381d2eb3fe4a529b2";

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
