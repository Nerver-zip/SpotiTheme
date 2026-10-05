package com.spotitheme.hooks;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.spotitheme.theme.NativePaletteRefresh;
import com.spotitheme.theme.PaletteColors;
import com.spotitheme.theme.ThemePalette;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Collections;
import java.util.List;

/** Captures framework views' palette generation for migration before owner refreshes. */
public final class NativeViewPaletteHook {
    private final ThemeRuntime runtime;
    private final PaletteColors nativeColors;
    private final Map<View, PaletteColors> views = Collections.synchronizedMap(new WeakHashMap<>());
    private volatile PaletteColors currentColors;
    private final Field[] artworkFields;
    private final Field[] overlayColors;
    private final Method overlayStateChange;
    private final Field glyphColors, circleColors, circleGlyph;
    private final Method glyphSetter, circleStateChange;

    public NativeViewPaletteHook(ThemeRuntime runtime, ThemePalette nativePalette, Profile_9_1_86_2432 profile)
            throws ReflectiveOperationException {
        this.runtime = runtime;
        nativeColors = PaletteColors.from(nativePalette);
        currentColors = colors();
        artworkFields = new Field[]{profile.field("artwork.encorePlaceholder"), profile.field("artwork.encoreOverlay"),
                profile.field("artwork.encoreLayers"), profile.field("artwork.creativePlaceholder"),
                profile.field("artwork.creativeOverlay")};
        overlayColors = new Field[]{profile.field("artwork.overlayFill"), profile.field("artwork.overlayStroke"),
                profile.field("artwork.overlayTint")};
        overlayStateChange = (Method) profile.resolve("artwork.overlayStateChange");
        glyphColors = profile.field("glyph.colorState");
        glyphSetter = (Method) profile.resolve("glyph.colorState");
        circleColors = profile.field("transport.circleColors");
        circleGlyph = profile.field("transport.circleGlyph");
        circleStateChange = (Method) profile.resolve("transport.circleState");
    }

    public void install() {
        XposedHelpers.findAndHookConstructor(View.class, Context.class, AttributeSet.class, int.class, int.class,
                new XC_MethodHook() {
                    @Override protected void afterHookedMethod(MethodHookParam param) {
                        views.put((View) param.thisObject, currentColors);
                    }
                });
        runtime.addListener(this::refresh);
        ModuleLog.info("Native framework view palette tracking installed");
    }

    private PaletteColors colors() {
        return runtime.snapshot().enabled ? PaletteColors.from(runtime.snapshot().palette) : nativeColors;
    }

    private void refresh() {
        PaletteColors next = colors();
        currentColors = next;
        NativePaletteRefresh refresh = new NativePaletteRefresh(artworkFields, overlayColors, overlayStateChange,
                glyphColors, glyphSetter, circleColors, circleGlyph, circleStateChange);
        List<View> captured;
        synchronized (views) { captured = new ArrayList<>(views.keySet()); }
        for (View view : captured) {
            if (view == null) continue;
            PaletteColors previous = views.get(view);
            if (previous == null) continue;
            try {
                refresh.refresh(view, previous, next);
                views.put(view, next);
            } catch (Throwable failure) {
                ModuleLog.error("Native palette refresh failed for " + view.getClass().getName(), failure);
            }
        }
    }
}
