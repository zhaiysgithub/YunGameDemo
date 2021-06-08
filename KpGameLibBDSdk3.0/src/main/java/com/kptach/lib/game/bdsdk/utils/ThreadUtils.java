package com.kptach.lib.game.bdsdk.utils;

import android.os.Handler;
import android.os.Looper;

public class ThreadUtils {
    private static final Handler handler = new Handler(Looper.getMainLooper());
    public static void runUi(Runnable runnable) {
        if (Looper.myLooper() == handler.getLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }
}
