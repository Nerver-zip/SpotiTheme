# CI and APK releases

Users download the installable SpotiTheme APK from [GitHub Releases](https://github.com/Nerver-zip/SpotiTheme/releases/latest). They do not need to clone or build the project for rooted GUI installation. Rootless users download the installer archive containing the prebuilt module and existing patching tools; no clone or Gradle build is needed, but host tools remain required. Releases do not redistribute Spotify.

## Continuous integration

CI runs on pushes to main, pull requests and manual dispatch. It uses JDK 21, Android platform 35 and build-tools 35.0.0, runs Android unit tests and lint, checks installer tests and shell syntax, and uploads a development APK and reports for 14 days. Development APKs use the runner's debug key and are not stable release/update artifacts.

## One-time release signing setup

Create a dedicated signing keystore using Android Studio's **Build → Generate Signed Bundle / APK → APK → Create new**, or reuse an existing project release key. Keep an offline backup of the keystore, alias and passwords. Do not replace the key between releases; Android requires the same signing identity for updates. Do not upload the keystore to Git or send passwords in issues.

In GitHub **Settings → Secrets and variables → Actions → New repository secret**, configure:

| Secret | Value |
| --- | --- |
| `SPOTITHEME_KEYSTORE_BASE64` | Base64-encoded keystore contents |
| `SPOTITHEME_KEYSTORE_PASSWORD` | Keystore password |
| `SPOTITHEME_KEY_ALIAS` | Signing-key alias |
| `SPOTITHEME_KEY_PASSWORD` | Key password |

Encode the keystore locally with a trusted tool, never a public online encoder. For maintainers using a terminal, `base64 -w 0 /path/to/release.jks` produces the value on Linux. Treat the encoded value as sensitive as the original file. GitHub supports [repository secrets and Base64 binary values](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-secrets).

Release signing values are exposed only to the tag workflow's signing step. The temporary decoded keystore is removed on exit; only the signed APK and checksum are published. Missing signing settings fail the release instead of falling back to a debug key.

## Publish a version

1. Update `versionName` in `app/build.gradle` and increase `versionCode` for every release. The first configured version is 0.1.0 / code 1.
2. Merge the reviewed changes into main and verify CI passes. Keep any device acceptance results current in `docs/MVP_VALIDATION.md`.
3. Push a tag matching the version exactly, such as `v0.1.0`. Use only `vMAJOR.MINOR.PATCH`; the workflow rejects mismatched tags.
4. The tag workflow repeats unit tests, release lint and installer checks, builds and signs the release APK, verifies the APK signature, computes SHA-256, and publishes a GitHub Release with generated change notes and installation guidance.
5. Confirm Assets contains `SpotiTheme-v0.1.0.apk`, `SpotiTheme-installer-v0.1.0.tar.gz` and `SHA256SUMS`. Test installation and an update with the same key before announcing the release.

If a workflow fails before publication, fix the cause and rerun the failed workflow from Actions. It never overwrites an existing release or silently replaces its APK. Do not move a published tag or replace its signing key.

A debug-signed local installation cannot accept an in-place update from the dedicated release key. Replacing that installation requires uninstalling SpotiTheme and loses its local settings; imported JSON files outside the app remain separate. This limitation concerns SpotiTheme's signature, not Spotify's independent LSPatch signing key.

## Validation boundaries

Host CI proves build, unit-test and lint gates. It does not prove Spotify/Vector injection or visual correctness on a phone. Rootless preparation and a truly unrooted handset remain subject to the acceptance limits documented in the README.
