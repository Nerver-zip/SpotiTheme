package com.spotitheme.theme;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ArtworkModePolicyTest {
    @Test public void fixedPaletteAlwaysDisablesAutoTheme() {
        assertFalse(ArtworkModePolicy.autoThemeEnabled(true, true));
        assertFalse(ArtworkModePolicy.autoThemeEnabled(true, false));
    }

    @Test public void artworkModeUsesTheExplicitAutoThemeChoice() {
        assertTrue(ArtworkModePolicy.autoThemeEnabled(false, true));
        assertFalse(ArtworkModePolicy.autoThemeEnabled(false, false));
    }
}
