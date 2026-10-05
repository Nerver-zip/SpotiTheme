package com.spotitheme.settings;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Process;
import com.spotitheme.theme.ThemePaletteStore;
import com.spotitheme.theme.ThemeRuntime;

/** Spotify can read the selected palette; external callers cannot modify settings. */
public final class SettingsProvider extends ContentProvider {
    public static final Uri URI = Uri.parse("content://com.spotitheme.settings/state");
    public static final String ENABLED = "theme_enabled", AUTO = "auto_theme", MODE = "auto_theme_mode";

    @Override public boolean onCreate() { return true; }

    public static void notifyChanged(Context context) {
        context.getContentResolver().notifyChange(URI, null);
    }

    @Override public Bundle call(String method, String argument, Bundle extras) {
        if (!allowed(getContext(), Binder.getCallingUid()))
            throw new SecurityException("Settings are available only to Spotify");
        if (!"snapshot".equals(method)) throw new IllegalArgumentException("Unknown settings operation");
        return snapshot(getContext());
    }

    static boolean allowed(Context context, int uid) {
        if (uid == Process.myUid()) return true;
        String[] packages = context.getPackageManager().getPackagesForUid(uid);
        if (packages != null) for (String name : packages)
            if ("com.spotify.music".equals(name)) return true;
        return false;
    }

    static Bundle snapshot(Context context) {
        SharedPreferences prefs = ThemePreferences.open(context);
        Bundle result = new Bundle();
        String json = prefs.getString(ThemePaletteStore.LAST_GOOD_JSON, null);
        if (json == null && ThemePaletteStore.hasLegacyPalette(prefs))
            json = new com.google.gson.Gson().toJson(ThemePaletteStore.legacyPalette(prefs).toJsonObject());
        result.putString("palette", json);
        result.putBoolean(ENABLED, prefs.getBoolean(ENABLED, true));
        result.putBoolean(ThemePaletteStore.FIXED_MODE, prefs.getBoolean(ThemePaletteStore.FIXED_MODE, true));
        result.putBoolean(AUTO, prefs.getBoolean(AUTO, false));
        result.putString(MODE, prefs.getString(MODE, "neutral"));
        return result;
    }

    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] args, String order) {
        throw new UnsupportedOperationException("Use snapshot");
    }
    @Override public String getType(Uri uri) { return "vnd.android.cursor.item/vnd.spotitheme.settings"; }
    @Override public Uri insert(Uri uri, ContentValues values) { throw new SecurityException("Read-only settings bridge"); }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] args) {
        throw new SecurityException("Read-only settings bridge");
    }
    @Override public int delete(Uri uri, String selection, String[] args) {
        throw new SecurityException("Read-only settings bridge");
    }
}
