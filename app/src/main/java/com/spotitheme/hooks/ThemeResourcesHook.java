package com.spotitheme.hooks;

import com.spotitheme.theme.ThemePalette;
import com.spotitheme.theme.ThemeRuntime;

import android.content.res.XResources;
import android.graphics.Color;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ThemeResourcesHook implements IXposedHookInitPackageResources {

    private static final String SPOTIFY_PACKAGE = "com.spotify.music";

    private static volatile XResources spotifyResources;
    private static volatile boolean enabled;
    private static volatile int currentBackground = 0xFF121212;
    private static volatile int currentSurface = 0xFF1F1F1F;
    private static volatile int currentTinted = 0x1AFFFFFF;
    private static volatile int currentText = 0xFFFFFFFF;
    private static volatile int currentTextSubdued = 0xFFB3B3B3;
    private static volatile int currentAccent = 0xFF1ED760;
    private static volatile int currentOnAccent = 0xFF000000;
    private static volatile int currentAlbumHeaderBackground = 0xFF121212;
    private static volatile int currentOutline = 0xFF454545;
    private static final Map<String, Integer> originalColors = new LinkedHashMap<>();

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) {
        if (!SPOTIFY_PACKAGE.equals(resparam.packageName)) {
            return;
        }

        spotifyResources = resparam.res;
        if (enabled) applyPalette(currentBackground, currentSurface, currentTinted, currentText,
                currentTextSubdued, currentAccent, currentOnAccent, currentAlbumHeaderBackground, currentOutline);
    }

    private static synchronized void applyPalette(int background, int surface, int tinted, int text, int textSubdued,
                                                 int accent, int onAccent, int albumHeaderBackground, int outline) {
        XResources resources = spotifyResources;

        currentBackground = background;
        currentSurface = surface;
        currentTinted = tinted;
        currentText = text;
        currentTextSubdued = textSubdued;
        currentAccent = accent;
        currentOnAccent = onAccent;
        currentAlbumHeaderBackground = albumHeaderBackground;
        currentOutline = outline;

        if (resources == null || !enabled) return;

        replace(resources, "dark_base_background_base", background);
        replace(resources, "dark_base_background_highlight", blend(background, Color.BLACK, 0.06f));
        replace(resources, "dark_base_background_press", blend(background, Color.BLACK, 0.12f));

        replace(resources, "dark_base_background_elevated_base", surface);
        replace(resources, "dark_base_background_elevated_highlight", blend(surface, accent, 0.08f));
        replace(resources, "dark_base_background_elevated_press", blend(surface, accent, 0.15f));

        replace(resources, "dark_base_background_tinted_base", tinted);
        replace(resources, "dark_base_background_tinted_highlight", blend(tinted, accent, 0.10f));
        replace(resources, "dark_base_background_tinted_press", blend(tinted, accent, 0.18f));

        replace(resources, "dark_base_text_base", text);
        replace(resources, "dark_base_text_subdued", textSubdued);
        replace(resources, "dark_base_text_brightaccent", accent);

        replace(resources, "dark_base_essential_base", text);
        replace(resources, "dark_base_essential_subdued", textSubdued);
        replace(resources, "dark_base_essential_brightaccent", accent);

        replace(resources, "encore_row_title", text);
        replace(resources, "encore_row_subtitle", textSubdued);

        replace(resources, "gray_7", background);
        replace(resources, "gray_15", surface);
        replace(resources, "gray_20", tinted);
        replace(resources, "gray_70", textSubdued);

        replace(resources, "default_card_background_color", surface);
        replace(resources, "merch_card_background", surface);
        replace(resources, "sidedrawer_background", surface);

        replace(resources, "encore_header_gradient_end", background);
        replace(resources, "header_gradient_end", background);
        replace(resources, "header_overlay_end_color", background);
        replace(resources, "creative_work_header_background", albumHeaderBackground);

        replace(resources, "encore_accent_color", accent);
        replace(resources, "spotify_green_157", accent);
        replace(resources, "green_light", accent);

        replace(resources, "dark_brightaccent_background_base", accent);
        replace(resources, "dark_brightaccent_background_highlight", blend(accent, onAccent, 0.12f));
        replace(resources, "dark_brightaccent_background_press", blend(accent, onAccent, 0.22f));

        replace(resources, "dark_brightaccent_text_base", onAccent);
        replace(resources, "dark_brightaccent_essential_base", onAccent);
        replace(resources, "dark_brightaccent_decorative_base", onAccent);
        replace(resources, "outline", outline);
        replace(resources, "outline_subdued", outline);
        replace(resources, "border_subdued", outline);
        replace(resources, "divider_color", outline);
    }

    public static synchronized void configure(ThemeRuntime.Snapshot state) {
        enabled = state.enabled;
        ThemePalette p = state.palette;
        applyPalette(p.color("background"), p.color("surface"), p.color("tinted"),
                p.color("text"), p.color("textSubdued"), p.color("accent"), p.color("onAccent"),
                p.color("albumHeaderBackground"), p.color("outline"));
        if (!enabled) restoreOriginalPalette();
    }

    /** Native resource value saved before the first replacement, if available. */
    public static synchronized Integer originalColor(String name) { return originalColors.get(name); }

    private static void replace(XResources resources, String name, int color) {
        try {
            if (!originalColors.containsKey(name)) {
                int id = resources.getIdentifier(name, "color", SPOTIFY_PACKAGE);
                if (id != 0) originalColors.put(name, resources.getColor(id, null));
            }
            resources.setReplacement(SPOTIFY_PACKAGE, "color", name, color);
        } catch (Throwable ignored) {
        }
    }

    private static void restoreOriginalPalette() {
        XResources resources = spotifyResources;
        if (resources == null) return;
        for (Map.Entry<String, Integer> color : originalColors.entrySet()) {
            try {
                resources.setReplacement(SPOTIFY_PACKAGE, "color", color.getKey(), color.getValue());
            } catch (Throwable ignored) {
            }
        }
    }

    private static int blend(int first, int second, float amount) {
        float inverse = 1.0f - amount;
        return Color.argb(Math.round(Color.alpha(first) * inverse + Color.alpha(second) * amount), Math.round(Color.red(first) * inverse + Color.red(second) * amount), Math.round(Color.green(first) * inverse + Color.green(second) * amount), Math.round(Color.blue(first) * inverse + Color.blue(second) * amount));
    }
}
