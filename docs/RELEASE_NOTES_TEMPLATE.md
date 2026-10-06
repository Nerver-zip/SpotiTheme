Download **SpotiTheme-vMAJOR.MINOR.PATCH.apk** from Assets and install it on your phone. **SHA256SUMS** contains checksums for the APK and installer archive.

Supports Spotify **9.1.86.2432 / 146555520**. For rooted devices, enable SpotiTheme in Vector and select Spotify in its scope, then force-stop and reopen Spotify. See the README for GUI installation and theme selection.

This is the SpotiTheme manager/module APK, not a patched Spotify APK. For rootless installation without cloning or compiling, download the installer archive, extract it on a computer with the required ADB/Java/SDK tools, and run `bash install.sh --rootless`. See the installer guide for prerequisites and the first-install data-loss prompt. No Spotify APK or private signing key is included.

Release APKs use a stable release signing key. A development/debug installation signed with another key cannot be updated in place by this APK. Back up your theme files and note your selected settings before replacing such an installation; uninstalling SpotiTheme removes its local settings.
