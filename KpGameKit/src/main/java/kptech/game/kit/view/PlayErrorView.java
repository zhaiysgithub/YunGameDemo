package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.xutils.x;
import kptech.game.kit.GameInfo;
import kptech.game.kit.R;
import kptech.game.kit.download.DownloadTask;
import kptech.game.kit.utils.StringUtil;

public class PlayErrorView extends LinearLayout implements View.OnClickListener {

    private TextView mErrorText;
    private ImageView mGameIcon;
    private TextView mErrorDownText;
    private ProgressBar mErrorDownPb;
    private ViewGroup mErrorDownBtn;

    private GameInfo mGameInfo;

    private ClickListener mListener;

    public interface ClickListener {
        void onBack();
        void onRetry();
        void onDown(View view);
        void onCopyInf();
    }

    public void setClickListener(ClickListener listener){
        this.mListener = listener;
    }

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

    public void setGameInfo(GameInfo info){
        mGameInfo = info;
    }

    public void setErrorText(String err) {
        mErrorText.setText(err);
        try{
            if (this.mGameInfo!=null){
                int localResId = mGameInfo.localResId;
                if (localResId > 0){
                    mGameIcon.setImageResource(localResId);
                }else {
                    String iconUrl = mGameInfo.iconUrl;
                    if (iconUrl != null && !iconUrl.isEmpty()){
                        x.image().bind(mGameIcon,iconUrl);
                    }
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
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void setProgress(int num, String text) {
        mErrorDownText.setText(text);
        mErrorDownPb.setProgress(num);
    }

    public void setDownloadStatus(int status){
        switch (status){
            case DownloadTask.STATUS_STARTED:
                mErrorDownText.setText("下载中...");
                break;
            case DownloadTask.STATUS_ERROR:
                mErrorDownText.setText("下载出错");
                break;
            case DownloadTask.STATUS_FINISHED:
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

        mGameIcon.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    if (mListener != null){
                        mListener.onCopyInf();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_back){
            if (this.mListener != null){
                this.mListener.onBack();
            }
        }else if (view.getId() == R.id.error_down_layout) {
            if (view.getTag()!=null && view.getTag() instanceof String){
                String tag = (String) view.getTag();
                if ("reload".equals(tag)){
                    if (this.mListener != null){
                        this.mListener.onRetry();
                    }
                }else if("down".equals(tag)){
                    if (this.mListener != null){
                        this.mListener.onDown(view);
                    }
                }
            }
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mGameInfo = null;
        mListener = null;
    }
}
