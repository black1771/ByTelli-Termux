# APK build troubleshooting in standard Termux
# Normal Termux'ta APK derleme sorunları

> This is a diagnostic guide, not a verified universal installer. / Bu belge tüm cihazlarda doğrulanmış otomatik kurulum değildir.

## 1. First collect versions / Önce sürümleri öğrenin

Run in the Android Gradle project directory / Android Gradle projesinin klasöründe:

```sh
uname -m
getprop ro.build.version.release
getprop ro.build.version.sdk
echo "PREFIX=$PREFIX"
command -v java; java -version
command -v javac; javac -version
command -v gradle; gradle --version
test -f gradlew && sh ./gradlew --version
test -f gradle/wrapper/gradle-wrapper.properties && cat gradle/wrapper/gradle-wrapper.properties
echo "ANDROID_HOME=$ANDROID_HOME"
echo "ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT"
command -v aapt2
```

Some commands may report not found; that's useful diagnostic information. Do not paste secrets or private filesystem content into public Issues.

## 2. Gradle 9 and Java 26 / Gradle 9 ve Java 26

Our phone-only development work included Gradle 9.x and Java 21 setups; a particular project's requirement for Java 26 must be verified against its actual Gradle and Android Gradle Plugin (AGP) versions.

**Java 26 is not a universal requirement for Android APK builds.** A JDK may be too new for a particular Gradle or AGP release. Likewise, using Gradle 9 with an older AGP may be incompatible.

Bizim telefon üzerinde yaptığımız geliştirme çalışmalarında Gradle 9.x ve Java 21 kullanıldı. Bir proje Java 26 istiyorsa bu gereksinimi projenin Gradle ve AGP sürümlerinden doğrulamak gerekir. **Java 26 tüm APK projeleri için zorunlu değildir.**

- Read `gradle/wrapper/gradle-wrapper.properties` to identify the selected Gradle version.
- Find the AGP version in the project's Gradle plugin declarations.
- Verify the selected Java runtime with `java -version`.
- Use the official [Gradle compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html) and [AGP release notes](https://developer.android.com/build/releases/gradle-plugin).
- Prefer the project's Gradle wrapper (`./gradlew`) over an unrelated globally installed Gradle.

## 3. AAPT2: Exec format error / Yanlış mimari

### Symptom / Belirti
`Exec format error`, `aapt2 failed`, or failure to launch `aapt2`.

### Why / Neden
AAPT2 from standard Android SDK build-tools may be compiled for a desktop host (e.g. Linux x86_64), not Android aarch64. Installing Java or upgrading Gradle does **not** by itself fix this.

### Diagnose / Teşhis
```sh
uname -m
command -v aapt2
aapt2 version
```
If the project uses a separate SDK aapt2 binary, inspect **that exact binary** too. On Termux, `file /path/to/aapt2` can help identify its architecture when the `file` package is installed.

### Fix direction / Çözüm yönü
Use a **trusted, Android-native ARM64 AAPT2 binary** compatible with the project and AGP; confirm it runs before rebuilding. The exact setup depends on SDK/AGP and is not yet verified as a universal command in this repository. Avoid downloading random binaries or blindly applying unverified `android.aapt2FromMavenOverride` paths.

## 4. SDK location not found / SDK yolu bulunamadı

Check `ANDROID_HOME` and the project's SDK configuration. The SDK path must exist and contain the required platform and build-tools. `local.properties` can include a machine-specific `sdk.dir`, but should not be committed to GitHub.

## 5. Java/Gradle/AGP version errors / Sürüm uyumsuzlukları

| Symptom / Hata | Likely check / Kontrol |
|---|---|
| Unsupported class file major version | JDK runtime and Gradle/AGP compatibility |
| Android Gradle plugin requires Java X | Use a supported JDK version |
| Minimum supported Gradle version is X | Project Gradle wrapper and AGP |
| Could not find or load main class | JDK install, classpath, wrapper files |
| Permission denied on gradlew | `chmod +x gradlew`; executable location and permissions |
| Could not resolve dependencies | Internet, repository configuration, TLS, DNS |

**Do not disable TLS verification or add insecure repositories just to make Gradle downloads work.**

## 6. Build with useful logs / Ayrıntılı logla derleme

After the SDK, AAPT2, Java and Gradle compatibility have been verified:

```sh
./gradlew assembleDebug --stacktrace --info
```

A conventional single-module project may produce `app/build/outputs/apk/debug/app-debug.apk`, but output paths vary. A successful Gradle configuration step alone does not mean an APK was built.

## 7. Contribute a tested solution / Denenmiş çözüm gönderin

In a GitHub Issue, share:
- Phone model, Android version, `uname -m`
- Termux source and package versions
- `java -version`, Gradle wrapper version and AGP version
- Android SDK/build-tools version and actual AAPT2 binary architecture
- Sanitized error and exact fix commands
- Whether `assembleDebug` completed and the APK installed

GitHub Issues: https://github.com/black1771/ByTelli-Termux/issues

Never include signing keys, private tokens, wallet credentials or unredacted logs.
