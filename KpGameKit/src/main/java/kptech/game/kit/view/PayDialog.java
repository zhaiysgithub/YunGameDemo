package kptech.game.kit.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.net.URI;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import kptech.game.kit.R;
import kptech.game.kit.analytic.Event;
import kptech.game.kit.analytic.EventCode;
import kptech.game.kit.analytic.MobclickAgent;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.data.RequestPayDiscountTask;
import kptech.game.kit.data.RequestPayTask;
import kptech.game.kit.utils.IsInstallUtils;
import kptech.game.kit.utils.ProferencesUtils;

public class PayDialog extends Dialog {
    private static final String DEFAULT_REFERER = "https://wxapp.kuaipantech.com";
    private String PAY_URLS = "https://wxapp.kuaipantech.com/h5demo/Toc/androidpay/androidpay.php";

    private static int PAY_TYPE_WECHAT = 1;
    private static int PAY_TYPE_ALIPAY = 2;

    private static int PAY_STATE_NONE = 0;
    private static int PAY_STATE_CREATE_ORDER = 1;
    private static int PAY_STATE_LOAD_WEB = 2;
    private static int PAY_STATE_FINISHED = 3;
    private static int PAY_STATE_ERROR = 4;

    public interface ICallback {
        void onResult(int ret, String msg);
    }

    private static final String TAG = "AlertDialog";
    private Activity mActivity;

    private ProgressBar mProBar;
    private WebView webView;
    private PayDialog.ICallback mCallback;
    private Button mConfirmPaymentBtn;//确认支付
    private TextView mUserPhoneText;
    private TextView mProductNameText;
    private TextView mProductPriceText;
    private TextView mPayPriceText;

    public String mPadCode;

    public String guid = "";
    public String productcode = "";
    public String productname = "";
    public String productprice = "";
    public String cp_orderid = "";
    public String cpid = "";
    public String corpKey = "";
    public String gameId = "";
    public String gamePkg = "";
    public String gameName = "";
    public String phone = "";

    private RadioGroup mRadioGroup;
    private int mPayType = PAY_TYPE_WECHAT;
    private int payState = 0;

    public void setCallback(PayDialog.ICallback callback) {
        mCallback = callback;
    }

    public void setPadCode(String padCode){
        this.mPadCode = padCode;
    }

    public PayDialog(Activity context) {
        super(context, R.style.MyTheme_CustomDialog);
        this.mActivity = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_pay);
        setCanceledOnTouchOutside(false);

        mUserPhoneText = findViewById(R.id.user_acct);
        mProductNameText = findViewById(R.id.product_name);
        mProductPriceText = findViewById(R.id.product_price);
        mPayPriceText = findViewById(R.id.pay_price);

        String showPrice = "";
        try {
            double d = Double.parseDouble(productprice)/100.0;
            DecimalFormat df = new DecimalFormat("0.00");
            showPrice = df.format(d);
        }catch (Exception e){
            e.printStackTrace();
        }

        mUserPhoneText.setText(phone);
        mProductNameText.setText(productname);
        mProductPriceText.setText(showPrice);
        mPayPriceText.setText(showPrice);

        mConfirmPaymentBtn = findViewById(R.id.confirm_the_payment);
        mProBar = findViewById(R.id.progressBar1);

        RadioButton mRadioWechat = findViewById(R.id.radio_wechat);
        RadioButton mRadioAlipay = findViewById(R.id.radio_alipay);

        try {
            //通过配置文件，关闭支付方式
            String payConf  = ProferencesUtils.getString(mActivity, SharedKeys.KEY_PAY_CONF, null);
            if (payConf != null){
                JSONObject obj = new JSONObject(payConf);
                String wechat = obj.has("wechat") ? obj.getString("wechat") : "";
                String alipay = obj.has("alipay") ? obj.getString("alipay") : "";
                if (wechat.equals("0")){
                    mRadioWechat.setVisibility(View.GONE);

                    mPayType = PAY_TYPE_ALIPAY;
                    mRadioAlipay.setChecked(true);
                }
                if (alipay.equals("0")){
                    mRadioAlipay.setVisibility(View.GONE);

                    mPayType = PAY_TYPE_WECHAT;
                    mRadioWechat.setChecked(true);
                }
            }
        }catch (Exception e){}

