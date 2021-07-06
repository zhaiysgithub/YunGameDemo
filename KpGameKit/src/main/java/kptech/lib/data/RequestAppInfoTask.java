package kptech.lib.data;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import kptech.game.kit.BuildConfig;
import kptech.lib.conf.Config;
import kptech.lib.constants.SharedKeys;
import kptech.lib.constants.Urls;
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
                String kp3CorpKey = dObj.has("kp3Corpkey") ? dObj.getString("kp3Corpkey") : null;

                //缓存数据
                ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_KEY, ak);
                ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_SECRET, sk);
                if (ch != null){
                    ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_CHANNEL, ch);
                }
                if (paas != null){
                    ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_PAAS, paas);
                    if (paas.equals("KP")){
                        ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_PAAS3CORPKEY, kp3CorpKey);
                    }
                }

                Config.saveConfig(mContext, dObj);

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
    }

    //获取扩展配置信息
    private String requestAppInfo(String corpKey) {

        String str = Urls.getEnvUrl(Urls.GET_CONFIG);
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

        String str = Urls.getEnvUrl(Urls.GET_AD_CONFIG);
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
