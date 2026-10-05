package com.spotitheme.theme;

import android.content.res.ColorStateList;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import de.robv.android.xposed.XposedHelpers;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Framework drawable and selector migration; custom Spotify fields use exact feature bindings. */
public final class NativePaletteRefresh {
    private final Field[] artworkFields;
    private final Field[] overlayColors;
    private final Method overlayStateChange;
    private final Field glyphColors, circleColors, circleGlyph;
    private final Method glyphSetter, circleStateChange;
    public NativePaletteRefresh(Field[] artworkFields, Field[] overlayColors, Method overlayStateChange,
            Field glyphColors, Method glyphSetter, Field circleColors, Field circleGlyph, Method circleStateChange) {
        this.artworkFields = artworkFields.clone();
        this.overlayColors = overlayColors.clone();
        this.overlayStateChange = overlayStateChange;
        this.glyphColors = glyphColors; this.glyphSetter = glyphSetter;
        this.circleColors = circleColors; this.circleGlyph = circleGlyph;
        this.circleStateChange = circleStateChange;
    }
    private final Set<Drawable> visited = Collections.newSetFromMap(new IdentityHashMap<>());

    public void refresh(View view, PaletteColors old, PaletteColors next) {
        drawable(view.getBackground(), old, next, false);
        drawable(view.getForeground(), old, next, true);
        ColorStateList backgroundTint = colors(view.getBackgroundTintList(), old, next, false);
        if (backgroundTint != view.getBackgroundTintList()) view.setBackgroundTintList(backgroundTint);
        ColorStateList foregroundTint = colors(view.getForegroundTintList(), old, next, true);
        if (foregroundTint != view.getForegroundTintList()) view.setForegroundTintList(foregroundTint);
        if (view instanceof TextView) {
            TextView text = (TextView) view;
            ColorStateList color = colors(text.getTextColors(), old, next, true);
            if (color != text.getTextColors()) text.setTextColor(color);
            color = colors(text.getHintTextColors(), old, next, true);
            if (color != text.getHintTextColors()) text.setHintTextColor(color);
            color = colors(text.getLinkTextColors(), old, next, true);
            if (color != text.getLinkTextColors()) text.setLinkTextColor(color);
            color = colors(text.getCompoundDrawableTintList(), old, next, true);
            if (color != text.getCompoundDrawableTintList()) text.setCompoundDrawableTintList(color);
            for (Drawable icon : text.getCompoundDrawablesRelative()) drawable(icon, old, next, true);
        }
        if (view instanceof ImageView) {
            ImageView image = (ImageView) view;
            for (Field field : artworkFields) {
                if (!field.getDeclaringClass().isInstance(image)) continue;
                try { drawable((Drawable) field.get(image), old, next, false); }
                catch (IllegalAccessException failure) { throw new IllegalStateException(failure); }
            }
            drawable(image.getDrawable(), old, next, true);
            ColorStateList tint = colors(image.getImageTintList(), old, next, true);
            if (tint != image.getImageTintList()) image.setImageTintList(tint);
            if (image.getColorFilter() instanceof PorterDuffColorFilter) {
                PorterDuffColorFilter filter = (PorterDuffColorFilter) image.getColorFilter();
                int color = XposedHelpers.getIntField(filter, "mColor");
                int mapped = old.map(color, next, true);
                if (mapped != color) image.setColorFilter(mapped, (PorterDuff.Mode) XposedHelpers.getObjectField(filter, "mMode"));
            }
        }
        view.invalidate();
    }

    private ColorStateList colors(ColorStateList list, PaletteColors old, PaletteColors next, boolean foreground) {
        if (list == null) return null;
        int[] values = ((int[]) XposedHelpers.getObjectField(list, "mColors")).clone();
        boolean changed = false;
        for (int i = 0; i < values.length; i++) {
            int mapped = old.map(values[i], next, foreground);
            changed |= mapped != values[i];
            values[i] = mapped;
        }
        return changed ? new ColorStateList((int[][]) XposedHelpers.getObjectField(list, "mStateSpecs"), values) : list;
    }

