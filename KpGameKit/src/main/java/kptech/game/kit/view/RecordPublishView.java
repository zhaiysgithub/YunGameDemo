package kptech.game.kit.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import kptech.game.kit.R;
import kptech.game.kit.dialog.RecordPublishPopup;
import kptech.game.kit.utils.DensityUtil;
import kptech.game.kit.utils.StringUtil;
import kptech.game.kit.utils.TToast;
import kptech.lib.analytic.Event;
import kptech.lib.analytic.EventCode;
import kptech.lib.analytic.MobclickAgent;

public class RecordPublishView extends LinearLayout {

    private EditText mTitleEdit;
    private TextView mCountText;
    private ImageView mCoverImg;

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

        mCoverImg = findViewById(R.id.cover);
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
                try {
                    //发送打点事件
                    Event event = Event.getEvent(EventCode.DATA_RECORD_PUBLISH_BACKBTN, mPkgName );
                    event.setPadcode(mPadcode);
                    MobclickAgent.sendEvent(event);
                }catch (Exception e){
                }

                dismiss();

            }
        });
        findViewById(R.id.finish_publish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitleEdit.getText().toString().trim();
                String checkTitle = title.replace(" ", "");
                if (StringUtil.isEmpty(checkTitle)){
                    TToast.showCenterToast(getContext(), "请输入一个标题", Toast.LENGTH_SHORT);
                    return;
                }
                //验证标题
                if (StringUtil.isNumeric(checkTitle) || StringUtil.isAlphabat(checkTitle)){
                    TToast.showCenterToast(getContext(), "纯数字及字母标题无法发布", Toast.LENGTH_SHORT);
                    return;
                }


                try {
                    //发送打点事件
                    Event event = Event.getEvent(EventCode.DATA_RECORD_PUBLISH_PUBBTN, mPkgName );
                    event.setPadcode(mPadcode);
                    MobclickAgent.sendEvent(event);
                }catch (Exception e){
                }

                if (mListener != null){
                    mListener.onPublish(title);
                }

                dismiss();
            }
        });
    }
    private String mPadcode;
    private String mPkgName;
    public void show(String pkgName, String padCode, String cover) {
        if (this.getVisibility() == View.VISIBLE){
            return;
        }
        this.mPkgName = pkgName;
        this.mPadcode = padCode;
        this.setVisibility(VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.kp_view_enter_bottom);
        this.startAnimation(animation);

        try {
            //发送打点事件
            Event event = Event.getEvent(EventCode.DATA_RECORD_PUBLISH_DISPLAY, mPkgName );
            event.setPadcode(mPadcode);
            MobclickAgent.sendEvent(event);
        }catch (Exception e){
        }

        if (!StringUtil.isEmpty(cover)){

            Configuration conf = getResources().getConfiguration();
            int ori = 0;
            if (conf.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //横屏
                ori = 2;
            } else if (conf.orientation == Configuration.ORIENTATION_PORTRAIT) {
                //竖屏
                ori = 1;
            }

            LinearLayout.LayoutParams lp = (LayoutParams) mCoverImg.getLayoutParams();
            if (ori == 1){
                //竖屏
                lp.width = DensityUtil.dip2px(getContext(), 200);
                lp.height = DensityUtil.dip2px(getContext(), 350);
            }else if (ori == 2){
                //横屏
                lp.width = DensityUtil.dip2px(getContext(), 266);
                lp.height = DensityUtil.dip2px(getContext(), 150);
            }else {
                lp.width = DensityUtil.dip2px(getContext(), 150);
                lp.height = DensityUtil.dip2px(getContext(), 150);
            }

            mCoverImg.setLayoutParams(lp);

            mCoverImg.setVisibility(VISIBLE);
            try {
                Picasso.with(getContext()).load(cover).into(mCoverImg);
            }catch (Exception e){}
        }else {
            mCoverImg.setVisibility(GONE);
        }

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

        try {
            //发送打点事件
            Event event = Event.getEvent(EventCode.DATA_RECORD_PUBLISH_DESTORY, mPkgName );
            event.setPadcode(mPadcode);
            MobclickAgent.sendEvent(event);
        }catch (Exception e){
        }
    }



}
