# Validation notes

Updated 2026-10-06. Clean manager/module SHA-256: 09f737f5a4233a2384bb8d6c57e767c63655e9766c36baecd2072fd781611878. This document separates confirmed behavior from remaining device coverage.

## Scope and behavior

- Pinned target: Spotify 9.1.86.2432 / version code 146555520, Android 16 / API 36.
- Rootless injection uses LSPatch on a Magisk handset with Spotify outside Vector scope. A truly unrooted handset remains untested.
- The catalog contains 29 themes. The test palette is Catppuccin Latte, with fixed palette enabled and Auto Theme off.
- Confirmed square-cover artwork receives a flat theme background behind the image and no native overlay gradient. Foreground artwork is preserved.
- Confirmed ready featured-video surfaces retain Spotify's native backdrop treatment. Unknown or unready media passes through unchanged.
- The manual artwork-backdrop toggle has been removed. Its old stored key is ignored; preferences are not deleted. Auto Theme and Use album artwork colors remain independent.

## Confirmed renderer evidence

The pinned OverlayHidingGradientBackgroundView constructor stores the same GradientDrawable in its R0 field and its actual View background. The previous hook replaced only R0: runtime inspection showed a themed field alongside the original gray/black View background. The new hook synchronizes both references and restores native state for video or unknown media. Broad backgrounds on the player page and controls roots have been removed.

Still-artwork classification requires the pinned square-cover layout's music_container, cover_art_container and decoded image bitmap inside the active track carousel. Video classification requires its actual VideoSurfaceView with a shown, ready SurfaceView or TextureView. Playback state and generic image/video names are not classification evidence. Lower-page video cards are excluded.

The separate blurred_background_image_view resource was not observed in this player. Full-screen Canvas/image paths without the confirmed square-cover binding remain unknown; this change does not claim a universal blur-renderer mapping.

## Build and visual checks

- Clean Android build and 53 unit tests passed with Temurin JDK 21. Seven installer tests, shell syntax and diff whitespace checks also passed.
- All 109 declared profile method targets matched the saved pinned-APK method inventory.
- On the product candidate, the static cover remained pixel-identical in the compared artwork region. Its background sampled #EFF1F5, the selected Latte background.
- Reopening Spotify reproduced STILL_ARTWORK with nativeBackdrop=false and visible artwork.
- The Gladiator movie clip remained visible with FEATURED_VIDEO and nativeBackdrop=true on the product candidate.
- An orientation request and return preserved artwork, but Spotify kept this expanded-player screen in portrait. This is not landscape acceptance.
- Same-signer, in-place rootless updates preserve login and application data. All four splits passed combined API 36 signature/certificate verification. The provider query remains narrow.

## Remaining coverage

- The clean rootless artifact was installed in place. Final static-cover and featured-video captures passed. The manager visually confirms the retired toggle is absent; Latte, fixed mode and Auto Theme off remain selected.
- Truly unrooted hardware and actual landscape player rendering remain unvalidated.
- Unconfirmed Canvas/full-screen image layouts intentionally retain native rendering.
- Selected Repeat, a positive queued badge and a suitable related-video overflow sample remain unavailable. Active Connect was observed in-app; it does not establish Android system-route ownership.
- Coverage is limited to the pinned Spotify build and tested Android environment.

Diagnostic screenshots, logs and artifact hashes are kept in the ignored .project directory.
