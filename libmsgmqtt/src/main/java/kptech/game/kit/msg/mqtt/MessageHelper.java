package kptech.game.kit.msg.mqtt;

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
    private String pkgName;

    @Override
    public void init(Application app, IMessageCallback callback, boolean debug) {
        Messager.setDebug(debug);
        Messager.init(app, Messager.MESSAGER_CLIENT_TYPE_ANDROID);
        messager = Messager.getInstance();
        messager.addCallback(this);
        if (callback != null){
            callbackRef = new WeakReference<>(callback);
        }
    }

    @Override
    public void connect(){
        if (messager != null){
            messager.connect();
        }
    }

    @Override
    public void destory() {
        if (messager != null){
            messager.stop();
            messager.destory();
            messager.removeCallback(this);
        }
        messager = null;
    }

    @Override
    public void setParams(Map<String, Object> params) {
        if (params != null && params.containsKey("pkgName")){
            pkgName = (String) params.get("pkgName");
        }
    }

    @Override
    public void start(String deviceId) {
        if (messager != null) {
            messager.start(deviceId);
        }
    }

    @Override
    public void stop() {
        if (messager != null) {
            messager.stop();
        }
    }

    @Override
    public void sendMessage(MessageAction event, int code, String err, String data) {
        try {
            JSONObject  obj = new JSONObject();

            String respCode= getRespCode(event);
            if (respCode != null){
                obj.put("c", respCode);
            }else {
                obj.put("c", code + "");
            }
            obj.put("p",this.pkgName);
            obj.put("t", System.currentTimeMillis() + "");
            if (data != null){
                try {
                    JSONObject dataJson = new JSONObject(data);
                    obj.put("d", dataJson);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (obj != null) {
                messager.send(obj.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("", e.getMessage());
        }
    }

    private String getRespCode(MessageAction action) {
        switch (action){
            case Login:
                return "100012";
            case Logout:
                return "100022";
            case Pay:
                return "100032";
            case Exit:
                return "100042";
            case Echo:
                return "100052";
        }
        return null;
    }

    @Override
    public void onMessage(String topic, String msg) {
        if (callbackRef != null && callbackRef.get() != null){
            callbackRef.get().onEvent(MessageEvent.onMessage,1, msg);

            if (msg == null) {
                return;
            }
            JSONObject obj = null;
            try {
                obj = new JSONObject(msg);
            } catch (Exception e) {
                Log.e("MsgManager", e.getMessage());
            }
            if (obj == null || !obj.has("c")) {
                callbackRef.get().onMessageReceived(MessageAction.Third, msg);
                return;
            }

            try {

                String event = obj.getString("c");
                String appPkgName = obj.getString("p");

                MessageAction action = null;
                String data = null;
                if ("100011".equals(event) && this.pkgName.equals(appPkgName)) {
                    action = MessageAction.Login;
                } else if ("100031".equals(event) && this.pkgName.equals(appPkgName)) {
                    action = MessageAction.Pay;
                    data = obj.getString("d");
                } else if ("100021".equals(event) && this.pkgName.equals(appPkgName)) {
                    action = MessageAction.Logout;
                } else if ("100041".equals(event) && this.pkgName.equals(appPkgName)) {
                    action = MessageAction.Exit;
                } else if ("100051".equals(event) && this.pkgName.equals(appPkgName)) {
                    action = MessageAction.Echo;
                    data = obj.getString("d");
                }

                if (action != null){
                    callbackRef.get().onMessageReceived(action, data);
                }
            } catch (JSONException e) {
                Log.e("MsgManager", e.getMessage());
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
