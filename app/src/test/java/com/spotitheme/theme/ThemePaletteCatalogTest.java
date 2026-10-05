package com.spotitheme.theme;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ThemePaletteCatalogTest {
    @Test
    public void safFolderRequestCodeFitsActivityResultContract() {
        assertTrue(ThemePaletteCatalog.REQUEST_SELECT_FOLDER >= 0);
        assertTrue(ThemePaletteCatalog.REQUEST_SELECT_FOLDER <= 0xFFFF);
    }

    @Test
    public void duplicateNamesShowTheirSourceAndRemainDistinct() throws Exception {
        ThemePalette builtIn = ThemePaletteParser.parse(
                "{\"schemaVersion\":1,\"id\":\"built-in\",\"name\":\"Ocean\",\"base\":\"dark\",\"colors\":{}}");
        ThemePalette imported = ThemePaletteParser.parse(
                "{\"schemaVersion\":1,\"id\":\"imported\",\"name\":\"ocean\",\"base\":\"light\",\"colors\":{}}");
        List<ThemePaletteCatalog.ThemeEntry> entries = Arrays.asList(
                new ThemePaletteCatalog.ThemeEntry(builtIn, ThemePaletteCatalog.Source.BUNDLED, "Ocean.json", null),
                new ThemePaletteCatalog.ThemeEntry(imported, ThemePaletteCatalog.Source.CUSTOM, "ocean.json", null));

        assertEquals(Arrays.asList("Ocean — Built-in", "ocean — ocean.json"),
                ThemePaletteCatalog.displayLabels(entries));
        assertEquals("ocean — ocean.json", ThemePaletteCatalog.displayLabel(entries.get(1), entries));
    }

    @Test
    public void repeatedSourceLabelsFallBackToStableThemeIds() throws Exception {
        ThemePalette first = ThemePaletteParser.parse(
                "{\"schemaVersion\":1,\"id\":\"first\",\"name\":\"Ocean\",\"base\":\"dark\",\"colors\":{}}");
        ThemePalette second = ThemePaletteParser.parse(
                "{\"schemaVersion\":1,\"id\":\"second\",\"name\":\"Ocean\",\"base\":\"dark\",\"colors\":{}}");
        List<ThemePaletteCatalog.ThemeEntry> entries = Arrays.asList(
                new ThemePaletteCatalog.ThemeEntry(first, ThemePaletteCatalog.Source.CUSTOM, "same.json", null),
                new ThemePaletteCatalog.ThemeEntry(second, ThemePaletteCatalog.Source.CUSTOM, "same.json", null));

        assertEquals(Arrays.asList("Ocean — same.json", "Ocean — same.json [second]"),
                ThemePaletteCatalog.displayLabels(entries));
    }
}
