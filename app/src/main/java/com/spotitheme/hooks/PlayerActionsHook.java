package com.spotitheme.hooks;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.PlayerActionColors;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedBridge;
import android.content.Context;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Verified Shuffle, Queue, Share and header overflow glyph roles. */
public final class PlayerActionsHook {
    private final ThemeRuntime runtime;
    private final Field icon, colors;
    private final Method setter;
    private final Method selectorResolver;
    private final Map<Drawable, Binding> bindings = new WeakHashMap<>();
    private static final class Binding { ColorStateList source, themed; int neutral, accent; }

    public PlayerActionsHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) throws ReflectiveOperationException {
        this.runtime = runtime;
        icon = profile.field("glyph.icon"); colors = profile.field("glyph.colorState");
        setter = (Method) profile.resolve("glyph.colorState");
        selectorResolver = (Method) profile.resolve("row.colorStateResolver");
    }
    public void install() {
        XposedBridge.hookMethod(selectorResolver, new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                ThemeRuntime.Snapshot state = runtime.snapshot();
                if (!state.enabled || !state.fixed || param.hasThrowable() || !(param.getResult() instanceof ColorStateList)) return;
                Context context = (Context) param.args[0];
                int resource = context.getResources().getIdentifier("encore_accent_color", "color", "com.spotify.music");
                if (resource == 0 || (Integer) param.args[1] != resource) return;
                ColorStateList original = (ColorStateList) param.getResult();
                if (!original.isStateful()) return;
                int[][] states = {{android.R.attr.state_pressed}, {-android.R.attr.state_enabled}, {}};
                int[] colors = new int[states.length];
                int accent = state.palette.color("accent");
                for (int i = 0; i < colors.length; i++) {
                    int source = original.getColorForState(states[i], original.getDefaultColor());
                    colors[i] = (accent & 0xFFFFFF) | (((source >>> 24) * (accent >>> 24) / 255) << 24);
                }
                param.setResult(new ColorStateList(states, colors));
            }
        });
        XposedHelpers.findAndHookMethod(ImageView.class, "onDraw", Canvas.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                ImageView view = (ImageView) param.thisObject;
                Drawable drawable = view.getDrawable();
                if (!ViewScopes.expandedPlayer(view) || drawable == null || drawable.getClass() != colors.getDeclaringClass()) return;
                try {
                    Object value = icon.get(drawable);
                    if (!(value instanceof Enum)) return;
                    String glyph = ((Enum<?>) value).name();
                    boolean scoped = glyph.equals("SHUFFLE") && ViewScopes.ancestor(view, "playback_controls_container")
                            || glyph.equals("QUEUE") && ViewScopes.resource(view).equals("queue_button") && ViewScopes.ancestor(view, "accessory_row")
                            || glyph.equals("SHARE_ANDROID") && ViewScopes.ancestor(view, "accessory_row")
                            || glyph.equals("MORE_ANDROID") && ViewScopes.ancestor(view, "player_overlay_header");
                    if (!scoped) return;
                    ThemeRuntime.Snapshot state = runtime.snapshot();
                    if (!state.enabled || !state.fixed) { restore(drawable); return; }
                    ColorStateList current = (ColorStateList) colors.get(drawable);
                    if (current == null) return;
                    Binding binding = bindings.get(drawable);
                    if (binding == null) { binding = new Binding(); bindings.put(drawable, binding); }
                    boolean changed = current != binding.themed;
                    if (changed) binding.source = current;
                    int neutral = state.palette.color("text"), accent = state.palette.color("accent");
                    if (changed || binding.themed == null || binding.neutral != neutral || binding.accent != accent) {
                        binding.neutral = neutral; binding.accent = accent;
                        int[] mapped = ((int[]) XposedHelpers.getObjectField(binding.source, "mColors")).clone();
                        for (int i = 0; i < mapped.length; i++) mapped[i] = PlayerActionColors.themed(mapped[i], neutral, accent);
                        binding.themed = new ColorStateList((int[][]) XposedHelpers.getObjectField(binding.source, "mStateSpecs"), mapped);
                    }
                    if (current != binding.themed) setter.invoke(drawable, binding.themed);
                } catch (ReflectiveOperationException failure) { ModuleLog.error("Player action tint failed", failure); }
            }
        });
        runtime.addListener(() -> {
            for (Drawable drawable : new ArrayList<>(bindings.keySet())) if (drawable != null) {
                try { restore(drawable); drawable.invalidateSelf(); }
                catch (ReflectiveOperationException failure) { ModuleLog.error("Player action restore failed", failure); }
            }
        });
        ModuleLog.info("Expanded player action hooks installed");
    }
    private void restore(Drawable drawable) throws ReflectiveOperationException {
        Binding binding = bindings.get(drawable);
        if (binding == null) return;
        if (colors.get(drawable) == binding.themed) setter.invoke(drawable, binding.source);
        bindings.remove(drawable);
    }
}
