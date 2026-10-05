package com.spotitheme.theme;

public final class PaletteColors {
    public final int[] backgrounds;
    public final int[] foregrounds;

    public PaletteColors(int[] backgrounds, int[] foregrounds) {
        this.backgrounds = backgrounds.clone();
        this.foregrounds = foregrounds.clone();
    }

    public int map(int color, PaletteColors next, boolean foreground) {
        if ((color >>> 24) == 0) return color;
        int[] first = foreground ? foregrounds : backgrounds;
        int[] second = foreground ? backgrounds : foregrounds;
        int[] nextFirst = foreground ? next.foregrounds : next.backgrounds;
        int[] nextSecond = foreground ? next.backgrounds : next.foregrounds;

        int mapped = 0;
        boolean matched = false;
        boolean ambiguous = false;
        for (int i = 0; i < first.length; i++) {
            if (color == first[i]) {
                if (!matched) mapped = nextFirst[i];
                else if (mapped != nextFirst[i]) ambiguous = true;
                matched = true;
            }
        }
        for (int i = 0; i < second.length; i++) {
            if (color == second[i]) {
                if (!matched) mapped = nextSecond[i];
                else if (mapped != nextSecond[i]) ambiguous = true;
                matched = true;
            }
        }
        if (matched) return ambiguous ? color : mapped;

        // Native views do not expose which Spotify semantic role produced a color.
        // Preserve an ambiguous value instead of silently assigning the first role.
        mapped = 0;
        matched = false;
        ambiguous = false;
        int rgb = color & 0xFFFFFF;
        for (int i = 0; i < first.length; i++) {
            if ((first[i] >>> 24) == 255 && rgb == (first[i] & 0xFFFFFF)) {
                int candidate = (color & 0xFF000000) | (nextFirst[i] & 0xFFFFFF);
                if (!matched) mapped = candidate;
                else if (mapped != candidate) ambiguous = true;
                matched = true;
            }
        }
        for (int i = 0; i < second.length; i++) {
            if ((second[i] >>> 24) == 255 && rgb == (second[i] & 0xFFFFFF)) {
                int candidate = (color & 0xFF000000) | (nextSecond[i] & 0xFFFFFF);
                if (!matched) mapped = candidate;
                else if (mapped != candidate) ambiguous = true;
                matched = true;
            }
        }
        return matched && !ambiguous ? mapped : color;
    }
}
