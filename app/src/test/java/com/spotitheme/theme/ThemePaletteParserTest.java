package com.spotitheme.theme;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

public class ThemePaletteParserTest {
    private static final String VALID = "{\"schemaVersion\":1,\"id\":\"unit-test\",\"name\":\"Unit Test\",\"base\":\"dark\",\"colors\":{\"accent\":\"#AABBCC\",\"scrim\":\"#10203040\",\"futureRole\":false}}";

    @Test
    public void parsesOpaqueAndRgbaColorsAndIgnoresUnknownRoles() throws Exception {
        ThemePalette palette = ThemePaletteParser.parse(VALID);

        assertEquals(0xFFAABBCC, palette.color("accent"));
        assertEquals(0x40102030, palette.color("scrim"));
        assertEquals(0xFFAABBCC, palette.color("savedIndicator"));
        assertNotEquals(0, palette.color("background"));
    }

    @Test
    public void omittedRolesComeFromTheDeclaredBase() throws Exception {
        ThemePalette dark = ThemePaletteParser.parse(VALID);
        ThemePalette light = ThemePaletteParser.parse(VALID.replace("\"base\":\"dark\"", "\"base\":\"light\""));

        assertNotEquals(dark.color("background"), light.color("background"));
        assertEquals(dark.color("accent"), dark.color("savedIndicator"));
        assertEquals(light.color("accent"), light.color("savedIndicator"));
    }

    @Test
    public void usesFilenameFallbackWhenNameIsOmitted() throws Exception {
        String nameless = VALID.replace("\"name\":\"Unit Test\",", "");
        ThemePalette palette = ThemePaletteParser.parse(nameless, "Imported Palette");
        assertEquals("Imported Palette", palette.getName());
        assertEquals(palette.color("accent"), palette.color("savedIndicator"));
    }

    @Test
    public void rejectsMalformedColorWithoutReturningPartialPalette() {
        String invalid = VALID.replace("#AABBCC", "green");
        assertThrows(ThemePaletteParser.PaletteParseException.class, () -> ThemePaletteParser.parse(invalid));
    }

    @Test
    public void rejectsUnknownSchemaVersionAndNonObjectColor() {
        assertThrows(ThemePaletteParser.PaletteParseException.class,
                () -> ThemePaletteParser.parse(VALID.replace("\"schemaVersion\":1", "\"schemaVersion\":2")));
        assertThrows(ThemePaletteParser.PaletteParseException.class,
                () -> ThemePaletteParser.parse(VALID.replace("\"schemaVersion\":1", "\"schemaVersion\":1.5")));
        assertThrows(ThemePaletteParser.PaletteParseException.class,
                () -> ThemePaletteParser.parse(VALID.replace("\"accent\":\"#AABBCC\"", "\"accent\":[]")));
    }

    @Test
    public void boundsBothStringAndStreamReads() {
        String oversized = " ".repeat(ThemePaletteParser.MAX_BYTES + 1);
        assertThrows(ThemePaletteParser.PaletteParseException.class, () -> ThemePaletteParser.parse(oversized));
        assertThrows(ThemePaletteParser.PaletteParseException.class,
                () -> ThemePaletteParser.parse(new ByteArrayInputStream(oversized.getBytes(StandardCharsets.UTF_8))));
    }
}
