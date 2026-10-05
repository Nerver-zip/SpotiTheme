package com.spotitheme.hooks;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import com.spotitheme.ModuleLog;
import com.spotitheme.theme.ThemeRuntime;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Rebase activity themes and redraw after all palette owners have refreshed. */
public final class ActivityPaletteHook implements Application.ActivityLifecycleCallbacks {
    private final ThemeRuntime runtime;
    private final Map<Activity, Long> generations = new WeakHashMap<>();
    public ActivityPaletteHook(ThemeRuntime runtime) { this.runtime = runtime; }

    public void install(Application application) {
        application.registerActivityLifecycleCallbacks(this);
        runtime.addListener(() -> {
            for (Activity activity : new ArrayList<>(generations.keySet()))
                if (activity != null) refresh(activity);
        });
        ModuleLog.info("Activity palette rebasing and redraw registered");
    }

    private void refresh(Activity activity) {
        if (activity.isDestroyed() || activity.isFinishing()) return;
        long generation = runtime.snapshot().generation;
        Long previous = generations.get(activity);
        if (previous != null && previous == generation) return;
        if (Build.VERSION.SDK_INT >= 29) activity.getTheme().rebase();
        activity.getWindow().getDecorView().invalidate();
        generations.put(activity, generation);
    }

    @Override public void onActivityCreated(Activity activity, Bundle state) { refresh(activity); }
    @Override public void onActivityResumed(Activity activity) { refresh(activity); }
    @Override public void onActivityDestroyed(Activity activity) { generations.remove(activity); }
    @Override public void onActivityStarted(Activity activity) {}
    @Override public void onActivityPaused(Activity activity) {}
    @Override public void onActivityStopped(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) {}
}
