# ByTelli Developer Lab — V0.1 (source example)

A **real Android Java source project**, not a Hello World screen. Includes:
- Device/Android/ABI information
- Source text editor
- Android Storage Access Framework open/create/save
- 1 MB read limit to avoid loading huge files into memory

**Not yet compiled or tested on a device.** This source does not include a Gradle wrapper or Android SDK/AAPT2 binary. It does **not** run Termux shell commands and does not yet provide Gradle build automation.

## Standard Termux / Normal Termux
Use the standard Termux APK, not a custom fork. See [build guide](../../docs/ANDROID-APK-BUILD.md). After setting up a compatible JDK, Gradle wrapper, Android SDK and ARM64-native aapt2, run `./gradlew assembleDebug` in this example directory.

The sample pins AGP 8.7.3 (requiring compatible Gradle/JDK); **Gradle 9 or Java 26 is not presumed compatible** with this AGP version. Verify the official compatibility matrix before changing versions.

## Planned / Planlanan
- Project folder explorer using Android document-tree permissions
- Editor enhancements (line numbers, syntax highlighting)
- Build log viewer
- Optional explicit Termux integration with user consent

No broad storage permission, network permission, or silent command execution is requested.
