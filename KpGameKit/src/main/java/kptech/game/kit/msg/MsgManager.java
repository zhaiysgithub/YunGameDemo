package kptech.game.kit.msg;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import kptech.cloud.kit.msg.Messager;
import kptech.game.kit.GameBoxManager;
import kptech.game.kit.data.RequestLoginTask;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.view.LoginActivity;
import kptech.game.kit.view.LoginDialog;

public class MsgManager {
    private static final String TAG = "Messager";

    private static volatile MsgManager instance = null;
    private static volatile Activity mActivity = null;
    public static void init(Application application){
        Messager.setDebug(true);
        Messager.init(application);

        if (instance == null){
            synchronized(GameBoxManager.class) {
                if (instance == null) {
                    instance = new MsgManager(application);
                }
            }
        }
    }

    private MsgManager(Context context){
        Messager.getInstance().addCallback(mCallback);
    }


    public static void start(@NonNull Activity activity, String token){
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
        Messager.getInstance().close();
    }

    private Messager.ICallback mCallback = new Messager.ICallback() {
        @Override
        public void onMessage(String msg) {
            Logger.i(TAG, "=========onMessage: " + msg);

            sendHandle(msg);
        }

        @Override
        public void onConnect(int code, String s) {
            Logger.i(TAG, "onConnect code: " + code +" msg: " + s);
        }

        @Override
        public void onClose(int code, String s) {
            Logger.i(TAG, "onClose code: " + code + " msg: " + s);
        }

        @Override
        public void onFailure(int code, String s) {
            Logger.i(TAG, "onFailure code: " + code + " msg: " + s);
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
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 1:
                    //处理登录，判断是联运登录，还是本地登录
                    String uninqueId = GameBoxManager.getInstance(mActivity).getUniqueId();
                    if (uninqueId!=null && uninqueId.length() > 0){
                        new RequestLoginTask(new RequestLoginTask.ICallback() {
                            @Override
                            public void onResult(HashMap<String, String> map) {
                                if (map==null){
                                    Toast.makeText(mActivity, "登录失败", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if (map.containsKey("access_token")){
                                    String token = map.get("access_token");
                                    sendLoginMsg(token);
                                }else if(map.containsKey("error")) {
                                    String error = map.get("error");
                                    Toast.makeText(mActivity, error, Toast.LENGTH_LONG).show();
                                }else {
                                    Toast.makeText(mActivity, "登录失败", Toast.LENGTH_LONG).show();
                                }
                            }
                        }).execute("uid",uninqueId);
                    }
                    //显示登录界面
                    else {
                        showKpLogin();
                    }
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
        mKpLoginDialog = new LoginDialog(mActivity);
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
            public void onResult(int ret, String msg) {
                if (ret == 1){
                    sendLoginMsg(msg);
                    mKpLoginDialog.dismiss();
                }else if (msg != null){
                    Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(mActivity, "登录失败", Toast.LENGTH_LONG).show();
                }
            }
        });
        mKpLoginDialog.show();
    }


    private void sendLoginMsg(String token){
        JSONObject obj = new JSONObject();
        try {
            obj.put("event","login");
            obj.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Messager.getInstance().send(obj.toString());
    }

}
