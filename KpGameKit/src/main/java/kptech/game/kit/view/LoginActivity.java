package kptech.game.kit.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;


import java.util.HashMap;
import java.util.Map;

import kptech.game.kit.R;
import kptech.game.kit.msg.MsgManager;

public class LoginActivity extends Activity {
    private static final String TAG = "LoginActivity";
    private static final String RESET_PSW_URL = "https://auth-dev.kuaipantech.com/auth/#/password/update?type=2&access_token=";
    private static final String LOGIN_URL = "https://auth-dev.kuaipantech.com/auth/#/login?app_key=675ef2c4c4234c008e56c4fa837b03d2&type=2";
    private WebView webView;
    private TextView mTitle;

    private Context mContext;
//    protected LoginActivity(Context context) {
//        super(context);
//        this.mContext = context;
//    }

    public static void showRemindDialog(final Activity activity){
        Intent i = new Intent(activity, LoginActivity.class);
        activity.startActivity(i);
//        final Dialog dialog = new LoginActivity(activity);
//
//        Window window = dialog.getWindow();
//        if (window != null) {
//            window.getDecorView().setPadding(0, 0, 0, 0);
//            window.getDecorView().setBackgroundColor(Color.WHITE);
//            WindowManager.LayoutParams layoutParams = window.getAttributes();
//            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
//            window.setAttributes(layoutParams);
//        }

//        View contentView = LayoutInflater.from(activity).inflate(R.layout.activity_login, null);
//        contentView.findViewById(R.id.tv_back).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//        dialog.setContentView(contentView);
//        dialog.setCanceledOnTouchOutside(true);
//        dialog.setCancelable(true);
//
//        dialog.show();
    }

    public static void startActivityForResult(Activity activity, Intent data, int requestCode) {
//        //判断是否有网
//        if (!((AppApplication)activity.getApplication()).isNetworkConnected()){
//            Toast.makeText(activity, "网络不给力，请稍后重试！", Toast.LENGTH_LONG).show();
//            return;
//        }
//        Intent i = new Intent(activity, LoginActivity.class);
//        if (data != null) {
//            i.putExtras(data);
//        }
//        activity.startActivityForResult(i, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mTitle = findViewById(R.id.title);
//        TitleBar mTitleBar =  findViewById(R.id.title_bar);
//        mTitleBar.setTitle("登录");
//        mTitleBar.setOnBackClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });
//
//        boolean resetPsw = getIntent().getBooleanExtra("resetPsw", false);
        String url = LOGIN_URL;
//        if (resetPsw){
//            String token = "";
//            LoginUser loginUser = LoginUser.get();
//            if (loginUser!=null){
//                token = loginUser.token;
//            }
//            url = RESET_PSW_URL + token;
//        }else {
//            url = LOGIN_URL;
//        }
        webView = findViewById(R.id.webview);
        webView.loadUrl(url);
        webView.requestFocus(View.FOCUS_DOWN);
        initView();
    }

    private void initView() {
        //声明WebSettings子类
        WebSettings webSettings = webView.getSettings();

        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);

        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        //缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件

        //其他细节操作
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); //关闭webview中缓存
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
        //webView.addJavascriptInterface(new JsObject(), "client");
        webView.addJavascriptInterface(new LoginJavascript(), "android");
        webView.clearCache(true);
        //复写shouldOverrideUrlLoading()方法，使得打开网页时不调用系统浏览器， 而是在本WebView中显示
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, url);
                view.loadUrl(url);
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b2 = new AlertDialog.Builder(mContext)
                        .setTitle("提示").setMessage(message)
                        .setPositiveButton("ok",
                                new AlertDialog.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                });

                b2.setCancelable(false);
                b2.create();
                b2.show();
                return true;
            }
        });
    }

    class LoginJavascript {
        @JavascriptInterface
        public void userLogin(String token, String phone, String guid) {
            Log.i("LoginActivity: ", token);
//            MsgManager.sendLogin(token);
            finish();
        }
        @JavascriptInterface
        public void reLogin(){
//            finish();

        }

    }

}
