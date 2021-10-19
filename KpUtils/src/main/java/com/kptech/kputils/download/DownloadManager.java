package com.kptech.kputils.download;

import android.util.Log;

import com.kptech.kputils.DbManager;
import com.kptech.kputils.common.Callback;
import com.kptech.kputils.common.task.PriorityExecutor;
import com.kptech.kputils.common.util.LogUtil;
import com.kptech.kputils.db.converter.ColumnConverterFactory;
import com.kptech.kputils.ex.DbException;
import com.kptech.kputils.http.RequestParams;
import com.kptech.kputils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * Author: wyouflf
 * Date: 13-11-10
 * Time: 下午8:10
 */
public final class DownloadManager {

    static {
        // 注册DownloadState在数据库中的值类型映射
        ColumnConverterFactory.registerColumnConverter(DownloadState.class, new DownloadStateConverter());
    }

    //限速 - 高速下载 2M/s
    public static long SPEED_PER_SECOND_H = 2 * 1024 * 1024;
    //限速 - 中速下载 1M/s
    public static long SPEED_PER_SECOND_M = 1024 * 1024;
    //限速 - 低速下载 512/s
    public static long SPEED_PER_SECOND_L = 512 * 1024;

    //每秒下载的数据量 （大于2M不限速）
    private long speedDataPerSecond = SPEED_PER_SECOND_H + 1024;


    private static volatile DownloadManager instance;
    private DownloadExtCallback mExtCallback;

    // 有效的值范围[1, RequestParams.MAX_FILE_LOAD_WORKER], 下载线程太多会影响图片加载.
//    private final static int MAX_DOWNLOAD_THREAD = RequestParams.MAX_FILE_LOAD_WORKER - 3;
    private final static int MAX_DOWNLOAD_THREAD = 1;

    private DbManager db;
    private final Executor executor = new PriorityExecutor(MAX_DOWNLOAD_THREAD, true);
    private final List<DownloadInfo> downloadInfoList = new ArrayList<>();
    //异常数据处理
    private final List<DownloadInfo> delDownloadInfoList = new ArrayList<>();
    private final ConcurrentHashMap<DownloadInfo, DownloadCallback>
            callbackMap = new ConcurrentHashMap<>(2);

    private DownloadManager() {
        DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
                .setDbName("download")
                .setDbVersion(1);
        try {
            delDownloadInfoList.clear();
            db = x.getDb(daoConfig);
            List<DownloadInfo> infoList = db.selector(DownloadInfo.class).findAll();
            if (infoList != null && infoList.size() > 0) {
                for (DownloadInfo info : infoList) {
                    String fileSavePath = info.getFileSavePath();
                    if (fileSavePath == null || fileSavePath.isEmpty()){
                        delDownloadInfoList.add(info);
                        continue;
                    }
                    File file = new File(fileSavePath);  //源文件错误
                    if (!file.exists() || file.length() < 2048){
                        delDownloadInfoList.add(info);
                        continue;
                    }

                    File tmpFile = new File(fileSavePath + ".tmp");  //临时文件错误
                    if (!tmpFile.exists() || tmpFile.length() < 2048){
                        delDownloadInfoList.add(info);
                        continue;
                    }

                    if (info.getState().value() < DownloadState.FINISHED.value()
                            || info.getState().value() == DownloadState.ERROR.value()) {
                        info.setState(DownloadState.STOPPED);
                    }

                    downloadInfoList.add(info);
                }
            }
            delInvalidDownloadInfo();
        } catch (DbException ex) {
            LogUtil.e(ex.getMessage(), ex);
        }
    }

