package com.kptach.lib.game.redfinger.utils;

import android.content.res.AssetManager;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.Properties;

public class FileUtils {
    public static final String TAG = FileUtils.class.getSimpleName();
    public static final DecimalFormat ONE_DECIMAL_POINT_DF = new DecimalFormat("0.0");
    private static final int a = 1073741824;
    private static final int b = 1048576;
    private static final int c = 1024;

    public static String formatSizeInByte(long sizeInByte) {
        if (sizeInByte >= 1073741824) {
            return ONE_DECIMAL_POINT_DF.format(((double) sizeInByte) / 1.073741824E9d) + "G";
        }
        if (sizeInByte >= 1048576) {
            return ONE_DECIMAL_POINT_DF.format(((double) sizeInByte) / 1048576.0d) + "M";
        }
        if (sizeInByte >= 1024) {
            return ONE_DECIMAL_POINT_DF.format(((double) sizeInByte) / 1024.0d) + "K";
        }
        return sizeInByte + "B";
    }

    public static boolean copyFile(String src, String dest, boolean force) {
        return new FileCopy().copy(src, dest, force);
    }

    public static boolean copyFile(InputStream is, String dest, boolean force) {
        return new FileCopy().copy(is, dest, force);
    }

    public static boolean copyFile(String src, String dest, long maxLength, boolean force) {
        return new FileCopy().copy(src, dest, maxLength, force);
    }

    public static boolean copyAssetFile(AssetManager assetManager, String assetsFilePath, String dest, boolean force) {
        InputStream is = null;
        try {
            is = assetManager.open(assetsFilePath);
            return copyFile(is, dest, force);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            close(is);
        }
        return false;
    }

    public static boolean isAssetDirectory(AssetManager am, String assetPath) {
        try {
            if (am.list(assetPath).length > 0) {
                return true;
            }
            am.open(assetPath);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public static String readAssetProperties(AssetManager assetManager, String file, String propKey){
        String ver = null;
        InputStream input = null;
        try {
            Properties properties = new Properties();
            input = assetManager.open(file);
            properties.load(input);
            ver = properties.getProperty(propKey, null);
        }catch (FileNotFoundException fe){
            Logger.error(TAG, "Asset文件不存在 " + file);
        }catch (Exception e){
            e.printStackTrace();
            Logger.error(TAG, e.getMessage());
        }finally {
            close(input);
        }
        return ver;
    }

    public static String readProperties(String file, String propKey){
        String ver = null;
        FileInputStream input = null;
        try {
            Properties properties = new Properties();
            input = new FileInputStream(file);
            properties.load(input);
            ver = properties.getProperty(propKey, null);
        }catch (FileNotFoundException fe){
            Logger.error(TAG, "文件不存在 " + file);
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }finally {
            close(input);
        }
        return ver;
    }

    public static boolean saveProperties(String file, String propKey, String propValue){
        //保存版本文件
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            Properties properties = new Properties();
            properties.put(propKey, propValue);
            properties.store(out,null);
            return true;
        }catch (Exception e){
            Logger.error(TAG, e.getMessage());
        }finally {
            close(out);
        }
        return false;
    }

    public static String getMD5(File file) {
        if (!file.isFile()) {
            return null;
        } else {
            MessageDigest digest = null;
            FileInputStream in = null;
            byte[] buffer = new byte[1024];
            try {
                digest = MessageDigest.getInstance("MD5");
                in = new FileInputStream(file);
                int len;
                while((len = in.read(buffer, 0, 1024)) != -1) {
                    digest.update(buffer, 0, len);
                }
                return toHex(digest.digest());
            } catch (Exception e) {
                Logger.error(TAG, e.getMessage());
            } finally {
                close(in);
            }
            return null;
        }
    }

    private static String toHex(byte[] md) {
        char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        int j = md.length;
        char[] str = new char[j * 2];

        for(int i = 0; i < j; ++i) {
            byte byte0 = md[i];
            str[2 * i] = hexDigits[byte0 >>> 4 & 15];
            str[i * 2 + 1] = hexDigits[byte0 & 15];
        }

        return new String(str);
    }

    private static void close(Closeable obj) {
        if (obj != null) {
            try {
                obj.close();
            } catch (IOException e) {
                Logger.error("StreamUtil", "io流关闭异常 ");
            }
        }
    }
}
