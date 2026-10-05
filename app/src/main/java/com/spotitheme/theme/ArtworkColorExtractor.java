package com.spotitheme.theme;

/** Weighted artwork histogram, independent of Android bitmap ownership. */
public final class ArtworkColorExtractor {
    private ArtworkColorExtractor() {}

    public static int select(int[] pixels) {
        final int hueBuckets = 24;
        final int saturationBuckets = 4;
        final int valueBuckets = 4;
        final int bucketCount = hueBuckets * saturationBuckets * valueBuckets;

        int[] populations = new int[bucketCount];
        long[] redTotals = new long[bucketCount];
        long[] greenTotals = new long[bucketCount];
        long[] blueTotals = new long[bucketCount];

        if (pixels == null) throw new IllegalArgumentException("Pixels are required");

        float[] hsv = new float[3];

        for (int pixel : pixels) {
            if (alpha(pixel) < 200) {
                continue;
            }

            hsv(pixel, hsv);

            float hue = hsv[0];
            float saturation = hsv[1];
            float value = hsv[2];

            if (value < 0.07f) {
                continue;
            }

            if (value > 0.94f && saturation < 0.14f) {
                continue;
            }

            int hueBucket = Math.min(hueBuckets - 1, (int) (hue / 360.0f * hueBuckets));
            int saturationBucket = Math.min(saturationBuckets - 1, (int) (saturation * saturationBuckets));
            int valueBucket = Math.min(valueBuckets - 1, (int) (value * valueBuckets));
            int bucket = hueBucket * saturationBuckets * valueBuckets + saturationBucket * valueBuckets + valueBucket;

            populations[bucket]++;
            redTotals[bucket] += red(pixel);
            greenTotals[bucket] += green(pixel);
            blueTotals[bucket] += blue(pixel);
        }

        int bestBucket = -1;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int bucket = 0; bucket < bucketCount; bucket++) {
            int population = populations[bucket];

            if (population == 0) {
                continue;
            }

            int red = (int) (redTotals[bucket] / population);
            int green = (int) (greenTotals[bucket] / population);
            int blue = (int) (blueTotals[bucket] / population);
            int averageColor = rgb(red, green, blue);

            hsv(averageColor, hsv);

            double saturationWeight = 0.70 + hsv[1] * 0.85;
            double brightnessWeight = 1.15 - Math.abs(hsv[2] - 0.55) * 0.45;

            double score = population * saturationWeight * brightnessWeight;

            if (score > bestScore) {
                bestScore = score;
                bestBucket = bucket;
            }
        }

        if (bestBucket < 0) {
            return averageBitmapColor(pixels);
        }

        int population = populations[bestBucket];

        return rgb((int) (redTotals[bestBucket] / population), (int) (greenTotals[bestBucket] / population), (int) (blueTotals[bestBucket] / population));
    }

    private static int averageBitmapColor(int[] pixels) {
        long red = 0;
        long green = 0;
        long blue = 0;
        int count = 0;

        for (int pixel : pixels) {
            if (alpha(pixel) < 200) {
                continue;
            }

            red += red(pixel);
            green += green(pixel);
            blue += blue(pixel);
            count++;
        }

        if (count == 0) {
            return 0xFF303030;
        }

        return rgb((int) (red / count), (int) (green / count), (int) (blue / count));
    }

    private static int alpha(int color) { return color >>> 24; }
    private static int red(int color) { return (color >>> 16) & 255; }
    private static int green(int color) { return (color >>> 8) & 255; }
    private static int blue(int color) { return color & 255; }
    private static int rgb(int red, int green, int blue) {
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static void hsv(int color, float[] result) {
        int red = red(color), green = green(color), blue = blue(color);
        int max = Math.max(red, Math.max(green, blue));
        int min = Math.min(red, Math.min(green, blue));
        float delta = max - min;
        float hue = 0f;
        if (delta != 0f) {
            if (max == red) hue = (green - blue) / delta;
            else if (max == green) hue = 2f + (blue - red) / delta;
            else hue = 4f + (red - green) / delta;
            hue *= 60f;
            if (hue < 0f) hue += 360f;
        }
        result[0] = hue;
        result[1] = max == 0 ? 0f : delta / max;
        result[2] = max / 255f;
    }
}
