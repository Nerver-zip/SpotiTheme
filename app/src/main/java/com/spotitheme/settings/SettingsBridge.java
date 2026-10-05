package com.spotitheme.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import com.spotitheme.ModuleLog;
import com.spotitheme.theme.ArtworkThemeController;
import com.spotitheme.theme.ThemePalette;
import com.spotitheme.theme.ThemePaletteParser;
import com.spotitheme.theme.ThemePaletteStore;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XSharedPreferences;

/** Read-only preferences and JSON parsing stay off Spotify's main-thread startup path. */
public final class SettingsBridge {
    private final Context context;
    private final ThemePalette fallback;
    private final ArtworkThemeController controller;
    private final HandlerThread thread = new HandlerThread("SpotiTheme-settings");
    private Handler worker;
    private XSharedPreferences shared;
    private final SharedPreferences.OnSharedPreferenceChangeListener changes = (preferences, key) ->
            worker.post(this::reloadShared);

    public SettingsBridge(Context context, ThemePalette fallback, ArtworkThemeController controller) {
        this.context = context.getApplicationContext();
        this.fallback = fallback; this.controller = controller;
    }
    public void start() {
        thread.start();
        worker = new Handler(thread.getLooper());
        worker.post(() -> {
            try {
                shared = new XSharedPreferences("com.spotitheme", ThemeRuntime.PREFERENCES);
                if (shared.getFile().canRead()) {
                    shared.registerOnSharedPreferenceChangeListener(changes);
                    reloadShared();
                    ModuleLog.info("Vector shared theme preferences connected");
                    return;
                }
                ModuleLog.info("Shared theme preferences unavailable; trying provider");
            } catch (RuntimeException failure) { ModuleLog.error("Shared preferences unavailable", failure); }
            try {
                context.getContentResolver().registerContentObserver(SettingsProvider.URI, false,
                        new ContentObserver(worker) {
                            @Override public void onChange(boolean selfChange) { reloadProvider(); }
                        });
            } catch (RuntimeException failure) { ModuleLog.error("Settings observer registration failed", failure); }
            reloadProvider();
        });
    }
    private void reloadShared() {
        try {
            shared.reload();
            Bundle state = new Bundle();
            String json = shared.getString(ThemePaletteStore.LAST_GOOD_JSON, null);
            if (json == null && ThemePaletteStore.hasLegacyPalette(shared))
                json = new com.google.gson.Gson().toJson(ThemePaletteStore.legacyPalette(shared).toJsonObject());
            state.putString("palette", json);
            state.putBoolean(SettingsProvider.ENABLED, shared.getBoolean(SettingsProvider.ENABLED, true));
            state.putBoolean(ThemePaletteStore.FIXED_MODE, shared.getBoolean(ThemePaletteStore.FIXED_MODE, true));
            state.putBoolean(SettingsProvider.AUTO, shared.getBoolean(SettingsProvider.AUTO, false));
            state.putString(SettingsProvider.MODE, shared.getString(SettingsProvider.MODE, "neutral"));
            apply(state);
        } catch (RuntimeException failure) { ModuleLog.error("Shared settings reload failed", failure); }
    }
    private void reloadProvider() {
        try {
            Bundle state = context.getContentResolver().call(SettingsProvider.URI, "snapshot", null, null);
            if (state == null) throw new IllegalStateException("Missing settings snapshot");
            apply(state);
        } catch (RuntimeException failure) { ModuleLog.error("Settings snapshot rejected; current palette retained", failure); }
    }
    private void apply(Bundle state) {
        try {
            String json = state.getString("palette");
            ThemePalette palette = json == null ? fallback : ThemePaletteParser.parse(json);
            if (ThemePaletteStore.isRetiredMochaMauveId(palette.getId())) palette = fallback;
            ModuleLog.info("Settings snapshot received; palette=" + palette.getId());
            controller.configure(palette, state.getBoolean(SettingsProvider.ENABLED, true),
                    state.getBoolean(ThemePaletteStore.FIXED_MODE, true), state.getBoolean(SettingsProvider.AUTO, false),
                    state.getString(SettingsProvider.MODE, "neutral"));
        } catch (Exception failure) { ModuleLog.error("Settings snapshot rejected; current palette retained", failure); }
    }
}
