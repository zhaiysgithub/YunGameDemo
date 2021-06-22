package kptech.game.kit.msg;

import android.app.Activity;
import android.app.Application;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import kptech.cloud.kit.msg.Messager;
import kptech.game.kit.BuildConfig;
import kptech.game.kit.msg.mqtt.MsgSuper;
import kptech.lib.constants.SharedKeys;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;

public class MsgManager2 extends MsgSuper {

    /*private MsgManager2(Activity activity, String corpId, String pkgName){
        super(activity, corpId, pkgName);
    }*/
    private static boolean inited = false;
    private static MsgManager2 mMsgManager2;

    public static MsgManager2 getInstance() {
        return mMsgManager2;
    }

    private Messager.ICallback mCallback =  new Messager.ICallback() {
        @Override
        public void onMessage( String msg) {
            Logger.info("MsgManager", "onMessage: " + msg);
            sendHandle(msg);
        }

        @Override
        public void onConnect(int code, String s) {
            Logger.info("MsgManager", "onConnect code: " + code + " msg: " + s);
        }

        @Override
        public void onClose(int code, String s) {
            Logger.info("MsgManager", "onClose code: " + code + " msg: " + s);
        }

        @Override
        public void onFailure(int code, String s) {
            Logger.error("MsgManager", "onFailure code: " + code + " msg: " + s);
        }
    };
    @Override
    public void init(Application application, String corpId) {
        Messager.init(application);
        inited = true;
    }

    @Override
    public void setDebug(boolean debug) {
        Messager.setDebug(debug);
    }

    @Override
    public void start(Activity activity, String corpId, String padCode, String pkgName, String gameId, String gameName) {
        if (!inited) {
            Logger.error("MsgManager", "kpckit messager not initialized");
            return;
        }

        if (padCode == null || "".equals(padCode)) {
            Logger.error("MsgManager", "padcode is null");
            return;
        }

        if (padCode.toLowerCase().startsWith("vm")) {
            padCode = padCode.substring(2,padCode.length());
        }

        if (mMsgManager2 == null){
            mMsgManager2 = new MsgManager2(activity, corpId, pkgName);
        }
        Messager.getInstance().addCallback(mCallback);

        if (mMsgManager2 != null){
            mMsgManager2.setPadCode("VM"+padCode);
            mMsgManager2.setGameId(gameId);
            mMsgManager2.setGameName(gameName);
            mMsgManager2.setPkgName(pkgName);
        }

        String wsurl = null;
        try {
            wsurl = ProferencesUtils.getString(activity, SharedKeys.KEY_GAME_APP_WSURL, null);
        } catch (Exception e) {
        }

        if (padCode != null) {
//            Messager.getInstance().start(1, padCode);
            Messager.getInstance().startWithUri(wsurl, 1, padCode);
        }

    }

    @Override
    public void sendMessage(String msg) {
        try {
            Messager.getInstance().send(msg);
        } catch (Exception e) {
            Logger.error("MsgManager", e.getMessage());
        }
    }

    @Override
    public void stop() {
        try {
            if (Messager.getInstance().isConnected()) {
                Messager.getInstance().close();
            }

            if (mMsgManager2 != null) {
                Messager.getInstance().removeCallback(mCallback);
                mMsgManager2.destory();
                mMsgManager2 = null;
            }
        }catch (Exception e){
            Logger.error("MsgManager",e.getMessage());
        }
    }


    private void sendHandle(String msg){
        if (msg == null) {
            return;
        }
        JSONObject obj = null;
        try {
            obj = new JSONObject(msg);
        }catch (Exception e){
            Logger.error("MsgManager",e.getMessage());
        }

        if (obj == null || !obj.has("event")) {
            if (mReceiverRef!=null && mReceiverRef.get()!=null){
                mReceiverRef.get().onMessageReceived(msg);
            }
            return;
        }

        try {

            String event = obj.getString("event");
            if ("login".equals(event)){
                mHandler.sendEmptyMessage(MsgHandler.MSG_LOGIN);
            }else if ("reLogin".equals(event)){
                mHandler.sendEmptyMessage(MsgHandler.MSG_RELOGIN);
            }else if ("pay".equals(event)){
                mHandler.sendMessage(Message.obtain(mHandler,MsgHandler.MSG_PAY,msg));
            }else if ("logout".equals(event)){
                mHandler.sendEmptyMessage(MsgHandler.MSG_LOGOUT);
            }else if ("exit".equals(event)){
                if (mReceiverRef!=null && mReceiverRef.get()!=null){
                    mReceiverRef.get().onMessageReceived(event, null);
                }
            }else if ("echo".equals(event)){
                echo(obj);
            }
        } catch (JSONException e) {
            Logger.error("MsgManager",e.getMessage());
        }
    }

    private void sendInfo(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("event","respinf");
            obj.put("verName", BuildConfig.VERSION_NAME);
            obj.put("verCode", BuildConfig.VERSION_CODE);
            obj.put("corpKey", this.mCorpKey);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Messager.getInstance().send(obj.toString());
    }

    @Override
    public void onLogout() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("event","logout");
            obj.put("result", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Messager.getInstance().send(obj.toString());
    }

    @Override
    public void onLogin(int code, String err, Map<String, Object> map) {
        JSONObject obj = null;
        try {
            if (map!=null){
                obj = new JSONObject(map);
            }else {
                obj = new JSONObject();
            }

            obj.put("event","login");
            obj.put("result", code);
            if (code == 0){
                obj.put("error", err);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            if (obj == null){
                obj = new JSONObject();
            }
        }
        if (obj != null){
            Messager.getInstance().send(obj.toString());
        }
    }

    @Override
    public void onPay(int code, String err, Map<String, Object> map) {
        JSONObject obj = null;
        try {
            if (map!=null){
                obj = new JSONObject(map);
            }else {
                obj = new JSONObject();
            }

            obj.put("event","payover");
            obj.put("result", code);
            if (code == 0){
                obj.put("error", err);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            if (obj == null){
                obj = new JSONObject();
            }
        }
        if (obj != null){
            Messager.getInstance().send(obj.toString());
        }
    }

    private void echo(JSONObject obj){
        if (obj == null){
            return;
        }

        try {
            //读取cp sdk信息
            if (obj.has("cp")){
                JSONObject cpObj = obj.getJSONObject("cp");
                String appKey = cpObj.has("appkey") ? cpObj.getString("appkey") : null;
                String ver = cpObj.has("ver") ? cpObj.getString("ver") : null;
            }

            //渠道信息
            JSONObject chObj = new JSONObject();
            chObj.put("ver", BuildConfig.VERSION_NAME);
            chObj.put("corpkey", this.mCorpKey);

            obj.put("ch", chObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Messager.getInstance().send(obj.toString());
    }

}
