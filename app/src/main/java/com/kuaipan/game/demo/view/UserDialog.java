package com.kuaipan.game.demo.view;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kuaipan.game.demo.R;

import java.util.HashMap;

import kptech.game.kit.GameBoxManager;


public class UserDialog extends Dialog {
    public interface ICallback{
        void runGame();
    }

    private static final String TAG = "AlertDialog";
    private Activity mActivity;
    private EditText mUidText;

    private UserDialog.ICallback mCallback;
    public void setCallback(UserDialog.ICallback callback){
        mCallback = callback;
    }

    public UserDialog(Activity context) {
        super(context, R.style.UserDialog);
        this.mActivity = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_dialog);
        setCanceledOnTouchOutside(false);

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        mUidText = findViewById(R.id.uid);

        findViewById(R.id.kpbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GameBoxManager.getInstance(mActivity).setUniqueId(null);
                dismiss();
                //启动游戏
                if (mCallback!=null){
                    mCallback.runGame();
                }

            }
        });

        findViewById(R.id.lybtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid = mUidText.getText().toString();
                if (uid==null || "".equals(uid.trim())){
                    Toast.makeText(mActivity, "请输入联运Uid", Toast.LENGTH_LONG).show();
                    return;
                }
                GameBoxManager.getInstance(mActivity).setUniqueId(uid);

                dismiss();
                //启动游戏
                if (mCallback!=null){
                    mCallback.runGame();
                }

            }
        });
    }


}

