package kptech.game.kit.view;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import kptech.game.kit.R;

public class LoginDialog extends Dialog {
    public interface OnLoginListener{
        void onClick(String phone, String psw);
    }

    private static final String TAG = "AlertDialog";
    private Activity mActivity;
    private EditText mPhoneText;
    private EditText mPswText;

    private LoginDialog.OnLoginListener mCallback;
    public void setCallback(LoginDialog.OnLoginListener callback){
        mCallback = callback;
    }

    public LoginDialog(Activity context) {
        super(context, R.style.MyTheme_CustomDialog);
        this.mActivity = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_dialog);
        setCanceledOnTouchOutside(false);

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        mPhoneText = findViewById(R.id.phone);
        mPswText = findViewById(R.id.psw);

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    private void login(){
        String phone = mPhoneText.getText().toString().trim();
        if ("".equals(phone)){
            Toast.makeText(mActivity, "请输入手机号", Toast.LENGTH_LONG).show();
            return;
        }
        String psw = mPswText.getText().toString().trim();
        if ("".equals(psw)){
            Toast.makeText(mActivity, "请输入密码", Toast.LENGTH_LONG).show();
            return;
        }

        if (mCallback != null){
            mCallback.onClick(phone, psw);
        }
    }

}

