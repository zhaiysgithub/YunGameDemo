package kptech.game.kit.msg;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import kptech.game.kit.GameBoxManager;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.data.RequestLoginTask;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;
import kptech.game.kit.view.LoginDialog;
import kptech.game.kit.view.PayDialog;
import kptech.game.kit.view.PayRadioButton;

public class MsgHandler extends Handler {
    private static final Logger logger = new Logger("MsgHandler") ;

    protected static final int MSG_LOGIN = 1;
    protected static final int MSG_RELOGIN = 2;
    protected static final int MSG_PAY = 3;



    private Activity mActivity;
    private LoginDialog mLoginDialog;
    private PayDialog mPayDialog;

    private String mCorpId;
    private String mPkgName;

    private String mCacheKey;

    private int systemUi = -1;

    private ICallback mCallback;
    protected interface ICallback {
        void onLogin(int code, String msg, Map<String, Object> map);
        void onPay(int code, String msg, Map<String, Object> map);
    }

    public MsgHandler(Activity activity, String corpId, String pkgName){
        super(Looper.getMainLooper());
        this.mActivity = activity;
        this.mCorpId = corpId;
        this.mPkgName = pkgName;
        this.mCacheKey = SharedKeys.KEY_GAME_USER_LOGIN_DATA_PRE + pkgName;
    }

    public void setCallback(ICallback callback){
        this.mCallback = callback;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what){
            case MSG_LOGIN:
                handleLogin();
                break;
            case MSG_RELOGIN:
                handleRelogin();
                break;
            case MSG_PAY:
                handlePay(msg.obj);
                break;
        }
    }

    private void handleLogin(){
        try {
            //获取缓存，判断是否已经登录过
            Map<String, Object> loginData = getLoginData();
            String guid = loginData.containsKey("guid") ? loginData.get("guid").toString() : null;
            String token = loginData.containsKey("access_token") ? loginData.get("access_token").toString() : null;

            //发送缓存数据
            if (guid!=null && token!=null){
                if (mCallback!=null){
                    mCallback.onLogin(1, "", loginData);
                }
                return;
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }

        //登录
        showLoginDialog();
    }

    private void handleRelogin(){
        //清除缓存数据
        clearCacheLoginData();

        //登录
        showLoginDialog();
    }

    private void cacheLoginData(Map map){
        try {
            if (map!=null){
                JSONObject obj = new JSONObject(map);
                ProferencesUtils.setString(mActivity, mCacheKey, obj.toString());
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    private void clearCacheLoginData(){
        ProferencesUtils.remove(mActivity, mCacheKey);
    }

    private Map<String,Object> getLoginData(){
        HashMap<String,Object> map = new HashMap<>();
        try {
            String data = ProferencesUtils.getString(mActivity, mCacheKey, null);
            if (data!=null){
                JSONObject obj = new JSONObject(data);
                Iterator<String> keys = obj.keys();
                while (keys.hasNext()){
                    String key = keys.next();
                    Object val = obj.get(key);
                    map.put(key,val);
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }
        return map;
    }

    private void showLoginDialog(){

        //处理登录，判断是联运登录，还是本地登录
        String uninqueId = GameBoxManager.getInstance(mActivity).getUniqueId();
        if (uninqueId!=null && uninqueId.length() > 0){
            //联运帐号登录
            requestLogin("uid", mCorpId, uninqueId);
        }else {
            if (mActivity == null || mActivity.isFinishing()){
                //回调
                if (mCallback!=null){
                    mCallback.onLogin(0, "Error params: activity ", null);
                }
                return;
            }

            //快盘帐号
            systemUi = mActivity.getWindow().getDecorView().getSystemUiVisibility();
            if (mLoginDialog!=null && mLoginDialog.isShowing()){
                return;
            }
            mLoginDialog = new LoginDialog(mActivity);
            mLoginDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    mLoginDialog = null;
                    if (systemUi != -1){
                        mActivity.getWindow().getDecorView().setSystemUiVisibility(systemUi);
                    }
                }
            });
            mLoginDialog.setCallback(new LoginDialog.OnLoginListener() {
                @Override
                public void onClick(String phone, String psw) {
                    requestLogin("kp", mCorpId, phone, psw);
                }
            });
            mLoginDialog.show();
        }
    }

    private void requestLogin(String ...params){
        new RequestLoginTask(new RequestLoginTask.ICallback() {
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

                //缓存数据
                cacheLoginData(map);

                if (mLoginDialog!=null){
                    mLoginDialog.dismiss();
                }

                if (mCallback!=null){
                    mCallback.onLogin(1, "", map);
                }
            }
        }).execute(params);
    }

    private void handlePay(Object obj){
        String productcode = null;
        String orderID = null;
        try {
            if (obj!=null){
                HashMap<String,String> map = (HashMap<String, String>)obj;
                productcode = map.get("productcode");
                orderID = map.get("orderID");
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }

        if (productcode == null || orderID == null){
            //回调
            if (mCallback!=null){
                mCallback.onPay(0, "Error params: productcode or orderID ", null);
            }

            return;
        }
        if (mActivity == null || mActivity.isFinishing()){
            //回调
            if (mCallback!=null){
                mCallback.onPay(0, "Error params: activity ", null);
            }
            return;
        }

        systemUi = mActivity.getWindow().getDecorView().getSystemUiVisibility();
        if (mPayDialog!=null && mPayDialog.isShowing()){
            return;
        }

        Map<String, Object> loginData = getLoginData();
        String guid = loginData.containsKey("guid") ? loginData.get("guid").toString() : null;
        String globaid = loginData.containsKey("global_id") ? loginData.get("global_id").toString() : null;

        mPayDialog = new PayDialog(mActivity);
        mPayDialog.productcode = productcode;
        mPayDialog.cp_orderid = orderID;
        mPayDialog.guid = guid;
        mPayDialog.globaluserid =  globaid;
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
                    mPayDialog.dismiss();

                    //回调
                    if (mCallback!=null){
                        mCallback.onPay(1, "", null);
                    }
//                    sendPayMsg();
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
