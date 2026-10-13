# Build status / Derleme durumu

## Verified phone build / Telefonda dogrulanan derleme (2026-10-13)

**ByTelli Developer Lab V0.1** was built as a debug APK using **standard Google Play Termux** on an ARM64 Samsung SM-S918B running Android 16. The user installed and opened the app; a screenshot confirms the main screen renders device details and editor/file controls.

**ByTelli Developer Lab V0.1**, normal Google Play Termux ile ARM64 Samsung SM-S918B (Android 16) cihazinda debug APK olarak derlendi. Kullanici APK'yi kurup uygulamayi acti; ekran goruntusu ana ekranin ve cihaz bilgilerinin goruntulendigini dogruluyor.

| Component | Observed working setup |
| --- | --- |
| Java | OpenJDK 25.0.3 |
| Gradle | 9.6.0 (system `gradle` command) |
| Android Gradle Plugin | 8.7.3 (example project) |
| compileSdk | 34 (changed locally from 35) |
| Android SDK | `$HOME/android-sdk` |
| AAPT2 | Termux ARM64 `$PREFIX/bin/aapt2` via `android.aapt2FromMavenOverride` |
| Build command | `gradle --no-daemon assembleDebug` |
| Build result | `BUILD SUCCESSFUL in 39s` |
| APK | `app/build/outputs/apk/debug/app-debug.apk` |
| Install and launch | Confirmed by user screenshot |

**Important / Onemli:** The checked-in example still specifies `compileSdk 35`. The successful build used a **local change to 34**, not the unmodified repository. The user has now confirmed an end-to-end text-file workflow: enter English text, save as a new text file, and reopen it successfully. Other editor edge cases remain untested. This is one-device validation, not universal compatibility.

## Encountered issues / Karsilasilan hatalar

- Java 21.0.11: `javac` and class execution aborted with `PerfettoTrace` error (exit 134); Java 25.0.3 compiled and ran a Java test and started Gradle 9.6 successfully.
- SDK build-tools 35.0.0 included x86-64 `aapt2`; replaced locally with Termux ARM64 `aapt2`, retaining a backup.
- Termux ARM64 `aapt2` successfully parsed Android 33 and 34 framework resources, but failed for 35 and crashed for 36; changing project `compileSdk` to 34 enabled the build.
- APK was copied to Android Downloads after build and installed on the phone.

## Remaining work / Kalan isler

1. Expand file open/create/save testing (large files, Unicode, edits to existing files) and fix any bugs.
2. Update example build configuration to the verified SDK 34 baseline while documenting SDK 35 limitations.
3. Produce a repeatable from-scratch installation guide for Android SDK and ARM64 tools.
4. Test on other devices and review third-party redistribution licensing.

**No universal compatibility claim / Tum cihazlar icin uyumluluk garantisi yoktur.**
