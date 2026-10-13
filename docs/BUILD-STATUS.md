# Build status / Derleme durumu

## English
This repository currently contains documentation and a read-only diagnostic script. **It does not yet contain the complete ByTelli-Termux Android application source or a verified release APK.** Do not treat it as a ready-to-build application.

Development history reported for the independent Android package:
- Package ID: `com.vedat.bytellitermux`
- Prefix: `/data/data/com.vedat.bytellitermux/files/usr`
- Target: ARM64 / aarch64
- Experimented with: Java 21, Gradle, Python, C/C++, clang, CMake, make, apt/dpkg.

These details describe the development environment; they are not independently reproducible from the files currently published here.

### Before publishing a build
1. Add the actual Android project source and Gradle wrapper/configuration.
2. Document dependencies, patches and third-party license obligations.
3. Check for hardcoded secrets, private keys, credentials and private paths.
4. Test a clean build on a supported ARM64 Android device.
5. Publish a signed release only with an appropriate signing/security plan.

## Türkçe
Depoda şu anda belgeler ve yalnızca bilgi okuyan bir kontrol betiği bulunmaktadır. **Uygulamanın eksiksiz Android kaynak kodları ve doğrulanmış APK sürümü henüz yayımlanmamıştır.** Bu dosyalardan doğrudan APK üretilebildiği iddia edilmemektedir.

Gerçek kaynaklar eklendikten sonra Gradle yapılandırması, bağımlılıklar, üçüncü taraf lisansları ve temiz cihazda derleme testi belgelenmelidir.
