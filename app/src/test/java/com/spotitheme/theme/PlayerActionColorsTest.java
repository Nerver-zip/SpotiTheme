package com.spotitheme.theme;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class PlayerActionColorsTest {
    @Test public void neutralWhiteUsesThemeForeground() {
        assertEquals(0xFF4C4F69, PlayerActionColors.themed(0xFFFFFFFF, 0xFF4C4F69, 0xFF8839EF));
        assertEquals(0x4DCDD6F4, PlayerActionColors.themed(0x4DFFFFFF, 0xFFCDD6F4, 0xFFCBA6F7));
    }

    @Test public void activeAccentKeepsItsRoleAndOpacity() {
        assertEquals(0xFFCBA6F7, PlayerActionColors.themed(0xFF1ED760, 0xFFCDD6F4, 0xFFCBA6F7));
        assertEquals(0x66CBA6F7, PlayerActionColors.themed(0x661DB954, 0xFFCDD6F4, 0xFFCBA6F7));
    }

    @Test public void unknownRolesAndTransparencyArePreserved() {
        assertEquals(0xFFED2C3F, PlayerActionColors.themed(0xFFED2C3F, 0xFFCDD6F4, 0xFFCBA6F7));
        assertEquals(0x00CDD6F4, PlayerActionColors.themed(0x00FFFFFF, 0xFFCDD6F4, 0xFFCBA6F7));
        assertEquals(0x27CDD6F4, PlayerActionColors.themed(0x4DFFFFFF, 0x80CDD6F4, 0xFFCBA6F7));
    }
}
