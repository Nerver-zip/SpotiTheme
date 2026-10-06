package com.spotitheme.hooks;

import android.graphics.BlendMode;
import android.os.Build;
import android.view.View;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** SongDNA fixed card roles and native deferred-content palette preservation. */
public final class SongDnaHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final Method localRead;
    private final Object viewKey;
    private final Field variant;
    private final Constructor<?> tint, color;
    private final ThreadLocal<String> foreground = new ThreadLocal<>();
    private final ThreadLocal<Long> surface = new ThreadLocal<>();

    public SongDnaHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) throws ReflectiveOperationException {
        this.runtime = runtime; this.profile = profile;
        localRead = method("compose.localRead"); viewKey = profile.field("compose.viewLocal").get(null);
        variant = profile.field("songDna.contentVariant");
        tint = (Constructor<?>) profile.resolve("songDna.tintConstructor");
        color = (Constructor<?>) profile.resolve("compose.colorConstructor");
        if (Build.VERSION.SDK_INT < 29)
            throw new IllegalStateException("SongDNA tint requires Android 10 or newer");
        if (method("compose.blendMode").invoke(null, 5) != BlendMode.SRC_IN)
            throw new IllegalStateException("Unsupported SongDNA tint mode");
    }

    public void install() throws ReflectiveOperationException {
        nativeScope("songDna.contributor", 2, false);
        nativeScope("songDna.content", 0, true);
        foregroundScope("songDna.logo", "logo");
        foregroundScope("songDna.contributor", "roles");
        XposedBridge.hookMethod(method("compose.image"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (!"logo".equals(foreground.get()) || param.args[7] != null) return;
                try {
                    param.args[7] = tint.newInstance(5, packed("text"));
                    param.args[10] = (Integer) param.args[10] & ~256;
                    param.args[9] = (Integer) param.args[9] & ~0xE000000;
                } catch (ReflectiveOperationException failure) { ModuleLog.error("SongDNA logo tint failed", failure); }
            }
        });
        XposedBridge.hookMethod(method("compose.text"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (!"roles".equals(foreground.get()) || ((Integer) param.args[14] & 8) == 0) return;
                param.args[3] = packed("textSubdued");
                param.args[14] = (Integer) param.args[14] & ~8;
                param.args[12] = (Integer) param.args[12] & ~3072;
            }
        });
        XposedBridge.hookMethod(method("songDna.card"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                param.setObjectExtra("spotitheme.songDna.surface", surface.get());
                surface.remove();
                if (!scoped(param.args[2])) return;
                runtime.observeComposition();
                if (fixed()) surface.set(packed("surface"));
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                Long previous = (Long) param.getObjectExtra("spotitheme.songDna.surface");
                if (previous == null) surface.remove(); else surface.set(previous);
            }
        });
        XposedBridge.hookMethod(method("songDna.gradient"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                Long selected = surface.get();
                if (selected == null) return;
                try {
                    param.args[0] = color.newInstance(selected.longValue());
                    param.args[1] = selected; param.args[2] = selected;
                    param.args[8] = (Integer) param.args[8] & ~0x3FE;
                } catch (ReflectiveOperationException failure) { ModuleLog.error("SongDNA surface failed", failure); }
            }
        });
        ModuleLog.info("SongDNA surface, foreground and deferred native palette hooks installed");
    }

    private void nativeScope(String role, int composerIndex, boolean deferred) throws ReflectiveOperationException {
        XposedBridge.hookMethod(method(role), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                try {
                    if (deferred && variant.getInt(param.thisObject) != 0 || !scoped(param.args[composerIndex])) return;
                    runtime.observeComposition();
                    if (runtime.snapshot().enabled && !runtime.snapshot().fixed)
                        param.setObjectExtra("spotitheme.songDna.native", NativePaletteScope.enter());
                } catch (ReflectiveOperationException failure) { ModuleLog.error("SongDNA native scope failed", failure); }
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                Integer previous = (Integer) param.getObjectExtra("spotitheme.songDna.native");
                if (previous != null) NativePaletteScope.restore(previous);
            }
        });
    }

    private void foregroundScope(String methodRole, String role) throws ReflectiveOperationException {
        XposedBridge.hookMethod(method(methodRole), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                param.setObjectExtra("spotitheme.songDna.foreground", foreground.get());
                foreground.remove();
                if (!scoped(param.args[2])) return;
                runtime.observeComposition();
                if (fixed()) foreground.set(role);
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                String previous = (String) param.getObjectExtra("spotitheme.songDna.foreground");
                if (previous == null) foreground.remove(); else foreground.set(previous);
            }
        });
    }
    private boolean scoped(Object composer) {
        try {
            Object local = localRead.invoke(composer, viewKey);
            return local instanceof View && ViewScopes.expandedPlayer((View) local) && ViewScopes.ancestor((View) local, "widgets_container");
        } catch (ReflectiveOperationException failure) { return false; }
    }
    private Method method(String role) throws ReflectiveOperationException { return (Method) profile.resolve(role); }
    private boolean fixed() { return runtime.snapshot().enabled && runtime.snapshot().fixed; }
    private long packed(String role) { return ((long) runtime.snapshot().palette.color(role)) << 32; }
}
