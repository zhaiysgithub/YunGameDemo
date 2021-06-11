package kptech.lib.data;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import com.kptach.lib.inter.game.APIConstants;
import kptech.game.kit.BuildConfig;
import kptech.lib.constants.Urls;
import kptech.game.kit.utils.Logger;

public class RequestDeviceTask extends AsyncTask<String,Void,HashMap> {
    private static final String TAG = "RequestDeviceTask";

    public interface ICallback{
        void onResult(int code, String info);
    }

    private ICallback mCallback;
    public RequestDeviceTask(ICallback callback){
        mCallback = callback;
    }

    @Override
    protected HashMap doInBackground(String... args) {
        HashMap<String,String> ret = new HashMap();
        int code = 0;
        String msg = null;
        try {
            String corpKey = args[0];
            String pkgName = args[1];
            String uid = args[2];
            String gameId = args[3];

            String str = requestAppInfo(corpKey, pkgName, uid, gameId);

            JSONObject jsonObject = new JSONObject(str);
            int c = jsonObject.getInt("c");

            if (c == 0){
                JSONObject dObj = jsonObject.getJSONObject("d");
                Logger.info(TAG,"device connect success: " + dObj.toString());

                JSONObject err = dObj.optJSONObject("error");
                JSONObject devInfo = dObj.optJSONObject("resultInfo");
                if (devInfo==null || devInfo.length()<=0) {
                    code = APIConstants.ERROR_DEVICE_BUSY;
                    msg = err.toString();
                }else {
                    code = APIConstants.APPLY_DEVICE_SUCCESS;
//                    try {
//                        if (dObj.has("extInfo")){
//                            JSONObject extInfo = devInfo.getJSONObject("extInfo");
//                            devInfo.put("apiLevel", extInfo.optInt("apiLevel"));
//                            devInfo.put("useSSL", extInfo.optInt("useSSL"));
//                        }
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }

                    msg = dObj.toString();
                }

            }else {
                String m = jsonObject.getString("m");
                Logger.error(TAG,"device connect faile:" + m);
                JSONObject json = new JSONObject();
                json.put("code", c);
                json.put("msg", m);
                code = APIConstants.ERROR_CALL_API;
                msg = json.toString();
            }
        }catch (Exception e){
            Logger.error(TAG,"device connect error:" + e.getMessage());
            code = APIConstants.ERROR_CALL_API;
            msg = e.getMessage();
        }

        ret.put("code", code+"");
        ret.put("info", msg);

        return ret;
    }

    @Override
    protected void onPostExecute(HashMap ret) {
        if(mCallback!=null){
            if (ret == null){
                mCallback.onResult(APIConstants.ERROR_CALL_API, "申请设备接口错误");
                return;
            }

            int code = Integer.parseInt(ret.get("code").toString()) ;
            String info = (String) ret.get("info");
            mCallback.onResult(code, info);
        }
    }

    //获取扩展配置信息
    private String requestAppInfo(String corpKey, String pkgName, String uid, String gameId) {

        String str = Urls.getEnvUrl(Urls.GET_DEVICE_CONNECT);
        Logger.info(TAG,"device connect :" + str);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append("?corpKey="+corpKey);
            sb.append("&version=" + BuildConfig.VERSION_NAME);
            URL url = new URL(sb.toString());
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("POST");//post 请求
            postConnection.setConnectTimeout(1000*10);
            postConnection.setReadTimeout(1000*10);
            postConnection.setDoInput(true);//允许从服务端读取数据
            postConnection.setRequestProperty("Content-type", "application/json");//以表单形式传递参数

            HashMap map = new HashMap();
            map.put("corpKey", corpKey);
            map.put("pkgName", pkgName);
            map.put("gameId", gameId);
            map.put("uuid", uid);
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
