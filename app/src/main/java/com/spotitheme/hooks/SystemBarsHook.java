package com.spotitheme.hooks;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsetsController;
import com.spotitheme.theme.ThemeRuntime;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Theme-aware system icon contrast without changing Android rotation settings. */
public final class SystemBarsHook implements Application.ActivityLifecycleCallbacks {
    private final ThemeRuntime runtime;
    private final Map<Activity, int[]> originals = new WeakHashMap<>();
    private final Map<Activity, Boolean> resumed = new WeakHashMap<>();

    public SystemBarsHook(ThemeRuntime runtime) { this.runtime = runtime; }

    public void install(Application application) {
        application.registerActivityLifecycleCallbacks(this);
        runtime.addListener(() -> {
            for (Activity activity : new ArrayList<>(resumed.keySet())) if (activity != null) refresh(activity);
        });
    }

    private void refresh(Activity activity) {
        if (Build.VERSION.SDK_INT < 30) return;
        String name = activity.getClass().getName();
        boolean player = name.equals("com.spotify.nowplaying.musicinstallation.NowPlayingActivity");
        boolean main = name.equals("com.spotify.music.SpotifyMainActivity") || name.equals("com.spotify.music.MainActivity");
        if (!player && !main) return;
        activity.getWindow().getDecorView().post(() -> {
            if (activity.isFinishing() || activity.isDestroyed()) return;
            WindowInsetsController controller = activity.getWindow().getInsetsController();
            if (controller == null) return;
            int mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
            int[] original = originals.get(activity);
            ThemeRuntime.Snapshot state = runtime.snapshot();
            if (state.enabled && (main || state.fixed)) {
                if (original == null) originals.put(activity, new int[] {controller.getSystemBarsAppearance() & mask,
                        activity.getWindow().isStatusBarContrastEnforced() ? 1 : 0});
                int background = state.palette.color(player ? "surface" : "background");
                activity.getWindow().setStatusBarContrastEnforced(false);
                controller.setSystemBarsAppearance(Color.luminance(background) > .5f ? mask : 0, mask);
            } else if (original != null) {
                activity.getWindow().setStatusBarContrastEnforced(original[1] != 0);
                controller.setSystemBarsAppearance(original[0], mask);
                originals.remove(activity);
            }
        });
    }

    @Override public void onActivityResumed(Activity activity) { resumed.put(activity, true); refresh(activity); }
    @Override public void onActivityPaused(Activity activity) { resumed.remove(activity); }
    @Override public void onActivityDestroyed(Activity activity) { resumed.remove(activity); originals.remove(activity); }
    @Override public void onActivityCreated(Activity activity, Bundle state) {}
    @Override public void onActivityStarted(Activity activity) {}
    @Override public void onActivityStopped(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) {}
}
