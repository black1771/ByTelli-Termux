# ByBlack DeviceLab V3 Bilingual

**[Türkçe](#turkce) | [English](#english)**

![ByBlack DeviceLab real-device demo / Gerçek cihaz tanıtımı](../../ByBlack-DeviceLab-Preview.gif)

<a id="turkce"></a>
## 🇹🇷 Türkçe

**ByBlack DeviceLab V3 Bilingual**, Android telefonlar için açık kaynaklı, deneysel donanım bilgisi ve tanılama uygulamasıdır. Samsung Galaxy S24 Ultra üzerinde geliştirilip Termux ile derlenmiştir.

### Dil desteği
- Türkçe ve İngilizce arayüz; uygulama içinden dil seçimi
- Dil tercihi sonraki açılışlarda korunur
- Galaxy S24 Ultra / Android 17 üzerinde kullanıcı testi başarılı bildirildi

### Özellikler
- Cihaz, Android sürümü, RAM ve depolama bilgileri
- Pil durumu ve RAM geçmiş grafiği
- Sensör listesi ve desteklenen sensörlerden canlı veriler
- Ekran renkleri ve dokunmatik çizim testleri
- Kopyalanabilir ve paylaşılabilir cihaz raporu

### Telefonda APK derle

**[Adım adım Termux rehberi](TERMUX-BUILD.md)** · **[Java kaynak kodu](app/src/main/java/com/byblack/devicelab/MainActivity.java)**

Önce [Java, Gradle, SDK 34 ve ARM64 AAPT2 kurulumunu](../../docs/ANDROID-APK-BUILD.md) tamamla. Ardından standart Termux'ta:

```sh
pkg install -y git curl
curl -fL https://raw.githubusercontent.com/black1771/ByTelli-Termux/main/scripts/build-devicelab-termux.sh -o build.sh && bash build.sh
```

Başarılı olursa APK: `/storage/emulated/0/Download/ByBlack-DeviceLab-V3-Bilingual.apk`.

**Doğrulama (19 Ekim 2026):** GitHub Actions APK derlemesi başarılı. Samsung Galaxy S24 Ultra üzerinde standart ARM64 Termux derlemesi de 18 saniyede, 31 Gradle göreviyle başarıyla tamamlandı. Başka cihazlarda henüz doğrulanmadı. Bu bir deneysel debug APK'dır; Google Play sürümü değildir.

<a id="english"></a>
## 🇬🇧 English

**ByBlack DeviceLab V3 Bilingual** is an open-source experimental Android hardware information and diagnostics app, developed and built on a Samsung Galaxy S24 Ultra using Termux.

### Language support
- Turkish and English interface with in-app language selector
- Language preference is saved between launches
- User-reported successful testing on Galaxy S24 Ultra / Android 17

### Features
- Device, Android version, RAM and storage information
- Battery status and RAM history graph
- Sensor inventory and live readings where supported
- Display color and touchscreen drawing tests
- Copyable and shareable device report

### Build an APK on your Android phone

**[Step-by-step Termux guide](TERMUX-BUILD.md)** · **[Java source code](app/src/main/java/com/byblack/devicelab/MainActivity.java)**

First install [Java, Gradle, Android SDK 34 and native ARM64 AAPT2](../../docs/ANDROID-APK-BUILD.md). Then run in standard Termux:

```sh
pkg install -y git curl
curl -fL https://raw.githubusercontent.com/black1771/ByTelli-Termux/main/scripts/build-devicelab-termux.sh -o build.sh && bash build.sh
```

On success, the debug APK is copied to `/storage/emulated/0/Download/ByBlack-DeviceLab-V3-Bilingual.apk`.

**Verified (19 October 2026):** GitHub Actions successfully built the APK. A standard ARM64 Termux build on Samsung Galaxy S24 Ultra also succeeded in 18 seconds with 31 Gradle tasks. Other devices have not been verified. This is an experimental debug APK, not a Google Play release.

**[GitHub Actions build](https://github.com/black1771/ByTelli-Termux/actions/workflows/byblack-devicelab-build.yml)** · **[Build script](../../scripts/build-devicelab-termux.sh)**
