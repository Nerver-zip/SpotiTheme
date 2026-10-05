#!/usr/bin/env bash
vector_cli() { adb_device shell su -c "/data/adb/lspd/cli $*"; }
verify_vector() {
    adb_device shell su -c 'test -x /data/adb/lspd/cli' || die 'Vector CLI is unavailable.'
    vector_cli status
}
install_root() {
    local modules present scope conflict=no
    modules="$(vector_cli modules --json ls)" || die 'Cannot inspect Vector modules.'
    present="$(python3 -c 'import json,sys; d=json.load(sys.stdin); assert d.get("success") and isinstance(d.get("data"),list); print("yes" if any(x.get("PACKAGE")=="com.lenerd46.spotifyplus" for x in d["data"]) else "no")' <<<"$modules")" || die 'Invalid Vector module response.'
    if [[ "$present" == yes ]]; then
        scope="$(vector_cli scope --json ls com.lenerd46.spotifyplus)" || die 'Cannot inspect SpotifyPlus scope.'
        conflict="$(python3 -c 'import json,sys; d=json.load(sys.stdin); assert d.get("success") and isinstance(d.get("data"),list); print("yes" if any(x.get("APP_PACKAGE")=="com.spotify.music" and x.get("USER_ID")==0 for x in d["data"]) else "no")' <<<"$scope")" || die 'Invalid Vector scope response.'
    fi
    if [[ "$conflict" == yes ]]; then
        say 'SpotifyPlus currently themes Spotify. Running both engines can cause conflicting hooks.'
        confirm 'Remove only SpotifyPlus scope com.spotify.music/0 and replace it with SpotiTheme?' || die 'No configuration changed.'
        REMOVE_SOURCE_SCOPE=1
    fi
    confirm 'Install SpotiTheme and enable its Spotify user-0 scope in Vector?' || die 'Cancelled.'
    adb_device install -r "$MODULE_APK"
    vector_cli modules enable com.spotitheme
    vector_cli scope add com.spotitheme com.spotify.music/0
    vector_cli modules --json ls | python3 -c 'import json,sys; d=json.load(sys.stdin); sys.exit(0 if d.get("success") and any(x.get("PACKAGE")=="com.spotitheme" and x.get("STATUS")=="enabled" for x in d.get("data",[])) else 1)' || die 'Vector did not enable SpotiTheme.'
    vector_cli scope --json ls com.spotitheme | python3 -c 'import json,sys; d=json.load(sys.stdin); sys.exit(0 if d.get("success") and any(x.get("APP_PACKAGE")=="com.spotify.music" and x.get("USER_ID")==0 for x in d.get("data",[])) else 1)' || die 'Vector did not apply the Spotify scope.'
    if [[ "${REMOVE_SOURCE_SCOPE:-0}" == 1 ]]; then
        vector_cli scope rm com.lenerd46.spotifyplus com.spotify.music/0
    fi
    say 'Installed. Force-stop and reopen Spotify to load SpotiTheme. Playback was not changed by this installer.'
}
