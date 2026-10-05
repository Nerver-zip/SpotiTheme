package com.spotitheme.hooks;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.spotitheme.ModuleLog;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Retains the landscape panel's native drawable and tint across palette changes. */
public final class LandscapeSidePanelHook {
    private final ThemeRuntime runtime;
    private final Map<View, Original> originals = new WeakHashMap<>();
    private boolean applying;
    private static final class Original {
        final Drawable background;
        final ColorStateList tint;
        Original(View view) {
            Drawable source = view.getBackground();
            if (source != null && source.getConstantState() == null)
                throw new IllegalArgumentException("Panel background cannot be copied");
            background = source == null ? null : source.getConstantState().newDrawable(view.getResources()).mutate();
            tint = view.getBackgroundTintList();
        }
    }

    public LandscapeSidePanelHook(ThemeRuntime runtime) { this.runtime = runtime; }

    public void install() {
        XposedHelpers.findAndHookMethod(View.class, "onAttachedToWindow", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (!"side_panel_background".equals(ViewScopes.resource(view))) return;
                if (!originals.containsKey(view)) {
                    try { originals.put(view, new Original(view)); }
                    catch (IllegalArgumentException failure) {
                        ModuleLog.error("Landscape panel restoration unavailable; surface unchanged", failure);
                        return;
                    }
                }
                refresh(view);
            }
        });
        runtime.addListener(() -> {
            for (View view : new ArrayList<>(originals.keySet()))
                if (view != null && view.isAttachedToWindow()) refresh(view);
        });
        ModuleLog.info("Landscape side-panel background hook installed");
    }

    private void refresh(View view) {
        if (applying) return;
        Original original = originals.get(view);
        if (original == null) return;
        ThemeRuntime.Snapshot state = runtime.snapshot();
        applying = true;
        try {
            boolean themed = state.enabled && (state.fixed || state.animated);
            view.setBackgroundTintList(themed ? null : original.tint);
            view.setBackground(themed ? new ColorDrawable(state.palette.color("background")) : original.background);
        } finally { applying = false; }
    }
}
