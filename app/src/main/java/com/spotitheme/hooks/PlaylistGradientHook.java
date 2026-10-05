package com.spotitheme.hooks;

import android.view.View;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** Scoped playlist artwork fade and the matching compact-header solid fill. */
public final class PlaylistGradientHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final PlaylistHeaderScope header;
    private final ThreadLocal<View> drawing = new ThreadLocal<>();
    private WeakReference<View> artworkRoot = new WeakReference<>(null);
    private int artworkColor;
    public PlaylistGradientHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile, PlaylistHeaderScope header) {
        this.runtime = runtime; this.profile = profile; this.header = header;
    }

    public void install() throws ReflectiveOperationException {
        Field value = profile.field("compose.packedColor");
        Constructor<?> color = (Constructor<?>) profile.resolve("compose.colorConstructor");
        XposedBridge.hookMethod(profile.resolve("compose.dispatchDraw"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                param.setObjectExtra("spotitheme.playlist.draw", drawing.get());
                View view = (View) param.thisObject;
                if (!header.contains(view)) drawing.remove();
                else {
                    drawing.set(view);
                    View root = header.root();
                    if (artworkRoot.get() != root) { artworkRoot = new WeakReference<>(root); artworkColor = 0; }
                }
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                View previous = (View) param.getObjectExtra("spotitheme.playlist.draw");
                if (previous == null) drawing.remove(); else drawing.set(previous);
            }
        });
        XposedBridge.hookMethod(profile.resolve("compose.linearGradient"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (!active() || !(param.args[3] instanceof List)) return;
                List<?> source = (List<?>) param.args[3];
                if (source.size() != 2) return;
                long start = (Long) param.args[1], end = (Long) param.args[2];
                float sx = high(start), sy = low(start), ex = high(end), ey = low(end);
                if (sx != ex || ey <= sy) return;
                try {
                    int first = (int) (value.getLong(source.get(0)) >>> 32);
                    int last = (int) (value.getLong(source.get(1)) >>> 32);
                    boolean scrim = (first & 0xFFFFFF) == 0x121212 && (last & 0xFFFFFF) == 0x181818
                            && sx == 0 && sy == 0 && ey <= drawing.get().getResources().getDisplayMetrics().density * 100f;
                    if (!scrim && last != runtime.snapshot().palette.color("background")) return;
                    if (!scrim) artworkColor = first;
                    List<Object> replacement = new ArrayList<>(source);
                    replacement.set(0, color.newInstance(packed()));
                    if (scrim) replacement.set(1, color.newInstance(packed()));
                    param.args[3] = replacement;
                } catch (ReflectiveOperationException failure) { ModuleLog.error("Playlist gradient failed", failure); }
            }
        });
        XposedBridge.hookMethod(profile.resolve("compose.colorRect"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (!active() || artworkColor == 0) return;
                int source = (int) ((Long) param.args[1] >>> 32);
                long size = (Long) param.args[3];
                float width = high(size), height = low(size);
                if ((source & 0xFFFFFF) != (artworkColor & 0xFFFFFF)
                        || width < 160f || height <= 0 || width < height * 2f) return;
                param.args[1] = packed();
            }
        });
        ModuleLog.info("Playlist artwork gradient and compact fill hooks installed");
    }

    private boolean active() {
        View root = header.root();
        return runtime.snapshot().enabled && runtime.snapshot().fixed && root != null
                && root.isAttachedToWindow() && drawing.get() != null && header.contains(drawing.get());
    }
    private long packed() { return ((long) runtime.snapshot().palette.color("albumHeaderBackground")) << 32; }
    private static float high(long packed) { return Float.intBitsToFloat((int) (packed >>> 32)); }
    private static float low(long packed) { return Float.intBitsToFloat((int) packed); }
}
