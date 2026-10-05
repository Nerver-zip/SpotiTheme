package com.spotitheme.theme;

public final class StatusIconColors {
    private StatusIconColors() {}

    public static boolean isSpotifyAccent(int color) {
        int rgb = color & 0xFFFFFF;
        return (color >>> 24) != 0 && (rgb == 0x1ED760 || rgb == 0x1DB954 || rgb == 0x1ABC54);
    }

    public static boolean isSpotifySavedIndicator(int color) {
        return isSpotifyAccent(color)
                || ((color >>> 24) != 0 && (color & 0xFFFFFF) == 0x1ED75F);
    }

    public static int themed(int color, int accent) {
        if (!isSpotifyAccent(color)) return color;
        int alpha = Math.round((color >>> 24) * (accent >>> 24) / 255f);
        return (alpha << 24) | (accent & 0xFFFFFF);
    }

    public static int themedSavedIndicator(int color, int savedIndicator) {
        if (!isSpotifySavedIndicator(color)) return color;
        int alpha = Math.round((color >>> 24) * (savedIndicator >>> 24) / 255f);
        return (alpha << 24) | (savedIndicator & 0xFFFFFF);
    }
}
