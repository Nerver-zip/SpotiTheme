#!/usr/bin/env bash
set -euo pipefail
bundle_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$bundle_root"
[[ -s SpotiTheme.apk && -s SHA256SUMS ]] || { echo 'Run this script from an extracted SpotiTheme installer release bundle.' >&2; exit 1; }
sha256sum --check SHA256SUMS
export SPOTITHEME_MODULE_APK="$bundle_root/SpotiTheme.apk"
exec bash "$bundle_root/installer/install.sh" "$@"
