package com.spotitheme.hooks;

import android.graphics.Color;
import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.BiConsumer;

/** Observes completed player state using pinned accessors; retains no player model. */
public final class TrackArtworkHook {
    private final Profile_9_1_86_2432 profile;
    private final BiConsumer<String, Integer> listener;

    public TrackArtworkHook(Profile_9_1_86_2432 profile, BiConsumer<String, Integer> listener) {
        this.profile = profile; this.listener = listener;
    }

    public void install() throws ReflectiveOperationException {
        Method track = (Method) profile.resolve("artwork.currentTrack");
        Method present = (Method) profile.resolve("artwork.trackPresent");
        Method value = (Method) profile.resolve("artwork.trackValue");
        Method metadata = (Method) profile.resolve("artwork.trackMetadata");
        XposedBridge.hookMethod(profile.resolve("artwork.playerStateBuild"), new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (param.hasThrowable() || !track.getDeclaringClass().isInstance(param.getResult())) return;
                try {
                    Object optional = track.invoke(param.getResult());
                    if (!Boolean.TRUE.equals(present.invoke(optional))) {
                        listener.accept(null, null);
                        return;
                    }
                    Object current = value.invoke(optional);
                    if (!metadata.getDeclaringClass().isInstance(current)) return;
                    Object result = metadata.invoke(current);
                    if (!(result instanceof Map)) return;
                    Map<?, ?> fields = (Map<?, ?>) result;
                    Object image = fields.get("image_url");
                    String url = image instanceof String && !((String) image).trim().isEmpty()
                            ? (String) image : null;
                    Integer fallback = null;
                    Object encoded = fields.get("extracted_color");
                    if (encoded instanceof String && !((String) encoded).isEmpty()) {
                        String color = (String) encoded;
                        try { fallback = Color.parseColor(color.startsWith("#") ? color : "#" + color); }
                        catch (IllegalArgumentException ignored) { }
                    }
                    listener.accept(url, fallback);
                } catch (ReflectiveOperationException failure) {
                    ModuleLog.error("Track artwork metadata read failed", failure);
                }
            }
        });
        ModuleLog.info("Pinned track artwork observer installed");
    }
}
