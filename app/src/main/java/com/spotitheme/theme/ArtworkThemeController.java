package com.spotitheme.theme;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import com.spotitheme.ModuleLog;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Background artwork extraction with main-thread configuration and publication. */
public final class ArtworkThemeController {
    private final ThemeRuntime runtime;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "SpotiTheme-artwork");
        thread.setDaemon(true);
        return thread;
    });
    private final ArtworkRequestGate gate = new ArtworkRequestGate();
    private ThemePalette selected;
    private boolean active, auto;
    private String mode = "neutral", artwork;
    private Integer fallback;
    private Future<?> pending;

    public ArtworkThemeController(ThemeRuntime runtime) {
        this.runtime = runtime;
        selected = runtime.snapshot().palette;
    }

    public void configure(ThemePalette palette, boolean enabled, boolean fixed, boolean auto, String mode) {
        if (palette == null || !("light".equals(mode) || "dark".equals(mode) || "neutral".equals(mode)))
            throw new IllegalArgumentException("A valid palette and artwork mode are required");
        main.post(() -> {
            gate.invalidate();
            if (pending != null) pending.cancel(true);
            selected = palette;
            this.mode = mode;
            this.auto = auto;
            active = enabled && !fixed && auto;
            ModuleLog.info("Artwork mode configured; enabled=" + enabled + " fixed=" + fixed
                    + " auto=" + auto);
            runtime.apply(palette, enabled, fixed);
            if (active) request();
        });
    }

    /** Retain only the latest artwork reference, including while Auto Theme is disabled. */
    public void observe(String url, Integer color) {
        main.post(() -> {
            if (Objects.equals(artwork, url) && Objects.equals(fallback, color)) return;
            artwork = url; fallback = color;
            ModuleLog.info("Track artwork observed; image=" + (url != null) + " fallback=" + (color != null));
            gate.invalidate();
            if (pending != null) pending.cancel(true);
            if (active) request();
        });
    }

    private void request() {
        String url = normalize(artwork);
        Integer backup = fallback;
        ArtworkRequestGate.Request request = gate.begin(url);
        if (url == null) {
            if (backup != null) publish(request, backup);
            return;
        }
        pending = worker.submit(() -> {
            Integer extracted = null;
            try { extracted = extract(url); }
            catch (Exception failure) { ModuleLog.error("Artwork extraction failed", failure); }
            if (Thread.currentThread().isInterrupted()) return;
            Integer result = extracted;
            if (result != null || backup != null)
                main.post(() -> publish(request, result == null ? backup : result));
        });
    }

    private void publish(ArtworkRequestGate.Request request, int color) {
        gate.publish(request, () -> {
            if (active && auto) {
                runtime.apply(ArtworkPaletteGenerator.generate(selected, color, mode), true, false);
                ModuleLog.info("Auto Theme palette applied; color=" + Integer.toHexString(color) + " mode=" + mode);
            }
        });
    }

    private static String normalize(String url) {
        if (url == null) return null;
        url = url.trim();
        if (url.startsWith("spotify:image:") && url.length() > 14)
            return "https://i.scdn.co/image/" + url.substring(14);
        return url.startsWith("https://") || url.startsWith("http://") ? url : null;
    }

    private static int extract(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        Bitmap decoded = null, sample = null;
        try {
            connection.setConnectTimeout(6000);
            connection.setReadTimeout(8000);
            connection.setRequestProperty("User-Agent", "SpotiTheme/ArtworkTheme");
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) throw new java.io.IOException("Artwork HTTP " + status);
            try (InputStream input = connection.getInputStream()) { decoded = BitmapFactory.decodeStream(input); }
            if (decoded == null) throw new java.io.IOException("Artwork could not be decoded");
            float scale = Math.min(1f, 64f / Math.max(decoded.getWidth(), decoded.getHeight()));
            sample = Bitmap.createScaledBitmap(decoded, Math.max(1, Math.round(decoded.getWidth() * scale)),
                    Math.max(1, Math.round(decoded.getHeight() * scale)), true);
            int[] pixels = new int[sample.getWidth() * sample.getHeight()];
            sample.getPixels(pixels, 0, sample.getWidth(), 0, 0, sample.getWidth(), sample.getHeight());
            return ArtworkColorExtractor.select(pixels);
        } finally {
            if (sample != null && sample != decoded) sample.recycle();
            if (decoded != null) decoded.recycle();
            connection.disconnect();
        }
    }
}
