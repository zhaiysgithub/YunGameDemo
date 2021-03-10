package kptech.game.kit.dialog;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import kptech.game.kit.R;
import kptech.game.kit.utils.DensityUtil;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.StringUtil;
import kptech.game.kit.utils.TToast;
import kptech.game.kit.view.popuplayout.PopupDialog;

public class RecordPublishPopup extends PopupDialog {
    private static final String TAG = RecordPublishPopup.class.getSimpleName();

    private EditText mTitleEdit;
    private TextView mCountText;

    public interface OnPublishListener{
        void onPublish(String title);
    }
    private OnPublishListener mListener;

    public RecordPublishPopup(@NonNull Context context) {
        super(context, R.style.MyTheme_CustomDialog);
        setContentLayout(R.layout.kp_dialog_record_publish);
        setWindowWidth(DensityUtil.getScreenWidth(getContext()));
        setWindowHeight(DensityUtil.getScreenHeight(getContext()));
        setUseRadius(false);
    }

    public void setOnPublishListener(OnPublishListener listener){
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitleEdit = findViewById(R.id.et_title);
        mCountText = findViewById(R.id.text_count);
        mTitleEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25)});
        mTitleEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCountText.setText(s.length()+"/25");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        findViewById(R.id.finish_publish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitleEdit.getText().toString();
                if (StringUtil.isEmpty(title)){
                    TToast.showCenterToast(getContext(), "请输入一个标题", Toast.LENGTH_SHORT);
                    return;
                }

                if (mListener != null){
                    mListener.onPublish(title);
                }

                dismiss();
            }
        });
    }
}
