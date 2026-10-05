# SpotiTheme

Standalone theme-only Android Xposed module for pinned Spotify 9.1.86.2432.

## Attribution

Derived from [LeNerd46/SpotifyPlus](https://github.com/LeNerd46/SpotifyPlus), licensed under MIT. The original copyright and license are retained in LICENSE. Vector, DexKit research and the Catppuccin community informed the original theme work.

The JSON catalog, pinned reflection profile, player/media/library hooks, static Auto Theme color extraction, standalone manager and interactive installer are implemented. Animated artwork backgrounds and their motion, drift, blur and quality controls are excluded from the MVP. Regular Catppuccin Mocha is the only bundled Mocha palette and is the default. The installed manager shows 29 themes and migrates older Mocha Mauve selections to regular Mocha. Isolated Vector injection, fixed-palette rendering, an Auto Theme on/off cycle and the active Spotify Connect indicator have been verified on the pinned device. See [MVP validation](docs/MVP_VALIDATION.md) for tested surfaces and remaining acceptance limits. Startup is measured across registration phases; the original 50 ms aspiration is not a delivery gate. The preceding clean Spotify process registered in about 256 ms; the final catalog-only APK was installed without restarting the active Spotify Connect process.

The standalone manager includes 29 bundled palettes, defaults to Catppuccin Mocha, and supports a read-only JSON theme folder through Android's document picker. Vector settings synchronization uses its framework-supported shared-preference channel. See [installation instructions](installer/README.md) for root and rootless preparation, confirmations and validation limits.

Build with `./gradlew testDebugUnitTest assembleDebug`. The module targets only Spotify 9.1.86.2432 (version code 146555520) and has no runtime DexKit dependency. See [the reflection profile](docs/REFLECTION_PROFILE.md) and [startup measurement](docs/STARTUP_MEASUREMENT.md) for verification boundaries.
