package kptech.game.kit.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import kptech.game.kit.R;
import kptech.game.kit.data.RequestLoginTask;
import kptech.game.kit.msg.MsgManager;

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
        super(context, R.style.MyTheme_CustomDialog_LoginDialog);
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

