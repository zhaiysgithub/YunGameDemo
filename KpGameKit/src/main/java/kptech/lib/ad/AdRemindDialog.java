package kptech.lib.ad;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;

import kptech.game.kit.R;

public class AdRemindDialog extends AlertDialog implements View.OnClickListener {
    public AdRemindDialog(Activity context) {
        super(context, R.style.RemindDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kp_ad_remind_dialog);
        initView();
        this.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mCancelListener!=null){
            mCancelListener.onClick(null);
        }
    }

    public void initView() {
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.submit).setOnClickListener(this);
    }

    private View.OnClickListener mSubmitListener;
    public AdRemindDialog setOnSubmitListener(View.OnClickListener listener){
        this.mSubmitListener = listener;
        return this;
    }

    private View.OnClickListener mCancelListener;
    public AdRemindDialog setOnCancelListener(View.OnClickListener listener){
        this.mCancelListener = listener;
        return this;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.cancel) {
            dismiss();
            if (mCancelListener!=null){
                mCancelListener.onClick(view);
            }
        } else if (i == R.id.submit) {
            dismiss();
            if (mSubmitListener!=null){
                mSubmitListener.onClick(view);
            }
        }
    }

}
