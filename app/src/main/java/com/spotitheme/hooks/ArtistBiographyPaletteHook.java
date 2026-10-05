package com.spotitheme.hooks;

import android.view.View;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Biography action renderers retain native contrast in artwork mode. */
public final class ArtistBiographyPaletteHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    public ArtistBiographyPaletteHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) {
        this.runtime = runtime; this.profile = profile;
    }

    public void install() throws ReflectiveOperationException {
        Field variant = profile.field("biography.rowVariant");
        Field owner = profile.field("biography.rowOwner");
        Field view = profile.field("biography.rowView");
        Method localRead = (Method) profile.resolve("compose.localRead");
        Object key = profile.field("compose.viewLocal").get(null);
        Object following = profile.field("biography.followingInstance").get(null);
        XposedBridge.hookMethod(profile.resolve("biography.row"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (variant.getInt(param.thisObject) != 3) return;
                Object rowOwner = owner.get(param.thisObject);
                if (rowOwner == null) return;
                Object local = view.get(rowOwner);
                if (!(local instanceof View) || !"list_row_compose_view".equals(ViewScopes.resource((View) local))) return;
                enter(param, (View) local);
            }
            @Override protected void afterHookedMethod(MethodHookParam param) { restore(param); }
        });
        XposedBridge.hookMethod(profile.resolve("biography.following"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.thisObject != following) return;
                Object local = localRead.invoke(param.args[3], key);
                if (local instanceof View) enter(param, (View) local);
            }
            @Override protected void afterHookedMethod(MethodHookParam param) { restore(param); }
        });
        ModuleLog.info("Biography row and Following native palette scopes installed");
    }

    private void enter(XC_MethodHook.MethodHookParam param, View view) {
        if (!ViewScopes.expandedPlayer(view) || !ViewScopes.ancestor(view, "creator_biography_card")) return;
        runtime.observeComposition();
        ThemeRuntime.Snapshot state = runtime.snapshot();
        if (state.enabled && !state.fixed)
            param.setObjectExtra("spotitheme.biography.depth", NativePaletteScope.enter());
    }

    private void restore(XC_MethodHook.MethodHookParam param) {
        Integer depth = (Integer) param.getObjectExtra("spotitheme.biography.depth");
        if (depth != null) NativePaletteScope.restore(depth);
    }
}
