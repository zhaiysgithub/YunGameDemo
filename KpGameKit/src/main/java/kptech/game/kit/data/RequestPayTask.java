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

public class RequestPayTask extends AsyncTask<HashMap,Void,String> {
    public interface ICallback{
        void onResult(HashMap<String, String> map);
    }

    private ICallback mCallback;
    public RequestPayTask(ICallback callback){
        mCallback = callback;
    }

    private String mCmd = null;

    @Override
    protected String doInBackground(HashMap... args) {
        String ret = null;
        try {
            ret = requestOrder(args[0]);
        }catch (Exception e){

        }

        return ret;
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
    private String requestOrder(HashMap map) {

        String str = "http://kp.you121.top/h5demo/wxjspay/maketrade.php";
        try {
            URL url = new URL(str);
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("POST");//post 请求
            postConnection.setConnectTimeout(1000*5);
            postConnection.setReadTimeout(1000*5);
            postConnection.setDoInput(true);//允许从服务端读取数据
            postConnection.setDoOutput(true);//允许写入
//            postConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");//以表单形式传递参数
            String postParms = "productcode=" + map.get("productcode")
                    + "&cp_orderID=" + map.get("cp_orderid")
                    + "&guid=" +  map.get("guid")
                    + "&globaluserid=" + map.get("globaluserid")
                    + "&globalusername=" + map.get("globalusername")
                    + "&cotype=" + map.get("cotype")  ;

//            HashMap<String, String> map = new HashMap();
//            map.put("productcode","");
//            map.put("cp_orderid","");
//            map.put("guid","");
//            map.put("globaluserid","");
//            map.put("globalusername:","");
//            map.put("cotype:","lianyun");
//            JSONObject json = new JSONObject(map);

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
