package com.spotitheme.theme;

import org.junit.Test;
import static org.junit.Assert.*;

public class ArtworkColorExtractorTest {
    @Test public void dominantColoredRegionWinsOverMinority() {
        assertEquals(0xFFB04030, ArtworkColorExtractor.select(new int[]{
                0xFFB04030, 0xFFB04030, 0xFFB04030, 0xFFB04030, 0xFF3050B0}));
    }

    @Test public void whiteBorderAndTransparentPixelsDoNotDominate() {
        assertEquals(0xFF30A060, ArtworkColorExtractor.select(new int[]{
                0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0x003050B0, 0xFF30A060}));
    }

    @Test public void monochromeFallbackRetainsActualImageColor() {
        assertEquals(0xFFFFFFFF, ArtworkColorExtractor.select(new int[]{0xFFFFFFFF, 0xFFFFFFFF}));
        assertEquals(0xFF000000, ArtworkColorExtractor.select(new int[]{0xFF000000}));
    }

    @Test public void EmptyOrTransparentImageHasStableFallback() {
        assertEquals(0xFF303030, ArtworkColorExtractor.select(new int[0]));
        assertEquals(0xFF303030, ArtworkColorExtractor.select(new int[]{0x00304050}));
    }

    @Test public void adjacentColorsInWinningBucketAreAveraged() {
        assertEquals(0xFFB24232, ArtworkColorExtractor.select(new int[]{0xFFB04030, 0xFFB44434}));
    }
}
