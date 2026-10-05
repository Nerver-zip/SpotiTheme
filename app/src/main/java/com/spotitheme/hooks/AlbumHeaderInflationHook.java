package com.spotitheme.hooks;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.spotitheme.theme.AlbumArtworkGradient;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Covers album artwork surfaces before the modern holder registers them. */
public final class AlbumHeaderInflationHook {
    private final ThemeRuntime runtime;
    private final Map<View, Original> surfaces = new WeakHashMap<>();
    private static final class Original {
        final Drawable.ConstantState background;
        final ColorStateList tint;
        Original(View view) {
            background = view.getBackground() == null ? null : view.getBackground().getConstantState();
            tint = view.getBackgroundTintList();
        }
    }

    public AlbumHeaderInflationHook(ThemeRuntime runtime) { this.runtime = runtime; }

    public void install() {
        XposedHelpers.findAndHookMethod(LayoutInflater.class, "inflate", int.class, ViewGroup.class,
                boolean.class, new XC_MethodHook() {
                    @Override protected void afterHookedMethod(MethodHookParam param) {
                        if (param.hasThrowable() || !(param.getResult() instanceof View)) return;
                        LayoutInflater inflater = (LayoutInflater) param.thisObject;
                        String name;
                        try { name = inflater.getContext().getResources().getResourceEntryName((Integer) param.args[0]); }
                        catch (android.content.res.Resources.NotFoundException ignored) { return; }
                        if (!"expanded_header".equals(name) && !"condensed_header".equals(name)) return;
                        View root = (View) param.getResult();
                        int id = root.getResources().getIdentifier("cwp_header_artwork_background", "id", "com.spotify.music");
                        View surface = id == 0 ? null : root.findViewById(id);
                        if (surface == null || surfaces.containsKey(surface)
                                || surface.getBackground() instanceof AlbumArtworkGradient) return;
                        if (surface.getBackground() != null && surface.getBackground().getConstantState() == null) return;
                        surfaces.put(surface, new Original(surface));
                        refresh(surface);
                    }
                });
        runtime.addListener(() -> {
            for (View surface : new ArrayList<>(surfaces.keySet())) if (surface != null) refresh(surface);
        });
    }

    private void refresh(View surface) {
        Original original = surfaces.get(surface);
        if (runtime.snapshot().animated) {
            surface.setBackgroundTintList(null);
            surface.setBackground(new AlbumArtworkGradient(0, 0, true));
        } else if (surface.getBackground() instanceof AlbumArtworkGradient) {
            surface.setBackground(original.background == null ? null
                    : original.background.newDrawable(surface.getResources()).mutate());
            surface.setBackgroundTintList(original.tint);
        }
    }
}
