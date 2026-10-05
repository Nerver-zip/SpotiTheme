package com.spotitheme.hooks;

import android.graphics.BlendModeColorFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.view.View;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.StatusIconColors;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/** Recolors active Connect bitmap filters inside the mini-player host only. */
public final class ConnectBitmapHook {
    private static final String PREVIOUS = "spotitheme.connect.previous";
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final ThreadLocal<View> drawHost = new ThreadLocal<>();
    private final Set<Class<?>> canvases = new HashSet<>();
    private final Set<Method> methods = new HashSet<>();
    private boolean observed;

    public ConnectBitmapHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) {
        this.runtime = runtime;
        this.profile = profile;
    }

    public void install() throws ReflectiveOperationException {
        if (Build.VERSION.SDK_INT < 29) return;
        Method localRead = (Method) profile.resolve("compose.localRead");
        Object viewKey = profile.field("compose.viewLocal").get(null);
        runtime.addNativePaletteCondition(composer -> {
            if (!runtime.snapshot().enabled || runtime.snapshot().fixed) return false;
            try {
                Object local = localRead.invoke(composer, viewKey);
                return local instanceof View && ViewScopes.connectHost((View) local) != null;
            } catch (ReflectiveOperationException ignored) { return false; }
        });
        XposedBridge.hookMethod(profile.resolve("compose.dispatchDraw"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                param.setObjectExtra(PREVIOUS, drawHost.get());
                View host = ViewScopes.connectHost((View) param.thisObject);
                // Clear nested unrelated hosts, then restore the outer scope after drawing.
                if (host == null) drawHost.remove();
                else {
                    drawHost.set(host);
                    installCanvas((Canvas) param.args[0]);
                }
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                View previous = (View) param.getObjectExtra(PREVIOUS);
                if (previous == null) drawHost.remove(); else drawHost.set(previous);
            }
        });
        ModuleLog.info("Mini-player Connect bitmap hook installed");
    }

    private synchronized void installCanvas(Canvas canvas) {
        if (!canvases.add(canvas.getClass())) return;
        // Only Android framework Canvas overrides are enumerated. Spotify bindings
        // remain exact profile identities; this is not application discovery.
        for (Class<?> type = canvas.getClass(); type != null && Canvas.class.isAssignableFrom(type);
                type = type.getSuperclass()) {
            if (!type.getName().startsWith("android.graphics.")) continue;
            for (Method method : type.getDeclaredMethods()) {
                if (!method.getName().equals("drawBitmap") || methods.contains(method)) continue;
                Class<?>[] parameters = method.getParameterTypes();
                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i] != Paint.class) continue;
                    final int paintIndex = i;
                    try {
                        XposedBridge.hookMethod(method, new XC_MethodHook() {
                            @Override protected void beforeHookedMethod(MethodHookParam param) {
                                tint(param, paintIndex);
                            }
                        });
                        methods.add(method);
                    } catch (Throwable failure) { ModuleLog.error("Connect Canvas hook failed", failure); }
                    break;
                }
            }
        }
    }

    private void tint(XC_MethodHook.MethodHookParam param, int index) {
        ThemeRuntime.Snapshot state = runtime.snapshot();
        if (!state.enabled || drawHost.get() == null || !(param.args[index] instanceof Paint)) return;
        Paint original = (Paint) param.args[index];
        if (!(original.getColorFilter() instanceof BlendModeColorFilter)) return;
        BlendModeColorFilter filter = (BlendModeColorFilter) original.getColorFilter();
        int color = StatusIconColors.themed(filter.getColor(), state.palette.color("accent"));
        if (color == filter.getColor()) return;
        Paint replacement = new Paint(original);
        replacement.setColorFilter(new BlendModeColorFilter(color, filter.getMode()));
        param.args[index] = replacement;
        if (!observed) {
            observed = true;
            ModuleLog.info("Mini-player Connect bitmap tint observed");
        }
    }
}
