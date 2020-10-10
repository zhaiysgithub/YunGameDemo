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
    private String mGameId;
    private String mGameName;
    private String mPadCode;

//    private String mCacheKey;

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
                if (map.containsKey("access_token")){
                    Object at =  map.get("access_token");
                    map.put("token", at);
                    map.remove("access_token");
                }
                JSONObject obj = new JSONObject(map);
                ProferencesUtils.setString(mActivity, getCacheKey(), obj.toString());
            }
        }catch (Exception e){
            logger.error(e.getMessage());
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
            logger.error(e.getMessage());
        }
        return map;
    }

    private void showLoginDialog(){
        //处理登录，判断是联运登录，还是本地登录
        String uninqueId = GameBoxManager.getInstance(mActivity).getUniqueId();
        if (uninqueId!=null && uninqueId.length() > 0){
            //联运帐号登录
            LoginDialog login = new LoginDialog(mActivity, mCorpId, mPkgName, mPadCode);
            login.setCallback(new LoginDialog.OnLoginListener() {
                @Override
                public void onLoginSuccess(HashMap<String, Object> map) {
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
            mLoginDialog = new LoginDialog(mActivity, mCorpId, mPkgName, mPadCode);
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
                public void onLoginSuccess(HashMap<String, Object> map) {
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

        String productcode = null;
        String orderID = null;
        String productName = null;
        String productMoney = null;
        String cpId = null;

        try {
            JSONObject obj = new JSONObject(msg);

            productcode = obj.has("productcode") ? obj.getString("productcode") : null;
            orderID = obj.has("orderID") ? obj.getString("orderID") : null;
            productName = obj.has("productname") ? obj.getString("productname") : null;
            productMoney = obj.has("money") ? obj.getString("money") : null;
            cpId = obj.has("cpid") ? obj.getString("cpid") : null;

        }catch (Exception e){
            logger.error(e.getMessage());
        }

        if (productcode == null || orderID == null){
            Toast.makeText(mActivity, "未获取到商品信息", Toast.LENGTH_SHORT).show();
            //回调
            if (mCallback!=null){
                mCallback.onPay(0, "Error params: productcode or orderID ", null);
            }
            return;
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

        mPayDialog = new PayDialog(mActivity);

        mPayDialog.setPadCode(mPadCode);

        mPayDialog.productcode = productcode;
        mPayDialog.productname = productName;
        mPayDialog.productprice = productMoney;
        mPayDialog.cp_orderid = orderID;
        mPayDialog.guid = guid;
        mPayDialog.cpid = cpId;
        mPayDialog.corpKey = mCorpId;
        mPayDialog.gameId = mGameId;
        mPayDialog.gamePkg = mPkgName;
        mPayDialog.gameName = mGameName;
        mPayDialog.phone = phone;

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
        systemUi = mActivity.getWindow().getDecorView().getSystemUiVisibility();
        mPayDialog.show();
    }

}
