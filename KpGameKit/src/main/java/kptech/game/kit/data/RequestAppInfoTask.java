package kptech.game.kit.data;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import kptech.game.kit.BuildConfig;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.constants.Urls;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;

public class RequestAppInfoTask extends AsyncTask<String,Void,Boolean> {
    private static final String TAG = "RequestAppInfoTask";
    private Context mContext;

    public interface ICallback{
        void onResult(boolean success);
    }

    private ICallback mCallback;
    public RequestAppInfoTask(Context context, ICallback callback){
        this(context);
        mCallback = callback;
    }

    public RequestAppInfoTask(Context context){
        this.mContext = context;
    }

    @Override
    protected Boolean doInBackground(String... args) {
        boolean ret = false;
        try {
            String str = requestAppInfo(args[0]);

            JSONObject jsonObject = new JSONObject(str);
            int c = jsonObject.getInt("c");
            if (c == 0){
                JSONObject dObj = jsonObject.getJSONObject("d");
                String ak = dObj.has("ak") ? dObj.getString("ak") : null;
                String sk = dObj.has("sk") ? dObj.getString("sk") : null;
                String ch = dObj.has("ch") ? dObj.getString("ch") : null;
                String paas =  dObj.has("paas") ? dObj.getString("paas") : null;

                //缓存数据
                ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_KEY, ak);
                ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_SECRET, sk);
                if (ch != null){
                    ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_CHANNEL, ch);
                }
                if (paas != null){
                    ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_PAAS, paas);
                }

                //获取支付配置信息
                String payJson = dObj.has("payConf") ? dObj.getString("payConf") : null;
                if (payJson != null){
                    ProferencesUtils.setString(mContext, SharedKeys.KEY_PAY_CONF, payJson);
                }

                //挽留弹窗次数
                String exitAlertNum = dObj.has("detentionNum") ? dObj.getString("detentionNum") : null;
                if (exitAlertNum != null){
                    try {
                        ProferencesUtils.setInt(mContext, SharedKeys.KEY_GAME_EXITALERTCOUNT_CONF, Integer.parseInt(exitAlertNum));
                    }catch (Exception e){}
                }

                try {
                    //websocket URL
                    String wsurl = dObj.has("wsurl") ? dObj.getString("wsurl") : null;
                    if (wsurl != null){
                        ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_WSURL, wsurl);

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                try {
                    //mockSleepTime
                    String mockSleepTime = dObj.has("mockSleepTime") ? dObj.getString("mockSleepTime") : null;
                    if (mockSleepTime != null){
                        ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_MOCK_SLEEPTIME, mockSleepTime);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                ret = true;
            }else {
                String m = jsonObject.getString("m");
                Logger.error(TAG,"requestAppInfo faile:" + m);
            }
        }catch (Exception e){
            Logger.error(TAG,"requestAppInfo error:" + e.getMessage());
        }


        try {
            String adStr = requestAdInfo(args[0]);

            JSONObject jsonObject = new JSONObject(adStr);
            int c = jsonObject.getInt("c");
            if (c == 0){
                JSONObject dObj = jsonObject.getJSONObject("d");
                String adJson = dObj.has("adJson") ? dObj.getString("adJson") : null;
                String adEnable = dObj.has("adEnable") ? dObj.getString("adEnable") : null;

                //缓存数据
                if (adJson != null){
                    ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_ADJSON, adJson);
                }
                if (adEnable != null){
                    ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_ADENABLE, adEnable);
                }

            }else {
                String m = jsonObject.getString("m");
                Logger.error(TAG,"requestAdInfo faile:" + m);
            }
        }catch (Exception e){
            Logger.error(TAG,"requestAdInfo error:" + e.getMessage());
        }

        return ret;
    }

    @Override
    protected void onPostExecute(Boolean ret) {
        if(mCallback!=null){
            mCallback.onResult(ret);
        }

//        boolean ret = false;
//        try {
//            JSONObject jsonObject = new JSONObject(str);
//            int c = jsonObject.getInt("c");
//            if (c == 0){
//                JSONObject dObj = jsonObject.getJSONObject("d");
//                String ak = dObj.has("ak") ? dObj.getString("ak") : null;
//                String sk = dObj.has("sk") ? dObj.getString("sk") : null;
//                String ch = dObj.has("ch") ? dObj.getString("ch") : null;
//                String paas =  dObj.has("paas") ? dObj.getString("paas") : null;
//                String adJson = dObj.has("adJson") ? dObj.getString("adJson") : null;
//                String adEnable = dObj.has("adEnable") ? dObj.getString("adEnable") : null;
//
//                if (dObj.has("BD")){
//                    JSONObject DBObj =  dObj.getJSONObject("BD");
//                    if (DBObj.has("ak")){
//                        ak = DBObj.getString("ak");
//                    }
//                    if (DBObj.has("sk")){
//                        sk = DBObj.getString("sk");
//                    }
//                    paas = "BD";
//                }
//
//                //缓存数据
//                ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_KEY, ak);
//                ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_SECRET, sk);
//                if (ch != null){
//                    ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_CHANNEL, ch);
//                }
//                if (paas != null){
//                    ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_PAAS, paas);
//                }
//                if (adJson != null){
//                    ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_ADJSON, adJson);
//                }
//                if (adEnable != null){
//                    ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_ADENABLE, adEnable);
//                }
//
//                ret = true;
//            }else {
//                String m = jsonObject.getString("m");
//                Logger.error(TAG,m);
//            }
//        }catch (Exception e){
//            Logger.error(TAG,e.getMessage());
//        }
//
//        if(mCallback!=null){
//            mCallback.onResult(ret);
//        }
    }

    //获取扩展配置信息
    private String requestAppInfo(String corpKey) {

        String str = Urls.GET_CONFIG;
        Logger.info(TAG,"url:" + str);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append("?corpKey="+corpKey);
            sb.append("&version=" + BuildConfig.VERSION_NAME);
            URL url = new URL(sb.toString());
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("GET");//post 请求
            postConnection.setConnectTimeout(1000*10);
            postConnection.setReadTimeout(1000*10);
            postConnection.setDoInput(true);//允许从服务端读取数据
//            postConnection.setDoOutput(true);//允许写入
//            postConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");//以表单形式传递参数
//            String postParms = "name=1702C2019&password=12345&verifyCode=8888";
//            OutputStream outputStream = postConnection.getOutputStream();
//            outputStream.write(postParms.getBytes());//把参数发送过去.
//            outputStream.flush();
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

    //获取广告配置信息
    private String requestAdInfo(String corpKey) {

        String str = Urls.GET_AD_CONFIG;
        Logger.info(TAG,"url:" + str);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append("?corpKey="+corpKey);
            sb.append("&version=" + BuildConfig.VERSION_NAME);
            URL url = new URL(sb.toString());
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("GET");//post 请求
            postConnection.setConnectTimeout(1000*5);
            postConnection.setReadTimeout(1000*5);
            postConnection.setDoInput(true);//允许从服务端读取数据
//            postConnection.setDoOutput(true);//允许写入
//            postConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");//以表单形式传递参数
//            String postParms = "name=1702C2019&password=12345&verifyCode=8888";
//            OutputStream outputStream = postConnection.getOutputStream();
//            outputStream.write(postParms.getBytes());//把参数发送过去.
//            outputStream.flush();
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
                Logger.error(TAG,"appAdInfo response code:" + code + "msg:" + msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }



}
