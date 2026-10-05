package com.spotitheme.hooks;

import com.spotitheme.ModuleLog;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemeRuntime;
import com.spotitheme.theme.PaletteValues;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/** Unselected chips use Encore's tinted palette through a copied pinned style. */
public final class HomeFilterChipHook {
    private final ThemeRuntime runtime;
    private final Profile_9_1_86_2432 profile;
    private boolean styleObserved;
    public HomeFilterChipHook(ThemeRuntime runtime, Profile_9_1_86_2432 profile) {
        this.runtime = runtime; this.profile = profile;
    }

    public void install() throws ReflectiveOperationException {
        Field style = profile.field("home.filterChipUnselected");
        Object original = style.get(null);
        Field color = profile.field("home.filterChipColor");
        long originalColor = color.getLong(null);
        Constructor<?> constructor = (Constructor<?>) profile.resolve("home.filterChipStyle");
        Object tinted = constructor.newInstance(2,
                profile.field("home.filterChipFirstProvider").get(original),
                profile.field("home.filterChipSecondProvider").get(original),
                profile.field("home.filterChipThirdProvider").get(original), 0);
        Field provider = profile.field("home.filterChipPaletteProvider");
        Constructor<?> simple = (Constructor<?>) profile.resolve("home.filterChipSimpleStyle");
        provider.set(tinted, provider.get(simple.newInstance(2)));
        Runnable refresh = () -> {
            try {
                boolean enabled = runtime.snapshot().enabled;
                style.set(null, enabled ? tinted : original);
                color.setLong(null, enabled ? PaletteValues.pack(runtime.snapshot().palette.color("tinted")) : originalColor);
            }
            catch (IllegalAccessException failure) { ModuleLog.error("Home chip style restoration failed", failure); }
        };
        refresh.run();
        runtime.addListener(refresh);
        XposedBridge.hookMethod(profile.resolve("home.filterChip"), new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                runtime.observeComposition();
                if (!styleObserved && !Boolean.TRUE.equals(param.args[3]) && param.args[1] != null) {
                    styleObserved = true;
                    ModuleLog.info("Home unselected chip input style=" + param.args[1].getClass().getName());
                }
            }
        });
        ModuleLog.info("Home filter-chip tinted style hook installed");
    }
}
