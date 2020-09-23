package kptech.game.kit.msg;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
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

public class MsgManager {
    private static final Logger logger = new Logger("MsgManager") ;

    private static volatile MsgManager instance = null;
    private static volatile Activity mActivity = null;

    private static boolean inited = false;

    private String mCorpId;
    public static void init(Application application, String corpId){
        Messager.init(application);
        inited = true;
        if (instance == null){
            synchronized(GameBoxManager.class) {
                if (instance == null) {
                    instance = new MsgManager(application, corpId);
                }
            }
        }
    }
    public static void setDebug(boolean debug){
        Messager.setDebug(debug);
    }

    private MsgManager(Context context, String corpId){
        Messager.getInstance().addCallback(mCallback);
        mCorpId = corpId;
    }


    public static void start(Activity activity, String token){
        if (!inited){
            logger.error("kpckit messager not initialized");
            return;
        }

        mActivity = activity;

        String padCode = null;
        try {
            JSONObject obj = new JSONObject(token);
            JSONObject tokenObj =  new JSONObject(obj.getString("token"));
            String deviceId = tokenObj.getString("deviceId");

            if (deviceId!=null && deviceId.startsWith("VM")){
                padCode = deviceId.substring(2,deviceId.length());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (padCode!=null){
            Messager.getInstance().start(1, padCode);
        }

    }

    public static void stop(){
        if (Messager.getInstance().isConnected()){
            Messager.getInstance().close();
        }
    }

    private Messager.ICallback mCallback = new Messager.ICallback() {
        @Override
        public void onMessage(String msg) {
            logger.info("=========onMessage: " + msg);

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
    };

    private void sendHandle(String msg){
        if (msg == null) {
            return;
        }

        try {
            JSONObject obj = new JSONObject(msg);
            String event = obj.getString("event");
            if ("login".equals(event)){
                mHandler.sendEmptyMessage(1);
            }else if ("pay".equals(event)){
                String proCode = obj.getString("productcode");
                String orderId = obj.getString("orderID");
                HashMap<String,String> map = new HashMap();
                map.put("productcode",proCode);
                map.put("orderID",orderId);
                mHandler.sendMessage(Message.obtain(mHandler,2,map));
            }
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }
    }

    private String mGuid;
    private String mToken;
    private String mGloablId;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    //处理登录，判断是联运登录，还是本地登录
                    String uninqueId = GameBoxManager.getInstance(mActivity).getUniqueId();
                    if (uninqueId!=null && uninqueId.length() > 0){
                        new RequestLoginTask(mCorpId, new RequestLoginTask.ICallback() {
                            @Override
                            public void onResult(HashMap<String, Object> map) {
                                if (map==null){
                                    Toast.makeText(mActivity, "登录失败", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if (map != null && map.containsKey("error")){
                                    String error = map.get("error").toString();
                                    Toast.makeText(mActivity, error, Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (map.containsKey("access_token")){
                                    mToken = map.get("access_token").toString();
                                }
                                if (map.containsKey("guid")){
                                    mGuid = map.get("guid").toString();
                                }
                                if (map.containsKey("global_id")){
                                    mGloablId = map.get("global_id").toString();
                                }

                                sendLoginMsg(map);
                            }
                        }).execute("uid",uninqueId);
                    }
                    //显示登录界面
                    else {
                        showKpLogin();
                    }
                    break;
                case 2:
                    HashMap<String,String> map = (HashMap<String, String>) msg.obj;
                    String productcode = map.get("productcode");
                    String orderID = map.get("orderID");
                    //弹出支付窗口
                    showPayDialog(productcode, orderID);
                    break;
            }
        }
    };
    private LoginDialog mKpLoginDialog;
    int systemUi = -1;
    private void showKpLogin(){
        systemUi = mActivity.getWindow().getDecorView().getSystemUiVisibility();
        if (mKpLoginDialog!=null && mKpLoginDialog.isShowing()){
            return;
        }
        mKpLoginDialog = new LoginDialog(mActivity, mCorpId);
        mKpLoginDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mKpLoginDialog = null;
                if (systemUi != -1){
                    mActivity.getWindow().getDecorView().setSystemUiVisibility(systemUi);
                }
            }
        });
        mKpLoginDialog.setCallback(new LoginDialog.ICallback() {
            @Override
            public void onResult(Map map) {
                if (map==null){
                    Toast.makeText(mActivity, "登录失败", Toast.LENGTH_LONG).show();
                    return;
                }
                if (map != null && map.containsKey("error")){
                    String error = map.get("error").toString();
                    Toast.makeText(mActivity, error, Toast.LENGTH_LONG).show();
                    return;
                }
                mKpLoginDialog.dismiss();
                sendLoginMsg(map);


//                if (ret == 1){
//                    sendLoginMsg(msg);
//                    mKpLoginDialog.dismiss();
//                }else if (msg != null && msg.containsKey("error")){
//                    Toast.makeText(mActivity, msg.get("error").toString(), Toast.LENGTH_LONG).show();
//                }else {
//                    Toast.makeText(mActivity, "登录失败", Toast.LENGTH_LONG).show();
//                }
            }
        });
        mKpLoginDialog.show();
    }


    private void sendLoginMsg(Map map){
        JSONObject obj = new JSONObject(map);
        try {
            obj.put("event","login");
//            obj.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Messager.getInstance().send(obj.toString());
    }

    private void sendPayMsg(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("event","payover");
            obj.put("result", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Messager.getInstance().send(obj.toString());
    }

    private PayDialog mPayDialog;
    private void showPayDialog(String productcode, String orderID){
        systemUi = mActivity.getWindow().getDecorView().getSystemUiVisibility();
        if (mPayDialog!=null && mPayDialog.isShowing()){
            return;
        }
        mPayDialog = new PayDialog(mActivity);
        mPayDialog.productcode = productcode;
        mPayDialog.cp_orderid = orderID;
        mPayDialog.guid = mGuid;
        mPayDialog.globaluserid =  mGloablId;
        mPayDialog.globalusername = "";
        mPayDialog.cotype = "lianyun";
        mPayDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mPayDialog = null;
                if (systemUi != -1){
                    mActivity.getWindow().getDecorView().setSystemUiVisibility(systemUi);
                }
            }
        });
        mPayDialog.setCallback(new PayDialog.ICallback() {
            @Override
            public void onResult(int ret, String msg) {
                if (ret == 1){
                    sendPayMsg();
                    mPayDialog.dismiss();
                }else if (msg != null){
                    Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(mActivity, "支付失败", Toast.LENGTH_LONG).show();
                }
            }
        });
        mPayDialog.show();
    }

}
