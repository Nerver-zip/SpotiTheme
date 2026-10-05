package com.spotitheme.theme;

import android.content.SharedPreferences;

import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ThemePaletteStoreTest {
    @Test
    public void legacyLightPalettePreservesSavedSurfaceBackgroundAndAccent() {
        Map<String, Object> values = new HashMap<>();
        values.put("theme_palette_saved", true);
        values.put("theme_background", 0xFFEFF1F5);
        values.put("theme_surface", 0xFFE9ECF2);
        values.put("theme_text", 0xFF4C4F69);
        values.put("theme_accent", 0xFF8839EF);

        ThemePalette palette = ThemePaletteStore.legacyPalette(preferences(values));

        assertEquals("light", palette.getBase());
        assertEquals(0xFFEFF1F5, palette.color("background"));
        assertEquals(0xFFE9ECF2, palette.color("playerBackground"));
        assertEquals(0xFFEFF1F5, palette.color("albumHeaderBackground"));
        assertEquals(0xFF8839EF, palette.color("savedIndicator"));
        assertEquals(0xFF454545, palette.color("outline"));
    }

    @Test
    public void previouslyCapturedLightBackupOverridesOldHardcodedDarkBase() {
        Map<String, Object> values = new HashMap<>();
        values.put(ThemePaletteStore.SELECTED_ID, "catppuccin-latte");
        values.put(ThemePaletteStore.LEGACY_AVAILABLE, true);
        values.put("legacy_theme_legacy_base", "dark");
        values.put("legacy_theme_background", 0xFFEFF1F5);
        values.put("legacy_theme_surface", 0xFFE9ECF2);
        values.put("legacy_theme_text", 0xFF4C4F69);
        values.put("legacy_theme_accent", 0xFF8839EF);

        ThemePalette palette = ThemePaletteStore.legacyPalette(preferences(values));

        assertEquals("light", palette.getBase());
        assertEquals(0xFFE9ECF2, palette.color("playerBackground"));
        assertEquals(0xFFEFF1F5, palette.color("albumHeaderBackground"));
        assertEquals(0xFF8839EF, palette.color("savedIndicator"));
    }

    @Test
    public void legacyDarkPalettePreservesSavedSurfaceBackgroundAndAccent() {
        Map<String, Object> values = new HashMap<>();
        values.put("theme_background", 0xFF121212);
        values.put("theme_surface", 0xFF262626);
        values.put("theme_text", 0xFFFFFFFF);
        values.put("theme_accent", 0xFFCBA6F7);

        ThemePalette palette = ThemePaletteStore.legacyPalette(preferences(values));

        assertEquals("dark", palette.getBase());
        assertEquals(0xFF262626, palette.color("playerBackground"));
        assertEquals(0xFF121212, palette.color("albumHeaderBackground"));
        assertEquals(0xFFCBA6F7, palette.color("savedIndicator"));
        assertEquals(0xFF454545, palette.color("outline"));
    }

    private static SharedPreferences preferences(Map<String, Object> values) {
        return (SharedPreferences) Proxy.newProxyInstance(
                SharedPreferences.class.getClassLoader(),
                new Class<?>[]{SharedPreferences.class},
                (proxy, method, args) -> {
                    String key;
                    switch (method.getName()) {
                        case "contains":
                            return values.containsKey(args[0]);
                        case "getBoolean":
                        case "getInt":
                        case "getLong":
                        case "getFloat":
                        case "getString":
                        case "getStringSet":
                            key = (String) args[0];
                            return values.containsKey(key) ? values.get(key) : args[1];
                        default:
                            throw new UnsupportedOperationException(method.getName());
                    }
                });
    }
}
