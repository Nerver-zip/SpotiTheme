package com.spotitheme.theme;

import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class BundledThemesTest {
    @Test public void bundledCatalogContainsEveryAdaptedThemeWithUniqueIdentity() throws Exception {
        Path directory = Path.of("src/main/assets/themes");
        assertTrue("Bundled assets must be available", Files.isDirectory(directory));
        Set<String> ids = new HashSet<>();
        int count = 0;
        try (java.util.stream.Stream<Path> files = Files.list(directory)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".json")).toList()) {
                ThemePalette palette = ThemePaletteParser.parse(new String(Files.readAllBytes(file), java.nio.charset.StandardCharsets.UTF_8));
                assertTrue("Duplicate theme: " + palette.getId(), ids.add(palette.getId()));
                assertEquals(ThemePalette.COLOR_ROLES.size(), palette.getColors().size());
                count++;
            }
        }
        assertEquals("28 adapted palettes plus the regular default", 29, count);
        assertTrue("The regular Catppuccin Mocha palette remains bundled", ids.contains("catppuccin-mocha"));
        assertFalse("The Mocha Mauve palette is excluded from the bundled catalog",
                ids.contains("catppuccin-mocha-mauve"));
        assertFalse("The previous built-in Mocha Mauve identity is excluded",
                ids.contains("bundled-catppuccin-mocha-mauve"));
    }

    @Test public void bothRetiredMochaMauveIdsAreRecognizedForFallback() {
        assertTrue(ThemePaletteStore.isRetiredMochaMauveId("catppuccin-mocha-mauve"));
        assertTrue(ThemePaletteStore.isRetiredMochaMauveId("bundled-catppuccin-mocha-mauve"));
        assertFalse(ThemePaletteStore.isRetiredMochaMauveId("catppuccin-mocha"));
    }
}
