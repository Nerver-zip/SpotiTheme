#!/usr/bin/env bash
verify_spotify() {
    local info version code
    info="$(adb_device shell dumpsys package com.spotify.music)"
    version="$(sed -n 's/^[[:space:]]*versionName=//p' <<<"$info" | head -1 | tr -d '\r')"
    code="$(sed -n 's/^[[:space:]]*versionCode=\([0-9]*\).*/\1/p' <<<"$info" | head -1)"
    [[ "$version" == 9.1.86.2432 && "$code" == 146555520 ]] || die "Spotify must be 9.1.86.2432 / 146555520; found $version / $code."
    say "Spotify: $version ($code)"
}
extract_spotify() {
    local path name
    mkdir -p "$WORK/original"
    INPUT_APKS=()
    while IFS= read -r path; do
        path="${path#package:}"; path="${path%$'\r'}"
        [[ "$path" == /*.apk ]] || die 'Invalid APK path from PackageManager.'
        name="${path##*/}"
        [[ ! -e "$WORK/original/$name" ]] || die 'Duplicate split APK filename.'
        adb_device pull "$path" "$WORK/original/$name" >/dev/null
    done < <(adb_device shell pm path --user 0 com.spotify.music)
    [[ -s "$WORK/original/base.apk" ]] || die 'Spotify base APK was not extracted.'
    INPUT_APKS=("$WORK/original/base.apk")
    for path in "$WORK/original/"*.apk; do
        [[ "$path" == "$WORK/original/base.apk" ]] || INPUT_APKS+=("$path")
    done
    say "Saved ${#INPUT_APKS[@]} original APKs in $WORK/original"
}
