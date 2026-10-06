package com.spotitheme.hooks;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/** Keeps square-cover media clear while preserving the pinned video renderer's native treatment. */
public final class ExpandedBackgroundHook {
    private static final String OVERLAY =
            "com.spotify.nowplaying.uiusecases.overlay.OverlayHidingGradientBackgroundView";
    private static final String VIDEO = "com.spotify.betamax.player.VideoSurfaceView";
    private final ThemeRuntime runtime;
    private final Method overlayColor, videoSurface, videoTexture;
    private final Field overlayDrawable;
    private final Map<View, OverlayState> overlays = new WeakHashMap<>();
    private final Map<View, Drawable> mediaBackgrounds = new WeakHashMap<>();
    private boolean applying;
    private PlayerMediaPolicy.Mode reportedMode;

    private static final class OverlayState {
        Drawable.ConstantState nativeGradient;
        Integer nativeColor, appliedColor;
        PlayerMediaPolicy.Mode appliedMode;
        long generation = -1;
    }

    public ExpandedBackgroundHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile)
            throws ReflectiveOperationException {
        this.runtime = runtime;
        overlayColor = (Method) profile.resolve("player.overlayColor");
        overlayDrawable = profile.field("player.overlayDrawable");
        videoSurface = (Method) profile.resolve("player.videoSurface");
        videoTexture = (Method) profile.resolve("player.videoTexture");
    }

    public void install() {
        XposedBridge.hookMethod(overlayColor, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (applying || !ViewScopes.expandedPlayer(view)) return;
                OverlayState state = captureOverlay(view);
                if (state == null) return;
                state.nativeColor = (Integer) param.args[0];
                if (suppress(mode(view.getRootView()))) param.args[0] = Color.TRANSPARENT;
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (!applying && !param.hasThrowable()) refreshRoot(((View) param.thisObject).getRootView());
            }
        });
        XposedHelpers.findAndHookMethod(View.class, "onAttachedToWindow", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (!ViewScopes.expandedPlayer(view)) return;
                if (OVERLAY.equals(view.getClass().getName())) {
                    if (captureOverlay(view) == null) return;
                    view.getViewTreeObserver().addOnGlobalLayoutListener(() -> refreshRoot(view.getRootView()));
                } else if (mediaRoot(view) && !mediaBackgrounds.containsKey(view)) {
                    mediaBackgrounds.put(view, view.getBackground());
                    view.getViewTreeObserver().addOnGlobalLayoutListener(() -> refreshRoot(view.getRootView()));
                } else return;
                refreshRoot(view.getRootView());
            }
        });
        XposedHelpers.findAndHookMethod(View.class, "setBackground", Drawable.class, new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (applying) return;
                View view = (View) param.thisObject;
                if (mediaBackgrounds.containsKey(view)) {
                    mediaBackgrounds.put(view, (Drawable) param.args[0]);
                    refreshRoot(view.getRootView());
                } else if (overlays.containsKey(view) && param.args[0] instanceof GradientDrawable) {
                    overlays.get(view).nativeGradient = ((Drawable) param.args[0]).getConstantState();
                    overlays.get(view).generation = -1;
                    refreshRoot(view.getRootView());
                }
            }
        });
        XposedHelpers.findAndHookMethod(ImageView.class, "setImageDrawable", Drawable.class, new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (!applying && "image".equals(ViewScopes.resource(view))
                        && ViewScopes.expandedPlayer(view) && ViewScopes.ancestor(view, "music_container"))
                    refreshRoot(view.getRootView());
            }
        });
        XposedHelpers.findAndHookMethod(View.class, "setVisibility", int.class, new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (!applying && VIDEO.equals(view.getClass().getName())
                        && "video_surface".equals(ViewScopes.resource(view))
                        && ViewScopes.expandedPlayer(view) && ViewScopes.ancestor(view, "music_container"))
                    refreshRoot(view.getRootView());
            }
        });
        runtime.addListener(() -> {
            for (View view : new ArrayList<>(overlays.keySet()))
                if (view != null) refreshRoot(view.getRootView());
        });
        ModuleLog.info("Media-aware expanded background hooks installed");
    }

    private OverlayState captureOverlay(View view) {
        OverlayState existing = overlays.get(view);
        if (existing != null) return existing;
        try {
            Object drawable = overlayDrawable.get(view);
            if (!(drawable instanceof GradientDrawable)) return null;
            Drawable.ConstantState original = ((Drawable) drawable).getConstantState();
            if (original == null) return null;
            OverlayState state = new OverlayState();
            state.nativeGradient = original.newDrawable(view.getResources()).mutate().getConstantState();
            overlays.put(view, state);
            return state;
        } catch (IllegalAccessException failure) {
            ModuleLog.error("Player gradient capture failed", failure);
            return null;
        }
    }

    private static boolean mediaRoot(View view) {
        return "music_container".equals(ViewScopes.resource(view))
                && ViewScopes.ancestor(view, "track_carousel");
    }

    private PlayerMediaPolicy.Mode mode(View windowRoot) {
        PlayerMediaPolicy.Mode result = PlayerMediaPolicy.Mode.UNKNOWN;
        for (View media : new ArrayList<>(mediaBackgrounds.keySet())) {
            if (media == null || media.getRootView() != windowRoot || !activePage(media)) continue;
            View video = find(media, "video_surface");
            boolean boundVideo = video != null && VIDEO.equals(video.getClass().getName());
            View cover = find(media, "cover_art_container");
            View image = cover == null ? null : find(cover, "image");
            boolean bitmap = image instanceof ImageView
                    && ((ImageView) image).getDrawable() instanceof BitmapDrawable
                    && image.getVisibility() == View.VISIBLE;
            PlayerMediaPolicy.Mode candidate = PlayerMediaPolicy.classify(
                    cover != null, bitmap, boundVideo,
                    boundVideo && video.getVisibility() == View.VISIBLE && video.isShown(),
                    boundVideo && readyVideo(video));
            // A visible video or unresolved page must never be covered by another carousel page.
            if (candidate != PlayerMediaPolicy.Mode.STILL_ARTWORK) return candidate;
            result = candidate;
        }
        return result;
    }

    private static boolean activePage(View view) {
        if (!view.isShown() || view.getWidth() <= 0 || view.getHeight() <= 0 || view.getAlpha() <= 0f) return false;
        Rect visible = new Rect();
        return view.getGlobalVisibleRect(visible) && visible.width() * 4 >= view.getWidth() * 3;
    }

    private boolean readyVideo(View video) {
        try {
            SurfaceView surfaceView = (SurfaceView) videoSurface.invoke(video);
            if (surfaceView != null && surfaceView.isShown()) {
                Surface surface = surfaceView.getHolder().getSurface();
                if (surface != null && surface.isValid()) return true;
            }
            TextureView texture = (TextureView) videoTexture.invoke(video);
            return texture != null && texture.isShown() && texture.isAvailable();
        } catch (ReflectiveOperationException | RuntimeException failure) {
            return false;
        }
    }

    private static View find(View view, String resource) {
        if (resource.equals(ViewScopes.resource(view))) return view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View found = find(group.getChildAt(i), resource);
                if (found != null) return found;
            }
        }
        return null;
    }

    private boolean suppress(PlayerMediaPolicy.Mode mode) {
        return PlayerMediaPolicy.suppressBackdrop(runtime.snapshot().enabled, mode);
    }

    private void refreshRoot(View windowRoot) {
        if (applying || windowRoot == null) return;
        PlayerMediaPolicy.Mode mode = mode(windowRoot);
        ThemeRuntime.Snapshot snapshot = runtime.snapshot();
        boolean themed = suppress(mode);
        Integer nativeColor = null;
        for (Map.Entry<View, OverlayState> entry : new ArrayList<>(overlays.entrySet()))
            if (entry.getKey() != null && entry.getKey().getRootView() == windowRoot
                    && entry.getValue().nativeColor != null) nativeColor = entry.getValue().nativeColor;
        // Album-color mode keeps its native flat color; fixed/Auto Theme palettes remain independently selected.
        int background = snapshot.fixed || snapshot.autoTheme || nativeColor == null
                ? snapshot.palette.color("background") : nativeColor;
        applying = true;
        try {
            for (Map.Entry<View, Drawable> entry : new ArrayList<>(mediaBackgrounds.entrySet())) {
                View media = entry.getKey();
                if (media == null || media.getRootView() != windowRoot) continue;
                if (themed && activePage(media)) {
                    Drawable current = media.getBackground();
                    if (!(current instanceof ColorDrawable) || ((ColorDrawable) current).getColor() != background)
                        media.setBackground(new ColorDrawable(background));
                } else if (media.getBackground() != entry.getValue()) media.setBackground(entry.getValue());
            }
            for (Map.Entry<View, OverlayState> entry : new ArrayList<>(overlays.entrySet())) {
                View view = entry.getKey();
                OverlayState state = entry.getValue();
                if (view == null || view.getRootView() != windowRoot || state.nativeGradient == null) continue;
                PlayerMediaPolicy.Mode applied = themed ? mode : PlayerMediaPolicy.Mode.UNKNOWN;
                if (state.appliedMode == applied && state.generation == snapshot.generation
                        && Objects.equals(state.appliedColor, state.nativeColor)) continue;
                GradientDrawable replacement = (GradientDrawable) state.nativeGradient
                        .newDrawable(view.getResources()).mutate();
                if (themed) {
                    replacement.clearColorFilter();
                    replacement.setColors(new int[]{Color.TRANSPARENT, Color.TRANSPARENT});
                }
                // Spotify draws the View background, not just R0. Keep both references synchronized.
                overlayDrawable.set(view, replacement);
                view.setBackground(replacement);
                if (!themed && state.nativeColor != null) overlayColor.invoke(view, state.nativeColor);
                state.appliedMode = applied;
                state.generation = snapshot.generation;
                state.appliedColor = state.nativeColor;
            }
        } catch (ReflectiveOperationException failure) {
            ModuleLog.error("Player media background update failed", failure);
        } finally { applying = false; }
        if (reportedMode != mode) {
            reportedMode = mode;
            ModuleLog.info("Expanded player media mode=" + mode + " nativeBackdrop=" + !themed);
        }
    }
}
