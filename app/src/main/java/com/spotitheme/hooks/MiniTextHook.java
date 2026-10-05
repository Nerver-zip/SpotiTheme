package com.spotitheme.hooks;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.TextView;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Mini-player title/artist colors, including the combined-line span renderer. */
public final class MiniTextHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private final Map<TextView, ColorStateList> originals = new WeakHashMap<>();
    private final Map<Object, String> spanRoles = new WeakHashMap<>();
    private final Map<Object, String> stickyRoles = new WeakHashMap<>();
    private final ThreadLocal<TextView> drawOwner = new ThreadLocal<>();
    private boolean applying;
    private boolean observed;

    public MiniTextHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) {
        this.runtime = runtime;
        this.profile = profile;
    }

    public void install() throws ReflectiveOperationException {
        hookSetter(int.class);
        hookSetter(ColorStateList.class);
        XposedHelpers.findAndHookMethod(View.class, "onAttachedToWindow", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (!(param.thisObject instanceof TextView)) return;
                TextView text = (TextView) param.thisObject;
                if (role(text) != null) { originals.putIfAbsent(text, text.getTextColors()); refresh(text); }
            }
        });
        XposedBridge.hookMethod(profile.resolve("mini.spanConstructor"), new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (param.hasThrowable()) return;
                Context context = (Context) param.args[0];
                int style = (Integer) param.args[1];
                if (style == 0) return;
                try {
                    String name = context.getResources().getResourceEntryName(style);
                    if (name.equals("TextAppearance.NowPlayingBar.SingleLineTitle")) spanRoles.put(param.thisObject, "text");
                    else if (name.equals("TextAppearance.NowPlayingBar.SingleLineSubtitle")) spanRoles.put(param.thisObject, "textSubdued");
                } catch (android.content.res.Resources.NotFoundException ignored) {}
            }
        });
        XposedBridge.hookAllConstructors(TextAppearanceSpan.class, new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (param.hasThrowable() || param.args.length < 2 || !(param.args[0] instanceof Context)
                        || !(param.args[1] instanceof Integer) || (Integer) param.args[1] == 0) return;
                try {
                    String name = ((Context) param.args[0]).getResources().getResourceEntryName((Integer) param.args[1]);
                    if (name.equals("TextAppearance.TrackViewConnect.Title")) stickyRoles.put(param.thisObject, "text");
                    else if (name.equals("TextAppearance.TrackViewConnect.Title.Light")) stickyRoles.put(param.thisObject, "textSubdued");
                } catch (android.content.res.Resources.NotFoundException ignored) {}
            }
        });
        XposedHelpers.findAndHookMethod(TextView.class, "onDraw", Canvas.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                param.setObjectExtra("spotitheme.text.previous", drawOwner.get());
                TextView owner = (TextView) param.thisObject;
                if (role(owner) == null) drawOwner.remove(); else drawOwner.set(owner);
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                TextView previous = (TextView) param.getObjectExtra("spotitheme.text.previous");
                if (previous == null) drawOwner.remove(); else drawOwner.set(previous);
            }
        });
        XposedBridge.hookMethod(profile.resolve("mini.spanDraw"), new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (param.hasThrowable() || !fixed() || drawOwner.get() == null) return;
                String role = spanRoles.get(param.thisObject);
                if (role != null && ownsSpan(drawOwner.get(), param.thisObject))
                    ((TextPaint) param.args[0]).setColor(runtime.snapshot().palette.color(role));
            }
        });
        XposedHelpers.findAndHookMethod(TextAppearanceSpan.class, "updateDrawState", TextPaint.class, new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (param.hasThrowable() || !fixed()) return;
                TextView owner = drawOwner.get();
                String role = stickyRoles.get(param.thisObject);
                if (owner == null || role == null || !ViewScopes.expandedPlayer(owner)
                        || !ViewScopes.ancestor(owner, "revised_template_sticky_header") || !ownsSpan(owner, param.thisObject)) return;
                TextPaint paint = (TextPaint) param.args[0];
                int color = runtime.snapshot().palette.color(role);
                int alpha = (paint.getColor() >>> 24) * (color >>> 24) / 255;
                paint.setColor((color & 0xFFFFFF) | (alpha << 24));
            }
        });
        runtime.addListener(() -> {
            for (TextView view : new ArrayList<>(originals.keySet())) if (view != null) refresh(view);
        });
        ModuleLog.info("Mini-player text and inline span hooks installed");
    }

    private void hookSetter(Class<?> type) {
        XposedHelpers.findAndHookMethod(TextView.class, "setTextColor", type, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                TextView view = (TextView) param.thisObject;
                String role = role(view);
                if (applying || role == null || param.args[0] == null) return;
                originals.put(view, type == int.class ? ColorStateList.valueOf((Integer) param.args[0])
                        : (ColorStateList) param.args[0]);
                if (fixed()) {
                    int color = runtime.snapshot().palette.color(role);
                    param.args[0] = type == int.class ? (Object) color : ColorStateList.valueOf(color);
                }
                param.setObjectExtra("spotitheme.text.internal", Boolean.TRUE);
                applying = true;
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (Boolean.TRUE.equals(param.getObjectExtra("spotitheme.text.internal"))) applying = false;
            }
        });
    }

    private void refresh(TextView view) {
        String role = role(view);
        ColorStateList original = originals.get(view);
        if (original == null) return;
        boolean previous = applying;
        applying = true;
        try {
            view.setTextColor(fixed() && role != null ? ColorStateList.valueOf(runtime.snapshot().palette.color(role)) : original);
            view.invalidate();
            if (fixed() && role != null && !observed) {
                observed = true;
                ModuleLog.info("Mini-player text color applied");
            }
        } finally { applying = previous; }
    }

    private String role(TextView view) {
        if (!ViewScopes.miniAncestor(view) && !ViewScopes.expandedPlayer(view)) return null;
        String name = ViewScopes.resource(view);
        if (name.equals("track_info_view_title")) return "text";
        if (name.equals("track_info_view_subtitle")) return "textSubdued";
        if (ViewScopes.expandedPlayer(view) && ViewScopes.ancestor(view, "player_overlay_header")) {
            if (name.equals("context_header_title")) return "textSubdued";
            if (name.equals("context_header_subtitle")) return "text";
        }
        return null;
    }
    private boolean fixed() { return runtime.snapshot().enabled && runtime.snapshot().fixed; }
    private static boolean ownsSpan(TextView owner, Object span) {
        return owner != null && owner.getText() instanceof Spanned && ((Spanned) owner.getText()).getSpanStart(span) >= 0;
    }
}
