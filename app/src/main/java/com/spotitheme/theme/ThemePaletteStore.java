package com.spotitheme.theme;

import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.LinkedHashMap;
import java.util.Map;

/** Preference adapter for selected JSON themes and the pre-library manual palette. */
public final class ThemePaletteStore {
    public static final String SELECTED_ID = "theme_selected_id";
    public static final String SELECTED_NAME = "theme_selected_name";
    public static final String LAST_GOOD_JSON = "theme_last_good_json";
    public static final String FOLDER_URI = "theme_folder_uri";
    public static final String FIXED_MODE = "theme_fixed_palette";
    public static final String LEGACY_AVAILABLE = "theme_legacy_backup_available";

    private static final Gson GSON = new Gson();
    private static final Map<String, String> ACTIVE_KEYS = new LinkedHashMap<>();

    static {
        ACTIVE_KEYS.put("background", "theme_background");
        ACTIVE_KEYS.put("backgroundHighlight", "theme_background_highlight");
        ACTIVE_KEYS.put("backgroundPress", "theme_background_press");
        ACTIVE_KEYS.put("surface", "theme_surface");
        ACTIVE_KEYS.put("surfaceHighlight", "theme_surface_highlight");
        ACTIVE_KEYS.put("surfacePress", "theme_surface_press");
        ACTIVE_KEYS.put("tinted", "theme_tinted");
        ACTIVE_KEYS.put("tintedHighlight", "theme_tinted_highlight");
        ACTIVE_KEYS.put("tintedPress", "theme_tinted_press");
        ACTIVE_KEYS.put("text", "theme_text");
        ACTIVE_KEYS.put("textSubdued", "theme_text_subdued");
        ACTIVE_KEYS.put("accent", "theme_accent");
        ACTIVE_KEYS.put("accentHighlight", "theme_accent_highlight");
        ACTIVE_KEYS.put("accentPress", "theme_accent_press");
        ACTIVE_KEYS.put("announcement", "theme_announcement");
        ACTIVE_KEYS.put("decorative", "theme_decorative");
        ACTIVE_KEYS.put("decorativeSubdued", "theme_decorative_subdued");
        ACTIVE_KEYS.put("negative", "theme_negative");
        ACTIVE_KEYS.put("warning", "theme_warning");
        ACTIVE_KEYS.put("positive", "theme_positive");
        ACTIVE_KEYS.put("scrim", "theme_scrim");
        ACTIVE_KEYS.put("onAccent", "theme_on_accent");
        ACTIVE_KEYS.put("savedIndicator", "theme_saved_indicator");
        ACTIVE_KEYS.put("playerBackground", "theme_player_background");
        ACTIVE_KEYS.put("albumHeaderBackground", "theme_album_header_background");
        ACTIVE_KEYS.put("outline", "theme_outline");
    }

    private ThemePaletteStore() {}

