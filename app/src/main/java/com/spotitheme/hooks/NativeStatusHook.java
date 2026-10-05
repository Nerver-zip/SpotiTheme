package com.spotitheme.hooks;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.view.View;
import android.widget.ImageView;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.StatusIconColors;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/** Exact native status greens, preserving selector states and source setter values. */
public final class NativeStatusHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final Map<Object, Map<Method, Object[]>> originals = new WeakHashMap<>();
    private boolean replaying;
    private boolean observed;

    public NativeStatusHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) {
        this.runtime = runtime;
        this.profile = profile;
    }

    public void install() throws ReflectiveOperationException {
        hook(ImageView.class.getDeclaredMethod("setImageTintList", ColorStateList.class));
        hook(ImageView.class.getDeclaredMethod("setColorFilter", int.class));
        hook(ImageView.class.getDeclaredMethod("setColorFilter", int.class, PorterDuff.Mode.class));
        hook(VectorDrawable.class.getDeclaredMethod("setTintList", ColorStateList.class));
        hook((Method) profile.resolve("glyph.color"));
        hook((Method) profile.resolve("glyph.colorState"));
        runtime.addListener(this::refresh);
        ModuleLog.info("Native status color setter hooks installed");
    }

    private void hook(Method method) {
        XposedBridge.hookMethod(method, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (replaying) return;
                Object original = param.args[0];
                Object themed = recolor(original, savedOwner(param.thisObject));
                Map<Method, Object[]> bindings = originals.get(param.thisObject);
                if (themed == original) {
                    if (bindings != null) { bindings.remove(method); if (bindings.isEmpty()) originals.remove(param.thisObject); }
                    return;
                }
                if (bindings == null) { bindings = new LinkedHashMap<>(); originals.put(param.thisObject, bindings); }
                bindings.put(method, param.args.clone());
                param.args[0] = themed;
                if (!observed) { observed = true; ModuleLog.info("Native status setter tint observed"); }
            }
        });
    }

    private Object recolor(Object source, boolean saved) {
        if (!runtime.snapshot().enabled) return source;
        int role = runtime.snapshot().palette.color(saved ? "savedIndicator" : "accent");
        if (source instanceof Integer) {
            int value = (Integer) source;
            int result = saved ? StatusIconColors.themedSavedIndicator(value, role) : StatusIconColors.themed(value, role);
            return result == value ? source : result;
        }
        if (!(source instanceof ColorStateList)) return source;
        ColorStateList list = (ColorStateList) source;
        int[] colors = ((int[]) XposedHelpers.getObjectField(list, "mColors")).clone();
        boolean changed = false;
        for (int i = 0; i < colors.length; i++) {
            int original = colors[i];
            colors[i] = saved ? StatusIconColors.themedSavedIndicator(original, role) : StatusIconColors.themed(original, role);
            changed |= colors[i] != original;
        }
        return changed ? new ColorStateList((int[][]) XposedHelpers.getObjectField(list, "mStateSpecs"), colors) : source;
    }

    private boolean savedOwner(Object object) {
        if (object instanceof Drawable) {
            Drawable.Callback callback = ((Drawable) object).getCallback();
            // Nested drawable callbacks are bounded to avoid malformed callback cycles.
            for (int i = 0; callback instanceof Drawable && i < 8; i++) callback = ((Drawable) callback).getCallback();
            object = callback;
        }
        if (!(object instanceof View)) return false;
        View view = (View) object;
        String name = ViewScopes.resource(view);
        return (ViewScopes.miniAncestor(view) || ViewScopes.expandedPlayer(view))
                && (name.equals("add_to_button") || name.equals("animated_heart_button"));
    }

    private void refresh() {
        boolean previous = replaying;
        replaying = true;
        try {
            for (Object object : new ArrayList<>(originals.keySet())) {
                if (object == null) continue;
                Map<Method, Object[]> bindings = originals.get(object);
                if (bindings == null) continue;
                for (Map.Entry<Method, Object[]> entry : bindings.entrySet()) {
                    Object[] args = entry.getValue().clone();
                    args[0] = recolor(args[0], savedOwner(object));
                    try { entry.getKey().invoke(object, args); }
                    catch (ReflectiveOperationException failure) { ModuleLog.error("Native status restore failed", failure); }
                }
            }
        } finally { replaying = previous; }
    }
}
