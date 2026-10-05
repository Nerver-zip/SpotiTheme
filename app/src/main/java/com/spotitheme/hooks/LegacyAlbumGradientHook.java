package com.spotitheme.hooks;

import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Pinned legacy album models; unrelated same-color-class fades remain untouched. */
public final class LegacyAlbumGradientHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;

    public LegacyAlbumGradientHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) {
        this.runtime = runtime;
        this.profile = profile;
    }

    public void install() throws ReflectiveOperationException {
        Constructor<?> start = (Constructor<?>) profile.resolve("album.legacyStartColor");
        Constructor<?> end = (Constructor<?>) profile.resolve("album.legacyEndColor");
        Constructor<?> stop = (Constructor<?>) profile.resolve("album.legacyStop");
        Field position = profile.field("album.legacyStopPosition");
        Field color = profile.field("album.legacyStopColor");
        XposedBridge.hookMethod(profile.resolve("album.gradient"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                ThemeRuntime.Snapshot state = runtime.snapshot();
                if (!state.enabled || !state.fixed || !(param.args[0] instanceof List)) return;
                List<?> source = (List<?>) param.args[0];
                if (source.size() != 2 || !start.getDeclaringClass().isInstance(source.get(0))
                        || !end.getDeclaringClass().isInstance(source.get(1))) return;
                try {
                    param.args[0] = Arrays.asList(start.newInstance(state.palette.color("albumHeaderBackground")),
                            end.newInstance(state.palette.color("background")));
                } catch (ReflectiveOperationException failure) {
                    ModuleLog.error("Legacy album gradient failed", failure);
                }
            }
        });
        XposedBridge.hookMethod(profile.resolve("album.positionedGradient"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                ThemeRuntime.Snapshot state = runtime.snapshot();
                if (!state.enabled || !state.fixed || !(param.args[1] instanceof List)) return;
                List<?> source = (List<?>) param.args[1];
                if (source.size() != 3) return;
                for (Object entry : source) if (!stop.getDeclaringClass().isInstance(entry)) return;
                try {
                    if (position.getFloat(source.get(0)) != 0f || position.getFloat(source.get(1)) != .1f
                            || position.getFloat(source.get(2)) != 1f) return;
                    if (!start.getDeclaringClass().isInstance(color.get(source.get(0)))
                            || !start.getDeclaringClass().isInstance(color.get(source.get(1)))
                            || !end.getDeclaringClass().isInstance(color.get(source.get(2)))) return;
                    List<Object> replacement = new ArrayList<>(source);
                    replacement.set(2, stop.newInstance(1f, end.newInstance(state.palette.color("background"))));
                    param.args[1] = replacement;
                } catch (ReflectiveOperationException failure) {
                    ModuleLog.error("Legacy positioned album gradient failed", failure);
                }
            }
        });
        ModuleLog.info("Legacy album model hooks installed");
    }
}
