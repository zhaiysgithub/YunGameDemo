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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import kptech.game.kit.GameBoxManager;
import kptech.game.kit.R;
import kptech.game.kit.activity.GamePlay;
import kptech.game.kit.utils.ProferencesUtils;
import kptech.lib.analytic.Event;
import kptech.lib.analytic.EventCode;
import kptech.lib.analytic.MobclickAgent;
import kptech.lib.constants.SharedKeys;
import kptech.lib.constants.Urls;
import kptech.lib.data.AccountTask;
import kptech.game.kit.dialog.WebViewActivity;

public class PhoneLoginView extends LinearLayout implements View.OnClickListener {
    private Context mActivity;

    private EditText mPLoginPhoneText;
    private EditText mPLoginCodeText;

    private Button mPLoginBtn;
    private Button mPLoginCodeBtn;

    private Loading mLoading;

    private Handler mHandler = null;

    private boolean mLockCodeBtn = false;
    private int mCodeTimerCount = 60;
    private String smsCodeId = "";

    private String mCorpKey;
    private String mPkgName;
    private String mPadCode;
    private String mUserSign;

    public interface OnLoginListener{
        void onLoginSuccess(Map<String, Object> map);
        void onLoginFailed(Map<String, Object> map);
    }
    private OnLoginListener mOnLoginListener;
    public void setOnLoginListener(OnLoginListener callback){
        mOnLoginListener = callback;
    }

    public PhoneLoginView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mActivity = context;
        init();
    }

    private void init(){
        View.inflate(mActivity, R.layout.kp_view_login_phone, this);
    }

    public void setCorpKey(String corpKey, String pkgName, String padCode){
        this.mCorpKey = corpKey;
        this.mPkgName = pkgName;
        this.mPadCode = padCode;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mPLoginPhoneText = findViewById(R.id.phone);
        mPLoginCodeText = findViewById(R.id.phone_code);
        mPLoginBtn = findViewById(R.id.submit_login);
        mPLoginBtn.setOnClickListener(this);
        mPLoginCodeBtn = findViewById(R.id.phone_code_btn);
        mPLoginCodeBtn.setOnClickListener(this);

        mPLoginCodeBtn.setEnabled(false);
        mPLoginBtn.setEnabled(false);

        mPLoginPhoneText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!mLockCodeBtn && charSequence.length() == 11){
                    mPLoginCodeBtn.setEnabled(true);
                    if (mPLoginCodeText.getText().toString().length() > 0){
                        mPLoginBtn.setEnabled(true);
                    }
                }else {
                    mPLoginCodeBtn.setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        mPLoginCodeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {  }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0 && mPLoginPhoneText.getText().toString().length() == 11){
                    mPLoginBtn.setEnabled(true);
                }else {
                    mPLoginBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        argumentView();

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.submit_login) {
            login();
        }else if (view.getId() == R.id.phone_code_btn){
            getPhoneCode();
        }
    }

    private void login(){
        String phone = mPLoginPhoneText.getText().toString().trim();
        if ("".equals(phone) || phone.length() != 11){
            Toast.makeText(mActivity, "请输入正确手机号", Toast.LENGTH_LONG).show();
            return;
        }
        String psw = mPLoginCodeText.getText().toString().trim();
        if ("".equals(psw)){
            Toast.makeText(mActivity, "请输入验证码", Toast.LENGTH_LONG).show();
            return;
        }

        requestLogin(phone, psw);
    }

    private void requestLogin(final String phone, String smsCode){


        try {
            Event event = Event.getEvent(EventCode.DATA_USER_LOGINPHONE_START, mPkgName, mPadCode);
            HashMap<String,String> ext = new HashMap<>();
            ext.put("acct", phone);
            event.setExt(ext);
            MobclickAgent.sendEvent(event);
        }catch (Exception e){}

        if (mPLoginBtn!=null){
            mPLoginBtn.setEnabled(false);
        }
        mLoading = Loading.build(mActivity);
        mLoading.show();

        String userSign = GameBoxManager.getInstance().getUniqueId();
        new AccountTask(mActivity, AccountTask.ACTION_LOGIN_PHONE)
                .setCorpKey(mCorpKey)
                .setPkgName(mPkgName)
                .setCallback(new AccountTask.ICallback() {
                    @Override
                    public void onResult(Map<String, Object> map) {
                        mLoading.dismiss();

                        if (mPLoginBtn!=null) {
                            mPLoginBtn.setEnabled(true);
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
                                Event event = Event.getEvent(EventCode.DATA_USER_LOGINPHONE_FAILED, mPkgName, mPadCode);
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
                            int reg = 0;
                            try {
                                if (map.containsKey("doreg")) {
                                    reg = (int) map.get("doreg");
                                }
                            } catch (Exception e) {
                            }
                            String eventCode = reg == 1 ? EventCode.DATA_USER_LOGINPHONE_SUCCESSREG : EventCode.DATA_USER_LOGINPHONE_SUCCESS;
                            //发送打点事件
                            Event event = Event.getEvent(eventCode, mPkgName, mPadCode);
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
                .execute(phone, smsCode, smsCodeId, userSign);
    }

    private void getPhoneCode(){
        String phone = mPLoginPhoneText.getText().toString().trim();
        if ("".equals(phone) || phone.length() != 11){
            Toast.makeText(mActivity, "请输入手机号", Toast.LENGTH_LONG).show();
            return;
        }

        requestPhoneCode(phone);
    }

    /**
     * 发送验证码
     * @param phone
     */
    private void requestPhoneCode(String phone){

        if (mPLoginCodeBtn!=null){
            mPLoginCodeBtn.setEnabled(false);
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

                        String errMsg = null;
                        if (map==null || map.size()<=0){
                            errMsg = "获取验证码失败";
                        }else if (map.containsKey("error")){
                            errMsg = map.get("error").toString();
                        }

                        if (errMsg != null){
                            mPLoginCodeBtn.setEnabled(true);
                            Toast.makeText(mActivity, errMsg, Toast.LENGTH_SHORT).show();

                            try {
                                //发送打点事件
                                Event event = Event.getEvent(EventCode.DATA_GET_CREDIT_FAILED);
                                event.setErrMsg(errMsg);
                                HashMap ext = new HashMap<>();
                                ext.put("type", "phonelogin");
                                MobclickAgent.sendEvent(event);
                            }catch (Exception ex){
                            }
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
                                    mPLoginCodeBtn.setText(""+mCodeTimerCount+"秒");
                                    if (mCodeTimerCount > 0){
                                        mHandler.sendEmptyMessageDelayed(1, 1000);
                                    }else {
                                        mPLoginCodeBtn.setText("获取验证码");
                                        mPLoginCodeBtn.setEnabled(true);
                                        mLockCodeBtn = false;
                                    }
                                }
                            };
                        }

                        mCodeTimerCount = 60;
                        mHandler.sendEmptyMessage(1);
                        mLockCodeBtn = true;
                    }
                })
                .execute(phone, AccountTask.SENDSMS_TYPE_PHONELOGIN);
    }

    public String getPhoneText() {
        if (mPLoginPhoneText!=null){
            return mPLoginPhoneText.getText().toString();
        }
        return "";
    }

    public void setPhoneText(String phone){
        if (mPLoginPhoneText!=null && phone!=null) {
            mPLoginPhoneText.setText(phone);
        }
    }

    private void argumentView(){
        TextView pswArgument = findViewById(R.id.argument);

        String str = "登录即代表同意《用户协议》和《隐私政策》";
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
}
