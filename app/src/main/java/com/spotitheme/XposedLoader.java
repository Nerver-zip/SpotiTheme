package com.spotitheme;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import com.spotitheme.hooks.BasePaletteHook;
import com.spotitheme.hooks.ConnectBitmapHook;
import com.spotitheme.hooks.SavedIndicatorHook;
import com.spotitheme.hooks.MiniBackgroundHook;
import com.spotitheme.hooks.MiniTextHook;
import com.spotitheme.hooks.MiniTransportHook;
import com.spotitheme.hooks.MiniProgressHook;
import com.spotitheme.hooks.MiniQueuedBadgeHook;
import com.spotitheme.hooks.NativeStatusHook;
import com.spotitheme.hooks.CreditsCardHook;
import com.spotitheme.hooks.SystemBarsHook;
import com.spotitheme.hooks.ExpandedBackgroundHook;
import com.spotitheme.hooks.RelatedVideoHook;
import com.spotitheme.hooks.PlayerActionsHook;
import com.spotitheme.hooks.SongDnaHook;
import com.spotitheme.hooks.RepeatIconHook;
import com.spotitheme.hooks.PlayerHeaderHook;
import com.spotitheme.hooks.SearchLandingHook;
import com.spotitheme.hooks.PlaylistCreationHook;
import com.spotitheme.hooks.LibraryHeaderHook;
import com.spotitheme.hooks.PlaylistHeaderScope;
import com.spotitheme.hooks.PlaylistTextHook;
import com.spotitheme.hooks.PlaylistGradientHook;
import com.spotitheme.hooks.PlaylistAppBarHook;
import com.spotitheme.hooks.AlbumComposeGradientHook;
import com.spotitheme.hooks.LegacyAlbumGradientHook;
import com.spotitheme.hooks.MainContentBackgroundHook;
import com.spotitheme.hooks.LandscapeSidePanelHook;
import com.spotitheme.hooks.SideDrawerHook;
import com.spotitheme.hooks.ArtistBiographyCardHook;
import com.spotitheme.hooks.ArtistBiographyPaletteHook;
import com.spotitheme.hooks.HomeFilterChipHook;
import com.spotitheme.hooks.HomeHeaderHook;
import com.spotitheme.hooks.HomeShortcutHook;
import com.spotitheme.hooks.HomeShortcutTitleHook;
import com.spotitheme.hooks.HomeCarouselTextHook;
import com.spotitheme.hooks.NativeViewPaletteHook;
import com.spotitheme.hooks.ActivityPaletteHook;
import com.spotitheme.hooks.TrackArtworkHook;
import com.spotitheme.theme.ArtworkThemeController;
import com.spotitheme.hooks.NavigationGradientHook;
import com.spotitheme.hooks.ModernAlbumHeaderHook;
import com.spotitheme.hooks.ArtistHeaderChromeHook;
import com.spotitheme.hooks.ArtistBodyScrimHook;
import com.spotitheme.hooks.TrackRowColorsHook;
import com.spotitheme.hooks.CountdownPhotoTitleHook;
import com.spotitheme.hooks.AlbumSavedIndicatorHook;
import com.spotitheme.hooks.ThemeResourcesHook;
import com.spotitheme.profile.Profile_9_1_86_2432;
import com.spotitheme.theme.ThemePalette;
import com.spotitheme.theme.ThemePaletteParser;
import com.spotitheme.theme.ThemeRuntime;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import android.content.res.XModuleResources;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public final class XposedLoader implements IXposedHookLoadPackage, IXposedHookInitPackageResources, IXposedHookZygoteInit {
    public static final String SPOTIFY = "com.spotify.music";
    private final AtomicBoolean initialized = new AtomicBoolean();
    private String modulePath;
    private ThemeRuntime runtime;
    private ArtworkThemeController artworkController;

    @Override public void initZygote(StartupParam param) { modulePath = param.modulePath; }

    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) {
        if (!SPOTIFY.equals(param.packageName) || !SPOTIFY.equals(param.processName)) return;
        long started = StartupMetrics.start();
        try {
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam hook) {
                    if (hook.hasThrowable() || !initialized.compareAndSet(false, true)) return;
                    initialize((Context) hook.args[0], (Application) hook.thisObject, param.classLoader);
                }
            });
        } finally {
            StartupMetrics.finish("loadPackage", started);
        }
    }

    private void initialize(Context context, Application application, ClassLoader loader) {
        long started = StartupMetrics.start();
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(SPOTIFY, 0);
            long code = Build.VERSION.SDK_INT >= 28 ? info.getLongVersionCode() : info.versionCode;
            if (!Profile_9_1_86_2432.supports(info.versionName, code)) {
                ModuleLog.info("Unsupported Spotify version; hooks skipped: " + info.versionName + " / " + code);
                return;
            }
            Profile_9_1_86_2432 profile = new Profile_9_1_86_2432(info.versionName, code, loader);
            android.content.res.AssetManager assets = XModuleResources.createInstance(modulePath, null).getAssets();
            ThemePalette palette;
            try (InputStream stream = assets.open("themes/catppuccin-mocha-mauve-bundled.json")) {
                palette = ThemePaletteParser.parse(stream);
            }
            runtime = new ThemeRuntime(palette, true, true);
            runtime.addListener(this::applyResources);
            ThemePalette nativePalette;
            try (InputStream stream = assets.open("themes/spotify-dark.json")) {
                nativePalette = ThemePaletteParser.parse(stream);
            }
            ModuleLog.info("Registration checkpoint=assets elapsedMs="
                    + (StartupMetrics.start() - started) / 1_000_000.0);
            new NativeViewPaletteHook(runtime, nativePalette, profile).install();
            BasePaletteHook palettes = new BasePaletteHook(runtime, profile);
            palettes.install();
            ModuleLog.info("Registration checkpoint=base-palettes elapsedMs="
                    + (StartupMetrics.start() - started) / 1_000_000.0);
            new ConnectBitmapHook(runtime, profile).install();
            new SavedIndicatorHook(runtime, profile).install();
            new MiniBackgroundHook(runtime, profile).install();
            new MiniTextHook(runtime, profile).install();
            new MiniTransportHook(runtime, profile).install();
            new MiniProgressHook(runtime).install();
            new MiniQueuedBadgeHook(runtime).install();
            new NativeStatusHook(runtime, profile).install();
            new CreditsCardHook(runtime, profile).install();
            new SystemBarsHook(runtime).install(application);
            new ExpandedBackgroundHook(runtime, profile).install();
            new RelatedVideoHook(runtime, profile).install();
            new PlayerActionsHook(runtime, profile).install();
            new SongDnaHook(runtime, profile).install();
            new RepeatIconHook(runtime).install();
            new PlayerHeaderHook(runtime, profile).install();
            ModuleLog.info("Registration checkpoint=players elapsedMs="
                    + (StartupMetrics.start() - started) / 1_000_000.0);
            new SearchLandingHook(runtime, profile).install();
            new PlaylistCreationHook(runtime, profile).install();
            new LibraryHeaderHook(runtime, profile).install();
            PlaylistHeaderScope playlistHeader = new PlaylistHeaderScope();
            playlistHeader.install();
            new PlaylistTextHook(runtime, profile, playlistHeader).install();
            new PlaylistGradientHook(runtime, profile, playlistHeader).install();
            new PlaylistAppBarHook(runtime, playlistHeader).install();
            new AlbumComposeGradientHook(runtime, profile, context).install();
            new LegacyAlbumGradientHook(runtime, profile).install();
            new MainContentBackgroundHook(runtime).install();
            new LandscapeSidePanelHook(runtime).install();
            new SideDrawerHook(runtime, profile).install();
            new ArtistBiographyCardHook(runtime).install();
            new ArtistBiographyPaletteHook(runtime, profile).install();
            new HomeFilterChipHook(runtime, profile).install();
            new HomeHeaderHook(runtime, profile).install();
            new HomeShortcutHook(runtime).install();
            new HomeShortcutTitleHook(runtime, profile).install();
            new HomeCarouselTextHook(runtime).install();
            new NavigationGradientHook(runtime, profile).install();
            new ModernAlbumHeaderHook(runtime, profile).install();
            new ArtistHeaderChromeHook(runtime, profile).install();
            new ArtistBodyScrimHook(runtime, profile, palettes).install();
            new TrackRowColorsHook(runtime, profile).install(context);
            new CountdownPhotoTitleHook(runtime, profile).install();
            new AlbumSavedIndicatorHook(runtime, profile).install();
            ModuleLog.info("Registration checkpoint=surface-hooks elapsedMs="
                    + (StartupMetrics.start() - started) / 1_000_000.0);
            new ActivityPaletteHook(runtime).install(application);
            artworkController = new ArtworkThemeController(runtime);
            new TrackArtworkHook(profile, artworkController::observe).install();
            ModuleLog.info("Registration checkpoint=lifecycle-artwork elapsedMs="
                    + (StartupMetrics.start() - started) / 1_000_000.0);
            applyResources();
            ModuleLog.info("Module initialized in Spotify; palette=" + palette.getId());
        } catch (Throwable failure) {
            ModuleLog.error("Module initialization failed", failure);
        } finally {
            StartupMetrics.finish("attach", started);
        }
    }

    private void applyResources() {
        ThemeResourcesHook.configure(runtime.snapshot());
    }

    @Override public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam param) {
        if (!SPOTIFY.equals(param.packageName)) return;
        long started = StartupMetrics.start();
        try { new ThemeResourcesHook().handleInitPackageResources(param); }
        finally { StartupMetrics.finish("resources", started); }
    }
}
