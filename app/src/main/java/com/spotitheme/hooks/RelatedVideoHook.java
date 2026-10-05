package com.spotitheme.hooks;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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

/** Related-video card roles; thumbnails and duration backplates remain native. */
public final class RelatedVideoHook {
    private final ThemeRuntime runtime;
    private final Field glyphIcon, glyphColors;
    private final Method glyphSetter;
    private final Map<Drawable, Binding> overflow = new WeakHashMap<>();
    private static final class Binding { ColorStateList source, themed; int color; }

    public RelatedVideoHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) throws ReflectiveOperationException {
        this.runtime = runtime;
        glyphIcon = profile.field("glyph.icon");
        glyphColors = profile.field("glyph.colorState");
        glyphSetter = (Method) profile.resolve("glyph.colorState");
    }

    public void install() {
        XposedHelpers.findAndHookMethod(View.class, "draw", Canvas.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (!fixed() || !outerCard(view)) return;
                Drawable original = view.getBackground();
                if (original == null || original.getConstantState() == null) return;
                Drawable themed = original.getConstantState().newDrawable(view.getResources()).mutate();
                int color = runtime.snapshot().palette.color("surface");
                if (themed instanceof GradientDrawable) ((GradientDrawable) themed).setColor(color);
                else if (themed instanceof ColorDrawable) ((ColorDrawable) themed).setColor(color);
                else return;
                themed.setBounds(0, 0, view.getWidth(), view.getHeight());
                param.setObjectExtra("spotitheme.video.background", original);
                XposedHelpers.setObjectField(view, "mBackground", themed);
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                Drawable original = (Drawable) param.getObjectExtra("spotitheme.video.background");
                if (original != null) XposedHelpers.setObjectField(param.thisObject, "mBackground", original);
            }
        });
        XposedHelpers.findAndHookMethod(TextView.class, "onDraw", Canvas.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                TextView text = (TextView) param.thisObject;
                if (!runtime.snapshot().enabled || !ViewScopes.expandedPlayer(text)) return;
                String name = ViewScopes.resource(text);
                Integer role = null;
                if (fixed() && name.equals("title") && text.getParent() instanceof View && outerCard((View) text.getParent()))
                    role = runtime.snapshot().palette.color("text");
                else if (videoChild(text)) {
                    if (name.equals("duration_label") && ViewScopes.ancestor(text, "media_slot")) role = 0xFFFFFFFF;
                    else if (ViewScopes.ancestor(text, "title_subtitle_box")) {
                        if (name.equals("title")) role = fixed() ? runtime.snapshot().palette.color("text") : 0xFFFFFFFF;
                        else if (name.equals("subtitle")) role = fixed() ? runtime.snapshot().palette.color("textSubdued") : 0xFFB3B3B3;
                    }
                }
                if (role == null) return;
                int original = text.getCurrentTextColor();
                param.setObjectExtra("spotitheme.video.text", original);
                XposedHelpers.setIntField(text, "mCurTextColor", (role & 0xFFFFFF) | (((original >>> 24) * (role >>> 24) / 255) << 24));
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                Integer original = (Integer) param.getObjectExtra("spotitheme.video.text");
                if (original != null) XposedHelpers.setIntField(param.thisObject, "mCurTextColor", original);
            }
        });
        XposedHelpers.findAndHookMethod(ImageView.class, "onDraw", Canvas.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                ImageView view = (ImageView) param.thisObject;
                Drawable drawable = view.getDrawable();
                if (!videoChild(view) || !ViewScopes.resource(view).equals("button_context_menu")
                        || drawable == null || drawable.getClass() != glyphColors.getDeclaringClass()) return;
                try {
                    Object icon = glyphIcon.get(drawable);
                    if (!(icon instanceof Enum) || !((Enum<?>) icon).name().equals("MORE_ANDROID")) return;
                    if (!runtime.snapshot().enabled) { restore(drawable); return; }
                    ColorStateList current = (ColorStateList) glyphColors.get(drawable);
                    Binding binding = overflow.get(drawable);
                    if (binding == null) { binding = new Binding(); overflow.put(drawable, binding); }
                    if (current != binding.themed) binding.source = current;
                    int role = fixed() ? runtime.snapshot().palette.color("textSubdued") : 0xFFB3B3B3;
                    if (binding.themed == null || binding.color != role || current != binding.themed) {
                        binding.color = role;
                        binding.themed = roleColors(binding.source, role);
                    }
                    if (current != binding.themed) glyphSetter.invoke(drawable, binding.themed);
                } catch (ReflectiveOperationException failure) { ModuleLog.error("Video overflow tint failed", failure); }
            }
        });
        runtime.addListener(() -> {
            for (Drawable drawable : new ArrayList<>(overflow.keySet())) if (drawable != null) {
                try { restore(drawable); drawable.invalidateSelf(); }
                catch (ReflectiveOperationException failure) { ModuleLog.error("Video overflow restore failed", failure); }
            }
        });
        ModuleLog.info("Related-video card, metadata and overflow hooks installed");
    }

    private void restore(Drawable drawable) throws ReflectiveOperationException {
        Binding binding = overflow.get(drawable);
        if (binding == null) return;
        if (glyphColors.get(drawable) == binding.themed) glyphSetter.invoke(drawable, binding.source);
        overflow.remove(drawable);
    }
    private static ColorStateList roleColors(ColorStateList source, int role) {
        if (source == null) return ColorStateList.valueOf(role);
        int[] colors = ((int[]) XposedHelpers.getObjectField(source, "mColors")).clone();
        for (int i = 0; i < colors.length; i++) colors[i] = (role & 0xFFFFFF) | (((colors[i] >>> 24) * (role >>> 24) / 255) << 24);
        return new ColorStateList((int[][]) XposedHelpers.getObjectField(source, "mStateSpecs"), colors);
    }
    private static boolean videoChild(View view) {
        return ViewScopes.expandedPlayer(view) && ViewScopes.ancestor(view, "music_video_card_view_root")
                && ViewScopes.ancestor(view, "video_card_carousel_recycler_view");
    }
    private static boolean outerCard(View view) {
        if (!(view instanceof ViewGroup) || !ViewScopes.expandedPlayer(view)
                || !ViewScopes.resource(view).equals("root") || !ViewScopes.ancestor(view, "widgets_container")) return false;
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (!ViewScopes.resource(child).equals("content_container")) continue;
            int id = view.getResources().getIdentifier("video_card_carousel_recycler_view", "id", "com.spotify.music");
            return id != 0 && child.findViewById(id) != null;
        }
        return false;
    }
    private boolean fixed() { return runtime.snapshot().enabled && runtime.snapshot().fixed; }
}
