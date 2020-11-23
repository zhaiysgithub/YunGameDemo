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
import kptech.game.kit.analytic.Event;
import kptech.game.kit.analytic.EventCode;
import kptech.game.kit.analytic.MobclickAgent;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.dialog.AccountActivity;
import kptech.game.kit.dialog.PayActivity;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;

public class MsgHandler extends Handler {
//    private static final Logger logger = new Logger("MsgHandler") ;

    protected static final int MSG_LOGIN = 1;
    protected static final int MSG_RELOGIN = 2;
    protected static final int MSG_PAY = 3;
    protected static final int MSG_LOGOUT = 4;



    private Activity mActivity;
    private AccountActivity mLoginDialog;
    private PayActivity mPayDialog;

    private String mCorpId;
    private String mPkgName;
    private String mGameId;
    private String mGameName;
    private String mPadCode;

//    private String mCacheKey;

    private int systemUi = -1;

    private ICallback mCallback;

    public void destory() {
        mActivity = null;
        mLoginDialog = null;
        mPayDialog = null;
        mCallback = null;
    }

    protected interface ICallback {
        void onLogin(int code, String msg, Map<String, Object> map);
        void onPay(int code, String msg, Map<String, Object> map);
        void onLogout();
    }

    public MsgHandler(Activity activity, String corpId, String pkgName){
        super(Looper.getMainLooper());
        this.mActivity = activity;
        this.mCorpId = corpId;
        this.mPkgName = pkgName;
    }

    public void setCallback(ICallback callback){
        this.mCallback = callback;
    }

    public void setGameId(String gameId) {
        this.mGameId = gameId;
    }

    public void setGameName(String gameName) {
        this.mGameName = gameName;
    }

    public void setPadCode(String padCode) {
        this.mPadCode = padCode;
    }

    public void setPkgName(String pkgName) {
        this.mPkgName = pkgName;
    }

    public String getCacheKey(){
        return SharedKeys.KEY_GAME_USER_LOGIN_DATA_PRE + this.mPkgName;
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
                handlePay(msg.obj.toString());
                break;
            case MSG_LOGOUT:
                handleLogout();
                break;
        }
    }

    private void handleLogin(){

        try {
            //获取缓存，判断是否已经登录过
            Map<String, Object> loginData = getLoginData();
            String guid = loginData.containsKey("guid") ? loginData.get("guid").toString() : null;
            String token = loginData.containsKey("token") ? loginData.get("token").toString() : null;

            //发送缓存数据
            if (guid!=null && token!=null){
                if (mCallback!=null){
                    mCallback.onLogin(1, "", loginData);
                }

                try {
                    //发送打点事件
                    Event event = Event.getEvent(EventCode.DATA_USER_LOGIN_CACHE, mPkgName, mPadCode);
                    MobclickAgent.sendEvent(event);
                }catch (Exception e){}

                return;
            }
        }catch (Exception e){
            Logger.error("MsgHandler",e.getMessage());
        }

        //登录
        showLoginDialog();
    }

    private void handleLogout(){
        //清除缓存数据
        clearCacheLoginData();

        //回调
        if (mCallback!=null){
            mCallback.onLogout();
        }
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
                if (map.containsKey("access_token")){
                    Object at =  map.get("access_token");
                    map.put("token", at);
                    map.remove("access_token");
                }
                JSONObject obj = new JSONObject(map);
                ProferencesUtils.setString(mActivity, getCacheKey(), obj.toString());
            }
        }catch (Exception e){
            Logger.error("MsgHandler",e.getMessage());
        }
    }

    private void clearCacheLoginData(){
        ProferencesUtils.remove(mActivity, getCacheKey());
    }

    private Map<String,Object> getLoginData(){
        HashMap<String,Object> map = new HashMap<>();
        try {
            String data = ProferencesUtils.getString(mActivity, getCacheKey(), null);
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
            Logger.error("MsgHandler",e.getMessage());
        }
        return map;
    }

    private void showLoginDialog(){
        //处理登录，判断是联运登录，还是本地登录
        String uninqueId = GameBoxManager.getInstance().getUniqueId();
        if (uninqueId!=null && uninqueId.length() > 0){
            //联运帐号登录
            AccountActivity login = new AccountActivity(mActivity, mCorpId, mPkgName, mPadCode);
            login.setCallback(new AccountActivity.OnLoginListener() {
                @Override
                public void onLoginSuccess(Map<String, Object> map) {
                    //缓存数据
                    cacheLoginData(map);

                    //回调
                    if (mCallback!=null){
                        mCallback.onLogin(1, "", map);
                    }
                }
            });
            login.requestUidLogin(uninqueId);
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
            mLoginDialog = new AccountActivity(mActivity, mCorpId, mPkgName, mPadCode);
            mLoginDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    mLoginDialog = null;
                    if (systemUi != -1){
                        mActivity.getWindow().getDecorView().setSystemUiVisibility(systemUi);
                    }
                }
            });
            mLoginDialog.setCallback(new AccountActivity.OnLoginListener() {
                @Override
                public void onLoginSuccess(Map<String, Object> map) {
                    //缓存数据
                    cacheLoginData(map);

                    //回调
                    if (mCallback!=null){
                        mCallback.onLogin(1, "", map);
                    }
                }
            });
            mLoginDialog.show();
        }
    }

    private void handlePay(String msg){
        if (mPayDialog!=null && mPayDialog.isShowing()){
            return;
        }

        if (mActivity == null || mActivity.isFinishing()){
            //回调
            if (mCallback!=null){
                mCallback.onPay(0, "Error params: activity ", null);
            }
            return;
        }

        HashMap<String,Object> params = new HashMap<>();
        try {
            JSONObject obj = new JSONObject(msg);
            Iterator<String> keys = obj.keys();
            while (keys.hasNext()){
                String key = keys.next();
                if ("event".equals(key)){
                    continue;
                }
                params.put(key, obj.get(key));
            }
        }catch (Exception e){
            Logger.error("MsgHandler",e.getMessage());
        }

        Map<String, Object> loginData = getLoginData();
        String guid = loginData.containsKey("guid") ? loginData.get("guid").toString() : null;
        String phone = loginData.containsKey("phone" )? loginData.get("phone").toString() : null;

        //判断用户是否已登录
        if (guid == null){
            Toast.makeText(mActivity, "用户未登录", Toast.LENGTH_SHORT).show();
            //回调
            if (mCallback!=null){
                mCallback.onPay(0, "Error params: guid,gloabid", null);
            }
            return;
        }

        mPayDialog = new PayActivity(mActivity, mCorpId, mGameId, mPkgName, mPadCode);
        mPayDialog.setParams(params);
        mPayDialog.setUserInfo(guid, phone);

        mPayDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mPayDialog = null;
                if (systemUi != -1){
                    mActivity.getWindow().getDecorView().setSystemUiVisibility(systemUi);
                }
            }
        });
        mPayDialog.setCallback(new PayActivity.ICallback() {
            @Override
            public void onResult(int ret, String msg) {
                if (ret == 1){
                    //回调
                    if (mCallback!=null){
                        mCallback.onPay(1, "", null);
                    }
                }else if (msg != null){
                    Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(mActivity, "支付失败", Toast.LENGTH_LONG).show();
                }
            }
        });
        systemUi = mActivity.getWindow().getDecorView().getSystemUiVisibility();
        mPayDialog.show();
    }

}
