package com.spotitheme.hooks;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Owns only the two Lottie saved-check fill paths, never the entire animation. */
public final class SavedIndicatorHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final Constructor<?> callbackConstructor;
    private final Method setter, materialIcon;
    private final Field imageState, encoreState, addedState;
    private final Object property;
    private final Object[] paths;
    private final Map<Object, PorterDuffColorFilter> filters = new WeakHashMap<>();
    private final Map<Drawable, Binding> bindings = new WeakHashMap<>();
    private final Map<View, WeakReference<Drawable>> icons = new WeakHashMap<>();
    private boolean observed;

    private static final class Binding {
        final int color;
        final List<Object> callbacks;
        final boolean complete;
        Binding(int color, List<Object> callbacks, boolean complete) {
            this.color = color; this.callbacks = callbacks; this.complete = complete;
        }
    }

    public SavedIndicatorHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile)
            throws ReflectiveOperationException {
        this.runtime = runtime;
        this.profile = profile;
        callbackConstructor = (Constructor<?>) profile.resolve("lottie.callbackConstructor");
        Constructor<?> path = (Constructor<?>) profile.resolve("lottie.keyPathConstructor");
        paths = new Object[] {
            path.newInstance((Object) new String[] {"Tick", "Subtract", "check-color"}),
            path.newInstance((Object) new String[] {"Tick", "Subtract", "Fill 1"})
        };
        setter = (Method) profile.resolve("lottie.callback");
        materialIcon = (Method) profile.resolve("saved.materialIcon");
        imageState = profile.field("saved.viewState");
        encoreState = profile.field("saved.encoreState");
        addedState = profile.field("saved.addedState");
        property = profile.field("lottie.colorFilterProperty").get(null);
    }

    public void install() throws ReflectiveOperationException {
        XposedBridge.hookMethod(profile.resolve("lottie.value"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                PorterDuffColorFilter filter = filters.get(param.thisObject);
                if (filter != null) param.setResult(filter);
            }
        });
        XC_MethodHook update = new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (!param.hasThrowable()) refresh((View) param.thisObject);
            }
        };
        XposedBridge.hookMethod(profile.resolve("saved.viewState"), update);
        XposedBridge.hookMethod(profile.resolve("saved.encoreState"), update);
        XposedHelpers.findAndHookMethod(View.class, "onAttachedToWindow", update);
        XposedHelpers.findAndHookMethod(ImageView.class, "setImageDrawable", Drawable.class, update);
        runtime.addListener(() -> {
            for (View view : new ArrayList<>(icons.keySet())) if (view != null) refresh(view);
        });
        ModuleLog.info("Mini-player saved-check hook installed");
    }

    private void refresh(View view) {
        boolean scoped = ViewScopes.miniAncestor(view)
                && (ViewScopes.resource(view).equals("add_to_button")
                    || ViewScopes.resource(view).equals("animated_heart_button"));
        scoped |= encoreState.getDeclaringClass().isInstance(view);
        if (!scoped && !icons.containsKey(view)) return;
        try {
            Drawable icon = view instanceof ImageView ? ((ImageView) view).getDrawable()
                    : encoreState.getDeclaringClass().isInstance(view) ? (Drawable) materialIcon.invoke(view) : null;
            WeakReference<Drawable> previous = icons.get(view);
            Drawable old = previous == null ? null : previous.get();
            if (old != null && old != icon) clear(old);
            if (icon == null) { icons.remove(view); return; }
            icons.put(view, new WeakReference<>(icon));
            Field stateField = imageState.getDeclaringClass().isInstance(view) ? imageState
                    : encoreState.getDeclaringClass().isInstance(view) ? encoreState : null;
            Object state = stateField == null ? null : stateField.get(view);
            Object added = state == null ? null : addedState.get(state);
            if (!scoped || !runtime.snapshot().enabled || !(added instanceof Enum)
                    || !((Enum<?>) added).name().equals("ADDED")
                    || !setter.getDeclaringClass().isInstance(icon)) { clear(icon); return; }
            apply(icon, runtime.snapshot().palette.color("savedIndicator"));
        } catch (ReflectiveOperationException failure) { ModuleLog.error("Saved-check update failed", failure); }
    }

    private void apply(Drawable icon, int color) throws ReflectiveOperationException {
        Binding old = bindings.get(icon);
        if (old != null && old.complete && old.color == color) return;
        clear(icon);
        List<Object> callbacks = new ArrayList<>();
        try {
            for (Object path : paths) {
                Object callback = callbackConstructor.newInstance(15);
                filters.put(callback, new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
                callbacks.add(callback);
                setter.invoke(icon, path, property, callback);
            }
            bindings.put(icon, new Binding(color, callbacks, true));
        } catch (ReflectiveOperationException failure) {
            // Retain ownership until every successfully attached path can be cleared.
            bindings.put(icon, new Binding(color, callbacks, false));
            clear(icon);
            throw failure;
        }
        if (!observed) { observed = true; ModuleLog.info("Mini-player saved-check Lottie tint observed"); }
    }

    private void clear(Drawable icon) throws ReflectiveOperationException {
        Binding old = bindings.get(icon);
        if (old == null) return;
        // Remove bookkeeping only after successful detachment so failures remain retryable.
        for (int i = 0; i < old.callbacks.size(); i++) setter.invoke(icon, paths[i], property, null);
        bindings.remove(icon);
        for (Object callback : old.callbacks) filters.remove(callback);
    }
}
