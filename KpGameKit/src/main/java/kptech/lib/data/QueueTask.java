package kptech.lib.data;

import android.content.Context;
import android.os.AsyncTask;

import com.kptach.lib.inter.game.APIConstants;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import kptech.game.kit.GameBoxManager;
import kptech.game.kit.model.GameBoxConfig;
import kptech.game.kit.utils.Logger;
import kptech.lib.analytic.DeviceInfo;
import kptech.lib.analytic.Event;
import kptech.lib.constants.Urls;

/**
 * 队列任务
 */
public class QueueTask extends AsyncTask<Object, Void, String> {

    private static final String TAG = "QueueTask";
    // 进入队列
    public static final String ACTION_PUSH = "PUSH";
    //退出队列
    public static final String ACTION_POP = "POP";
    //获取队列信息
    public static final String ACTION_FETCH = "FETCH";
    private final String mAction;
    private final String mSessionId;
    private final String mUserId;
    private final String mTuid;
    private String mCorpKey;
    private String mPkgName;

    private ICallback mCallback;


    public interface ICallback {
        void onResult(String jsonData);
    }

    public QueueTask(Context context, String action) {
        mAction = action;
        mSessionId = Event.getTraceId();
        mUserId = DeviceInfo.getUserId(context);
        GameBoxConfig gameBoxConfig = GameBoxManager.getInstance().getGameBoxConfig();
        if (gameBoxConfig != null) {
            mTuid = gameBoxConfig.tUid;
        } else {
            mTuid = "";
        }


    }

    public QueueTask setCallback(ICallback callback) {
        this.mCallback = callback;
        return this;
    }

    public QueueTask setCorpKey(String mCorpKey) {
        this.mCorpKey = mCorpKey;
        return this;
    }

    public QueueTask setPkgName(String pkgName) {
        this.mPkgName = pkgName;
        return this;
    }


    @Override
    protected String doInBackground(Object... params) {

        return dealRequest(mAction);
    }

    //PUSH
    private String dealRequest(String action) {
        try {
            //SessionId;
            //Uid;
            //Tuid;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", action);

            JSONObject jsonData = new JSONObject();
            jsonData.put("sessionid", (mSessionId == null ? "" : mSessionId));
            jsonData.put("uid", mUserId);
            jsonData.put("tuid", mTuid);
            jsonData.put("corpkey", mCorpKey);
            jsonData.put("pkgname", mPkgName);

            jsonObject.put("data", jsonData.toString());

            return request(jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String jsonStr) {
        try {
            if (mCallback != null) {


                if (jsonStr != null && !jsonStr.isEmpty()) {
                    JSONObject jsonResult = new JSONObject(jsonStr);
                    int code = jsonResult.optInt("code");
                    String msg = jsonResult.optString("msg");
                    if (code == 0) {
                        JSONObject dataObject = jsonResult.optJSONObject("data");
                        if (dataObject != null) {
                            int rest = dataObject.optInt("rest");
                            switch (mAction) {
                                case ACTION_PUSH:
                                    //入队
                                    mCallback.onResult(getMsgByPushCode(rest));
                                    break;
                                case ACTION_POP:
                                    //出队
                                    mCallback.onResult(generateJsonResult(APIConstants.QUEUE_RET_SUCCESS, "退出队列成功"));
                                    break;
                                case ACTION_FETCH:
                                    //队列信息  TODO 处理返回信息
                                    String info = dataObject.optString("info");
                                    mCallback.onResult(getMsgByFetchCode(rest, info));
                                    break;
                            }
                        } else {
                            String result = generateJsonResult(APIConstants.QUEUE_ERROR_DATA, "服务异常，请稍后再试");
                            mCallback.onResult(result);
                        }
                    } else {
                        String result = generateJsonResult(code, msg);
                        mCallback.onResult(result);
                    }
                } else {
                    String result = generateJsonResult(APIConstants.QUEUE_ERROR_RESULT, "服务异常，请稍后再试");
                    mCallback.onResult(result);
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mCallback != null) {
                mCallback.onResult(e.getMessage());
            }
        }

    }

    /**
     * 获取fetch数据的返回信息
     */
    private String getMsgByFetchCode(int rest, String info) {
        try{
            int code;
            String msg;
            if (rest == 1) {//当前用户不在队列
                code = APIConstants.QUEUE_NOTIN_TEAM;
                msg = "当前用户不在队列";
            } else if (rest == 2) {//当前用户在队列
                code = APIConstants.QUEUE_IN_TEAM;
                JSONObject infoObject = new JSONObject(info);
                int front = infoObject.optInt("front");
                int wttm = infoObject.optInt("wttm");
                JSONObject msgObject = new JSONObject();
                msgObject.put("front",front);
                msgObject.put("wttm", wttm);
                msg = msgObject.toString();
            } else {
                code = APIConstants.QUEUE_RET_ERROR;
                msg = "队列其他问题";
            }
            return generateJsonResult(code, msg);
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    private String getMsgByPushCode(int restCode) {
        //code 0 操作成功
        //code 10000 进入队列错误，该用户已经在队列中
        //code 10001  队列不存在
        String msg;
        int code;
        switch (restCode) {
            case 0:
                code = APIConstants.QUEUE_RET_SUCCESS;
                msg = "操作成功";
                break;
            case 10000:
                code = APIConstants.QUEUE_IN_TEAM;
                msg = "该用户已经在队列中";
                break;
            case 10001:
                code = APIConstants.QUEUE_ERROR_EXIST;
                msg = "队列不存在";
                break;
            default:
                code = APIConstants.QUEUE_RET_ERROR;
                msg = "队列其他错误";
                break;
        }
        return generateJsonResult(code, msg);
    }


    private String generateJsonResult(int code, String msg) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", code);
            jsonObject.put("msg", msg);
            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";

    }


    private String request(String jsonStr) {
        try {
            String requestUrl = Urls.getTraceUrlPAAS() + "?f=QUEUE&p=" + jsonStr;
            URL url = new URL(requestUrl);
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("GET");//get 请求
            postConnection.setConnectTimeout(1000 * 10);
            postConnection.setReadTimeout(1000 * 10);
            postConnection.setDoInput(true);//允许从服务端读取数据

            final StringBuilder buffer = new StringBuilder();
            int code = postConnection.getResponseCode();
            Logger.info(TAG, "response code:" + code);
            if (code == 200) {//成功
                InputStream inputStream = postConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;//一行一行的读取
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
