package com.yd.yunapp.gamebox;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.kuaipan.game.demo.R;

import java.util.Objects;

public class UserCertificationDialog extends Dialog {

    private TextInputEditText mEtUserName;
    private TextInputEditText mEtUserIdCard;
    private TextInputEditText mEtUserPhone;
    private TextView mTvCancel;
    private TextView mTvConfirm;
    private OnUserCerificationCallbck mCallback;

    public UserCertificationDialog(@NonNull Context context) {
        super(context,R.style.UserDialog);
    }

    public void setOnCallback(OnUserCerificationCallbck callback){
        this.mCallback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_user_certification);
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        if (window != null){
            window.setGravity(Gravity.CENTER);
        }
        initView();
        initEvent();
    }

    private void initEvent() {
        mTvCancel.setOnClickListener(v -> {
            dismiss();
            if (mCallback != null){
                mCallback.onUserCancel();
            }
        });

        mTvConfirm.setOnClickListener(v -> {
            String userName = Objects.requireNonNull(mEtUserName.getText()).toString().trim();
            String userIdCard = Objects.requireNonNull(mEtUserIdCard.getText()).toString().trim();
            String userPhone = Objects.requireNonNull(mEtUserPhone.getText()).toString().trim();
            if (userName.isEmpty() || userIdCard.isEmpty() || userPhone.isEmpty()){
                return;
            }
            dismiss();
            if (mCallback != null){
                mCallback.onUserConfirm(userName,userIdCard,userPhone);
            }
        });

    }

    private void initView() {
        mEtUserName = findViewById(R.id.etUserName);
        mEtUserIdCard = findViewById(R.id.etUserIdCard);
        mEtUserPhone = findViewById(R.id.etUserPhone);
        mTvCancel = findViewById(R.id.tvCancel);
        mTvConfirm = findViewById(R.id.tvConfirm);
        mEtUserName.requestFocus();
    }



    public interface OnUserCerificationCallbck{

        void onUserCancel();

        void onUserConfirm(String userName,String userIdCard,String userPhone);
    }
}
