#!/usr/bin/env bash
select_device() {
    require_command adb
    local -a devices=()
    mapfile -t devices < <(adb devices | awk 'NR>1 && $2=="device" {print $1}')
    if [[ -n "${SPOTITHEME_SERIAL:-}" ]]; then
        [[ "$(adb -s "$SPOTITHEME_SERIAL" get-state 2>/dev/null)" == device ]] || die 'Selected device is unavailable.'
        SERIAL="$SPOTITHEME_SERIAL"
    else
        [[ ${#devices[@]} == 1 ]] || die 'Connect exactly one authorized device, or set SPOTITHEME_SERIAL.'
        SERIAL="${devices[0]}"
    fi
    say "Device: $SERIAL"
}
adb_device() { adb -s "$SERIAL" "$@"; }
has_root() { [[ "$(adb_device shell su -c id 2>/dev/null)" == *'uid=0(root)'* ]]; }
