package com.spotitheme.hooks;

import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Field;

/** Exact shortcut title lambda, excluding artwork and badge variants. */
public final class HomeShortcutTitleHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final ThreadLocal<String> expected = new ThreadLocal<>();

    public HomeShortcutTitleHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) {
        this.runtime = runtime; this.profile = profile;
    }

    public void install() throws ReflectiveOperationException {
        Field variant = profile.field("home.shortcutVariant");
        Field model = profile.field("home.shortcutModel");
        Field title = profile.field("home.shortcutTitle");
        XposedBridge.hookMethod(profile.resolve("home.shortcutTitleOwner"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                param.setObjectExtra("spotitheme.shortcut.previous", expected.get());
                expected.remove();
                try {
                    if (variant.getInt(param.thisObject) != 2) return;
                    runtime.observeComposition();
                    ThemeRuntime.Snapshot state = runtime.snapshot();
                    if (state.enabled && state.fixed)
                        expected.set((String) title.get(model.get(param.thisObject)));
                } catch (ReflectiveOperationException failure) {
                    ModuleLog.error("Home shortcut title scope failed", failure);
                }
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                String previous = (String) param.getObjectExtra("spotitheme.shortcut.previous");
                if (previous == null) expected.remove(); else expected.set(previous);
            }
        });
        XposedBridge.hookMethod(profile.resolve("compose.text"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                String text = expected.get();
                if (text == null || !text.equals(param.args[0])) return;
                param.args[3] = ((long) runtime.snapshot().palette.color("text")) << 32;
                param.args[14] = (Integer) param.args[14] & ~8;
                param.args[12] = (Integer) param.args[12] & ~3072;
            }
        });
        ModuleLog.info("Home Compose shortcut title hook installed");
    }
}
