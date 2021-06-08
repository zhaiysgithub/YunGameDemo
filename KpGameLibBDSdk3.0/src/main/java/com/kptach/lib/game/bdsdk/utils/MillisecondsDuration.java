package com.kptach.lib.game.bdsdk.utils;

import android.os.SystemClock;

public class MillisecondsDuration {
    private final long time = SystemClock.elapsedRealtime();

    public long duration() {
        return SystemClock.elapsedRealtime() - this.time;
    }

    public long getCurentTime(){
        return SystemClock.elapsedRealtime();
    }

    public long getSavedTime(){
        return time;
    }

    public String toString() {
        return String.valueOf(duration()) + "ms";
    }
}
