#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

echo "=== ByBlack DeviceLab V2 PRO | Termux APK build ==="
if [[ -z "${PREFIX:-}" || ! -d "$PREFIX" ]]; then
  echo "HATA: Bu betik standart Termux icinde calistirilmalidir."
  exit 1
fi

for c in git java gradle aapt2; do
  command -v "$c" >/dev/null 2>&1 || { echo "Eksik arac: $c"; exit 1; }
done

if [[ -z "${ANDROID_HOME:-}" ]]; then
  export ANDROID_HOME="$HOME/android-sdk"
fi
if [[ ! -f "$ANDROID_HOME/platforms/android-34/android.jar" ]]; then
  echo "Android SDK 34 bulunamadi: $ANDROID_HOME"
  echo "Kurulum: https://github.com/black1771/ByTelli-Termux/blob/main/docs/ANDROID-APK-BUILD.md"
  exit 1
fi

if [[ -d "$PREFIX/lib/jvm/java-26-openjdk" ]]; then
  export JAVA_HOME="$PREFIX/lib/jvm/java-26-openjdk"
  export PATH="$JAVA_HOME/bin:$PATH"
fi
export TMPDIR="$PREFIX/tmp"
mkdir -p "$TMPDIR"
export LD_PRELOAD="$PREFIX/lib/libtermux-exec.so"

ROOT="$HOME/ByTelli-Termux"
if [[ -e "$ROOT" ]]; then
  echo "Mevcut klasor bulundu: $ROOT"
  echo "Var olan dosyalarin uzerine yazilmiyor."
  echo "Lutfen kaynak kodu bu klasorde guncelleyip yeniden calistirin."
else
  git clone --depth 1 https://github.com/black1771/ByTelli-Termux.git "$ROOT"
fi
cd "$ROOT/examples/ByBlackDeviceLab"

# Termux-native aapt2 is necessary on ARM64.
printf '\nandroid.aapt2FromMavenOverride=%s\n' "$PREFIX/bin/aapt2" >> gradle.properties

echo "Java: $(java -version 2>&1 | head -1)"
echo "Android SDK: $ANDROID_HOME"
echo "Derleme basliyor..."
gradle --offline --no-daemon assembleDebug

APK="app/build/outputs/apk/debug/app-debug.apk"
[[ -s "$APK" ]] || { echo "APK olusmadi."; exit 1; }
OUT="/storage/emulated/0/Download/ByBlack-DeviceLab-V2-PRO.apk"
if [[ -d /storage/emulated/0/Download && -w /storage/emulated/0/Download ]]; then
  cp "$APK" "$OUT"
  echo "APK HAZIR: $OUT"
else
  echo "APK HAZIR: $PWD/$APK"
  echo "Indirilenler klasorune kopyalamak icin: termux-setup-storage"
fi
