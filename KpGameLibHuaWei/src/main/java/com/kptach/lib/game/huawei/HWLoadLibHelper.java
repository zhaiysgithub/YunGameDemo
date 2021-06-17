package com.kptach.lib.game.huawei;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
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

    private SharedPreferences sharedPreferences;
    //存放so和zip的文件夹   data/data/packagename/lib
    private File mLibDir;
    //zip文件   data/data/packagename/lib/hwsolib.zip
    private File mZipFile;
    private ILoadLibListener mListener;
    private boolean isLoading;
    private String mCpuInfo;

    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public HWLoadLibHelper(Application context) {
        boolean mkdirs = false;
        try {
            sharedPreferences = context.getSharedPreferences(HWFileUtils.SP_HWLIB, Context.MODE_PRIVATE);
            mLibDir = context.getDir("lib", Context.MODE_PRIVATE);
            if (!mLibDir.exists()) {
                mkdirs = mLibDir.mkdirs();
            }
            Log.e(TAG, "dir:" + mLibDir.getAbsolutePath() + ";mkdirs = " + mkdirs);
            createZipFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除重新创建空文件
     */
    private void createZipFile() {
        boolean delete = false;
        boolean createNewFile = false;
        try {
            if (mZipFile != null && mZipFile.exists() && mZipFile.length() > 0) {
                delete = mZipFile.delete();
            }
            if (mZipFile == null) {
                mZipFile = new File(mLibDir, HWFileUtils.libZipName);
            }
            if (!mZipFile.exists()) {
                createNewFile = mZipFile.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        HWCloudGameUtils.info("mZipFile delete = " + delete + ";createNewFile = " + createNewFile);
    }

    /**
     * 检查文件md5是否正确
     */
    private boolean checkZipFile(String md5Value) {
        boolean result = false;
        if (mZipFile != null && mZipFile.exists() && mZipFile.length() > 0) {
            result = HWFileUtils.checkLibFile(mZipFile, md5Value);
        }
        return result;
    }

    public void loadLib(String corpKey, String sdkVersion, int soVersion, ILoadLibListener listener) {
        if (listener == null) {
            return;
        }
        this.mListener = listener;

        mCpuInfo = HWFileUtils.getCpuName();
        if (mCpuInfo == null || mCpuInfo.isEmpty()) {
            mListener.onResult(LOADLIB_STATUS_FAIL, "cpuInfo is empty");
            return;
        }

        int cacheVersion = 0;
        boolean unzipSuccess = false;
        String md5Value = "";
        if (sharedPreferences != null) {
            cacheVersion = sharedPreferences.getInt(HWFileUtils.SPKEY_VER, 0);
            unzipSuccess = sharedPreferences.getBoolean(HWFileUtils.SPKEY_UNZIPOK, false);
            md5Value = sharedPreferences.getString(HWFileUtils.SPKEY_MD5, "");
        }
        if (cacheVersion != HWGameBoxManager.soVersion) {
            requestUpdate(corpKey, sdkVersion, soVersion);
        } else {
            boolean zipFileCorrect = checkZipFile(md5Value);
            if (zipFileCorrect) {
                if (unzipSuccess) {
                    mListener.onResult(LOADLIB_STATUS_SUCCESS, "so file correct");
                } else {
                    startUnZipFile(mZipFile.getAbsolutePath());
                }
            } else {
                requestUpdate(corpKey, sdkVersion, soVersion);
            }
        }
    }

    /**
     * 请求版本校验
     */
    private void requestUpdate(String corpKey, String sdkVersion, int soVersion) {
        createZipFile();
        if (isLoading) {
            return;
        }
        isLoading = true;

        executor.execute(() ->
                HWTaskHelper.instance().getAppSoInfo(corpKey, sdkVersion, soVersion, mCpuInfo
                        , new HWTaskHelper.TaskCallback() {
                            @Override
                            public void onSucces(String libUrl, String md5) {

                                String[] datas = new String[2];
                                datas[0] = libUrl;
                                datas[1] = md5;
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
    private void downloadLibZip(String url) {
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

    /**
     * 解压文件
     */
    private void startUnZipFile(String zipFilePath) {

        File[] files = mLibDir.listFiles();
        if (files == null || files.length == 0) {
            isLoading = false;
            mListener.onResult(LOADLIB_STATUS_FAIL, " zipFile is empty");
            return;
        }

        boolean isDel;
        String fileName;
        for (File file : files) {
            fileName = file.getName();
            if (HWFileUtils.libZipName.equals(fileName)) {
                continue;
            }
            //删除过期文件
            isDel = file.delete();
            HWCloudGameUtils.info("unZipFileDel:name=" + fileName + ";isDel=" + isDel);
        }
        File destDir = new File(mLibDir, mCpuInfo);
        if (!destDir.exists()) {
            boolean mkdirs = destDir.mkdirs();
            HWCloudGameUtils.info("startUnZipFile:destDir=" + mkdirs);
        }
        executor.execute(() -> {
            int code = HWFileUtils.unzipFile(new File(zipFilePath), destDir);
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
                        if (datas == null || datas.length != 2) {
                            isLoading = false;
                            mListener.onResult(LOADLIB_STATUS_FAIL, "requestAppSoInfo error");
                            return;
                        }
                        String libUrl = datas[0];
                        String md5 = datas[1];

                        //本地存储md5和so版本
                        if (sharedPreferences != null) {
                            sharedPreferences.edit().putString(HWFileUtils.SPKEY_MD5, md5).putInt(HWFileUtils.SPKEY_VER, HWGameBoxManager.soVersion).apply();
                        }
                        //执行下载
                        downloadLibZip(libUrl);
                        break;

                    case WHAT_REQUEST_APPINFO_FAIL:
                        String errorMsg = (String) msg.obj;
                        isLoading = false;
                        mListener.onResult(LOADLIB_STATUS_FAIL, errorMsg);
                        break;

                    case WHAT_DOWNLOAD_SUCCESS:
                        //开始解压并存储到指定文件
                        String zipFilePath = (String) msg.obj;
                        if (zipFilePath == null || zipFilePath.isEmpty()) {
                            isLoading = false;
                            mListener.onResult(LOADLIB_STATUS_FAIL, "zipFile is empty");
                            return;
                        }
                        String md5Value = "";
                        if (sharedPreferences != null) {
                            md5Value = sharedPreferences.getString(HWFileUtils.SPKEY_MD5, "");
                        }
                        if (!checkZipFile(md5Value)) {
                            isLoading = false;
                            mListener.onResult(LOADLIB_STATUS_FAIL, "zipfile md5 check value is wrong");
                            return;
                        }

                        startUnZipFile(zipFilePath);
                        break;

                    case WHAT_DOWNLOAD_FAIL:
                        isLoading = false;
                        String downloadErroMsg = (String) msg.obj;
                        mListener.onResult(LOADLIB_STATUS_FAIL, downloadErroMsg);
                        break;
                    case WHAT_UNZIP_SUCCESS:
                        if (sharedPreferences != null) {
                            sharedPreferences.edit().putBoolean(HWFileUtils.SPKEY_UNZIPOK, true).apply();
                        }
                        isLoading = false;
                        mListener.onResult(LOADLIB_STATUS_SUCCESS, mLibDir.getAbsolutePath());
                        break;
                    case WHAT_UNZIP_FAIL:
                        if (sharedPreferences != null) {
                            sharedPreferences.edit().putBoolean(HWFileUtils.SPKEY_UNZIPOK, false).apply();
                        }
                        isLoading = false;
                        mListener.onResult(LOADLIB_STATUS_FAIL, "unzipFile error");
                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
