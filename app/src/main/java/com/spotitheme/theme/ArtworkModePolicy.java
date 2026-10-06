package com.spotitheme.theme;

/** Keeps Auto Theme inactive unless artwork-derived colors are enabled. */
public final class ArtworkModePolicy {
    private ArtworkModePolicy() {}

    public static boolean autoThemeEnabled(boolean fixedPalette, boolean autoTheme) {
        return !fixedPalette && autoTheme;
    }
}
