#!/usr/bin/env bash
set -euo pipefail
# Never enable shell tracing here: signing values are supplied through environment variables.
for variable in RELEASE_TAG SPOTITHEME_KEYSTORE_BASE64 SPOTITHEME_KEYSTORE_PASSWORD SPOTITHEME_KEY_ALIAS SPOTITHEME_KEY_PASSWORD; do
    [[ -n "${!variable:-}" ]] || { printf 'Missing release setting: %s\n' "$variable" >&2; exit 1; }
done
python3 - <<'PY'
import json, os, re
from pathlib import Path
metadata = json.loads(Path('app/build/outputs/apk/release/output-metadata.json').read_text())
tag = os.environ['RELEASE_TAG']
if not re.fullmatch(r'v\d+\.\d+\.\d+', tag):
    raise SystemExit('Release tags must use vMAJOR.MINOR.PATCH')
outputs = metadata['elements']
if len(outputs) != 1 or outputs[0]['versionName'] != tag[1:]:
    raise SystemExit('Tag must match the single release APK versionName')
if outputs[0]['versionCode'] <= 0:
    raise SystemExit('A positive Android versionCode is required')
PY
sdk_root="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
apksigner="${APKSIGNER:-$sdk_root/build-tools/35.0.0/apksigner}"
[[ -x "$apksigner" ]] || { echo 'Android build-tools 35.0.0 apksigner is required' >&2; exit 1; }
signing_dir="$(mktemp -d)"
trap 'rm -rf "$signing_dir"' EXIT
umask 077
printf '%s' "$SPOTITHEME_KEYSTORE_BASE64" | base64 --decode > "$signing_dir/release.jks"
mkdir -p dist
output="dist/SpotiTheme-${RELEASE_TAG}.apk"
"$apksigner" sign --ks "$signing_dir/release.jks" \
    --ks-key-alias "$SPOTITHEME_KEY_ALIAS" \
    --ks-pass env:SPOTITHEME_KEYSTORE_PASSWORD --key-pass env:SPOTITHEME_KEY_PASSWORD \
    --out "$output" app/build/outputs/apk/release/app-release-unsigned.apk
"$apksigner" verify --verbose --print-certs "$output"
bundle="$signing_dir/SpotiTheme-installer"
mkdir -p "$bundle/installer/lib" "$bundle/installer/vendor"
cp "$output" "$bundle/SpotiTheme.apk"
cp scripts/install-prebuilt.sh "$bundle/install.sh"
cp installer/install.sh installer/README.md "$bundle/installer/"
cp installer/lib/*.sh installer/lib/*.py "$bundle/installer/lib/"
cp installer/vendor/lspatch.jar installer/vendor/README.md installer/vendor/LSPatch-LICENSE "$bundle/installer/vendor/"
cp LICENSE "$bundle/LICENSE"
(cd "$bundle" && sha256sum SpotiTheme.apk > SHA256SUMS)
tar -czf "dist/SpotiTheme-installer-${RELEASE_TAG}.tar.gz" -C "$signing_dir" SpotiTheme-installer
(cd dist && sha256sum "SpotiTheme-${RELEASE_TAG}.apk" "SpotiTheme-installer-${RELEASE_TAG}.tar.gz" > SHA256SUMS)
