package kptech.game.kit.ad.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.zad.sdk.Oapi.ZadSdkApi;
import com.zad.sdk.Oapi.callback.ZadRewardAdObserver;
import com.zad.sdk.Oapi.work.ZadRewardWorker;

import java.util.HashMap;

import kptech.game.kit.GameInfo;
import kptech.game.kit.R;
import kptech.game.kit.ad.IAdCallback;
import kptech.game.kit.analytic.Event;
import kptech.game.kit.analytic.EventCode;
import kptech.game.kit.analytic.MobclickAgent;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;


public class AdRemindDialog extends AlertDialog implements View.OnClickListener {
    private static final Logger logger = new Logger("AdRemindDialog") ;


    public AdRemindDialog(Activity context) {
        super(context, R.style.RemindDialog);
//        this.mActivity = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ad_remind_dialog);
        initView();
        this.setCanceledOnTouchOutside(false);
//
//
//        try {
//            //发送打点事件
//            Event event = Event.getEvent(EventCode.DATA_AD_DIALOG_DISPLAY);
//            if (this.mGameInfo!=null){
//                event.setGamePkg(this.mGameInfo.pkgName);
//            }
//            HashMap ext = new HashMap<>();
//            ext.put("rewardAdCode", this.mRewardAdCode);
//            ext.put("extAdCode", this.mExtAdCode);
//            event.setExt(ext);
//            MobclickAgent.sendEvent(event);
//        }catch (Exception e){}
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mCancelListener!=null){
            mCancelListener.onClick(null);
        }
    }

    public void initView() {
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.submit).setOnClickListener(this);
    }

    private View.OnClickListener mSubmitListener;
    public AdRemindDialog setOnSubmitListener(View.OnClickListener listener){
        this.mSubmitListener = listener;
        return this;
    }

    private View.OnClickListener mCancelListener;
    public AdRemindDialog setOnCancelListener(View.OnClickListener listener){
        this.mCancelListener = listener;
        return this;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.cancel) {
            dismiss();
            if (mCancelListener!=null){
                mCancelListener.onClick(view);
            }
        } else if (i == R.id.submit) {
            dismiss();
            if (mSubmitListener!=null){
                mSubmitListener.onClick(view);
            }
        }
    }

}
