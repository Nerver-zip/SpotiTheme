package com.spotitheme.hooks;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.lang.reflect.Method;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Fixed mini-player surfaces with original backgrounds retained for native/OFF. */
public final class MiniBackgroundHook {
    private final ThemeRuntime runtime;
    private final Method composeSet, composeGet;
    private final Map<View, Original> originals = new WeakHashMap<>();
    private final Map<Drawable, WeakReference<View>> owners = new WeakHashMap<>();
    private boolean applying;
    private boolean observed;

    private static final class Original {
        Drawable drawable;
        ColorStateList tint;
        Long compose;
        Original(View view) { drawable = view.getBackground(); tint = view.getBackgroundTintList(); }
    }

    public MiniBackgroundHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile)
            throws ReflectiveOperationException {
        this.runtime = runtime;
        composeSet = (Method) profile.resolve("mini.backgroundSet");
        composeGet = (Method) profile.resolve("mini.backgroundGet");
    }

    public void install() {
        XposedBridge.hookMethod(composeSet, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (applying || !ViewScopes.miniBar(view)) return;
                Original original = original(view);
                original.compose = (Long) param.args[0];
                if (fixed()) param.args[0] = packed();
                param.setObjectExtra("spotitheme.background.internal", Boolean.TRUE);
                applying = true;
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (Boolean.TRUE.equals(param.getObjectExtra("spotitheme.background.internal"))) applying = false;
            }
        });
        XposedHelpers.findAndHookMethod(View.class, "setBackground", Drawable.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (applying || !ViewScopes.miniBar(view)) return;
                original(view).drawable = (Drawable) param.args[0];
                if (fixed()) param.args[0] = replacement(view, (Drawable) param.args[0]);
            }
        });
        XposedHelpers.findAndHookMethod(View.class, "setBackgroundColor", int.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (applying || !ViewScopes.miniBar(view)) return;
                original(view).drawable = new ColorDrawable((Integer) param.args[0]);
                if (fixed()) param.args[0] = color();
                param.setObjectExtra("spotitheme.background.internal", Boolean.TRUE);
                applying = true;
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (Boolean.TRUE.equals(param.getObjectExtra("spotitheme.background.internal"))) applying = false;
            }
        });
        XposedHelpers.findAndHookMethod(View.class, "setBackgroundTintList", ColorStateList.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (applying || !ViewScopes.miniBar(view)) return;
                original(view).tint = (ColorStateList) param.args[0];
                if (fixed()) param.args[0] = ColorStateList.valueOf(color());
            }
        });
        XposedHelpers.findAndHookMethod(View.class, "onAttachedToWindow", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (ViewScopes.miniBar(view)) { original(view); view.post(() -> refresh(view)); }
            }
        });
        runtime.addListener(() -> {
            for (View view : new ArrayList<>(originals.keySet())) if (view != null) refresh(view);
        });
        for (Class<?> type : new Class<?>[] {Drawable.class, ColorDrawable.class, GradientDrawable.class}) {
            hookTint(type, "setTint", int.class);
            hookTint(type, "setTintList", ColorStateList.class);
        }
        ModuleLog.info("Mini-player background hooks installed");
    }

    private void hookTint(Class<?> type, String name, Class<?> argument) {
        final Method method;
        try { method = type.getDeclaredMethod(name, argument); }
        catch (NoSuchMethodException absentOverride) { return; }
        XposedBridge.hookMethod(method, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (applying || !fixed()) return;
                WeakReference<View> reference = owners.get((Drawable) param.thisObject);
                View view = reference == null ? null : reference.get();
                if (view == null || view.getBackground() != param.thisObject || !ViewScopes.miniBar(view)) return;
                Original original = originals.get(view);
                if (original == null) return;
                // Replay Spotify's native mutation on the retained original, so
                // disabling fixed mode restores the latest artwork tint.
                boolean previous = applying;
                applying = true;
                try {
                    if (original.drawable != null && type.isInstance(original.drawable))
                        method.invoke(original.drawable, param.args[0]);
                } catch (ReflectiveOperationException failure) {
                    ModuleLog.error("Mini background native tint replay failed", failure);
                } finally { applying = previous; }
                param.args[0] = argument == int.class ? (Object) color() : ColorStateList.valueOf(color());
                param.setObjectExtra("spotitheme.background.tint", Boolean.TRUE);
                applying = true;
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (Boolean.TRUE.equals(param.getObjectExtra("spotitheme.background.tint"))) applying = false;
            }
        });
    }

    private Original original(View view) {
        Original value = originals.get(view);
        if (value != null) return value;
        value = new Original(view);
        if (composeGet.getDeclaringClass().isInstance(view)) {
            try { value.compose = (Long) composeGet.invoke(view); }
            catch (ReflectiveOperationException failure) { ModuleLog.error("Mini background read failed", failure); }
        }
        originals.put(view, value);
        return value;
    }

    private Drawable replacement(View view, Drawable source) {
        Drawable.ConstantState state = source == null ? null : source.getConstantState();
        Drawable copy = state == null ? null : state.newDrawable(view.getResources()).mutate();
        if (copy instanceof GradientDrawable) ((GradientDrawable) copy).setColor(color());
        else copy = new ColorDrawable(color());
        copy.setTintList(ColorStateList.valueOf(color()));
        owners.put(copy, new WeakReference<>(view));
        return copy;
    }

    private void refresh(View view) {
        Original original = original(view);
        boolean previous = applying;
        applying = true;
        try {
            if (original.compose != null) composeSet.invoke(view, fixed() ? packed() : original.compose);
            else {
                view.setBackground(fixed() ? replacement(view, original.drawable) : original.drawable);
                view.setBackgroundTintList(fixed() ? ColorStateList.valueOf(color()) : original.tint);
            }
            if (fixed() && !observed) {
                observed = true;
                ModuleLog.info("Mini-player fixed background applied");
            }
        } catch (ReflectiveOperationException failure) { ModuleLog.error("Mini background refresh failed", failure); }
        finally { applying = previous; }
    }

    private boolean fixed() { return runtime.snapshot().enabled && runtime.snapshot().fixed; }
    private int color() { return runtime.snapshot().palette.color("playerBackground"); }
    private long packed() { return ((long) color()) << 32; }
}
