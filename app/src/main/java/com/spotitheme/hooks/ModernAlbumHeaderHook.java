package com.spotitheme.hooks;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import com.spotitheme.theme.AlbumArtworkGradient;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Modern expanded/condensed album header and its separate artwork callback. */
public final class ModernAlbumHeaderHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final Map<View, Integer> roots = new WeakHashMap<>();
    private final Map<View, Drawable> backgrounds = new WeakHashMap<>();
    private final Map<View, ColorStateList> tints = new WeakHashMap<>();
    private final Map<TextView, ColorStateList> texts = new WeakHashMap<>();
    public ModernAlbumHeaderHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) {
        this.runtime = runtime; this.profile = profile;
    }
    public void install() throws ReflectiveOperationException {
        Method getView = (Method) profile.resolve("album.header.view");
        Field callbackOwner = profile.field("album.callbackOwner");
        XC_MethodHook update = new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View root = (View) getView.invoke(param.thisObject);
                if (root == null) return;
                if (!roots.containsKey(root)) roots.put(root, null);
                refresh(root);
            }
        };
        XposedBridge.hookMethod(profile.resolve("album.header"), update);
        XposedBridge.hookMethod(profile.resolve("album.header.update"), update);
        XposedBridge.hookMethod(profile.resolve("album.header.artworkCallback"), new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object owner = callbackOwner.get(param.thisObject);
                if (!getView.getDeclaringClass().isInstance(owner)) return;
                View root = (View) getView.invoke(owner);
                if (root == null) return;
                if (param.args[0] instanceof Integer) roots.put(root, (Integer) param.args[0]);
                else if (!roots.containsKey(root)) roots.put(root, null);
                refresh(root);
            }
        });
        runtime.addListener(() -> {
            for (View root : new ArrayList<>(roots.keySet())) if (root != null) refresh(root);
        });
        ModuleLog.info("Modern album header hooks installed");
    }
    private View child(View root, String name) {
        int id = root.getResources().getIdentifier(name, "id", "com.spotify.music");
        return id == 0 ? null : root.findViewById(id);
    }
    private void background(View view, Drawable replacement) {
        if (view == null) return;
        if (!backgrounds.containsKey(view)) {
            Drawable source = view.getBackground();
            backgrounds.put(view, source == null || source.getConstantState() == null ? source
                    : source.getConstantState().newDrawable(view.getResources()).mutate());
            tints.put(view, view.getBackgroundTintList());
        }
        view.setBackgroundTintList(runtime.snapshot().enabled ? null : tints.get(view));
        view.setBackground(runtime.snapshot().enabled ? replacement : backgrounds.get(view));
    }
    private void text(View view, String role) {
        if (view instanceof TextView) {
            TextView text = (TextView) view;
            if (!texts.containsKey(text)) texts.put(text, text.getTextColors());
            text.setTextColor(runtime.snapshot().enabled
                    ? ColorStateList.valueOf(runtime.snapshot().palette.color(role)) : texts.get(text));
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) text(group.getChildAt(i), role);
        }
    }
    private void refresh(View root) {
        ThemeRuntime.Snapshot state = runtime.snapshot();
        int end = state.palette.color("albumHeaderBackground");
        Integer extracted = roots.get(root);
        int start = state.fixed ? end : extracted == null ? state.palette.color("background") : extracted | 0xFF000000;
        background(child(root, "cwp_header_artwork_background"),
                new AlbumArtworkGradient(start, end, state.animated));
        background(root, new ColorDrawable(end));
        background(child(root, "toolbar"), new ColorDrawable(end));
        for (String name : new String[]{"cwp_header_title", "cwp_header_creatorsRow", "toolbar_title"}) text(child(root, name), "text");
        text(child(root, "cwp_header_metadataRow"), "textSubdued");
        text(child(root, "cwp_header_preTitle"), "accent");
    }
}
