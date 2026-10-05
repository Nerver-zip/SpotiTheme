package com.spotitheme.theme;

/** Only the latest artwork/configuration request may publish its result. */
public final class ArtworkRequestGate {
    public static final class Request {
        public final String artwork;
        private Request(String artwork) { this.artwork = artwork; }
    }

    private Request current;

    /** Null artwork deliberately supersedes downloads for fallback-only events. */
    public synchronized Request begin(String artwork) {
        current = new Request(artwork);
        return current;
    }

    /** Call when disabling, selecting a palette or changing extraction mode. */
    public synchronized void invalidate() { current = null; }

    public synchronized boolean isCurrent(Request request) {
        return request != null && request == current;
    }

    /** Check at publication time, not when a background task finishes. */
    public synchronized boolean publish(Request request, Runnable result) {
        if (!isCurrent(request)) return false;
        result.run();
        return true;
    }
}
