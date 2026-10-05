package com.spotitheme.hooks;

import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Expanded page and controls backdrop, leaving album media layers intact. */
public final class ExpandedBackgroundHook {
    private final ThemeRuntime runtime;
    private final Method overlayColor;
    private final Field overlayDrawable;
    private final Profile_9_1_86_2432 profile;
    private final Map<View, Drawable.ConstantState> gradients = new WeakHashMap<>();
    private final Map<View, Integer> overlays = new WeakHashMap<>();
    private final Map<View, Boolean> pages = new WeakHashMap<>();
    private boolean applying;

    public ExpandedBackgroundHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile)
            throws ReflectiveOperationException {
        this.runtime = runtime;
        this.profile = profile;
        overlayColor = (Method) profile.resolve("player.overlayColor");
        overlayDrawable = profile.field("player.overlayDrawable");
    }

    public void install() {
        try {
            XposedBridge.hookMethod(profile.resolve("player.overlay"), new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    View view = (View) param.thisObject;
                    Object drawable = overlayDrawable.get(view);
                    if (!(drawable instanceof GradientDrawable) || ((GradientDrawable) drawable).getConstantState() == null) return;
                    gradients.put(view, ((GradientDrawable) drawable).getConstantState().newDrawable(view.getResources()).mutate().getConstantState());
                    refreshGradient(view);
                }
            });
        } catch (ReflectiveOperationException failure) { ModuleLog.error("Player gradient constructor binding failed", failure); }
        XposedBridge.hookMethod(overlayColor, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (applying || !ViewScopes.resource(view).equals("overlay_controls_layout")) return;
                overlays.put(view, (Integer) param.args[0]);
                if (fixed()) param.args[0] = runtime.snapshot().palette.color("playerBackground");
            }
        });
        XposedHelpers.findAndHookMethod(View.class, "onAttachedToWindow", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (view.getClass().getName().equals("p.tek0") && ViewScopes.resource(view).equals("content")
                        && ViewScopes.expandedPlayer(view) && view.getBackground() instanceof ColorDrawable) {
                    pages.put(view, true);
                    refreshPage(view);
                }
            }
        });
        runtime.addListener(() -> {
            for (View view : new ArrayList<>(gradients.keySet())) if (view != null) refreshGradient(view);
            for (View page : new ArrayList<>(pages.keySet())) if (page != null) refreshPage(page);
            boolean previous = applying;
            applying = true;
            try {
                for (View view : new ArrayList<>(overlays.keySet())) {
                    if (view == null) continue;
                    try { overlayColor.invoke(view, fixed() ? runtime.snapshot().palette.color("playerBackground") : overlays.get(view)); }
                    catch (ReflectiveOperationException failure) { ModuleLog.error("Expanded overlay restore failed", failure); }
                }
            } finally { applying = previous; }
        });
        ModuleLog.info("Expanded player background hooks installed");
    }

    private void refreshGradient(View view) {
        Drawable.ConstantState original = gradients.get(view);
        if (original == null) return;
        GradientDrawable replacement = (GradientDrawable) original.newDrawable(view.getResources()).mutate();
        if (fixed()) {
            int background = runtime.snapshot().palette.color("background");
            if (Build.VERSION.SDK_INT >= 29) replacement.setColors(
                    new int[]{Color.TRANSPARENT, Color.TRANSPARENT, background}, new float[]{0f, .88f, 1f});
            else replacement.setColors(new int[]{Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, background});
        }
        try { overlayDrawable.set(view, replacement); view.invalidate(); }
        catch (IllegalAccessException failure) { ModuleLog.error("Player gradient restoration failed", failure); }
    }

    private void refreshPage(View view) {
        if (!(view.getBackground() instanceof ColorDrawable)) return;
        int color = fixed() ? runtime.snapshot().palette.color("background") : 0xFF121212;
        if (((ColorDrawable) view.getBackground()).getColor() != color) view.setBackground(new ColorDrawable(color));
    }
    private boolean fixed() { return runtime.snapshot().enabled && runtime.snapshot().fixed; }
}
