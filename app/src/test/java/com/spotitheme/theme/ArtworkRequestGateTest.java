package com.spotitheme.theme;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class ArtworkRequestGateTest {
    @Test public void latePreviousTrackCannotOverwriteNewTrack() {
        ArtworkRequestGate gate = new ArtworkRequestGate();
        ArtworkRequestGate.Request old = gate.begin("first");
        ArtworkRequestGate.Request next = gate.begin("second");
        List<String> published = new ArrayList<>();
        assertTrue(gate.publish(next, () -> published.add("second")));
        assertFalse(gate.publish(old, () -> published.add("first")));
        assertEquals(java.util.Collections.singletonList("second"), published);
    }

    @Test public void fallbackOnlyTrackCancelsDownload() {
        ArtworkRequestGate gate = new ArtworkRequestGate();
        ArtworkRequestGate.Request download = gate.begin("image");
        ArtworkRequestGate.Request fallback = gate.begin(null);
        assertFalse(gate.isCurrent(download));
        assertTrue(gate.publish(fallback, () -> {}));
    }

    @Test public void disabledOrChangedThemeRejectsQueuedResult() {
        ArtworkRequestGate gate = new ArtworkRequestGate();
        ArtworkRequestGate.Request queued = gate.begin("image");
        gate.invalidate();
        assertFalse(gate.publish(queued, () -> fail("Stale result must not run")));
        ArtworkRequestGate.Request replay = gate.begin("image");
        assertFalse(gate.isCurrent(queued));
        assertTrue(gate.isCurrent(replay));
    }

    @Test public void sameArtworkRefreshSupersedesOldResult() {
        ArtworkRequestGate gate = new ArtworkRequestGate();
        ArtworkRequestGate.Request older = gate.begin("image");
        ArtworkRequestGate.Request refreshed = gate.begin("image");
        assertFalse(gate.isCurrent(older));
        assertTrue(gate.isCurrent(refreshed));
    }
}
