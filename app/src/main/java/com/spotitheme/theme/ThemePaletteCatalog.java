package com.spotitheme.theme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.XModuleResources;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Read-only bundled and SAF folder theme catalog. */
public final class ThemePaletteCatalog {
    public static final int REQUEST_SELECT_FOLDER = 0x5A17;
    public static final int MAX_FILES = 128;
    private static final String ASSET_DIRECTORY = "themes";

    private ThemePaletteCatalog() {}

    public static ScanResult scan(Context context, String modulePath, SharedPreferences preferences) {
        List<ThemeEntry> entries = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> ids = new HashSet<>();

        loadBundled(modulePath, entries, ids, errors);

        String folderUri = preferences.getString(ThemePaletteStore.FOLDER_URI, null);
        if (folderUri != null) {
            scanFolder(context, Uri.parse(folderUri), entries, ids, errors);
        }

        if (ThemePaletteStore.hasLegacyPalette(preferences)) {
            ThemePalette legacy = ThemePaletteStore.legacyPalette(preferences);
            if (ids.add(legacy.getId())) entries.add(new ThemeEntry(legacy, Source.LEGACY, null, null));
        }

        entries.sort(Comparator.comparing((ThemeEntry entry) -> entry.palette.getName().toLowerCase(Locale.ROOT))
                .thenComparing(entry -> entry.palette.getId()));
        return new ScanResult(entries, errors, folderLabel(context, folderUri), folderUri);
    }

    public static List<String> displayLabels(List<ThemeEntry> entries) {
        Map<String, Integer> counts = new HashMap<>();
        for (ThemeEntry entry : entries) {
            String key = entry.palette.getName().toLowerCase(Locale.ROOT);
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        }

        List<String> labels = new ArrayList<>(entries.size());
        Set<String> used = new HashSet<>();
        for (ThemeEntry entry : entries) {
            String name = entry.palette.getName();
            String label = name;
            if (counts.get(name.toLowerCase(Locale.ROOT)) > 1) {
                String source = entry.source == Source.BUNDLED ? "Built-in"
                        : entry.source == Source.CUSTOM
                        ? (entry.sourceName == null || entry.sourceName.isEmpty() ? "Imported JSON" : entry.sourceName)
                        : "Legacy Custom";
                label = name + " — " + source;
            }
            if (!used.add(label.toLowerCase(Locale.ROOT))) {
                label += " [" + entry.palette.getId() + "]";
                used.add(label.toLowerCase(Locale.ROOT));
            }
            labels.add(label);
        }
        return Collections.unmodifiableList(labels);
    }

    public static String displayLabel(ThemeEntry target, List<ThemeEntry> entries) {
        List<String> labels = displayLabels(entries);
        for (int i = 0; i < entries.size(); i++) {
            ThemeEntry entry = entries.get(i);
            if (entry == target || entry.palette.getId().equals(target.palette.getId())) return labels.get(i);
        }
        return target.palette.getName();
    }

    public static void persistFolder(Activity activity, Intent result) throws Exception {
        Uri uri = result.getData();
        if (uri == null) throw new IllegalArgumentException("The folder picker returned no URI");
        int flags = result.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if ((flags & Intent.FLAG_GRANT_READ_URI_PERMISSION) == 0) {
            throw new SecurityException("The selected folder did not grant read access");
        }
        activity.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.getSharedPreferences("SpotiTheme", Context.MODE_PRIVATE).edit()
                .putString(ThemePaletteStore.FOLDER_URI, uri.toString()).apply();
    }

    private static void loadBundled(String modulePath, List<ThemeEntry> entries, Set<String> ids, List<String> errors) {
        if (modulePath == null || modulePath.isEmpty()) {
            errors.add("Built-in theme assets are unavailable");
            return;
        }
        try {
            XModuleResources resources = XModuleResources.createInstance(modulePath, null);
            String[] names = resources.getAssets().list(ASSET_DIRECTORY);
            if (names == null) return;
            Arrays.sort(names, Comparator.comparing((String name) -> name.toLowerCase(Locale.ROOT))
                    .thenComparing(name -> name));
            for (String name : names) {
                if (!name.toLowerCase(Locale.ROOT).endsWith(".json")) continue;
                try (InputStream input = resources.getAssets().open(ASSET_DIRECTORY + "/" + name)) {
                    add(input, name, Source.BUNDLED, null, entries, ids, errors);
                } catch (Exception exception) {
                    errors.add(name + ": " + message(exception));
                }
            }
        } catch (Throwable throwable) {
            errors.add("Built-in themes: " + message(throwable));
        }
    }

