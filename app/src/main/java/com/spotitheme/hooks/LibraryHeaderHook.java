package com.spotitheme.hooks;

import android.view.View;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** The exact Library title String receives a scoped explicit Compose color. */
public final class LibraryHeaderHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final ThreadLocal<String> title = new ThreadLocal<>();
    public LibraryHeaderHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) { this.runtime = runtime; this.profile = profile; }

    public void install() throws ReflectiveOperationException {
        Field titleField = profile.field("library.title");
        Method localRead = (Method) profile.resolve("compose.localRead");
        Object viewKey = profile.field("compose.viewLocal").get(null);
        XposedBridge.hookMethod(profile.resolve("library.header"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                param.setObjectExtra("spotitheme.library.title", title.get()); title.remove();
                try {
                    Object local = localRead.invoke(param.args[5], viewKey);
                    if (!(local instanceof View) || !ViewScopes.ancestor((View) local, "fragment_container")) return;
                    runtime.observeComposition();
                    if (runtime.snapshot().enabled && runtime.snapshot().fixed) title.set((String) titleField.get(param.args[0]));
                } catch (ReflectiveOperationException failure) { ModuleLog.error("Library header scope failed", failure); }
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                String previous = (String) param.getObjectExtra("spotitheme.library.title");
                if (previous == null) title.remove(); else title.set(previous);
            }
        });
        XposedBridge.hookMethod(profile.resolve("compose.text"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (title.get() == null || title.get() != param.args[0]) return;
                param.args[3] = ((long) runtime.snapshot().palette.color("text")) << 32;
                param.args[14] = (Integer) param.args[14] & ~8;
                param.args[12] = (Integer) param.args[12] & ~3072;
            }
        });
        ModuleLog.info("Library header title hook installed");
    }
}
