package com.spotitheme.theme;

import android.graphics.Color;
import java.util.LinkedHashMap;
import java.util.Map;

/** Artwork-derived transient colors; catalog identity and semantic status colors remain intact. */
public final class ArtworkPaletteGenerator {
    private ArtworkPaletteGenerator() {}

    public static ThemePalette generate(ThemePalette selected, int baseColor, String mode) {
        if (!"light".equals(mode) && !"dark".equals(mode) && !"neutral".equals(mode))
            throw new IllegalArgumentException("Unknown artwork mode: " + mode);
        Map<String, Integer> colors = new LinkedHashMap<>(selected.getColors());
        int background = adjustBaseColor(baseColor | 0xFF000000, mode);
        int text = bestTextColor(background);
        int textSubdued = ensureContrast(blendColors(text, background, 0.32f), background, text, 4.5);

        float surfaceAmount = "light".equals(mode) ? 0.035f : "dark".equals(mode) ? 0.09f : 0.07f;
        int surface = blendColors(background, text, surfaceAmount);
        int surfaceHighlight = blendColors(background, text, surfaceAmount + 0.05f);
        int surfacePress = blendColors(background, text, surfaceAmount + 0.11f);

        int accent = createThemeAccent(background, text, mode);
        int onAccent = bestTextColor(accent);

        int tinted = blendColors(background, accent, "neutral".equals(mode) ? 0.24f : 0.20f);
        int tintedHighlight = blendColors(tinted, text, 0.08f);
        int tintedPress = blendColors(tinted, text, 0.15f);

        colors.put("background", background);
        colors.put("backgroundHighlight", blendColors(background, text, 0.08f));
        colors.put("backgroundPress", blendColors(background, text, 0.15f));

        colors.put("surface", surface);
        colors.put("surfaceHighlight", surfaceHighlight);
        colors.put("surfacePress", surfacePress);

        colors.put("tinted", tinted);
        colors.put("tintedHighlight", tintedHighlight);
        colors.put("tintedPress", tintedPress);

        colors.put("text", text);
        colors.put("textSubdued", textSubdued);

        colors.put("accent", accent);
        colors.put("accentHighlight", blendColors(accent, onAccent, 0.12f));
        colors.put("accentPress", blendColors(accent, onAccent, 0.22f));

        colors.put("onAccent", onAccent);
        colors.put("announcement", accent);
        colors.put("decorative", accent);
        colors.put("decorativeSubdued", blendColors(background, accent, 0.42f));
        colors.put("savedIndicator", accent);
        colors.put("playerBackground", surface);
        colors.put("albumHeaderBackground", background);
        colors.put("outline", surfaceHighlight);

        return selected.withRuntimeColors(colors);
    }

    private static int adjustBaseColor(int color, String mode) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        switch (mode) {
            case "light": {
                hsv[1] = Math.min(hsv[1], 0.42f);
                hsv[2] = Math.max(hsv[2], 0.90f);
                break;
            }

            case "dark": {
                hsv[1] = Math.max(hsv[1], 0.30f);
                hsv[2] = Math.min(hsv[2], 0.24f);
                break;
            }

            default: {
                hsv[1] = Math.max(hsv[1], 0.18f);
                hsv[2] = Math.max(0.16f, Math.min(hsv[2], 0.86f));
                break;
            }
        }

        return Color.HSVToColor(Color.alpha(color), hsv);
    }

    private static int createThemeAccent(int background, int contrastTarget, String mode) {
        float[] hsv = new float[3];
        Color.colorToHSV(background, hsv);

        hsv[1] = Math.max(hsv[1], 0.48f);

        switch (mode) {
            case "light":
                hsv[2] = Math.min(Math.max(hsv[2], 0.52f), 0.68f);
                break;

            case "dark":
                hsv[2] = Math.max(hsv[2], 0.78f);
                break;

            default:
                hsv[2] = relativeLuminance(background) < 0.42 ? Math.max(hsv[2], 0.76f) : Math.min(hsv[2], 0.48f);
                break;
        }

        return ensureContrast(Color.HSVToColor(hsv), background, contrastTarget, 3.0);
    }

    private static int bestTextColor(int background) {
        int lightText = 0xFFF8FAFC;
        int darkText = 0xFF101418;

        double lightContrast = contrastRatio(lightText, background);
        double darkContrast = contrastRatio(darkText, background);

        return lightContrast >= darkContrast ? lightText : darkText;
    }

    private static int ensureContrast(int foreground, int background, int contrastTarget, double minimumRatio) {
        int result = foreground;

        for (int i = 0; i < 20 && contrastRatio(result, background) < minimumRatio; i++) {
            result = blendColors(result, contrastTarget, 0.10f);
        }

        return result;
    }

    private static double contrastRatio(int first, int second) {
        double firstLuminance = relativeLuminance(first);
        double secondLuminance = relativeLuminance(second);

        double lighter = Math.max(firstLuminance, secondLuminance);
        double darker = Math.min(firstLuminance, secondLuminance);

        return (lighter + 0.05) / (darker + 0.05);
    }

    private static double relativeLuminance(int color) {
        double red = linearColorComponent(Color.red(color) / 255.0);
        double green = linearColorComponent(Color.green(color) / 255.0);
        double blue = linearColorComponent(Color.blue(color) / 255.0);

        return 0.2126 * red + 0.7152 * green + 0.0722 * blue;
    }

    private static double linearColorComponent(double component) {
        return component <= 0.04045 ? component / 12.92 : Math.pow((component + 0.055) / 1.055, 2.4);
    }

    private static int blendColors(int first, int second, float secondAmount) {
        float firstAmount = 1.0f - secondAmount;

        return Color.rgb(Math.round(Color.red(first) * firstAmount + Color.red(second) * secondAmount), Math.round(Color.green(first) * firstAmount + Color.green(second) * secondAmount), Math.round(Color.blue(first) * firstAmount + Color.blue(second) * secondAmount));
    }

}
