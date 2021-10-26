package com.kptach.lib.game.huawei;

import android.os.Build;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class HWFileUtils {

    /*public static final String so_libcloudapp = "libcloudapp.so";
    public static final String so_libhwsecure = "libhwsecure.so";
    public static final String so_libopus = "libopus.so";
    public static final String so_libVideoDecoder = "libVideoDecoder.so";*/

    public static final String soInfoTestUrl = "https://test-operation.kuaipantech.com/kp/api/hwso/version/info";
    public static final String soInfoUrl = "https://api.omsys.kuaipantech.com/kp/api/hwso/version/info";
    public static final String SP_HWLIB = "sp_hwlib";
    public static final String SPKEY_MD5 = "spkey_somd5";
    public static final String SPKEY_VER = "spkey_sover";
    public static final String SPKEY_UNZIPOK = "spkey_unzipok";
    //zip文件名称
    public static final String libZipName = "hwsolib.zip";

    // 获取CPU名字
    public static String getCpuName() {
        String cpuAbi;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String[] cpuAbis = Build.SUPPORTED_ABIS;
//            boolean contains = Arrays.asList(cpuAbis).contains("arm64-v8a");
//            cpuAbi = contains ? "arm64-v8a" : "armeabi-v7a";
            if (cpuAbis != null && cpuAbis.length > 0) {
                cpuAbi = cpuAbis[0];
            } else {
                cpuAbi = "armeabi-v7a";
            }
        } else {
            cpuAbi = Build.CPU_ABI;
        }
        return cpuAbi;
    }

    public static boolean checkLibFile(File file, String md5) {
        if (md5 == null || md5.trim().isEmpty()) {
            return false;
        }
        String dataMd5 = getMD5(file);
        return dataMd5 != null && dataMd5.equals(md5);
    }

    public static String getMD5(File file) {
        if (file.isFile()) {
            MessageDigest digest;
            FileInputStream in = null;
            byte[] buffer = new byte[1024];
            try {
                digest = MessageDigest.getInstance("MD5");
                in = new FileInputStream(file);
                int len;
                while ((len = in.read(buffer, 0, 1024)) != -1) {
                    digest.update(buffer, 0, len);
                }
                return toHex(digest.digest());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close(in);
            }
        }
        return null;
    }

    private static String toHex(byte[] md) {
        char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        int j = md.length;
        char[] str = new char[j * 2];

        for (int i = 0; i < j; ++i) {
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
                e.printStackTrace();
            }
        }
    }


    /**
     * 解压缩 zip 文件
     *
     * @param zipFile 待压缩的文件
     * @param destDir 压缩后文件的存储目标目录
     * @return -1:error  0:success
     */
    public static int unzipFile(final File zipFile, final File destDir) {
        int BUFFER_LEN = 2048;
        InputStream in = null;
        FileOutputStream out = null;
        try {

            if (zipFile == null || destDir == null) {
                return -1;
            }

            ZipFile zip = new ZipFile(zipFile);
            Enumeration<?> entries = zip.entries();
            while (entries.hasMoreElements()) {

                ZipEntry entry = ((ZipEntry) entries.nextElement());
                //获得的元素是否是目录，是就跳过
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName().replace("\\", "/");
                int speIndex = entryName.indexOf("/");
                String libName = entryName.substring(speIndex + 1);
                File file = new File(destDir, libName);
                if (!file.exists()) {
                    boolean newFile = file.createNewFile();
                    HWCloudGameUtils.info("file:" + file.getName() + ";newFile = " + newFile);
                }
                in = zip.getInputStream(entry);
                out = new FileOutputStream(file);
                byte[] buffer = new byte[BUFFER_LEN];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }
}
