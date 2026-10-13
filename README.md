# Build Android APKs on Your Phone with Termux
# Normal Termux ile Telefonda APK Geliştirme

**No PC required. No custom Termux APK required. / Bilgisayar veya özel Termux APK'sı gerekmiyor.**

This repository shares a community-driven workflow for developing Android apps directly on an Android phone using **standard Termux**. The Android SDK and especially an ARM64-compatible `aapt2` still require careful setup; this is not a guaranteed one-command installation.

Bu depo, **normal Termux** kullanarak Android telefonda uygulama geliştirme ve APK derleme notlarını paylaşır. Android SDK ve özellikle ARM64 uyumlu `aapt2` ayrıca ayarlanmalıdır; her cihazda tek komutla kurulum garantisi verilmez.

## English — Getting started
1. Install the official Termux app from a trusted [Termux release source](https://github.com/termux/termux-app#installation).
2. Update packages and install the basic development tools:
   ```sh
   pkg update && pkg upgrade
   pkg install git python clang make cmake openjdk-21
   ```
3. Check versions: `java -version`, `python --version`, `clang --version`.
4. Read the [Android APK build guide](docs/ANDROID-APK-BUILD.md) **before** attempting Gradle builds. Android SDK, Gradle/AGP compatibility and Android-native `aapt2` must be resolved for your device.
5. Share reproducible fixes through [Issues](https://github.com/black1771/ByTelli-Termux/issues) and pull requests.

## Türkçe — Başlangıç
1. [Resmî Termux kaynağından](https://github.com/termux/termux-app#installation) normal Termux'u kurun.
2. Termux içinde:
   ```sh
   pkg update && pkg upgrade
   pkg install git python clang make cmake openjdk-21
   ```
3. `java -version`, `python --version`, `clang --version` ile kontrol edin.
4. APK derlemeden önce [APK geliştirme rehberini](docs/ANDROID-APK-BUILD.md) okuyun. Android SDK, Gradle ve ARM64 uyumlu `aapt2` ayrıca yapılandırılmalıdır.
5. Çözümleri GitHub Issues ve pull request üzerinden paylaşın.

## Project scope / Proje kapsamı
- Standard Termux / Normal Termux
- Java, Python, C/C++ development / Geliştirme
- Gradle and Android SDK setup notes / Kurulum notları
- APK build troubleshooting / Derleme hataları
- Tested community fixes / Topluluk düzeltmeleri

**Not an official Termux project. / Resmî Termux projesi değildir.**

The original ByTelli-Termux custom application is a separate experiment and is **not required** for this guide. This repository does not currently provide a verified turnkey APK build environment or a complete sample Android app.

Özel ByTelli-Termux uygulaması ayrı bir denemedir; bu rehberi kullanmak için gerekli değildir. Depoda henüz her cihazda doğrulanmış hazır APK derleme ortamı veya eksiksiz örnek Android uygulaması yoktur.

See [Build status](docs/BUILD-STATUS.md) and [Contributing](CONTRIBUTING.md).

## Safety / Güvenlik
Never share signing keys, credentials, private tokens, wallet secrets or sensitive logs. Do not run unreviewed scripts from strangers.

İmza anahtarlarını, şifreleri, tokenları veya özel logları paylaşmayın. Kaynağını incelemediğiniz betikleri çalıştırmayın.
