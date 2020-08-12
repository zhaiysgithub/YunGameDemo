package kptech.game.kit.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import kptech.game.kit.R;
import kptech.game.kit.data.RequestLoginTask;
import kptech.game.kit.data.RequestPayTask;

public class PayDialog extends Dialog {
    private static final String DEFAULT_REFERER = "https://wxapp.kuaipantech.com";
    private String PAY_URLS = "https://wxapp.kuaipantech.com/h5demo/wxjspay/weixin_to_h5.php";

    public interface ICallback {
        void onResult(int ret, String msg);
    }

    private static final String TAG = "AlertDialog";
    private Activity mActivity;

    private ProgressBar mProBar;
    private WebView webView;
    private PayDialog.ICallback mCallback;
    private Button confirm_the_payment;//确认支付
    //    private String tradenum = "kp_27_20200627152653_407742";
    private int payState = 0;
    public String guid = "";
    public String productcode = "";
    public String cp_orderid = "";
    public String globaluserid = "";
    public String globalusername = "";
    public String cotype = "lianyun";
    private boolean choose = true;
    private MyRadioButton mWeChat_id, mtreasure_id;

    public void setCallback(PayDialog.ICallback callback) {
        mCallback = callback;
    }

    public PayDialog(Activity context) {
        super(context, R.style.MyTheme_CustomDialog_LoginDialog);
        this.mActivity = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_dialog);
        setCanceledOnTouchOutside(false);

        mProBar = findViewById(R.id.progressBar1);

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (mCallback != null) {
                    mCallback.onResult(1, "");
                }
            }
        });
        mWeChat_id = findViewById(R.id.mWeChat_id);
        mtreasure_id = findViewById(R.id.treasure_id);
        mWeChat_id.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                choose = isChecked;
            }
        });
//        调起支付
        confirm_the_payment = findViewById(R.id.confirm_the_payment);
        confirm_the_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (choose) {
                    //微信
                    PAY_URLS = "https://wxapp.kuaipantech.com/h5demo/wxjspay/weixin_to_h5.php";
                    weChatpay();
                } else {
                    //支付宝
                    PAY_URLS = "https://kppay.kuaipantech.com/alipay_wap/wappay/pay.php";
                    weChatpay();
                }
            }
        });

//        String url = PAY_URL + "?tradenum=" + tradenum;
        webView = findViewById(R.id.webview);
//        webView.loadUrl(url);
        initView();

    }



    private void weChatpay() {
        if (payState == 0) {
//                    String url = PAY_URL + "?tradenum=" + tradenum;
//                    webView.loadUrl(url);
            //处理按钮
            confirm_the_payment.setText("生成订单...");
            confirm_the_payment.setEnabled(false);
            payState = 1;

            //生成订单
            HashMap<String, String> map = new HashMap();
            map.put("productcode", productcode);
            map.put("cp_orderid", cp_orderid);
            map.put("guid", guid);
            map.put("globaluserid", globaluserid);
            map.put("globalusername", globalusername);
            map.put("cotype", cotype);
            new RequestPayTask(new RequestPayTask.ICallback() {
                @Override
                public void onResult(HashMap<String, String> map) {
                    //获取订单号
                    String msg = "";
                    int code = 0;
                    if (map == null) {
                        msg = "map null";
                    } else if (map.containsKey("tradenum")) {
                        code = 1;
                        String tradenum = map.get("tradenum");

                        //调用微信
                        confirm_the_payment.setText("支付中，请稍等...");
                        confirm_the_payment.setEnabled(false);
                        payState = 2;

                        String url = PAY_URLS + "?tradenum=" + tradenum;
                        webView.loadUrl(url);

                    } else {
                        if (map.containsKey("error")) {
                            msg = map.get("error");
                        } else {
                            msg = "error";
                        }
                        Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
                        confirm_the_payment.setEnabled(true);
                        confirm_the_payment.setText("生成订单失败，点击重试");
                        payState = 0;
                    }
                }
            }).execute(map);

        } else if (payState == 2) {
            dismiss();
            if (mCallback != null) {
                mCallback.onResult(1, "");
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && payState == 2) {
            confirm_the_payment.setText("支付完成");
            confirm_the_payment.setEnabled(true);
        }
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
        webView.addJavascriptInterface(new PayDialog.JavascriptCallback(), "android");
        webView.clearCache(true);
        //复写shouldOverrideUrlLoading()方法，使得打开网页时不调用系统浏览器， 而是在本WebView中显示
        webView.setWebViewClient(new WebViewClient() {
            String referer = DEFAULT_REFERER;

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    if (url.startsWith("weixin://") || url.startsWith("alipays://")) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        mActivity.startActivity(intent);
                        return true;
                    }
                } catch (Exception e) {
                    return false;
                }

                if (url.contains("https://wx.tenpay.com")) {
                    Map<String, String> extraHeaders = new HashMap<>();
                    extraHeaders.put("Referer", referer);
                    view.loadUrl(url, extraHeaders);
                    referer = url;
                    return true;
                }
                view.loadUrl(url);
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // TODO 自动生成的方法存根

                if (newProgress == 100) {
                    mProBar.setVisibility(View.INVISIBLE);//加载完网页进度条消失
                } else {
                    mProBar.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    mProBar.setProgress(newProgress);//设置进度值
                }

            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b2 = new AlertDialog.Builder(mActivity)
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

    class JavascriptCallback {
        @JavascriptInterface
        public void paySuccess(String token, String phone, String guid) {
        }
    }

}

