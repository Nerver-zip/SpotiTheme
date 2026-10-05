package com.spotitheme.hooks;

import android.view.View;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** Playlist naming gradient, heading and owner divider; input child fills stay intact. */
public final class PlaylistCreationHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final ThreadLocal<Boolean> background = new ThreadLocal<>(), heading = new ThreadLocal<>(), input = new ThreadLocal<>();
    public PlaylistCreationHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) { this.runtime = runtime; this.profile = profile; }

    public void install() throws ReflectiveOperationException {
        Method localRead = method("compose.localRead");
        Object viewKey = profile.field("compose.viewLocal").get(null);
        Field model = profile.field("creation.model");
        // Resolve an exact constructor only to obtain the verified model class;
        // never initialize or instantiate Spotify's creation model.
        Class<?> modelClass = profile.resolve("creation.modelConstructor").getDeclaringClass();
        owner("creation.background", background, true, 4, model, modelClass, localRead, viewKey);
        owner("creation.heading", heading, false, 1, model, modelClass, localRead, viewKey);
        owner("creation.input", input, false, 4, model, modelClass, localRead, viewKey);
        Constructor<?> packedColor = (Constructor<?>) profile.resolve("compose.colorConstructor");
        XposedBridge.hookMethod(profile.resolve("compose.gradientConstructor"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (!Boolean.TRUE.equals(background.get())) return;
                List<?> original = (List<?>) param.args[3];
                if (original == null || original.isEmpty()) return;
                try {
                    List<Object> replacement = new ArrayList<>(original.size());
                    for (int i = 0; i < original.size(); i++) replacement.add(packedColor.newInstance(packed("background")));
                    param.args[3] = replacement;
                } catch (ReflectiveOperationException failure) { ModuleLog.error("Creation gradient failed", failure); }
            }
        });
        XposedBridge.hookMethod(method("creation.fieldContent"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                param.setObjectExtra("spotitheme.creation.child", input.get()); input.remove();
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                restore(input, (Boolean) param.getObjectExtra("spotitheme.creation.child"));
            }
        });
        XposedBridge.hookMethod(method("compose.roundedFill"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (Boolean.TRUE.equals(input.get())) param.args[1] = packed("textSubdued");
            }
        });
        XposedBridge.hookMethod(method("compose.text"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (!Boolean.TRUE.equals(heading.get())) return;
                param.args[3] = packed("text");
                param.args[14] = (Integer) param.args[14] & ~8;
                param.args[12] = (Integer) param.args[12] & ~3072;
            }
        });
        ModuleLog.info("Playlist creation background, heading and divider hooks installed");
    }

    private void owner(String role, ThreadLocal<Boolean> scope, boolean staticOwner, int composerIndex,
            Field model, Class<?> modelClass, Method localRead, Object viewKey) throws ReflectiveOperationException {
        XposedBridge.hookMethod(method(role), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                param.setObjectExtra("spotitheme.creation.previous", scope.get()); scope.remove();
                try {
                    Object receiver = staticOwner ? param.args[0] : param.thisObject;
                    if (!modelClass.isInstance(model.get(receiver))) return;
                    Object local = localRead.invoke(param.args[composerIndex], viewKey);
                    if (!(local instanceof View) || ViewScopes.expandedPlayer((View) local)) return;
                    runtime.observeComposition();
                    if (runtime.snapshot().enabled) scope.set(true);
                } catch (ReflectiveOperationException failure) { ModuleLog.error("Creation owner scope failed", failure); }
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                restore(scope, (Boolean) param.getObjectExtra("spotitheme.creation.previous"));
            }
        });
    }
    private static void restore(ThreadLocal<Boolean> scope, Boolean value) { if (value == null) scope.remove(); else scope.set(value); }
    private Method method(String role) throws ReflectiveOperationException { return (Method) profile.resolve(role); }
    private long packed(String role) { return ((long) runtime.snapshot().palette.color(role)) << 32; }
}
