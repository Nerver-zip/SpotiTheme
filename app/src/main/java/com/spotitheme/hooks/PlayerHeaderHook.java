package com.spotitheme.hooks;

import android.graphics.Canvas;
import android.view.View;
import android.widget.TextView;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.lang.reflect.Method;

/** Native close control and ordinary expanded seekbar timestamps. */
public final class PlayerHeaderHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    public PlayerHeaderHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) { this.runtime = runtime; this.profile = profile; }
    public void install() throws ReflectiveOperationException {
        Method localRead = (Method) profile.resolve("compose.localRead");
        Object viewKey = profile.field("compose.viewLocal").get(null);
        XposedBridge.hookMethod(profile.resolve("player.closePalette"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[1] == null || !param.args[1].getClass().getName().equals("p.c7f")) return;
                try {
                    Object local = localRead.invoke(param.args[3], viewKey);
                    if (!(local instanceof View) || !ViewScopes.expandedPlayer((View) local)
                            || !ViewScopes.ancestor((View) local, "player_overlay_header")) return;
                    runtime.observeComposition();
                    if (runtime.snapshot().enabled && !runtime.snapshot().fixed)
                        param.setObjectExtra("spotitheme.close.native", NativePaletteScope.enter());
                } catch (ReflectiveOperationException failure) { ModuleLog.error("Player close scope failed", failure); }
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                Integer previous = (Integer) param.getObjectExtra("spotitheme.close.native");
                if (previous != null) NativePaletteScope.restore(previous);
            }
        });
        XposedHelpers.findAndHookMethod(TextView.class, "onDraw", Canvas.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                TextView view = (TextView) param.thisObject;
                if (!runtime.snapshot().enabled || !ViewScopes.expandedPlayer(view)
                        || !view.getClass().getName().equals("com.spotify.nowplaying.uiusecases.seekbar.SuppressLayoutTextView")) return;
                View layout = view;
                while (layout != null && !ViewScopes.resource(layout).equals("track_seekbar"))
                    layout = layout.getParent() instanceof View ? (View) layout.getParent() : null;
                if (layout == null || !layout.getClass().getName().equals("android.widget.FrameLayout")) return;
                String name = ViewScopes.resource(view);
                if (!name.equals("position_text") && !name.equals("duration_text")) return;
                int original = view.getCurrentTextColor();
                int role = runtime.snapshot().fixed ? runtime.snapshot().palette.color("textSubdued") : 0xFFB3B3B3;
                param.setObjectExtra("spotitheme.timestamp.original", original);
                XposedHelpers.setIntField(view, "mCurTextColor", (role & 0xFFFFFF) | (((original >>> 24) * (role >>> 24) / 255) << 24));
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                Integer original = (Integer) param.getObjectExtra("spotitheme.timestamp.original");
                if (original != null) XposedHelpers.setIntField(param.thisObject, "mCurTextColor", original);
            }
        });
        ModuleLog.info("Player close and timestamp contrast hooks installed");
    }
}