        mRadioGroup = findViewById(R.id.radiogroup);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                //运行中不修改
                if (payState == PAY_STATE_CREATE_ORDER || payState == PAY_STATE_LOAD_WEB){
                    return;
                }

                if (id == R.id.radio_wechat){
                    mPayType = PAY_TYPE_WECHAT;
                }else if (id == R.id.radio_alipay){
                    mPayType = PAY_TYPE_ALIPAY;
                }

                if (payState == PAY_STATE_ERROR){
                    mConfirmPaymentBtn.setEnabled(true);
                    mConfirmPaymentBtn.setText("确认支付");
                    payState = PAY_STATE_NONE;
                }
            }
        });

        findViewById(R.id.yhj_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PayYouHuiDialog dialog = new PayYouHuiDialog(mActivity);
                dialog.show();
            }
        });

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        findViewById(R.id.confirm_the_payment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildOrder();
            }
        });

        webView = findViewById(R.id.webview);
        initView();

    }

    @Override
    public void dismiss() {
        super.dismiss();

        if (payState == PAY_STATE_FINISHED) {
            if (mCallback != null) {
                mCallback.onResult(1, "");
            }
        }
    }

    /**
     * 创建订单
     */
    private void buildOrder(){
        if (payState == PAY_STATE_FINISHED || payState == PAY_STATE_ERROR){
            dismiss();
            return;
        }

        if (payState == PAY_STATE_NONE) {
            if (mPayType == PAY_TYPE_ALIPAY){
                //判断是否安装支付宝
                if (!IsInstallUtils.isAliPayInstalled(mActivity)){
                    Toast.makeText(mActivity, "未安装支付宝", Toast.LENGTH_SHORT).show();
                    return;
                }
            }else if (mPayType == PAY_TYPE_WECHAT){
                if (!IsInstallUtils.isWeixinInstalled(mActivity)){
                    Toast.makeText(mActivity, "未安装微信", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            //处理按钮
            mConfirmPaymentBtn.setEnabled(false);
            mConfirmPaymentBtn.setText("生成订单...");
            payState = PAY_STATE_CREATE_ORDER;

            //生成订单
            HashMap<String, String> map = new HashMap();
            map.put("guid", guid);
            map.put("paytype",  (mPayType == PAY_TYPE_ALIPAY ? "3" : "1"));
            map.put("money", productprice);
            map.put("offcode", "nooff");
            map.put("cporder", cp_orderid);
            map.put("cpproductcode", productcode);
            map.put("gamepackage",gamePkg);
            map.put("gameid", gameId);
            map.put("gamename", gameName);
            map.put("cpid", cpid);
            map.put("clientid", corpKey);

            //发送打点事件
            try {
                Event event = Event.getEvent(EventCode.DATA_PAY_MAKETRADE_START, gamePkg, mPadCode);
                HashMap<String,String> ext = new HashMap<>();
                ext.put("guid", guid);
                ext.put("paytype", (mPayType == PAY_TYPE_ALIPAY ? "3" : "1"));
                ext.put("money", productprice);
                ext.put("cporder", cp_orderid);
                ext.put("cpproductcode", productcode);
                ext.put("cpid", cpid);
                event.setExt(ext);
                MobclickAgent.sendEvent(event);
            }catch (Exception e){}


            new RequestPayTask(new RequestPayTask.ICallback() {
                @Override
                public void onResult(HashMap<String, String> map) {
                    if (map!=null && map.containsKey("tradenum")) {
                        String tradenum = map.get("tradenum");

                        //发送打点事件
                        try {
                            Event event = Event.getEvent(EventCode.DATA_PAY_MAKETRADE_SUCCESS, gamePkg, mPadCode);
                            HashMap<String,String> ext = new HashMap<>();
                            ext.put("tradenum", tradenum);
                            event.setExt(ext);
                            MobclickAgent.sendEvent(event);
                        }catch (Exception e){}

                        //调用微信
                        mConfirmPaymentBtn.setText("支付中，请稍等...");
                        mConfirmPaymentBtn.setEnabled(false);
                        payState = PAY_STATE_LOAD_WEB;

                        String url = PAY_URLS + "?paytype=" + (mPayType == PAY_TYPE_ALIPAY ? "ZFB" : "WX") + "&tradenum=" + tradenum;
                        webView.loadUrl(url);
                        return;
                    }

                    //获取订单号
                    String msg = "";
                    if (map == null) {
                        msg = "map null";
                    } else {
                        if (map.containsKey("error")) {
                            msg = map.get("error");
                        } else {
                            msg = "error";
                        }
                    }
                    Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();

                    mConfirmPaymentBtn.setEnabled(true);
                    mConfirmPaymentBtn.setText("生成订单失败，点击重试");
                    payState = PAY_STATE_NONE;

                    //发送打点事件
                    try {
                        Event event = Event.getEvent(EventCode.DATA_PAY_MAKETRADE_FAILED, gamePkg, mPadCode);
                        event.setErrMsg(msg);
                        MobclickAgent.sendEvent(event);
                    }catch (Exception e){}
                }
            }).execute(map);
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
        if (hasFocus && payState == PAY_STATE_LOAD_WEB) {

            mConfirmPaymentBtn.setText("支付完成");
            mConfirmPaymentBtn.setEnabled(true);
            payState = PAY_STATE_FINISHED;

            try {
                //发送打点事件
                Event event = Event.getEvent(EventCode.DATA_PAY_APP_FINISH, gamePkg, mPadCode);
                MobclickAgent.sendEvent(event);
            }catch (Exception e){}
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

                        if (url.startsWith("alipays://")){
                            //关闭错误定时器
                            mHandler.removeMessages(MSG_ALIPAY_TIMEOUT_ERROR);
                        }

                        try {
                            //发送打点事件
                            String ecode = url.startsWith("weixin://") ? EventCode.DATA_PAY_APP_WX : EventCode.DATA_PAY_APP_ZFB ;
                            Event event = Event.getEvent(ecode, gamePkg, mPadCode);
                            HashMap<String,String> ext = new HashMap<>();
                            ext.put("url", url);
                            event.setExt(ext);
                            MobclickAgent.sendEvent(event);
                        }catch (Exception e){}

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

                //判断是否是错误界面
                if (url.contains("payerror.php")){
                    try {
                        Uri uri = Uri.parse(url);
                        String msg= uri.getQueryParameter("msg");
                        String errMsg = URLDecoder.decode(msg,"utf-8");

                        Toast.makeText(mActivity, errMsg, Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                    }

                    mConfirmPaymentBtn.setText("支付失败");
                    mConfirmPaymentBtn.setEnabled(true);
                    payState = PAY_STATE_ERROR;

                    return true;
                }

                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);

                String errMsg = "支付出错";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String des = error.getDescription().toString();
                    int code = error.getErrorCode();
                    errMsg = "支付出错,"+ code+"," +des;
                }

                String requestUri = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Uri uri = request.getUrl();
                    if (uri.toString().startsWith("weixin://")) {

                    }else if (uri.toString().startsWith("alipays://")) {

                    }
                    requestUri = uri.toString();
                }
//                else if (view.getUrl().startsWith("https://wx.tenpay.com")){
//                    urlStr = view.getUrl();
//                }

                Toast.makeText(mActivity, errMsg, Toast.LENGTH_SHORT).show();
                mConfirmPaymentBtn.setText("支付失败");
                mConfirmPaymentBtn.setEnabled(true);
                payState = PAY_STATE_ERROR;

                try {
                    //发送打点事件
                    Event event = Event.getEvent(EventCode.DATA_PAY_APP_ERROR, gamePkg, mPadCode);
                    event.setErrMsg(errMsg);
                    HashMap<String,String> ext = new HashMap<>();
                    ext.put("webviewUrl", view.getUrl());
                    ext.put("requestUri", requestUri);
                    event.setExt(ext);
                    MobclickAgent.sendEvent(event);
                }catch (Exception e){}
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                String str = view.getUrl();
                if (str.startsWith("https://openapi.alipay.com/gateway.do") ){
                    //支付宝页面，启动错误定时器
                    mHandler.sendEmptyMessageDelayed(MSG_ALIPAY_TIMEOUT_ERROR, 10 * 1000);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
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

    private static final int MSG_ALIPAY_TIMEOUT_ERROR = 1001;
    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MSG_ALIPAY_TIMEOUT_ERROR:
                    Toast.makeText(mActivity, "支付失败，支付宝出错", Toast.LENGTH_SHORT).show();
                    mConfirmPaymentBtn.setText("支付失败");
                    mConfirmPaymentBtn.setEnabled(true);
                    payState = PAY_STATE_ERROR;
                    break;
            }
        }
    };
}

