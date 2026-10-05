package com.spotitheme.hooks;

/** Nested renderer scopes that must retain Spotify's original palette. */
public final class NativePaletteScope {
    private static final ThreadLocal<Integer> depth = ThreadLocal.withInitial(() -> 0);
    private NativePaletteScope() {}
    public static boolean active() { return depth.get() != 0; }
    public static int enter() {
        int previous = depth.get();
        depth.set(previous + 1);
        return previous;
    }
    public static void restore(int previous) {
        if (previous == 0) depth.remove(); else depth.set(previous);
    }
}
