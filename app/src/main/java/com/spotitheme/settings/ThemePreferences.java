package com.spotitheme.settings;

import android.content.Context;
import android.content.SharedPreferences;
import com.spotitheme.theme.ThemeRuntime;

/** Vector redirects this theme-only store into its supported shared-preference directory. */
public final class ThemePreferences {
    private ThemePreferences() {}
    @SuppressWarnings("deprecation")
    public static SharedPreferences open(Context context) {
        try { return context.getSharedPreferences(ThemeRuntime.PREFERENCES, Context.MODE_WORLD_READABLE); }
        catch (SecurityException unavailable) {
            return context.getSharedPreferences(ThemeRuntime.PREFERENCES, Context.MODE_PRIVATE);
        }
    }
}
