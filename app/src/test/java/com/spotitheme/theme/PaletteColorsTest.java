package com.spotitheme.theme;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PaletteColorsTest {
    @Test
    public void preservesMochaColorWhenMatchingRolesSplitInDestinationTheme() {
        PaletteColors mocha = new PaletteColors(
                // Order matches currentNativePalette(): background, surface, tinted, backgroundHighlight.
                new int[]{0xFF1E1E2E, 0xFF181825, 0xFF313244, 0xFF313244},
                new int[]{0xFFCDD6F4, 0xFFA6ADC8, 0xFFCBA6F7});
        PaletteColors nord = new PaletteColors(
                new int[]{0xFFECEFF4, 0xFFE5E9F0, 0xFFE5E9F0, 0xFFD8DEE9},
                new int[]{0xFF2E3440, 0xFF4C566A, 0xFF5E81AC});

        assertEquals(0xFF313244, mocha.map(0xFF313244, nord, false));
    }

    @Test
    public void mapsDuplicateSourceRolesWhenTheirDestinationColorsAgree() {
        PaletteColors old = new PaletteColors(
                new int[]{0xFF313244, 0xFF313244}, new int[0]);
        PaletteColors next = new PaletteColors(
                new int[]{0xFF45475A, 0xFF45475A}, new int[0]);

        assertEquals(0xFF45475A, old.map(0xFF313244, next, false));
    }

    @Test
    public void mapsUniqueForegroundAndBackgroundColors() {
        PaletteColors old = new PaletteColors(
                new int[]{0xFF181825}, new int[]{0xFFCDD6F4});
        PaletteColors next = new PaletteColors(
                new int[]{0xFF2E3440}, new int[]{0xFFD8DEE9});

        assertEquals(0xFF2E3440, old.map(0xFF181825, next, false));
        assertEquals(0xFFD8DEE9, old.map(0xFFCDD6F4, next, true));
    }

    @Test
    public void preservesAlphaForUniqueRgbMatch() {
        PaletteColors old = new PaletteColors(new int[]{0xFF181825}, new int[0]);
        PaletteColors next = new PaletteColors(new int[]{0xFF2E3440}, new int[0]);

        assertEquals(0x802E3440, old.map(0x80181825, next, false));
    }

    @Test
    public void preservesAmbiguousAlphaSafeRgbMatch() {
        PaletteColors old = new PaletteColors(new int[]{0xFF313244, 0xFF313244}, new int[0]);
        PaletteColors next = new PaletteColors(new int[]{0xFFE5E9F0, 0xFFD8DEE9}, new int[0]);

        assertEquals(0x80313244, old.map(0x80313244, next, false));
    }

    @Test
    public void leavesTransparentAndUnknownColorsUnchanged() {
        PaletteColors old = new PaletteColors(new int[]{0xFF181825}, new int[0]);
        PaletteColors next = new PaletteColors(new int[]{0xFF2E3440}, new int[0]);

        assertEquals(0x00181825, old.map(0x00181825, next, false));
        assertEquals(0xFF123456, old.map(0xFF123456, next, false));
    }
}
