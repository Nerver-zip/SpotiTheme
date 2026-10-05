package com.spotitheme.theme;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/** Validates an entire SpotiTheme JSON palette before returning any colors. */
public final class ThemePaletteParser {
    public static final int MAX_BYTES = 64 * 1024;
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");
    private static final Pattern OPAQUE_HEX = Pattern.compile("#[0-9a-fA-F]{6}");
    private static final Pattern RGBA_HEX = Pattern.compile("#[0-9a-fA-F]{8}");

    private ThemePaletteParser() {}

    public static ThemePalette parse(InputStream input) throws IOException, PaletteParseException {
        return parse(input, null);
    }

    public static ThemePalette parse(InputStream input, String fallbackName) throws IOException, PaletteParseException {
        return parse(new String(readBounded(input), StandardCharsets.UTF_8), fallbackName);
    }

    public static ThemePalette parse(String json) throws PaletteParseException {
        return parse(json, null);
    }

    public static ThemePalette parse(String json, String fallbackName) throws PaletteParseException {
        if (json == null) throw new PaletteParseException("Theme file is empty");
        if (json.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new PaletteParseException("Theme file exceeds " + MAX_BYTES + " bytes");
        }

        final JsonElement parsed;
        try {
            parsed = JsonParser.parseString(json);
        } catch (JsonParseException exception) {
            throw new PaletteParseException("Invalid JSON: " + exception.getMessage());
        }
        if (!parsed.isJsonObject()) throw new PaletteParseException("Top-level JSON value must be an object");
        JsonObject document = parsed.getAsJsonObject();

        JsonElement versionValue = document.get("schemaVersion");
        if (versionValue == null || !versionValue.isJsonPrimitive() || !versionValue.getAsJsonPrimitive().isNumber()
                || !isSupportedSchemaVersion(versionValue)) {
            throw new PaletteParseException("schemaVersion must be 1");
        }
        String id = requiredString(document, "id", 96);
        if (!ID_PATTERN.matcher(id).matches()) throw new PaletteParseException("id must use lowercase letters, numbers, and hyphens");
        String name = optionalDisplayName(document, fallbackName, id);
        String base = requiredString(document, "base", 8);
        if (!"dark".equals(base) && !"light".equals(base)) {
            throw new PaletteParseException("base must be either dark or light");
        }

        JsonElement colorsValue = document.get("colors");
        if (colorsValue == null || !colorsValue.isJsonObject()) {
            throw new PaletteParseException("colors must be an object");
        }
        JsonObject colorObject = colorsValue.getAsJsonObject();
        Map<String, Integer> colors = new LinkedHashMap<>(ThemePalette.defaultsFor(base));
        for (Map.Entry<String, JsonElement> entry : colorObject.entrySet()) {
            String role = entry.getKey();
            if (!ThemePalette.COLOR_ROLES.contains(role)) continue;
            JsonElement value = entry.getValue();
            if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                throw new PaletteParseException("colors." + role + " must be a hex string");
            }
            colors.put(role, parseColor(value.getAsString(), role));
        }
        if (!colorObject.has("savedIndicator")) {
            colors.put("savedIndicator", colors.get("accent"));
        }
        return new ThemePalette(id, name, base, colors);
    }

    private static boolean isSupportedSchemaVersion(JsonElement value) {
        try {
            return value.getAsJsonPrimitive().getAsBigDecimal().compareTo(BigDecimal.ONE) == 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    public static int parseColor(String value, String role) throws PaletteParseException {
        if (value == null) throw new PaletteParseException("colors." + role + " is null");
        try {
            if (OPAQUE_HEX.matcher(value).matches()) {
                return 0xFF000000 | Integer.parseInt(value.substring(1), 16);
            }
            if (RGBA_HEX.matcher(value).matches()) {
                long rgba = Long.parseLong(value.substring(1), 16);
                int redGreenBlue = (int) ((rgba >>> 8) & 0x00FFFFFFL);
                int alpha = (int) (rgba & 0xFFL);
                return (alpha << 24) | redGreenBlue;
            }
        } catch (NumberFormatException exception) {
            throw new PaletteParseException("colors." + role + " is not valid hexadecimal");
        }
        throw new PaletteParseException("colors." + role + " must be #RRGGBB or #RRGGBBAA");
    }

    private static String requiredString(JsonObject object, String key, int maxLength) throws PaletteParseException {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new PaletteParseException(key + " must be a string");
        }
        String result = value.getAsString().trim();
        if (result.isEmpty() || result.length() > maxLength) {
            throw new PaletteParseException(key + " must contain 1-" + maxLength + " characters");
        }
        return result;
    }

    private static String optionalDisplayName(JsonObject object, String fallbackName, String fallbackId)
            throws PaletteParseException {
        JsonElement value = object.get("name");
        if (value == null) {
            String fallback = fallbackName == null ? fallbackId : fallbackName.trim();
            if (fallback.isEmpty() || fallback.length() > 96) {
                throw new PaletteParseException("fallback name must contain 1-96 characters");
            }
            return fallback;
        }
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new PaletteParseException("name must be a string");
        }
        String name = value.getAsString().trim();
        if (name.isEmpty() || name.length() > 96) {
            throw new PaletteParseException("name must contain 1-96 characters");
        }
        return name;
    }

    private static byte[] readBounded(InputStream input) throws IOException, PaletteParseException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = input.read(buffer)) != -1) {
            if (output.size() + count > MAX_BYTES) throw new PaletteParseException("Theme file exceeds " + MAX_BYTES + " bytes");
            output.write(buffer, 0, count);
        }
        return output.toByteArray();
    }

    public static final class PaletteParseException extends Exception {
        public PaletteParseException(String message) { super(message); }
    }
}
