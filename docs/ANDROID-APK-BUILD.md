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


## 7. Java and Gradle versions / Java ve Gradle sürümleri

**A real-world lesson from our phone-based APK experiments:** The Java and Gradle versions must be compatible with **each other and the Android Gradle Plugin (AGP)**. A version number by itself is not enough to guarantee a successful build.

**Telefonda APK geliştirme deneyimimizden ders:** Java, Gradle ve Android Gradle Plugin (AGP) sürümleri **birbirleriyle uyumlu olmalıdır**. Her proje için otomatik olarak "Java 26 ve Gradle 9 zorunlu" denemez. Projeye ve kullanılan eklentiye göre değişir.

### Inspect the project's requirements / Projenin sürümlerini kontrol edin
Run inside an existing Gradle project / Mevcut Gradle projesinde:
```sh
java -version
./gradlew --version
cat gradle/wrapper/gradle-wrapper.properties
grep -R -n 'com.android.application\|com.android.library' settings.gradle* build.gradle* app/build.gradle* 2>/dev/null
```
The wrapper's `distributionUrl` determines which Gradle release the project downloads. The AGP version determines which Gradle/JDK combinations are supported. See official [Gradle compatibility](https://docs.gradle.org/current/userguide/compatibility.html) and [Android Gradle Plugin compatibility](https://developer.android.com/build/releases/gradle-plugin) documentation.

### Gradle 9 example / Gradle 9 örneği
If your project explicitly requires Gradle 9, set its wrapper to a **specific compatible 9.x release** after verifying AGP compatibility. For example, the wrapper configuration has a line of this form (the version below is illustrative):
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.2.1-bin.zip
```
Do not blindly replace an existing working wrapper. Do not download a Gradle distribution from an unknown site.

### Java 26 note / Java 26 notu
If a particular project requires Java 26, you must obtain a **trusted, Android ARM64-compatible JDK 26 build** and verify it with `java -version`. Do not assume `pkg install openjdk-26` is available in Termux or that Java 26 is needed for every APK. Newer Java versions may not be supported by the Gradle/AGP combination selected by the project.

Belirli bir proje Java 26 gerektiriyorsa güvenilir ve Android ARM64 uyumlu JDK 26 kurulumu gerekir. Termux'ta `openjdk-26` paketinin mevcut olduğunu varsaymayın. Daha yeni Java her zaman daha iyi uyumluluk sağlamaz.

### Troubleshooting / Hata ayıklama
- `Unsupported class file major version`: check JDK versus Gradle/AGP compatibility.
- `Android Gradle plugin requires Java ...`: use the minimum supported JDK stated by AGP.
- `Gradle ... is not supported`: check the project's AGP version and wrapper.
- `aapt2` fails even after Java/Gradle succeed: this is a **separate Android ARM64 build-tools problem**, not necessarily a Java version problem.

**Community test request / Topluluk testi:** Please share the exact `java -version`, `./gradlew --version`, AGP version and whether `assembleDebug` actually produced an APK. This helps replace anecdotal requirements with verified compatibility records.


## 8. Copy the built APK to Downloads / APK'yi Indirilenler'e kopyalama

After a **successful** `assembleDebug` build, run this **from the Android project root**:

Basarili `assembleDebug` isleminden sonra, **proje ana klasorunde**:

```sh
# Request shared-storage access once if it has not been granted.
# Depolama izni daha once verilmediyse bir kez calistirin:
termux-setup-storage

# Check that the APK really exists / APK'nin olustugunu dogrulayin:
test -f app/build/outputs/apk/debug/app-debug.apk || {
  echo "APK not found / APK bulunamadi"; exit 1;
}

# Copy into the phone's Downloads folder / Telefonun Indirilenler klasorune kopyala:
cp app/build/outputs/apk/debug/app-debug.apk \
  /storage/emulated/0/Download/ByTelliDeveloperLab-V1.apk

# Verify / Dogrula:
ls -lh /storage/emulated/0/Download/ByTelliDeveloperLab-V1.apk
```

**Alternative / Alternatif:** On Termux installations with storage shortcuts configured, `$HOME/storage/downloads/` usually points to the same shared Downloads folder.

If you get `Permission denied`, grant Termux storage permission via Android settings and run `termux-setup-storage` again. Do not use `sudo` or change permissions on `/storage/emulated/0`. If the source file is missing, check the build log for `BUILD SUCCESSFUL` and confirm the project's APK output path.

`Permission denied` hatasinda Android ayarlarindan Termux depolama iznini verin ve `termux-setup-storage` komutunu yeniden calistirin. `sudo` kullanmayin; paylasilan depolamanin izinlerini degistirmeyin. Kaynak APK yoksa derleme sonucunu ve dosya yolunu kontrol edin.

**Test status / Test durumu:** On one ARM64 Android 16 device, the ByTelli Developer Lab sample built successfully in 39 seconds with Java 25.0.3, Gradle 9.6.0, `compileSdk 34` and Termux ARM64 AAPT2 override. Copying and installing the APK are separate steps; successful installation and runtime behavior are not yet verified.
