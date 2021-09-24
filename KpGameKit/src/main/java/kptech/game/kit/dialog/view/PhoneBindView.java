package kptech.game.kit.dialog.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import kptech.game.kit.R;
import kptech.game.kit.dialog.WebViewActivity;
import kptech.game.kit.utils.Logger;
import kptech.lib.analytic.Event;
import kptech.lib.analytic.EventCode;
import kptech.lib.analytic.MobclickAgent;
import kptech.lib.constants.Urls;
import kptech.lib.data.AccountTask;

public class PhoneBindView extends FrameLayout implements View.OnClickListener {

    private static final String TAG = "PhoneBindView";
    private static final String sourceStr = "阅读并同意《用户协议》和《隐私政策》";
    private final Context mContext;
    private final LayoutInflater inflater;
    private String mUninqueId;
    private String mCorpKey;
    private String mPkgName;
    private String mPadCode;
    private EditText mEtPBNum;
    private EditText mEtPBCode;
    private Button mBtnPBCode;
    private Button mBtnPBSumbit;
    private TextView mTvPBArgument;
    private Loading mLoading;

    private String smsCodeId = "";
    private boolean mLockCodeBtn = false;
    private int mCodeTimerCount = 60;
    private Handler mHandler = null;
    private OnPhoneBindListener mListener;

    public PhoneBindView(@NonNull Context context) {
        this(context, null);
    }

