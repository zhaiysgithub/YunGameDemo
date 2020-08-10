package com.yd.yunapp.gamebox.hardware.camera;


import android.hardware.Camera;

public class CameraEncodeUtils {
    public static final int TYPE_ALL = 0;
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_AUDIO = 2;

    private static byte[] mSPSData = null;
    private static byte[] mPPSData = null;

    /**
     * get pps start position
     *
     * @param data, sps and pps data
     * @return pps start position
     */
    public static int getPpsStartIndex(byte[] data) {
        int index = 0;
        int len = 0;
        if (data == null) {
            return index;
        }

        len = data.length - 5;
        for (int i = 0; i < len; i++) {
            if (data[i] == 0x00 &&
                    data[i + 1] == 0x00 &&
                    data[i + 2] == 0x00 &&
                    data[i + 3] == 0x01 &&
                    (data[i + 4] & 0x1F) == 0x08) {
                index = i - 1;
            }
        }

        if (index <= 5 && index >= len) {
            index = 0;
        }

        return index;
    }


    public static int mappingAacSampleRate(int samplingRate) {
        int ret = 0xB; // 8k
        switch (samplingRate) {
            case 96000:
                ret = 0x0;
                break;
            case 88200:
                ret = 0x1;
                break;
            case 64000:
                ret = 0x2;
                break;
            case 48000:
                ret = 0x3;
                break;
            case 44100:
                ret = 0x4;
                break;
            case 32000:
                ret = 0x5;
                break;
            case 24000:
                ret = 0x6;
                break;
            case 22050:
                ret = 0x7;
                break;
            case 16000:
                ret = 0x8;
                break;
            case 12000:
                ret = 0x9;
                break;
            case 11025:
                ret = 0xA;
                break;
            case 8000:
                ret = 0xB;
                break;
            case 7350:
                ret = 0xC;
                break;
            default:
                break;
        }
        return ret;
    }


    public interface HandlerCollectAVDataCallBack {
        void handlerCollectAVData(int avType, int frameType, byte[] data);
    }

    public static boolean openCloseCameraFlash(Camera camera, boolean flashState) {
        boolean ret = false;
        if (camera != null) {
            Camera.Parameters parameters;
            String value = null;
            parameters = camera.getParameters();
            if (flashState) {
                value = Camera.Parameters.FLASH_MODE_TORCH;
            } else {
                value = Camera.Parameters.FLASH_MODE_OFF;
            }
            parameters.setFlashMode(value);
            camera.setParameters(parameters);
            ret = true;
        }
        return ret;
    }

    public static void yuv420spRotate90(byte[] des, byte[] src,
                                        int width, int height) {
        int wh = width * height;
        int k = 0;
        for (int i = 0; i < width; i++) {
            for (int j = height - 1; j >= 0; j--) {
                des[k] = src[width * j + i];
                k++;
            }
        }
        for (int i = 0; i < width; i += 2) {
            for (int j = height / 2 - 1; j >= 0; j--) {
                des[k] = src[wh + width * j + i];
                des[k + 1] = src[wh + width * j + i + 1];
                k += 2;
            }
        }
    }

    public static void yuv420spRotate180(byte[] des, byte[] src, int width, int height) {
        int n = 0;
        int uh = height >> 1;
        int wh = width * height;
        // copy y
        for (int j = height - 1; j >= 0; j--) {
            for (int i = width - 1; i >= 0; i--) {
                des[n++] = src[width * j + i];
            }
        }


        for (int j = uh - 1; j >= 0; j--) {
            for (int i = width - 1; i > 0; i -= 2) {
                des[n] = src[wh + width * j + i - 1];
                des[n + 1] = src[wh + width * j + i];
                n += 2;
            }
        }
    }

    public static void yuv420spRotate270(byte[] des, byte[] src, int width, int height) {
        int n = 0;
        int uvHeight = height >> 1;
        int wh = width * height;
        // copy y
        for (int j = width - 1; j >= 0; j--) {
            for (int i = 0; i < height; i++) {
                des[n++] = src[width * i + j];
            }
        }

        for (int j = width - 1; j > 0; j -= 2) {
            for (int i = 0; i < uvHeight; i++) {
                des[n++] = src[wh + width * i + j - 1];
                des[n++] = src[wh + width * i + j];
            }
        }
    }
}
