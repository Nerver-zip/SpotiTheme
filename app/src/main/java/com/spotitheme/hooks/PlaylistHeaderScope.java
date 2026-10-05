package com.spotitheme.hooks;

import android.view.LayoutInflater;
import android.view.View;
import com.spotitheme.ModuleLog;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.ref.WeakReference;

/** Captures the pinned playlist header layout rather than guessing Compose ancestry. */
public final class PlaylistHeaderScope {
    private volatile WeakReference<View> root = new WeakReference<>(null);

    public void install() {
        XposedBridge.hookAllMethods(LayoutInflater.class, "inflate", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (param.hasThrowable() || param.args.length == 0 || !(param.args[0] instanceof Integer)
                        || !(param.getResult() instanceof View)) return;
                View view = (View) param.getResult();
                int id = view.getResources().getIdentifier("playlist_header_layout", "layout", "com.spotify.music");
                if (id != 0 && (Integer) param.args[0] == id) root = new WeakReference<>(view);
            }
        });
        ModuleLog.info("Playlist header layout scope installed");
    }

    public boolean contains(View view) {
        View expected = root.get();
        if (expected == null) return false;
        for (View current = view; current != null;) {
            if (current == expected) return true;
            current = current.getParent() instanceof View ? (View) current.getParent() : null;
        }
        return false;
    }

    public View root() { return root.get(); }
}
