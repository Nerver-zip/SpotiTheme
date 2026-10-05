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

/** Header app-bar paint source, distinct from Compose's gradient and compact fill. */
public final class PlaylistAppBarHook {
    private final ThemeRuntime runtime;
    private final PlaylistHeaderScope header;
    private final Map<View, Original> originals = new WeakHashMap<>();
    private boolean applying;
    private static final class Original {
        Drawable background;
        ColorStateList tint;
        Original(View view) { background = view.getBackground(); tint = view.getBackgroundTintList(); }
    }
    public PlaylistAppBarHook(ThemeRuntime runtime, PlaylistHeaderScope header) { this.runtime = runtime; this.header = header; }

    public void install() {
        XposedHelpers.findAndHookMethod(View.class, "setBackground", Drawable.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (applying || !owner(view)) return;
                original(view).background = (Drawable) param.args[0];
                if (fixed()) param.args[0] = new ColorDrawable(color());
            }
        });
        XposedHelpers.findAndHookMethod(View.class, "setBackgroundColor", int.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (applying || !owner(view)) return;
                original(view).background = new ColorDrawable((Integer) param.args[0]);
                if (fixed()) param.args[0] = color();
                param.setObjectExtra("spotitheme.appbar.internal", Boolean.TRUE); applying = true;
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (Boolean.TRUE.equals(param.getObjectExtra("spotitheme.appbar.internal"))) applying = false;
            }
        });
        XposedHelpers.findAndHookMethod(View.class, "setBackgroundTintList", ColorStateList.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (applying || !owner(view)) return;
                original(view).tint = (ColorStateList) param.args[0];
                if (fixed()) param.args[0] = ColorStateList.valueOf(color());
            }
        });
        XposedHelpers.findAndHookMethod(View.class, "setBackgroundResource", int.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (applying || !owner((View) param.thisObject)) return;
                param.setObjectExtra("spotitheme.appbar.beforeResource", ((View) param.thisObject).getBackground());
                param.setObjectExtra("spotitheme.appbar.internal", Boolean.TRUE); applying = true;
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (!Boolean.TRUE.equals(param.getObjectExtra("spotitheme.appbar.internal"))) return;
                applying = false;
                if (param.hasThrowable()) return;
                View view = (View) param.thisObject;
                // View can skip reloading the same resource. Do not capture our
                // existing themed drawable as a new native restoration value.
                if (view.getBackground() != param.getObjectExtra("spotitheme.appbar.beforeResource"))
                    original(view).background = view.getBackground();
                refresh(view);
            }
        });
        XposedHelpers.findAndHookMethod(View.class, "onAttachedToWindow", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (owner(view)) { original(view); refresh(view); }
            }
        });
        runtime.addListener(() -> {
            for (View view : new ArrayList<>(originals.keySet())) if (view != null) refresh(view);
        });
        ModuleLog.info("Playlist app-bar background hooks installed");
    }

    private Original original(View view) {
        Original original = originals.get(view);
        if (original == null) { original = new Original(view); originals.put(view, original); }
        return original;
    }
    private void refresh(View view) {
        Original original = original(view);
        boolean previous = applying; applying = true;
        try {
            boolean themed = fixed() && owner(view);
            view.setBackground(themed ? new ColorDrawable(color()) : original.background);
            view.setBackgroundTintList(themed ? ColorStateList.valueOf(color()) : original.tint);
        } finally { applying = previous; }
    }
    private boolean owner(View view) { return ViewScopes.resource(view).equals("app_bar_layout") && header.contains(view); }
    private boolean fixed() { return runtime.snapshot().enabled && runtime.snapshot().fixed; }
    private int color() { return runtime.snapshot().palette.color("albumHeaderBackground"); }
}
