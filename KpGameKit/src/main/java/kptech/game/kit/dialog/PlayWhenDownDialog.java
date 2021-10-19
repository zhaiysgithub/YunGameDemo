package kptech.game.kit.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.HashMap;

import kptech.game.kit.GameInfo;
import kptech.game.kit.R;
import kptech.game.kit.manager.FastRepeatClickManager;
import kptech.game.kit.manager.KpGameDownloadManger;
import kptech.game.kit.utils.DensityUtil;
import kptech.game.kit.view.CircularProgressView;
import kptech.lib.analytic.Event;
import kptech.lib.analytic.EventCode;
import kptech.lib.analytic.MobclickAgent;

/**
 * 边玩变下的弹窗
 */
public class PlayWhenDownDialog extends Dialog {

//    private static final String TAG = "PlayWhenDownDialog";

    public static final int TYPE_CLICK_LIMIT = 11;
    public static final int TYPE_CLICK_PROGRESS = 12;
    public static final int TYPE_CLICK_FULL = 13;

    private GameInfo mGameInfo;
    private String mPkgName;
    private String mDownloadUrl;
    private ImageView mIvDel;
    private RelativeLayout mLayoutProgress;
    private CircularProgressView mProgressView;
    private TextView mTvDownload;
//    private TextView mTvStop;
    private TextView mTvDesc;
//    private TextView mTvSlash;
    private TextView mTvLimit, mTvLimitDesc, mTvFull, mTvFullDesc;
    private ImageView mIvDownStatusIcon;
    private LinearLayout mLayoutDownLimit;
    private LinearLayout mLayoutDownFull;
    private IPlayWhenDownListener mListener;
    private KpGameDownloadManger mDownloadManager;
    private int px26, px28, px20;
    private int color_999, color_333, color_666, color_ddd, color_cfff;
    private Drawable mLimitEnableSelected, mDisableDrawable, mFullEnableSelected;

    //是否是wifi状态
    private boolean isWifi;

    public PlayWhenDownDialog(@NonNull Context context) {
        this(context, R.style.MyTheme_CustomDialog_Background);
    }

