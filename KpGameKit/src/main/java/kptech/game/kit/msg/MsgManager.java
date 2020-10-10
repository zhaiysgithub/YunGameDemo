package kptech.game.kit.msg;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import kptech.cloud.kit.msg.Messager;
import kptech.game.kit.GameBoxManager;
import kptech.game.kit.data.RequestLoginTask;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.view.LoginDialog;
import kptech.game.kit.view.PayDialog;

public class MsgManager implements Messager.ICallback, MsgHandler.ICallback {
    private static final Logger logger = new Logger("MsgManager") ;

    private static boolean inited = false;
    private static MsgManager mMsgManager;
    public static void init(Application application, String corpId){
        Messager.init(application);
        inited = true;
    }
    public static void setDebug(boolean debug){
        Messager.setDebug(debug);
    }

    public static void start(Activity activity, String corpId, String token, String pkgName, String gameId, String gameName){
        if (!inited){
            logger.error("kpckit messager not initialized");
            return;
        }

        if (mMsgManager == null){
            mMsgManager = new MsgManager(activity, corpId, pkgName);
        }
        Messager.getInstance().addCallback(mMsgManager);


        String padCode = null;
        try {
            JSONObject obj = new JSONObject(token);
            JSONObject tokenObj =  new JSONObject(obj.getString("token"));
            String deviceId = tokenObj.getString("deviceId");

            if (deviceId!=null && deviceId.startsWith("VM")){
                padCode = deviceId.substring(2,deviceId.length());
            }
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }


        if (mMsgManager != null){
            mMsgManager.setPadCode("VM"+padCode);
            mMsgManager.setGameId(gameId);
            mMsgManager.setGameName(gameName);
            mMsgManager.setPkgName(pkgName);
        }

        if (padCode!=null){
            Messager.getInstance().start(1, padCode);
        }

    }

    public static void stop(){
        try {
            if (Messager.getInstance().isConnected()){
                Messager.getInstance().close();
            }

            if (mMsgManager!=null){
                Messager.getInstance().removeCallback(mMsgManager);
                mMsgManager.destory();
                mMsgManager = null;
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }


    private MsgHandler mHandler;
    private MsgManager(Activity activity, String corpId, String pkgName){
        this.mHandler = new MsgHandler(activity, corpId, pkgName);
        this.mHandler.setCallback(this);
    }


    private void destory(){
        this.mHandler.setCallback(null);
        this.mHandler = null;
    }

    private void setPadCode(String padCode){
        if (mHandler!=null){
            mHandler.setPadCode(padCode);
        }
    }

    private void setGameId(String gameId){
        if (mHandler!=null){
            mHandler.setGameId(gameId);
        }
    }

    private void setGameName(String gameName){
        if (mHandler!=null){
            mHandler.setGameName(gameName);
        }
    }

    private void setPkgName(String pkgName){
        if (mHandler!=null){
            mHandler.setPkgName(pkgName);
        }
    }

    @Override
    public void onMessage(String msg) {
        logger.info("onMessage: " + msg);
        sendHandle(msg);
    }

    @Override
    public void onConnect(int code, String s) {
        logger.info("onConnect code: " + code +" msg: " + s);
    }

    @Override
    public void onClose(int code, String s) {
        logger.info("onClose code: " + code + " msg: " + s);
    }

    @Override
    public void onFailure(int code, String s) {
        logger.error("onFailure code: " + code + " msg: " + s);
    }

    private void sendHandle(String msg){
        if (msg == null) {
            return;
        }

        try {
            JSONObject obj = new JSONObject(msg);
            String event = obj.getString("event");
            if ("login".equals(event)){
                mHandler.sendEmptyMessage(MsgHandler.MSG_LOGIN);
            }else if ("reLogin".equals(event)){
                mHandler.sendEmptyMessage(MsgHandler.MSG_RELOGIN);
            }else if ("pay".equals(event)){
//                String proCode = obj.getString("productcode");
//                String orderId = obj.getString("orderID");
//                HashMap<String,String> map = new HashMap();
//                map.put("productcode",proCode);
//                map.put("orderID",orderId);
                mHandler.sendMessage(Message.obtain(mHandler,MsgHandler.MSG_PAY,msg));
            }
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void onLogin(int code, String msg, Map<String, Object> map) {
        if (code != 1){
            return;
        }

        JSONObject obj = new JSONObject(map);
        try {
            obj.put("event","login");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Messager.getInstance().send(obj.toString());
    }

    @Override
    public void onPay(int code, String msg, Map<String, Object> map) {
        if (code != 1){
            return;
        }

        JSONObject obj = new JSONObject();
        try {
            obj.put("event","payover");
            obj.put("result", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Messager.getInstance().send(obj.toString());
    }
}
