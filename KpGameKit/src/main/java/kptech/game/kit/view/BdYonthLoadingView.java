package kptech.game.kit.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import kptech.game.kit.GameInfo;
import kptech.game.kit.R;
import kptech.game.kit.manager.FastRepeatClickManager;

public class BdYonthLoadingView extends LoadingPageView {

    private TextView mLoadingValue;
    private OnLoadingCallback mCallback;

    public BdYonthLoadingView(Context context) {
        super(context);
    }

    public BdYonthLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void inflateView() {
        View view = inflate(getContext(), R.layout.kp_bdyouth_loading, this);

        mLoadingValue = view.findViewById(R.id.tvLoadingProgress);
        ImageView mIvLoadingBack = view.findViewById(R.id.ivLoadingBack);

        mIvLoadingBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastRepeatClickManager.getInstance().isFastDoubleClick()){
                    return;
                }
                if (mCallback != null){
                    mCallback.onBackClick();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void updateChildProgress(int progress) {
        int value = progress * 100 / pbMax;
        mLoadingValue.setText(value + "%");
    }

    @Override
    protected void updateChildText() {

    }

    @Override
    protected void setLoadingInfo(GameInfo gameInfo) {

    }

    @Override
    protected void onConfigChanged(Configuration newConfig) {

    }

    public void updateLoadingText(String text) {

    }

    public void setOnLoadingCallback(OnLoadingCallback callback){
        this.mCallback = callback;
    }

   public interface OnLoadingCallback{

        void onBackClick();
   }

}
