package com.spotitheme.hooks;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.spotitheme.ModuleLog;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Verified inactive Repeat selector only; selected/native treatments remain owned by Spotify. */
public final class RepeatIconHook {
    private final ThemeRuntime runtime;
    private final Map<ImageView, Binding> bindings = new WeakHashMap<>();
    private static final class Binding {
        WeakReference<Drawable> drawable;
        ColorFilter source;
        PorterDuffColorFilter themed;
        int color;
    }
    public RepeatIconHook(ThemeRuntime runtime) { this.runtime = runtime; }
    public void install() {
        XposedHelpers.findAndHookMethod(ImageView.class, "onDraw", Canvas.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                ImageView view = (ImageView) param.thisObject;
                Binding binding = bindings.get(view);
                Drawable drawable = view.getDrawable();
                boolean scoped = view instanceof ImageButton && ViewScopes.expandedPlayer(view)
                        && ViewScopes.ancestor(view, "playback_controls_container") && drawable instanceof StateListDrawable;
                int id = scoped ? view.getResources().getIdentifier("np_content_desc_repeat", "string", "com.spotify.music") : 0;
                scoped &= id != 0 && android.text.TextUtils.equals(view.getContentDescription(), view.getResources().getString(id));
                ThemeRuntime.Snapshot state = runtime.snapshot();
                if (!state.enabled || !state.fixed || !scoped || view.getImageTintList() != null) { restore(view); return; }
                if (binding != null && binding.drawable.get() != drawable) { restore(view); binding = null; }
                ColorFilter current = view.getColorFilter();
                if (binding == null) {
                    if (current != null) return;
                    binding = new Binding(); binding.drawable = new WeakReference<>(drawable); binding.source = current;
                    bindings.put(view, binding);
                } else if (current != binding.themed && current != binding.source) { bindings.remove(view); return; }
                int color = state.palette.color("text");
                if (binding.themed == null || binding.color != color) {
                    binding.color = color; binding.themed = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
                }
                if (view.getColorFilter() != binding.themed) view.setColorFilter(binding.themed);
            }
        });
        runtime.addListener(() -> {
            for (ImageView view : new ArrayList<>(bindings.keySet())) if (view != null) { restore(view); view.invalidate(); }
        });
        ModuleLog.info("Inactive Repeat selector hook installed; selected Repeat acceptance remains open");
    }
    private void restore(ImageView view) {
        Binding binding = bindings.remove(view);
        if (binding != null && view.getColorFilter() == binding.themed) view.setColorFilter(binding.source);
    }
}
