# Build Android APKs using standard Termux
# Normal Termux ile Android APK derleme

## 1. Install Termux / Termux'u kurun
Use the official Termux installation instructions: https://github.com/termux/termux-app#installation

Install the **normal Termux application**. ByTelli-Termux is not required.
**Normal Termux** kullanın. Özel ByTelli-Termux APK'sına gerek yok.

## 2. Install basic tools / Temel araçları kurun
```sh
pkg update && pkg upgrade
pkg install git python clang make cmake openjdk-21
```
Check your environment / Ortamı kontrol edin:
```sh
uname -m
java -version
python --version
clang --version
```
Package availability can change across Termux releases. If a package is unavailable, check `pkg search <name>` rather than copying unknown install scripts.

## 3. Android build prerequisites / APK derleme gereksinimleri
For an Android Gradle project, you need:
- A suitable JDK for the project's Android Gradle Plugin (AGP).
- A Gradle wrapper and compatible AGP version.
- Android SDK platform and build-tools matching the project.
- An **Android ARM64-compatible `aapt2`** or another tested compatible solution.

**Important:** Standard SDK downloads may contain host binaries intended for Linux x86_64 rather than Android ARM64. They will not necessarily run on your phone. This repository does not yet publish a verified universal ARM64 aapt2 installation method.

**Önemli:** Standart SDK paketindeki `aapt2` Linux x86_64 için olabilir ve Android ARM64 telefonda çalışmayabilir. Bu depoda henüz bütün cihazlarda doğrulanmış bir `aapt2` kurulum yöntemi bulunmuyor.

## 4. Build an existing project / Var olan projeyi derleyin
After configuring a working Android SDK and ARM64-compatible build tools, inside the Android Gradle project:
```sh
chmod +x gradlew
./gradlew --version
./gradlew assembleDebug
```
Expected output for a conventional single-app module:
`app/build/outputs/apk/debug/app-debug.apk`

**These commands are conditional on completing step 3.** They are not a standalone installer.

**Bu komutlar 3. adım tamamlandıktan sonra çalıştırılır.** Tek başına kurulum yapmazlar.

## 5. Common errors / Yaygın hatalar
| Error / Hata | Check / Kontrol |
|---|---|
| Exec format error | Wrong host CPU architecture / Yanlış işlemci mimarisi |
| aapt2 failed | Android-native aapt2 and SDK build-tools compatibility / aapt2 ve SDK uyumu |
| SDK location not found | Android SDK location / SDK yolu |
| Unsupported class file version | JDK and Gradle/AGP compatibility / Java ve Gradle uyumu |
| Permission denied | Executable permissions and storage restrictions / Çalıştırma izni ve depolama |

## 6. Share your results / Sonuçları paylaşın
Report Android version, CPU architecture, Termux source, JDK, Gradle, AGP, SDK/build-tools versions, exact commands and sanitized error output. Clearly distinguish tested steps from proposed fixes.

Android sürümü, mimari, Termux kaynağı, Java/Gradle/AGP/SDK sürümleri, komutlar ve özel bilgileri temizlenmiş logları paylaşın. Denenen çözümlerle önerileri ayırın.

See [CONTRIBUTING.md](../CONTRIBUTING.md).
