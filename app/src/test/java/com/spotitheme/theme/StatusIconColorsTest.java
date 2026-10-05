package com.spotitheme.theme;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StatusIconColorsTest {
    @Test
    public void mapsSpotifyGreenUsedByConnectIconToThemeAccent() {
        int source = 0xFF1ED760;
        int themeAccent = 0xFFFFCC66;

        assertTrue(StatusIconColors.isSpotifyAccent(source));
        assertEquals(themeAccent, StatusIconColors.themed(source, themeAccent));
    }

    @Test
    public void mapsObservedSavedCheckGreenWithoutChangingGenericAccentAliases() {
        int source = 0xFF1ED75F;
        int savedIndicator = 0xFFCA9EE6;

        assertTrue(StatusIconColors.isSpotifySavedIndicator(source));
        assertFalse(StatusIconColors.isSpotifyAccent(source));
        assertEquals(savedIndicator, StatusIconColors.themedSavedIndicator(source, savedIndicator));
        assertEquals(source, StatusIconColors.themed(source, savedIndicator));
    }

    @Test
    public void preservesSourceAlphaWhenMappingSavedIndicator() {
        assertEquals(0x80CA9EE6,
                StatusIconColors.themedSavedIndicator(0x801ED75F, 0xFFCA9EE6));
    }
}
