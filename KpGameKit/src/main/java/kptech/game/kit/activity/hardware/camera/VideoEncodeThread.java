package kptech.game.kit.activity.hardware.camera;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Vector;

import kptech.game.kit.SensorConstants;
import kptech.game.kit.activity.hardware.SaveLocalUtils;
import kptech.game.kit.activity.hardware.sampler.CameraSampler;
import kptech.game.kit.activity.hardware.sampler.SensorDataCallback;
import kptech.game.kit.utils.Logger;

/**
 * 视频编码线程
 */
public class VideoEncodeThread extends Thread {

    private static final String TAG = VideoEncodeThread.class.getSimpleName();

    // 编码相关参数
    private static final String MIME_TYPE = "video/avc"; // H.264 Advanced Video
    private static final int FRAME_RATE = 60; // 帧率
    private static final int I_FRAME_INTERVAL = 60; // I帧间隔（GOP）
    private static final int TIMEOUT_ENCODE = 10000; // 编码超时时间

    // 视频宽高参数
    private int mWidth;
    private int mHeight;

    // 存储每一帧的数据 Vector 自增数组
    private Vector<byte[]> frameBytes;
    private byte[] mFrameData;

    private byte[] mSPSData = null;
    private byte[] mPPSData = null;

    private static final int COMPRESS_RATIO = 256;
    private static final int BIT_RATE = CameraSampler.WIDTH_VIDEO * CameraSampler.HEIGHT_VIDEO
            * 3 * 8 * FRAME_RATE / COMPRESS_RATIO; // bit rate CameraWrapper.

    private final Object lock = new Object();

    private MediaCodecInfo mCodecInfo;
    private MediaCodec mMediaCodec;  // Android硬编解码器
    private MediaCodec.BufferInfo mBufferInfo; //  编解码Buffer相关信息

    private MediaFormat mediaFormat; // 音视频格式

    private volatile boolean isStart = false;
    private volatile boolean isExit = false;
    private SensorDataCallback<byte[]> mSensorDataCallback;

    public VideoEncodeThread(int mWidth, int mHeight, SensorDataCallback<byte[]> callback) {
        // 初始化相关对象和参数
        mSensorDataCallback = callback;
        this.mWidth = mWidth;
        this.mHeight = mHeight;
        frameBytes = new Vector<byte[]>();
        prepare();
    }

