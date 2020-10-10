package kptech.game.kit.view;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

import kptech.game.kit.R;
import kptech.game.kit.analytic.Event;
import kptech.game.kit.analytic.EventCode;
import kptech.game.kit.analytic.MobclickAgent;
import kptech.game.kit.data.RequestLoginTask;
import kptech.game.kit.data.RequestPhoneCodeTask;
import kptech.game.kit.data.RequestRegistTask;

public class LoginDialog extends Dialog implements View.OnClickListener {

    public interface OnLoginListener{
        void onLoginSuccess(HashMap<String, Object> map);
    }

    private static final int TYPE_LOGIN = 1;
    private static final int TYPE_REGISAT = 2;
    private static final int TYPE_FORGET = 3;

    private static final String TAG = "AlertDialog";
    private Activity mActivity;

    private ViewGroup mLoginLayout;
    private ViewGroup mRegistLayout;

    private EditText mLoginPhoneText;
    private EditText mLoginPswText;


    private EditText mRegistPhoneText;
    private EditText mRegistCodeText;
    private EditText mRegistPswText;
    private EditText mRegistPsw1Text;

    private Button mLoginBtn;
    private Button mRegistBtn;
    private Button mPhoneCodeBtn;

    private int mType = TYPE_LOGIN;

    private String mPkgName;
    private String mPadCode;

    private String mCorpId;

    private LoginDialog.OnLoginListener mCallback;
    public void setCallback(LoginDialog.OnLoginListener callback){
        mCallback = callback;
    }

    private LoadingDialog mLoading;

    public LoginDialog(Activity context, String corpId, String pkgName, String padCode) {
        super(context, R.style.MyTheme_CustomDialog);
        this.mActivity = context;
        this.mCorpId = corpId;
        this.mPkgName = pkgName;
        this.mPadCode = padCode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_login);
        setCanceledOnTouchOutside(false);

        findViewById(R.id.close).setOnClickListener(this);

        mLoginLayout = findViewById(R.id.login_layout);
        mRegistLayout = findViewById(R.id.regist_layout);


        mLoginPhoneText = mLoginLayout.findViewById(R.id.login_phone);
        mLoginPswText = mLoginLayout.findViewById(R.id.login_psw);
        mLoginBtn = mLoginLayout.findViewById(R.id.submit_login);
        mLoginBtn.setOnClickListener(this);
        mLoginLayout.findViewById(R.id.go_regist).setOnClickListener(this);
        mLoginLayout.findViewById(R.id.go_forget).setOnClickListener(this);


