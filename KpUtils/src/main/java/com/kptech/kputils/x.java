package com.kptech.kputils;

import android.app.Application;

import com.kptech.kputils.common.TaskController;
import com.kptech.kputils.common.task.TaskControllerImpl;
import com.kptech.kputils.config.DownloadSpeedConfig;
import com.kptech.kputils.db.DbManagerImpl;
import com.kptech.kputils.ex.DbException;
import com.kptech.kputils.http.HttpManagerImpl;
import com.kptech.kputils.image.ImageManagerImpl;
import com.kptech.kputils.view.ViewInjectorImpl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;


/**
 * Created by wyouflf on 15/6/10.
 * 任务控制中心, http, image, db, view注入等接口的入口.
 * 需要在在application的onCreate中初始化: x.Ext.init(this);
 */
public final class x {

    private x() {
    }

    public static boolean isDebug() {
        return Ext.debug;
    }

    public static DownloadSpeedConfig getSpeedConfig(){
        return Ext.speedConfig;
    }

    public static Application app() {
        if (Ext.app == null) {
            throw new RuntimeException("please invoke x.Ext.init(app) on Application#onCreate()"
                    + " and register your Application in manifest.");
        }
        return Ext.app;
    }

    public static TaskController task() {
        return Ext.taskController;
    }

    public static HttpManager http() {
        if (Ext.httpManager == null) {
            HttpManagerImpl.registerInstance();
        }
        return Ext.httpManager;
    }

    public static ImageManager image() {
        if (Ext.imageManager == null) {
            ImageManagerImpl.registerInstance();
        }
        return Ext.imageManager;
    }

    public static ViewInjector view() {
        if (Ext.viewInjector == null) {
            ViewInjectorImpl.registerInstance();
        }
        return Ext.viewInjector;
    }

    public static DbManager getDb(DbManager.DaoConfig daoConfig) throws DbException {
        return DbManagerImpl.getInstance(daoConfig);
    }

    public static class Ext {
        private static boolean debug;
        private static Application app;
        private static TaskController taskController;
        private static HttpManager httpManager;
        private static ImageManager imageManager;
        private static ViewInjector viewInjector;
        private static DownloadSpeedConfig speedConfig = DownloadSpeedConfig.SPEED_HIGH;

        private Ext() {
        }

        public static void init(Application app) {
            TaskControllerImpl.registerInstance();
            if (Ext.app == null) {
                Ext.app = app;
            }
        }

        public static void setDebug(boolean debug) {
            Ext.debug = debug;
        }

        public static void setSpeedConfig(DownloadSpeedConfig speedConfig){
            Ext.speedConfig = speedConfig;
        }

        public static void setTaskController(TaskController taskController) {
            if (Ext.taskController == null) {
                Ext.taskController = taskController;
            }
        }

        public static void setHttpManager(HttpManager httpManager) {
            Ext.httpManager = httpManager;
        }

        public static void setImageManager(ImageManager imageManager) {
            Ext.imageManager = imageManager;
        }

        public static void setViewInjector(ViewInjector viewInjector) {
            Ext.viewInjector = viewInjector;
        }

        public static void setDefaultHostnameVerifier(HostnameVerifier hostnameVerifier) {
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
        }
    }
}
