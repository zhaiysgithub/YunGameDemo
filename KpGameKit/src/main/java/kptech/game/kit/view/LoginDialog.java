package kptech.game.kit.view;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

import kptech.game.kit.R;
import kptech.game.kit.data.RequestLoginTask;

public class LoginDialog extends Dialog {
    public interface ICallback{
        void onResult(int ret, String msg);
    }

    private static final String TAG = "AlertDialog";
    private Activity mActivity;
    private EditText mPhoneText;
    private EditText mPswText;

    private LoginDialog.ICallback mCallback;
    public void setCallback(LoginDialog.ICallback callback){
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

        new RequestLoginTask(new RequestLoginTask.ICallback() {
            @Override
            public void onResult(HashMap<String, String> map) {
                String msg = "";
                int code = 0;
                if (map == null){
                    msg = "map null";
                }else if (map.containsKey("access_token")){
                    code = 1;
                    msg = map.get("access_token");
                }else if (map.containsKey("error")){
                    msg = map.get("error");
                }else{
                    msg = "error";
                }

                if (mCallback!=null){
                    mCallback.onResult(code, msg);
                }
            }
        }).execute("kp",phone,psw);
    }

//    private void fullScreenImmersive(View view) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
//            view.setSystemUiVisibility(uiOptions);
//        }
//    }
//
//    @Override
//    public void show() {
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
//        super.show();
//        fullScreenImmersive(getWindow().getDecorView());
//        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
//    }
}

