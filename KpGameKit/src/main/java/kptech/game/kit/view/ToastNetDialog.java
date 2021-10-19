package kptech.game.kit.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import kptech.game.kit.R;

public class ToastNetDialog extends Dialog {

    private final Handler mHandler;
    public static final String netTipStr = "检测到当前处于WiFi网络环境，为您自动下载完整游戏内容！";
    public static final String downFinishedTipsStr = "下载完成，点击“立即安装”解锁完整游戏内容";
    public ToastNetDialog(@NonNull Context context) {
        super(context);
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.kp_view_net_tips);

        Window window = getWindow();
        if (window != null){
            window.setDimAmount(0f);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setGravity(Gravity.CENTER);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
            attributes.gravity = Gravity.CENTER;
            window.setAttributes(attributes);
        }
        setCanceledOnTouchOutside(false);

    }

    public void showDialog(){
        try {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    show();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            cancel();
                        }
                    },2500);
                }
            },1000);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void cancel(){
        try {
            dismiss();
            if (mHandler != null){
                mHandler.removeCallbacksAndMessages(null);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
