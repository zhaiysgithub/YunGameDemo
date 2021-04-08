package kptech.game.kit.dialog.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import kptech.game.kit.R;
import kptech.lib.analytic.Event;
import kptech.lib.analytic.EventCode;
import kptech.lib.analytic.MobclickAgent;
import kptech.lib.constants.Urls;
import kptech.lib.data.AccountTask;
import kptech.game.kit.dialog.WebViewActivity;

public class PwdLoginView extends LinearLayout implements View.OnClickListener {

    private Context mActivity;

    public static final int TYPE_LOGIN = 1;
    public static final int TYPE_REGISAT = 2;
    public static final int TYPE_FORGET = 3;

    private ViewGroup mLoginLayout;
    private ViewGroup mRegistLayout;

    private EditText mLoginPhoneText;
    private EditText mLoginPswText;


    private TextView mArgument;
    private EditText mRegistPhoneText;
    private EditText mRegistCodeText;
    private EditText mRegistPswText;
    private EditText mRegistPsw1Text;

    private Button mLoginBtn;
    private Button mRegistBtn;
    private Button mPhoneCodeBtn;

    private int mType = TYPE_LOGIN;

    private String mCorpKey;
    private String mPkgName;
    private String mPadCode;

    private Loading mLoading;

    public interface RegistViewChangeListener{
        void onViewChange(int type);
    }
    private RegistViewChangeListener mRegistViewChangeListener;
    public void setRegistViewChangeListener(RegistViewChangeListener callback){
        mRegistViewChangeListener = callback;
    }

    public interface OnLoginListener{
        void onLoginSuccess(Map<String, Object> map);
        void onLoginFailed(Map<String, Object> map);
    }
    private OnLoginListener mOnLoginListener;
    public void setOnLoginListener(OnLoginListener callback){
        mOnLoginListener = callback;
    }

