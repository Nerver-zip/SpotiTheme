package com.spotitheme.hooks;

import android.view.View;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Normal playlist header foregrounds; annotated creator content remains intact. */
public final class PlaylistTextHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final PlaylistHeaderScope header;
    private final ThreadLocal<String> expectedTitle = new ThreadLocal<>();
    private final ThreadLocal<Object> expectedCreator = new ThreadLocal<>();

    public PlaylistTextHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile, PlaylistHeaderScope header) {
        this.runtime = runtime; this.profile = profile; this.header = header;
    }

    public void install() throws ReflectiveOperationException {
        Method localRead = (Method) profile.resolve("compose.localRead");
        Object key = profile.field("compose.viewLocal").get(null);
        Field variant = profile.field("playlist.titleVariant"), model = profile.field("playlist.titleModel");
        Field title = profile.field("playlist.title"), creator = profile.field("playlist.creatorText");
        XposedBridge.hookMethod(profile.resolve("playlist.titleOwner"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                param.setObjectExtra("spotitheme.playlist.title", expectedTitle.get()); expectedTitle.remove();
                try {
                    if (variant.getInt(param.thisObject) != 14 || !scoped(localRead, key, param.args[0])) return;
                    runtime.observeComposition();
                    Object data = model.get(param.thisObject);
                    if (fixed() && title.getDeclaringClass().isInstance(data)) expectedTitle.set((String) title.get(data));
                } catch (ReflectiveOperationException failure) { ModuleLog.error("Playlist title scope failed", failure); }
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                String previous = (String) param.getObjectExtra("spotitheme.playlist.title");
                if (previous == null) expectedTitle.remove(); else expectedTitle.set(previous);
            }
        });
        XposedBridge.hookMethod(profile.resolve("playlist.creatorOwner"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                param.setObjectExtra("spotitheme.playlist.creator", expectedCreator.get()); expectedCreator.remove();
                try {
                    if (!scoped(localRead, key, param.args[9])) return;
                    runtime.observeComposition();
                    if (fixed()) expectedCreator.set(creator.get(param.args[3]));
                } catch (ReflectiveOperationException failure) { ModuleLog.error("Playlist creator scope failed", failure); }
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                Object previous = param.getObjectExtra("spotitheme.playlist.creator");
                if (previous == null) expectedCreator.remove(); else expectedCreator.set(previous);
            }
        });
        XposedBridge.hookMethod(profile.resolve("compose.text"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                String expected = expectedTitle.get();
                if (expected != null && expected.equals(param.args[0])) foreground(param, 14, 12);
            }
        });
        XposedBridge.hookMethod(profile.resolve("compose.annotatedText"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                Object expected = expectedCreator.get();
                if (expected != null && expected == param.args[0] && fixed()) foreground(param, 15, 13);
            }
        });
        ModuleLog.info("Playlist title and annotated creator hooks installed");
    }

    private void foreground(XC_MethodHook.MethodHookParam param, int defaults, int changes) {
        param.args[3] = ((long) runtime.snapshot().palette.color("text")) << 32;
        param.args[defaults] = (Integer) param.args[defaults] & ~8;
        param.args[changes] = (Integer) param.args[changes] & ~3072;
    }
    private boolean scoped(Method read, Object key, Object composer) throws ReflectiveOperationException {
        if (!read.getDeclaringClass().isInstance(composer)) return false;
        Object local = read.invoke(composer, key);
        return local instanceof View && header.contains((View) local);
    }
    private boolean fixed() { return runtime.snapshot().enabled && runtime.snapshot().fixed; }
}
