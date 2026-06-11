# Mars Maintained Changelog

This changelog tracks the MarsGao maintained fork builds. Upstream official tags currently stop at `v0.1.2`; the `1.1.x-mars` line is a device-validated maintenance line for the devices and app versions listed below.

## v1.1.11-mars.1

Validated on:

- Xiaomi Mi 14 Pro `shennong`, Android 16, APatch/ZygiskNext/Vector
- OnePlus ACE 5 `OP5D2BL1`, Android 16, Wild KSU/ZygiskNext/Vector
- WeChat Google Play APK `8.0.69 GP` / `versionCode=3040`
- AMap Google Play APK `16.17.3 GP`

Channel note:

- WeChat and AMap both have China-mainland APKs and Google Play APKs. This release was validated against the Google Play channel builds commonly abbreviated as `GP`; do not assume the same obfuscated classes or SDK package names apply to mainland APK builds without re-validation.

Changes:

- Fixed WeChat 8.0.69 GP send-location spoofing by installing hooks from `SoSoProxyUI`'s runtime class loader.
- Added WeChat location dispatch hooks for `cx0.h`, `j1.onGetLocation`, `p2` / `LocationInfo`, SDK wrapper objects, and Pigeon location objects.
- Fixed AMap internal SDK coverage for `com.amap.location.*` location classes, setters, constructors, callbacks, and field-backed coordinates.
- Added repeated third-party hook installation after app startup to handle late-loaded SDK classes.
- Added one-time migration from legacy local hook preferences to Vector/libxposed remote preferences, including `lat/lng` to `latitude/longitude` normalization.
- Kept Vector's `"system"` scope out of `SYSTEM_HOOK_PACKAGES`; system-server experiments must remain isolated because a previous direct scope expansion caused watchdog reboot.

Tested behavior:

- AMap: tapping the locate button remains on the spoofed target after 45-60 seconds.
- WeChat: opening send-location remains in the spoofed Huangpu/Yonghe target area after 45 seconds and after tapping "move to my location".
- No observed app fatal exception or system-server reboot during the final validation pass.

## Local iteration notes before v1.1.11-mars.1

- `1.1.3`: Vector/API 101 app-process init timing and explicit warning against adding Vector `"system"` scope directly.
- `1.1.4` - `1.1.6`: AMap class-name corrections and broader AMap object/callback interception.
- `1.1.7` - `1.1.8`: AMap runtime reverse engineering and stability tests.
- `1.1.9`: Remote preference migration fix; resolved stale coordinates on ACE 5.
- `1.1.10`: First WeChat `cx0.h` dispatcher attempt; superseded by runtime class-loader installation in `1.1.11-mars.1`.
