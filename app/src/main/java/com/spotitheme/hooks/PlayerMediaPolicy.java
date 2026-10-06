package com.spotitheme.hooks;

/** Conservative decisions for the pinned square-cover and featured-video bindings. */
public final class PlayerMediaPolicy {
    public enum Mode { UNKNOWN, STILL_ARTWORK, FEATURED_VIDEO }
    private PlayerMediaPolicy() {}

    public static Mode classify(boolean squareCoverBinding, boolean bitmapReady,
            boolean videoBinding, boolean videoShown, boolean videoReady) {
        if (videoBinding) return videoShown && videoReady ? Mode.FEATURED_VIDEO : Mode.UNKNOWN;
        return squareCoverBinding && bitmapReady ? Mode.STILL_ARTWORK : Mode.UNKNOWN;
    }

    public static boolean suppressBackdrop(boolean enabled, Mode mode) {
        return enabled && mode == Mode.STILL_ARTWORK;
    }
}
