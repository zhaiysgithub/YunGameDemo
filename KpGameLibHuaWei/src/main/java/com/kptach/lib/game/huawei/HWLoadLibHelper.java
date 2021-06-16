package com.kptach.lib.game.huawei;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 加载 so 库工具类
 * 包含
 * libcloudapp.so ， libhwsecure.so ， libopus.so ， libVideoDecoder.so
 * 四个文件
 * 步骤：校验版本，下载zip, 压缩zip,
 */
public class HWLoadLibHelper {

    private static final String TAG = "HW";

    private static final int WHAT_REQUEST_APPINFO_SUCCESS = 0;
    private static final int WHAT_REQUEST_APPINFO_FAIL = 1;
    private static final int WHAT_DOWNLOAD_SUCCESS = 2;
    private static final int WHAT_DOWNLOAD_FAIL = 3;
    private static final int WHAT_UNZIP_SUCCESS = 4;
    private static final int WHAT_UNZIP_FAIL = 5;
    public static final int LOADLIB_STATUS_SUCCESS = 6;
    public static final int LOADLIB_STATUS_FAIL = 7;

    //校验版本的地址
    public static final String checkVerionUrl = "";
    private static final String SPKEY_MD5 = "spkey_somd5";
    private static final String SPKEY_VER = "spkey_sover";
    private static final String libZipName = "hwsolib.zip";
    private String soFilePath;
    private File mLibDir;
    private File mZipFile;
    private ILoadLibListener mListener;
    private boolean isLoading;

    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public HWLoadLibHelper(Application context) {
        boolean mkdirs = false;
        try {
            mLibDir = context.getDir("lib", Context.MODE_PRIVATE);
            if (!mLibDir.exists()) {
                mkdirs = mLibDir.mkdirs();
            }
            Log.e(TAG, "dir:" + mLibDir.getAbsolutePath() + ";mkdirs = " + mkdirs);
            mZipFile = new File(mLibDir, libZipName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadLib(String corpKey,String sdkVersion, int soVersion,ILoadLibListener listener) {
        if (listener == null) {
            return;
        }
        String cpuInfo = HWFileUtils.getCpuName();
        if (cpuInfo == null || cpuInfo.isEmpty()){
            listener.onResult(LOADLIB_STATUS_FAIL, "cpuInfo is empty");
           return;
        }
        this.mListener = listener;
        File soFile = new File(mLibDir, cpuInfo);
        soFilePath = soFile.getAbsolutePath();
        boolean soFilemkDirs = false;
        if (!soFile.exists()){
            soFilemkDirs = soFile.mkdirs();
        }
        HWCloudGameUtils.info("soFilemkDirs=" + soFilemkDirs + ";soFilePath = " + soFilePath + ";cpuInfo = " + cpuInfo);
        requestUpdate(corpKey, sdkVersion, cpuInfo);
    }

    /**
     * 请求版本校验
     */
    private void requestUpdate(String corpKey, String sdkVersion, String cpuInfo) {
        if (isLoading) {
            return;
        }

        isLoading = true;

        executor.execute(() ->
                HWTaskHelper.instance().getAppSoInfo(corpKey, sdkVersion, cpuInfo
                        , new HWTaskHelper.TaskCallback() {
                            @Override
                            public void onSucces(String libUrl, String md5, String soVersion) {
                                String[] datas = new String[3];
                                datas[0] = libUrl;
                                datas[1] = md5;
                                datas[2] = soVersion;
                                Message msg = Message.obtain();
                                msg.what = WHAT_REQUEST_APPINFO_SUCCESS;
                                msg.obj = datas;
                                mHandler.sendMessage(msg);
                            }

                            @Override
                            public void onFaile(int code, String errMsg) {
                                HWCloudGameUtils.error("code:" + code + ";errMsg:" + errMsg);
                                Message msg = Message.obtain();
                                msg.what = WHAT_REQUEST_APPINFO_FAIL;
                                msg.obj = errMsg;
                                mHandler.sendMessage(msg);
                            }
                        }));
    }

    /**
     * 下载 lib zip 文件
     */
    private void downloadLibZip(String url, String md5) {
        boolean zipIsDel = false;
        if (mZipFile.exists()) {
            zipIsDel = mZipFile.delete();
        }
        mZipFile = new File(mLibDir, libZipName);

        HWCloudGameUtils.info("zipIsDel=" + zipIsDel);

        executor.execute(() ->
                HWTaskHelper.instance().startDownloadLibZip(url, mZipFile, new HWTaskHelper.DownloadCallback() {

                    @Override
                    public void onSuccess(String filePath) {
                        Message msg = Message.obtain();
                        msg.what = WHAT_DOWNLOAD_SUCCESS;
                        msg.obj = filePath;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void onFailed(String errorMsg) {
                        Message msg = Message.obtain();
                        msg.what = WHAT_DOWNLOAD_FAIL;
                        msg.obj = errorMsg;
                        mHandler.sendMessage(msg);
                    }
                }));
    }

    private void startUnZipFile(String zipFilePath) {

        File destDir = mLibDir;

        File[] files = destDir.listFiles();
        if (files == null || files.length == 0) {
            isLoading = false;
            mListener.onResult(LOADLIB_STATUS_FAIL, " zipFile is empty");
            return;
        }

        boolean isDel;
        String fileName;
        for (File file : files) {
            fileName = file.getName();
            if (libZipName.equals(fileName)) {
                continue;
            }
            //删除过期文件
            isDel = file.delete();
            HWCloudGameUtils.info("unZipFileDel:name=" + fileName + ";isDel=" + isDel);
        }
        File soFile = new File(soFilePath);
        if (!soFile.exists()){
            boolean mkdirs = soFile.mkdirs();
            HWCloudGameUtils.info("soFilemkdirs:" + mkdirs);
        }
        executor.execute(() -> {
            int code = HWFileUtils.unzipFileByKeyword(new File(zipFilePath), destDir);
            int what = (code == 0 ? WHAT_UNZIP_SUCCESS : WHAT_UNZIP_FAIL);
            mHandler.sendEmptyMessage(what);
        });

    }


    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            try {

                int what = msg.what;
                switch (what) {
                    case WHAT_REQUEST_APPINFO_SUCCESS:
                        String[] datas = (String[]) msg.obj;
                        if (datas == null || datas.length != 3){
                            isLoading = false;
                            mListener.onResult(LOADLIB_STATUS_FAIL, "requestAppSoInfo error");
                            return;
                        }
                        String libUrl = datas[0];
                        String md5 = datas[1];
                        String soVerion = datas[2];

                        /*SharedPreferences sharedPreferences = mContext.getSharedPreferences(SP_HWLIB, Context.MODE_PRIVATE);
                        String md5Value = sharedPreferences.getString(SPKEY_MD5, "");
                        String verValue = sharedPreferences.getString(SPKEY_VER, "");*/

                        if (mZipFile != null && mZipFile.exists() && mZipFile.length() > 0) {
                            if (HWFileUtils.checkLibFile(mZipFile, md5)) {
                                mListener.onResult(LOADLIB_STATUS_SUCCESS, mZipFile.getAbsolutePath());
                                return;
                            }
                            isLoading = false;
                        } else {
                            //执行下载
                            downloadLibZip(libUrl, md5);
                        }
                        break;

                    case WHAT_REQUEST_APPINFO_FAIL:
                        String errorMsg = (String) msg.obj;
                        isLoading = false;
                        File soFile = new File(soFilePath);
                        if (soFile.exists()){
                            mListener.onResult(LOADLIB_STATUS_SUCCESS, "requestAppSoInfoError:" + errorMsg);
                        }else {
                            mListener.onResult(LOADLIB_STATUS_FAIL, errorMsg);
                        }

                        break;

                    case WHAT_DOWNLOAD_SUCCESS:
                        //开始解压并存储到指定文件
                        String zipFilePath = (String) msg.obj;
                        if (zipFilePath == null || zipFilePath.isEmpty()) {
                            mListener.onResult(LOADLIB_STATUS_FAIL, "zipFile is empty");
                            return;
                        }

                        startUnZipFile(zipFilePath);
                        break;

                    case WHAT_DOWNLOAD_FAIL:
                        isLoading = false;
                        String downloadErroMsg = (String) msg.obj;

                        File downloadPhaseSoFile = new File(soFilePath);
                        if (downloadPhaseSoFile.exists()){
                            mListener.onResult(LOADLIB_STATUS_SUCCESS, "DOWNLOAD_FAIL:" + downloadErroMsg);
                        }else {
                            mListener.onResult(LOADLIB_STATUS_FAIL, downloadErroMsg);
                        }
                        break;
                    case WHAT_UNZIP_SUCCESS:
                        isLoading = false;
                        mListener.onResult(LOADLIB_STATUS_SUCCESS, mLibDir.getAbsolutePath());
                        break;
                    case WHAT_UNZIP_FAIL:
                        isLoading = false;
                        File unZipPhaseSoFile = new File(soFilePath);
                        if (unZipPhaseSoFile.exists()){
                            mListener.onResult(LOADLIB_STATUS_SUCCESS, "UNZIP_FAIL:unzipFile error");
                        }else {
                            mListener.onResult(LOADLIB_STATUS_FAIL, "unzipFile error");
                        }
                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
