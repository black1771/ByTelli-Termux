# ByTelli — Android Development, Right on Your Phone 🚀

**Build Android apps directly on your phone. No PC required.**

[![Download Developer Lab](https://img.shields.io/badge/Download-Developer%20Lab%20v1.0-blue?logo=android)](https://github.com/black1771/ByTelli-Termux/releases/tag/v1.0.0)
[![Open Source](https://img.shields.io/badge/Open%20Source-GitHub-black?logo=github)](https://github.com/black1771/ByTelli-Termux)
[![Help Wanted](https://img.shields.io/badge/Contributions-Welcome-brightgreen)](CONTRIBUTING.md)
[![Support](https://img.shields.io/badge/Support-ByTelli-orange)](SUPPORT.md)

**English | [Türkçe](#türkçe)**

ByTelli is an independent, experimental Android development project built and tested **using an Android phone and standard Termux**. We share working source code, build instructions, troubleshooting notes, and a downloadable Android editor.

> **What makes this different?** The development workflow runs on the phone itself — including Java, Gradle and Android SDK setup. No desktop computer was used for the verified ByTelli Developer Lab build.

## 📱 Featured app: ByTelli Developer Lab V1.0

A lightweight Android **Java source editor** built entirely on an ARM64 phone.

**Features**
- Dark editor with basic Java syntax highlighting and line numbers
- Create projects and keep local files between sessions
- Open and save files with Android's Storage Access Framework
- Collapsible project explorer

**[⬇ Download APK — V1.0](https://github.com/black1771/ByTelli-Termux/releases/download/v1.0.0/ByTelliDeveloperLab-FINAL-V1.apk)** · **[📁 View source](examples/ByTelliDeveloperLabV1)** · **[📝 Release notes](https://github.com/black1771/ByTelli-Termux/releases/tag/v1.0.0)**

**Important:** V1.0 is a source-code editor, **not** an on-device compiler or full APK-building IDE. The published APK is a **debug build**, not a production-signed Play Store release. Review source and verify downloads before installing.

## 🛠️ Build Android APKs using standard Termux

This repository also documents how to compile Android apps **inside regular Termux**, without a PC or a custom Termux fork.

**Verified Developer Lab build environment:** ARM64 Android / Java 25 / Gradle 9.6 / Android Gradle Plugin 8.7.3 / Android SDK platform 34 / Termux-native `aapt2`.

1. Install Termux from a [trusted official source](https://github.com/termux/termux-app#installation).
2. Read the **[Android APK build guide](docs/ANDROID-APK-BUILD.md)**.
3. Explore the **[Developer Lab V1 source](examples/ByTelliDeveloperLabV1)**.
4. Check **[build status and known limitations](docs/BUILD-STATUS.md)**.

The Android SDK, compatible Gradle versions and ARM64 `aapt2` require manual setup. **This is not a universal one-command installer**, and device compatibility varies.

## 🤝 Help make ByTelli better

⭐ **Star this repository** if the project interests you.

🐛 [Report a bug](https://github.com/black1771/ByTelli-Termux/issues/new) — include device, Android version, expected/actual behavior and reproduction steps.

💡 [Suggest a feature](https://github.com/black1771/ByTelli-Termux/issues/new) — tell us what would make phone-only development easier.

🔧 [Contribute a fix](CONTRIBUTING.md) — documentation, Android compatibility and editor improvements are welcome.

❤️ [Support development / optional crypto donations](SUPPORT.md) — donations are voluntary, not required to use the project.

## Türkçe

**ByTelli — Bilgisayarsız, doğrudan Android telefonunda uygulama geliştir!**

Bu depo, **normal Termux** üzerinden Android APK derleme deneyimlerini, kaynak kodlarını ve karşılaşılan sorunların çözümlerini paylaşır.

- **[ByTelli Developer Lab V1.0 APK indir](https://github.com/black1771/ByTelli-Termux/releases/tag/v1.0.0)**
- **[Uygulamanın kaynak kodu](examples/ByTelliDeveloperLabV1)**
- **[Telefonda APK derleme rehberi](docs/ANDROID-APK-BUILD.md)**
- **[Hata bildir / öneri gönder](https://github.com/black1771/ByTelli-Termux/issues)**
- **[Projeye destek ol](SUPPORT.md)**

**Not:** Developer Lab V1.0 kod düzenleyicisidir; kendi içinde APK derlemez. APK, test amaçlı debug sürümüdür. Termux üzerinden APK derlemek için SDK ve araçların ayrıca kurulması gerekir.

---

**Independent community project; not affiliated with the official Termux project.**

**Security:** Never share signing keys, access tokens, private keys, recovery phrases or sensitive logs. Do not execute unreviewed third-party scripts.
