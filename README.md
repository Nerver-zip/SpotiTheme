# SpotiTheme

Standalone theme-only Android Xposed module for pinned Spotify 9.1.86.2432.

## Attribution

Derived from [LeNerd46/SpotifyPlus](https://github.com/LeNerd46/SpotifyPlus), licensed under MIT. The original copyright and license are retained in LICENSE. Vector, DexKit research and the Catppuccin community informed the original theme work.

Development in progress: the catalog, pinned reflection profile and Encore/Compose base palette hooks are implemented. Player, media-card and library coverage, settings UI and the installer are subsequent bundles. The current module has not passed device acceptance. Startup below 50 ms is a measured acceptance target, not an established result. Known alternate-renderer limitations remain to be assessed.

Build with `./gradlew testDebugUnitTest assembleDebug`. The module targets only Spotify 9.1.86.2432 (version code 146555520) and has no runtime DexKit dependency. See [the reflection profile](docs/REFLECTION_PROFILE.md) and [startup measurement](docs/STARTUP_MEASUREMENT.md) for verification boundaries.