    public PhoneBindView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        inflater = LayoutInflater.from(context);
        init();
    }

    public void setOnPhoneBindListener(OnPhoneBindListener callback){
        mListener = callback;
    }

    public void setExtraDatas(String corpkey, String pkgName, String padCode,String uninqueId) {
        this.mCorpKey = corpkey;
        this.mPkgName = pkgName;
        this.mPadCode = padCode;
        this.mUninqueId = uninqueId;
    }

    private void init() {
        View view = inflater.inflate(R.layout.kp_view_phone_bind, this);
        mEtPBNum = view.findViewById(R.id.etPhoneBindPhoneNum);
        mEtPBCode = view.findViewById(R.id.etPhoneBindCode);
        mBtnPBCode = view.findViewById(R.id.btnPhoneBindCode);
        mBtnPBSumbit = view.findViewById(R.id.btnPhoneBindSubmit);
        mTvPBArgument = view.findViewById(R.id.tvArgument);

        mBtnPBCode.setEnabled(false);
        mBtnPBSumbit.setEnabled(false);

        setArgumentSpanStr();
        addTextListener();

        mBtnPBCode.setOnClickListener(this);
        mBtnPBSumbit.setOnClickListener(this);

    }

    private void addTextListener() {
        mEtPBNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mLockCodeBtn && s.length() == 11) {
                    mBtnPBCode.setEnabled(true);
                    String phoneVerCode = mEtPBCode.getText().toString().trim();
                    if (!phoneVerCode.isEmpty()) {
                        mBtnPBSumbit.setEnabled(true);
                    }
                } else {
                    mBtnPBCode.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mEtPBCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                mBtnPBSumbit.setEnabled((s.length() > 0) && (mEtPBNum.getText().toString().trim().length() == 11));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setArgumentSpanStr() {
        SpannableStringBuilder textSpan = new SpannableStringBuilder();
        textSpan.append(sourceStr);
        int firstStart = sourceStr.indexOf("《");
        int firstEnd = sourceStr.indexOf("》", firstStart) + 1;
        textSpan.setSpan(new ClickableSpan() {

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
                ds.setUnderlineText(false);
                ds.clearShadowLayer();
            }

        }, firstStart, firstEnd, 0);

        int secondStart = sourceStr.indexOf("《", firstEnd);
        int secondEnd = sourceStr.indexOf("》", secondStart) + 1;
        textSpan.setSpan(new ClickableSpan() {
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
                ds.setUnderlineText(false);
                ds.clearShadowLayer();
            }
        }, secondStart, secondEnd, 0);
        mTvPBArgument.setHighlightColor(Color.TRANSPARENT);
        mTvPBArgument.setMovementMethod(LinkMovementMethod.getInstance());
        mTvPBArgument.setText(textSpan, TextView.BufferType.SPANNABLE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnPhoneBindCode){
            getPhoneCode();
        }else if(id == R.id.btnPhoneBindSubmit){
            startBindPhoneLogin();
        }
    }

    /**
     * 提交绑定手机号
     */
    private void startBindPhoneLogin() {
        final String phone = mEtPBNum.getText().toString().trim();
        if ("".equals(phone) || phone.length() != 11){
            Toast.makeText(mContext, "请输入正确手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        String smsCode = mEtPBCode.getText().toString().trim();
        if ("".equals(smsCode)){
            Toast.makeText(mContext, "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Event event = Event.getEvent(EventCode.DATA_USER_LOGINPHONE_START, mPkgName, mPadCode);
            HashMap<String,String> ext = new HashMap<>();
            ext.put("acct", phone);
            event.setExt(ext);
            MobclickAgent.sendEvent(event);
        }catch (Exception e){
            e.printStackTrace();
        }

        if (mBtnPBSumbit != null){
            mBtnPBSumbit.setEnabled(false);
        }
        mLoading = Loading.build(mContext);
        mLoading.show();

        new AccountTask(mContext,AccountTask.ACTION_LOGIN_PHONE)
                .setCorpKey(mCorpKey)
                .setPkgName(mPkgName)
                .setCallback(new AccountTask.ICallback() {
            @Override
            public void onResult(Map<String, Object> map) {
                if(mLoading != null){
                    mLoading.dismiss();
                }
                if (mBtnPBSumbit != null){
                    mBtnPBSumbit.setEnabled(true);
                }

                String errMsg = null;
                if (map==null || map.size()<=0){
                    errMsg = "绑定手机号失败";
                }else if (map.containsKey("error")){
                    errMsg = map.get("error").toString();
                }

                if (errMsg != null){
                    Toast.makeText(mContext, errMsg, Toast.LENGTH_SHORT).show();
                    try {
                        //发送打点事件
                        Event event = Event.getEvent(EventCode.DATA_USER_LOGINPHONE_FAILED, mPkgName, mPadCode);
                        event.setErrMsg(errMsg);
                        HashMap<String,String> ext = new HashMap<>();
                        ext.put("acct", phone);
                        event.setExt(ext);
                        MobclickAgent.sendEvent(event);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if (mListener != null){
                        mListener.onPhoneBindFailed(map);
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
                }catch (Exception e){
                    e.printStackTrace();
                }

                try {
                    int reg = 0;
                    try {
                        if (map.containsKey("doreg")) {
                            reg = (int) map.get("doreg");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String eventCode = reg == 1 ? EventCode.DATA_USER_LOGINPHONE_SUCCESSREG : EventCode.DATA_USER_LOGINPHONE_SUCCESS;
                    //发送打点事件
                    Event event = Event.getEvent(eventCode, mPkgName, mPadCode);
                    HashMap<String,String> ext = new HashMap<>();
                    ext.put("acct", phone);
                    event.setExt(ext);
                    MobclickAgent.sendEvent(event);
                }catch (Exception e){
                    e.printStackTrace();
                }

                if (mListener != null){
                    mListener.onPhoneBindSuccess(map);
                }
            }
        }).execute(phone, smsCode, smsCodeId, mUninqueId);
    }

    /**
     * 获取验证码
     */
    private void getPhoneCode() {
        String phone = mEtPBNum.getText().toString().trim();
        if ("".equals(phone) || phone.length() != 11){
            Toast.makeText(mContext, "请输入手机号", Toast.LENGTH_LONG).show();
            return;
        }

        if (mBtnPBCode!=null){
            mBtnPBCode.setEnabled(false);
        }
        mLoading = Loading.build(mContext);
        mLoading.show();

        new AccountTask(mContext, AccountTask.ACTION_SENDSMS)
                .setCorpKey(mCorpKey)
                .setPkgName(mPkgName)
                .setCallback(new AccountTask.ICallback() {
                    @Override
                    public void onResult(Map<String, Object> map) {
                        if (mLoading != null){
                            mLoading.dismiss();
                        }

                        String errMsg = null;
                        if (map==null || map.size()<=0){
                            errMsg = "获取验证码失败";
                        }else if (map.containsKey("error")){
                            errMsg = map.get("error").toString();
                        }

                        if (errMsg != null){
                            mBtnPBCode.setEnabled(true);
                            Toast.makeText(mContext, errMsg, Toast.LENGTH_SHORT).show();

                            try {
                                //发送打点事件
                                Event event = Event.getEvent(EventCode.DATA_GET_CREDIT_FAILED);
                                event.setErrMsg(errMsg);
                                HashMap<String,String> ext = new HashMap<>();
                                ext.put("type", "phonelogin");
                                event.setExt(ext);
                                MobclickAgent.sendEvent(event);
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }

                            return;
                        }

                        if (map.containsKey("smsCodeId")){
                            smsCodeId = map.get("smsCodeId").toString();
                        }

                        //启动倒记时
                        if (mHandler == null){
                            mHandler = new Handler(Looper.getMainLooper()){
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void handleMessage(@NonNull Message msg) {
                                    mCodeTimerCount--;
                                    mBtnPBCode.setText(""+mCodeTimerCount+"秒");
                                    if (mCodeTimerCount > 0){
                                        mHandler.sendEmptyMessageDelayed(1, 1000);
                                    }else {
                                        mBtnPBCode.setText("获取验证码");
                                        mBtnPBCode.setEnabled(true);
                                        mLockCodeBtn = false;
                                    }
                                }
                            };
                        }

                        mCodeTimerCount = 60;
                        mHandler.sendEmptyMessage(1);
                        mLockCodeBtn = true;
                    }
                }).execute(phone, AccountTask.SENDSMS_TYPE_BINDPHONE);
    }


    public interface OnPhoneBindListener{
        void onPhoneBindSuccess(Map<String, Object> map);
        void onPhoneBindFailed(Map<String, Object> map);
    }
}
