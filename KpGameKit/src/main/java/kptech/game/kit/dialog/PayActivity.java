package kptech.game.kit.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import kptech.game.kit.R;
import kptech.lib.analytic.Event;
import kptech.lib.analytic.EventCode;
import kptech.lib.analytic.MobclickAgent;
import kptech.lib.constants.Urls;
import kptech.lib.data.AccountTask;
import kptech.game.kit.utils.IsInstallUtils;
import kptech.game.kit.utils.Logger;

public class PayActivity extends Dialog implements View.OnClickListener {
    public interface ICallback {
        void onResult(int payState, String err, Map<String,Object> map);
    }

    private static final String TAG = "PayActivity";

    private static int PAY_TYPE_WECHAT = 1;
    private static int PAY_TYPE_ALIPAY = 3;

    public static int PAY_STATE_NONE = 0;
    public static int PAY_STATE_CREATE_ORDER = 1;
    public static int PAY_STATE_LOAD_WEB = 2;
    public static int PAY_STATE_FINISHED = 3;
    public static int PAY_STATE_ERROR = 4;

    private ProgressBar mProBar;
    private WebView webView;

    private Button mConfirmPaymentBtn;//确认支付
    private TextView mUserPhoneText;
    private TextView mProductNameText;
    private TextView mProductPriceText;
    private TextView mPayPriceText;

    private HashMap<String,Object> mParams;
    private int mPayState = 0;

    public String guid = "";
    public String phone = "";

    private Activity mActivity;
    private String mGameId;
    private String mPkgName;
    private String mPadCode;
    public String mCorpKey;

    private RadioGroup mRadioGroup;
    private int mPayType = PAY_TYPE_WECHAT;

    private String err = null;

    private String mCpOrderId;
    private String mTradeNum;

    private OnDismissListener mOnDismissListener;

    //重定向的 url
    private String webClientRedirectUrl;
    //当前 pageFinished 的 url
    private String webClientPageFinishedUrl;

    @Override
    public void setOnDismissListener(OnDismissListener listener) {
        this.mOnDismissListener = listener;
    }

    private ICallback mCallback;
    public void setCallback(ICallback callback) {
        mCallback = callback;
    }

    public PayActivity(Activity context, String corpId, String gameId, String pkgName, String padCode) {
        super(context, R.style.MyTheme_CustomDialog_Background);
        this.mActivity = context;
        this.mCorpKey = corpId;
        this.mPkgName = pkgName;
        this.mPadCode = padCode;
        this.mGameId = gameId;
    }

    public void setParams(Map params){
        this.mParams = new HashMap<>(params);
    }

    public void setUserInfo(String guid, String phone){
        this.guid = guid;
        this.phone = phone;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kp_activity_pay);
        setCanceledOnTouchOutside(false);
        try {
            //发送打点事件
            Event event = Event.getEvent(EventCode.DATA_DIALOG_PAY_DISPLAY);
            MobclickAgent.sendEvent(event);
        }catch (Exception ex){
            Logger.error(TAG,ex.getMessage());
        }

        findViewById(R.id.close).setOnClickListener(this);
        findViewById(R.id.confirm_the_payment).setOnClickListener(this);

        super.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                try {
                    //发送打点事件
                    Event event = Event.getEvent(EventCode.DATA_DIALOG_PAY_CLOSE);
                    MobclickAgent.sendEvent(event);
                }catch (Exception ex){
                    Logger.error(TAG,ex.getMessage());
                }


                if (mCallback != null) {
                    String cpOrderID = (mParams!=null && mParams.containsKey("orderID")) ?  mParams.get("orderID")+"" : "";
                    Map<String, Object> map = new HashMap<>();
                    map.put("cporder", cpOrderID);

                    if (mPayState == PAY_STATE_FINISHED){
                        map.put("tradenum", mTradeNum);
                        mCallback.onResult(1, "", map);
                    }else {
                        String errStr = mPayState == PAY_STATE_NONE ? "cancel" : err;
                        mCallback.onResult(0, errStr, map);
                    }
                }

