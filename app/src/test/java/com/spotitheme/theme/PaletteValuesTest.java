package com.spotitheme.theme;

import org.junit.Test;
import static org.junit.Assert.*;

public class PaletteValuesTest {
    @Test public void packedSrgbPreservesArgbIncludingAlpha() {
        for (int color : new int[]{0, 0xFFFFFFFF, 0xFF181825, 0x1AFFFFFF, 0x80123456}) {
            long packed = PaletteValues.pack(color);
            assertEquals(color, PaletteValues.unpack(packed));
            assertEquals(0L, packed & 0xFFFFFFFFL);
        }
    }

    @Test public void fixedModeOverridesArtworkButNativeModeRetainsIt() {
        assertNull(PaletteValues.backgroundRole(new int[]{0xFF654321}, false));
        assertEquals("background", PaletteValues.backgroundRole(new int[]{0xFF654321}, true));
        assertEquals("surface", PaletteValues.backgroundRole(new int[]{0xFF1F1F1F}, false));
        assertEquals("accent", PaletteValues.backgroundRole(new int[]{0xFF1ED760}, false));
        assertEquals("tinted", PaletteValues.backgroundRole(new int[]{0x1AFFFFFF}, false));
    }

    @Test public void materialSurfaceAndForegroundSlotsUseSelectedRoles() throws Exception {
        ThemePalette palette = ThemePaletteParser.parse("{\"schemaVersion\":1,\"id\":\"fixture\",\"name\":\"Fixture\",\"base\":\"light\",\"colors\":{\"background\":\"#112233\",\"surface\":\"#445566\",\"text\":\"#778899\",\"accent\":\"#AABBCC\"}}");
        Object[] colors = PaletteValues.material(palette);
        assertEquals(48, colors.length);
        assertEquals(PaletteValues.pack(0xFFAABBCC), colors[0]);
        assertEquals(PaletteValues.pack(0xFF112233), colors[13]);
        assertEquals(PaletteValues.pack(0xFF778899), colors[14]);
        assertEquals(PaletteValues.pack(0xFF445566), colors[15]);
        assertEquals(PaletteValues.pack(0xFF778899), colors[16]);
    }
}
