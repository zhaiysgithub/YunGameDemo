package com.kptach.lib.game.bdsdk;

import java.lang.ref.WeakReference;

import com.kptach.lib.game.bdsdk.play.IPlayListener;
import com.kptach.lib.game.bdsdk.play.IVideoListener;
import com.kptach.lib.game.bdsdk.utils.ThreadUtils;
import com.kptach.lib.inter.game.APIConstants;

public class SDKListener implements IPlayListener, IVideoListener {
    WeakReference<BDSdkDeviceControl> ref = null;
    public SDKListener(BDSdkDeviceControl control){
        ref = new WeakReference<>(control);
    }

    @Override
    public void onRelease() {
        if(ref!=null && ref.get()!=null){
            ref.get().callback("Release Success", APIConstants.RELEASE_SUCCESS);
        }
    }

    @Override
    public void onConnectSuccess(String str, int i) {
        if(ref!=null && ref.get()!=null){
            ref.get().callback(str, APIConstants.CONNECT_DEVICE_SUCCESS);
        }
    }

    @Override
    public void onConnectError(String str, int i) {
        if(ref!=null && ref.get()!=null){
            ref.get().callback(str, APIConstants.ERROR_OTHER);
        }
    }

    @Override
    public void onReceiverBuffer() {

    }

    @Override
    public void onScreenCapture(final byte[] bArr) {
        ThreadUtils.runUi(new Runnable() {
            @Override
            public void run() {
                if (ref != null && ref.get() != null && ref.get().mPlayListener!=null){
                    ref.get().mPlayListener.onScreenCapture(bArr);
                }
            }
        });
    }

    @Override
    public void onSensorInput(int i, int i2) {
        if(ref!=null && ref.get()!=null && ref.get().mSensorListener!=null){
            ref.get().mSensorListener.onSensorSamper(i,i2);
        }
    }

    @Override
    public void onTransparentMsg(int i, String str, String str2) {

    }

    @Override
    public void onMsgSendFailed(int i, int i2, String str) {

    }

    @Override
    public void onScreenChange(final int i) {
        ThreadUtils.runUi(new Runnable() {
            @Override
            public void run() {
                if (ref != null && ref.get() != null && ref.get().mPlayListener!=null){
                    ref.get().mPlayListener.onScreenChange(i);
                }
            }
        });
    }

    @Override
    public void onDelayTime(final int i) {
        ThreadUtils.runUi(new Runnable() {
            @Override
            public void run() {
                if (ref != null && ref.get() != null){
                    ref.get().mPlayListener.onPingUpdate(i);
                }
            }
        });
    }

    @Override
    public void onNoOpsTimeout(final int type, final long timeout) {
        ThreadUtils.runUi(new Runnable() {
            @Override
            public void run() {
                if (ref != null && ref.get() != null){
                    ref.get().mPlayListener.onNoOpsTimeout(type,timeout);
                }
            }
        });
    }

    @Override
    public void onResolutionChange(int i, int i2) {

    }

    @Override
    public void onEncodeChange(int i) {

    }

    @Override
    public void onFPSChange(int i) {

    }

    @Override
    public void onBitrateChange(int i) {

    }

    @Override
    public void onQualityChange(int i) {

    }

    @Override
    public void onMaxIdrChange(int i) {

    }

}