# Pinned Spotify reflection profile

SpotiTheme targets Spotify `9.1.86.2432`, version code `146555520`. Both identifiers must match before creating its profile. Another release requires a separately verified profile; no runtime bytecode discovery fallback is provided.

`Profile_9_1_86_2432` names exact executable and field identities. `MethodTarget` resolves the declared overload, including parameter and return types. `FieldTarget` checks the field's type and static/instance ownership. Resolution does not initialize the declaring class, enumerate the APK or search for candidate members. Successful resolutions are cached within the profile instance.

The mappings cover Encore layout/accessor/provider entry points, Material 3, Compose drawing and mutable state, gradients, saved-state and Lottie callbacks, Home chips, drawer and navigation, Credits, modern album/artist headers, entity titles, row selectors and Auto Theme consumers. The Credits card maps its direct rounded fill to the theme's surface role in fixed mode; the hook implementation must retain native artwork-mode behavior and its original scope.

Some historical source queries have no matching path in this pinned release: the four-parameter shortcut-card constructor, legacy Connect layout and legacy album-header layout. They must not trigger fallback scans. The modern paths have separate mappings.

These identities were checked against declared methods and selected caller disassembly in the pinned APK. Identity verification does not establish visual behavior, successful hook installation or startup duration. Those require acceptance on the built module. The hook engine has executed under Vector injection and through LSPatch; representative surfaces are recorded in [MVP validation](MVP_VALIDATION.md). The rootless patcher now adds a narrow visibility query for the settings provider, and manager-selected palettes reached the hook without `QUERY_ALL_PACKAGES`. Testing on a separate truly-unrooted device remains open. Startup measurements are informational, with no strict numeric delivery threshold.

Do not update obfuscated names based on a similar signature alone. Verify the owner's semantics, callers, field shape and actual surface on the new APK, then repeat device acceptance.
