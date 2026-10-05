package com.spotitheme;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Union of registration intervals, so nested phases are counted only once. */
final class StartupIntervals {
    private final List<long[]> intervals = new ArrayList<>();

    synchronized long record(long start, long end) {
        if (end < start) throw new IllegalArgumentException("Registration time moved backwards");
        intervals.add(new long[]{start, end});
        intervals.sort(Comparator.comparingLong(interval -> interval[0]));
        long total = 0, from = intervals.get(0)[0], to = intervals.get(0)[1];
        for (int i = 1; i < intervals.size(); i++) {
            long[] interval = intervals.get(i);
            if (interval[0] > to) {
                total += to - from;
                from = interval[0];
            }
            to = Math.max(to, interval[1]);
        }
        return total + to - from;
    }
}
