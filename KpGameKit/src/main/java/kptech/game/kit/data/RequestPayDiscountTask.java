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

public class RequestPayDiscountTask extends AsyncTask<String,Void,String> {
    public interface ICallback{
        void onResult(HashMap<String, String> map);
    }

    private ICallback mCallback;
    public RequestPayDiscountTask(ICallback callback){
        mCallback = callback;
    }

    @Override
    protected String doInBackground(String... args) {
        if (args==null || args.length < 3){
            return null;
        }

        String corpId = args[0];
        String guid = args[1];
        String gameId = args[2];
        return requestDiscount(corpId, guid, gameId);
    }

    @Override
    protected void onPostExecute(String str) {

        if (mCallback!=null){
            HashMap<String,String> map = new HashMap();
            if (str == null){
                map.put("error","参数出错");
                mCallback.onResult(map);
                return;
            }

            if (str.startsWith("Error:")){
                map.put("error",str.substring(6,str.length()-1));
                mCallback.onResult(map);
                return;
            }

            try{
                JSONObject jsonObject = new JSONObject(str);
                int c = jsonObject.getInt("c");

                if (c == 200){
                    JSONObject dObj = jsonObject.getJSONObject("d");
                    String tradenum = dObj.getString("tradenum");
                    map.put("tradenum",tradenum);
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
    private String requestDiscount(String corpKey, String guid, String gameId) {

        String str = "https://wxgzh.kuaipantech.com/kp/api/wx/gzh/game/discounts/list";
        //https://wxgzh.kuaipantech.com/kp/api/wx/gzh/game/discounts/list?gameId=&guid=
        try {
            URL url = new URL(str);
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("POST");//post 请求
            postConnection.setConnectTimeout(1000*10);
            postConnection.setReadTimeout(1000*10);
            postConnection.setDoInput(true);//允许从服务端读取数据
            postConnection.setDoOutput(true);//允许写入
//            postConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");//以表单形式传递参数
            String postParms = "gameId=" + gameId
                    + "&guid=" + guid
                    + "&corpKey=" + corpKey;

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
            return "Error:" + e.getMessage();
        }
        return "";
    }

}
