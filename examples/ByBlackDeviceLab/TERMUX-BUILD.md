# ByBlack DeviceLab V2 PRO — Termux ile kaynak koddan APK

**Açık kaynak Android donanım tanılama uygulaması.** Bilgisayar veya root gerektirmeden, standart ARM64 Termux üzerinde APK derlenebilir. Derleme ortamı kurulumu internet ve depolama alanı gerektirir; önbellekte bağımlılıklar varsa derleme çevrimdışı yapılabilir.

![Uygulama videosu](../../ByBlack-DeviceLab-Preview.gif)

## 1. Gerekli araçları kur

Önce [Android APK derleme kurulum rehberini](../../docs/ANDROID-APK-BUILD.md) takip et. Android SDK 34, ARM64 `aapt2`, Java, Gradle ve Android Gradle Plugin bağımlılıklarını kur. **Bu adımı atlama.** Kurulum tamamlanmadan aşağıdaki komut çalışmaz.

Standart Termux'ta depolama erişimini aç:

```sh
termux-setup-storage
```

## 2. Tek komutla derle

```sh
curl -fL https://raw.githubusercontent.com/black1771/ByTelli-Termux/main/scripts/build-devicelab-termux.sh -o build-devicelab-termux.sh && bash build-devicelab-termux.sh
```

`curl` yüklü değilse `pkg install curl` komutunu kullan. İndirdiğin betiği çalıştırmadan önce incele.

Başarılı derlemede APK: `/storage/emulated/0/Download/ByBlack-DeviceLab-V2-PRO.apk`.

## Önemli

- Kaynak: [Java ve Gradle dosyaları](.)
- Betik mevcut `~/ByTelli-Termux` klasörünü silmez veya üzerine yazmaz; varsa onu kullanır.
- Derleme `--offline` kullanır: AGP/Gradle bağımlılıkları önceden indirilmemişse başarısız olabilir. Bu durumda bağımlılıkları kurduktan sonra tekrar dene.
- Bu proje **deneysel debug APK** üretir. Google Play için imzalı sürüm değildir.
- Kullanılan `targetSdk 28` modern Android dağıtımı için yükseltilmelidir; cihazdan cihaza uyumluluk değişebilir.
- APK otomatik yüklenmez. Yalnızca oluşturulur.
- Betiğin her cihazda çalıştığı henüz doğrulanmamıştır.

## English

Build the open-source Android diagnostics app directly on an ARM64 phone using standard Termux. First complete the [Android SDK 34 + native AAPT2 setup](../../docs/ANDROID-APK-BUILD.md), then run the command above. The output is a debug APK in Downloads, not a production-signed release.
