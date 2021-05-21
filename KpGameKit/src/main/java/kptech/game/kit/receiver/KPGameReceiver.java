package kptech.game.kit.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class KPGameReceiver extends BroadcastReceiver {

    public static final String ACTION = "kptech.game.kit.receiver.KPGameReceiver.action";
    public static final String RANDOM_KEY = "random_key";
    private String randomValue;
    private OnKpGameReceiverCallback mCallback;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null){
            String action = intent.getAction();
            if (ACTION.equals(action)){
                String ranValue = intent.getStringExtra(RANDOM_KEY);
                if (randomValue.isEmpty() || randomValue.equals(ranValue)){
                    return;
                }
                //关闭游戏
                if (mCallback != null){
                    mCallback.onExitGame();
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
