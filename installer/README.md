# Interactive installation

Run from the repository root:

```sh
./installer/install.sh --check
./installer/install.sh --root
./installer/install.sh --rootless
```

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
