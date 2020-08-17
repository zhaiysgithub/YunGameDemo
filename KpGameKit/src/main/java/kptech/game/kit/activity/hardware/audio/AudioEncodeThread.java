package kptech.game.kit.activity.hardware.audio;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;

import java.nio.ByteBuffer;

import kptech.game.kit.SensorConstants;
import kptech.game.kit.activity.hardware.sampler.SensorDataCallback;
import kptech.game.kit.utils.Logger;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AudioEncodeThread extends Thread {
    Logger logger = new Logger("AudioEncodeThread");
    private static final boolean DEBUG = true;
    // 音频通道(单声道)
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    // 音频格式
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    // 音频源（麦克风）
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
    private static final int BIT_RATE = 96000;

    private String mEncodeType = MediaFormat.MIMETYPE_AUDIO_AAC;
    private static final int SAMPLES_PRE_FRAME = 2048 * 2;
    // 采样率 8K或16K
    public static final int SAMPLE_RATE = 8000;
    public static final int CHANNEL_COUNT = 2;

    public static AudioRecord mAudioRecord;

    private MediaCodec mMediaEncode;
    private MediaCodec.BufferInfo mEncodeBufferInfo;
    private ByteBuffer[] mEncodeInputBuffers;
    private ByteBuffer[] mEncodeOutputBuffers;

    private volatile boolean mIsStart = false;
    private volatile boolean mIsExit = false;
    private final Object lock = new Object();

    private SensorDataCallback<byte[]> mSensorDataCallback;

    public AudioEncodeThread(SensorDataCallback<byte[]> sensorDataCallback) {
        mIsStart = false;
        mIsExit = false;
        mSensorDataCallback = sensorDataCallback;
    }

    /**
     * start record and encode
     */
    public void startMediaCodec() {
        if (mIsStart) {
            return;
        }
        logger.info("startMediaCodec");
        try {
            startMediaEncode();
            startAudioRecord();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (mMediaEncode != null && mAudioRecord != null) {
            mIsStart = true;
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    /**
     * stop audio recorder and encode
     */
    public void exitRecorder() {
        mIsExit = true;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    /**
     * release audio recorder and encode
     */
    public void releaseMediaCodec() {
        logger.info("releaseMediaCodec");
        mIsStart = false;
        if (mAudioRecord != null) {
            mAudioRecord.setRecordPositionUpdateListener(null);
            if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                mAudioRecord.stop();
            }
            mAudioRecord.release();
            mAudioRecord = null;
        }

        if (mMediaEncode != null) {
            mMediaEncode.stop();
            mMediaEncode.release();
            mMediaEncode = null;
        }

        mEncodeInputBuffers = null;
        mEncodeOutputBuffers = null;
    }

    @Override
    public void run() {
        super.run();
        int bufferReadResult = 0;
        if (DEBUG) {
            logger.info( "AudioEncodeThread start encode  " + mIsStart + "   " + mIsExit);
        }
        try {
            while (!mIsExit) {
                if (!mIsStart) {
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                } else {
                    if (mAudioRecord != null) {
                        byte[] buffer = new byte[SAMPLES_PRE_FRAME];
                        // 从缓冲区中读取数据，存入到buffer字节数组数组中
                        bufferReadResult = mAudioRecord.read(buffer, 0, buffer.length);
                        // 判断是否读取成功
                        if (bufferReadResult == AudioRecord.ERROR_BAD_VALUE
                                || bufferReadResult == AudioRecord.ERROR_INVALID_OPERATION) {
                            if (DEBUG) {
                                logger.error( "Read error");
                            }
                        }
                        if (mAudioRecord != null && bufferReadResult > 0) {
                            dstAudioFormatFromPCM(buffer);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // just continue
            logger.error("=======" + e.getMessage());
        }
        if (DEBUG) {
            logger.info("AudioEncodeThread end encode");
        }
    }

    private void startAudioRecord() {
        // 获取最小缓冲区大小
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

        mAudioRecord = new AudioRecord(
                // 音频源
                AUDIO_SOURCE,
                // 采样率
                SAMPLE_RATE,
                // 音频通道
                CHANNEL_CONFIG,
                // 音频格式\采样精度
                AUDIO_FORMAT,
                // 缓冲区
                bufferSizeInBytes * 4
        );
        mAudioRecord.startRecording();
    }

    private void startMediaEncode() throws Exception {
        MediaFormat encodeFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, SAMPLE_RATE, CHANNEL_COUNT);
        // 比特率
        encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        encodeFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, CHANNEL_COUNT);
        encodeFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, CHANNEL_CONFIG);
        encodeFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        // 作用于inputBuffer的大小
        encodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, SAMPLES_PRE_FRAME);

        mMediaEncode = MediaCodec.createEncoderByType(mEncodeType);
        mMediaEncode.configure(encodeFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaEncode.start();

        mEncodeInputBuffers = mMediaEncode.getInputBuffers();
        mEncodeOutputBuffers = mMediaEncode.getOutputBuffers();
        mEncodeBufferInfo = new MediaCodec.BufferInfo();
    }


    /**
     * 编码PCM数据 得到AAC格式的音频文件
     */
    private void dstAudioFormatFromPCM(byte[] pcmData) {
        int inputIndex;
        ByteBuffer inputBuffer;
        int outputIndex;
        ByteBuffer outputBuffer;

        int outBitSize;
        byte[] PCMAudio;
        PCMAudio = pcmData;

        mEncodeInputBuffers = mMediaEncode.getInputBuffers();
        mEncodeOutputBuffers = mMediaEncode.getOutputBuffers();
        mEncodeBufferInfo = new MediaCodec.BufferInfo();

        inputIndex = mMediaEncode.dequeueInputBuffer(200);
        inputBuffer = mEncodeInputBuffers[inputIndex];
        inputBuffer.clear();
        inputBuffer.limit(PCMAudio.length);
        // PCM数据填充给inputBuffer
        inputBuffer.put(PCMAudio);
        // 通知编码器 编码
        mMediaEncode.queueInputBuffer(inputIndex, 0, PCMAudio.length, 0, 0);


        outputIndex = mMediaEncode.dequeueOutputBuffer(mEncodeBufferInfo, 0);
        while (outputIndex > 0) {
            outBitSize = mEncodeBufferInfo.size;
            // 拿到输出Buffer
            outputBuffer = mEncodeOutputBuffers[outputIndex];
            outputBuffer.position(mEncodeBufferInfo.offset);
            outputBuffer.limit(mEncodeBufferInfo.offset + outBitSize);

            handleAudioData(outputBuffer, mEncodeBufferInfo.offset, outBitSize);

            outputBuffer.position(mEncodeBufferInfo.offset);
            mMediaEncode.releaseOutputBuffer(outputIndex, false);
            outputIndex = mMediaEncode.dequeueOutputBuffer(mEncodeBufferInfo, 0);
        }
    }

    private void handleAudioData(ByteBuffer dataBuffer, int offset, int length) {
        if (mSensorDataCallback != null) {
            byte[] data = new byte[length + 7];
            AudioEncodeUtils.addADTStoPacket(data, data.length);
            dataBuffer.get(data, 7, length);
            if (length == 2) {
                mSensorDataCallback.sampling(SensorConstants.AUDIO_TYPE_ACC_SPECIAL_DATA, data);
            } else {
                mSensorDataCallback.sampling(SensorConstants.AUDIO_TYPE_ACC_FRAME, data);
            }
        }
    }


}