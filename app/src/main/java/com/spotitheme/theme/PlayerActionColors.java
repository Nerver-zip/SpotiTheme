package com.spotitheme.theme;

/** Color roles for the verified native expanded-player action glyphs only. */
public final class PlayerActionColors {
    private PlayerActionColors() {}

    public static int themed(int source, int neutral, int accent) {
        if ((source & 0xFFFFFF) == 0xFFFFFF) {
            int alpha = Math.round((source >>> 24) * (neutral >>> 24) / 255f);
            return (alpha << 24) | (neutral & 0xFFFFFF);
        }
        return StatusIconColors.themed(source, accent);
    }
}
