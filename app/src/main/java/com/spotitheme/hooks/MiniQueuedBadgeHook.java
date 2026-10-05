package com.spotitheme.hooks;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.widget.ImageView;
import com.spotitheme.ModuleLog;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Alternate queued badge: retained native filter and stable themed replacement. */
public final class MiniQueuedBadgeHook {
    private final ThemeRuntime runtime;
    private final Map<ImageView, Binding> bindings = new WeakHashMap<>();
    private boolean observed;
    private static final class Binding {
        ColorFilter source;
        PorterDuffColorFilter themed;
        int color;
    }
    public MiniQueuedBadgeHook(ThemeRuntime runtime) { this.runtime = runtime; }

    public void install() {
        XposedHelpers.findAndHookMethod(ImageView.class, "onDraw", Canvas.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                ImageView view = (ImageView) param.thisObject;
                if (!view.getClass().getName().equals("com.spotify.encoreconsumermobile.elements.badge.queued.QueuedBadgeView")
                        || !ViewScopes.resource(view).equals("track_info_view_queued_badge")
                        || !ViewScopes.miniAncestor(view)) return;
                ThemeRuntime.Snapshot state = runtime.snapshot();
                if (!state.enabled || !state.fixed) { restore(view); return; }
                Binding binding = bindings.get(view);
                if (binding == null) { binding = new Binding(); bindings.put(view, binding); }
                if (view.getColorFilter() != binding.themed) binding.source = view.getColorFilter();
                int color = state.palette.color("accent");
                if (binding.themed == null || binding.color != color) {
                    binding.color = color;
                    binding.themed = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
                }
                if (view.getColorFilter() != binding.themed) view.setColorFilter(binding.themed);
                if (!observed) { observed = true; ModuleLog.info("Mini-player queued badge tint observed"); }
            }
        });
        runtime.addListener(() -> {
            for (ImageView view : new ArrayList<>(bindings.keySet())) if (view != null) { restore(view); view.invalidate(); }
        });
        ModuleLog.info("Mini-player queued badge hook installed; natural positive sample remains required");
    }

    private void restore(ImageView view) {
        Binding binding = bindings.remove(view);
        if (binding != null && view.getColorFilter() == binding.themed) view.setColorFilter(binding.source);
    }
}
