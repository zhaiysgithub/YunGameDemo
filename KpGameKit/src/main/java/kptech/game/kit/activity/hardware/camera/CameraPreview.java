package kptech.game.kit.activity.hardware.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

import kptech.game.kit.activity.hardware.SaveLocalUtils;
import kptech.game.kit.activity.hardware.sampler.SensorDataCallback;
import kptech.game.kit.utils.Logger;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";

    private final SurfaceHolder mHolder;
    private Camera mCamera;
    public static int WIDTH_VIDEO = 0;
    public static int HEIGHT_VIDEO = 0;

    private VideoEncodeThread mVideoEncodeThread = null;
    private SensorDataCallback<byte[]> mCallback;
    private boolean mIsVideoEncodeStarted = false;
    private int mVideoRotateDegrees = 0;
    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mVideoEncodeThread != null) {
                byte[] dstData = new byte[data.length];
                int width = WIDTH_VIDEO;
                int height = HEIGHT_VIDEO;
                switch (mVideoRotateDegrees) {
                    case 90:
                        CameraEncodeUtils.yuv420spRotate90(dstData, data,
                                WIDTH_VIDEO, HEIGHT_VIDEO);
                        width = HEIGHT_VIDEO;
                        height = WIDTH_VIDEO;
                        break;
                    case 180:
                        CameraEncodeUtils.yuv420spRotate180(dstData, data,
                                WIDTH_VIDEO, HEIGHT_VIDEO);
                        break;
                    case 270:
                        CameraEncodeUtils.yuv420spRotate270(dstData, data,
                                WIDTH_VIDEO, HEIGHT_VIDEO);
                        width = HEIGHT_VIDEO;
                        height = WIDTH_VIDEO;
                        break;
                    default:
                        dstData = data;
                        break;
                }
                mVideoEncodeThread.encodeReset(width, height);
                mVideoEncodeThread.startMediaCodec();
                mVideoEncodeThread.addFrame(dstData);
            }
        }
    };

    public CameraPreview(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void setPreviewSize(int width, int height) {
        WIDTH_VIDEO = width;
        HEIGHT_VIDEO = height;
    }

    public void setSensorDataCallback(SensorDataCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // surface第一次创建时回调
        // 打开相机
        // mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        mCamera = Camera.open();
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        List<Camera.Size> list = parameters.getSupportedPictureSizes();
        parameters.setPreviewSize(WIDTH_VIDEO, HEIGHT_VIDEO);

//        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//        List<int[]> fps = parameters.getSupportedPreviewFpsRange();
//        parameters.setPreviewFpsRange(30000,30000);
        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(mPreviewCallback);
            mCamera.startPreview();
            mCamera.setDisplayOrientation(90);
            mCamera.autoFocus(new Camera.AutoFocusCallback() {

                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                   Logger.info(TAG,"onAutoFocus");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(TAG,"exception: "+ e.getMessage());
            return;
        }

        mVideoEncodeThread = new VideoEncodeThread(WIDTH_VIDEO, HEIGHT_VIDEO, new SensorDataCallback<byte[]>() {
            @Override public void sampling(int type, byte[] data) {
                if (mCallback != null) {
                    mCallback.sampling(type, data);
                }
            }
        });
        if (mVideoEncodeThread != null) {
            // start video encode thread
            mVideoEncodeThread.start();
        }
        SaveLocalUtils.openSaveDataFile(SaveLocalUtils.TYPE_VIDEO);
        Logger.info(TAG,"surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // surface变化的时候回调(格式/大小)
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // surface销毁的时候回调
        mHolder.removeCallback(this);
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;

        if (mVideoEncodeThread != null) {
            mVideoEncodeThread.release();
            try {
                mVideoEncodeThread.join(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                mVideoEncodeThread.stopMediaCodec();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mVideoEncodeThread = null;
        }
        Logger.info(TAG,"surfaceDestroyed");
    }

    public void release() {

    }
}