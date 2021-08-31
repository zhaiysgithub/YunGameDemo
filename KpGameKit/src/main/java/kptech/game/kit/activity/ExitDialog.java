package kptech.game.kit.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import kptech.game.kit.R;
import kptech.game.kit.utils.DensityUtil;
import kptech.game.kit.utils.VersionUtils;

public class ExitDialog extends Dialog {

    private static final String dialogMsgDefault = "确认退出云游戏吗？";
    private static final String dialogMsgXiaoYu = "退出后，本次试玩记录将无法保留！";

    private View.OnClickListener mListener;
    private String mText;

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

        Window window = getWindow();
        if (window != null){
            window.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            WindowManager.LayoutParams attributes = window.getAttributes();
            window.setAttributes(attributes);
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
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        /*TextView dialogTitle = findViewById(R.id.title);
        boolean xiaoYuChannel = VersionUtils.isXiaoYuChannel();
        if (xiaoYuChannel){
            dialogTitle.setText(dialogMsgXiaoYu);
        }else {
            dialogTitle.setText(dialogMsgDefault);
        }*/

        /*TextView tv = findViewById(R.id.text);
        if (mText != null && !mText.isEmpty() && !xiaoYuChannel){
            tv.setVisibility(View.VISIBLE);
            tv.setText(mText);
        }else {
            tv.setVisibility(View.GONE);
        }*/

    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

    }

    public void setText(String exitRemind) {
        this.mText = exitRemind;
    }
}
