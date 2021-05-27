package kptech.game.kit.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import kptech.game.kit.utils.Logger;

public class KPGameReceiver extends BroadcastReceiver {

    private static final String TAG = "KPGameReceiver";
    public static final String ACTION = "kptech.game.kit.receiver.KPGameReceiver.action";
    public static final String ACTION_STARTACTIVITY = "KP_Cloud_Game_Play_StartActivity";
    public static final String RANDOM_KEY = "random_key";
    private String randomValue;
    private OnKpGameReceiverCallback mCallback;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null){
            String action = intent.getAction();
            Logger.error(TAG, "onReceive:action = " + action);
            if (ACTION.equals(action)){
                String ranValue = intent.getStringExtra(RANDOM_KEY);
                if (randomValue.isEmpty() || randomValue.equals(ranValue)){
                    return;
                }
                //关闭游戏
                if (mCallback != null){
                    mCallback.onExitGame();
                }
            }else if(ACTION_STARTACTIVITY.equals(action)){
                String activityClassName = intent.getStringExtra("className");
                Bundle bundleData = intent.getBundleExtra("bundleData");
                String optValue = intent.getStringExtra("option");
                Logger.error(TAG,"activityClassName=" + activityClassName + ";optValue=" + optValue);
                if (activityClassName != null && !activityClassName.isEmpty() && optValue != null && optValue.equals("exit")){
                    Intent actIntent = new Intent();
                    actIntent.setClassName(context,activityClassName);
                    actIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (bundleData != null){
                        actIntent.putExtras(bundleData);
                    }
                    context.startActivity(actIntent);
                    //关闭游戏
                    if (mCallback != null){
                        mCallback.onExitGame();
                    }
                }
            }
        }
    }

    public void setRandomValue(String randomValue) {
        this.randomValue = randomValue;
    }

    public void setCallback(OnKpGameReceiverCallback mCallback) {
        this.mCallback = mCallback;
    }

    public interface OnKpGameReceiverCallback{

        void onExitGame();
    }
}
