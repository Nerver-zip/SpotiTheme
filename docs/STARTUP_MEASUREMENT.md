# Startup acceptance

Startup remains measured across all module registration phases. There is no strict numeric startup acceptance threshold. The user explicitly accepts isolated device measurements around 250 ms; further optimization is not a delivery gate. Zero runtime DexKit and reliable startup remain required.

The module emits `SpotiTheme` logcat records for loader, resource and application-attach registration. Each record includes phase duration, whether it ran on the main thread, and cumulative main-thread time. Nested intervals are merged, so resource registration inside attachment is not counted twice. Unit tests check that accounting.

Require a successful `Module initialized in Spotify` record before assessing the final attachment timing. A short attachment interval after `Module initialization failed` measures an aborted registration and cannot pass the gate. Read the completed initialization record, not an early loader record, when assessing the total. The measurement covers work inside the registration phases, including profile construction, asset parsing, hook installation and resource configuration. It does not measure framework work before callback entry or unrelated Spotify startup. Device trace evidence is still needed to establish the complete startup boundary and check for unmeasured module work.

```sh
adb -s 17004dab logcat -d -v threadtime -s SpotiTheme
```

Record the exact APK hash, pinned Spotify identity, effective Vector module/scope, process identity and cold/warm results. Preserve full crash logs separately if initialization fails. Successful registration is not proof that the hooks execute; require the `Base palette hook executed` record and visual acceptance too.

Current status: on the pinned Android 16/API 36 device, the preceding clean start completed registration in 256.138 ms cumulative main-thread time (253.148 ms for attach), followed by a settings snapshot for `catppuccin-mocha` with `fixed=true` and `auto=false`; the base-palette hook executed. The final catalog APK was installed while Spotify's remote Connect session was active, and Spotify was not restarted to reload it. The final change removes the Mocha Mauve asset and migrates its legacy IDs; registration timing has not been repeated for that binary. Earlier individual launches measured 249.524 ms and 318.955 ms. These are diagnostic samples, not a statistical benchmark. The user accepts this range; 50 ms is not a delivery threshold. See [MVP validation](MVP_VALIDATION.md) for remaining UI and rootless checks.
