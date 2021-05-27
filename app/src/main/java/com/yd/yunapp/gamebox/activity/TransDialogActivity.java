package com.yd.yunapp.gamebox.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.kuaipan.game.demo.R;

public class TransDialogActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dialog_half_trans);
        findViewById(R.id.tvDialogText).setOnClickListener(v -> {
            //发送广播关闭页面关闭此页面
            sendBroadCast();
            TransDialogActivity.this.finish();
        });
    }

    private void sendBroadCast() {
        //"KP_Cloud_Game_Play_StartActivity"
        Intent intent = new Intent();
        intent.setAction("KP_Cloud_Game_Play_StartActivity");
        intent.putExtra("className","com.yd.yunapp.gamebox.activity.XiaoYuOtherActivity");
        intent.putExtra("option","exit");
        sendBroadcast(intent);
    }
}
