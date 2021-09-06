package kptech.game.kit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;

import kptech.game.kit.GameInfo;
import kptech.game.kit.R;
import kptech.game.kit.manager.FastRepeatClickManager;

public class BdYonthErrorView extends FrameLayout {

    //加载失败
    public static final int STATUS_LOAD_ERROR = -10;
    //设备不足
    public static final int STATUS_NO_DEVICE = -11;
    //设备断开
    public static final int STATUS_DEVICE_OFFLINE = -12;
    //页面异常
    public static final int STATUS_PAGE_ERROR = -13;

    private OnErrorCallback mCallback;
    private LottieAnimationView mIvErrorPic;
    private TextView mTvShowErrorMsg;

    private int mCurrentStatus;

    public BdYonthErrorView(Context context) {
        super(context);
        initView(context);
    }

    public BdYonthErrorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View view = inflate(context, R.layout.kp_bdyouth_play_error, this);

        mIvErrorPic = view.findViewById(R.id.ivErrorPic);
        mTvShowErrorMsg = view.findViewById(R.id.tvErrorMsg);
        TextView mTvReload = view.findViewById(R.id.tvErrorReload);

        mTvReload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastRepeatClickManager.getInstance().isFastDoubleClick(v.getId())) {
                    return;
                }
                if (mCallback != null) {
                    mCallback.onReloadGame();
                }
            }
        });

        mIvErrorPic.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mCallback != null) {
                    mCallback.onCopyDeviceInf();
                }
                return true;
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    /**
     * 更新页面
     */
    public void updateErrorStatus(int status, String msg) {
        if (mCurrentStatus == status) {
            return;
        }
        mCurrentStatus = status;
        switch (status) {
            case STATUS_LOAD_ERROR:
            case STATUS_DEVICE_OFFLINE:
                mIvErrorPic.setAnimation("lot_game_load_error.json");
                mTvShowErrorMsg.setText("啊哦，加载失败了");
                break;
            case STATUS_NO_DEVICE:
                mIvErrorPic.setAnimation("lot_game_no_device.json");
                mTvShowErrorMsg.setText("啊哦，游戏太火了，云设备排队中");
                break;
            case STATUS_PAGE_ERROR:
                mIvErrorPic.setAnimation("lot_game_error.json");
                mTvShowErrorMsg.setText("啊哦，页面异常");
                break;
            default:
                mIvErrorPic.setAnimation("lot_game_load_error.json");
                mTvShowErrorMsg.setText(msg);
                break;
        }
    }

    public void setGameInfo(GameInfo info) {

    }

    public void setOnErrorCallback(OnErrorCallback callback) {
        this.mCallback = callback;
    }


    public interface OnErrorCallback {

        void onBackClick();

        void onReloadGame();

        void onDownloadGame();

        void onCopyDeviceInf();
    }

}
