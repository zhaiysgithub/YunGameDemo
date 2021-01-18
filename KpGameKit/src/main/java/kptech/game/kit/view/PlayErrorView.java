package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import kptech.game.kit.GameInfo;
import kptech.game.kit.R;
import kptech.game.kit.activity.GamePlay;
import kptech.game.kit.analytic.Event;
import kptech.game.kit.utils.StringUtil;

public class PlayErrorView extends LinearLayout implements View.OnClickListener {

    private TextView mErrorText;
    private ImageView mGameIcon;
    private TextView mErrorDownText;
    private ProgressBar mErrorDownPb;
    private ViewGroup mErrorDownBtn;

    private GameInfo mGameInfo;

    private OnClickListener mBackListener;
    private OnClickListener mDownListener;
    private OnClickListener mRetryListener;

    public PlayErrorView(Context context) {
        super(context);
        initView();
    }

    public PlayErrorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.kp_view_play_error, this);
    }

    public void setOnBackListener(OnClickListener listener){
        this.mBackListener = listener;
    }

    public void setOnDownListener(OnClickListener listener){
        this.mDownListener= listener;
    }

    public void setOnRetryListener(OnClickListener listener){
        this.mRetryListener = listener;
    }

    public void setGameInfo(GameInfo info){
        mGameInfo = info;
    }

    public void setErrorText(String err) {
        mErrorText.setText(err);

        if (this.mGameInfo!=null){
            if (this.mGameInfo.iconUrl!=null && !"".equals(this.mGameInfo.iconUrl)){
                try {
                    Picasso.with(getContext()).load(this.mGameInfo.iconUrl).into(mGameIcon);
                }catch (Exception e){}
            }
            if (!StringUtil.isEmpty(mGameInfo.downloadUrl) && mGameInfo.enableDownload == 1){
                //显示下载按钮
                mErrorDownBtn.setTag("down");
                mErrorDownText.setText("下载游戏直接玩");
            } else {
                //显示重试按钮
                mErrorDownBtn.setTag("reload");
                mErrorDownText.setText("重新加载游戏");
            }
        }else {
            //隐藏按钮
            mErrorDownBtn.setTag("reload");
            mErrorDownBtn.setVisibility(View.GONE);
        }
    }

    public void setProgress(int num, String text) {
        mErrorDownText.setText(text);
        mErrorDownPb.setProgress(num);
    }

    public void setDownloadStatus(int status){
        switch (status){
            case GamePlay.STATUS_STARTED:
                mErrorDownText.setText("下载中...");
                break;
            case GamePlay.STATUS_ERROR:
                mErrorDownText.setText("下载出错");
                break;
            case GamePlay.STATUS_FINISHED:
                mErrorDownText.setText("下载完成");
                break;
            default:
                mErrorDownText.setText("下载游戏直接玩");
                break;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mGameIcon = findViewById(R.id.game_icon);
        mErrorText = findViewById(R.id.error_text);

        mErrorDownText = findViewById(R.id.error_down_text);
        mErrorDownPb = findViewById(R.id.error_down_pb);

        mErrorDownBtn = findViewById(R.id.error_down_layout);
        mErrorDownBtn.setOnClickListener(this);

        findViewById(R.id.btn_back).setOnClickListener(this);
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
        }else if (view.getId() == R.id.error_down_layout) {
            if (view.getTag()!=null && view.getTag() instanceof String){
                Event event = null;
                String tag = (String) view.getTag();
                if ("reload".equals(tag)){
                    if (this.mRetryListener != null){
                        this.mRetryListener.onClick(view);
                    }
                }else if("down".equals(tag)){
                    if (this.mDownListener != null){
                        this.mDownListener.onClick(view);
                    }
                }
            }
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mGameInfo = null;
        mBackListener = null;
        mDownListener = null;
        mRetryListener = null;
    }
}
