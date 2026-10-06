package com.spotitheme.hooks;

import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Expanded page and controls backdrop, with an independent native artwork-blur setting. */
public final class ExpandedBackgroundHook {
    private static final String OVERLAY_VIEW_CLASS =
            "com.spotify.nowplaying.uiusecases.overlay.OverlayHidingGradientBackgroundView";
    private static final String ARTWORK_BACKDROP = "blurred_background_image_view";
    private final ThemeRuntime runtime;
    private final Method overlayColor;
    private final Field overlayDrawable;
    private final Profile_9_1_86_2432 profile;
    private final Map<View, Drawable.ConstantState> gradients = new WeakHashMap<>();
    private final Map<View, Integer> overlays = new WeakHashMap<>();
    private final Map<View, Boolean> pages = new WeakHashMap<>();
    private final Map<View, Drawable> pageBackgrounds = new WeakHashMap<>();
    private final Map<View, Integer> artworkBackdrops = new WeakHashMap<>();
    private final ThreadLocal<Boolean> applyingBackdrop = ThreadLocal.withInitial(() -> false);
    private final ThreadLocal<Boolean> applyingPageBackground = ThreadLocal.withInitial(() -> false);
    private boolean applying;
    private boolean observedArtworkBackdrop;
    private boolean reportedHiddenBackdrop;
    private boolean observedPageBackground;
    private boolean observedOverlayColor;
    private boolean observedGradient;