        mRegistPhoneText = mRegistLayout.findViewById(R.id.regist_phone);
        mRegistCodeText = mRegistLayout.findViewById(R.id.phone_code);
        mRegistPswText = mRegistLayout.findViewById(R.id.regist_psw);
        mRegistPsw1Text = mRegistLayout.findViewById(R.id.regist_psw_1);
        mRegistBtn = mRegistLayout.findViewById(R.id.submit_regist);
        mRegistBtn.setOnClickListener(this);
        mPhoneCodeBtn = mRegistLayout.findViewById(R.id.phone_code_btn);
        mPhoneCodeBtn.setOnClickListener(this);
        mRegistLayout.findViewById(R.id.phone_code_btn).setOnClickListener(this);
        mRegistLayout.findViewById(R.id.go_login).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.close) {
            dismiss();
        }else if (view.getId() == R.id.submit_login) {
            login();
        }else if (view.getId() == R.id.submit_regist) {
            regist();
        }else if (view.getId() == R.id.phone_code_btn){
            getPhoneCode();
        }else if (view.getId() == R.id.go_login){
            mType = TYPE_LOGIN;
            changeLayout();
        }else if (view.getId() == R.id.go_regist){
            mType = TYPE_REGISAT;
            changeLayout();
        }else if (view.getId() == R.id.go_forget){
            mType = TYPE_FORGET;
            changeLayout();
        }
    }

    private void changeLayout(){
        switch (mType){
            case TYPE_LOGIN:
                mLoginLayout.setVisibility(View.VISIBLE);
                mRegistLayout.setVisibility(View.GONE);
                mLoginPhoneText.setText(mRegistPhoneText.getText().toString());
                break;
            case TYPE_FORGET:
                mLoginLayout.setVisibility(View.GONE);
                mRegistLayout.setVisibility(View.VISIBLE);
                mRegistBtn.setText("修改密码");
                mRegistPhoneText.setText(mLoginPhoneText.getText().toString());
                break;
            case TYPE_REGISAT:
                mLoginLayout.setVisibility(View.GONE);
                mRegistLayout.setVisibility(View.VISIBLE);
                mRegistBtn.setText("立即注册");
                mRegistPhoneText.setText(mLoginPhoneText.getText().toString());
                break;
        }

    }

    private void login(){
        String phone = mLoginPhoneText.getText().toString().trim();
        if ("".equals(phone)){
            Toast.makeText(mActivity, "请输入手机号", Toast.LENGTH_LONG).show();
            return;
        }
        String psw = mLoginPswText.getText().toString().trim();
        if ("".equals(psw)){
            Toast.makeText(mActivity, "请输入密码", Toast.LENGTH_LONG).show();
            return;
        }

        //发送请求
        requestLogin("kp", mCorpId, phone, psw);
    }

    private void getPhoneCode(){
        String phone = mRegistPhoneText.getText().toString().trim();
        if ("".equals(phone) || phone.length() != 11){
            Toast.makeText(mActivity, "请输入手机号", Toast.LENGTH_LONG).show();
            return;
        }

        requestPhoneCode(phone);
    }

    private void regist(){
        String phone = mRegistPhoneText.getText().toString().trim();
        if ("".equals(phone) || phone.length() != 11){
            Toast.makeText(mActivity, "请输入手机号", Toast.LENGTH_LONG).show();
            return;
        }
        String smsCode = mRegistCodeText.getText().toString().trim();
        if ("".equals(smsCode)) {
            Toast.makeText(mActivity, "请输入验证码", Toast.LENGTH_LONG).show();
            return;
        }
        String psw = mRegistPswText.getText().toString().trim();
        if ("".equals(psw)) {
            Toast.makeText(mActivity, "请输入密码", Toast.LENGTH_LONG).show();
            return;
        }
        String psw1 = mRegistPsw1Text.getText().toString().trim();
        if (!psw.equals(psw1)) {
            Toast.makeText(mActivity, "确认密码不一致", Toast.LENGTH_LONG).show();
            return;
        }

        requestRegist(phone, psw, smsCode);
    }

    private void requestRegist(final String phone, String psw, String smsCode){
        if ( mType == TYPE_REGISAT){
            //发送打点事件
            try {
                Event event = Event.getEvent(EventCode.DATA_USER_REGIST_START, mPkgName, mPadCode);
                HashMap<String,String> ext = new HashMap<>();
                ext.put("phone", phone);
                event.setExt(ext);
                MobclickAgent.sendEvent(event);
            }catch (Exception e){}
        }

        if (mRegistBtn!=null){
            mRegistBtn.setEnabled(false);
        }
        mLoading = LoadingDialog.build(mActivity);
        mLoading.show();
        String action = mType == TYPE_FORGET ? RequestPhoneCodeTask.ACTION_FORGET : RequestPhoneCodeTask.ACTION_REGIST;
        new RequestRegistTask(mCorpId, action, phone, psw, smsCode, smsCodeId, new RequestRegistTask.ICallback() {
            @Override
            public void onResult(HashMap<String, Object> map) {
                mLoading.dismiss();
                if (mRegistBtn!=null){
                    mRegistBtn.setEnabled(true);
                }
                String errMsg = null;
                if (map == null){
                    errMsg = mType == TYPE_FORGET ? "修改密码失败" : "注册失败";
                }else if (map.containsKey("error")){
                    errMsg = map.get("error").toString();
                }

                if (errMsg != null){
                    Toast.makeText(mActivity, errMsg, Toast.LENGTH_SHORT).show();

                    try {
                        //发送打点事件
                        Event event = Event.getEvent(EventCode.DATA_USER_REGIST_FAILED, mPkgName, mPadCode);
                        event.setErrMsg(errMsg);
                        HashMap<String,String> ext = new HashMap<>();
                        ext.put("phone", phone);
                        event.setExt(ext);
                        MobclickAgent.sendEvent(event);
                    }catch (Exception e){}

                    return;
                }

                //修改密码成功，跳转到登录页面
                if (mType == TYPE_FORGET) {
                    Toast.makeText(mActivity, "修改成功，请重新登录", Toast.LENGTH_SHORT).show();
                    mType = TYPE_LOGIN;
                    changeLayout();
                }else {
                    Toast.makeText(mActivity, "注册成功", Toast.LENGTH_SHORT).show();
                    //发送登录成功的消息
                    if (mCallback!=null){
                        mCallback.onLoginSuccess(map);
                    }
                    if (isShowing()){
                        dismiss();
                    }

                    try {
                        //发送打点事件
                        Event event = Event.getEvent(EventCode.DATA_USER_REGIST_SUCCESS, mPkgName, mPadCode);
                        HashMap<String,String> ext = new HashMap<>();
                        ext.put("phone", phone);
                        event.setExt(ext);
                        MobclickAgent.sendEvent(event);
                    }catch (Exception e){}
                }
            }
        }).execute();

    }

    private Handler mHandler = null;
    private int mCodeTimerCount = 60;
    private String smsCodeId = "";
    private void requestPhoneCode(String phone){
        if (mPhoneCodeBtn!=null){
            mPhoneCodeBtn.setEnabled(false);
        }
        mLoading = LoadingDialog.build(mActivity);
        mLoading.show();
        //发送请求
        String action = mType == TYPE_FORGET ? RequestPhoneCodeTask.ACTION_FORGET : RequestPhoneCodeTask.ACTION_REGIST;
        new RequestPhoneCodeTask(mCorpId, action, phone, new RequestPhoneCodeTask.ICallback() {
            @Override
            public void onResult(HashMap<String, Object> map) {
                mLoading.dismiss();

                if (map == null){
                    mPhoneCodeBtn.setEnabled(true);
                    Toast.makeText(mActivity, "获取验证码失败", Toast.LENGTH_LONG).show();
                    return;
                }

                if (map.containsKey("error")){
                    mPhoneCodeBtn.setEnabled(true);
                    String error = map.get("error").toString();
                    Toast.makeText(mActivity, error, Toast.LENGTH_LONG).show();
                    return;
                }

                if (map.containsKey("smsCodeId")){
                    smsCodeId = map.get("smsCodeId").toString();
                }

                //启动倒记时
                if (mHandler == null){
                    mHandler = new Handler(Looper.getMainLooper()){
                        @Override
                        public void handleMessage(@NonNull Message msg) {
                            mCodeTimerCount--;
                            mPhoneCodeBtn.setText(""+mCodeTimerCount+"秒");
                            if (mCodeTimerCount > 0){
                                mHandler.sendEmptyMessageDelayed(1, 1000);
                            }else {
                                mPhoneCodeBtn.setText("获取验证码");
                                mPhoneCodeBtn.setEnabled(true);
                            }
                        }
                    };
                }

                mCodeTimerCount = 60;
                mHandler.sendEmptyMessage(1);
            }
        }).execute();
    }


    private void requestLogin(final String ...params){
        //发送打点事件
        try {
            Event event = Event.getEvent(EventCode.DATA_USER_LOGIN_START, mPkgName, mPadCode);
            if (params!=null && params.length >= 3){
                String type = params[0];
                String account = params[2];
                HashMap<String,String> ext = new HashMap<>();
                ext.put("type", type);
                ext.put("acct", account);
                event.setExt(ext);
            }
            MobclickAgent.sendEvent(event);
        }catch (Exception e){}

        if (mLoginBtn!=null){
            mLoginBtn.setEnabled(false);
        }
        mLoading = LoadingDialog.build(mActivity);
        mLoading.show();
        new RequestLoginTask(new RequestLoginTask.ICallback() {
            @Override
            public void onResult(HashMap<String, Object> map) {
                mLoading.dismiss();

                if (mLoginBtn!=null) {
                    mLoginBtn.setEnabled(true);
                }

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
                        if (params!=null && params.length >= 3){
                            String type = params[0];
                            String account = params[2];
                            HashMap<String,String> ext = new HashMap<>();
                            ext.put("type", type);
                            ext.put("acct", account);
                            event.setExt(ext);
                        }
                        MobclickAgent.sendEvent(event);
                    }catch (Exception e){}

                    return;
                }

                if (isShowing()){
                    dismiss();
                }

                if (mCallback!=null){
                    mCallback.onLoginSuccess(map);
                }

                try {
                    //发送打点事件
                    Event event = Event.getEvent(EventCode.DATA_USER_LOGIN_SUCCESS, mPkgName, mPadCode);
                    if (params!=null && params.length >= 3){
                        String type = params[0];
                        String account = params[2];
                        HashMap<String,String> ext = new HashMap<>();
                        ext.put("type", type);
                        ext.put("acct", account);
                        event.setExt(ext);
                    }
                    MobclickAgent.sendEvent(event);
                }catch (Exception e){}
            }
        }).execute(params);
    }

    public void requestUidLogin(String uninqueId){
        //发送请求
        requestLogin("kp", mCorpId, uninqueId);
    }

}

