package com.spotitheme.hooks;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import android.view.ViewParent;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.StatusIconColors;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/** Album action's Compose path paint, scoped independently from player checks. */
public final class AlbumSavedIndicatorHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final ThreadLocal<View> host = new ThreadLocal<>();
    private final Set<Method> installed = new HashSet<>();
    public AlbumSavedIndicatorHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) {
        this.runtime = runtime; this.profile = profile;
    }
    public void install() throws ReflectiveOperationException {
        XposedBridge.hookMethod(profile.resolve("compose.dispatchDraw"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                param.setObjectExtra("spotitheme.albumSaved.previous", host.get());
                View owner = (View) param.thisObject;
                while (owner != null && !"cwp_header_action2".equals(ViewScopes.resource(owner))) {
                    ViewParent parent = owner.getParent(); owner = parent instanceof View ? (View) parent : null;
                }
                if (owner == null) host.remove();
                else { host.set(owner); installCanvas((Canvas) param.args[0]); }
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                View previous = (View) param.getObjectExtra("spotitheme.albumSaved.previous");
                if (previous == null) host.remove(); else host.set(previous);
            }
        });
        ModuleLog.info("Album Compose saved-action paint hook installed");
    }
    private synchronized void installCanvas(Canvas canvas) {
        for (Class<?> type = canvas.getClass(); type != null && Canvas.class.isAssignableFrom(type); type = type.getSuperclass()) {
            if (!type.getName().startsWith("android.graphics.")) continue;
            Method draw;
            try { draw = type.getDeclaredMethod("drawPath", Path.class, Paint.class); }
            catch (NoSuchMethodException ignored) { continue; }
            if (installed.contains(draw)) return;
            XposedBridge.hookMethod(draw, new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                    if (!runtime.snapshot().enabled || host.get() == null || !(param.args[1] instanceof Paint)) return;
                    Paint source = (Paint) param.args[1];
                    int color = StatusIconColors.themedSavedIndicator(source.getColor(), runtime.snapshot().palette.color("savedIndicator"));
                    if (color == source.getColor()) return;
                    Paint themed = new Paint(source); themed.setColor(color); param.args[1] = themed;
                }
            });
            installed.add(draw); return;
        }
    }
}
