package com.spotitheme.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.spotitheme.theme.ThemePalette;
import com.spotitheme.theme.ThemePaletteCatalog;
import com.spotitheme.theme.ThemePaletteParser;
import com.spotitheme.theme.ThemePaletteStore;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Standalone JSON theme manager with read-only SAF catalog scanning. */
public final class ThemeActivity extends Activity {
    private SharedPreferences prefs;
    private ThemePalette palette;
    private ThemePaletteCatalog.ScanResult catalog;
    private LinearLayout content;
    private final ExecutorService scanner = Executors.newSingleThreadExecutor();
    private int scanGeneration;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = ThemePreferences.open(this);
        boolean retiredMochaMauve = ThemePaletteStore.isRetiredMochaMauveId(
                prefs.getString(ThemePaletteStore.SELECTED_ID, null));
        palette = retiredMochaMauve ? null : ThemePaletteStore.restoreSelected(prefs);
        if (palette == null && !retiredMochaMauve && ThemePaletteStore.hasLegacyPalette(prefs))
            palette = ThemePaletteStore.legacyPalette(prefs);
        if (palette == null) {
            try (InputStream input = getAssets().open("themes/Catppuccin Mocha.json")) {
                palette = ThemePaletteParser.parse(input);
            } catch (Exception failure) { throw new IllegalStateException("Missing default palette", failure); }
        }
        if (retiredMochaMauve) {
            ThemePaletteStore.select(prefs, palette);
        } else if (!prefs.contains(ThemePaletteStore.LAST_GOOD_JSON)
                && !ThemePaletteStore.hasLegacyPalette(prefs)) {
            prefs.edit().putString(ThemePaletteStore.SELECTED_ID, palette.getId())
                    .putString(ThemePaletteStore.SELECTED_NAME, palette.getName())
                    .putString(ThemePaletteStore.LAST_GOOD_JSON,
                            new com.google.gson.Gson().toJson(palette.toJsonObject())).apply();
        }
        render();
        scan();
    }

    private void scan() {
        int generation = ++scanGeneration;
        scanner.execute(() -> {
            ThemePaletteCatalog.ScanResult result = ThemePaletteCatalog.scan(this, null, prefs);
            runOnUiThread(() -> {
                if (isDestroyed() || generation != scanGeneration) return;
                catalog = result; render();
            });
        });
    }

    private void render() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(android.view.WindowInsets.Type.systemBars());
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            } else view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        scroll.setBackgroundColor(palette.color("background"));
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(24), dp(24), dp(32));
        scroll.addView(content);
        setContentView(scroll);
        getWindow().setStatusBarColor(palette.color("background"));
        getWindow().setNavigationBarColor(palette.color("background"));
        getWindow().getDecorView().setSystemUiVisibility("light".equals(palette.getBase())
                ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR : 0);
        label("SpotiTheme", 28, "text");
        label("Theme", 18, "text");
        if (catalog != null && !catalog.entries.isEmpty()) {
            List<String> labels = ThemePaletteCatalog.displayLabels(catalog.entries);
            int selected = 0;
            for (int i = 0; i < catalog.entries.size(); i++)
                if (catalog.entries.get(i).palette.getId().equals(palette.getId())) selected = i;
            Spinner themes = spinner(labels.toArray(new String[0]), selected);
            themes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onNothingSelected(AdapterView<?> parent) {}
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ThemePaletteCatalog.ThemeEntry entry = catalog.entries.get(position);
                    if (entry.palette.getId().equals(palette.getId())) return;
                    if (entry.source == ThemePaletteCatalog.Source.LEGACY) ThemePaletteStore.selectLegacy(prefs);
                    else ThemePaletteStore.select(prefs, entry.palette);
                    palette = entry.palette;
                    SettingsProvider.notifyChanged(ThemeActivity.this);
                    render();
                }
            });
            label(catalog.summary(), 14, "textSubdued");
            label(catalog.folderLabel == null ? "Built-in themes" : catalog.folderLabel, 14, "textSubdued");
        } else label("Loading themes…", 14, "textSubdued");
        button("Choose theme folder", () -> startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION),
                ThemePaletteCatalog.REQUEST_SELECT_FOLDER));
        button("Refresh themes", this::scan);
        if (catalog != null && !catalog.errors.isEmpty()) button("View import issues", () ->
                new android.app.AlertDialog.Builder(this).setTitle("Import issues")
                        .setMessage(String.join("\n", catalog.errors)).setPositiveButton("OK", null).show());
        toggle("Enable theme", SettingsProvider.ENABLED, true, false, true);
        toggle("Use album artwork colors", ThemePaletteStore.FIXED_MODE, true, true, true);
        boolean artwork = !prefs.getBoolean(ThemePaletteStore.FIXED_MODE, true);
        toggle("Auto Theme", SettingsProvider.AUTO, false, false, artwork);
        label("Artwork palette", 18, "text");
        String[] modes = {"neutral", "light", "dark"};
        String mode = prefs.getString(SettingsProvider.MODE, "neutral");
        int selectedMode = "light".equals(mode) ? 1 : "dark".equals(mode) ? 2 : 0;
        Spinner modePicker = spinner(new String[]{"Neutral", "Light", "Dark"}, selectedMode);
        modePicker.setEnabled(artwork);
        modePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) {}
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (modes[position].equals(prefs.getString(SettingsProvider.MODE, "neutral"))) return;
                prefs.edit().putString(SettingsProvider.MODE, modes[position]).apply();
                SettingsProvider.notifyChanged(ThemeActivity.this);
            }
        });
    }

    private void toggle(String title, String key, boolean fallback, boolean inverse, boolean enabled) {
        Switch control = new Switch(this);
        control.setText(title); control.setTextColor(controlColors("text", "textSubdued", "text"));
        control.setTextSize(16); control.setPadding(0, dp(12), 0, dp(12));
        control.setChecked(prefs.getBoolean(key, fallback) != inverse);
        control.setEnabled(enabled);
        control.setThumbTintList(controlColors("accent", "textSubdued", "outline"));
        control.setTrackTintList(controlColors("accentPress", "surface", "surfaceHighlight"));
        content.addView(control, new LinearLayout.LayoutParams(-1, -2));
        control.setOnCheckedChangeListener((button, checked) -> {
            prefs.edit().putBoolean(key, checked != inverse).apply();
            SettingsProvider.notifyChanged(this);
            if (ThemePaletteStore.FIXED_MODE.equals(key)) render();
        });
    }

    private ColorStateList controlColors(String checked, String disabled, String idle) {
        return new ColorStateList(new int[][] {
                {-android.R.attr.state_enabled}, {android.R.attr.state_checked}, {}
        }, new int[] {palette.color(disabled), palette.color(checked), palette.color(idle)});
    }

    private Spinner spinner(String[] labels, int selection) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels) {
            @Override public View getView(int position, View recycled, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, recycled, parent);
                view.setTextColor(palette.color("text")); view.setTextSize(16); return view;
            }
            @Override public View getDropDownView(int position, View recycled, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, recycled, parent);
                view.setTextColor(palette.color("text")); view.setBackgroundColor(palette.color("surface"));
                view.setPadding(dp(16), dp(12), dp(16), dp(12)); return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter); spinner.setSelection(selection);
        spinner.setBackgroundTintList(ColorStateList.valueOf(palette.color("outline")));
        content.addView(spinner, new LinearLayout.LayoutParams(-1, dp(56)));
        return spinner;
    }

    private void button(String title, Runnable action) {
        Button button = new Button(this); button.setText(title); button.setAllCaps(false);
        button.setTextColor(palette.color("text"));
        GradientDrawable surface = new GradientDrawable();
        surface.setColor(palette.color("surface")); surface.setCornerRadius(dp(12));
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(palette.color("surfacePress")), surface, null));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(52)); params.topMargin = dp(8);
        content.addView(button, params); button.setOnClickListener(view -> action.run());
    }

    private void label(String text, int size, String role) {
        TextView label = new TextView(this); label.setText(text); label.setTextSize(size);
        label.setTextColor(palette.color(role)); label.setPadding(0, dp(12), 0, dp(8)); content.addView(label);
    }
    private int dp(int size) { return Math.round(size * getResources().getDisplayMetrics().density); }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != ThemePaletteCatalog.REQUEST_SELECT_FOLDER || result != RESULT_OK || data == null) return;
        try { ThemePaletteCatalog.persistFolder(this, data); scan(); }
        catch (Exception failure) { Toast.makeText(this, failure.getMessage(), Toast.LENGTH_LONG).show(); }
    }
    @Override protected void onDestroy() { scanner.shutdownNow(); super.onDestroy(); }
}