    public PwdLoginView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mActivity = context;
        init();
    }

    public void setCorpKey(String corpKey, String pkgName, String padCode){
        this.mCorpKey = corpKey;
        this.mPkgName = pkgName;
        this.mPadCode = padCode;
    }

    private void init(){
        View.inflate(mActivity, R.layout.kp_view_login_psw, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mLoginLayout = findViewById(R.id.login_layout);
        mRegistLayout = findViewById(R.id.regist_layout);


        mLoginPhoneText = mLoginLayout.findViewById(R.id.login_phone);
        mLoginPswText = mLoginLayout.findViewById(R.id.login_psw);
        mLoginBtn = mLoginLayout.findViewById(R.id.submit_login);
        mLoginBtn.setOnClickListener(this);
        mLoginLayout.findViewById(R.id.go_regist).setOnClickListener(this);
        mLoginLayout.findViewById(R.id.go_forget).setOnClickListener(this);
//        mLoginLayout.findViewById(R.id.go_phone_login).setOnClickListener(this);

        mLoginBtn.setEnabled(false);

        mLoginPhoneText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 11 && mLoginPswText.getText().toString().length() > 7){
                    mLoginBtn.setEnabled(true);
                }else {
                    mLoginBtn.setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        mLoginPswText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {  }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 7 && mLoginPhoneText.getText().toString().length() == 11){
                    mLoginBtn.setEnabled(true);
                }else {
                    mLoginBtn.setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });


        mArgument = mRegistLayout.findViewById(R.id.argument);
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

        argumentView();
    }

    private OnClickListener mGoPhoneLoginListener;
    public void setGoPhoneLoginListener(OnClickListener listener){
        mGoPhoneLoginListener = listener;
    }

    public String getPhoneText() {
        if (mLoginPhoneText!=null){
            return mLoginPhoneText.getText().toString();
        }
        return "";
    }

    public void setPhoneText(String phone){
        if (mLoginPhoneText!=null && phone!=null) {
            mLoginPhoneText.setText(phone);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.go_phone_login){
            if (mGoPhoneLoginListener!=null){
                mGoPhoneLoginListener.onClick(view);
            }
        }else if (view.getId() == R.id.submit_login) {
            login();
        }else if (view.getId() == R.id.submit_regist) {
            regist();
        }else if (view.getId() == R.id.phone_code_btn){
            getPhoneCode();
        }else if (view.getId() == R.id.go_login){
            changeLayout(TYPE_LOGIN);
        }else if (view.getId() == R.id.go_regist){
            changeLayout(TYPE_REGISAT);
        }else if (view.getId() == R.id.go_forget){
            changeLayout(TYPE_FORGET);
        }
    }


    private void changeLayout(int type){
        if (mType == type){
            return;
        }
        mType = type;
        switch (mType){
            case TYPE_LOGIN:
                mLoginLayout.setVisibility(View.VISIBLE);
                mRegistLayout.setVisibility(View.GONE);
                mLoginPhoneText.setText(mRegistPhoneText.getText().toString());
                mRegistPhoneText.setText(null);
                mRegistCodeText.setText(null);
                mRegistPswText.setText(null);
                mRegistPsw1Text.setText(null);
                break;
            case TYPE_FORGET:
                mLoginLayout.setVisibility(View.GONE);
                mRegistLayout.setVisibility(View.VISIBLE);
                mRegistBtn.setText("修改密码");
                mRegistPhoneText.setText(mLoginPhoneText.getText().toString());
                mArgument.setVisibility(GONE);
                break;
            case TYPE_REGISAT:
                mLoginLayout.setVisibility(View.GONE);
                mRegistLayout.setVisibility(View.VISIBLE);
                mRegistBtn.setText("立即注册");
                mRegistPhoneText.setText(mLoginPhoneText.getText().toString());
                mArgument.setVisibility(VISIBLE);
                break;
        }
        if (mRegistViewChangeListener!=null){
            mRegistViewChangeListener.onViewChange(mType);
        }

    }

    private void login(){
        String phone = mLoginPhoneText.getText().toString().trim();
        if ("".equals(phone) || phone.length() != 11){
            Toast.makeText(mActivity, "请输入正确手机号", Toast.LENGTH_LONG).show();
            return;
        }
        String psw = mLoginPswText.getText().toString().trim();
        if ("".equals(psw)){
            Toast.makeText(mActivity, "请输入密码", Toast.LENGTH_LONG).show();
            return;
        }

        requestLogin(phone, psw);
    }

    private void getPhoneCode(){
        String phone = mRegistPhoneText.getText().toString().trim();
        if ("".equals(phone) || phone.length() != 11){
            Toast.makeText(mActivity, "请输入正确手机号", Toast.LENGTH_LONG).show();
            return;
        }

        requestPhoneCode(phone);
    }

    private void regist(){
        String phone = mRegistPhoneText.getText().toString().trim();
        if ("".equals(phone) || phone.length() != 11){
            Toast.makeText(mActivity, "请输入正确手机号", Toast.LENGTH_LONG).show();
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

    private void requestLogin(final String phone, String pwd){

        try {
            Event event = Event.getEvent(EventCode.DATA_USER_LOGIN_START, mPkgName, mPadCode);
            HashMap<String,String> ext = new HashMap<>();
            ext.put("acct", phone);
            event.setExt(ext);
            MobclickAgent.sendEvent(event);
        }catch (Exception e){}


        if (mLoginBtn!=null){
            mLoginBtn.setEnabled(false);
        }
        mLoading = Loading.build(mActivity);
        mLoading.show();

        new AccountTask(mActivity, AccountTask.ACTION_LOGIN_PASSWORD)
                .setCorpKey(mCorpKey)
                .setPkgName(mPkgName)
                .setCallback(new AccountTask.ICallback() {
                    @Override
                    public void onResult(Map<String, Object> map) {
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
                                HashMap<String,String> ext = new HashMap<>();
                                ext.put("acct", phone);
                                event.setExt(ext);
                                MobclickAgent.sendEvent(event);
                            }catch (Exception e){}

                            if (mOnLoginListener!=null){
                                mOnLoginListener.onLoginFailed(map);
                            }

                            return;
                        }

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
                            //发送打点事件
                            Event event = Event.getEvent(EventCode.DATA_USER_LOGIN_SUCCESS, mPkgName, mPadCode);
                            HashMap<String,String> ext = new HashMap<>();
                            ext.put("acct", phone);
                            event.setExt(ext);
                            MobclickAgent.sendEvent(event);
                        }catch (Exception e){}

                        if (mOnLoginListener!=null){
                            mOnLoginListener.onLoginSuccess(map);
                        }
                    }
                })
                .execute(phone, pwd);
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
        mLoading = Loading.build(mActivity);
        mLoading.show();

        String action = mType == TYPE_FORGET ? AccountTask.ACTION_UPDATE_PWD : AccountTask.ACTION_REGIST;
        String codeId = smsCodeId.containsKey(action) ? smsCodeId.get(action) : "";

        new AccountTask(mActivity,action)
                .setCorpKey(mCorpKey)
                .setPkgName(mPkgName)
                .setCallback(new AccountTask.ICallback() {
                    @Override
                    public void onResult(Map<String, Object> map) {
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
                            changeLayout(TYPE_LOGIN);
                        }else {
                            Toast.makeText(mActivity, "注册成功", Toast.LENGTH_SHORT).show();

                            try {
                                //设置打点guid
                                if (map.containsKey("guid")){
                                    Object guid = map.get("guid");
                                    if (guid != null){
                                        Event.setGuid(guid+"");
                                    }
                                }
                            }catch (Exception e){}

                            //发送登录成功的消息
                            if (mOnLoginListener!=null){
                                mOnLoginListener.onLoginSuccess(map);
                            }

                            try {
                                //发送打点事件
                                Event event = Event.getEvent(EventCode.DATA_USER_REGIST_SUCCESS, mPkgName, mPadCode);
                                HashMap<String,String> ext = new HashMap<>();
                                ext.put("acct", phone);
                                event.setExt(ext);
                                MobclickAgent.sendEvent(event);
                            }catch (Exception e){}
                        }

                    }
                })
                .execute(phone, smsCode, codeId, psw);

    }

    private Handler mHandler = null;
    private int mCodeTimerCount = 60;
    private HashMap<String,String> smsCodeId = new HashMap();
    private void requestPhoneCode(String phone){

        if (mPhoneCodeBtn!=null){
            mPhoneCodeBtn.setEnabled(false);
        }
        mLoading = Loading.build(mActivity);
        mLoading.show();
        //发送请求
        new AccountTask(mActivity, AccountTask.ACTION_SENDSMS)
                .setCorpKey(mCorpKey)
                .setPkgName(mPkgName)
                .setCallback(new AccountTask.ICallback() {
                    @Override
                    public void onResult(Map<String, Object> map) {
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

                            try {
                                //发送打点事件
                                Event event = Event.getEvent(EventCode.DATA_GET_CREDIT_FAILED);
                                event.setErrMsg(error);
                                HashMap ext = new HashMap<>();
                                ext.put("type", "regist");
                                MobclickAgent.sendEvent(event);
                            }catch (Exception ex){
                            }

                            return;
                        }

                        if (map.containsKey("smsCodeId")){
                            String codeId = map.get("smsCodeId").toString();

                            String key = mType == TYPE_FORGET ? AccountTask.ACTION_UPDATE_PWD : AccountTask.ACTION_REGIST;
                            smsCodeId.put(key, codeId);
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
                })
                .execute(phone, mType == TYPE_FORGET ? AccountTask.SENDSMS_TYPE_UPDATEPSW : AccountTask.SENDSMS_TYPE_REGIST);
    }

    private void argumentView(){
        TextView pswArgument = findViewById(R.id.argument);

        String str = "注册即代表同意《用户协议》和《隐私政策》";
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(str);

        final int start = str.indexOf("《");//第一个出现的位置
        ssb.setSpan(new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(getContext(), WebViewActivity.class);
                intent.putExtra("url", Urls.LOGIN_ARGMENT_URL + "?page=userprotocal&client=androidsdk");
                getContext().startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#304ece"));       //设置文件颜色
//                ds.setFakeBoldText(true);
                // 去掉下划线
                ds.setUnderlineText(false);
            }

        }, start, start + 6, 0);
        ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), WebViewActivity.class);
                intent.putExtra("url", Urls.LOGIN_ARGMENT_URL + "?page=privatepolicy&client=androidsdk");
                getContext().startActivity(intent);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#304ece"));       //设置文件颜色
//                ds.setFakeBoldText(true);
                // 去掉下划线
                ds.setUnderlineText(false);
            }
        }, start + 7, start+13, 0);

        pswArgument.setMovementMethod(LinkMovementMethod.getInstance());
        pswArgument.setText(ssb, TextView.BufferType.SPANNABLE);
    }

//
//    public void requestUidLogin(String uninqueId){
//        //发送请求
//        requestLogin("kp", mCorpId, uninqueId);
//    }
}
