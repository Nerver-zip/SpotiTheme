package com.spotitheme.theme;

/** Color ordering required by the pinned Encore and Material constructors. */
public final class PaletteValues {
    private PaletteValues() {}

    public static long pack(int argb) { return (argb & 0xFFFFFFFFL) << 32; }
    public static int unpack(long packed) { return (int) (packed >>> 32); }

    public static String backgroundRole(int[] original, boolean fixed) {
        for (int color : original) {
            switch (color) {
                case 0xFF121212: return "background";
                case 0xFF1F1F1F: return "surface";
                case 0x1AFFFFFF: return "tinted";
                case 0xFF1ED760: return "accent";
                default: break;
            }
        }
        return fixed ? "background" : null;
    }

    public static Object[] colors(int... colors) {
        Object[] packed = new Object[colors.length];
        for (int i = 0; i < colors.length; i++) packed[i] = pack(colors[i]);
        return packed;
    }

    public static Object[] layer(ThemePalette palette, String role) {
        return colors(palette.color(role), palette.color(role + "Highlight"), palette.color(role + "Press"));
    }

    public static Object[] foreground(ThemePalette p, boolean accent) {
        if (accent) return colors(p.color("onAccent"), 0xFFD8F1FF, p.color("onAccent"),
                0xFFFFE1E6, 0xFFFFF0C2, 0xFFD5FFED, 0xFFDCEEFF);
        return colors(p.color("text"), p.color("textSubdued"), p.color("accent"),
                p.color("negative"), p.color("warning"), p.color("positive"), p.color("announcement"));
    }

    public static Object[] material(ThemePalette p) {
        return colors(p.color("accent"), p.color("onAccent"), 0xFFCBEAFF, 0xFF082F49,
                0xFF7DD3FC, 0xFF476F85, p.color("onAccent"), 0xFFD6EFFC, 0xFF163746,
                0xFF5E5A92, p.color("onAccent"), 0xFFE5DFFF, 0xFF2B2857,
                p.color("background"), p.color("text"), p.color("surface"), p.color("text"),
                0xFFDCECF5, 0xFF3C5668, p.color("accent"), 0xFF233A4A, 0xFFE9F5FB,
                0xFFBA1A1A, p.color("onAccent"), 0xFFFFDAD6, 0xFF410002, 0xFF6F8795,
                0xFFBFCCD4, p.color("scrim"), p.color("onAccent"), 0xFFCFDDE5, 0xFFEDF7FC,
                0xFFE4F1F7, 0xFFD9EAF2, 0xFFF4FAFD, p.color("onAccent"),
                p.color("accentHighlight"), p.color("accent"), p.color("onAccent"),
                p.color("onAccent"), 0xFFD6EFFC, 0xFF476F85, p.color("onAccent"),
                0xFF163746, 0xFFE5DFFF, 0xFF5E5A92, p.color("onAccent"), 0xFF2B2857);
    }
}
