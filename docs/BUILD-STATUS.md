# Build status / Derleme durumu

## Verified phone build / Telefonda dogrulanan derleme (2026-10-13)

**Byblack Developer Lab V0.1** was built as a debug APK using **standard Google Play Termux** on an ARM64 Samsung SM-S918B running Android 16. The user installed and opened the app; a screenshot confirms the main screen renders device details and editor/file controls.

**Byblack Developer Lab V0.1**, normal Google Play Termux ile ARM64 Samsung SM-S918B (Android 16) cihazinda debug APK olarak derlendi. Kullanici APK'yi kurup uygulamayi acti; ekran goruntusu ana ekranin ve cihaz bilgilerinin goruntulendigini dogruluyor.

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

**Important / Onemli:** The checked-in V1 example now specifies `compileSdk 34`, matching the verified successful phone build. The user has now confirmed an end-to-end text-file workflow: enter English text, save as a new text file, and reopen it successfully. Other editor edge cases remain untested. This is one-device validation, not universal compatibility.

## Encountered issues / Karsilasilan hatalar

- Java 21.0.11: `javac` and class execution aborted with `PerfettoTrace` error (exit 134); Java 25.0.3 compiled and ran a Java test and started Gradle 9.6 successfully.
- SDK build-tools 35.0.0 included x86-64 `aapt2`; replaced locally with Termux ARM64 `aapt2`, retaining a backup.
- Termux ARM64 `aapt2` successfully parsed Android 33 and 34 framework resources, but failed for 35 and crashed for 36; changing project `compileSdk` to 34 enabled the build.
- APK was copied to Android Downloads after build and installed on the phone.

## Remaining work / Kalan isler

1. Expand file open/create/save testing (large files, Unicode, edits to existing files) and fix any bugs.
2. Test the documented setup on additional ARM64 Android devices.
3. Test large files, Unicode and editor edge cases.
4. Keep SDK/Gradle/AGP compatibility notes current.

**No universal compatibility claim / Tum cihazlar icin uyumluluk garantisi yoktur.**


## V0.2 on-device verification (2026-10-13)

The user built and installed Byblack Developer Lab V0.2 on an Android 16 ARM64 phone using standard Termux with compileSdk 34. Verified on device: app launches; project `MyFirstApp` created; `Main.java` created and displayed with source text; the file was saved, the app was fully closed and reopened, and `Main.java` still contained the saved code. This confirms persistent local project-file storage for the tested workflow. Compilation/execution of Java source inside the app is **not implemented or tested**. Further editor edge-case tests remain pending.


## Developer Lab V1.0 final candidate — on-device verification (2026-10-13)

User-built debug APK on Android 16 ARM64 via standard Termux (Java 25, Gradle 9.6, compileSdk 34, ARM64 Termux aapt2). Screenshots confirm the V1.0 app launches with dark theme, project explorer retains `MyFirstApp`, `Main.java` reopens, line numbers 1–5 align, and syntax coloring is visible on Java keywords and a string literal. Prior V0.2 test verified persistent save after app restart. The V1.0 APK and full source are currently on the user's phone; **this documentation update does not upload them to GitHub**. Source-code execution/compiler functionality is not implemented. APK is a debug build; no release signing or broad device compatibility testing has been performed.
