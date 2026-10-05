package com.spotitheme.hooks;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Pinned mini-player transport drawables with reversible palette ownership. */
public final class MiniTransportHook {
    private final ThemeRuntime runtime;
    private final Field icon, glyphColors, glyphPaint, circleGlyph, circleColors, circlePaint;
    private final Method glyphSetter, circleState;
    private final Map<Drawable, Binding> bindings = new WeakHashMap<>();
    private boolean observed;

    private static final class Binding {
        final boolean circle;
        ColorStateList source, themed;
        int color;
        Binding(boolean circle) { this.circle = circle; }
    }

    public MiniTransportHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile)
            throws ReflectiveOperationException {
        this.runtime = runtime;
        icon = profile.field("glyph.icon");
        glyphColors = profile.field("glyph.colorState");
        glyphPaint = profile.field("glyph.paint");
        circleGlyph = profile.field("transport.circleGlyph");
        circleColors = profile.field("transport.circleColors");
        circlePaint = profile.field("transport.circlePaint");
        glyphSetter = (Method) profile.resolve("glyph.colorState");
        circleState = (Method) profile.resolve("transport.circleState");
    }

    public void install() {
        XposedHelpers.findAndHookMethod(ImageView.class, "onDraw", Canvas.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                ImageView view = (ImageView) param.thisObject;
                if (!ViewScopes.miniAncestor(view)) return;
                Drawable drawable = view.getDrawable();
                if (drawable == null) return;
                try {
                    if (!fixed()) { restore(drawable); return; }
                    View parent = view.getParent() instanceof View ? (View) view.getParent() : null;
                    if (ViewScopes.resource(parent).equals("play_pause_button")) {
                        if (drawable.getClass() == circleGlyph.getDeclaringClass()) {
                            Object glyph = circleGlyph.get(drawable);
                            if (!(glyph instanceof Drawable) || glyph.getClass() != glyphColors.getDeclaringClass()) return;
                            apply(drawable, true, runtime.snapshot().palette.color("accent"), false);
                            apply((Drawable) glyph, false, runtime.snapshot().palette.color("onAccent"), false);
                        } else if (drawable.getClass() == glyphColors.getDeclaringClass()) {
                            apply(drawable, false, runtime.snapshot().palette.color("text"), false);
                        }
                    } else if (drawable.getClass() == glyphColors.getDeclaringClass()) {
                        Object glyph = icon.get(drawable);
                        if (!(glyph instanceof Enum) || !((Enum<?>) glyph).name().equals("SKIP_FORWARD")) return;
                        int id = view.getResources().getIdentifier("np_content_desc_next", "string", "com.spotify.music");
                        if (id != 0 && android.text.TextUtils.equals(view.getContentDescription(), view.getResources().getString(id)))
                            apply(drawable, false, runtime.snapshot().palette.color("text"), true);
                    }
                } catch (ReflectiveOperationException failure) { ModuleLog.error("Mini transport update failed", failure); }
            }
        });
        runtime.addListener(() -> {
            // Remove old palette ownership before the next draw, including native/OFF.
            for (Drawable drawable : new ArrayList<>(bindings.keySet())) {
                if (drawable == null) continue;
                try { restore(drawable); drawable.invalidateSelf(); }
                catch (ReflectiveOperationException failure) { ModuleLog.error("Mini transport restore failed", failure); }
            }
        });
        ModuleLog.info("Mini-player transport hooks installed");
    }

    private void apply(Drawable drawable, boolean circle, int color, boolean selector)
            throws ReflectiveOperationException {
        Field colors = circle ? circleColors : glyphColors;
        ColorStateList current = (ColorStateList) colors.get(drawable);
        if (current == null) return;
        Binding binding = bindings.get(drawable);
        if (binding == null) { binding = new Binding(circle); bindings.put(drawable, binding); }
        if (current != binding.themed) binding.source = current;
        if (binding.themed == null || binding.color != color) {
            binding.color = color;
            binding.themed = selector ? new ColorStateList(new int[][] {
                {-android.R.attr.state_enabled}, {android.R.attr.state_pressed}, {}
            }, new int[] { alpha(color, .3f), alpha(color, .5f), color }) : ColorStateList.valueOf(color);
        }
        int expected = binding.themed.getColorForState(drawable.getState(), color);
        Paint paint = (Paint) (circle ? circlePaint : glyphPaint).get(drawable);
        // Stable setters avoid an invalidate-on-every-draw loop.
        if (current != binding.themed || paint.getColor() != expected) write(drawable, binding, binding.themed);
        if (!observed) { observed = true; ModuleLog.info("Mini-player transport tint observed"); }
    }

    private void write(Drawable drawable, Binding binding, ColorStateList colors) throws ReflectiveOperationException {
        if (binding.circle) {
            circleColors.set(drawable, colors);
            circleState.invoke(drawable, (Object) drawable.getState());
        } else glyphSetter.invoke(drawable, colors);
    }

    private void restore(Drawable drawable) throws ReflectiveOperationException {
        Binding binding = bindings.get(drawable);
        if (binding != null) {
            Object current = (binding.circle ? circleColors : glyphColors).get(drawable);
            if (current == binding.themed) write(drawable, binding, binding.source);
            bindings.remove(drawable);
        }
        if (circleGlyph.getDeclaringClass().isInstance(drawable)) {
            Object glyph = circleGlyph.get(drawable);
            if (glyph instanceof Drawable) restore((Drawable) glyph);
        }
    }

    private static int alpha(int color, float opacity) {
        return (color & 0xFFFFFF) | (Math.round((color >>> 24) * opacity) << 24);
    }
    private boolean fixed() { return runtime.snapshot().enabled && runtime.snapshot().fixed; }
}
