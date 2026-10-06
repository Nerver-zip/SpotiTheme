#!/usr/bin/env bash
verify_lspatch() {
    require_command java
    require_command python3
    require_command keytool
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

# Never enable shell tracing around LSPatch signing.
# LSPatch reads each input APK's signature. Modify the original base manifest,
# then sign the staged split set with a disposable key before LSPatch re-signs it.
# The generated keystore/password are kept under /tmp and removed after patching.
patch_split_set() {
    local -a signing=() staged_inputs=() patch_inputs=()
    local store_password="" alias_password="" alias=""
    local input staged output temporary_signing_dir password_file key_password_file
    local base_count=0
    if [[ -n "${SPOTITHEME_KEYSTORE:-}" ]]; then
        [[ -f "$SPOTITHEME_KEYSTORE" ]] || die 'Custom signing keystore does not exist.'
        alias="${SPOTITHEME_KEY_ALIAS:-}"
        [[ -n "$alias" ]] || die 'Set SPOTITHEME_KEY_ALIAS with SPOTITHEME_KEYSTORE.'
        # Reading interactively avoids storing final signing credentials in the environment.
        [[ "$-" != *x* ]] || die 'Disable shell tracing before custom signing.'
        read -r -s -p 'Keystore password: ' store_password
        printf '\n'
        read -r -s -p 'Key password: ' alias_password
        printf '\n'
        signing=(-k "$SPOTITHEME_KEYSTORE" "$store_password" "$alias" "$alias_password")
    fi
    [[ "$-" != *x* ]] || die 'Disable shell tracing before temporary APK signing.'

    mkdir -p "$WORK/staged" "$WORK/lspatch-input"
    for input in "${INPUT_APKS[@]}"; do
        staged="$WORK/staged/${input##*/}"
        if [[ "${input##*/}" == base.apk ]]; then
            python3 "$INSTALLER/lib/patch_provider_queries.py" "$input" "$staged" || die 'Could not add the narrow SpotiTheme provider visibility query.'
            base_count=$((base_count + 1))
        else
            cp "$input" "$staged"
        fi
        staged_inputs+=("$staged")
    done
    [[ "$base_count" == 1 ]] || die 'Expected exactly one Spotify base APK to stage.'

    temporary_signing_dir="$(mktemp -d "${TMPDIR:-/tmp}/spottheme-lspatch-signing.XXXXXX")"
    password_file="$temporary_signing_dir/keystore-password"
    key_password_file="$temporary_signing_dir/key-password"
    python3 -c 'import secrets; print(secrets.token_urlsafe(32))' > "$password_file"
    cp "$password_file" "$key_password_file"
    chmod 600 "$password_file" "$key_password_file"
    if ! keytool -genkeypair -keystore "$temporary_signing_dir/input.jks" \
            -storepass:file "$password_file" -keypass:file "$password_file" \
            -alias spotitheme-staging -keyalg RSA -keysize 2048 -validity 3650 \
            -dname 'CN=SpotiTheme temporary LSPatch input'; then
        rm -rf "$temporary_signing_dir"
        die 'Could not create a temporary signing key for LSPatch input staging.'
    fi

    for input in "${staged_inputs[@]}"; do
        output="$WORK/lspatch-input/${input##*/}"
        if ! "$APKSIGNER" sign --ks "$temporary_signing_dir/input.jks" \
                --ks-key-alias spotitheme-staging --ks-pass "file:$password_file" \
                --key-pass "file:$key_password_file" --out "$output" "$input"; then
            rm -rf "$temporary_signing_dir"
            die "Could not sign staged LSPatch input: ${input##*/}"
        fi
        patch_inputs+=("$output")
    done

    if ! java -jar "$INSTALLER/vendor/lspatch.jar" "${patch_inputs[@]}" -l 2 -m "$MODULE_APK" \
            "${signing[@]}" -o "$WORK/patched"; then
        rm -rf "$temporary_signing_dir"
        die 'LSPatch failed to prepare the split set.'
    fi
    rm -rf "$temporary_signing_dir"
}
