package kptech.game.kit.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import java.util.HashMap;
import java.util.Map;

import kptech.game.kit.R;
import kptech.game.kit.analytic.Event;
import kptech.game.kit.analytic.EventCode;
import kptech.game.kit.analytic.MobclickAgent;
import kptech.game.kit.data.AccountTask;
import kptech.game.kit.dialog.view.PhoneLoginView;
import kptech.game.kit.dialog.view.PwdLoginView;
import kptech.game.kit.utils.Logger;

public class AccountActivity extends Dialog implements View.OnClickListener {
    private static final Logger logger = new Logger("AccountActivity");

    public interface OnLoginListener{
        void onLoginSuccess(Map<String, Object> map);
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

    private OnLoginListener mCallback;
    public void setCallback(OnLoginListener callback){
        mCallback = callback;
    }

    public AccountActivity(Activity context, String corpId, String pkgName, String padCode) {
        super(context, R.style.MyTheme_CustomDialog);
        this.mActivity = context;
        this.mCorpKey = corpId;
        this.mPkgName = pkgName;
        this.mPadCode = padCode;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        logger.info("onCreate " + mCorpKey);

        try {
            //发送打点事件
            Event event = Event.getEvent(EventCode.DATA_DIALOG_PHONELOGIN_DISPLAY);
            MobclickAgent.sendEvent(event);
        }catch (Exception ex){
            logger.error(ex.getMessage());
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
                success(map);
            }
        });
        mPhoneView.setOnLoginListener(new PhoneLoginView.OnLoginListener() {
            @Override
            public void onLoginSuccess(Map<String, Object> map) {
                success(map);
            }
        });
    }

    private void success(Map<String, Object> map){
        logger.info("login success data: " + map!=null ? map.toString() : null);

        //登录成功
        if (map!=null){
            String guid = map.containsKey("guid") ? map.get("guid").toString() : null;
            if (guid!=null){
                //登录成功
                if (mCallback!=null){
                    mCallback.onLoginSuccess(map);
                }
            }
        }

        dismiss();
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
        //发送请求
//        requestLogin("kp", mCorpId, uninqueId);

        //通过渠道设置的UID登录
        new AccountTask(mActivity, AccountTask.ACTION_LOGIN_CHANNEL_UUID)
                .setCorpKey(mCorpKey)
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
                                Event event = Event.getEvent(EventCode.DATA_USER_LOGIN_FAILED, mPkgName, mPadCode);
                                event.setErrMsg(errMsg);
                                HashMap<String,String> ext = new HashMap<>();
                                ext.put("logintype", "channel");
                                ext.put("acct", uninqueId);
                                event.setExt(ext);
                                MobclickAgent.sendEvent(event);
                            }catch (Exception e){}

                            return;
                        }

                        try {
                            //发送打点事件
                            Event event = Event.getEvent(EventCode.DATA_USER_LOGIN_SUCCESS, mPkgName, mPadCode);
                            HashMap<String,String> ext = new HashMap<>();
                            ext.put("logintype", "channel");
                            ext.put("acct", uninqueId);
                            event.setExt(ext);
                            MobclickAgent.sendEvent(event);
                        }catch (Exception e){}

                        success(map);
                    }
                })
                .execute(uninqueId);
    }

}
