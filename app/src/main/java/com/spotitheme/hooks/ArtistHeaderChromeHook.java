package com.spotitheme.hooks;

import android.content.res.ColorStateList;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Image-header chrome is scoped by the back control in the shared holder. */
public final class ArtistHeaderChromeHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final Map<View, Boolean> roots = new WeakHashMap<>();
    private final Map<View, Drawable> backgrounds = new WeakHashMap<>();
    private final Map<TextView, ColorStateList> texts = new WeakHashMap<>();
    private final Map<ImageView, ColorFilter> filters = new WeakHashMap<>();
    public ArtistHeaderChromeHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) {
        this.runtime = runtime; this.profile = profile;
    }
    public void install() throws ReflectiveOperationException {
        Method getView = (Method) profile.resolve("artist.header.view");
        XC_MethodHook update = new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View root = (View) getView.invoke(param.thisObject);
                if (root == null || child(root, "back_button") == null) return;
                roots.put(root, true); refresh(root);
            }
        };
        XposedBridge.hookMethod(profile.resolve("artist.header"), update);
        XposedBridge.hookMethod(profile.resolve("artist.header.update"), update);
        runtime.addListener(() -> {
            for (View root : new ArrayList<>(roots.keySet())) if (root != null) refresh(root);
        });
        ModuleLog.info("Artist image-header chrome hooks installed");
    }
    private View child(View root, String name) {
        int id = root.getResources().getIdentifier(name, "id", "com.spotify.music");
        return id == 0 ? null : root.findViewById(id);
    }
    private void background(View view, boolean rounded) {
        if (view == null) return;
        if (!backgrounds.containsKey(view)) {
            Drawable source = view.getBackground();
            if (rounded && source != null && source.getConstantState() == null) return;
            backgrounds.put(view, source == null || source.getConstantState() == null ? source
                    : source.getConstantState().newDrawable(view.getResources()).mutate());
        }
        Drawable original = backgrounds.get(view), replacement = original;
        if (runtime.snapshot().enabled) {
            int color = runtime.snapshot().palette.color("albumHeaderBackground");
            if (!rounded) replacement = new ColorDrawable(color);
            else if (original != null && original.getConstantState() != null) {
                replacement = original.getConstantState().newDrawable(view.getResources()).mutate();
                replacement.setTint(color);
            }
        }
        view.setBackground(replacement);
    }
    private void text(View view) {
        if (view instanceof TextView) {
            TextView text = (TextView) view;
            if (!texts.containsKey(text)) texts.put(text, text.getTextColors());
            text.setTextColor(runtime.snapshot().enabled
                    ? ColorStateList.valueOf(runtime.snapshot().palette.color("text")) : texts.get(text));
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) text(group.getChildAt(i));
        }
    }
    private void refresh(View root) {
        background(root, false); background(child(root, "toolbar"), false);
        text(child(root, "toolbar_title")); background(child(root, "back_button_bg"), true);
        View back = child(root, "back_button");
        if (back instanceof ImageView) {
            ImageView icon = (ImageView) back;
            if (!filters.containsKey(icon)) filters.put(icon, icon.getColorFilter());
            if (runtime.snapshot().enabled) icon.setColorFilter(runtime.snapshot().palette.color("text"), PorterDuff.Mode.SRC_IN);
            else icon.setColorFilter(filters.get(icon));
        }
    }
}
