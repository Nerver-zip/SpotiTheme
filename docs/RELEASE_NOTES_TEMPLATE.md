SpotiTheme brings **29 built-in themes** and custom JSON palettes to Spotify on Android. Pick a theme in the app, or select a folder containing your own palettes.

The expanded player keeps album artwork visible against your theme background and preserves Spotify’s native backdrop for featured videos. Auto Theme and album artwork colors remain separate options.

Requires **Spotify 9.1.86.2432** (version code **146555520**).

- **Rooted phone:** download the APK below, install it, then enable SpotiTheme in Vector with Spotify selected in its scope. Force-stop and reopen Spotify.
- **Rootless setup:** download the installer archive and extract it on your computer. With ADB, Java and Android SDK signing tools available, run `bash install.sh --rootless`. There’s no need to clone or compile the project. The first patched installation replaces Spotify and clears its local data, so you’ll need to sign in again.

See the [installation guide](https://github.com/Nerver-zip/SpotiTheme#installation) for the full steps. Checksums are included in `SHA256SUMS`.

If you’re switching from a development build, save your theme settings first: the release uses a different signing key, so you’ll need to uninstall the development version of SpotiTheme before installing this APK. Future release APKs will use the same key.
