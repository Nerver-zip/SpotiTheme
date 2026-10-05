package com.spotitheme.hooks;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Navigation's owned fade remains consistent when Spotify rebuilds it on scroll. */
public final class NavigationGradientHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final Map<View, Original> originals = new WeakHashMap<>();
    private boolean applying;
    private static final class Original {
        final Drawable background;
        Integer bottom;
        boolean hasBottom;
        Original(View view) { background = view.getBackground(); }
    }
    public NavigationGradientHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) {
        this.runtime = runtime; this.profile = profile;
    }
    public void install() throws ReflectiveOperationException {
        Method bottom = (Method) profile.resolve("navigation.bottomColor");
        XposedBridge.hookMethod(profile.resolve("navigation.gradient"), new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                originals.put(view, new Original(view));
                refresh(view, bottom);
            }
        });
        XposedBridge.hookMethod(bottom, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (applying) return;
                View view = (View) param.thisObject;
                Original original = originals.get(view);
                if (original == null) return;
                original.bottom = (Integer) param.args[0]; original.hasBottom = true;
                if (fixed()) param.args[0] = bottomColor();
            }
        });
        runtime.addListener(() -> {
            for (View view : new ArrayList<>(originals.keySet())) if (view != null) refresh(view, bottom);
        });
        ModuleLog.info("Navigation gradient hooks installed");
    }
    private void refresh(View view, Method setter) {
        Original original = originals.get(view);
        boolean previous = applying; applying = true;
        try {
            view.setBackground(fixed() ? new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{Color.TRANSPARENT, bottomColor()}) : original.background);
            if (original.hasBottom) setter.invoke(view, fixed()
                    ? Integer.valueOf(bottomColor()) : original.bottom);
        } catch (ReflectiveOperationException failure) { ModuleLog.error("Navigation gradient restoration failed", failure); }
        finally { applying = previous; }
    }
    private int bottomColor() {
        return runtime.snapshot().animated ? 0xEE0A0E16
                : runtime.snapshot().palette.color("background");
    }
    private boolean fixed() { return runtime.snapshot().enabled
            && (runtime.snapshot().fixed || runtime.snapshot().animated); }
}