    /*package*/
    public static DownloadManager getInstance() {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager();
                }
            }
        }
        return instance;
    }


    public long getSleepReceiveBuffer(){
        if (speedDataPerSecond > SPEED_PER_SECOND_H){//超速下载，不限制
            return -1;
        }else if(speedDataPerSecond > (SPEED_PER_SECOND_M - 1024)){ //高速下载 每接收48kb计算一次时间
            return 48 * 1024;
        }else if(speedDataPerSecond > (SPEED_PER_SECOND_L - 1024)){ //中速下载  每接收24kb计算一次时间
            return 24 * 1024;
        }else { //每接收2kb计算一次时间
            return 2048;
        }
    }

    /**
     * 删除数据库中无效的数据
     */
    private void delInvalidDownloadInfo() {
        try{
            if (delDownloadInfoList.size() > 0){
                for(DownloadInfo info :delDownloadInfoList){
                    removeDownload(info);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 对外提供的接口
     */
    public void setOnDownloadListener(DownloadExtCallback listener){
        this.mExtCallback = listener;
    }

    /**
     * 设置限速值 （每秒下载的数据量值）单位 byte
     */
    public void updateSpeedPerSecond(boolean isSpeedLimit, long PerSecondBuf){
        if (isSpeedLimit){
            this.speedDataPerSecond = PerSecondBuf;
        }else {
            this.speedDataPerSecond = (SPEED_PER_SECOND_H + 1024);
        }

    }

    public long getSpeedPerSecond(){
        return speedDataPerSecond;
    }

    public void updateDownloadInfo(DownloadInfo info) throws DbException {
        db.update(info);
    }

    public int getDownloadListCount() {
        return downloadInfoList.size();
    }


    /**
     * 通过下载地址查找对应的下载对象
     */
    public DownloadInfo getDownloadInfo(String url){

        if (db == null){
            return null;
        }
        DownloadInfo downloadInfo = null;
        try{
            downloadInfo = db.selector(DownloadInfo.class).where("url", "=" , url).findFirst();
        }catch (Exception e){
            e.printStackTrace();
        }
        return downloadInfo;
    }

    /**
     * 通过下载地址获取当前对象的状态信息
     */
    public int getDownloadInfoState(String url){
        DownloadInfo downloadInfo = getDownloadInfo(url);
        if (downloadInfo != null){
            return downloadInfo.getState().value();
        }else {
            return -1;
        }
    }

    /**
     * 获取当前进度
     */
    public int getDownloadProgress(String url){
        DownloadInfo downloadInfo = getDownloadInfo(url);
        if (downloadInfo != null){
            return downloadInfo.getProgress();
        }else {
            return 0;
        }
    }

    /**
     * 开始下载
     */
    public synchronized void startDownload(String url, String originPkgName, String savedPath) throws DbException {
        startDownload(url,originPkgName,savedPath,true,true,null);
    }

    /**
     * 开始下载
     */
    private synchronized void startDownload(String url, String label, String savePath,
                                           boolean autoResume, boolean autoRename,
                                           DownloadViewHolder viewHolder) throws DbException {

        String fileSavePath = new File(savePath).getAbsolutePath();
        DownloadInfo downloadInfo = db.selector(DownloadInfo.class)
                .where("label", "=", label)
                .and("fileSavePath", "=", fileSavePath)
                .findFirst();
        if (downloadInfo != null) {
            DownloadCallback callback = callbackMap.get(downloadInfo);
            if (callback != null) {
                if (viewHolder == null) {
                    viewHolder = new DefaultDownloadViewHolder(null, downloadInfo);
                }
                if (callback.switchViewHolder(viewHolder)) {
                    return;
                } else {
                    callback.cancel();
                }
            }
        }

        // create download info
        if (downloadInfo == null) {
            downloadInfo = new DownloadInfo();
            downloadInfo.setUrl(url);
            downloadInfo.setAutoRename(autoRename);
            downloadInfo.setAutoResume(autoResume);
            downloadInfo.setLabel(label);
            downloadInfo.setFileSavePath(fileSavePath);
            db.saveBindingId(downloadInfo);
        }

        // start downloading
        if (viewHolder == null) {
            viewHolder = new DefaultDownloadViewHolder(null, downloadInfo);
        } else {
            viewHolder.update(downloadInfo);
        }
        DownloadCallback callback = new DownloadCallback(viewHolder,mExtCallback);
        callback.setDownloadManager(this);
        callback.switchViewHolder(viewHolder);
        RequestParams params = new RequestParams(url);
        params.setAutoResume(downloadInfo.isAutoResume());
        params.setAutoRename(downloadInfo.isAutoRename());
        params.setSaveFilePath(downloadInfo.getFileSavePath());
        params.setExecutor(executor);
        params.setCancelFast(true);
        Callback.Cancelable cancelable = x.http().get(params, callback);
        callback.setCancelable(cancelable);
        callbackMap.put(downloadInfo, callback);

        if (downloadInfoList.contains(downloadInfo)) {
            int index = downloadInfoList.indexOf(downloadInfo);
            downloadInfoList.remove(downloadInfo);
            downloadInfoList.add(index, downloadInfo);
        } else {
            downloadInfoList.add(downloadInfo);
        }
    }

    /**
     * 暂停下载
     */
    public void stopDownload(String infoUrl){
        if (infoUrl == null || infoUrl.isEmpty()){
            return;
        }
        DownloadInfo info = getDownloadInfo(infoUrl);
        if (info != null){
            stopDownload(info);
        }
    }

    public void stopDownload(DownloadInfo downloadInfo) {
        Callback.Cancelable cancelable = callbackMap.get(downloadInfo);
        if (cancelable != null) {
            cancelable.cancel();
        }
    }

    public void removeDownload(DownloadInfo downloadInfo) throws DbException {
        db.delete(downloadInfo);
        stopDownload(downloadInfo);
        downloadInfoList.remove(downloadInfo);
    }

    /**
     * 删除下载的数据
     */
    public void removeDownload(String downloadUrl){
        try {
            DownloadInfo downloadInfo = getDownloadInfo(downloadUrl);
            if (downloadInfo != null){
                removeDownload(downloadInfo);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
