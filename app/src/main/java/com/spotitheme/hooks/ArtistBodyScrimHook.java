package com.spotitheme.hooks;

import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/** The .75-to-1 body scrim is distinct from the photograph's 0-to-.75 overlay. */
public final class ArtistBodyScrimHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final ThreadLocal<Boolean> body = new ThreadLocal<>();
    private final BasePaletteHook palettes;
    public ArtistBodyScrimHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile, BasePaletteHook palettes) {
        this.runtime = runtime; this.profile = profile; this.palettes = palettes;
    }
    public void install() throws ReflectiveOperationException {
        Field packed = profile.field("compose.packedColor");
        Constructor<?> color = (Constructor<?>) profile.resolve("compose.colorConstructor");
        Method scrim = (Method) profile.resolve("artist.scrim");
        Method provider = (Method) profile.resolve("encore.provider.c");
        Class<?> contentInterface = scrim.getParameterTypes()[3];
        XposedBridge.hookMethod(profile.resolve("artist.scrim"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                param.setObjectExtra("spotitheme.artist.previousBody", body.get());
                runtime.observeComposition();
                body.set(runtime.snapshot().enabled && (Float) param.args[1] == .75f && (Float) param.args[2] == 1f);
                Object content = param.args[3];
                if (Boolean.TRUE.equals(body.get()) && provider.getParameterTypes()[1].isInstance(content)) {
                    param.args[3] = Proxy.newProxyInstance(contentInterface.getClassLoader(), new Class<?>[]{contentInterface},
                            (proxy, method, args) -> {
                                if (method.getDeclaringClass() == Object.class) {
                                    if (method.getName().equals("equals")) return proxy == args[0];
                                    if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                                    return "SpotiTheme artist body content";
                                }
                                try {
                                    if (!runtime.snapshot().enabled) return method.invoke(content, args);
                                    runtime.observeComposition();
                                    provider.invoke(null, palettes.bodyPalette(), content, args[0], 0);
                                    return null;
                                } catch (InvocationTargetException failure) { throw failure.getCause(); }
                            });
                }
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                Boolean previous = (Boolean) param.getObjectExtra("spotitheme.artist.previousBody");
                if (previous == null) body.remove(); else body.set(previous);
            }
        });
        XposedBridge.hookMethod(profile.resolve("compose.gradientConstructor"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!Boolean.TRUE.equals(body.get()) || !(param.args[3] instanceof List)) return;
                List<?> original = (List<?>) param.args[3];
                if (original.size() != 2 || !packed.getDeclaringClass().isInstance(original.get(0))
                        || !packed.getDeclaringClass().isInstance(original.get(1))) return;
                int first = (int) (packed.getLong(original.get(0)) >>> 32);
                int last = (int) (packed.getLong(original.get(1)) >>> 32);
                if (first != 0xBF121212 || last != 0xFF121212) return;
                int rgb = runtime.snapshot().palette.color("albumHeaderBackground") & 0xFFFFFF;
                List<Object> replacement = new ArrayList<>(2);
                replacement.add(color.newInstance(runtime.snapshot().animated ? 0L
                        : ((long) ((first & 0xFF000000) | rgb)) << 32));
                replacement.add(color.newInstance(runtime.snapshot().animated ? 0L
                        : ((long) ((last & 0xFF000000) | rgb)) << 32));
                param.args[3] = replacement;
                body.set(false);
            }
        });
        ModuleLog.info("Artist body scrim gradient hook installed");
    }
}
