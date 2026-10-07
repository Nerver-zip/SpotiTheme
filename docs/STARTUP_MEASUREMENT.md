# Startup acceptance

Startup remains measured across all module registration phases. There is no strict numeric startup acceptance threshold. The user explicitly accepts isolated device measurements around 250 ms; further optimization is not a delivery gate. Zero runtime DexKit and reliable startup remain required.

The module emits `SpotiTheme` logcat records for loader, resource and application-attach registration. Each record includes phase duration, whether it ran on the main thread, and cumulative main-thread time. Nested intervals are merged, so resource registration inside attachment is not counted twice. Unit tests check that accounting.

Require a successful `Module initialized in Spotify` record before assessing the final attachment timing. A short attachment interval after `Module initialization failed` measures an aborted registration and cannot pass the gate. Read the completed initialization record, not an early loader record, when assessing the total. The measurement covers work inside the registration phases, including profile construction, asset parsing, hook installation and resource configuration. It does not measure framework work before callback entry or unrelated Spotify startup. Device trace evidence is still needed to establish the complete startup boundary and check for unmeasured module work.

```sh
adb -s 17004dab logcat -d -v threadtime -s SpotiTheme
```

Record the exact APK hash, pinned Spotify identity, effective Vector module/scope, process identity and cold/warm results. Preserve full crash logs separately if initialization fails. Successful registration is not proof that the hooks execute; require the `Base palette hook executed` record and visual acceptance too.

Current status: on the pinned Android 16/API 36 device, the latest clean start of the final rootless build completed registration in 241.164 ms cumulative main-thread time (239.448 ms for attach), then loaded `catppuccin-latte` with `fixed=true`, `auto=false`, and the former backdrop preference (since removed); the base-palette hook executed. This is one diagnostic sample, not a statistical benchmark or release threshold. The user accepts this startup range; 50 ms is not a delivery gate. See [validation notes](VALIDATION.md) for remaining UI and rootless checks.
