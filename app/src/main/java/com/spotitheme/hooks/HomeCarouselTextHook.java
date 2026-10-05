package com.spotitheme.hooks;

import android.graphics.Canvas;
import android.graphics.Color;
import android.widget.TextView;
import com.spotitheme.ModuleLog;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/** Draw-only foreground override retains native selectors, spans and opacity. */
public final class HomeCarouselTextHook {
    private final ThemeRuntime runtime;
    public HomeCarouselTextHook(ThemeRuntime runtime) { this.runtime = runtime; }

    public void install() {
        XposedHelpers.findAndHookMethod(TextView.class, "onDraw", Canvas.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                if (!runtime.snapshot().enabled) return;
                TextView text = (TextView) param.thisObject;
                String name = ViewScopes.resource(text);
                if (!("title".equals(name) || "pre_title".equals(name) || "subtitle".equals(name))
                        || !"com.spotify.encoremobile.component.textview.EncoreTextView".equals(text.getClass().getName())
                        || !ViewScopes.ancestor(text, "card_root")
                        || !ViewScopes.ancestor(text, "carousel")
                        || !ViewScopes.ancestor(text, "funkis_subfeed_framelayout")) return;
                int original = text.getCurrentTextColor();
                int role = runtime.snapshot().palette.color("title".equals(name) ? "text" : "textSubdued");
                int alpha = Color.alpha(original) * Color.alpha(role) / 255;
                param.setObjectExtra("spotitheme.carousel.original", original);
                XposedHelpers.setIntField(text, "mCurTextColor", (role & 0xFFFFFF) | (alpha << 24));
            }
            @Override protected void afterHookedMethod(MethodHookParam param) {
                Integer original = (Integer) param.getObjectExtra("spotitheme.carousel.original");
                if (original != null) XposedHelpers.setIntField(param.thisObject, "mCurTextColor", original);
            }
        });
        ModuleLog.info("Home carousel draw-time text hook installed");
    }
}
