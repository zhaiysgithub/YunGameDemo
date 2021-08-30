package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import org.xutils.x;

import kptech.game.kit.R;


public class UserAuthView extends LinearLayout implements View.OnClickListener {
    private ImageView mGameIcon;
    private TextView mGameName;


    private OnClickListener mBackListener;
    private OnClickListener mAuthListener;

    public UserAuthView(Context context) {
        super(context);
        initView();
    }

    public UserAuthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.kp_view_user_auth, this);
    }

    public void setOnBackListener(OnClickListener listener){
        this.mBackListener = listener;
    }

    public void setOnAuthListener(OnClickListener listener){
        this.mAuthListener= listener;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mGameIcon = findViewById(R.id.game_icon);
        mGameName = findViewById(R.id.game_name);

        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.auth_btn).setOnClickListener(this);

    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_back){
            if (this.mBackListener != null){
                this.mBackListener.onClick(view);
            }
        }else if (view.getId() == R.id.auth_btn) {


            if (this.mAuthListener != null){
                this.mAuthListener.onClick(view);
            }
        }
    }

    public void setInfo(String name, String iconUrl) {
        mGameName.setText(name);
        if (iconUrl!=null && !"".equals(iconUrl)){
            try {
                x.image().bind(mGameIcon,iconUrl);
            }catch (Exception e){}
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mBackListener = null;
        mAuthListener = null;

    }
}
