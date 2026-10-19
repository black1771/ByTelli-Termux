# ByBlack DeviceLab V2 PRO (experimental)

Android hardware information and diagnostics app developed and tested on a Samsung Galaxy S24 Ultra with Termux.

![Real-device demo](../../ByBlack-DeviceLab-Preview.gif)

## Features
- Device, Android, memory and storage information
- Battery readings and RAM history graph
- Sensor inventory and live sensor readings
- Display color and touchscreen drawing tests
- Copyable and shareable device report

## Build environment
Java 26, Gradle 9.6, Android SDK 34, Android Gradle Plugin 8.7.3, Termux ARM64 `aapt2`. Offline Gradle build requires dependencies already cached.

```sh
export JAVA_HOME="$PREFIX/lib/jvm/java-26-openjdk"
export PATH="$JAVA_HOME/bin:$PREFIX/bin:$PATH"
export ANDROID_HOME="$HOME/android-sdk"
gradle --offline --no-daemon assembleDebug
```

**Status:** Java source and Gradle project files are published. This is an experimental debug project, not a production release. Building on another device has not been independently verified.
