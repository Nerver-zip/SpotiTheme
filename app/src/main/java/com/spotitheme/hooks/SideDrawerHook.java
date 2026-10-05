package com.spotitheme.hooks;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Exact drawer constructor and the source's two-level drawer surface ownership. */
public final class SideDrawerHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final Map<View, Drawable> originals = new WeakHashMap<>();

    public SideDrawerHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) {
        this.runtime = runtime; this.profile = profile;
    }

    public void install() throws ReflectiveOperationException {
        XposedBridge.hookMethod(profile.resolve("drawer.constructor"), new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (!(param.thisObject instanceof ViewGroup)) return;
                ViewGroup layout = (ViewGroup) param.thisObject;
                View outer = layout.getChildCount() == 0 ? null : layout.getChildAt(0);
                View drawer = outer instanceof ViewGroup && ((ViewGroup) outer).getChildCount() > 0
                        ? ((ViewGroup) outer).getChildAt(0) : null;
                if (drawer == null) return;
                Drawable background = drawer.getBackground();
                if (background != null && background.getConstantState() == null) {
                    ModuleLog.info("Drawer surface cannot be copied; leaving background unchanged");
                    return;
                }
                originals.put(drawer, background == null ? null
                        : background.getConstantState().newDrawable(drawer.getResources()).mutate());
                refresh(drawer);
            }
        });
        runtime.addListener(() -> {
            for (View drawer : new ArrayList<>(originals.keySet())) if (drawer != null) refresh(drawer);
        });
        ModuleLog.info("Side drawer background hook installed");
    }

    private void refresh(View drawer) {
        drawer.setBackground(runtime.snapshot().enabled
                ? new ColorDrawable(runtime.snapshot().palette.color("surface")) : originals.get(drawer));
    }
}
