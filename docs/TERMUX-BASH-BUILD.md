# Termux'ta Gradle olmadan Android APK derleme (Bash)

Bu rehber, **Java tabanlı basit Android projelerini** Termux üzerinde Android SDK araçlarını doğrudan çağırarak derlemeyi anlatır. Gradle ile derleme için [mevcut rehbere](ANDROID-APK-BUILD.md) bakın.

> **Durum:** Bu yöntem BYBLACK Office V1/V2 ve V3 motor test APK'sında kullanıldı. Apache POI/PDFBox'ın tüm özellikleri Android'de doğrulanmış değildir. Buradaki komutlar, proje yapısına uyarlanması gereken bir örnektir.

## Bash, Gradle ve `fi` nedir?

- **Bash** komutları sırayla çalıştıran kabuktur; kendi başına Java kodu derlemez.
- **Gradle** bağımlılıkları ve derleme adımlarını yöneten otomasyon sistemidir; Android Gradle Plugin ile AAPT2, javac/D8 ve paketleme araçlarını çağırır.
- **`fi`** Bash'te `if` koşul bloğunu bitiren anahtar sözcüktür; ayrı bir derleyici değildir.

## Gerekenler

- Termux, Java JDK (`javac`, `java`), Python 3 ve Android SDK
- Android platformu (örnekte `android-34`), build-tools (örnekte `34.0.0`)
- Termux ortamında çalışan `aapt2` ve SDK build-tools içindeki `d8.jar` / `apksigner`
- Android uygulama manifesti ve Java kaynakları
- APK imzalama için **size ait** bir keystore; debug anahtarını yayın sürümünde kullanmayın

Örnek proje düzeni:

```text
my-app/
  AndroidManifest.xml
  src/com/example/app/MainActivity.java
```

Önce `pkg install openjdk-21 python aapt2` gibi paketleri ortamınıza uygun şekilde kurun. SDK yollarını ve sürümlerini kendi kurulumunuza göre değiştirin.

## Basit Java projesini Bash ile derleme

Aşağıdaki komutları **proje kökünde** çalıştırın. Manifestteki `package` ve `MainActivity` adı kaynak kodla uyumlu olmalıdır.

```bash
set -e
SDK="$HOME/android-sdk"
BT="$SDK/build-tools/34.0.0"
ANDROID="$SDK/platforms/android-34/android.jar"
AAPT2="$PREFIX/bin/aapt2"
OUT="$PWD/build-termux"

mkdir -p "$OUT/gen" "$OUT/classes" "$OUT/dex"

# 1) Android kaynakları ve manifest
"$AAPT2" link -o "$OUT/resources.apk" \
  --manifest AndroidManifest.xml \
  -I "$ANDROID" \
  --min-sdk-version 26 --target-sdk-version 28 \
  --java "$OUT/gen"

# 2) Java derlemesi
find src "$OUT/gen" -name '*.java' > "$OUT/sources.txt"
javac -source 8 -target 8 -classpath "$ANDROID" \
  -d "$OUT/classes" @"$OUT/sources.txt"

# 3) Android DEX
find "$OUT/classes" -name '*.class' > "$OUT/classes.txt"
java -cp "$BT/lib/d8.jar" com.android.tools.r8.D8 \
  --min-api 26 --lib "$ANDROID" \
  --output "$OUT/dex" @"$OUT/classes.txt"

# 4) DEX dosyalarını APK arşivine ekle
python - "$OUT" <<'PY'
import glob, os, sys, zipfile
out=sys.argv[1]
with zipfile.ZipFile(out+'/resources.apk') as src:
    with zipfile.ZipFile(out+'/unsigned.apk', 'w') as dst:
        for item in src.infolist():
            dst.writestr(item, src.read(item.filename))
        for path in sorted(glob.glob(out+'/dex/classes*.dex')):
            dst.write(path, os.path.basename(path))
PY

# 5) Debug APK imzalama
KEY="$HOME/.android/debug.keystore"
test -f "$KEY" || {
  echo "Debug keystore yok. Önce kendinize ait bir keystore oluşturun."
  exit 1
}
"$BT/apksigner" sign --ks "$KEY" \
  --ks-key-alias androiddebugkey \
  --ks-pass pass:android --key-pass pass:android \
  --out "$OUT/app-debug.apk" "$OUT/unsigned.apk"
"$BT/apksigner" verify "$OUT/app-debug.apk"
echo "APK: $OUT/app-debug.apk"
```

**Dikkat:** `target-sdk-version 28` yalnızca önceki Termux prototipleriyle uyumlu bir örnektir; modern Android uygulamalarında güncel hedef SDK ve buna bağlı izin/manifest kuralları kullanılmalıdır. Play Store dağıtımı için bu örnek yeterli değildir.

## Harici JAR kütüphaneleri

`javac -classpath` içine JAR yolları eklenmeli ve aynı JAR'lar D8'e verilmelidir. **JAR içindeki veri dosyaları** (`.xsb`, yapılandırma, servis kayıtları vb.) DEX'e otomatik eklenmez. Sadece JAR'ı DEX'e çevirmek kütüphanenin Android'de çalışacağını kanıtlamaz.

BYBLACK Office V3 deneyinde Apache POI + XMLBeans JAR'ları DEX'e dönüştü, fakat XLSX açılırken XMLBeans `ExceptionInInitializerError` verdi. PDFBox-Android PDF'den metin okuyabildi. Bu sorunlar çözülmeden Office uyumluluğu tamamlandı denmemelidir.

## Gradle mı Bash mi?

| Konu | Gradle + Android Gradle Plugin | Doğrudan Bash |
| --- | --- | --- |
| Bağımlılık yönetimi | Otomatikleştirilebilir | Elle |
| Standart Android proje akışı | Evet | Özel betik gerekir |
| Termux'ta Gradle hata verirse | Engellenebilir | Alternatif olabilir |
| Kaynak/manifest/DEX/imzalama | Araçları yönetir | Adımları tek tek yazarsınız |

**Bilinen Termux sorunu:** Bazı ortamlarda Gradle `ProtoLogNative` hatasıyla durabiliyor. Bash yolu Gradle'ı atlatabilir; her cihazda ve her Android projesinde başarı garantisi yoktur.

## Güvenlik ve GitHub

Keystore, parolalar, API anahtarları, `build/` klasörü ve kişisel dosyaları GitHub'a göndermeyin. Üçüncü taraf JAR/AAR dosyalarını yüklemek yerine sürüm, indirme kaynağı ve lisans bilgilerini belgeleyin.
