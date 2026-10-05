package com.spotitheme.theme;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Immutable SpotiTheme palette using stable semantic color roles. */
public final class ThemePalette {
    public static final List<String> COLOR_ROLES;

    private static final Map<String, Integer> DARK_DEFAULTS;
    private static final Map<String, Integer> LIGHT_DEFAULTS;

    static {
        List<String> roles = new ArrayList<>();
        Collections.addAll(roles,
                "background", "backgroundHighlight", "backgroundPress",
                "surface", "surfaceHighlight", "surfacePress",
                "tinted", "tintedHighlight", "tintedPress",
                "text", "textSubdued", "accent", "accentHighlight", "accentPress",
                "announcement", "decorative", "decorativeSubdued", "negative", "warning",
                "positive", "scrim", "onAccent", "savedIndicator", "playerBackground",
                "albumHeaderBackground", "outline");
        COLOR_ROLES = Collections.unmodifiableList(roles);

        Map<String, Integer> dark = new LinkedHashMap<>();
        dark.put("background", 0xFF121212);
        dark.put("backgroundHighlight", 0xFF1F1F1F);
        dark.put("backgroundPress", 0xFF000000);
        dark.put("surface", 0xFF1F1F1F);
        dark.put("surfaceHighlight", 0xFF2A2A2A);
        dark.put("surfacePress", 0xFF191919);
        dark.put("tinted", 0x1AFFFFFF);
        dark.put("tintedHighlight", 0x24FFFFFF);
        dark.put("tintedPress", 0x36FFFFFF);
        dark.put("text", 0xFFFFFFFF);
        dark.put("textSubdued", 0xFFB3B3B3);
        dark.put("accent", 0xFF1ED760);
        dark.put("accentHighlight", 0xFF3BE477);
        dark.put("accentPress", 0xFF1ABC54);
        dark.put("announcement", 0xFF539DF5);
        dark.put("decorative", 0xFFFFFFFF);
        dark.put("decorativeSubdued", 0xFF292929);
        dark.put("negative", 0xFFED2C3F);
        dark.put("warning", 0xFFFFA42B);
        dark.put("positive", 0xFF1ED760);
        dark.put("scrim", 0xFF000000);
        dark.put("onAccent", 0xFF000000);
        dark.put("savedIndicator", 0xFF1ED760);
        dark.put("playerBackground", 0xFF1F1F1F);
        dark.put("albumHeaderBackground", 0xFF121212);
        dark.put("outline", 0xFF454545);
        DARK_DEFAULTS = Collections.unmodifiableMap(dark);

        Map<String, Integer> light = new LinkedHashMap<>();
        light.put("background", 0xFFF7F7F7);
        light.put("backgroundHighlight", 0xFFECECEC);
        light.put("backgroundPress", 0xFFE3E3E3);
        light.put("surface", 0xFFFFFFFF);
        light.put("surfaceHighlight", 0xFFF0F0F0);
        light.put("surfacePress", 0xFFE4E4E4);
        light.put("tinted", 0x1A000000);
        light.put("tintedHighlight", 0x24000000);
        light.put("tintedPress", 0x36000000);
        light.put("text", 0xFF161616);
        light.put("textSubdued", 0xFF606060);
        light.put("accent", 0xFF168A42);
        light.put("accentHighlight", 0xFF1E9F4D);
        light.put("accentPress", 0xFF116B32);
        light.put("announcement", 0xFF1769AA);
        light.put("decorative", 0xFF161616);
        light.put("decorativeSubdued", 0xFF777777);
        light.put("negative", 0xFFB3261E);
        light.put("warning", 0xFF8A4A00);
        light.put("positive", 0xFF167D36);
        light.put("scrim", 0x4D000000);
        light.put("onAccent", 0xFFFFFFFF);
        light.put("savedIndicator", 0xFF168A42);
        light.put("playerBackground", 0xFFFFFFFF);
        light.put("albumHeaderBackground", 0xFFF7F7F7);
        light.put("outline", 0xFF7A7A7A);
        LIGHT_DEFAULTS = Collections.unmodifiableMap(light);
    }

    private final String id;
    private final String name;
    private final String base;
    private final Map<String, Integer> colors;

    ThemePalette(String id, String name, String base, Map<String, Integer> colors) {
        this.id = id;
        this.name = name;
        this.base = base;
        this.colors = Collections.unmodifiableMap(new LinkedHashMap<>(colors));
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getBase() { return base; }
    public Map<String, Integer> getColors() { return colors; }

    /** Keep catalog identity while presenting a complete transient runtime palette. */
    public ThemePalette withRuntimeColors(Map<String, Integer> runtimeColors) {
        for (String role : COLOR_ROLES) {
            if (runtimeColors.get(role) == null) throw new IllegalArgumentException("Missing color role: " + role);
        }
        return new ThemePalette(id, name, base, runtimeColors);
    }

    public int color(String role) {
        Integer value = colors.get(role);
        if (value == null) throw new IllegalArgumentException("Unknown color role: " + role);
        return value;
    }

    static Map<String, Integer> defaultsFor(String base) {
        return "light".equals(base) ? LIGHT_DEFAULTS : DARK_DEFAULTS;
    }

    public JsonObject toJsonObject() {
        JsonObject document = new JsonObject();
        document.addProperty("schemaVersion", 1);
        document.addProperty("id", id);
        document.addProperty("name", name);
        document.addProperty("base", base);
        JsonObject colorObject = new JsonObject();
        for (String role : COLOR_ROLES) colorObject.addProperty(role, formatColor(color(role)));
        document.add("colors", colorObject);
        return document;
    }

    static String formatColor(int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        int rgb = argb & 0x00FFFFFF;
        if (alpha == 0xFF) return String.format(java.util.Locale.ROOT, "#%06X", rgb);
        return String.format(java.util.Locale.ROOT, "#%06X%02X", rgb, alpha);
    }
}
