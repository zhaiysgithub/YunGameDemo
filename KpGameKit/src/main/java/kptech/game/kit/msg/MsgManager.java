package kptech.game.kit.msg;

import android.app.Activity;
import android.app.Application;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Map;

import kptech.cloud.kit.msg.Messager;
import kptech.lib.constants.SharedKeys;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;

public class MsgManager implements Messager.ICallback, MsgHandler.ICallback {
//    private static final Logger logger = new Logger("MsgManager") ;

//    public interface IMessageReceiver {
//        void onMessageReceived(String msg);
//    }

    private static boolean inited = false;
    private static MsgManager mMsgManager;
    public static void init(Application application, String corpId){
        Messager.init(application);
        inited = true;
    }
    public static void setDebug(boolean debug){
        Messager.setDebug(debug);
    }
    public static MsgManager getInstance(){
        return mMsgManager;
    }

    public static void start(Activity activity, String corpId, String padCode, String pkgName, String gameId, String gameName){
        if (!inited){
            Logger.error("MsgManager", "kpckit messager not initialized");
            return;
        }

        if (padCode == null || "".equals(padCode)){
            Logger.error("MsgManager", "padcode is null");
            return;
        }

        if (padCode.toLowerCase().startsWith("vm")) {
            padCode = padCode.substring(2,padCode.length());
        }

        if (mMsgManager == null){
            mMsgManager = new MsgManager(activity, corpId, pkgName);
        }
        Messager.getInstance().addCallback(mMsgManager);

        if (mMsgManager != null){
            mMsgManager.setPadCode("VM"+padCode);
            mMsgManager.setGameId(gameId);
            mMsgManager.setGameName(gameName);
            mMsgManager.setPkgName(pkgName);
        }

        String wsurl = null;
        try {
            wsurl = ProferencesUtils.getString(activity, SharedKeys.KEY_GAME_APP_WSURL, null);
        }catch (Exception e){}

        if (padCode!=null){
//            Messager.getInstance().start(1, padCode);
            Messager.getInstance().startWithUri(wsurl,1, padCode);
        }

    }

    public static void sendMessage(String msg){
        try {
            Messager.getInstance().send(msg);
        }catch (Exception e){
            Logger.error("MsgManager",e.getMessage());
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
            Logger.error("MsgManager",e.getMessage());
        }
    }


    private MsgHandler mHandler;
    private MsgManager(Activity activity, String corpId, String pkgName){
        this.mHandler = new MsgHandler(activity, corpId, pkgName);
        this.mHandler.setCallback(this);
    }


    private void destory(){
        this.mHandler.destory();
        this.mHandler = null;
        try {
            if (mReceiverRef != null){
                mReceiverRef.clear();
                mReceiverRef = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
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

    private WeakReference<IMsgReceiver>  mReceiverRef;
    public void setMessageReceiver(IMsgReceiver receiver){
        if (receiver != null){
            mReceiverRef = new WeakReference<>(receiver);
        }
    }

    @Override
    public void onMessage(String msg) {
        Logger.info("MsgManager","onMessage: " + msg);
        sendHandle(msg);
    }

    @Override
    public void onConnect(int code, String s) {
        Logger.info("MsgManager","onConnect code: " + code +" msg: " + s);
    }

    @Override
    public void onClose(int code, String s) {
        Logger.info("MsgManager","onClose code: " + code + " msg: " + s);
    }

    @Override
    public void onFailure(int code, String s) {
        Logger.error("MsgManager","onFailure code: " + code + " msg: " + s);
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
            }
        } catch (JSONException e) {
            Logger.error("MsgManager",e.getMessage());
        }
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

}