    public static ThemePalette restoreSelected(SharedPreferences preferences) {
        String selectedId = preferences.getString(SELECTED_ID, null);
        String cachedJson = preferences.getString(LAST_GOOD_JSON, null);
        if (selectedId == null || cachedJson == null) return null;
        try {
            ThemePalette palette = ThemePaletteParser.parse(cachedJson);
            return selectedId.equals(palette.getId()) ? palette : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static ThemePalette legacyPalette(SharedPreferences preferences) {
        boolean useBackup = preferences.getString(SELECTED_ID, null) != null
                && preferences.getBoolean(LEGACY_AVAILABLE, false);
        String prefix = useBackup ? "legacy_" : "";
        String base = inferLegacyBase(preferences, prefix);
        Map<String, Integer> colors = new LinkedHashMap<>(ThemePalette.defaultsFor(base));
        for (Map.Entry<String, String> entry : ACTIVE_KEYS.entrySet()) {
            String key = prefix + entry.getValue();
            if (preferences.contains(key)) colors.put(entry.getKey(), preferences.getInt(key, colors.get(entry.getKey())));
        }
        if (!preferences.contains(prefix + "theme_player_background")) {
            colors.put("playerBackground", colors.get("surface"));
        }
        if (!preferences.contains(prefix + "theme_album_header_background")) {
            colors.put("albumHeaderBackground", colors.get("background"));
        }
        if (!preferences.contains(prefix + "theme_saved_indicator")) {
            colors.put("savedIndicator", colors.get("accent"));
        }
        if (!preferences.contains(prefix + "theme_outline")) {
            colors.put("outline", 0xFF454545);
        }
        return new ThemePalette("legacy-custom", "Legacy Custom", base, colors);
    }

    public static boolean hasLegacyPalette(SharedPreferences preferences) {
        if (preferences.getString(SELECTED_ID, null) != null) {
            return preferences.getBoolean(LEGACY_AVAILABLE, false);
        }
        return preferences.getBoolean("theme_palette_saved", false);
    }

    /** Saves a complete palette and its last-good JSON before changing hook state. */
    public static void select(SharedPreferences preferences, ThemePalette palette) {
        captureLegacyIfNeeded(preferences);
        SharedPreferences.Editor editor = preferences.edit()
                .putString(SELECTED_ID, palette.getId())
                .putString(SELECTED_NAME, palette.getName())
                .putString(LAST_GOOD_JSON, GSON.toJson(palette.toJsonObject()))
                .putBoolean("theme_palette_saved", true)
                .putBoolean("theme_enabled", true)
                .putBoolean(FIXED_MODE, true);
        writeColors(editor, palette, "");
        editor.apply();
    }

    /** Selects the saved pre-library manual palette and clears JSON selection identity. */
    public static ThemePalette selectLegacy(SharedPreferences preferences) {
        ThemePalette palette = legacyPalette(preferences);
        SharedPreferences.Editor editor = preferences.edit()
                .putBoolean("theme_palette_saved", true)
                .putString(SELECTED_NAME, palette.getName())
                .putBoolean("theme_enabled", true)
                .putBoolean(FIXED_MODE, true);
        writeColors(editor, palette, "");
        editor.remove(SELECTED_ID).remove(LAST_GOOD_JSON).apply();
        return palette;
    }

    /** Records edits from the retained manual editor as the new Legacy Custom palette. */
    public static void saveLegacyFromActive(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit().putBoolean(LEGACY_AVAILABLE, true)
                .putString("legacy_theme_legacy_base", inferLegacyBase(preferences, ""));
        for (String key : ACTIVE_KEYS.values()) {
            if (preferences.contains(key)) editor.putInt("legacy_" + key, preferences.getInt(key, 0));
        }
        editor.remove(SELECTED_ID).remove(LAST_GOOD_JSON).putString(SELECTED_NAME, "Legacy Custom").apply();
    }

    public static void writeColors(SharedPreferences.Editor editor, ThemePalette palette, String keyPrefix) {
        for (Map.Entry<String, String> entry : ACTIVE_KEYS.entrySet()) {
            editor.putInt(keyPrefix + entry.getValue(), palette.color(entry.getKey()));
        }
    }

    public static Map<String, String> activePreferenceKeys() {
        return java.util.Collections.unmodifiableMap(ACTIVE_KEYS);
    }

    private static void captureLegacyIfNeeded(SharedPreferences preferences) {
        if (preferences.getString(SELECTED_ID, null) != null || preferences.getBoolean(LEGACY_AVAILABLE, false)
                || !preferences.getBoolean("theme_palette_saved", false)) return;
        SharedPreferences.Editor editor = preferences.edit().putBoolean(LEGACY_AVAILABLE, true)
                .putString("legacy_theme_legacy_base", inferLegacyBase(preferences, ""));
        for (String key : ACTIVE_KEYS.values()) {
            if (preferences.contains(key)) editor.putInt("legacy_" + key, preferences.getInt(key, 0));
        }
        editor.apply();
    }

    private static String inferLegacyBase(SharedPreferences preferences, String prefix) {
        String backgroundKey = prefix + "theme_background";
        if (preferences.contains(backgroundKey)) {
            return inferLegacyBase(preferences.getInt(backgroundKey, 0xFF121212));
        }

        String surfaceKey = prefix + "theme_surface";
        if (preferences.contains(surfaceKey)) {
            return inferLegacyBase(preferences.getInt(surfaceKey, 0xFF1F1F1F));
        }
        return preferences.getString(prefix + "theme_legacy_base", "dark");
    }

    static String inferLegacyBase(int argb) {
        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = argb & 0xFF;
        int perceivedBrightness = (299 * red + 587 * green + 114 * blue) / 1000;
        return perceivedBrightness >= 128 ? "light" : "dark";
    }
}
