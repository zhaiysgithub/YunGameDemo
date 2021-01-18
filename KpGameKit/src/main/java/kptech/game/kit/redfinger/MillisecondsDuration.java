package kptech.game.kit.redfinger;

import android.os.SystemClock;

public class MillisecondsDuration {
    private final long a = SystemClock.elapsedRealtime();

    public long duration() {
        return SystemClock.elapsedRealtime() - this.a;
    }

    public String toString() {
        return String.valueOf(duration()) + "ms";
    }
}
