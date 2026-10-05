package com.spotitheme.hooks;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Method;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;

/** Home app-bar and filter-row fade identified by their inflated resource owner. */
public final class HomeHeaderHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private Method statusGetter, statusSetter;
    private boolean replayingStatus;
    private final Map<View, Original> headers = new WeakHashMap<>();
    private static final class Original {
        final ColorStateList tint;
        Drawable.ConstantState statusBackground;
        boolean statusAbsent;
        WeakReference<View> fade;
        Drawable.ConstantState fadeBackground;
        Original(View header) {
            tint = header.getBackgroundTintList();
            int id = header.getResources().getIdentifier("gradient", "id", "com.spotify.music");
            View fadeView = id == 0 ? null : header.findViewById(id);
            fade = new WeakReference<>(fadeView);
            // Capture through the owner after accounting for resource replacements.
            fadeBackground = null;
        }
    }
    public HomeHeaderHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) {
        this.runtime = runtime; this.profile = profile;
    }

    public void install() throws ReflectiveOperationException {
        statusGetter = (Method) profile.resolve("home.statusForeground");
        statusSetter = (Method) profile.resolve("home.setStatusForeground");
        XposedBridge.hookMethod(statusSetter, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (replayingStatus || !(param.thisObject instanceof View)) return;
                View header = (View) param.thisObject;
                Original original = headers.get(header);
                if (original == null) return;
                captureStatus(original, (Drawable) param.args[0]);
                if (runtime.snapshot().enabled && original.statusBackground != null)
                    param.args[0] = themedStatus(header, original);
            }
        });
        XposedHelpers.findAndHookMethod(LayoutInflater.class, "inflate", int.class, ViewGroup.class, boolean.class,
                new XC_MethodHook() {
                    @Override protected void afterHookedMethod(MethodHookParam param) {
                        if (param.hasThrowable()) return;
                        LayoutInflater inflater = (LayoutInflater) param.thisObject;
                        String name;
                        try { name = inflater.getContext().getResources().getResourceEntryName((Integer) param.args[0]); }
                        catch (android.content.res.Resources.NotFoundException ignored) { return; }
                        if (!"funkis_home".equals(name) || !(param.getResult() instanceof View)) return;
                        View root = (View) param.getResult();
                        int id = root.getResources().getIdentifier("app_bar", "id", "com.spotify.music");
                        View header = id == 0 ? null : root.findViewById(id);
                        if (header == null) return;
                        if (!headers.containsKey(header)) {
                            Original original = new Original(header);
                            if (statusGetter.getDeclaringClass().isInstance(header)) {
                                try { captureStatus(original, (Drawable) statusGetter.invoke(header)); }
                                catch (ReflectiveOperationException failure) {
                                    ModuleLog.error("Home status foreground capture failed", failure);
                                }
                            }
                            headers.put(header, original);
                            header.addOnLayoutChangeListener((view, left, top, right, bottom,
                                    oldLeft, oldTop, oldRight, oldBottom) -> refresh(view));
                            header.post(() -> refresh(header));
                        }
                        refresh(header);
                    }
                });
        runtime.addListener(() -> {
            for (View header : new ArrayList<>(headers.keySet())) if (header != null) refresh(header);
        });
        ModuleLog.info("Home header resource surface hook installed");
    }

    private static void captureStatus(Original original, Drawable drawable) {
        original.statusAbsent = drawable == null;
        original.statusBackground = drawable == null ? null : drawable.getConstantState();
    }

    private Drawable themedStatus(View header, Original original) {
        Drawable drawable = original.statusBackground.newDrawable(header.getResources()).mutate();
        drawable.setTint(runtime.snapshot().palette.color("background"));
        return drawable;
    }

    private void refresh(View header) {
        Original original = headers.get(header);
        boolean enabled = runtime.snapshot().enabled;
        int color = runtime.snapshot().palette.color("background");
        header.setBackgroundTintList(enabled ? ColorStateList.valueOf(color) : original.tint);
        View fadeView = original.fade.get();
        if (fadeView == null) {
            int id = header.getResources().getIdentifier("gradient", "id", "com.spotify.music");
            fadeView = id == 0 ? null : header.findViewById(id);
        }
        if (fadeView != null && original.fadeBackground == null
                && fadeView.getBackground() instanceof GradientDrawable) {
            original.fade = new WeakReference<>(fadeView);
            GradientDrawable captured = (GradientDrawable) fadeView.getBackground();
            if (captured.getConstantState() != null) {
                captured = (GradientDrawable) captured.getConstantState().newDrawable(header.getResources()).mutate();
                int[] stops = captured.getColors();
                Integer nativeBackground = ThemeResourcesHook.originalColor("gray_7");
                if (enabled && nativeBackground != null && stops != null && stops.length == 2
                        && stops[0] == color && Color.alpha(stops[1]) == 0) {
                    captured.setColors(new int[]{nativeBackground, stops[1]});
                    ModuleLog.info("Home fade restoration normalized from themed resource capture");
                }
                original.fadeBackground = captured.getConstantState();
                ModuleLog.info("Home filter-row fade bound after layout");
            }
        }
        if (fadeView != null && original.fadeBackground != null) {
            GradientDrawable fade = (GradientDrawable) original.fadeBackground.newDrawable(header.getResources()).mutate();
            if (enabled) fade.setColors(new int[]{color, Color.TRANSPARENT});
            fadeView.setBackground(fade);
        }
        if (statusSetter.getDeclaringClass().isInstance(header)
                && (original.statusAbsent || original.statusBackground != null)) {
            try {
                replayingStatus = true;
                Drawable status = original.statusAbsent ? null : enabled ? themedStatus(header, original)
                        : original.statusBackground.newDrawable(header.getResources()).mutate();
                statusSetter.invoke(header, status);
            } catch (ReflectiveOperationException failure) {
                ModuleLog.error("Home status foreground refresh failed", failure);
            } finally { replayingStatus = false; }
        }
        header.invalidate();
    }
}
