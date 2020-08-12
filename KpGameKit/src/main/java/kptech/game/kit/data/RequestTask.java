package kptech.game.kit.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import kptech.game.kit.GameInfo;
import kptech.game.kit.constants.Urls;
import kptech.game.kit.utils.Logger;

public class RequestTask {
    private static final Logger logger = new Logger("RequestTask") ;

    public static List<GameInfo> queryGameList(String corpKey, int page, int limit){
        String str = Urls.GET_GAME_LIST +  "/" + corpKey;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
//            sb.append("?currentPage="+page);
//            sb.append("&pageSize="+limit);
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

                //转成对象

                return buildList(buffer.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<GameInfo> buildList(String json){
        List<GameInfo> list = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            int c = jsonObject.getInt("c");
            if (c == 0){
                list = new ArrayList<>();

                JSONObject dObj = jsonObject.getJSONObject("d");
                JSONArray gameListObj = dObj.getJSONArray("gameList");
                for (int i = 0; i < gameListObj.length(); i++) {
                    try {
                        GameInfo inf = new GameInfo();
                        JSONObject gameObj = gameListObj.getJSONObject(i);
                        inf.gid = gameObj.getInt("gameId");
                        inf.pkgName =  gameObj.getString("gamePackageName");
                        JSONObject gameInfObj = gameObj.getJSONObject("gameInfo");
                        inf.name = gameInfObj.getString("gameName");
                        inf.iconUrl = gameInfObj.getString("gameLogoUrl");
                        inf.downloadUrl = gameInfObj.has("gameDownUrl") ? gameInfObj.getString("gameDownUrl") :  null;
                        list.add(inf);
                    }catch (Exception e){
                        logger.error(e.getMessage());
                    }
                }
            }else {
                String m = jsonObject.getString("m");
                logger.error(m);
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }

        return list;
    }


    public static GameInfo queryGameInfo(String corpKey, String pkg, String pass ){
        String str = Urls.GET_GAME_INFO;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append("?corpKey=" + corpKey);
            sb.append("&paas=" + pass);
            sb.append("&pkgName=" + pkg);
            URL url = new URL(sb.toString());
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("GET");//post 请求
            postConnection.setConnectTimeout(1000*5);
            postConnection.setReadTimeout(1000*5);
            postConnection.setDoInput(true);//允许从服务端读取数据

            final StringBuffer buffer = new StringBuffer();
            int code = postConnection.getResponseCode();
            if (code == 200) {//成功
                InputStream inputStream = postConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;//一行一行的读取
                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line);//把一行数据拼接到buffer里
                }

                //转成对象
                return buildInfo(buffer.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static GameInfo buildInfo(String json){
        GameInfo inf = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            int c = jsonObject.getInt("c");
            if (c == 0) {
                JSONObject gameObj = jsonObject.getJSONObject("d");

                inf = new GameInfo();
                inf.gid = gameObj.getInt("gid");
                inf.pkgName = gameObj.getString("gamePackageName");
                inf.name = gameObj.getString("gameName");
                inf.iconUrl = gameObj.getString("gameLogoUrl");
                inf.downloadUrl = gameObj.has("gameDownUrl") ? gameObj.getString("gameDownUrl") : null;
                inf.showAd = gameObj.has("showAd") ? gameObj.getInt("showAd") : 0;
            }else {
                String m = jsonObject.getString("m");
                logger.error(m);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return inf;
    }

}