    public PlayWhenDownDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);

    }

    /**
     * 设置游戏信息和网络状态
     *
     * @param gameInfo 游戏参数
     * @param isWifi   是否是wifi状态
     */
    public void setGameConfig(GameInfo gameInfo, boolean isWifi) {
        this.mGameInfo = gameInfo;
        this.mPkgName = gameInfo.pkgName;
        this.mDownloadUrl = gameInfo.downloadUrl;
        this.isWifi = isWifi;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.kp_view_dialog_playdown);

        Window window = getWindow();
        if (window != null) {
            window.setGravity(Gravity.CENTER);
        }
        setCanceledOnTouchOutside(true);
        mDownloadManager = KpGameDownloadManger.instance();
        initView();

        initData();

        initEvent();

    }

    /**
     * 更新状态
     *
     * @param progress 当前进度
     */
    @SuppressLint("SetTextI18n")
    public void updateProgress(int progress) {
        if (!isShowing()) {
            return;
        }
        if (progress >= 100){
            notifyProgressUI(KpGameDownloadManger.STATE_FINISHED);
            notifyLayoutDown(KpGameDownloadManger.STATE_FINISHED);
            return;
        }
        mProgressView.setProgress(progress);
        String proValue = progress + "%";
        if (isWifi) {
            mTvDownload.setText(proValue);
        }
        if (mDownloadManager != null) {
            boolean speedLimitEnable = mDownloadManager.isSpeedLimitEnable();
            String downingDesc = getDowningDesc(progress,speedLimitEnable);
            mTvDesc.setText(downingDesc);
        }
    }

    /**
     * 显示的文字描述
     */
    private String getDowningDesc(int progress,boolean isSpeedLimit){

        if (isWifi){
            if (isSpeedLimit){
                return "正在低速下载，解锁完整游戏内容";
            }else {
                return "正在高速下载，解锁完整游戏内容";
            }
        }else {
            if (isSpeedLimit){
                return "正在低速下载，已下载" + progress + "%...";
            }else {
                return "正在高速下载，已下载" + progress + "%...";
            }
        }
    }

    private void initView() {
        mIvDel = findViewById(R.id.ivDel);
        mLayoutProgress = findViewById(R.id.layoutProgress);
        mProgressView = findViewById(R.id.circularProgress);
        mTvDownload = findViewById(R.id.tvDownload);
//        mTvStop = findViewById(R.id.tvStop);
        mTvDesc = findViewById(R.id.tvDesc);
//        mTvSlash = findViewById(R.id.tvSlash);
        mTvLimit = findViewById(R.id.tvTextLimit);
        mIvDownStatusIcon = findViewById(R.id.ivDownStatusIcon);
        mTvLimitDesc = findViewById(R.id.tvTextLimitDesc);
        mTvFull = findViewById(R.id.tvTextFull);
        mTvFullDesc = findViewById(R.id.tvTextFullDesc);
        mLayoutDownLimit = findViewById(R.id.layoutDownloadLimit);
        mLayoutDownFull = findViewById(R.id.layoutDownloadFull);
    }

    private void initData() {
        if (mGameInfo == null || mDownloadManager == null) {
            return;
        }
        color_999 = getContext().getResources().getColor(R.color.kp_color_999);
        color_333 = getContext().getResources().getColor(R.color.kp_color_333);
        color_666 = getContext().getResources().getColor(R.color.kp_color_666);
        color_ddd = getContext().getResources().getColor(R.color.kp_color_ddd);
        //白色80%透明度
        color_cfff = getContext().getResources().getColor(R.color.kp_color_cfff);
        px26 = DensityUtil.px2sp(getContext(), 26);
        px28 = DensityUtil.px2sp(getContext(), 28);
        px20 = DensityUtil.px2sp(getContext(), 20);
        mLimitEnableSelected = ContextCompat.getDrawable(getContext(), R.drawable.kp_sel_down_limit_tv);
        mFullEnableSelected = ContextCompat.getDrawable(getContext(), R.drawable.kp_sel_down_full_tv);
        mDisableDrawable = ContextCompat.getDrawable(getContext(), R.drawable.kp_download_disable);
        if (isWifi) {
            mTvDownload.setTextColor(Color.WHITE);
        }
//        mTvSlash.setTextColor(color_999);
        int state = mDownloadManager.getDownloadState(mGameInfo.downloadUrl);
        updateDownStatus(state);
    }

    /**
     * 更新下载的状态
     */
    @SuppressLint("SetTextI18n")
    public void updateDownStatus(int state) {
        //当前下载的状态
        notifyProgressUI(state);
        //更新按钮状态
        notifyLayoutDown(state);
    }

    /**
     * 更新进度布局的UI
     */
    @SuppressLint("SetTextI18n")
    private void notifyProgressUI(int state) {
        int progress = mDownloadManager.getDownloadProgress(mDownloadUrl);
        switch (state) {
            case KpGameDownloadManger.STATE_WAITING: //未下载
                mIvDownStatusIcon.setImageResource(R.mipmap.kp_ic_dialog_download);
                mProgressView.setProgress(progress);
//                mTvStop.setVisibility(View.GONE);
//                mTvSlash.setVisibility(View.GONE);
                mTvDownload.setTextSize(TypedValue.COMPLEX_UNIT_SP, px28);
                mTvDownload.setText(progress + "%");
//                mTvDownload.setTextColor(Color.WHITE);
                break;
            case KpGameDownloadManger.STATE_STARTED: //下载中
                mIvDownStatusIcon.setImageResource(R.mipmap.kp_ic_dialog_download);
                mProgressView.setProgress(progress);
//                mTvStop.setVisibility(isWifi ? View.GONE : View.VISIBLE);
//                mTvSlash.setVisibility(isWifi ? View.GONE : View.VISIBLE);
//                mTvDownload.setTextColor(Color.WHITE);
                int tvSizeDownStarted = isWifi ? px28 : px26;
                mTvDownload.setTextSize(TypedValue.COMPLEX_UNIT_SP, tvSizeDownStarted);
                if (!isWifi) {
                    mTvDownload.setText("暂停下载");
//                    mTvSlash.setTextSize(TypedValue.COMPLEX_UNIT_SP, px20);
//                    mTvStop.setTextSize(TypedValue.COMPLEX_UNIT_SP, px20);
//                    mTvStop.setTextColor(color_999);
                }else {
                    mTvDownload.setText(progress + "%");
                }
                break;
            case KpGameDownloadManger.STATE_STOPPED: //暂停
                mIvDownStatusIcon.setImageResource(R.mipmap.kp_ic_dialog_stop);
                mProgressView.setProgress(progress);
//                mTvStop.setVisibility(isWifi ? View.GONE : View.VISIBLE);
//                mTvSlash.setVisibility(isWifi ? View.GONE : View.VISIBLE);
//                mTvDownload.setTextColor(isWifi ? Color.WHITE : color_999);
                int tvSizeDownStoped = isWifi ? px28 : px26;
                mTvDownload.setTextSize(TypedValue.COMPLEX_UNIT_SP, tvSizeDownStoped);
                if (!isWifi) {
                    mTvDownload.setText("继续下载");
//                    mTvSlash.setTextSize(TypedValue.COMPLEX_UNIT_SP, px24);
//                    mTvStop.setTextSize(TypedValue.COMPLEX_UNIT_SP, px24);
//                    mTvStop.setTextColor(Color.WHITE);
                }else {
                    mTvDownload.setText(progress + "%");
                }
                break;
            case KpGameDownloadManger.STATE_FINISHED: //下载完成
                mIvDownStatusIcon.setImageResource(R.mipmap.kp_ic_dialog_finish);
                mProgressView.setProgress(100);
//                mTvStop.setVisibility(View.GONE);
//                mTvSlash.setVisibility(View.GONE);
                mTvLimitDesc.setVisibility(View.GONE);
                mTvFullDesc.setVisibility(View.GONE);
                mTvDownload.setTextSize(TypedValue.COMPLEX_UNIT_SP, px28);
                mTvDownload.setText("100%");
//                mTvDownload.setTextColor(Color.WHITE);
                break;
            case KpGameDownloadManger.STATE_ERROR://错误
                mIvDownStatusIcon.setImageResource(R.mipmap.kp_ic_dialog_error);
                mProgressView.setProgress(progress);
//                mTvStop.setVisibility(isWifi ? View.GONE : View.VISIBLE);
//                mTvSlash.setVisibility(isWifi ? View.GONE : View.VISIBLE);
//                mTvDownload.setTextColor(isWifi ? Color.WHITE : color_999);
                int tvSizeDownError = isWifi ? px28 : px26;
                mTvDownload.setTextSize(TypedValue.COMPLEX_UNIT_SP, tvSizeDownError);
                if (!isWifi) {
                    mTvDownload.setText("点击重试");
//                    mTvSlash.setTextSize(TypedValue.COMPLEX_UNIT_SP, px20);
//                    mTvStop.setTextSize(TypedValue.COMPLEX_UNIT_SP, px20);
//                    mTvStop.setTextColor(color_999);
                }else {
                    mTvDownload.setText(progress + "%");
                }
                break;
            default:
                break;
        }
    }

    private void notifyLayoutDown(int state){
        //是否是限速下载
        boolean speedLimitEnable = mDownloadManager.isSpeedLimitEnable();
        switch (state){
            case KpGameDownloadManger.STATE_WAITING:
                //设置按钮
                mLayoutDownLimit.setBackground(mLimitEnableSelected);
                mTvLimit.setTextColor(color_333);
                mTvLimitDesc.setTextColor(color_666);
                mLayoutDownFull.setBackground(mFullEnableSelected);
                mTvFull.setTextColor(Color.WHITE);
                mTvFullDesc.setTextColor(color_cfff);
                mTvDesc.setText("边玩边下，解锁完整游戏内容");
                break;
            case KpGameDownloadManger.STATE_STARTED:
                //设置按钮
                if (speedLimitEnable) {
                    mLayoutDownLimit.setBackground(mDisableDrawable);
                    mLayoutDownFull.setBackground(mFullEnableSelected);
                    mTvLimit.setTextColor(color_ddd);
                    mTvLimitDesc.setTextColor(color_ddd);
                    mTvFull.setTextColor(Color.WHITE);
                    mTvFullDesc.setTextColor(color_cfff);
                } else {
                    mLayoutDownLimit.setBackground(mLimitEnableSelected);
                    mLayoutDownFull.setBackground(mDisableDrawable);
                    mTvLimit.setTextColor(color_333);
                    mTvLimitDesc.setTextColor(color_666);
                    mTvFull.setTextColor(color_ddd);
                    mTvFullDesc.setTextColor(color_ddd);
                }
                int progress = mDownloadManager.getDownloadProgress(mDownloadUrl);
                String showDesc = getDowningDesc(progress, speedLimitEnable);
                mTvDesc.setText(showDesc);
                break;
            case KpGameDownloadManger.STATE_STOPPED:
                //设置按钮
                if (speedLimitEnable) {
                    mLayoutDownLimit.setBackground(mLimitEnableSelected);
                    mTvLimit.setTextColor(color_333);
                    mTvLimitDesc.setTextColor(color_666);
                } else {
                    mLayoutDownFull.setBackground(mFullEnableSelected);
                    mTvFull.setTextColor(Color.WHITE);
                    mTvFullDesc.setTextColor(color_cfff);
                }
                mTvDesc.setText("已暂停下载，点击继续下载…");
                break;
            case KpGameDownloadManger.STATE_ERROR:
                //设置按钮
                mLayoutDownLimit.setBackground(mLimitEnableSelected);
                mLayoutDownFull.setBackground(mFullEnableSelected);
                mTvLimit.setTextColor(color_333);
                mTvLimitDesc.setTextColor(color_666);
                mTvFull.setTextColor(Color.WHITE);
                mTvFullDesc.setTextColor(color_cfff);
                mTvDesc.setText("下载出错，点击重试…");
                break;
            case KpGameDownloadManger.STATE_FINISHED:
                mLayoutDownLimit.setBackground(mLimitEnableSelected);
                mTvLimit.setTextColor(color_333);
                mTvLimit.setText("下次再说");
                mLayoutDownFull.setBackground(mFullEnableSelected);
                mTvFull.setTextColor(Color.WHITE);
                mTvFull.setText("立即安装");
                mTvDesc.setText("下载完成，安装解锁更多游戏内容！");
                break;
        }
    }

    /**
     * 处理未下载的情况
     */
    private void handunDownloadState() {
        try {
            int state = mDownloadManager.getDownloadState(mDownloadUrl);
            if (state == KpGameDownloadManger.STATE_WAITING) {
                //开始下载
                mDownloadManager.initDownload(getContext());
            } else if (state == KpGameDownloadManger.STATE_ERROR || state == KpGameDownloadManger.STATE_STOPPED) {
                //继续下载
                mDownloadManager.continueDownload(getContext(),mGameInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initEvent() {
        mIvDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastRepeatClickManager.getInstance().isFastDoubleClick(v.getId())) {
                    return;
                }
                dismiss();
            }
        });

        //开启下载，暂停下载
        mLayoutProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isWifi) {
                        return;
                    }
                    if (FastRepeatClickManager.getInstance().isFastDoubleClick(v.getId())) {
                        return;
                    }
                    String msg = "";
                    int state = mDownloadManager.getDownloadState(mDownloadUrl);
                    switch (state) {
                        case KpGameDownloadManger.STATE_STARTED: //下载中
                            msg = "已暂停";
                            mDownloadManager.stopDownload(mDownloadUrl);
                            notifyProgressUI(state);
                            break;
                        case KpGameDownloadManger.STATE_STOPPED: //暂停
                        case KpGameDownloadManger.STATE_ERROR: //错误
                            msg = "继续下载";
                            mDownloadManager.continueDownload(getContext(),mGameInfo);
                            notifyProgressUI(state);
                            break;
                        default:
                            break;
                    }
                    //打点
                    Event event = Event.getEvent(EventCode.DATA_ACTIVITY_RECEIVE_DOWNLOADRESUMESTOP,mPkgName);
                    HashMap<String,String> ext = new HashMap<>();
                    ext.put("preStatus",state + "");
                    ext.put("msg",msg);
                    event.setExt(ext);
                    MobclickAgent.sendEvent(event);
                    if (mListener != null) {
                        mListener.onDownloadClick(TYPE_CLICK_PROGRESS);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //限速下载 , 下次再说
        mLayoutDownLimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (FastRepeatClickManager.getInstance().isFastDoubleClick(v.getId())) {
                        return;
                    }
                    int state = mDownloadManager.getDownloadState(mGameInfo.downloadUrl);
                    if (state == KpGameDownloadManger.STATE_FINISHED){
                        //关闭弹窗
                        dismiss();
                        Event event = Event.getEvent(EventCode.DATA_ACTIVITY_RECEIVE_DOWNLOADUNINSTALL,mPkgName);
                        MobclickAgent.sendEvent(event);
                        return;
                    }
                    if (state == KpGameDownloadManger.STATE_STARTED){
                        boolean speedLimitEnable = mDownloadManager.isSpeedLimitEnable();
                        if (speedLimitEnable) {
                            return;
                        }
                    }
                    //开启限速下载
                    mDownloadManager.setSpeedLimitEnable(true);
                    handunDownloadState();
                    notifyLayoutDown(state);

                    Event event = Event.getEvent(EventCode.DATA_ACTIVITY_RECEIVE_DOWNLOADLIMIT,mPkgName);
                    MobclickAgent.sendEvent(event);

                    if (mListener != null) {
                        mListener.onDownloadClick(TYPE_CLICK_LIMIT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        //全速下载 , 立即安装
        mLayoutDownFull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (FastRepeatClickManager.getInstance().isFastDoubleClick(v.getId())) {
                        return;
                    }
                    int state = mDownloadManager.getDownloadState(mGameInfo.downloadUrl);
                    if (state == KpGameDownloadManger.STATE_FINISHED){
                        //执行安装
                        mDownloadManager.doInstallApk(getContext(),mGameInfo.pkgName);
                        Event event = Event.getEvent(EventCode.DATA_ACTIVITY_RECEIVE_DOWNLOADINSTALL,mPkgName);
                        MobclickAgent.sendEvent(event);
                        return;
                    }

                    if (state == KpGameDownloadManger.STATE_STARTED){
                        boolean speedLimitEnable = mDownloadManager.isSpeedLimitEnable();
                        if (!speedLimitEnable) {
                            return;
                        }
                    }
                    //开启高速下载
                    mDownloadManager.setSpeedLimitEnable(false);
                    handunDownloadState();
                    notifyLayoutDown(state);

                    Event event = Event.getEvent(EventCode.DATA_ACTIVITY_RECEIVE_DOWNLOADFULL,mPkgName);
                    MobclickAgent.sendEvent(event);

                    if (mListener != null) {
                        mListener.onDownloadClick(TYPE_CLICK_FULL);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void setIPlayWhenDownListener(IPlayWhenDownListener listener) {
        this.mListener = listener;
    }

    public interface IPlayWhenDownListener {

        /**
         * 继续下载，暂停
         *
         * @param type 点击的类型
         */
        void onDownloadClick(int type);

    }
}
