package com.spotitheme;

import android.util.Log;

public final class ModuleLog {
    private ModuleLog() {}
    public static void info(String message) { Log.i("SpotiTheme", message); }
    public static void error(String message, Throwable failure) { Log.e("SpotiTheme", message, failure); }
}
