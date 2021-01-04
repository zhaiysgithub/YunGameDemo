package kptech.game.kit.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import kptech.game.kit.R;

public class TimeoutDialog extends Dialog {

    private View.OnClickListener mListener;
    private View.OnClickListener mReloadListener;

    public void setOnExitListener(View.OnClickListener listener) {
        this.mListener = listener;
    }

    public void setOnReloadListener(View.OnClickListener listener){
        this.mReloadListener = listener;
    }

    public TimeoutDialog(Context context) {
        super(context, R.style.MyTheme_CustomDialog_Background);
    }

    private String mTitle = null;
    public void setTitle(String msg) {
        this.mTitle = msg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kp_dialog_timeout);

        TextView tv = findViewById(R.id.title);
        if (mTitle != null){
            tv.setText(this.mTitle);
        }

        findViewById(R.id.exit_game).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (mListener != null){
                    mListener.onClick(view);
                }
            }
        });
        findViewById(R.id.reload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (mReloadListener != null){
                    mReloadListener.onClick(view);
                }
            }
        });
        setCanceledOnTouchOutside(false);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mListener != null){
            mListener.onClick(null);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
