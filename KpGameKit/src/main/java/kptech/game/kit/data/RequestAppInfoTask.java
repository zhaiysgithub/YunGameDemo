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

public class RequestAppInfoTask extends AsyncTask<String,Void,String> {
    private static final Logger logger = new Logger("RequestAppInfoTask") ;
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
    protected String doInBackground(String... args) {
        String str = requestAppInfo(args[0]);
        return str;
    }

    @Override
    protected void onPostExecute(String str) {
        boolean ret = false;
        try {
            JSONObject jsonObject = new JSONObject(str);
            int c = jsonObject.getInt("c");
            if (c == 0){
                JSONObject dObj = jsonObject.getJSONObject("d");
                String ak = dObj.getString("ak");
                String sk = dObj.getString("sk");
                String ch = dObj.has("ch") ? dObj.getString("ch") : null;
                String paas =  dObj.has("paas") ? dObj.getString("paas") : null;
                String adJson = dObj.has("adJson") ? dObj.getString("adJson") : null;
                String adEnable = dObj.has("adEnable2") ? dObj.getString("adEnable2") : null;

                //缓存数据
                ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_KEY, ak);
                ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_SECRET, sk);
                if (ch != null){
                    ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_CHANNEL, ch);
                }
                if (paas != null){
                    ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_PAAS, paas);
                }
                if (adJson != null){
                    ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_ADJSON, adJson);
                }
                if (adEnable != null){
                    ProferencesUtils.setString(mContext, SharedKeys.KEY_GAME_APP_ADENABLE, adEnable);
                }

                ret = true;
            }else {
                String m = jsonObject.getString("m");
                logger.error(m);
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }

        if(mCallback!=null){
            mCallback.onResult(ret);
        }
    }

    //post请求
    private String requestAppInfo(String corpKey) {

        String str = Urls.GET_CONFIG;
        logger.info("url:" + str);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append("?corpKey="+corpKey);
            sb.append("&apiVer=" + BuildConfig.API_VERSION);
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
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


}
