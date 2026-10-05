package com.spotitheme.hooks;

import android.view.View;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Explicit over-media countdown foreground without modifying its photograph. */
public final class CountdownPhotoTitleHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final ThreadLocal<Title> active = new ThreadLocal<>();
    private static final class Title {
        final String text; final int color;
        Title(String text, int color) { this.text = text; this.color = color; }
    }
    public CountdownPhotoTitleHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) {
        this.runtime = runtime; this.profile = profile;
    }
    public void install() throws ReflectiveOperationException {
        Field model = profile.field("countdown.ownerModel");
        Class<?> modelClass = profile.resolve("countdown.model").getDeclaringClass();
        Method localRead = (Method) profile.resolve("compose.localRead");
        Object key = profile.field("compose.viewLocal").get(null);
        XposedBridge.hookMethod(profile.resolve("countdown.titleOwner"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setObjectExtra("spotitheme.countdown.previous", active.get());
                active.remove();
                if (!modelClass.isInstance(model.get(param.thisObject))
                        || !localRead.getDeclaringClass().isInstance(param.args[1])) return;
                Object local = localRead.invoke(param.args[1], key);
                if (!(local instanceof View)) return;
                View view = (View) local;
                if (!ViewScopes.expandedPlayer(view) || !ViewScopes.ancestor(view, "widgets_container")) return;
                runtime.observeComposition();
                if (!runtime.snapshot().enabled) return;
                int title = view.getResources().getIdentifier("release_countdown", "string", "com.spotify.music");
                int color = view.getResources().getIdentifier("dark_overmedia_text_base", "color", "com.spotify.music");
                if (title == 0 || color == 0) return;
                active.set(new Title(view.getResources().getString(title), view.getContext().getColor(color)));
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                Title previous = (Title) param.getObjectExtra("spotitheme.countdown.previous");
                if (previous == null) active.remove(); else active.set(previous);
            }
        });
        XposedBridge.hookMethod(profile.resolve("compose.text"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                Title title = active.get();
                if (title == null || !title.text.equals(param.args[0])) return;
                param.args[3] = ((long) title.color) << 32;
                param.args[14] = (Integer) param.args[14] & ~8;
                param.args[12] = (Integer) param.args[12] & ~3072;
            }
        });
        ModuleLog.info("Countdown photo-title foreground hook installed");
    }
}