    private void drawable(Drawable drawable, PaletteColors old, PaletteColors next, boolean foreground) {
        if (drawable == null || !visited.add(drawable)) return;

        if (drawable instanceof BitmapDrawable) return;
        try {
            if (glyphColors.getDeclaringClass().isInstance(drawable)) {
                ColorStateList original = (ColorStateList) glyphColors.get(drawable);
                ColorStateList replacement = colors(original, old, next, true);
                if (replacement != original) glyphSetter.invoke(drawable, replacement);
            }
            if (circleColors.getDeclaringClass().isInstance(drawable)) {
                ColorStateList original = (ColorStateList) circleColors.get(drawable);
                ColorStateList replacement = colors(original, old, next, false);
                if (replacement != original) {
                    circleColors.set(drawable, replacement);
                    circleStateChange.invoke(drawable, (Object) drawable.getState());
                    drawable.invalidateSelf();
                }
                drawable((Drawable) circleGlyph.get(drawable), old, next, true);
            }
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Pinned Encore drawable migration failed", failure);
        }
        if (overlayStateChange.getDeclaringClass().isInstance(drawable)) {
            Drawable.ConstantState state = drawable.getConstantState();
            boolean changed = false;
            for (Field field : overlayColors) {
                if (!field.getDeclaringClass().isInstance(state)) continue;
                try {
                    ColorStateList original = (ColorStateList) field.get(state);
                    changed |= colors(original, old, next, foreground) != original;
                } catch (IllegalAccessException failure) { throw new IllegalStateException(failure); }
            }
            if (changed) {
                drawable.mutate();
                state = drawable.getConstantState();
                try {
                    for (Field field : overlayColors) {
                        if (!field.getDeclaringClass().isInstance(state)) continue;
                        ColorStateList original = (ColorStateList) field.get(state);
                        field.set(state, colors(original, old, next, foreground));
                    }
                    overlayStateChange.invoke(drawable, (Object) drawable.getState());
                    drawable.invalidateSelf();
                } catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
            }
        }
        if (drawable instanceof ColorDrawable) {
            ColorDrawable solid = (ColorDrawable) drawable;
            int mapped = old.map(solid.getColor(), next, foreground);
            if (mapped != solid.getColor()) solid.setColor(mapped);
        } else if (drawable instanceof GradientDrawable) {
            GradientDrawable gradient = (GradientDrawable) drawable;
            int[] stops = gradient.getColors();
            if (stops != null) {

                int end = stops.length - 1;
                int mapped = old.map(stops[end], next, false);
                if (mapped != stops[end]) {
                    stops = stops.clone();
                    stops[end] = mapped;
                    if (Build.VERSION.SDK_INT >= 29) {
                        Object state = XposedHelpers.getObjectField(gradient, "mGradientState");
                        float[] positions = (float[]) XposedHelpers.getObjectField(state, "mPositions");
                        gradient.setColors(stops, positions);
                    } else gradient.setColors(stops);
                }
            } else {
                ColorStateList fill = colors(gradient.getColor(), old, next, foreground);
                if (fill != gradient.getColor()) gradient.setColor(fill);
            }
        }
        if (drawable instanceof RippleDrawable) {
            RippleDrawable ripple = (RippleDrawable) drawable;
            Object state = XposedHelpers.getObjectField(ripple, "mState");
            ColorStateList color = (ColorStateList) XposedHelpers.getObjectField(state, "mColor");
            ripple.setColor(colors(color, old, next, true));
        }
        if (drawable instanceof LayerDrawable) {
            LayerDrawable layers = (LayerDrawable) drawable;
            for (int i = 0; i < layers.getNumberOfLayers(); i++) drawable(layers.getDrawable(i), old, next, foreground);
        } else if (drawable instanceof StateListDrawable && Build.VERSION.SDK_INT >= 29) {
            StateListDrawable states = (StateListDrawable) drawable;
            for (int i = 0; i < states.getStateCount(); i++) drawable(states.getStateDrawable(i), old, next, foreground);
        } else if (drawable.getCurrent() != drawable) drawable(drawable.getCurrent(), old, next, foreground);
    }

}
