package com.spotitheme.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.spotitheme.hooks.PlayerMediaPolicy;
import com.spotitheme.hooks.PlayerMediaPolicy.Mode;
import org.junit.Test;

public final class PlayerMediaPolicyTest {
    @Test public void requiresConfirmedSquareCoverAndDecodedArtwork() {
        assertEquals(Mode.STILL_ARTWORK, PlayerMediaPolicy.classify(true, true, false, false, false));
        assertEquals(Mode.UNKNOWN, PlayerMediaPolicy.classify(true, false, false, false, false));
        assertEquals(Mode.UNKNOWN, PlayerMediaPolicy.classify(false, true, false, false, false));
    }

    @Test public void videoPosterAndUnreadySurfaceNeverBecomeStillArtwork() {
        assertEquals(Mode.UNKNOWN, PlayerMediaPolicy.classify(true, true, true, false, true));
        assertEquals(Mode.UNKNOWN, PlayerMediaPolicy.classify(true, true, true, true, false));
        assertEquals(Mode.FEATURED_VIDEO, PlayerMediaPolicy.classify(true, true, true, true, true));
    }

    @Test public void onlyEnabledStillArtworkSuppressesTheBackdrop() {
        assertTrue(PlayerMediaPolicy.suppressBackdrop(true, Mode.STILL_ARTWORK));
        assertFalse(PlayerMediaPolicy.suppressBackdrop(false, Mode.STILL_ARTWORK));
        assertFalse(PlayerMediaPolicy.suppressBackdrop(true, Mode.FEATURED_VIDEO));
        assertFalse(PlayerMediaPolicy.suppressBackdrop(true, Mode.UNKNOWN));
    }
}