    private static void scanFolder(Context context, Uri treeUri, List<ThemeEntry> entries,
                                   Set<String> ids, List<String> errors) {
        if (!hasPersistedReadPermission(context, treeUri)) {
            errors.add("The selected folder's read permission is unavailable; choose the folder again");
            return;
        }
        try {
            DocumentFile tree = DocumentFile.fromTreeUri(context, treeUri);
            if (tree == null || !tree.isDirectory()) {
                errors.add("The selected theme folder is missing or is not a directory");
                return;
            }
            DocumentFile[] documents = tree.listFiles();
            if (documents == null) {
                errors.add("The selected document provider could not list this folder");
                return;
            }
            Arrays.sort(documents, Comparator.comparing(
                    (DocumentFile document) -> document.getName() == null
                            ? "" : document.getName().toLowerCase(Locale.ROOT))
                    .thenComparing(document -> document.getName() == null ? "" : document.getName())
                    .thenComparing(document -> document.getUri().toString()));
            int jsonCount = 0;
            for (DocumentFile document : documents) {
                if (!document.isFile()) continue;
                String name = document.getName();
                if (name == null || !name.toLowerCase(Locale.ROOT).endsWith(".json")) continue;
                if (++jsonCount > MAX_FILES) {
                    errors.add("Only the first " + MAX_FILES + " JSON files are scanned in a folder");
                    break;
                }
                try (InputStream input = context.getContentResolver().openInputStream(document.getUri())) {
                    if (input == null) throw new IllegalStateException("The document provider returned no content stream");
                    add(input, name, Source.CUSTOM, document.getUri(), entries, ids, errors);
                } catch (Exception exception) {
                    errors.add(name + ": " + message(exception));
                }
            }
        } catch (Throwable throwable) {
            errors.add("Theme folder: " + message(throwable));
        }
    }

    private static boolean hasPersistedReadPermission(Context context, Uri treeUri) {
        for (android.content.UriPermission permission : context.getContentResolver().getPersistedUriPermissions()) {
            if (treeUri.equals(permission.getUri()) && permission.isReadPermission()) return true;
        }
        return false;
    }

    private static void add(InputStream input, String sourceName, Source source, Uri uri,
                            List<ThemeEntry> entries, Set<String> ids, List<String> errors) throws Exception {
        ThemePalette palette = ThemePaletteParser.parse(input, displayNameFromFilename(sourceName));
        if (!ids.add(palette.getId())) {
            errors.add(sourceName + ": duplicate theme id '" + palette.getId() + "'");
            return;
        }
        entries.add(new ThemeEntry(palette, source, sourceName, uri));
    }

    private static String displayNameFromFilename(String sourceName) {
        if (sourceName == null) return null;
        return sourceName.toLowerCase(Locale.ROOT).endsWith(".json")
                ? sourceName.substring(0, sourceName.length() - 5) : sourceName;
    }

    private static String folderLabel(Context context, String folderUri) {
        if (folderUri == null) return "No theme folder selected";
        try {
            DocumentFile folder = DocumentFile.fromTreeUri(context, Uri.parse(folderUri));
            String name = folder == null ? null : folder.getName();
            return name == null || name.isEmpty() ? "Selected folder" : name;
        } catch (Throwable ignored) {
            return "Selected folder (unavailable)";
        }
    }

    private static String message(Throwable throwable) {
        String detail = throwable.getMessage();
        return throwable.getClass().getSimpleName() + (detail == null || detail.isEmpty() ? "" : ": " + detail);
    }

    public enum Source { BUNDLED, CUSTOM, LEGACY }

    public static final class ThemeEntry {
        public final ThemePalette palette;
        public final Source source;
        public final String sourceName;
        public final Uri documentUri;

        ThemeEntry(ThemePalette palette, Source source, String sourceName, Uri documentUri) {
            this.palette = palette;
            this.source = source;
            this.sourceName = sourceName;
            this.documentUri = documentUri;
        }
    }

    public static final class ScanResult {
        public final List<ThemeEntry> entries;
        public final List<String> errors;
        public final String folderLabel;
        public final String folderUri;

        ScanResult(List<ThemeEntry> entries, List<String> errors, String folderLabel, String folderUri) {
            this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
            this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
            this.folderLabel = folderLabel;
            this.folderUri = folderUri;
        }

        public String summary() {
            String text = entries.size() + " themes available";
            return errors.isEmpty() ? text : text + " · " + errors.size() + " file/folder issue(s)";
        }
    }
}
