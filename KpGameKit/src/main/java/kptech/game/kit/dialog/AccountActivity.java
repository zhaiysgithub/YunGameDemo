package kptech.game.kit.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import java.util.HashMap;
import java.util.Map;

import kptech.game.kit.R;
import kptech.lib.analytic.Event;
import kptech.lib.analytic.EventCode;
import kptech.lib.analytic.MobclickAgent;
import kptech.lib.data.AccountTask;
import kptech.game.kit.dialog.view.PhoneLoginView;
import kptech.game.kit.dialog.view.PwdLoginView;
import kptech.game.kit.utils.Logger;

public class AccountActivity extends Dialog implements View.OnClickListener {
    private static final String TAG = "AccountActivity";
//    private static final Logger logger = new Logger("AccountActivity");

    public interface OnLoginListener{
        void onLoginSuccess(Map<String, Object> map);
        void onLoginFailed(String err);
    }

//    public static final String CORP_KEY = "corp_key";
//    public static final String CPID = "cpid";

    String mCorpKey;
    String mCpId;

    private PhoneLoginView mPhoneView;
    private PwdLoginView mPswView;

    private Activity mActivity;
    private String mPkgName;
    private String mPadCode;

    private Map<String, Object> mRetMap;

    private OnLoginListener mCallback;
    public void setCallback(OnLoginListener callback){
        mCallback = callback;
    }

    private OnDismissListener mOnDismissListener;

    @Override
    public void setOnDismissListener(OnDismissListener listener) {
        this.mOnDismissListener = listener;
    }

    public AccountActivity(Activity context, String corpId, String pkgName, String padCode) {
        super(context, R.style.MyTheme_CustomDialog_Background);
        this.mActivity = context;
        this.mCorpKey = corpId;
        this.mPkgName = pkgName;
        this.mPadCode = padCode;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kp_activity_account);

        Logger.info(TAG,"onCreate " + mCorpKey);

        try {
            //发送打点事件
            Event event = Event.getEvent(EventCode.DATA_DIALOG_PHONELOGIN_DISPLAY);
            MobclickAgent.sendEvent(event);
        }catch (Exception ex){
            Logger.error(TAG,ex.getMessage());
        }

        mPhoneView = findViewById(R.id.ph_login_view);
        mPhoneView.setCorpKey(mCorpKey, mPkgName, mPadCode);

        mPswView = findViewById(R.id.pw_login_view);
        mPswView.setCorpKey(mCorpKey, mPkgName, mPadCode);

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mPhoneView.findViewById(R.id.go_psw_login).setOnClickListener(this);
        mPswView.findViewById(R.id.go_phone_login).setOnClickListener(this);

        mPswView.setOnLoginListener(new PwdLoginView.OnLoginListener() {
            @Override
            public void onLoginSuccess(Map<String, Object> map) {
                mRetMap = map;
                dismiss();
            }

            @Override
            public void onLoginFailed(Map<String, Object> map) {
                mRetMap = map;
            }
        });
        mPhoneView.setOnLoginListener(new PhoneLoginView.OnLoginListener() {
            @Override
            public void onLoginSuccess(Map<String, Object> map) {
                mRetMap = map;
                dismiss();
            }

            @Override
            public void onLoginFailed(Map<String, Object> map) {
                mRetMap = map;
            }

        });

        setCanceledOnTouchOutside(false);
        super.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

                try {
                    //发送打点事件
                    Event event = Event.getEvent(EventCode.DATA_DIALOG_ACCOUNT_CLOSE);
                    MobclickAgent.sendEvent(event);
                }catch (Exception ex){
                    Logger.error(TAG,ex.getMessage());
                }

                handlerCallback(mRetMap);

