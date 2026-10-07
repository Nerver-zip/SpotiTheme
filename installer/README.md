# Interactive installation

## Install without cloning

Download the installer archive from [GitHub Releases](https://github.com/Nerver-zip/SpotiTheme/releases/latest), extract it, and open a terminal in the extracted `SpotiTheme-installer` folder. Use the top-level `install.sh` wrapper for either host-based route:

```sh
bash install.sh --root --check
bash install.sh --root
```

This installs the bundled release APK and enables/scopes SpotiTheme through Vector. For rootless installation, run:

```sh
bash install.sh --rootless --check
bash install.sh --rootless
```

Rooted users can also install the release APK through Android and configure Vector entirely in the phone UI described in the README.

The archive includes the signed release module and the pinned patcher, checks the module checksum before delegation, and reuses the same installer logic. It needs no Git checkout, Gradle or app compilation. Keep Bash, ADB and Python 3 available; rootless preparation additionally needs JDK 21 (`java` and `keytool`) and Android SDK build-tools 35.0.0 (`apksigner`). Set `ANDROID_HOME` to your SDK or `APKSIGNER` to the executable. Enable USB debugging and approve the connected computer on the phone.

Start with the supported store-signed Spotify build. This first-install flow must not be used to re-patch an existing LSPatch installation. Keep the extracted directory and its `.project` backups; first replacement deletes Spotify's local data. A same-signer patched update is a separate maintenance operation.

## Install from source

Run from the repository root:

```sh
./installer/install.sh --check
./installer/install.sh --root
./installer/install.sh --rootless
```

For a rooted installation using only Android and Vector screens, see [Manual installation through the phone UI](../README.md#manual-installation-through-the-phone-ui). The commands in this guide describe the host-based installer; both root and rootless modes use the downloaded bundle wrapper described above.

The default mode detects root. `--check` only checks the device, Spotify identity and selected framework/tool prerequisites. It does not build, install or change scope. Set `SPOTITHEME_SERIAL` when multiple devices are connected.

Requirements: Bash, ADB, Python 3, a JDK compatible with the Gradle build, and the configured Android SDK. Rootless preparation also requires Android SDK `apksigner`; set `APKSIGNER` if it is not found in the SDK or on PATH. The module only supports Spotify **9.1.86.2432 / 146555520**, user 0. The installer refuses other versions rather than upgrading or downgrading Spotify.

## Root / Vector

Vector must already be installed and operational, with `/data/adb/lspd/cli` available. The installer builds and tests the module before requesting installation confirmation. It installs the APK, enables `com.spotitheme` and adds `com.spotify.music/0` to its scope. If SpotifyPlus currently scopes Spotify, a separate prompt offers removal of only that target scope. SpotifyPlus preferences and all Magisk/Zygisk/framework modules remain intact.

Force-stop and reopen Spotify after successful installation. Scope changes cannot remove hooks from a running process. The installer does not start playback. Other installed theme modules must be isolated separately before acceptance.

## Rootless / LSPatch

The installer pulls all APKs returned by `pm path`, preserving the base and configuration splits in an ignored `.project/lspatch-install.*` directory. It runs the pinned local JAR with `-m` to embed SpotiTheme and explicit signature-bypass level `-l 2`. It does not use `--manager`, whose configuration is separate from embedded-module mode.

The original and patched APKs remain available for review. Only after successful patch preparation, split-count validation and verification that every APK has a valid signature for the connected device API and the same signer certificate does the installer ask permission to replace Spotify. **Replacing a store-signed application deletes its local data and requires signing in again.** A cancellation preserves the prepared files and leaves Spotify installed. An installation failure preserves originals for recovery using `adb install-multiple` with the entire original split set.

The module manager APK is installed separately so themes can be configured. Rootless preparation adds only the narrow provider-authority query for `com.spotitheme.settings`; it does not add `QUERY_ALL_PACKAGES`. On the Android 16 test handset, the same-signer split set updated in place, manager-selected Latte reached Spotify's hook, and Spotify remained outside Vector scope. The handset still has Magisk, so repeat acceptance on a separate truly-unrooted device. Do not enable a second root framework for the same patched target.

The vendored patcher uses its built-in signing key by default. To use your own key:

```sh
SPOTITHEME_KEYSTORE=/absolute/path/signing.jks SPOTITHEME_KEY_ALIAS=spotify ./installer/install.sh --rootless
```

The installer prompts privately for both passwords and rejects missing key files or aliases. LSPatch itself requires passwords as process arguments, so run custom signing on a trusted host without shell tracing. Keep the same keystore and alias for subsequent patched updates; do not commit signing keys or credentials. See [vendor provenance](vendor/README.md).

## Installer regression checks

Run `python3 installer/tests/test_vector.py`, `python3 installer/tests/test_signing.py` and `bash -n installer/install.sh installer/lib/*.sh`. The mocked tests cover invalid framework responses, failed replacement scope verification, an absent source module and successful scope-transition ordering; they execute no device commands. Signing tests exercise default/custom argument assembly, paths and aliases containing spaces, hidden password handling, and missing-key/alias rejection without invoking Java.
