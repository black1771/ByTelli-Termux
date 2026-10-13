# ByTelli-Termux

**Build Android projects on an Android phone — no PC required.**  
**Android telefonda, bilgisayar kullanmadan Android projeleri geliştirin.**

ByTelli-Termux is an experimental, independently packaged Termux-based development environment. It is a personal community project, not an official Termux release.

ByTelli-Termux, Termux tabanlı bağımsız paket kimliğine sahip deneysel bir geliştirme ortamıdır. Resmî Termux sürümü değildir.

## English

### Project goals
- Build and test Java, Python, C and C++ projects directly on an ARM64 Android phone.
- Develop Android applications and build APKs using command-line tools.
- Keep a separate application package and prefix from standard Termux.
- Share reproducible build instructions, fixes and troubleshooting with other phone-only developers.

### Development environment
- Android package: `com.vedat.bytellitermux`
- Prefix: `/data/data/com.vedat.bytellitermux/files/usr`
- Architecture: ARM64 / aarch64
- Tools explored: OpenJDK 21, Gradle, Python, clang, make, CMake, apt and dpkg.

**Important:** Tool versions and availability vary by build. These are development notes, not a promise that a downloadable, production-ready APK is published here.

### Start here
Read [Android APK development guide](docs/ANDROID-APK-BUILD.md). It explains the command-line workflow, SDK/aapt2 requirements, common Android/ARM64 pitfalls and verification.

### Contribute
Bug reports, reproducible fixes, build logs and documentation improvements are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md).

## Türkçe

### Projenin amacı
- ARM64 Android telefonda Java, Python, C ve C++ projeleri geliştirmek.
- Komut satırından Android uygulaması geliştirip APK üretmek.
- Normal Termux'tan ayrı paket adı ve dosya öneki kullanmak.
- Kurulum notlarını, hataları ve çözümleri toplulukla paylaşmak.

### Geliştirme ortamı
- Paket adı: `com.vedat.bytellitermux`
- PREFIX: `/data/data/com.vedat.bytellitermux/files/usr`
- Mimari: ARM64 / aarch64
- Denenen araçlar: OpenJDK 21, Gradle, Python, clang, make, CMake, apt ve dpkg.

**Not:** Araç sürümleri derlemeye göre değişebilir. Bu depo henüz doğrulanmış, herkesin indirebileceği hazır bir APK sunduğunu iddia etmez.

### Nereden başlamalı?
[Telefonda APK geliştirme rehberi](docs/ANDROID-APK-BUILD.md) ve [katkı rehberi](CONTRIBUTING.md).

## Security / Güvenlik
Do not publish signing keys, tokens, wallet secrets, personal paths containing sensitive data, or private logs.  
İmza anahtarlarını, tokenları, cüzdan gizli anahtarlarını ve özel verileri depoya yüklemeyin.

## License / Lisans
No license has been selected yet. Until one is added, do not assume the repository content is licensed for unrestricted reuse. Third-party Termux/Android components retain their own licenses.