                if (mOnDismissListener!=null){
                    mOnDismissListener.onDismiss(dialogInterface);
                }
            }
        });
    }


    private void handlerCallback(Map<String,Object> map){
        //登录成功
        if (map!=null){
            String guid = map.containsKey("guid") ? map.get("guid").toString() : null;
            if (guid!=null){
                //登录成功
                if (mCallback!=null){
                    mCallback.onLoginSuccess(map);
                }
            }
        }else {
            String error = "";
            try {
                if (map!=null && map.containsKey("error")){
                    error = (String) map.get("error");
                }
            }catch (Exception e){}

            if (mCallback!=null){
                mCallback.onLoginFailed(error);
            }
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.go_psw_login && mPswView.getVisibility() != View.VISIBLE) {
            mPswView.setVisibility(View.VISIBLE);
            mPhoneView.setVisibility(View.GONE);
            mPswView.setPhoneText(mPhoneView.getPhoneText());

            try {
                //发送打点事件
                Event event = Event.getEvent(EventCode.DATA_DIALOG_PWDLOGIN_DISPLAY);
                MobclickAgent.sendEvent(event);
            }catch (Exception ex){}

        }else if (view.getId() == R.id.go_phone_login && mPhoneView.getVisibility() != View.VISIBLE){
            mPswView.setVisibility(View.GONE);
            mPhoneView.setVisibility(View.VISIBLE);
            mPhoneView.setPhoneText(mPswView.getPhoneText());

            try {
                //发送打点事件
                Event event = Event.getEvent(EventCode.DATA_DIALOG_PHONELOGIN_DISPLAY);
                MobclickAgent.sendEvent(event);
            }catch (Exception ex){}
        }
    }


    public void requestUidLogin(final String uninqueId){
        try {
            Event event = Event.getEvent(EventCode.DATA_USER_LOGINUSIGN_START, mPkgName, mPadCode);
            HashMap<String,String> ext = new HashMap<>();
            ext.put("acct", uninqueId);
            event.setExt(ext);
            MobclickAgent.sendEvent(event);
        }catch (Exception e){}


        //通过渠道设置的UID登录
        new AccountTask(mActivity, AccountTask.ACTION_LOGIN_CHANNEL_UUID)
                .setCorpKey(mCorpKey)
                .setPkgName(mPkgName)
                .setCallback(new AccountTask.ICallback() {
                    @Override
                    public void onResult(Map<String, Object> map) {

                        String errMsg = null;
                        if (map==null || map.size()<=0){
                            errMsg = "登录失败";
                        }else if (map.containsKey("error")){
                            errMsg = map.get("error").toString();
                        }

                        if (errMsg != null){
                            Toast.makeText(mActivity, errMsg, Toast.LENGTH_SHORT).show();

                            try {
                                //发送打点事件
                                Event event = Event.getEvent(EventCode.DATA_USER_LOGINUSIGN_FAILED, mPkgName, mPadCode);
                                event.setErrMsg(errMsg);
                                HashMap<String,String> ext = new HashMap<>();
                                ext.put("logintype", "channel");
                                ext.put("acct", uninqueId);
                                event.setExt(ext);
                                MobclickAgent.sendEvent(event);
                            }catch (Exception e){}
                        }else {

                            try {
                                //设置打点guid
                                if (map.containsKey("guid")){
                                    Object guid = map.get("guid");
                                    if (guid != null){
                                        Event.setGuid(guid+"");
                                    }
                                }
                            }catch (Exception e){}

                            try {
                                int reg = 0;
                                try {
                                    if (map.containsKey("doreg")) {
                                        reg = (int) map.get("doreg");
                                    }
                                } catch (Exception e) {
                                }
                                String eventCode = reg == 1 ? EventCode.DATA_USER_LOGINUSIGN_SUCCESSREG : EventCode.DATA_USER_LOGINUSIGN_SUCCESS;
                                //发送打点事件
                                Event event = Event.getEvent(eventCode, mPkgName, mPadCode);
                                HashMap<String,String> ext = new HashMap<>();
                                ext.put("logintype", "channel");
                                ext.put("acct", uninqueId);
                                event.setExt(ext);
                                MobclickAgent.sendEvent(event);
                            }catch (Exception e){}
                        }

                        handlerCallback(map);

                        dismiss();
                    }
                })
                .execute(uninqueId);
    }

}
