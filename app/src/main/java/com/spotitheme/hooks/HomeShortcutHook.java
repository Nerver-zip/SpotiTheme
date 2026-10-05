package com.spotitheme.hooks;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.spotitheme.ModuleLog;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Resource-owned shortcut cards without inspecting Spotify holder members. */
public final class HomeShortcutHook {
    private final ThemeRuntime runtime;
    private final Map<View, ColorStateList> cards = new WeakHashMap<>();
    private final Map<TextView, Boolean> titles = new WeakHashMap<>();
    private boolean replaying;

    public HomeShortcutHook(ThemeRuntime runtime) { this.runtime = runtime; }

    public void install() {
        XposedHelpers.findAndHookMethod(LayoutInflater.class, "inflate", int.class, ViewGroup.class,
                boolean.class, new XC_MethodHook() {
                    @Override protected void afterHookedMethod(MethodHookParam param) {
                        if (param.hasThrowable() || !(param.getResult() instanceof View)) return;
                        LayoutInflater inflater = (LayoutInflater) param.thisObject;
                        try {
                            if (!"shortcut_card".equals(inflater.getContext().getResources()
                                    .getResourceEntryName((Integer) param.args[0]))) return;
                        } catch (android.content.res.Resources.NotFoundException ignored) { return; }
                        View result = (View) param.getResult();
                        ViewGroup parent = (ViewGroup) param.args[1];
                        boolean attached = Boolean.TRUE.equals(param.args[2]) && parent != null && result == parent;
                        View card = attached ? (parent.getChildCount() == 0 ? null
                                : parent.getChildAt(parent.getChildCount() - 1)) : result;
                        if (card == null || cards.containsKey(card)) return;
                        cards.put(card, card.getBackgroundTintList());
                        int id = card.getResources().getIdentifier("title", "id", "com.spotify.music");
                        View title = id == 0 ? null : card.findViewById(id);
                        if (title instanceof TextView) titles.put((TextView) title, Boolean.TRUE);
                        refresh(card);
                    }
                });
        XposedHelpers.findAndHookMethod(View.class, "setBackgroundTintList", ColorStateList.class,
                new XC_MethodHook() {
                    @Override protected void beforeHookedMethod(MethodHookParam param) {
                        View card = (View) param.thisObject;
                        if (replaying || !cards.containsKey(card)) return;
                        cards.put(card, (ColorStateList) param.args[0]);
                        if (runtime.snapshot().enabled)
                            param.args[0] = ColorStateList.valueOf(runtime.snapshot().palette.color("tinted"));
                    }
                });
        XposedHelpers.findAndHookMethod(TextView.class, "onDraw", Canvas.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                TextView title = (TextView) param.thisObject;
                if (!runtime.snapshot().enabled || !titles.containsKey(title)) return;
                int original = title.getCurrentTextColor();
                int role = runtime.snapshot().palette.color("text");
                param.setObjectExtra("spotitheme.shortcut.original", original);
                XposedHelpers.setIntField(title, "mCurTextColor", (role & 0xFFFFFF)
                        | ((Color.alpha(original) * Color.alpha(role) / 255) << 24));
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                Integer original = (Integer) param.getObjectExtra("spotitheme.shortcut.original");
                if (original != null) XposedHelpers.setIntField(param.thisObject, "mCurTextColor", original);
            }
        });
        runtime.addListener(() -> {
            for (View card : new ArrayList<>(cards.keySet())) if (card != null) refresh(card);
            for (TextView title : new ArrayList<>(titles.keySet())) if (title != null) title.invalidate();
        });
        ModuleLog.info("Home shortcut resource hooks installed");
    }

    private void refresh(View card) {
        try {
            replaying = true;
            card.setBackgroundTintList(runtime.snapshot().enabled
                    ? ColorStateList.valueOf(runtime.snapshot().palette.color("tinted")) : cards.get(card));
        } finally { replaying = false; }
    }
}