    public ExpandedBackgroundHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile)
            throws ReflectiveOperationException {
        this.runtime = runtime;
        this.profile = profile;
        overlayColor = (Method) profile.resolve("player.overlayColor");
        overlayDrawable = profile.field("player.overlayDrawable");
    }

    public void install() {
        try {
            XposedBridge.hookMethod(profile.resolve("player.overlay"), new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    View view = (View) param.thisObject;
                    captureGradient(view);
                    refreshGradient(view);
                }
            });
        } catch (ReflectiveOperationException failure) { ModuleLog.error("Player gradient constructor binding failed", failure); }
        XposedBridge.hookMethod(overlayColor, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (applying || !ViewScopes.expandedPlayer(view)) return;
                overlays.put(view, (Integer) param.args[0]);
                if (fixed()) param.args[0] = runtime.snapshot().palette.color("playerBackground");
                if (!observedOverlayColor) {
                    observedOverlayColor = true;
                    ModuleLog.info("Expanded player overlay color intercepted; resource="
                            + ViewScopes.resource(view) + " fixed=" + fixed());
                }
            }
        });
        XposedHelpers.findAndHookMethod(View.class, "onAttachedToWindow", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (OVERLAY_VIEW_CLASS.equals(view.getClass().getName()) && ViewScopes.expandedPlayer(view)) {
                    captureGradient(view);
                    refreshGradient(view);
                }
                if (isArtworkBackdrop(view) && ViewScopes.expandedPlayer(view)) {
                    if (!artworkBackdrops.containsKey(view)) artworkBackdrops.put(view, view.getVisibility());
                    observeArtworkBackdrop();
                    refreshArtworkBackdrop(view);
                }
                if (isExpandedPageBackground(view)) {
                    capturePageBackground(view, view.getBackground());
                    pages.put(view, true);
                    observePageBackground(view);
                    refreshPage(view);
                }
            }
        });
        XposedHelpers.findAndHookMethod(View.class, "setBackground", Drawable.class, new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (OVERLAY_VIEW_CLASS.equals(view.getClass().getName()) && ViewScopes.expandedPlayer(view)) {
                    captureGradient(view);
                    refreshGradient(view);
                }
                if (!applyingPageBackground.get() && pages.containsKey(view)) {
                    capturePageBackground(view, (Drawable) param.args[0]);
                    refreshPage(view);
                }
            }
        });
        XposedHelpers.findAndHookMethod(View.class, "setVisibility", int.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (applyingBackdrop.get() || !isArtworkBackdrop(view) || !ViewScopes.expandedPlayer(view)) return;
                artworkBackdrops.put(view, (Integer) param.args[0]);
                observeArtworkBackdrop();
                if (shouldHideArtworkBackdrop()) param.args[0] = View.INVISIBLE;
            }
        });
        runtime.addListener(() -> {
            for (View view : new ArrayList<>(gradients.keySet())) if (view != null) refreshGradient(view);
            for (View page : new ArrayList<>(pages.keySet())) if (page != null) refreshPage(page);
            for (View view : new ArrayList<>(artworkBackdrops.keySet()))
                if (view != null) refreshArtworkBackdrop(view);
            boolean previous = applying;
            applying = true;
            try {
                for (View view : new ArrayList<>(overlays.keySet())) {
                    if (view == null) continue;
                    try { overlayColor.invoke(view, fixed() ? runtime.snapshot().palette.color("playerBackground") : overlays.get(view)); }
                    catch (ReflectiveOperationException failure) { ModuleLog.error("Expanded overlay restore failed", failure); }
                }
            } finally { applying = previous; }
        });
        ModuleLog.info("Expanded player background hooks installed");
    }

    private boolean isArtworkBackdrop(View view) {
        if (view == null) return false;
        boolean expandedPage = ViewScopes.expandedPlayer(view)
                || ViewScopes.ancestor(view, "now_playing_container")
                && ViewScopes.ancestor(view, "fullscreen_container");
        if (!expandedPage) return false;
        // This preference controls Spotify's blurred backdrop only. Keep album art and video surfaces visible.
        return ARTWORK_BACKDROP.equals(ViewScopes.resource(view));
    }

    private void observeArtworkBackdrop() {
        if (observedArtworkBackdrop) return;
        observedArtworkBackdrop = true;
        ModuleLog.info("Expanded Spotify artwork backdrop target observed");
    }

    private void refreshArtworkBackdrop(View view) {
        Integer originalVisibility = artworkBackdrops.get(view);
        if (originalVisibility == null) return;
        int visibility = shouldHideArtworkBackdrop() ? View.INVISIBLE : originalVisibility;
        if (view.getVisibility() == visibility) return;
        applyingBackdrop.set(true);
        try { view.setVisibility(visibility); }
        finally { applyingBackdrop.set(false); }
        if (visibility == View.INVISIBLE && !reportedHiddenBackdrop) {
            reportedHiddenBackdrop = true;
            ModuleLog.info("Expanded Spotify artwork backdrop hidden by user setting");
        }
    }

    private void refreshGradient(View view) {
        Drawable.ConstantState original = gradients.get(view);
        if (original == null) return;
        GradientDrawable replacement = (GradientDrawable) original.newDrawable(view.getResources()).mutate();
        if (fixed()) {
            int background = runtime.snapshot().palette.color("background");
            if (Build.VERSION.SDK_INT >= 29) replacement.setColors(
                    new int[]{Color.TRANSPARENT, Color.TRANSPARENT, background}, new float[]{0f, .88f, 1f});
            else replacement.setColors(new int[]{Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, background});
        }
        try { overlayDrawable.set(view, replacement); view.invalidate(); }
        catch (IllegalAccessException failure) { ModuleLog.error("Player gradient restoration failed", failure); }
    }

    private void captureGradient(View view) {
        if (view == null || gradients.containsKey(view)) return;
        try {
            Object drawable = overlayDrawable.get(view);
            if (!(drawable instanceof GradientDrawable)) return;
            Drawable.ConstantState state = ((GradientDrawable) drawable).getConstantState();
            if (state == null) return;
            gradients.put(view, state.newDrawable(view.getResources()).mutate().getConstantState());
            if (!observedGradient) {
                observedGradient = true;
                ModuleLog.info("Expanded player gradient captured; resource=" + ViewScopes.resource(view));
            }
        } catch (IllegalAccessException failure) {
            ModuleLog.error("Player gradient capture failed", failure);
        }
    }

    private void capturePageBackground(View view, Drawable background) {
        pageBackgrounds.put(view, background);
    }

    private void refreshPage(View view) {
        if (view == null) return;
        ThemeRuntime.Snapshot snapshot = runtime.snapshot();
        if (!ArtworkBackdropPolicy.shouldHide(snapshot.enabled, snapshot.showArtworkBackdrop)) {
            Drawable original = pageBackgrounds.get(view);
            if (view.getBackground() != original) {
                applyingPageBackground.set(true);
                try { view.setBackground(original); }
                finally { applyingPageBackground.set(false); }
            }
            return;
        }
        int color = snapshot.palette.color("background");
        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable && ((ColorDrawable) background).getColor() == color) return;
        applyingPageBackground.set(true);
        try { view.setBackground(new ColorDrawable(color)); }
        finally { applyingPageBackground.set(false); }
    }

    private boolean isExpandedPageBackground(View view) {
        if (!ViewScopes.expandedPlayer(view)) return false;
        String resource = ViewScopes.resource(view);
        return "fullscreen_container".equals(resource)
                || "now_playing_container".equals(resource)
                || "overlay_controls_layout".equals(resource)
                || "revised_template_overlay".equals(resource)
                || view.getClass().getName().equals("p.tek0") && "content".equals(resource);
    }

    private void observePageBackground(View view) {
        if (observedPageBackground) return;
        observedPageBackground = true;
        Drawable background = view.getBackground();
        ModuleLog.info("Expanded player page background target observed; resource="
                + ViewScopes.resource(view) + " originalBackground="
                + (background == null ? "null" : background.getClass().getName()));
    }
    private boolean fixed() { return runtime.snapshot().enabled && runtime.snapshot().fixed; }
    private boolean shouldHideArtworkBackdrop() {
        ThemeRuntime.Snapshot snapshot = runtime.snapshot();
        return ArtworkBackdropPolicy.shouldHide(snapshot.enabled, snapshot.showArtworkBackdrop);
    }
}
