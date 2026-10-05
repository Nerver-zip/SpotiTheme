package com.spotitheme.hooks;

import android.view.View;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Method;

/** The Credits owner's single direct rounded fill uses the surface role. */
public final class CreditsCardHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final ThreadLocal<Boolean> pendingFill = new ThreadLocal<>();
    private boolean observed;

    public CreditsCardHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) {
        this.runtime = runtime;
        this.profile = profile;
    }

    public void install() throws ReflectiveOperationException {
        Method localRead = (Method) profile.resolve("compose.localRead");
        Object key = profile.field("compose.viewLocal").get(null);
        XposedBridge.hookMethod(profile.resolve("credits.card"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                param.setObjectExtra("spotitheme.credits.previousFill", pendingFill.get());
                pendingFill.remove();
                try {
                    Object local = localRead.invoke(param.args[7], key);
                    if (!(local instanceof View) || !ViewScopes.expandedPlayer((View) local)
                            || !ViewScopes.ancestor((View) local, "widgets_container")) return;
                    runtime.observeComposition();
                    if (!runtime.snapshot().enabled) return;
                    if (runtime.snapshot().fixed) pendingFill.set(true);
                    else param.setObjectExtra("spotitheme.credits.nativeDepth", NativePaletteScope.enter());
                } catch (ReflectiveOperationException failure) { ModuleLog.error("Credits scope failed", failure); }
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                Integer previousDepth = (Integer) param.getObjectExtra("spotitheme.credits.nativeDepth");
                if (previousDepth != null) NativePaletteScope.restore(previousDepth);
                Boolean previousFill = (Boolean) param.getObjectExtra("spotitheme.credits.previousFill");
                if (previousFill == null) pendingFill.remove(); else pendingFill.set(previousFill);
            }
        });
        XposedBridge.hookMethod(profile.resolve("compose.roundedFill"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (!Boolean.TRUE.equals(pendingFill.get())) return;
                pendingFill.remove();
                param.args[1] = ((long) runtime.snapshot().palette.color("surface")) << 32;
                if (!observed) { observed = true; ModuleLog.info("Credits surface fill applied"); }
            }
        });
        ModuleLog.info("Credits card surface hook installed");
    }
}
