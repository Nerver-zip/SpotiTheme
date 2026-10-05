package com.spotitheme.hooks;

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import com.spotitheme.ModuleLog;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Both mini-player progress layouts, preserving native selector opacity. */
public final class MiniProgressHook {
    private final ThemeRuntime runtime;
    private final Map<ProgressBar, ColorStateList[]> originals = new WeakHashMap<>();
    private boolean observed;

    public MiniProgressHook(ThemeRuntime runtime) { this.runtime = runtime; }

    public void install() {
        XposedHelpers.findAndHookMethod(View.class, "onAttachedToWindow", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (!(param.thisObject instanceof ProgressBar)) return;
                ProgressBar bar = (ProgressBar) param.thisObject;
                String name = ViewScopes.resource(bar);
                if (!ViewScopes.miniAncestor(bar) || !(bar instanceof SeekBar ? name.equals("seek_bar") : name.equals("progress_bar"))) return;
                originals.putIfAbsent(bar, new ColorStateList[] {bar.getProgressTintList(),
                    bar.getSecondaryProgressTintList(), bar.getProgressBackgroundTintList(),
                    bar instanceof SeekBar ? ((SeekBar) bar).getThumbTintList() : null});
                refresh(bar);
            }
        });
        runtime.addListener(() -> {
            for (ProgressBar bar : new ArrayList<>(originals.keySet())) if (bar != null) refresh(bar);
        });
        ModuleLog.info("Mini-player progress hooks installed");
    }

    private void refresh(ProgressBar bar) {
        ColorStateList[] original = originals.get(bar);
        if (original == null) return;
        ThemeRuntime.Snapshot state = runtime.snapshot();
        boolean fixed = state.enabled && state.fixed;
        int foreground = state.palette.color("text");
        try {
            bar.setProgressTintList(fixed ? recolor(original[0], foreground) : original[0]);
            bar.setSecondaryProgressTintList(fixed ? recolor(original[1], foreground) : original[1]);
            bar.setProgressBackgroundTintList(fixed ? recolor(original[2], foreground) : original[2]);
            if (bar instanceof SeekBar) ((SeekBar) bar).setThumbTintList(fixed ? recolor(original[3], foreground) : original[3]);
            if (fixed && !observed) { observed = true; ModuleLog.info("Mini-player progress tint applied"); }
        } catch (Throwable failure) { ModuleLog.error("Mini progress update failed", failure); }
    }

    private static ColorStateList recolor(ColorStateList original, int role) {
        if (original == null) return ColorStateList.valueOf(role);
        int[] colors = ((int[]) XposedHelpers.getObjectField(original, "mColors")).clone();
        for (int i = 0; i < colors.length; i++) colors[i] = (role & 0xFFFFFF)
                | (((role >>> 24) * (colors[i] >>> 24) / 255) << 24);
        return new ColorStateList((int[][]) XposedHelpers.getObjectField(original, "mStateSpecs"), colors);
    }
}
