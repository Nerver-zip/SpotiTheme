package com.spotitheme.hooks;

import android.view.View;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Method;

/** Search landing field fill, icon and label with native/fixed palette parity. */
public final class SearchLandingHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final ThreadLocal<Boolean> active = new ThreadLocal<>();
    public SearchLandingHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) { this.runtime = runtime; this.profile = profile; }

    public void install() throws ReflectiveOperationException {
        Method localRead = method("compose.localRead");
        Object viewKey = profile.field("compose.viewLocal").get(null);
        XposedBridge.hookMethod(method("search.landing"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                param.setObjectExtra("spotitheme.search.previous", active.get());
                active.remove();
                try {
                    Object local = localRead.invoke(param.args[3], viewKey);
                    if (!(local instanceof View) || ViewScopes.expandedPlayer((View) local)) return;
                    runtime.observeComposition();
                    if (runtime.snapshot().enabled) active.set(true);
                } catch (ReflectiveOperationException failure) { ModuleLog.error("Search field scope failed", failure); }
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                Boolean previous = (Boolean) param.getObjectExtra("spotitheme.search.previous");
                if (previous == null) active.remove(); else active.set(previous);
            }
        });
        renderer("compose.roundedFill", true, false);
        renderer("compose.icon", false, true);
        renderer("compose.text", false, false);
        ModuleLog.info("Search landing field hooks installed");
    }

    private void renderer(String role, boolean fill, boolean icon) throws ReflectiveOperationException {
        XposedBridge.hookMethod(method(role), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (!Boolean.TRUE.equals(active.get())) return;
                param.args[fill ? 1 : 3] = ((long) runtime.snapshot().palette.color(fill ? "tinted" : "textSubdued")) << 32;
                if (!fill) {
                    int defaults = icon ? 8 : 14, changes = icon ? 7 : 12;
                    param.args[defaults] = (Integer) param.args[defaults] & ~8;
                    param.args[changes] = (Integer) param.args[changes] & ~3072;
                }
            }
        });
    }
    private Method method(String role) throws ReflectiveOperationException { return (Method) profile.resolve(role); }
}
