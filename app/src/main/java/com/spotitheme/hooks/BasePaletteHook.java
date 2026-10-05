package com.spotitheme.hooks;

import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.PaletteValues;
import com.spotitheme.theme.ThemePalette;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/** Encore and Compose entry points, resolved exclusively through the pinned profile. */
public final class BasePaletteHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final Map<Object, Object> cache = new IdentityHashMap<>();
    private final Map<Object, Object> originals = new WeakHashMap<>();
    private final Constructor<?> paletteConstructor, backgroundConstructor, layerConstructor;
    private final Constructor<?> textConstructor, essentialConstructor, decorativeConstructor, materialConstructor;
    private final Field backgrounds;
    private final Field[] backgroundColors = new Field[3];
    private final Object state;
    private final Method stateRead, stateWrite;
    private boolean observed;
    private boolean failed;
    private Object bodyPalette;

    public synchronized Object bodyPalette() throws ReflectiveOperationException {
        if (bodyPalette == null) bodyPalette = createEncore(runtime.snapshot().palette, "background");
        return bodyPalette;
    }

    public BasePaletteHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) throws ReflectiveOperationException {
        this.runtime = runtime;
        this.profile = profile;
        paletteConstructor = constructor("encore.paletteConstructor");
        backgroundConstructor = constructor("encore.backgroundConstructor");
        layerConstructor = constructor("encore.layerConstructor");
        textConstructor = constructor("encore.textConstructor");
        essentialConstructor = constructor("encore.essentialConstructor");
        decorativeConstructor = constructor("encore.decorativeConstructor");
        materialConstructor = constructor("material.colorConstructor");
        backgrounds = profile.field("encore.backgrounds");
        backgroundColors[0] = profile.field("encore.backgroundBase");
        backgroundColors[1] = profile.field("encore.backgroundHighlight");
        backgroundColors[2] = profile.field("encore.backgroundPress");
        stateRead = method("compose.stateRead");
        stateWrite = method("compose.stateWrite");
        state = method("compose.stateFactory").invoke(null, runtime.snapshot().generation);
        runtime.addCompositionObserver(() -> {
            try { stateRead.invoke(state); }
            catch (ReflectiveOperationException failure) { report(failure); }
        });
        runtime.addListener(this::refresh);
    }

    private Constructor<?> constructor(String role) throws ReflectiveOperationException {
        return (Constructor<?>) profile.resolve(role);
    }
    private Method method(String role) throws ReflectiveOperationException {
        return (Method) profile.resolve(role);
    }

    public void install() throws ReflectiveOperationException {
        hookResult("encore.accessor", 0);
        hookResult("encore.resolver", 1);
        hookArgument("encore.provider.c", false, 2);
        hookArgument("encore.provider.d", false, 2);
        hookArgument("material.theme", true, 5);
        ModuleLog.info("Base palette hooks installed; profile=" + Profile_9_1_86_2432.VERSION_NAME);
    }

    private void hookResult(String role, int composerIndex) throws ReflectiveOperationException {
        XposedBridge.hookMethod(method(role), new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (param.hasThrowable() || param.getResult() == null) return;
                try { param.setResult(recolor(param.getResult(), false, param.args[composerIndex])); }
                catch (Throwable failure) { report(failure); }
            }
        });
    }

    private void hookArgument(String role, boolean material, int composerIndex) throws ReflectiveOperationException {
        XposedBridge.hookMethod(method(role), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0] == null) return;
                try { param.args[0] = recolor(param.args[0], material, param.args[composerIndex]); }
                catch (Throwable failure) { report(failure); }
            }
        });
    }

    private synchronized Object recolor(Object input, boolean material, Object composer) throws ReflectiveOperationException {
        stateRead.invoke(state);
        ThemeRuntime.Snapshot snapshot = runtime.snapshot();
        Object original = originals.getOrDefault(input, input);
        if (!snapshot.enabled || (!material && (NativePaletteScope.active() || runtime.keepNativePalette(composer)))) return original;
        Object cached = cache.get(original);
        if (cached != null) return cached;
        Object result;
        if (material) {
            if (!snapshot.fixed && !hasBaseBackground(original)) return original;
            result = materialConstructor.newInstance(PaletteValues.material(snapshot.palette));
        } else {
            Object originalBackground = backgrounds.get(original);
            int[] colors = new int[3];
            for (int i = 0; i < colors.length; i++) colors[i] = PaletteValues.unpack(backgroundColors[i].getLong(originalBackground));
            String role = PaletteValues.backgroundRole(colors, snapshot.fixed);
            if (role == null) return original;
            result = createEncore(snapshot.palette, role);
        }
        if (cache.size() >= 128) cache.clear();
        cache.put(original, result);
        originals.put(result, original);
        if (!observed) {
            observed = true;
            ModuleLog.info("Base palette hook executed; palette=" + snapshot.palette.getId());
        }
        return result;
    }

    private boolean hasBaseBackground(Object original) throws ReflectiveOperationException {
        for (int i = 0; i < Profile_9_1_86_2432.MATERIAL_COLOR_FIELDS.length(); i++) {
            if (PaletteValues.unpack(profile.field("material.color." + i).getLong(original)) == 0xFF121212) return true;
        }
        return false;
    }

    private Object createEncore(ThemePalette p, String role) throws ReflectiveOperationException {
        boolean accent = role.equals("accent");
        Object elevated = layerConstructor.newInstance(PaletteValues.layer(p, accent ? "accent" : "surface"));
        Object tinted = layerConstructor.newInstance(PaletteValues.layer(p, accent ? "accent" : "tinted"));
        Object[] main = PaletteValues.layer(p, role);
        Object background = backgroundConstructor.newInstance(elevated, tinted, main[0], main[1], main[2]);
        Object text = textConstructor.newInstance(PaletteValues.foreground(p, accent));
        Object essential = essentialConstructor.newInstance(PaletteValues.foreground(p, accent));
        Object decorative = decorativeConstructor.newInstance(PaletteValues.colors(
                p.color(accent ? "onAccent" : "decorative"), accent ? 0xFFB9E5FA : p.color("decorativeSubdued")));
        return paletteConstructor.newInstance(background, text, essential, decorative);
    }

    private synchronized void refresh() {
        bodyPalette = null;
        cache.clear();
        try { stateWrite.invoke(state, runtime.snapshot().generation); }
        catch (ReflectiveOperationException failure) { report(failure); }
    }

    private void report(Throwable failure) {
        if (!failed) {
            failed = true;
            ModuleLog.error("Base palette hook failed; retaining the original result", failure);
        }
    }
}
