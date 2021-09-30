package com.kptech.kputils.config;

import com.kptech.kputils.DbManager;
import com.kptech.kputils.common.util.LogUtil;
import com.kptech.kputils.ex.DbException;

/**
 * Created by wyouflf on 15/7/31.
 * 全局db配置
 */
public enum DbConfigs {
    HTTP(new DbManager.DaoConfig()
            .setDbName("xUtils_http_cache.db")
            .setDbVersion(2)
            .setDbOpenListener(new DbManager.DbOpenListener() {
                @Override
                public void onDbOpened(DbManager db) {
                    db.getDatabase().enableWriteAheadLogging();
                }
            })
            .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                @Override
                public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
                    try {
                        db.dropDb(); // 默认删除所有表
                    } catch (DbException ex) {
                        LogUtil.e(ex.getMessage(), ex);
                    }
                }
            })),

    COOKIE(new DbManager.DaoConfig()
            .setDbName("xUtils_http_cookie.db")
            .setDbVersion(1)
            .setDbOpenListener(new DbManager.DbOpenListener() {
                @Override
                public void onDbOpened(DbManager db) {
                    db.getDatabase().enableWriteAheadLogging();
                }
            })
            .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                @Override
                public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
                    try {
                        db.dropDb(); // 默认删除所有表
                    } catch (DbException ex) {
                        LogUtil.e(ex.getMessage(), ex);
                    }
                }
            }));

    private DbManager.DaoConfig config;

    DbConfigs(DbManager.DaoConfig config) {
        this.config = config;
    }

    public DbManager.DaoConfig getConfig() {
        return config;
    }
}
