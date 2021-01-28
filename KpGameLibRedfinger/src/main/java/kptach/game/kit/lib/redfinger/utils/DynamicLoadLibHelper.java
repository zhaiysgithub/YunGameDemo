package kptach.game.kit.lib.redfinger.utils;

import android.content.Context;
import android.nfc.cardemulation.OffHostApduService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/* compiled from: DynamicLoadLibHelper */
public class DynamicLoadLibHelper {
    private static final String TAG = DynamicLoadLibHelper.class.getSimpleName();

    public static final String libDir = "kp_lib";
    public static final String libVer = "1.0.7.9";
    public static final String libFileName = "libmci.so";

    private static final String PROPS_FILE = "KP_RED_FINGER";
    private static final String PROPS_KEY_LIBMD5 = "libMD5";
    private static DynamicLoadLibHelper instance;
    private File mDir;
    private String mCurMd5;

    private DynamicLoadLibHelper(Context context) {
        this.mDir = context.getDir(libDir, 0).getAbsoluteFile();
        this.mCurMd5 = context.getSharedPreferences(PROPS_FILE, 0).getString(PROPS_KEY_LIBMD5, null);

        if (!this.mDir.exists() && !this.mDir.mkdirs()) {
            Logger.error(TAG, this.mDir.getAbsolutePath() + " make dir fail!");
        }
    }

    public static DynamicLoadLibHelper getInstance(Context context) {
        if (instance == null) {
            synchronized(DynamicLoadLibHelper.class) {
                if (instance == null) {
                    instance = new DynamicLoadLibHelper(context);
                }
            }
        }
        return instance;
    }

//    public static String getLibMciFilePath(Context context){
//        String path = context.getFilesDir().getParent() + File.separator + libDir + File.separator + libVer;
//        File file = new File(path);
//        if (!file.exists()){
//            file.mkdirs();
//        }
//        file = new File(file, libFileName);
//        return file.getAbsolutePath();
//    }

    public boolean checkLibFile(File file, String md5){
        if (md5 == null || md5.trim().length() < 0){
            return false;
        }
        String dataMd5 = FileUtils.getMD5(file);
        if (dataMd5 != null && dataMd5.equals(md5)) {
            return true;
        }
        return false;
    }

    public boolean zip(File file, String str) {
        Logger.info(TAG, "zip文件目录:" + file.exists() + " :" + file.getAbsolutePath());
        Logger.info(TAG, "解压目录:" + str + " " + new File(str).exists() + "  isFile:" + new File(str).isFile());

            ZipFile zipFile = null;
            BufferedInputStream bufferedInputStream = null;
            BufferedOutputStream bufferedOutputStream = null;

            try {
                zipFile = new ZipFile(file);
                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                    if (!zipEntry.isDirectory()) {
                        byte[] bArr = new byte[1024];
                        if (zipEntry.getName().contains("libredfinger_qn.so")) {
                            File file2 = new File(str, "libredfinger_qn.so");
                            if (file2.exists() && !file2.delete()) {
                                Logger.error(TAG, "delete file fail:" + file2.getAbsolutePath());
                            }
                            try {
                                file2.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            bufferedInputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file2));

                            while (true) {
                                int read = bufferedInputStream.read(bArr, 0, 1024);
                                if (read != -1) {
                                    bufferedOutputStream.write(bArr, 0, read);
                                } else {
                                    break;
                                }
                            }
                            Logger.info(TAG, "zip2:" + file2.exists() + "   fff:" + file2.isFile() + "~~~" + file2.getAbsolutePath());
                            return true;
                        }
                    }
                }

            }catch (Exception e){
                Logger.error(TAG, "upZipFile.ZipFile:" + e.getMessage());
            }finally {
                try {
                    if (bufferedOutputStream != null) {
                        bufferedOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    if (bufferedInputStream != null) {
                        bufferedInputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    if (zipFile!=null) {
                        zipFile.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return false;
    }
}
