package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.xutils.x;

import kptech.game.kit.GameBoxManager;
import kptech.game.kit.GameInfo;
import kptech.game.kit.R;


public class UserAuthView extends PlayAuthPageView implements View.OnClickListener {

    private ImageView mGameIcon;
    private TextView mGameName;

    public UserAuthView(Context context) {
        super(context);
        initView();
    }

    public UserAuthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    @Override
    protected View getAuthView() {
        return null;
    }

    @Override
    protected int getAuthViewId() {
        return R.layout.kp_view_user_auth;
    }

    @Override
    protected void initView() {
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
            GameBoxManager.getInstance().setOnAuthClick(false);
        }else if (view.getId() == R.id.auth_btn) {
            GameBoxManager.getInstance().setOnAuthClick(true);
        }
    }

    @Override
    public void setGameInfo(GameInfo gameInfo) {
        if (gameInfo == null){
            return;
        }
        mGameName.setText(gameInfo.name);
        try {
            int localResId = gameInfo.localResId;
            if (localResId > 0){
                mGameIcon.setImageResource(localResId);
            }else {
                String iconUrl = gameInfo.iconUrl;
                if (iconUrl != null && !iconUrl.isEmpty()){
                    x.image().bind(mGameIcon,iconUrl);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
