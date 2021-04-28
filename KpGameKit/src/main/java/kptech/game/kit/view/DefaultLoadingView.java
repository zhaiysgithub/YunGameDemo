package kptech.game.kit.view;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

import kptech.game.kit.R;
import kptech.game.kit.utils.DensityUtil;
import kptech.game.kit.utils.Logger;

public class DefaultLoadingView extends FrameLayout {

    private ProgressBar mLoadingPb;
    private TextView mLoadingText;
    private ImageView mIconImg;
    private TextView mNameText;
    private MyHandler mHandler;
    private ViewGroup mBottomLL;

    public static final int pbMax = 10000;

    public DefaultLoadingView(Context context) {
        super(context);
        initView();
        mHandler = new MyHandler(this);
    }

    public DefaultLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }
    private void initView() {
        View view = inflate(getContext(), R.layout.kp_view_default_loading, this);

        mIconImg = view.findViewById(R.id.game_icon);
        mNameText = view.findViewById(R.id.game_name);
        mLoadingText = view.findViewById(R.id.loading_txt);
        mLoadingPb = view.findViewById(R.id.loading_pb);
        mLoadingPb.setMax(pbMax);
        mBottomLL = view.findViewById(R.id.bottom_ll);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == GONE){
            mHandler.removeMessages(0);
            mHandler.removeMessages(1);
            mPro = 0;
            mPuasePro = true;
            mLoadingPb.setProgress(0);
        }else {
            mPuasePro = false;
            mHandler.sendEmptyMessageDelayed(1, 4000);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mBottomLL.getLayoutParams();
            lp.bottomMargin = DensityUtil.dip2px(getContext(), 20);
            mBottomLL.setLayoutParams(lp);
        }else {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mBottomLL.getLayoutParams();
            lp.bottomMargin = DensityUtil.dip2px(getContext(), 94);
            mBottomLL.setLayoutParams(lp);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

//    public void setProgress(int mPro) {
////        mLoadingPb.setProgress(mPro);
//    }

    private int mPro = 0;
    public void setProgressStatus(int status){
        Logger.info("LoadingView", "status: "+status);
        int pro = mPro;
        switch (status) {
            case PlayStatusLayout.STATUS_LOADING:
                pro = 0;
                break;
            case PlayStatusLayout.STATUS_LOADING_INIT:
                pro = (int) (pbMax * 0.01);
                break;
//            case PlayStatusLayout.STATUS_LOADING_GET_GAMEINFO:
//                pro = (int) (pbMax * 0.10);
//                break;
            case PlayStatusLayout.STATUS_LOADING_CONNECT_DEVICE:
                pro = (int) (pbMax * 0.15);
                break;
            case PlayStatusLayout.STATUS_LOADING_RECOVER_GAMEINFO:
                pro = (int) (pbMax * 0.30);
                break;
            case PlayStatusLayout.STATUS_LOADING_AD_PAUSE:
            case PlayStatusLayout.STATUS_LOADING_START_GAME:
                pro = (int) (pbMax * 0.60);
                break;
//            case PlayStatusLayout.STATUS_LOADING_START_GAME:
//                pro = (int) (pbMax * 0.70);
//                break;
            case PlayStatusLayout.STATUS_LOADING_FINISHED:
                pro = (int) (pbMax * 0.98);
                break;
        }
        mPro = pro;
        mHandler.sendMessage(Message.obtain(mHandler, 0, pro));
    }

    public void setText(String text) {
        mLoadingText.setText(text);
    }


    private void updateProgress(int mPro){
        mLoadingPb.setProgress(mPro);

        //延时更新进度
        if (!mPuasePro && mPro < pbMax){
            if (mPro < (int) (pbMax * 0.85) && mPro >= (int) (pbMax * 0.70)){
                mHandler.sendMessageDelayed(Message.obtain(mHandler, 0, (int)(mPro + ((pbMax - mPro) * 0.05))), 150);
            }else if (mPro < (int) (pbMax * 0.95) && mPro >= (int) (pbMax * 0.85)){
                mHandler.sendMessageDelayed(Message.obtain(mHandler, 0, (int)(mPro + ((pbMax - mPro) * 0.1))), 3000);
            }else if (mPro >= (int) (pbMax * 0.95)){
                mHandler.sendMessageDelayed(Message.obtain(mHandler, 0, (int)(mPro + 50)), 3000);
            }else {
                //计算延时时间
                mHandler.sendEmptyMessageDelayed(0, 100);
            }

        }
    }

    private boolean mPuasePro = false;

    public void setPauseProgress(boolean b){
        mPuasePro = b;
    }

    public void setInfo(String name, String iconUrl) {
        mNameText.setText(name);
        if (iconUrl!=null && !"".equals(iconUrl)){
            try {
                Picasso.with(getContext()).load(iconUrl).into(mIconImg);
            }catch (Exception e){}
        }
    }

    String[] bottomTextArr = new String[]{
            "提示：请关闭手机旋转设置，体验会更好",
            "提示：游戏加载不消耗流量哦",
            "提示：网络延迟过高，请切换手机网络",
    };

    int textPos = 0;
    private void updateBottomText(){
        try {
            int pos = (textPos + 1) % bottomTextArr.length;
            mLoadingText.setText(bottomTextArr[pos]);
            textPos = pos;
            if (isShown()){
                mHandler.sendEmptyMessageDelayed(1, 4000);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static class MyHandler extends Handler {
        private int mPro = 0;

        WeakReference<DefaultLoadingView> ref = null;

        private MyHandler(DefaultLoadingView view){
            super(Looper.getMainLooper());
            ref = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            if (ref == null || ref.get() == null){
                return;
            }

            if(!ref.get().isShown()){
                return;
            }


            if (msg.what == 0){
                if (msg.obj != null){
                    int obj = (int) msg.obj;
                    if (obj > mPro){
                        mPro = obj;
                    }
                }else {
                    int last =  pbMax - mPro; //剩余长度

                    int in = 1;
                    if (last < 200){
                        in = 0;
                    }else if (last < 1000) {
                        in = 2;
                    }else if (last < 3000){
                        in = 20;
                    }else{
                        in = last / 100;
                    }
                    mPro += in;
                }

                if (mPro > pbMax) {
                    mPro = pbMax;
                }

//                Logger.info("LoadingView", "Progress: "+mPro);

                ref.get().updateProgress(mPro);
            }else if (msg.what == 1){
                ref.get().updateBottomText();
            }


        }
    }
}
