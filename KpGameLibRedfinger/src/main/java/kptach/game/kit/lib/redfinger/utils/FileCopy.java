package kptach.game.kit.lib.redfinger.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileCopy {
    public static final String TAG = "FileCopy";
    private static final int BUFFER_SIZE = 4096;
    private static final long b = Long.MAX_VALUE;

    public boolean copy(InputStream is, String dest, long maxLength, boolean force) {
        boolean ret = false;
        OutputStream out = null;
        File destFile = new File(dest);
        try {
            prepare();
            if (force || !destFile.exists()) {
                out = new FileOutputStream(destFile);
                byte[] buf = new byte[BUFFER_SIZE];
                long totalSize = 0;
                int readSize = maxLength > BUFFER_SIZE ? BUFFER_SIZE : (int) maxLength;
                while (true) {
                    int len = is.read(buf, 0, readSize);
                    if (len <= 0) {
                        break;
                    }
                    out.write(buf, 0, len);

                    onRead(buf, len);
                    totalSize += (long) readSize;
                    if (totalSize >= maxLength) {
                        break;
                    }
                    long remainLength = maxLength - totalSize;
                    readSize = remainLength > BUFFER_SIZE ? BUFFER_SIZE : (int) remainLength;
                }
                ret = true;
            }
        } catch (FileNotFoundException e3) {
            Logger.error("FileCopy", "文件没有找到");
        } catch (IOException e4) {
            Logger.error("FileCopy", "IO读取异常");
        } catch (Throwable th3) {
            Logger.error("FileCopy", th3.getMessage());
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                Logger.error("StreamUtil", "io流关闭异常 ");
            }
        }

        return ret;
    }

    public boolean copy(InputStream is, String dest, boolean force) {
        return copy(is, dest, b, force);
    }

    public boolean copy(String src, String dest, long length, boolean force) {
        File srcFile = new File(src);
        if (!srcFile.exists()) {
            return false;
        }
        InputStream in = null;
        try {
            prepare();
            in = new FileInputStream(srcFile);
            return copy(in, dest, length, force);
        } catch (FileNotFoundException e3) {
            Logger.error(TAG, "文件没有找到");
        } catch (Throwable th) {
            Logger.error(TAG, th.getMessage());
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                Logger.error("StreamUtil", "io流关闭异常 ");
            }
        }
        return false;
    }

    public boolean copy(String src, String dest, boolean force) {
        return copy(src, dest, b, force);
    }

    /* access modifiers changed from: protected */
    public void prepare() {
    }

    /* access modifiers changed from: protected */
    public void onRead(byte[] buf, int len) {
    }
}
