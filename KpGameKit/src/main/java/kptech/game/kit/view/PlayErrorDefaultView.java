package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.xutils.x;

import kptech.game.kit.GameBoxManager;
import kptech.game.kit.GameInfo;
import kptech.game.kit.R;
import kptech.game.kit.download.DownloadTask;
import kptech.game.kit.utils.StringUtil;

public class PlayErrorDefaultView extends PlayErrorPageView implements View.OnClickListener {

    private TextView mErrorText;
    private ImageView mGameIcon;
    private TextView mErrorDownText;
    private ProgressBar mErrorDownPb;
    private ViewGroup mErrorDownBtn;

    private GameInfo mGameInfo;

    public PlayErrorDefaultView(@NonNull Context context) {
        super(context);
    }

    public PlayErrorDefaultView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getErrorView() {
        return null;
    }

    @Override
    protected int getErrorLayoutRes() {
        return R.layout.kp_view_play_error;
    }

    @Override
    public void setGameInfo(GameInfo info){
        mGameInfo = info;
    }

    @Override
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

    @Override
    public void setProgress(int num, String text) {
        mErrorDownText.setText(text);
        mErrorDownPb.setProgress(num);
    }

    @Override
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
    protected void initView(View view) {
        mGameIcon = view.findViewById(R.id.game_icon);
        mErrorText = view.findViewById(R.id.error_text);

        mErrorDownText = view.findViewById(R.id.error_down_text);
        mErrorDownPb = view.findViewById(R.id.error_down_pb);

        mErrorDownBtn = view.findViewById(R.id.error_down_layout);
        mErrorDownBtn.setOnClickListener(this);

        view.findViewById(R.id.btn_back).setOnClickListener(this);

        mGameIcon.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                GameBoxManager.getInstance().setOnCopyInfoClick("");
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
            GameBoxManager.getInstance().setOnBackClick(true);
        }else if (view.getId() == R.id.error_down_layout) {
            if (view.getTag()!=null && view.getTag() instanceof String){
                String tag = (String) view.getTag();
                if ("reload".equals(tag)){
                    GameBoxManager.getInstance().setOnReloadClick();
                }else if("down".equals(tag)){
                    GameBoxManager.getInstance().setOnDownloadClick();
                }
            }
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mGameInfo = null;
    }
}
