package com.spotitheme.theme;

import android.graphics.drawable.GradientDrawable;

public final class AlbumArtworkGradient extends GradientDrawable {
    public AlbumArtworkGradient(int artwork, int endpoint) {
        super(Orientation.TOP_BOTTOM, AlbumGradientColors.stops(artwork, endpoint));
    }
}
