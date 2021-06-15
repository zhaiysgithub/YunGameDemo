package com.yd.yunapp.gamebox.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.kuaipan.game.demo.R;
import com.yd.yunapp.gamebox.TestXiaoYuBean;

public class TransDialogActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dialog_half_trans);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Bundle bundleData = extras.getBundle("bundleData");
            String abc = bundleData.getString("abc");
            TestXiaoYuBean xiaoyuBean = (TestXiaoYuBean) bundleData.getSerializable("xiaoyuBundle");
        }
        findViewById(R.id.tvDialogText).setOnClickListener(v -> {
            //发送广播关闭页面关闭此页面
            setResult(9001, getIntent());
            TransDialogActivity.this.finish();
        });
    }
}
