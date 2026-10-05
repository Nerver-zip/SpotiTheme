# MVP validation status

Updated 2026-10-05. This report describes the final debug build with SHA-256 `945ffabd8813d23147d32fbc9b2b1de229ebf2b63e76f29e204bc5bbf08f892c`.

## Scope

- Pinned target: Spotify `9.1.86.2432` / version code `146555520`.
- Device validation: Android 16 / API 36, Magisk with Zygisk, JingMatrix/Vector.
- Theme state: Catppuccin Mocha selected; theme enabled; album artwork colors and Auto Theme off after testing.
- Catalog: 29 bundled themes. Regular Catppuccin Mocha is the only bundled Mocha variant. Saved selections using either retired Mocha Mauve ID fall back to regular Catppuccin Mocha.
- Animated artwork backgrounds, animation/drift controls, blur/frosted layers and quality controls are excluded. Static Auto Theme color extraction and palette switching remain available.

## Build and device evidence

- `./gradlew testDebugUnitTest assembleDebug`: passed after the final history rewrite and catalog cleanup; 48 unit tests, no failures.
- `python3 -m unittest discover -s installer/tests -v`: passed.
- The preceding module build was loaded by Vector for Spotify. A clean startup logged `palette=catppuccin-mocha`, `enabled=true fixed=true auto=false`, and the base palette hook; no fatal exception appeared. The final APK was installed while the Connect session stayed active, so Spotify was not restarted and did not reload the final module classes during this run.
- The preceding clean-start registration measured 256.138 ms cumulative on the main thread (253.148 ms attach). The final APK differs only in the removed Mocha Mauve JSON asset and theme-ID fallback code; its hook path was not remeasured. The user accepts this range; no 50 ms threshold applies.
- A controlled Auto Theme check on the paused track logged `fixed=false auto=true` and `Auto Theme palette applied; color=ff032652 mode=neutral`. The test was then reversed in the manager. UIAutomator confirmed the restored values: artwork colors unchecked, Auto Theme unchecked, Mocha still selected, artwork palette Neutral.
- Final-build visual captures show the manager reporting 29 themes with regular Catppuccin Mocha selected; the visible picker begins with regular Mocha and contains no Mocha Mauve entry. Home/mini-player, Search, canceled playlist creation, expanded player, SongDNA, Credits, and active Connect rendering were captured from the immediately preceding build. A ZIP comparison confirms the player hook payloads were unchanged; only the retired JSON asset and settings/migration dex changed.

## Remaining acceptance limits

- Rootless/LSPatch runtime acceptance was not performed. The installer’s mocked tests and preparation code do not prove signing, preference sync or rendering on a patched install; testing this on the current phone would replace the store-signed Spotify installation.
- No suitable related-video overflow card appeared in the tested track, so the SpotiTheme build has no fresh positive visual sample for that surface.
- Selected Repeat and a positive queued-badge sample were unavailable. The cold-start media session reported Repeat off; that does not validate its selected state.
- A natural active Spotify Connect state was visible in the preceding build's Home capture: Spotify displayed `Spotifast` alongside its laptop glyph while the media session reported `PLAYING`. The glyph and label contained 746 and 559 exact pixels of Mocha accent `#A6E3A1`; neither crop contained Spotify green `#1ED760`. The final catalog APK was installed without restarting or controlling playback. Android MediaRouter2 still reported its default phone route, so this validates Spotify's in-app Connect indicator, not system-level route ownership.
- Coverage is specific to Spotify `9.1.86.2432` on the tested Android 16/Vector setup. It does not establish compatibility with other Spotify builds, Android versions, or every renderer/state.

Screens and complete device logs used for this report are retained privately under `.project/porting/evidence/2026-10-05/` in the SpotifyPlus workspace; they are intentionally not part of this repository.
