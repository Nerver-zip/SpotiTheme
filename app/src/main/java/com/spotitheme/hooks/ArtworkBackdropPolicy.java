package com.spotitheme.hooks;

/** Applies the native Spotify artwork blur preference only while SpotiTheme is enabled. */
public final class ArtworkBackdropPolicy {
    private ArtworkBackdropPolicy() {}

    public static boolean shouldHide(boolean themeEnabled, boolean showArtworkBackdrop) {
        return themeEnabled && !showArtworkBackdrop;
    }
}
