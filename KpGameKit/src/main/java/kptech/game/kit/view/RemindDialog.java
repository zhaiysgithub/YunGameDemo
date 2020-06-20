package kptech.game.kit.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import kptech.game.kit.R;

public class RemindDialog extends AlertDialog implements View.OnClickListener {
    private IRemindDialogCallback mCallback;
    public interface IRemindDialogCallback {
        void onSubmit();
        void onCancel();
    }

    public static void showRemindDialog(final Activity activity, IRemindDialogCallback callback){
        RemindDialog dialog = new RemindDialog(activity,callback);
        dialog.show();
    }

    protected RemindDialog(Context context, IRemindDialogCallback callback) {
        super(context, R.style.RemindDialog);
        this.mCallback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_dialog);
        initView();
        this.setCanceledOnTouchOutside(false);
    }

    public void initView() {
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.submit).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.cancel) {
            dismiss();
            if (mCallback!=null){
                mCallback.onCancel();
            }
        } else if (i == R.id.submit) {
            dismiss();
            if (mCallback!=null){
                mCallback.onSubmit();
            }
        }
    }
}
