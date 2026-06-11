# v1.1.11-mars.1

MarsGao maintained fork release for WeChat 8.0.69 GP and AMap 16.17.3 GP compatibility.

## Highlights

- Fixes WeChat 8.0.69 GP send-location spoofing on Mi 14 Pro and OnePlus ACE 5.
- Fixes AMap internal SDK location objects and late-loaded callback paths.
- Migrates stale local hook preferences into libxposed remote preferences.
- Keeps system-server `"system"` scope disabled in normal releases.

## Validated Devices

- Xiaomi Mi 14 Pro `shennong`, Android 16, APatch + ZygiskNext + Vector
- OnePlus ACE 5 `OP5D2BL1`, Android 16, Wild KSU + ZygiskNext + Vector

## APK Channel

This release was validated against Google Play channel APKs (`GP`). China-mainland APK builds of WeChat or AMap may use different obfuscated classes, dex layout, or SDK internals and are not covered by this validation.

## Validation

- `assembleRelease`
- `testReleaseUnitTest`
- WeChat send-location 45-second stability check
- AMap locate-button 45-second stability check

## Install Notes

Install over an existing compatible build with:

```powershell
adb install -r XposedFakeLocation-v1.1.11-mars.1-release.apk
adb reboot
```

After reboot, unlock the device and verify target apps are still in module scope.

## Community Status

This release keeps the original package name `com.noobexon.xposedfakelocation` for upgrade compatibility. The existing Xposed community listing is `Xposed-Modules-Repo/com.noobexon.xposedfakelocation`, so MarsGao fork builds are published from GitHub releases until upstream/community publishing is coordinated.

If this release helps, please star `MarsGao/XposedFakeLocation` and share reproducible reports with device model, root stack, app versionCode, APK channel (`GP` or mainland), and validation steps.
