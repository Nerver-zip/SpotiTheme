#!/usr/bin/env bash
set -euo pipefail
INSTALLER="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO="$(cd -- "$INSTALLER/.." && pwd)"
for library in ui adb spotify vector lspatch; do source "$INSTALLER/lib/$library.sh"; done
MODE=auto
CHECK_ONLY=0
while [[ $# -gt 0 ]]; do
    case "$1" in
        --root) MODE=root ;;
        --rootless) MODE=rootless ;;
        --check) CHECK_ONLY=1 ;;
        --help) say 'Usage: installer/install.sh [--root|--rootless] [--check]'; exit 0 ;;
        *) die "Unknown option: $1" ;;
    esac
    shift
done
require_command python3
select_device
verify_spotify
if [[ "$MODE" == auto ]]; then
    if has_root; then MODE=root; else MODE=rootless; fi
fi
if [[ "$MODE" == root ]]; then
    has_root || die 'Root access unavailable.'
    verify_vector
else verify_lspatch
fi
say "Install mode: $MODE"
[[ "$CHECK_ONLY" == 0 ]] || exit 0
mkdir -p "$REPO/.project"
if [[ -n "${SPOTITHEME_MODULE_APK:-}" ]]; then
    MODULE_APK="$SPOTITHEME_MODULE_APK"
    say "Using prebuilt module APK: $MODULE_APK"
else
    (cd "$REPO" && ./gradlew testDebugUnitTest assembleDebug)
    MODULE_APK="$REPO/app/build/outputs/apk/debug/app-debug.apk"
fi
[[ -s "$MODULE_APK" ]] || die 'Build did not produce the module APK.'
if [[ "$MODE" == root ]]; then install_root; else install_rootless; fi
