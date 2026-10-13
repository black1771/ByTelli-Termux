# ByTelli Developer Lab V1.0

Android Java source editor developed and built entirely on a phone using standard Termux. Tested on Samsung ARM64 / Android 16.

## Features
- Dark-themed source editor with basic syntax highlighting and line numbers
- Project creation and local file persistence
- Open and save external files via Android Storage Access Framework
- Collapsible project explorer

## Verified mobile build environment
- Termux ARM64, Java 25, Gradle 9.6, Android Gradle Plugin 8.7.3
- Android SDK platform 34 (`compileSdk 34`)
- Termux native `aapt2` configured through `android.aapt2FromMavenOverride` in a local `gradle.properties`

From the project directory, create `local.properties` containing `sdk.dir=/path/to/android-sdk`, configure the native aapt2 override and run `gradle --no-daemon assembleDebug`. The Android SDK and build tools are not bundled.

This is an Android source editor, **not** a compiler or an APK-building IDE. No automatic execution of source files. Tested APK is a debug build, not a signed production release. No broad storage or network permission is requested.

See [build guide](../../docs/ANDROID-APK-BUILD.md) and [build status](../../docs/BUILD-STATUS.md).
