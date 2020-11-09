package kptech.game.kit.activity.hardware.sampler;

import android.Manifest;
import android.app.Activity;
import android.widget.FrameLayout;


import kptech.game.kit.R;
import kptech.game.kit.SensorConstants;
import kptech.game.kit.activity.hardware.SaveLocalUtils;
import kptech.game.kit.activity.hardware.camera.CameraPreview;
import kptech.game.kit.utils.Logger;

public class CameraSampler extends Sampler implements SensorDataCallback<byte[]> {
    public static final String TAG = "CameraSampler";
    public static final int WIDTH_VIDEO = 720;
    public static final int HEIGHT_VIDEO = 480;

    private CameraPreview mSurfaceView;

    private boolean mIsVideoEncodeStarted = false;


    public CameraSampler(Activity context, SamplingCallback callback) {
        super(context, callback);
    }

    @Override
    public void onStart() {
        Logger.info(TAG,"onStart========");
    }

    @Override public void onResume() {
        if (mIsVideoEncodeStarted && mSurfaceView != null) {
            return;
        }

        Logger.info(TAG,"onResume========");
        mSurfaceView = new CameraPreview(mContext);
        mSurfaceView.setPreviewSize(WIDTH_VIDEO, HEIGHT_VIDEO);
        mSurfaceView.setSensorDataCallback(this);

        FrameLayout contentView = (FrameLayout) mContext.findViewById(R.id.content_view);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(300, 300);
        contentView.addView(mSurfaceView, layoutParams);

//        mSurfaceView.setVisibility(View.INVISIBLE);
//        mSurfaceView.setZOrderOnTop(true);
//        mSurfaceView.setZOrderMediaOverlay(true);
        mIsVideoEncodeStarted = true;
    }

    @Override public void onPause() {
        Logger.info(TAG,"onPause========");
        mIsVideoEncodeStarted = false;
        if (mSurfaceView != null) {
            FrameLayout contentView = (FrameLayout) mContext.findViewById(R.id.content_view);
            contentView.removeView(mSurfaceView);
            mSurfaceView = null;
        }
        SaveLocalUtils.stopSaveData(SaveLocalUtils.TYPE_VIDEO);
    }

    @Override public void onStop() {

    }

    @Override public String[] getRequestPermission() {
        return new String[]{Manifest.permission.CAMERA};
    }

    @Override public void waitGrantPermission() {

    }

    @Override public void sampling(int type, byte[] data) {
        if (callback != null) {
            callback.onSamplerData(SensorConstants.HARDWARE_ID_VIDEO_BACK, type, data);
        }
        SaveLocalUtils.saveEncodeData(SaveLocalUtils.TYPE_VIDEO, data);
    }
}
