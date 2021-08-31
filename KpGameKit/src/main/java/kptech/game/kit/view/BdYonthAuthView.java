package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.xutils.x;

import kptech.game.kit.R;
import kptech.game.kit.manager.FastRepeatClickManager;

public class BdYonthAuthView extends FrameLayout {

    private RoundImageView mAuthGameIcon;
    private TextView mAuthGameName;
    private OnAuthCallback mCallback;

    public BdYonthAuthView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public BdYonthAuthView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.kp_bdyouth_game_auth, this);

        ImageView mAuthBack = view.findViewById(R.id.ivAuthBack);
        mAuthGameIcon = view.findViewById(R.id.ivAuthGameIcon);
        mAuthGameName = view.findViewById(R.id.tvAuthGameName);
        TextView mAuthLogin = view.findViewById(R.id.tvAuthLogin);

        mAuthBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastRepeatClickManager.getInstance().isFastDoubleClick(v.getId())){
                    return;
                }
                if (mCallback != null){
                    mCallback.onBackListener();
                }
            }
        });

        mAuthLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastRepeatClickManager.getInstance().isFastDoubleClick(v.getId())){
                    return;
                }
                if (mCallback != null){
                    mCallback.onAuthListener();
                }
            }
        });

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    /**
     * 设置游戏信息
     */
    public void setGameInfo(String gameIcon,String gameName){
        if (mAuthGameIcon != null){
            if (gameIcon != null && !gameIcon.isEmpty()){
                x.image().bind(mAuthGameIcon, gameIcon);
            }else {
                x.image().bind(mAuthGameIcon, "res://" + R.mipmap.kp_game_icon_default);
            }
        }
        if (mAuthGameName != null && gameName != null && !gameName.isEmpty()){
            mAuthGameName.setText(gameName);
        }
    }

    public void setOnAuthCallback(OnAuthCallback callback) {
        this.mCallback = callback;
    }


    public interface OnAuthCallback {

        void onBackListener();

        void onAuthListener();
    }

}
