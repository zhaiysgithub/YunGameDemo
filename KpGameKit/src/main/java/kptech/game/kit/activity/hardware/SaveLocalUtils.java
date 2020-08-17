package kptech.game.kit.activity.hardware;

import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SaveLocalUtils {
    private static final boolean DEBUG = false;
    private static final String SAVE_DATA_PATH = "/Redfinger/Encode";
    private static final String SAVE_VIDEO_PATH = "/EncodeVideo.h264";
    private static final String SAVE_AUDIO_PATH = "/EncodeAudio.aac";
    public static final String SAVE_VIDEO_SPS_PATH = "/EncodeVideoSps.h264";
    public static final String SAVE_VIDEO_PPS_PATH = "/EncodeVideoPps.h264";
    private static final boolean SAVE_A_V_DATA = true;

    private static RandomAccessFile mVideoSaveFile = null;
    private static RandomAccessFile mAudioSaveFile = null;

    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_AUDIO = 2;

    public static void openSaveDataFile(int type) {
        if (!SAVE_A_V_DATA && !DEBUG) {
            return;
        }
        StringBuilder fullPath = new StringBuilder();
        fullPath.append(Environment.getExternalStorageDirectory().getPath());
        fullPath.append(SAVE_DATA_PATH);

        String dirs = fullPath.toString();
        File dir = new File(dirs);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileSuffix = null;
        if (type == TYPE_VIDEO) {
            fileSuffix = SAVE_VIDEO_PATH;
        } else if (type == TYPE_AUDIO) {
            fileSuffix = SAVE_AUDIO_PATH;
        }

        if (fileSuffix != null) {
            StringBuilder filePath = new StringBuilder(dirs);
            filePath.append(fileSuffix);
            File file = new File(filePath.toString());
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                if (type == TYPE_VIDEO) {
                    mVideoSaveFile = randomAccessFile;
                } else if (type == TYPE_AUDIO) {
                    mAudioSaveFile = randomAccessFile;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void stopSaveData(int type) {
        if (!DEBUG) {
            return;
        }
        if (type == TYPE_VIDEO) {
            if (mVideoSaveFile != null) {
                try {
                    mVideoSaveFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mVideoSaveFile = null;
            }
        } else if (type == TYPE_AUDIO) {
            if (mAudioSaveFile != null) {
                try {
                    mAudioSaveFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mAudioSaveFile = null;
            }

        }
    }

    public static void saveEncodeData(int type, byte[] outData) {
        if (!DEBUG) {
            return;
        }
        RandomAccessFile file = null;
        if (!SAVE_A_V_DATA || outData == null || outData.length <= 0) {
            return;
        }

        switch (type) {
            case TYPE_VIDEO:
                file = mVideoSaveFile;
                break;
            case TYPE_AUDIO:
                file = mAudioSaveFile;
                break;
            default:
                file = null;
                break;
        }

        if (file != null) {
            try {
                // Logger.d("marcoTest", "type : " + type + " , offset : " + " , outData[4] : " + outData[4]);
                long fileLength = file.length();
                file.seek(fileLength);
                file.write(outData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveData2File(String fileName, byte[] data) {
        if (!DEBUG) {
            return;
        }
        if (!SAVE_A_V_DATA || TextUtils.isEmpty(fileName) ||
                data == null || data.length <= 0) {
            return;
        }

        StringBuilder fullPath = new StringBuilder();
        fullPath.append(Environment.getExternalStorageDirectory().getPath());
        fullPath.append(SAVE_DATA_PATH);

        String dirs = fullPath.toString();
        File dir = new File(dirs);
        if (!dir.exists()) {
            dir.mkdirs();
        }

//        fullPath.append("/");
        fullPath.append(fileName);

        File file = new File(fullPath.toString());
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file, false));
            out.write(data, 0, data.length);
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
