package com.spotitheme.hooks;

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

/** Main activity's solid page surface, including configuration relaunches. */
public final class MainContentBackgroundHook {
    private final ThemeRuntime runtime;
    private final Map<View, Drawable> originals = new WeakHashMap<>();
    private boolean applying;

    public MainContentBackgroundHook(ThemeRuntime runtime) { this.runtime = runtime; }

    public void install() {
        XposedHelpers.findAndHookMethod(View.class, "setBackground", Drawable.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (applying || !owner(view)) return;
                // Track replacement media drawables too, so later palette updates
                // never reapply a stale solid surface over a new native layer.
                originals.put(view, (Drawable) param.args[0]);
                if (runtime.snapshot().enabled && param.args[0] instanceof ColorDrawable)
                    param.args[0] = new ColorDrawable(runtime.snapshot().palette.color("background"));
            }
        });
        XposedHelpers.findAndHookMethod(View.class, "onAttachedToWindow", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (!owner(view)) return;
                if (!originals.containsKey(view)) originals.put(view, view.getBackground());
                refresh(view);
            }
        });
        runtime.addListener(() -> {
            for (View view : new ArrayList<>(originals.keySet())) if (view != null) refresh(view);
        });
        ModuleLog.info("Main content solid background hooks installed");
    }

    private boolean owner(View view) {
        return "p.wbo0".equals(view.getClass().getName()) && "main_content".equals(ViewScopes.resource(view));
    }

    private void refresh(View view) {
        Drawable original = originals.get(view);
        if (!(original instanceof ColorDrawable)) return;
        boolean previous = applying; applying = true;
        try {
            view.setBackground(runtime.snapshot().enabled
                    ? new ColorDrawable(runtime.snapshot().palette.color("background")) : original);
        } finally { applying = previous; }
    }
}