                if (mOnDismissListener!=null){
                    mOnDismissListener.onDismiss(dialogInterface);
                }
            }
        });


        mUserPhoneText = findViewById(R.id.user_acct);
        mProductNameText = findViewById(R.id.product_name);
        mProductPriceText = findViewById(R.id.product_price);
        mPayPriceText = findViewById(R.id.pay_price);

        mConfirmPaymentBtn = findViewById(R.id.confirm_the_payment);
        mProBar = findViewById(R.id.progressBar1);
        mRadioGroup = findViewById(R.id.radiogroup);

        webView = findViewById(R.id.webview);

        String productname = mParams.containsKey("productname") ? (String) mParams.get("productname") : "";
        String productprice = mParams.containsKey("money") ?  mParams.get("money")+"" : "";

        String showPrice = "";
        try {
            double d = Double.parseDouble(productprice)/100.0;
            DecimalFormat df = new DecimalFormat("0.00");
            showPrice = df.format(d);
        }catch (Exception e){
            e.printStackTrace();
        }

        //显示数据
        mUserPhoneText.setText(phone);
        mProductNameText.setText(productname);
        mProductPriceText.setText(showPrice);
        mPayPriceText.setText(showPrice);

//        mParams.put("productname","aa");
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                //运行中不修改
                if (mPayState == PAY_STATE_CREATE_ORDER || mPayState == PAY_STATE_LOAD_WEB){
                    return;
                }

                if (id == R.id.radio_wechat){
                    mPayType = PAY_TYPE_WECHAT;
                }else if (id == R.id.radio_alipay){
                    mPayType = PAY_TYPE_ALIPAY;
                }

                if (mPayState == PAY_STATE_ERROR){
                    mConfirmPaymentBtn.setEnabled(true);
                    mConfirmPaymentBtn.setText("确认支付");
                    mPayState = PAY_STATE_NONE;
                    err = null;
                }
            }
        });

        initWebView();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.close) {
            dismiss();
        }else if (view.getId() == R.id.confirm_the_payment){
            if (mPayState == PAY_STATE_FINISHED || mPayState == PAY_STATE_ERROR){
                //点关闭按钮
                dismiss();
                return;
            }

            buildOrder();
        }
    }

    /**
     * 创建订单
     */
    private void buildOrder(){
        Logger.info(TAG,"buildOrder payState:" + mPayState);

        if (mPayState == PAY_STATE_NONE) {
            if (mPayType == PAY_TYPE_ALIPAY){
                //判断是否安装支付宝
                if (!IsInstallUtils.isAliPayInstalled(this.mActivity)){
                    Logger.error(TAG,"buildOrder error: 未安装支付宝 ");
                    Toast.makeText(this.mActivity, "未安装支付宝", Toast.LENGTH_SHORT).show();
                    return;
                }
            }else if (mPayType == PAY_TYPE_WECHAT){
                if (!IsInstallUtils.isWeixinInstalled(this.mActivity)){
                    Logger.error(TAG,"buildOrder error: 未安装微信");
                    Toast.makeText(this.mActivity, "未安装微信", Toast.LENGTH_SHORT).show();
                    return;
                }
            }



            try {
                String cpInfo = new JSONObject(mParams).toString();
                Logger.info(TAG,"buildOrder cpInfo:" + cpInfo);



                //发送打点事件
                Event event = Event.getEvent(EventCode.DATA_PAY_MAKETRADE_START);
                HashMap<String,Object> ext = new HashMap<>(mParams);
                ext.put("payType",mPayType);
                event.setExt(ext);
                MobclickAgent.sendEvent(event);
            }catch (Exception ex){
                Logger.error(TAG,ex.getMessage());
            }

            //处理按钮
            mConfirmPaymentBtn.setEnabled(false);
            mConfirmPaymentBtn.setText("生成订单...");
            mPayState = PAY_STATE_CREATE_ORDER;

            mTradeNum = null;
            //生成订单
            new AccountTask(mActivity, AccountTask.ACTION_PAY_ORDER)
                    .setCorpKey(mCorpKey)
                    .setCallback(new AccountTask.ICallback() {
                        @Override
                        public void onResult(Map<String, Object> map) {
                            Logger.info(TAG,"buildOrder resp:" + map!=null ? map.toString() : null );

                            if (map!=null && map.containsKey("tradenum")) {

                                try {
                                    //发送打点事件
                                    Event event = Event.getEvent(EventCode.DATA_PAY_MAKETRADE_SUCCESS);
                                    event.setExt(map);
                                    MobclickAgent.sendEvent(event);
                                }catch (Exception ex){
                                    Logger.error(TAG,ex.getMessage());
                                }

                                String tradenum = map.get("tradenum").toString();
                                mTradeNum = tradenum;

                                //调用微信
                                mConfirmPaymentBtn.setText("支付中，请稍等...");
                                mConfirmPaymentBtn.setEnabled(false);
                                mPayState = PAY_STATE_LOAD_WEB;

                                String url = Urls.PAY_URL + "?paytype=" + (mPayType == PAY_TYPE_ALIPAY ? "ZFB" : "WX") + "&tradenum=" + tradenum;
                                webView.loadUrl(url);

                                return;
                            }

                            //获取订单号
                            String msg = "";
                            if (map == null) {
                                msg = "map null";
                            } else {
                                if (map.containsKey("error")) {
                                    msg = map.get("error").toString();
                                } else {
                                    msg = "error";
                                }
                            }
                            if ("".equals(msg)){
                                msg = "生成订单失败";
                            }
                            Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();

                            mConfirmPaymentBtn.setEnabled(true);
                            mConfirmPaymentBtn.setText("生成订单失败，点击重试");
                            mPayState = PAY_STATE_NONE;

                            try {
                                //发送打点事件
                                Event event = Event.getEvent(EventCode.DATA_PAY_MAKETRADE_FAILED);
                                event.setErrMsg(msg);
                                MobclickAgent.sendEvent(event);
                            }catch (Exception ex){
                                Logger.error(TAG,ex.getMessage());
                            }

                        }
                    }).execute(guid, mPayType+"", mParams, mGameId, mPkgName.trim());
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && mPayState == PAY_STATE_LOAD_WEB) {
            mConfirmPaymentBtn.setText("操作完成");
            mConfirmPaymentBtn.setEnabled(true);
            mPayState = PAY_STATE_FINISHED;

            try {
                //发送打点事件
                Event event = Event.getEvent(EventCode.DATA_PAY_APP_FINISH, mPkgName, mPadCode);
                MobclickAgent.sendEvent(event);
            }catch (Exception e){}
        }
    }

    private void initWebView() {
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
        webSettings.setAllowFileAccess(false); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
        webSettings.setSavePassword(false);
        //webView.addJavascriptInterface(new JsObject(), "client");
        webView.addJavascriptInterface(new JavascriptCallback(), "android");
        webView.clearCache(true);
        //复写shouldOverrideUrlLoading()方法，使得打开网页时不调用系统浏览器， 而是在本WebView中显示
        webView.setWebViewClient(new WebViewClient() {
            String referer = Urls.DEFAULT_REFERER;

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                webClientRedirectUrl = url;
                try {
                    if (url.startsWith("weixin://") || url.startsWith("alipays://")) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        mActivity.startActivity(intent);

                        try {
                            //发送打点事件
                            String code = EventCode.DATA_PAY_APP_START;
                            if (url.startsWith("weixin://")){
                                code = EventCode.DATA_PAY_APP_WXSTART;
                            }else if (url.startsWith("alipays://")){
                                code = EventCode.DATA_PAY_APP_ZFBSTART;
                            }
                            Event event = Event.getEvent(code);
                            HashMap<String,Object> ext = new HashMap<>();
                            ext.put("uri", url);
                            MobclickAgent.sendEvent(event);
                        }catch (Exception ex){
                            Logger.error(TAG,ex.getMessage());
                        }
                        return true;
                    }
                }catch (ActivityNotFoundException ex) {
                    ex.printStackTrace();
                }
                catch (Exception e) {
                    e.printStackTrace();
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
                    String errMsg = "支付失败";
                    try {
                        Uri uri = Uri.parse(url);
                        String msg= uri.getQueryParameter("msg");
                        errMsg = URLDecoder.decode(msg,"utf-8");

                        Toast.makeText(mActivity, errMsg, Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                    }

                    mConfirmPaymentBtn.setText("支付失败");
                    mConfirmPaymentBtn.setEnabled(true);
                    mPayState = PAY_STATE_ERROR;
                    err = errMsg;

                    Logger.error(TAG,"payerror  " + url);

                    try {
                        //发送打点事件
                        Event event = Event.getEvent(EventCode.DATA_PAY_WEB_FAILED);
                        event.setErrMsg(errMsg);
                        HashMap<String,Object> ext = new HashMap<>();
                        event.setExt(ext);
                        MobclickAgent.sendEvent(event);
                    }catch (Exception ex){
                        Logger.error(TAG,ex.getMessage());
                    }

                    return true;
                }

                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);

                String msg = "支付失败";
                String requestUri = null;
                String type = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Uri uri = request.getUrl();
                    if (uri.toString().startsWith("weixin://")) {
                        msg = "支付失败，未安装微信";
                        type = "wx";
                    }else if (uri.toString().startsWith("alipays://")) {
                        msg = "支付失败，未安装支付宝";
                        type = "zfb";
                    }
                    requestUri = uri.toString();
                }
                else{
                    requestUri = view.getUrl();
                }

                Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
                mConfirmPaymentBtn.setText("支付失败");
                mConfirmPaymentBtn.setEnabled(true);
                mPayState = PAY_STATE_ERROR;
                err = msg;

                try {
                    //发送打点事件
                    String code = EventCode.DATA_PAY_APP_FAILED;
                    if (type!=null){
                        if ("wx".equals(type)){
                            code = EventCode.DATA_PAY_APP_WXFAILED;
                        }else if ("zfb".equals(type)){
                            code = EventCode.DATA_PAY_APP_ZFBFAILED;
                        }
                    }
                    Event event = Event.getEvent(code);
                    event.setErrMsg(msg);
                    HashMap<String,Object> ext = new HashMap<>();
                    ext.put("uri", requestUri);
                    MobclickAgent.sendEvent(event);
                }catch (Exception ex){
                    Logger.error(TAG,ex.getMessage());
                }

                Logger.error(TAG,"onReceivedError: " + msg + "\n" + " url:"+ requestUri);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webClientPageFinishedUrl = url;
            }

        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // TODO 自动生成的方法存根
                if (newProgress == 100) {
                    mProBar.setVisibility(View.INVISIBLE);//加载完网页进度条消失
                    //支付出现异常错误
                    if (webClientPageFinishedUrl.startsWith(Urls.PAY_URL)
                            && !webClientPageFinishedUrl.equals(webClientRedirectUrl)
                            && mPayState == PAY_STATE_LOAD_WEB){
                        mConfirmPaymentBtn.setEnabled(true);
                        mConfirmPaymentBtn.setText("生成订单失败，点击重试");
                        mPayState = PAY_STATE_NONE;
                    }
                } else {
                    mProBar.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    mProBar.setProgress(newProgress);//设置进度值
                }

            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b2 = new AlertDialog.Builder( mActivity)
                        .setTitle("提示").setMessage(message)
                        .setPositiveButton("ok",
                                new DialogInterface.OnClickListener() {

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
