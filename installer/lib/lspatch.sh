#!/usr/bin/env bash
verify_lspatch() {
    require_command java
    if [[ -z "${APKSIGNER:-}" ]]; then
        if command -v apksigner >/dev/null; then APKSIGNER="$(command -v apksigner)";
        else
            local sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
            if [[ -z "$sdk" && -f "$REPO/local.properties" ]]; then
                sdk="$(sed -n 's/^sdk.dir=//p' "$REPO/local.properties")"
            fi
            APKSIGNER="$(find "$sdk/build-tools" -maxdepth 2 -name apksigner -type f 2>/dev/null | sort -V | tail -1)"
        fi
    fi
    [[ -x "$APKSIGNER" ]] || die 'Set APKSIGNER to the Android SDK apksigner executable.'
    local actual
    actual="$(sha256sum "$INSTALLER/vendor/lspatch.jar" | awk '{print $1}')"
    [[ "$actual" == d238fdc414d121b7fa454d8b4ccf420df3a8c97d563761861ff92bd9c5da2165 ]] || die 'Vendored LSPatch checksum mismatch.'
}
install_rootless() {
    verify_lspatch
    WORK="$(mktemp -d "$REPO/.project/lspatch-install.XXXXXX")"
    extract_spotify
    mkdir -p "$WORK/patched"
    patch_split_set
    local -a patched=()
    mapfile -t patched < <(find "$WORK/patched" -maxdepth 1 -type f -name '*.apk' | sort)
    [[ ${#patched[@]} == ${#INPUT_APKS[@]} ]] || die "Patched split count differs from originals; inspect $WORK."
    validate_patched "${patched[@]}"
    say "Prepared patched APKs in $WORK/patched. Originals are preserved locally."
    say 'Replacing a store-signed Spotify requires uninstalling it, which deletes local app data and requires signing in again.'
    confirm 'Uninstall Spotify user 0 and install this patched split set?' || die "Cancelled; prepared APKs remain in $WORK."
    adb_device install -r "$MODULE_APK"
    adb_device uninstall --user 0 com.spotify.music
    if ! adb_device install-multiple "${patched[@]}"; then
        say "Installation failed. Original APKs remain at $WORK/original."
        die 'Restore using adb install-multiple with all original APKs before retrying.'
    fi
    say 'Patched Spotify installed. Open SpotiTheme to select a theme, then reopen Spotify.'
}

validate_patched() {
    local sdk apk output digest first=""
    sdk="$(adb_device shell getprop ro.build.version.sdk | tr -d '\r')"
    [[ "$sdk" =~ ^[0-9]+$ ]] || die 'Could not determine device API level.'
    for apk in "$@"; do
        output="$("$APKSIGNER" verify --min-sdk-version "$sdk" --print-certs "$apk")" || die "Signature validation failed: $apk"
        digest="$(sed -n 's/^Signer #1 certificate SHA-256 digest: //p' <<<"$output")"
        [[ -n "$digest" ]] || die 'Missing signer certificate.'
        if [[ -z "$first" ]]; then first="$digest"; fi
        [[ "$digest" == "$first" ]] || die 'Patched split certificates differ.'
    done
    say "All patched APK signatures verified for device API $sdk with one certificate."
}

# LSPatch requires password arguments; never enable shell tracing around this call.
patch_split_set() {
    local -a signing=()
    local store_password="" alias_password="" alias=""
    if [[ -n "${SPOTITHEME_KEYSTORE:-}" ]]; then
        [[ -f "$SPOTITHEME_KEYSTORE" ]] || die 'Custom signing keystore does not exist.'
        alias="${SPOTITHEME_KEY_ALIAS:-}"
        [[ -n "$alias" ]] || die 'Set SPOTITHEME_KEY_ALIAS with SPOTITHEME_KEYSTORE.'
        # Reading interactively avoids storing credentials in environment variables.
        [[ "$-" != *x* ]] || die 'Disable shell tracing before custom signing.'
        read -r -s -p 'Keystore password: ' store_password
        printf '\n'
        read -r -s -p 'Key password: ' alias_password
        printf '\n'
        signing=(-k "$SPOTITHEME_KEYSTORE" "$store_password" "$alias" "$alias_password")
    fi
    java -jar "$INSTALLER/vendor/lspatch.jar" "${INPUT_APKS[@]}" -l 2 -m "$MODULE_APK"         "${signing[@]}" -o "$WORK/patched"
}
