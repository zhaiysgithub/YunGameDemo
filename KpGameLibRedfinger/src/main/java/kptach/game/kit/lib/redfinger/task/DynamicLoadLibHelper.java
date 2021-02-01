package kptach.game.kit.lib.redfinger.task;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import kptach.game.kit.lib.redfinger.RedGameBoxManager;
import kptach.game.kit.lib.redfinger.utils.DeviceUtils;
import kptach.game.kit.lib.redfinger.utils.FileUtils;
import kptach.game.kit.lib.redfinger.utils.Logger;

/* compiled from: DynamicLoadLibHelper */
public class DynamicLoadLibHelper {

    public interface ILoadLibListener {
        void onResult(int code, String msg);
    }

    public static final int LOADLIB_STATUS_SUCCESS = 101;
    public static final int LOADLIB_STATUS_FILE = 102;

    private static final String TAG = DynamicLoadLibHelper.class.getSimpleName();

    public static final String libVer = "1.0.7.9";
    public static final String libDir = "kp_lib";
    public static final String libFileName = "libmci.so";
    public static final String libZipName = "libmci.zip";

    private static final String PROPS_FILE = "KP_RED_FINGER";
    private static final String PROPS_KEY_LIBMD5 = "libMD5";
    private static DynamicLoadLibHelper instance;
    private File mDir;
    private File mLibFile;

    private boolean mLoading = false;

    private Application mApplication;

    private ILoadLibListener mListener;

    public DynamicLoadLibHelper(Application context) {
        this.mApplication = context;
        String path = context.getFilesDir().getParent() + File.separator + libDir + File.separator + libVer;
        this.mDir = new File(path);
        if (!this.mDir.exists() && !this.mDir.mkdirs()) {
            Logger.error(TAG, this.mDir.getAbsolutePath() + " make dir fail!");
        }
        mLibFile = new File(this.mDir, libFileName);
    }
    private static String getFilesPath( Context context ){
        return context.getFilesDir().getParent();
    }

    public void loadLib(ILoadLibListener listener) {
        if (listener == null){
            return;
        }

        this.mListener = listener;
        if (mLibFile!=null && mLibFile.exists()){
            String md5 = mApplication.getSharedPreferences(PROPS_FILE, Context.MODE_PRIVATE).getString(PROPS_KEY_LIBMD5 , null);
            if (md5!=null && checkLibFile(mLibFile, md5)){
                this.mListener.onResult( LOADLIB_STATUS_SUCCESS, mLibFile.getAbsolutePath());
                return;
            }
        }

        File zipFile = new File(this.mDir, libZipName);
        if (zipFile.exists()){
            zipFile.delete();
        }
       boolean r =  FileUtils.copyAssetFile(mApplication.getAssets(),"libmci.zip",  zipFile.getAbsolutePath(), true);
        if (r) {
            r = zip(zipFile, mLibFile.getParent());

        }

        if (r){
            this.mListener.onResult( LOADLIB_STATUS_SUCCESS, mLibFile.getAbsolutePath());
        }

        Log.i("", r+"");

        //下载
//        requestLibVersion();
    }

    private synchronized void requestLibVersion(){
        if (mLoading){
            return;
        }

        mLoading = true;
        new RequestLibVerTask()
                .setCallback(new RequestLibVerTask.ICallback() {
                    @Override
                    public void onSucces(String libZipUrl, String md5) {
                        downloadLibZip(libZipUrl, md5);
                    }

                    @Override
                    public void onFaile(int code, String err) {
                        mLoading = false;
                    }
                })
                .execute(RedGameBoxManager.mCorpID, libVer);
    }

    private void downloadLibZip(String libZipUrl, final String md5){
//        boolean isArm64 = DeviceUtils.is64Bit();
//        if (isArm64){
//            url = "https://osspic.kuaipantech.com/android/kpscorp/REDF-LIB/arm64-v8a/libmci.so";
//        }else {
//            url = "https://osspic.kuaipantech.com/android/kpscorp/REDF-LIB/armeabi-v7a/libmci.so";
//        }

        File zipFile = new File(libZipUrl);
        if (zipFile.exists()){
            zipFile.delete();
        }

        if (mLibFile.exists()){
            mLibFile.delete();
        }

        HttpDownload down = new HttpDownload(libZipUrl, zipFile.getAbsolutePath());
        down.setCallback(new HttpDownload.ICallback() {
            @Override
            public void onSuccess(String zipFile) {
                mLoading = false;

                Logger.info(TAG, "下载完成");

                if (zip(new File(zipFile), mLibFile.getParent())) {
                    //验证文件
                    String str = FileUtils.getMD5(mLibFile);
                    if (str!=null && str.equals(md5)){
                        mApplication.getSharedPreferences(PROPS_FILE, Context.MODE_PRIVATE).edit().putString(PROPS_KEY_LIBMD5, md5).commit();
                        return;
                    }
                }
            }

            @Override
            public void onFailed() {
                mLoading = false;

                Logger.error(TAG, "下载SDK失败");

            }
        });
        down.start();
    }

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
                        String entryName = zipEntry.getName();
                        if (entryName.equals("libmci.so")) {
                            File file2 = new File(str, entryName);
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
