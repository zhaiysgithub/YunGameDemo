package kptech.game.kit.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import kptech.game.kit.R;

public class ExitDialog extends Dialog {

    private View.OnClickListener mListener;

    public void setOnExitListener(View.OnClickListener listener) {
        this.mListener = listener;
    }

    public ExitDialog(Context context) {
        super(context, R.style.MyTheme_CustomDialog_Background);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kp_dialog_exit);

        findViewById(R.id.exit_game).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (mListener != null){
                    mListener.onClick(view);
                }
            }
        });
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

    }
}
