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

    private String mCorpId;

    private LoginDialog.OnLoginListener mCallback;
    public void setCallback(LoginDialog.OnLoginListener callback){
        mCallback = callback;
    }

    public LoginDialog(Activity context, String corpId) {
        super(context, R.style.MyTheme_CustomDialog);
        this.mActivity = context;
        this.mCorpId = corpId;
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

    private void requestRegist(String phone, String psw, String smsCode){
        if (mRegistBtn!=null){
            mRegistBtn.setEnabled(false);
        }

        String action = mType == TYPE_FORGET ? RequestPhoneCodeTask.ACTION_FORGET : RequestPhoneCodeTask.ACTION_REGIST;
        new RequestRegistTask(mCorpId, action, phone, psw, smsCode, smsCodeId, new RequestRegistTask.ICallback() {
            @Override
            public void onResult(HashMap<String, Object> map) {
                if (mRegistBtn!=null){
                    mRegistBtn.setEnabled(true);
                }

                if (map == null){
                    Toast.makeText(mActivity, mType == TYPE_FORGET ? "修改密码失败" : "注册失败", Toast.LENGTH_LONG).show();
                    return;
                }

                if (map.containsKey("error")){
                    String error = map.get("error").toString();
                    Toast.makeText(mActivity, error, Toast.LENGTH_LONG).show();
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

        //发送请求
        String action = mType == TYPE_FORGET ? RequestPhoneCodeTask.ACTION_FORGET : RequestPhoneCodeTask.ACTION_REGIST;
        new RequestPhoneCodeTask(mCorpId, action, phone, new RequestPhoneCodeTask.ICallback() {
            @Override
            public void onResult(HashMap<String, Object> map) {
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


    private void requestLogin(String ...params){
        if (mLoginBtn!=null){
            mLoginBtn.setEnabled(false);
        }
        new RequestLoginTask(new RequestLoginTask.ICallback() {
            @Override
            public void onResult(HashMap<String, Object> map) {
                if (mLoginBtn!=null) {
                    mLoginBtn.setEnabled(true);
                }

                if (map==null){
                    Toast.makeText(mActivity, "登录失败", Toast.LENGTH_LONG).show();
                    return;
                }
                if (map != null && map.containsKey("error")){
                    String error = map.get("error").toString();
                    Toast.makeText(mActivity, error, Toast.LENGTH_LONG).show();
                    return;
                }

                if (isShowing()){
                    dismiss();
                }

                if (mCallback!=null){
                    mCallback.onLoginSuccess(map);
                }
            }
        }).execute(params);
    }

    public void requestUidLogin(String uninqueId){
        //发送请求
        requestLogin("kp", mCorpId, uninqueId);
    }

}

