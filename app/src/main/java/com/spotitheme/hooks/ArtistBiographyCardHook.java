package com.spotitheme.hooks;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;
import com.spotitheme.ModuleLog;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/** Biography body colors are separate from its photo-overlay title. */
public final class ArtistBiographyCardHook {
    private final ThemeRuntime runtime;
    private final Map<View, Drawable.ConstantState> cards = new WeakHashMap<>();
    private final Map<TextView, ColorStateList[]> descriptions = new WeakHashMap<>();
    private final Map<TextView, ColorStateList> photoTitles = new WeakHashMap<>();

    public ArtistBiographyCardHook(ThemeRuntime runtime) { this.runtime = runtime; }

    public void install() {
        XposedHelpers.findAndHookMethod(View.class, "onAttachedToWindow", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                if (!ViewScopes.expandedPlayer(view)) return;
                String name = ViewScopes.resource(view);
                if (view instanceof TextView && ViewScopes.ancestor(view, "creator_biography_card")) {
                    TextView text = (TextView) view;
                    if ("creator_bio_title".equals(name)) {
                        int id = view.getResources().getIdentifier("dark_overmedia_text_base", "color", "com.spotify.music");
                        if (id == 0) return;
                        photoTitles.put(text, view.getResources().getColorStateList(id, view.getContext().getTheme()));
                        text.setTextColor(photoTitles.get(text));
                        return;
                    }
                    if ("creator_description".equals(name)) {
                        if (!descriptions.containsKey(text))
                            descriptions.put(text, new ColorStateList[]{text.getTextColors(), text.getLinkTextColors()});
                        refreshDescription(text);
                        return;
                    }
                }
                if (!"creator_biography_card".equals(name)) return;
                Drawable background = view.getBackground();
                if (!(background instanceof GradientDrawable) || background.getConstantState() == null) return;
                if (!cards.containsKey(view)) cards.put(view, background.getConstantState());
                refreshCard(view);
            }
        });
        runtime.addListener(() -> {
            for (View card : new ArrayList<>(cards.keySet()))
                if (card != null && card.isAttachedToWindow()) refreshCard(card);
            for (TextView text : new ArrayList<>(descriptions.keySet()))
                if (text != null && text.isAttachedToWindow()) refreshDescription(text);
            for (TextView text : new ArrayList<>(photoTitles.keySet()))
                if (text != null && text.isAttachedToWindow()) text.setTextColor(photoTitles.get(text));
        });
        ModuleLog.info("Artist biography card and description hooks installed");
    }

    private void refreshCard(View view) {
        Drawable replacement = cards.get(view).newDrawable(view.getResources()).mutate();
        if (fixed()) ((GradientDrawable) replacement).setColor(runtime.snapshot().palette.color("surface"));
        view.setBackground(replacement);
    }

    private void refreshDescription(TextView text) {
        ColorStateList[] original = descriptions.get(text);
        text.setTextColor(fixed() ? ColorStateList.valueOf(runtime.snapshot().palette.color("textSubdued")) : original[0]);
        text.setLinkTextColor(fixed() ? ColorStateList.valueOf(runtime.snapshot().palette.color("text")) : original[1]);
    }

    private boolean fixed() { return runtime.snapshot().enabled && runtime.snapshot().fixed; }
}
