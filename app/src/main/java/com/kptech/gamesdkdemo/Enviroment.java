package com.kptech.gamesdkdemo;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class Enviroment {

    private final String appName;
    private final String mCropKey;
    private static Enviroment mEnviroment;

    private Enviroment(String appName, String cropKey) {

        this.appName = appName;
        this.mCropKey = cropKey;
    }

    private static Enviroment createEnv(Application application) {
        try {
            ApplicationInfo info = application.getPackageManager().getApplicationInfo(application.getPackageName(), PackageManager.GET_META_DATA);

            String appName = info.metaData.getString("APP_NAME");
            String cropKey = info.metaData.getString("CROPKEY");

            return new Enviroment(appName, cropKey);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

            return null;
        }
    }

    public static Enviroment getInstance() {
        if (mEnviroment == null) {
            mEnviroment = createEnv(CustomerApplication.appContext);
        }
        return mEnviroment;
    }

    public String getAppName() {
        return appName;
    }

    public String getmCropKey() {
        return mCropKey;
    }

}
