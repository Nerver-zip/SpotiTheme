package com.spotitheme.theme;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/** Process-local palette state shared by the theme feature hooks. */
public final class ThemeRuntime {
    public static final String PREFERENCES = "SpotiTheme";
    public static final class Snapshot {
        public final ThemePalette palette;
        public final boolean enabled;
        public final boolean fixed;
        public final long generation;
        private Snapshot(ThemePalette palette, boolean enabled, boolean fixed, long generation) {
            this.palette = palette;
            this.enabled = enabled;
            this.fixed = fixed;
            this.generation = generation;
        }
    }

    private final Handler main = new Handler(Looper.getMainLooper());
    private final CopyOnWriteArrayList<Runnable> listeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Runnable> compositionObservers = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Predicate<Object>> nativePaletteConditions = new CopyOnWriteArrayList<>();
    private volatile Snapshot snapshot;

    public ThemeRuntime(ThemePalette palette, boolean enabled, boolean fixed) {
        if (palette == null) throw new IllegalArgumentException("A validated palette is required");
        snapshot = new Snapshot(palette, enabled, fixed, 0);
    }

    public Snapshot snapshot() { return snapshot; }
    public void addListener(Runnable listener) { listeners.add(listener); }
    public void addCompositionObserver(Runnable observer) { compositionObservers.add(observer); }
    public void observeComposition() { for (Runnable observer : compositionObservers) observer.run(); }
    public void addNativePaletteCondition(Predicate<Object> condition) { nativePaletteConditions.add(condition); }
    public boolean keepNativePalette(Object composer) {
        if (composer == null) return false;
        for (Predicate<Object> condition : nativePaletteConditions) if (condition.test(composer)) return true;
        return false;
    }

    public void apply(ThemePalette palette, boolean enabled, boolean fixed) {
        if (palette == null) throw new IllegalArgumentException("A validated palette is required");
        if (Looper.myLooper() != Looper.getMainLooper()) {
            main.post(() -> apply(palette, enabled, fixed));
            return;
        }
        snapshot = new Snapshot(palette, enabled, fixed, snapshot.generation + 1);
        for (Runnable listener : listeners) listener.run();
    }
}
