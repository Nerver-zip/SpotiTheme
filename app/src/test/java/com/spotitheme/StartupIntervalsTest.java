package com.spotitheme;

import org.junit.Test;
import static org.junit.Assert.*;

public class StartupIntervalsTest {
    @Test public void nestedResourceInitializationDoesNotDoubleCount() {
        StartupIntervals times = new StartupIntervals();
        assertEquals(10, times.record(10, 20));
        assertEquals(40, times.record(0, 40));
        assertEquals(44, times.record(45, 49));
    }

    @Test public void overlappingIntervalsMergeRegardlessOfCompletionOrder() {
        StartupIntervals times = new StartupIntervals();
        assertEquals(20, times.record(20, 40));
        assertEquals(30, times.record(10, 30));
        assertEquals(45, times.record(35, 55));
        assertThrows(IllegalArgumentException.class, () -> times.record(2, 1));
    }
}
