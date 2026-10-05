package com.spotitheme.hooks;

import android.content.Context;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.AlbumGradientColors;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** The verified album base-fade factory, retaining source artwork in native mode. */
public final class AlbumComposeGradientHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final Context context;
    public AlbumComposeGradientHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile, Context context) {
        this.runtime = runtime; this.profile = profile; this.context = context;
    }

    public void install() throws ReflectiveOperationException {
        int startId = context.getResources().getIdentifier("encore_header_gradient_start", "color", "com.spotify.music");
        int endId = context.getResources().getIdentifier("encore_header_gradient_end", "color", "com.spotify.music");
        if (startId == 0 || endId == 0) { ModuleLog.info("Album gradient resources unavailable; hook skipped"); return; }
        Field value = profile.field("compose.packedColor");
        Constructor<?> color = (Constructor<?>) profile.resolve("compose.colorConstructor");
        XposedBridge.hookMethod(profile.resolve("album.composeGradientFactory"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                ThemeRuntime.Snapshot state = runtime.snapshot();
                if (!state.enabled || !(param.args[0] instanceof List)) return;
                List<?> original = (List<?>) param.args[0];
                if (original.size() < 2) return;
                Object first = original.get(0), last = original.get(original.size() - 1);
                if (!value.getDeclaringClass().isInstance(first) || !value.getDeclaringClass().isInstance(last)) return;
                try {
                    int sourceStart = (int) (value.getLong(first) >>> 32), sourceEnd = (int) (value.getLong(last) >>> 32);
                    if (!AlbumGradientColors.matchesBaseFade(sourceStart, sourceEnd,
                            context.getColor(startId), context.getColor(endId))) return;
                    List<Object> replacement = new ArrayList<>(original);
                    int start = state.fixed ? state.palette.color("albumHeaderBackground") : sourceStart;
                    int end = state.palette.color(state.fixed ? "albumHeaderBackground" : "background");
                    replacement.set(0, color.newInstance(((long) start) << 32));
                    replacement.set(replacement.size() - 1, color.newInstance(((long) end) << 32));
                    param.args[0] = replacement;
                } catch (ReflectiveOperationException failure) { ModuleLog.error("Album Compose gradient failed", failure); }
            }
        });
        ModuleLog.info("Album Compose base-fade hook installed");
    }
}
