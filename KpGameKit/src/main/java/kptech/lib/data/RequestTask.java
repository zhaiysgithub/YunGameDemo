package kptech.lib.data;

import com.kptach.lib.inter.game.APIConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import kptech.game.kit.BuildConfig;
import kptech.game.kit.GameInfo;
import kptech.game.kit.utils.JsonUtils;
import kptech.lib.constants.Urls;
import kptech.game.kit.utils.Logger;

public class RequestTask {
    private static final String TAG = "RequestTask";

    public static List<GameInfo> queryGameList(String corpKey, int page, int limit){
        String str = Urls.getEnvUrl(Urls.GET_GAME_LIST) +  "/" + corpKey;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append("?version=" + BuildConfig.VERSION_NAME);
            sb.append("&page="+page);
            sb.append("&pageSize="+limit);
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

                List<GameInfo> list = null;
                try {
                    //转成对象
                    JSONObject jsonObject = new JSONObject(buffer.toString());
                    int c = jsonObject.getInt("c");
                    if (c == 0) {
                        list = new ArrayList<>();
                        JSONObject dObj = jsonObject.getJSONObject("d");
                        JSONArray gameListObj = dObj.getJSONArray("gameList");
//                        JSONArray gameListObj = jsonObject.getJSONArray("d");
                        for (int i = 0; i < gameListObj.length(); i++) {
                            JSONObject gameObj = gameListObj.getJSONObject(i);
                            GameInfo gameInfo = buildInfo(gameObj);
                            if (gameInfo != null){
                                list.add(gameInfo);
                            }
                        }
                    }else {
                        String m = jsonObject.getString("m");
                        Logger.error(TAG,m);
                    }
                }catch (Exception e){
                    Logger.error(TAG,e.getMessage());
                }

                return list;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<GameInfo> queryGameListByPass3(String corpKey, int page, int limit){
        String gameUrl = Urls.getRequestDeviceUrl(Urls.URL_PASS_GAMES);

        BufferedReader reader = null;
        OutputStream writeStream = null;
        InputStreamReader isr = null;
        try{

            JSONObject jsonParams = new JSONObject();
            jsonParams.put("corpKey",corpKey);
            String jsonStr = jsonParams.toString();

            URL url = new URL(gameUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(1000*10);
            conn.setReadTimeout(1000*10);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("accept", "application/json");
            byte[] writeBytes = jsonStr.getBytes();
            conn.setRequestProperty("Content-Length", String.valueOf(writeBytes.length));
            writeStream = conn.getOutputStream();
            writeStream.write(writeBytes);
            writeStream.flush();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200){
                isr = new InputStreamReader(conn.getInputStream());
                reader = new BufferedReader(isr);
                String result = reader.readLine();
                Logger.info(TAG, "queryGameListByPass3=" + result);

                if (result != null && !result.isEmpty()){
                    JSONObject resultJson = new JSONObject(result);
                    int code = JsonUtils.optInt(resultJson,"code");
                    if (code == 0){
                        JSONArray dataJsonArray = resultJson.getJSONArray("data");
                        int num = dataJsonArray.length();
                        List<GameInfo> list = new ArrayList<>();
                        if (num > 0){
                            for(int i = 0; i< num; i++){
                                String gameJsonStr = dataJsonArray.get(i).toString();
                                JSONObject gameJsonObject = new JSONObject(gameJsonStr);
                                String name = JsonUtils.optString(gameJsonObject,"name");
                                String pkgName = JsonUtils.optString(gameJsonObject,"pkgName");
                                String appUrl = JsonUtils.optString(gameJsonObject,"appUrl");
                                GameInfo gameInfo = new GameInfo();
                                gameInfo.name = name;
                                gameInfo.pkgName = pkgName;
                                gameInfo.downloadUrl = appUrl;
                                list.add(gameInfo);
                            }
                        }
                        return list;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (writeStream != null) {
                    writeStream.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

//    private static List<GameInfo> buildList(String json){
//        List<GameInfo> list = null;
//        try {
//            JSONObject jsonObject = new JSONObject(json);
//            int c = jsonObject.getInt("c");
//            if (c == 0){
//                list = new ArrayList<>();
//
//                JSONArray gameListObj = jsonObject.getJSONArray("d");
//                for (int i = 0; i < gameListObj.length(); i++) {
//                    try {
//                        GameInfo inf = new GameInfo();
//                        JSONObject gameObj = gameListObj.getJSONObject(i);
//                        inf.gid = gameObj.getInt("gid");
//                        inf.kpGameId = gameObj.getString("gameId");
//                        inf.pkgName =  gameObj.getString("gamePackageName");
////                        JSONObject gameInfObj = gameObj.getJSONObject("gameInfo");
//                        inf.name = gameObj.getString("gameName");
//                        inf.iconUrl = gameObj.getString("gameLogoUrl");
//                        inf.downloadUrl = gameObj.has("gameDownUrl") ? gameObj.getString("gameDownUrl") :  null;
//                        list.add(inf);
//                    }catch (Exception e){
//                        logger.error(e.getMessage());
//                    }
//                }
//            }else {
//                String m = jsonObject.getString("m");
//                logger.error(m);
//            }
//        }catch (Exception e){
//            logger.error(e.getMessage());
//        }
//
//        return list;
//    }


    public static GameInfo queryGameInfo(String corpKey, String pkg, String pass ){
        String str = Urls.getEnvUrl(Urls.GET_GAME_INFO);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append("?corpKey=" + corpKey);
            sb.append("&paas=" + pass);
            sb.append("&pkgName=" + pkg);
            sb.append("&version=" + BuildConfig.VERSION_NAME);
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

                GameInfo inf = null;
                try {
                    JSONObject jsonObject = new JSONObject(buffer.toString());
                    int c = jsonObject.getInt("c");
                    if (c == 0) {
                        JSONObject gameObj = jsonObject.getJSONObject("d");
                        inf = buildInfo(gameObj);
                    }else {
                        String m = jsonObject.getString("m");
                        Logger.error(TAG,m);
                    }
                }catch (Exception e){
//                    e.printStackTrace();
                    Logger.error(TAG,e.getMessage());
                }
                //转成对象
                return inf;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    private static GameInfo buildInfo(String json){
//        GameInfo inf = null;
//        try {
//            JSONObject jsonObject = new JSONObject(json);
//            int c = jsonObject.getInt("c");
//            if (c == 0) {
//                JSONObject gameObj = jsonObject.getJSONObject("d");
//
//                inf = new GameInfo();
//                inf.gid = gameObj.getInt("gid");
//                inf.kpGameId = gameObj.getString("gameId");
//                inf.pkgName = gameObj.getString("gamePackageName");
//                inf.name = gameObj.getString("gameName");
//                inf.iconUrl = gameObj.getString("gameLogoUrl");
//                inf.downloadUrl = gameObj.has("gameDownUrl") ? gameObj.getString("gameDownUrl") : null;
//                inf.showAd = gameObj.has("showAd") ? gameObj.getInt("showAd") : 0;
//            }else {
//                String m = jsonObject.getString("m");
//                logger.error(m);
//            }
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return inf;
//    }


    public static List<GameInfo> queryGameExitList(String corpKey, String gameId ){
        String str = Urls.getEnvUrl(Urls.GET_GAME_EXIT_LIST);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append("?corpKey=" + corpKey);
            sb.append("&gameId=" + gameId);
            sb.append("&version=" + BuildConfig.VERSION_NAME);
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

                List<GameInfo> list = null;
                try {
                    //转成对象
                    JSONObject jsonObject = new JSONObject(buffer.toString());
                    int c = jsonObject.getInt("c");
                    if (c == 0) {
                        list = new ArrayList<>();
                        JSONArray gameListObj = jsonObject.getJSONArray("d");
                        for (int i = 0; i < gameListObj.length(); i++) {
                            JSONObject gameObj = gameListObj.getJSONObject(i);
                            GameInfo gameInfo = buildInfo(gameObj);
                            if (gameInfo != null){
                                list.add(gameInfo);
                            }
                        }
                    }else {
                        String m = jsonObject.getString("m");
                        Logger.error(TAG,m);
                    }
                }catch (Exception e){
                    Logger.error(TAG,e.getMessage());
                }

                return list;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static GameInfo buildInfo(JSONObject gameObj){
        GameInfo inf = null;
        try {
            inf = new GameInfo();
            inf.gid = gameObj.getInt("gid");
            inf.kpGameId = gameObj.getString("gameId");
            inf.pkgName = gameObj.getString("gamePackageName");
            inf.name = gameObj.getString("gameName");
            inf.iconUrl = gameObj.has("gameLogoUrl") ? gameObj.getString("gameLogoUrl") : null;
            inf.downloadUrl = gameObj.has("gameDownUrl") ? gameObj.getString("gameDownUrl") : null;
            inf.showAd = gameObj.has("showAd") ? gameObj.getInt("showAd") : 0;
            inf.enableDownload = gameObj.has("showDown") ? gameObj.getInt("showDown") : 1;
            inf.coverUrl = gameObj.has("gameAppCoverImgUrl") ? gameObj.getString("gameAppCoverImgUrl") : null;
            inf.recoverCloudData = gameObj.has("recoverCloudData") ? gameObj.getInt("recoverCloudData") : 1;
            inf.kpUnionGame = gameObj.has("isKpJointOpe") ? gameObj.getInt("isKpJointOpe") : 0;
            inf.mockSleepTime = gameObj.has("mockSleepTime") ? gameObj.getInt("mockSleepTime") : -1;
            inf.exitRemind = gameObj.has("exitRemind") ? gameObj.getString("exitRemind") : null;
            inf.enterRemind = gameObj.has("enterRemind") ? gameObj.getString("enterRemind") : null;
            String useSdkName = gameObj.has("useSDK") ? gameObj.optString("useSDK") : GameInfo.SdkType.DEFAULT.name();
            try {
                inf.useSDK = GameInfo.SdkType.valueOf(useSdkName);
            }catch (Exception e){
                e.printStackTrace();
            }
            if (gameObj.has("downloadExt")){
                HashMap<String,String> ext = new HashMap<>();
                try {
                    JSONObject extObj = gameObj.getJSONObject("downloadExt");
                    Iterator<String> keys = extObj.keys();
                    while (keys.hasNext()){
                        String key = keys.next();
                        String value = extObj.getString(key);
                        ext.put(key,value);
                    }
                }catch (Exception e){}
                if (ext.size() > 0){
                    inf.ext = ext;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return inf;
    }
}