    // 执行相关准备工作
    private void prepare() {
        Logger.info(TAG,"VideoEncoderThread().prepare");
        mFrameData = new byte[this.mWidth * this.mHeight * 3 / 2];
        mBufferInfo = new MediaCodec.BufferInfo();
        mCodecInfo = selectCodec(MIME_TYPE);
        if (mCodecInfo == null) {
            Logger.error(TAG,"Unable to find an appropriate codec for " + MIME_TYPE);
            return;
        }

        int colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
        MediaCodecInfo.CodecCapabilities capabilities = mCodecInfo.getCapabilitiesForType(MIME_TYPE);
        if (capabilities != null && capabilities.colorFormats != null) {
            for (int i = 0; i < capabilities.colorFormats.length; i++) {
                int format = capabilities.colorFormats[i];
                switch (format) {
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                        colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
                        break;
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                        colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
                        break;
                    default:
                        break;

                }
            }
        }
        mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, this.mWidth, this.mHeight);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);
    }

    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (String type : types) {
                if (type.equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }


    /**
     * 开始视频编码
     *
     * @throws IOException
     */
    public void startMediaCodec() {
        if (!isStart) {
            try {
                mMediaCodec = MediaCodec.createByCodecName(mCodecInfo.getName());
                mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                mMediaCodec.start();
                isStart = true;
                synchronized (lock) {
                    Logger.error(TAG,Thread.currentThread().getId() + " video -- setConfigureReady...");
                    lock.notifyAll();
                }
            } catch (Exception e) {
                e.printStackTrace();
                isStart = false;
            }
        }
    }

    public void addFrame(byte[] data) {
        if (frameBytes != null) {
            frameBytes.add(data);
        }
    }


    @Override
    public void run() {
        super.run();

        while (!isExit) {
            if (!isStart) {
//                stopMediaCodec();

                synchronized (lock) {
                    try {
                        Logger.error(TAG,"video -- 等待混合器准备...");
                        lock.wait();
                    } catch (InterruptedException e) {
                    }
                }

            } else if (!frameBytes.isEmpty()) {
                byte[] bytes = this.frameBytes.remove(0);
                // Logger.e("ang-->", "解码视频数据:" + bytes.length);
                try {
                    encodeFrame(bytes);
                } catch (Exception e) {
                    Logger.error(TAG,"解码视频(Video)数据 失败 "+ e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        Logger.error(TAG,"Video 录制线程 退出...");
    }

    public void release() {
        isExit = true;
        frameBytes = null;
        synchronized (lock) {
            Logger.error(TAG,Thread.currentThread().getId() + " video -- setExitReady...");
            lock.notifyAll();
        }
    }

    /**
     * 编码每一帧的数据
     *
     * @param input 每一帧的数据
     */
    private void encodeFrame(byte[] input) {
        // Logger.w(TAG, "VideoEncoderThread.encodeFrame()");

        // 将原始的N21数据转为I420
        nv21toI420SemiPlanar(input, mFrameData, this.mWidth, this.mHeight);

        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();

        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_ENCODE);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(mFrameData);
            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, mFrameData.length, System.nanoTime() / 1000, 0);
        } else {
            Logger.error(TAG,"input buffer not available");
        }

        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_ENCODE);
        Logger.info(TAG,"outputBufferIndex-->" + outputBufferIndex);
        do {
            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // just continue
                Logger.info(TAG,"outputBufferIndex = INFO_TRY_AGAIN_LATER");
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = mMediaCodec.getOutputBuffers();
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = mMediaCodec.getOutputFormat();
            } else if (outputBufferIndex < 0) {
                Logger.error(TAG,"outputBufferIndex < 0");
            } else {
                // Logger.d(TAG, "perform encoding");
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                if (outputBuffer == null) {
                    throw new RuntimeException("encoderOutputBuffer " + outputBufferIndex + " was null");
                }
//                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//                    LogHelper.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
//                    LogHelper.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
//                    mBufferInfo.size = 0;
//                }
                if (mBufferInfo.size != 0) {
                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    outputBuffer.position(mBufferInfo.offset);
                    outputBuffer.limit(mBufferInfo.offset + mBufferInfo.size);
                    handleVideoData(outputBuffer, mBufferInfo.offset, mBufferInfo.size);
                    Logger.info(TAG,"encode size : " + mBufferInfo.size + " , pts : " + mBufferInfo.presentationTimeUs);
                }

                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            }
            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_ENCODE);
        } while (outputBufferIndex >= 0);
    }

    /**
     * 停止视频编码
     */
    public void stopMediaCodec() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }

        if (frameBytes != null) {
            frameBytes.clear();
//            frameBytes = null;
        }
        isStart = false;
        mFrameData = null;
        mCodecInfo = null;
        mMediaCodec = null;
        mBufferInfo = null;
        mediaFormat = null;
        Logger.error(TAG,"stop video 录制...");
    }


    private static void nv21toI420SemiPlanar(byte[] nv21bytes, byte[] i420bytes, int width, int height) {
        System.arraycopy(nv21bytes, 0, i420bytes, 0, width * height);
        for (int i = width * height; i < nv21bytes.length; i += 2) {
            i420bytes[i] = nv21bytes[i + 1];
            i420bytes[i + 1] = nv21bytes[i];
        }
    }

    public void encodeReset(int width, int height) {
        if (mMediaCodec == null ||
                (mWidth == width && mHeight == height)) {
            return;
        }
        mWidth = width;
        mHeight = height;
        stopMediaCodec();
        prepare();
    }

    private void handleVideoData(ByteBuffer byteBuffer, int offset, int length) {
        byte[] data = new byte[length];
        byteBuffer.get(data);
        if (mSensorDataCallback != null) {
            if ((data[4] & 0x1F) == 0x07) {
                int ppsStartIndex = CameraEncodeUtils.getPpsStartIndex(data);
                int spsLen = ppsStartIndex + 1;
                int ppsLen = length - spsLen;
                if (spsLen > 0 && ppsLen > 0) {
                    // get sps data
                    mSPSData = new byte[spsLen];
                    System.arraycopy(data, offset, mSPSData, 0, spsLen);
                    // get pps data
                    mPPSData = new byte[ppsLen];
                    System.arraycopy(data, offset + ppsStartIndex + 1, mPPSData, 0, ppsLen);

                    mSensorDataCallback.sampling(SensorConstants.CAMERA_VIDEO_TYPE_SPS, mSPSData);
                    mSensorDataCallback.sampling(SensorConstants.CAMERA_VIDEO_TYPE_PPS, mPPSData);

                    SaveLocalUtils.saveData2File(SaveLocalUtils.SAVE_VIDEO_SPS_PATH, mSPSData);
                    SaveLocalUtils.saveData2File(SaveLocalUtils.SAVE_VIDEO_PPS_PATH, mPPSData);
                } else {
                    // just continue
                    Logger.info(TAG," handleVideoData null");
                }

            } else if ((data[4] & 0x1F) == 0x05) {
                // h264 I frame
                mSensorDataCallback.sampling(SensorConstants.CAMERA_VIDEO_TYPE_IFRAME, data);
            } else {
                // h264 p frame
                mSensorDataCallback.sampling(SensorConstants.CAMERA_VIDEO_TYPE_PFRAME, data);
            }
        }
    }
}