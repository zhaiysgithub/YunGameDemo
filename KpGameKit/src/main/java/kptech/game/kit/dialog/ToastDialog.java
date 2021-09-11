package kptech.game.kit.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kptech.game.kit.R;
import kptech.game.kit.utils.DensityUtil;
import kptech.game.kit.utils.ProferencesUtils;

public class ToastDialog extends Dialog {

    private final Handler mHandler;
    public ToastDialog(@NonNull Context context) {
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
            window.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.y = DensityUtil.dip2px(getContext(),88);
            window.setAttributes(attributes);
        }
        setCanceledOnTouchOutside(false);

        TextView tvTips = findViewById(R.id.tvNetTips);
        tvTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

    }

    public void showDialog(String key){
        try {
            show();
            saveTipsCache(key);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    cancel();
                }
            },3000);
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

    private void saveTipsCache(String key){
        try {
            ProferencesUtils.remove(getContext(),key);
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            final String today = sdf.format(new Date());
            Map<String,Integer> map = new HashMap<>();
            map.put(today,1);
            ProferencesUtils.setString(getContext(), key,new JSONObject(map).toString());
        }catch (Exception e){
            e.printStackTrace();
        }

    }


}
