package kptech.game.kit.view;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import kptech.game.kit.R;
import kptech.game.kit.dialog.RecordPublishPopup;
import kptech.game.kit.utils.StringUtil;
import kptech.game.kit.utils.TToast;

public class RecordPublishView extends LinearLayout {

    private EditText mTitleEdit;
    private TextView mCountText;

    private OnPublishListener mListener;
    public void setOnPublishListener(OnPublishListener listener) {
        mListener = listener;
    }

    interface OnPublishListener {
        void onPublish(String title);
    }

    public RecordPublishView(Context context) {
        super(context);
    }

    public RecordPublishView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView(){
        inflate(getContext(), R.layout.kp_dialog_record_publish, this);

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

    public void show() {
        if (this.getVisibility() == View.VISIBLE){
            return;
        }
        this.setVisibility(VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.kp_view_enter_bottom);
        this.startAnimation(animation);
    }

    public void dismiss(){
        if (this.getVisibility() != View.VISIBLE){
            return;
        }

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.kp_view_exit_bottom);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                RecordPublishView.this.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        this.startAnimation(animation);
        mTitleEdit.setText(null);
        mTitleEdit.clearFocus();
        try {
            if (getContext() instanceof Activity){
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(((Activity)getContext()).getWindow().getDecorView().getWindowToken(), 0);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



}
