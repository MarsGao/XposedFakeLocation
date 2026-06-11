# MarsGao Maintenance Notes

This fork is maintained for Gao Kong's rooted Android device fleet. It follows upstream where practical, but prioritizes tested behavior on the local devices below.

## Supported Baseline

- Package: `com.noobexon.xposedfakelocation`
- Maintained release line: `v1.1.11-mars.1`
- Devices validated:
  - Xiaomi Mi 14 Pro `shennong`, Android 16, APatch + ZygiskNext + Vector
  - OnePlus ACE 5 `OP5D2BL1`, Android 16, Wild KSU + ZygiskNext + Vector
- Apps validated:
  - WeChat Google Play APK `8.0.69 GP` / `versionCode=3040`
  - AMap Google Play APK `16.17.3 GP`

## APK Channel Boundary

WeChat and AMap both ship China-mainland APK builds and Google Play APK builds. The validated builds in this fork are the Google Play channel APKs, commonly abbreviated as `GP`.

Do not treat `8.0.69 GP` as equivalent to a mainland WeChat `8.0.69` APK. Obfuscated class names, dex layout, embedded map SDK classes, package internals, and location dispatch paths may differ by channel. Mainland APK support requires its own APK pull, decompile pass, hook check, and device validation.

## Maintenance Policy

- Keep the fork releaseable independently of upstream review.
- Prefer small upstream PRs for general fixes: preference migration, delayed SDK hook installation, and app SDK class-name corrections.
- Keep device-specific or high-risk system-server experiments in this fork until isolated probe builds prove stability.
- Do not add Vector's `"system"` scope to `SYSTEM_HOOK_PACKAGES` in normal releases.

## Release Checklist

1. Build and test locally:

   ```powershell
   .\gradlew.bat assembleRelease testReleaseUnitTest
   ```

2. Install on the target device and reboot:

   ```powershell
   adb install -r app\build\outputs\apk\release\app-release.apk
   adb reboot
   ```

3. Verify after reboot:

   - Device unlocked and launcher visible.
   - `sys.boot_completed=1`.
   - Root is available.
   - `dumpsys package com.noobexon.xposedfakelocation` shows the intended version.
   - WeChat send-location stays in the spoofed target area for at least 45 seconds.
   - AMap locate button stays on the spoofed target for at least 45 seconds.

4. Tag and release from the maintained branch:

   ```powershell
   git tag -a v1.1.11-mars.1 -m "v1.1.11-mars.1"
   git push fork local-maintained
   git push fork v1.1.11-mars.1
   gh release create v1.1.11-mars.1 --repo MarsGao/XposedFakeLocation --target local-maintained --notes-file .\docs\release-v1.1.11-mars.1.md
   ```

## Upstream PR Candidates

- Remote hook-preference migration from legacy local SharedPreferences to libxposed remote preferences.
- General delayed third-party SDK hook retry after app startup.
- AMap internal SDK class-name and callback coverage.
- WeChat 8.0.69 GP hooks as a separate PR only if scoped clearly and documented as app-version/channel-specific.
