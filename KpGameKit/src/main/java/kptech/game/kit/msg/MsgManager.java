package kptech.game.kit.msg;

import android.app.Activity;
import android.app.Application;
import android.os.Message;

import com.kptach.lib.inter.msg.IMessageCallback;
import com.kptach.lib.inter.msg.IMessageHelper;
import com.kptach.lib.inter.msg.MessageAction;
import com.kptach.lib.inter.msg.MessageEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import kptech.game.kit.BuildConfig;
import kptech.game.kit.GameBoxManager;
import kptech.game.kit.utils.Logger;

public class MsgManager implements IMessageCallback, MsgHandler.ICallback {
    private static final String TAG = "MsgManager";

    protected MsgHandler mHandler;
    protected String mCorpKey;
    protected WeakReference<IMsgReceiver> mReceiverRef;
    private IMessageHelper helper = null;

    private static MsgManager mMsgManager;

    public static void init(Application application, boolean isDebug){
        if (mMsgManager == null) {
            synchronized(MsgManager.class) {
                if (mMsgManager == null) {
                    mMsgManager = new MsgManager(application, isDebug);
                }
            }
        }
    }

    public static MsgManager getInstance(){
        return mMsgManager;
    }

    private MsgManager(Application application, boolean debug){
        //判断是否存在通讯类
        helper = getMessageHelper();
        if (helper == null){
            Logger.error(TAG, "Init MsgManager helper is null");
            return;
        }

        helper.init(application, this, debug);
        Logger.info(TAG, "Init success " + helper);
    }

    public void setMessageReceiver(IMsgReceiver receiver) {
        if (receiver != null) {
            mReceiverRef = new WeakReference<>(receiver);
        }
    }

    public void connect(){
        if (helper != null){
            helper.connect();
        }
    }

    public void disconnect(){

    }

    public void start(Activity activity, String corpId, String padCode, String pkgName, String gameId, String gameName){
        Logger.info(TAG, "start " + corpId + " | " + padCode + " | " + pkgName);
        if (helper == null){
            Logger.error(TAG, "start return, helper is null");
            return;
        }
        this.mCorpKey = corpId;
        this.mHandler = new MsgHandler(activity, corpId, pkgName);
        this.mHandler.setCallback(this);
        this.mHandler.setGameId(gameId);
        this.mHandler.setPadCode(padCode);
        this.mHandler.setGameName(gameName);

        HashMap<String, Object> params = new HashMap<>();
        params.put("pkgName", pkgName);
        helper.setParams(params);
        helper.start(padCode);
    }

    public void stop(){
        Logger.info(TAG, "stop");

        try {
            if (helper != null){
                helper.stop();
            }
        } catch (Exception e){
            Logger.error(TAG, "stop " + e.getMessage());
        }

        if (this.mHandler != null){
            this.mHandler.destory();
            this.mHandler = null;
        }

        try {
            if (mReceiverRef != null) {
                mReceiverRef.clear();
                mReceiverRef = null;
            }
        } catch (Exception e) {
            Logger.error(TAG, "stop " + e.getMessage());
        }
    }

    public void sendMessage(String msg){
        sendMessage(msg, 1);
    }

    public void sendMessage(String msg, int code){
        Logger.info(TAG, "sendMessage " + msg);
        if (helper == null){
            Logger.error(TAG, "sendMessage return, helper is null");
            return;
        }
        helper.sendMessage(MessageAction.Third, code, null, msg);
    }

    @Override
    public void onLogout() {
        Logger.info(TAG, "sendLogout ");
        if (helper == null){
            Logger.error(TAG, "sendLogout return, helper is null");
            return;
        }
        helper.sendMessage(MessageAction.Logout, 1, null, null);
    }

    @Override
    public void onLogin(int code, String err, Map<String, Object> map) {
        Logger.info(TAG, "sendLogin , code: " + code + (err != null ? ", err: "+err : ""));
        if (helper == null){
            Logger.error(TAG, "sendLogin return, helper is null");
            return;
        }

        String data = null;
        if (map != null){
            JSONObject jsonObj = new JSONObject(map);
            data = jsonObj.toString();
        }
        helper.sendMessage(MessageAction.Login, code, err, data);
    }

    @Override
    public void onPay(int code, String err, Map<String, Object> map) {
        Logger.info(TAG, "sendPay , code: " + code + (err != null ? ", err: "+err : ""));
        if (helper == null){
            Logger.error(TAG, "sendPay return, helper is null");
            return;
        }

        String data = null;
        if (map != null){
            JSONObject jsonObj = new JSONObject(map);
            data = jsonObj.toString();
        }
        helper.sendMessage(MessageAction.Pay, code, err, data);
    }

    private void onEcho(String data){
        try {
            JSONObject reqObj = null;
            if (data != null){
                reqObj = new JSONObject(data);
                //读取cp sdk信息
                if (reqObj.has("cp")){
                    JSONObject cpObj = reqObj.getJSONObject("cp");
                    String appKey = cpObj.has("appkey") ? cpObj.getString("appkey") : null;
                    String ver = cpObj.has("ver") ? cpObj.getString("ver") : null;
                }
            }

            if (reqObj == null){
                Logger.error(TAG, "sendEcho return, data is null");
                return;
            }

            if (helper == null){
                Logger.error(TAG, "sendEcho return, helper is null");
                return;
            }

            //渠道信息
            JSONObject chObj = new JSONObject();
            chObj.put("ver", BuildConfig.VERSION_NAME);
            chObj.put("corpkey", this.mCorpKey);

            reqObj.put("ch", chObj);

            helper.sendMessage(MessageAction.Echo, 1, null, reqObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(MessageAction event, String data) {
        switch (event){
            case Login:
                mHandler.sendEmptyMessage(MsgHandler.MSG_LOGIN);
                break;
            case Logout:
                mHandler.sendEmptyMessage(MsgHandler.MSG_LOGOUT);
                break;
            case Pay:
                mHandler.sendMessage(Message.obtain(mHandler,MsgHandler.MSG_PAY, data));
                break;
            case Exit:
                if (mReceiverRef!=null && mReceiverRef.get()!=null){
                    mReceiverRef.get().onMessageReceived("exit", null);
                }
                break;
            case Echo:
                onEcho(data);
                break;
        }
    }

    @Override
    public void onEvent(MessageEvent event, int code, String msg) {
        Logger.info(TAG,  event.name() + " code: " + code + " msg: " + msg);
    }

    private static final String WS_HELPER = "kptech.game.kit.msg.ws.MessageHelper";
    private static final String MQTT_HELPER = "kptech.game.kit.msg.mqtt.MessageHelper";
    private IMessageHelper getMessageHelper(){
        try {
            IMessageHelper helper = (IMessageHelper) newInstance(MQTT_HELPER, null, null);
            return helper;
        } catch (Exception e) {
            Logger.error(TAG, "Get message helper " + MQTT_HELPER + ", error: " + e.getMessage());
        }
        try {
            IMessageHelper helper = (IMessageHelper) newInstance(WS_HELPER, null, null);
            return helper;
        } catch (Exception e) {
            Logger.error(TAG, "Get message helper " + WS_HELPER + ", error: " + e.getMessage());
        }
        return null;
    }

    private static Object newInstance(String className, Class[] argsClass, Object[] args) throws Exception {
        Class newoneClass = Class.forName(className);
        Constructor cons = newoneClass.getConstructor(argsClass);
        return cons.newInstance(args);
    }
}
