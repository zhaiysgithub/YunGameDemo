package kptech.game.kit;

import android.app.Activity;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import kptech.cloud.kit.msg.Messager;
import kptech.game.kit.msg.MsgManager;

public class DeviceControl {
    private static final String TAG = "GameControl";

    private com.yd.yunapp.gameboxlib.DeviceControl mDeviceControl;
    private PlayListener mPlayListener;
    private TimeDownListener mTimeDownListener;

    protected DeviceControl(com.yd.yunapp.gameboxlib.DeviceControl control){
        this(control,null);
    }

    protected DeviceControl(com.yd.yunapp.gameboxlib.DeviceControl control, GameInfo game){
        this.mDeviceControl = control;
    }

    public void startGame(@NonNull Activity activity, @IdRes int res, @NonNull final APICallback<String> callback){

        //连接设备
        MsgManager.start(activity, mDeviceControl.getDeviceToken());

        mDeviceControl.startGame(activity, res, new com.yd.yunapp.gameboxlib.APICallback<String>() {
            @Override
            public void onAPICallback(String s, int i) {
                if (callback!=null){
                    callback.onAPICallback(s,i);
                }
            }
        });
    }

    public void stopGame(){
        try {
            MsgManager.stop();
        }catch (Exception e){
            e.printStackTrace();
        }

        mDeviceControl.stopGame();
    }


    public void setNoOpsTimeout(long font, long back) {
        mDeviceControl.setNoOpsTimeout(font, back);
    }

    public void switchQuality(@APIConstants.VideoQuality String leve){
        mDeviceControl.switchQuality(leve);
    }

    public void setAudioSwitch(boolean audioSwitch){
        mDeviceControl.setAudioSwitch(audioSwitch);
    }

    public boolean isReleased(){
        return mDeviceControl.isReleased();
    }

    public void registerQueueCallback(final APICallback<QueueRankInfo> callback){
        mDeviceControl.registerQueueCallback(new com.yd.yunapp.gameboxlib.APICallback<com.yd.yunapp.gameboxlib.QueueRankInfo>() {
            @Override
            public void onAPICallback(com.yd.yunapp.gameboxlib.QueueRankInfo queueRankInfo, int i) {
                if (callback!=null){
                    callback.onAPICallback(new QueueRankInfo(queueRankInfo), i);
                }
            }
        });
    }

    public void setPlayListener(PlayListener listener){
        this.mPlayListener = listener;

        mDeviceControl.setPlayListener(mDevicePlayListener);
    }

    public void setTimeDownListener(int threshold, TimeDownListener listener){
        this.mTimeDownListener = listener;

        mDeviceControl.setTimeCountDownListener(threshold, mTimeCountDownListener);
    }

    public interface PlayListener {
        void onPingUpdate(int ping);

        boolean onNoOpsTimeout(int type, long timeout);

        void onScreenChange(int value);
    }

    public interface TimeDownListener {
        void countDown(int remainingTime);
    }

    private com.yd.yunapp.gameboxlib.DeviceControl.PlayListener mDevicePlayListener = new com.yd.yunapp.gameboxlib.DeviceControl.PlayListener(){
        @Override
        public void onPingUpdate(int ping) {
            if (mPlayListener!=null){
                mPlayListener.onPingUpdate(ping);
            }
        }

        @Override
        public boolean onNoOpsTimeout(int type, long timeout) {
            if (mPlayListener!=null){
                return mPlayListener.onNoOpsTimeout(type,timeout);
            }
            return false;
        }

        @Override
        public void onScreenChange(int i) {
            if (mPlayListener!=null){
                mPlayListener.onScreenChange(i);
            }
        }
    };

    private com.yd.yunapp.gameboxlib.DeviceControl.TimeCountDownListener mTimeCountDownListener = new com.yd.yunapp.gameboxlib.DeviceControl.TimeCountDownListener() {
        @Override
        public void countDown(int remainingTime) {
            if (mTimeDownListener != null) {
                mTimeDownListener.countDown(remainingTime);
            }
        }
    };

}
