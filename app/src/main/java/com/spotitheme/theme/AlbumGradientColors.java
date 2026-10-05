package com.spotitheme.theme;

/** Recognizes the pinned album base fade without recoloring arbitrary brushes. */
public final class AlbumGradientColors {
    private AlbumGradientColors() {}
    public static int[] stops(int artwork, int background) {
        return new int[]{artwork, background};
    }
    public static boolean matchesBaseFade(int start, int end, int resourceStart, int resourceEnd) {
        return (start == resourceStart || start == 0x00181818 || start == 0x00121212)
                && (end == resourceEnd || end == 0xFF121212);
    }
}
