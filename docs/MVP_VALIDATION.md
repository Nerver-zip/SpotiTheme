# MVP validation status

Updated 2026-10-06. The latest manager/module APK SHA-256 is `6070e2217c98a243f9d836e295a1d62b61437bb997715e3d8d0f74e1d1cb3073`. This is a development checkpoint, not a frozen MVP acceptance build.

## Scope

- Pinned target: Spotify `9.1.86.2432` / version code `146555520`.
- Device validation: Android 16 / API 36, Magisk with Zygisk, JingMatrix/Vector.
- Theme state: Catppuccin Latte selected for the README player capture; theme enabled; album artwork colors off; Auto Theme off and disabled; Show Spotify artwork blur off.
- Catalog: 29 bundled themes. Regular Catppuccin Mocha is the only bundled Mocha variant. Saved selections using either retired Mocha Mauve ID fall back to regular Catppuccin Mocha.
- Animated artwork backgrounds and their motion, drift, blur/frosted and quality controls are excluded. Spotify's native blurred artwork layer has a separate switch and does not affect Auto Theme or palette colors.

## Build and device evidence

- `./gradlew testDebugUnitTest assembleDebug`: passed; 53 unit tests, no failures.
- `python3 -m unittest discover -s installer/tests -v`: passed; 7 tests. `bash -n installer/install.sh installer/lib/*.sh` and `git diff --check` also passed.
- A clean rootless startup logged `Module initialized in Spotify`, `Settings snapshot received; palette=catppuccin-latte`, `Base palette hook executed; palette=catppuccin-latte`, and `Artwork mode configured; enabled=true fixed=true auto=false showArtworkBackdrop=false`. No `Auto Theme palette applied` record appeared; Spotify remained in `MainActivity` with its media session paused.
- The final rootless APK set was prepared from the pinned store-signed split backup, whose SHA-256 records verified before build. All four LSPatch splits passed API 36 signer verification, and their signer matched the installed patch. `adb install-multiple -r` updated Spotify in place without uninstalling it or deleting app data. The patched base manifest contains the narrow `com.spotitheme.settings` provider query and does not request `QUERY_ALL_PACKAGES`.
- UIAutomator confirmed `Use album artwork colors=false`, `Auto Theme=false` and disabled, and `Show Spotify artwork blur=false`. Turning only the blur switch on produced `fixed=true auto=false showArtworkBackdrop=true` at runtime; turning it back off restored `showArtworkBackdrop=false`. The selected Latte palette remained unchanged. Runtime mode and UI behavior passed; a fresh visual A/B of both backdrop states in the expanded player remains open.
- The new Latte player screenshot replaces the previous artwork-dominated capture with a genuine device capture of the fixed-palette player while the blur layer was hidden. The gray Spotify player gradient remains visible, so the screenshot is evidence of the current rendered surface, not proof that every expanded-player background color matches Latte.
- The device still has Magisk, but Spotify was removed from Vector scope for this LSPatch run. This validates rootless injection and manager-to-provider palette switching on the test handset, not a separate truly-unrooted device.

## Remaining acceptance limits

- A separate truly-unrooted-device run remains outstanding.
- **Open regression:** expanded-player artwork is visible with Show Spotify artwork blur enabled, but the user confirms it disappears with the switch disabled. The current hook paints a solid background on the expanded root `content` in that state. The blur-specific resource target has not been observed on the pinned layout. Remove the root override and identify the actual blur renderer before claiming visual acceptance; preserve foreground artwork, Canvas and video. Spotify's native gray gradient and light-theme contrast also remain unaccepted.
- No suitable related-video overflow card appeared in the tested track, so the SpotiTheme build has no fresh positive visual sample for that surface.
- Selected Repeat and a positive queued-badge sample were unavailable. The cold-start media session reported Repeat off; that does not validate its selected state.
- A natural active Spotify Connect state was visible in an earlier build's Home capture: Spotify displayed `Spotifast` alongside its laptop glyph while the media session reported `PLAYING`. Android MediaRouter2 still reported its default phone route, so this validates Spotify's in-app Connect indicator, not system-level route ownership; the icon was not re-captured after this update.
- Coverage is specific to Spotify `9.1.86.2432` on the tested Android 16/Vector setup. It does not establish compatibility with other Spotify builds, Android versions, or every renderer/state.

Earlier screens and device logs remain private under `.project/porting/evidence/2026-10-05/`; rootless test evidence remains private under `.project/rootless-test.Ju0SMB/`. These ignored artifacts are intentionally not part of the repository.

## Latest artwork checkpoint

The latest source was built with Temurin JDK 21; all 53 Android unit tests passed. Seven installer tests, shell syntax validation and diff whitespace checks passed. The module was embedded into the preserved pinned Spotify split set and installed as a same-signer in-place update. An expanded-player capture confirms foreground artwork with the blur switch enabled only. Earlier screenshots and runtime preference checks do not establish that disabling blur preserves artwork. No playback action is required for the next paused-track A/B check.
