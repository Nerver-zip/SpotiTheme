package com.spotitheme;

import android.os.Looper;
import android.os.SystemClock;
import java.util.concurrent.atomic.AtomicLong;

/** Cumulative main-thread registration time across loader, resources and attach phases. */
public final class StartupMetrics {
    private static final AtomicLong mainNanos = new AtomicLong();
    private static final StartupIntervals intervals = new StartupIntervals();
    private StartupMetrics() {}
    public static long start() { return SystemClock.elapsedRealtimeNanos(); }
    public static void finish(String phase, long started) {
        long ended = SystemClock.elapsedRealtimeNanos();
        long elapsed = ended - started;
        boolean main = Looper.getMainLooper() != null && Looper.myLooper() == Looper.getMainLooper();
        long total;
        if (main) {
            total = intervals.record(started, ended);
            mainNanos.set(total);
        } else {
            total = mainNanos.get();
        }
        ModuleLog.info("Startup phase=" + phase + " mainThread=" + main + " elapsedMs="
                + elapsed / 1_000_000.0 + " totalMainMs=" + total / 1_000_000.0
                + " under50ms=" + (total < 50_000_000L));
    }
}
