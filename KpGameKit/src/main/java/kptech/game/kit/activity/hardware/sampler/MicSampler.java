package kptech.game.kit.activity.hardware.sampler;

import android.Manifest;
import android.app.Activity;

import kptech.game.kit.SensorConstants;
import kptech.game.kit.activity.hardware.SaveLocalUtils;
import kptech.game.kit.activity.hardware.audio.AudioEncodeThread;


/**
 * Created by zhouzhiyong on 19-5-13.
 * Project: GameBox
 */
public class MicSampler extends Sampler implements SensorDataCallback<byte[]> {

    private AudioEncodeThread mAudioEncodeThread;
//    private boolean mNeedFakeData = false;
    // 为了防止等待授权过程中云手机上出现授权失败的情况，定时发送空数据
//    private byte[] fakeData = new byte[1543];
//    private Runnable fakeDataRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if (mNeedFakeData) {
//                sampling(1, fakeData);
//                mHandler.postDelayed(fakeDataRunnable, 120);
//            }
//        }
//    };

    public MicSampler(Activity context, SamplingCallback samplingCallback) {
        super(context, samplingCallback);
    }

    @Override
    public void onStart() {
        SaveLocalUtils.openSaveDataFile(SaveLocalUtils.TYPE_AUDIO);
        mAudioEncodeThread = new AudioEncodeThread(this);
        mAudioEncodeThread.start();
    }

    @Override
    public void onResume() {
        mAudioEncodeThread.startMediaCodec();
    }

    @Override
    public void onPause() {
        mAudioEncodeThread.releaseMediaCodec();
    }

    @Override
    public void onStop() {
        if (mAudioEncodeThread != null) {
            mAudioEncodeThread.exitRecorder();
        }
        SaveLocalUtils.stopSaveData(SaveLocalUtils.TYPE_AUDIO);
    }

    @Override
    public String[] getRequestPermission() {
        return new String[]{Manifest.permission.RECORD_AUDIO};
    }

    @Override
    public void waitGrantPermission() {
//        mNeedFakeData = true;
//        AudioEncodeUtils.addADTStoPacket(fakeData, fakeData.length);
//        mHandler.post(fakeDataRunnable);
    }

    @Override
    public void onPermissionsGrantEnd(boolean grant) {
        // mNeedFakeData = false;
    }

    @Override
    public void sampling(final int type, final byte[] data) {
        if (callback != null) {
            callback.onSamplerData(SensorConstants.HARDWARE_ID_MIC, type, data);
        }
        SaveLocalUtils.saveEncodeData(SaveLocalUtils.TYPE_AUDIO, data);
    }
}
