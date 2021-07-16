package kptech.game.kit.msg.ws;

import android.app.Application;
import android.util.Log;

import com.kptach.lib.inter.msg.IMessageCallback;
import com.kptach.lib.inter.msg.IMessageHelper;
import com.kptach.lib.inter.msg.MessageAction;
import com.kptach.lib.inter.msg.MessageEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Map;

import kptech.cloud.kit.msg.Messager;

public class MessageHelper implements IMessageHelper, Messager.ICallback {

    private Messager messager;
    private WeakReference<IMessageCallback> callbackRef;

    @Override
    public void init(Application app, IMessageCallback callback, boolean debug) {
        Messager.setDebug(debug);
        Messager.init(app);
        messager = Messager.getInstance();
        messager.addCallback(this);
        if (callback != null){
            callbackRef = new WeakReference<>(callback);
        }
    }

    @Override
    public void connect() {

    }

    @Override
    public void destory() {
        if (messager != null){
            messager.removeCallback(this);
            messager.onDestory();
        }
        messager = null;
    }

    @Override
    public void setParams(Map<String, Object> params) {

    }

    @Override
    public void start(String padCode) {
        if (messager != null && padCode != null){
            if (padCode.toLowerCase().startsWith("vm")) {
                padCode = padCode.substring(2,padCode.length());
            }
            messager.startWithUri(null, 1, padCode);
        }
    }

    @Override
    public void stop() {
        if (messager != null){
            messager.close();
        }
    }

    @Override
    public void sendMessage(MessageAction event, int resultCode, String err, String data) {
        if (messager != null){
            JSONObject obj = null;
            try {
                if (data!=null){
                    obj = new JSONObject(data);
                }else {
                    obj = new JSONObject();
                }

                String eventStr = getEventStr(event);
                if (eventStr != null){
                    obj.put("event",eventStr);
                    obj.put("result", resultCode);
                    if (err != null){
                        obj.put("error", err);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                if (obj == null){
                    obj = new JSONObject();
                }
            }

            messager.send(obj.toString());
        }
    }

    private String getEventStr(MessageAction action){
        switch (action){
            case Login:
                return "login";
            case Logout:
                return "logout";
            case Pay:
                return "pay";
            case Exit:
                return "exit";
            case Third:
                return "third";
        }
        return null;
    }

    @Override
    public void onMessage(String msg) {
        if (callbackRef != null && callbackRef.get() != null){
            callbackRef.get().onEvent(MessageEvent.onMessage, 1, msg);

            if (msg == null) {
                return;
            }
            JSONObject obj = null;
            try {
                obj = new JSONObject(msg);
            }catch (Exception e){
                Log.e("MsgManager",e.getMessage());
            }

            if (obj == null || !obj.has("event")) {
                callbackRef.get().onMessageReceived(MessageAction.Third, msg);
                return;
            }

            try {
                MessageAction action = null;
                String data = null;

                String event = obj.getString("event");
                if ("login".equals(event)){
                    action = MessageAction.Login;
                }else if ("pay".equals(event)){
                    action = MessageAction.Pay;
                    data = msg;
                }else if ("logout".equals(event)){
                    action = MessageAction.Logout;
                }else if ("exit".equals(event)){
                    action = MessageAction.Exit;
                }else if ("echo".equals(event)){
                    action = MessageAction.Echo;
                    data = msg;
                }

                if (action != null){
                    callbackRef.get().onMessageReceived(action, data);
                }

            } catch (JSONException e) {
                Log.e("MsgManager",e.getMessage());
            }
        }
    }

    @Override
    public void onConnect(int i, String s) {
        if (callbackRef != null && callbackRef.get() != null){
            callbackRef.get().onEvent(MessageEvent.onConnect,i,s);
        }
    }

    @Override
    public void onClose(int i, String s) {
        if (callbackRef != null && callbackRef.get() != null){
            callbackRef.get().onEvent(MessageEvent.onClose,i, s);
        }
    }

    @Override
    public void onFailure(int i, String s) {
        if (callbackRef != null && callbackRef.get() != null){
            callbackRef.get().onEvent(MessageEvent.onFailure,i, s);
        }
    }

}
