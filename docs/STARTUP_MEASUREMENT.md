# Startup acceptance

The requirement is less than 50 ms of total hook startup work on the main thread. Removing DexKit is necessary but does not prove that requirement.

The module emits `SpotiTheme` logcat records for loader, resource and application-attach registration. Each record includes phase duration, whether it ran on the main thread, and cumulative main-thread time. Nested intervals are merged, so resource registration inside attachment is not counted twice. Unit tests check that accounting.

Read the completed initialization record, not an early loader record, when assessing the total. The measurement covers work inside the registration phases, including profile construction, asset parsing, hook installation and resource configuration. It does not measure framework work before callback entry or unrelated Spotify startup. Device trace evidence is still needed to establish the complete startup boundary and check for unmeasured module work.

```sh
adb -s 17004dab logcat -d -v threadtime -s SpotiTheme
```

Record the exact APK hash, pinned Spotify identity, effective Vector module/scope, process identity and cold/warm results. Preserve full crash logs separately if initialization fails. Successful registration is not proof that the hooks execute; require the `Base palette hook executed` record and visual acceptance too.

Current status: instrumented and unit-tested, but not measured on the device. The under-50-ms gate is open. The binary currently contains only the base palette portion of the theme engine.
