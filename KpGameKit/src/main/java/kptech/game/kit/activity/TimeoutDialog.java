package kptech.game.kit.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kp_dialog_timeout);

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
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

    }
}
