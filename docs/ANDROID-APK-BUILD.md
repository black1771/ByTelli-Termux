# Android APK development on a phone / Telefonda APK geliştirme

## English

This is a practical **workflow outline**, not a universal one-command installer. Android command-line builds need compatible tools for your phone's CPU and the project's Android Gradle Plugin (AGP).

1. Install a trustworthy Android terminal environment and verify the architecture: `uname -m`.
2. Check Java: `java -version`; check Gradle: `gradle --version` or use the project's `./gradlew --version`.
3. Check Android SDK components: platform SDK, build-tools and compatible `aapt2`. **A Linux x86_64 aapt2 binary will not execute natively on ARM64 Android.** Use a compatible Android-native toolchain or a documented, trusted replacement.
4. Open the project directory and inspect `settings.gradle`, `build.gradle` and `gradle.properties` before running builds.
5. Run `chmod +x gradlew` and `./gradlew assembleDebug` only after prerequisites are configured.
6. Look for output under `app/build/outputs/apk/debug/`. Confirm the APK exists and check the build log before installing it.

### Diagnostics
```sh
uname -m
java -version
./gradlew --version
echo "$ANDROID_HOME"
echo "$ANDROID_SDK_ROOT"
```
Run the Gradle commands inside an existing Gradle project. A successful `java -version` alone does not prove the Android SDK or aapt2 works.

### Frequent issues
- **Exec format error:** wrong binary architecture, often x86_64 instead of aarch64.
- **aapt2 error:** missing or incompatible Android build-tools.
- **SDK location not found:** configure the SDK location for the project.
- **Permission denied:** check executable permissions and Android storage restrictions.
- **Build succeeds but install fails:** inspect manifest, minSdk, ABI, signing and the device's installer error.

## Türkçe

Bu belge, tek komutla çalışan kesin kurulum betiği değil, **kontrol edilebilir geliştirme akışıdır**.

1. `uname -m` ile cihaz mimarisini doğrulayın.
2. `java -version` ve `./gradlew --version` ile Java/Gradle uyumunu kontrol edin.
3. Android SDK, platform, build-tools ve **ARM64 Android üzerinde çalışabilen aapt2** gereksinimini kontrol edin. Normal Linux x86_64 aapt2, Android ARM64 üzerinde doğrudan çalışmaz.
4. Projedeki Gradle dosyalarını ve Android Gradle Plugin sürümünü inceleyin.
5. Gereksinimler hazırsa proje dizininde `./gradlew assembleDebug` çalıştırın.
6. Oluşan APK'yı `app/build/outputs/apk/debug/` altında arayın.

### Hata ayıklama
- **Exec format error:** Yanlış mimari için derlenmiş araç.
- **aapt2 hatası:** Eksik veya uyumsuz derleme aracı.
- **SDK bulunamadı:** SDK yolunu yapılandırın.
- **Permission denied:** Çalıştırma izni veya Android depolama kısıtı.
- **APK kurulmadı:** Manifest, minSdk, ABI, imza ve kurulum hata mesajını inceleyin.

## Contributing build recipes / Derleme tarifi paylaşma
Please include phone model, Android version, architecture, Java/Gradle/AGP versions, exact commands, sanitized error logs, and whether the build was actually tested.  
Telefon modeli, Android sürümü, mimari, araç sürümleri, tam komutlar, gizli bilgileri temizlenmiş hata kaydı ve test sonucunu ekleyin.
