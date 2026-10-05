package com.spotitheme.hooks;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import java.util.function.Consumer;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import android.view.View;
import android.widget.TextView;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.ArrayList;

/** Pinned entity attributes and cached row selectors preserve playback states. */
public final class TrackRowColorsHook {
    private final ThemeRuntime runtime;
    private final Consumer<View> registerRow;
    private final Profile_9_1_86_2432 profile;
    private final Map<TextView, ColorStateList> originalText = new WeakHashMap<>();
    private final Map<TextView, Boolean> rowTitles = new WeakHashMap<>();
    public TrackRowColorsHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile, Consumer<View> registerRow) {
        this.runtime = runtime; this.profile = profile; this.registerRow = java.util.Objects.requireNonNull(registerRow);
    }
    public void install(Context context) throws ReflectiveOperationException {
        Method binding = (Method) profile.resolve("entity.titleBinding");
        Class<?> titleClass = binding.getParameterTypes()[1];
        int base = id(context, "attr", "textBase"), subdued = id(context, "attr", "textSubdued");
        int accent = id(context, "attr", "textBrightAccent");
        XposedBridge.hookMethod(profile.resolve("entity.colorResolver"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (!runtime.snapshot().enabled || !titleClass.isInstance(param.args[0])) return;
                int attr = (Integer) param.args[1];
                if (base != 0 && attr == base) param.setResult(runtime.snapshot().palette.color("text"));
                else if (subdued != 0 && attr == subdued) param.setResult(runtime.snapshot().palette.color("textSubdued"));
                else if (accent != 0 && attr == accent) param.setResult(runtime.snapshot().palette.color("accent"));
            }
        });
        int title = id(context, "color", "encore_row_title"), subtitle = id(context, "color", "encore_row_subtitle");
        int placeholder = id(context, "color", "encore_placeholder_background");
        int icon = id(context, "color", "encore_placeholder_icon");
        XposedBridge.hookMethod(profile.resolve("row.colorStateResolver"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (!runtime.snapshot().enabled) return;
                int resource = (Integer) param.args[1];
                if (title != 0 && resource == title) param.setResult(colors("text", true));
                else if (subtitle != 0 && resource == subtitle) param.setResult(colors("textSubdued", false));
                else if (placeholder != 0 && resource == placeholder) param.setResult(colors("surface", false));
                else if (icon != 0 && resource == icon) param.setResult(colors("textSubdued", false));
            }
        });
        Field rowRoot = profile.field("row.root");
        int titleView = id(context, "id", "title"), subtitleView = id(context, "id", "subtitle");
        XposedBridge.hookMethod(profile.resolve("row.factory"), new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.hasThrowable() || param.getResult() == null) return;
                Object root = rowRoot.get(param.getResult());
                if (!(root instanceof View)) return;
                View row = (View) root;
                registerRow.accept(row);
                if (titleView != 0) remember(row.findViewById(titleView), true);
                if (subtitleView != 0) remember(row.findViewById(subtitleView), false);
            }
        });
        runtime.addListener(() -> {
            for (TextView text : new ArrayList<>(rowTitles.keySet())) if (text != null) refresh(text);
        });
        ModuleLog.info("Track entity attribute and row selector hooks installed");
    }
    private void remember(View view, boolean title) {
        if (!(view instanceof TextView)) return;
        TextView text = (TextView) view;
        if (!originalText.containsKey(text)) originalText.put(text, text.getTextColors());
        rowTitles.put(text, title);
        refresh(text);
    }
    private void refresh(TextView text) {
        boolean title = Boolean.TRUE.equals(rowTitles.get(text));
        text.setTextColor(runtime.snapshot().enabled ? colors(title ? "text" : "textSubdued", title) : originalText.get(text));
    }
    private int id(Context context, String type, String name) {
        return context.getResources().getIdentifier(name, type, "com.spotify.music");
    }
    private ColorStateList colors(String role, boolean accented) {
        int normal = runtime.snapshot().palette.color(role);
        int accent = accented ? runtime.snapshot().palette.color("accent") : normal;
        int disabled = (normal & 0xFFFFFF) | (Math.round(Color.alpha(normal) * .5f) << 24);
        return new ColorStateList(new int[][]{{-android.R.attr.state_enabled}, {android.R.attr.state_activated},
                {android.R.attr.state_selected}, {}}, new int[]{disabled, accent, accent, normal});
    }
}
