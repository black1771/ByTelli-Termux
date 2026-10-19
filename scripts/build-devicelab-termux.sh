#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

echo "=== ByBlack DeviceLab V2 PRO | Android Termux APK ==="
if [[ "${PREFIX:-}" != "/data/data/com.termux/files/usr" ]]; then
  echo "HATA: Standart Termux gerekir (com.termux)."
  exit 1
fi
if [[ "$(uname -m)" != "aarch64" ]]; then
  echo "HATA: Bu rehber ARM64/aarch64 icindir."
  exit 1
fi

for c in git java gradle aapt2; do
  command -v "$c" >/dev/null 2>&1 || { echo "Eksik arac: $c"; echo "Kurulum: https://github.com/black1771/ByTelli-Termux/blob/main/docs/ANDROID-APK-BUILD.md"; exit 1; }
done

export ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
if [[ ! -f "$ANDROID_HOME/platforms/android-34/android.jar" ]]; then
  echo "Android SDK 34 eksik: $ANDROID_HOME/platforms/android-34/android.jar"
  echo "Rehber: https://github.com/black1771/ByTelli-Termux/blob/main/docs/ANDROID-APK-BUILD.md"
  exit 1
fi

export TMPDIR="$PREFIX/tmp"
mkdir -p "$TMPDIR"
if [[ -f "$PREFIX/lib/libtermux-exec.so" ]]; then
  export LD_PRELOAD="$PREFIX/lib/libtermux-exec.so"
fi

# Fresh source tree per run; do not overwrite user's projects.
WORK="$(mktemp -d "$HOME/byblack-devicelab-build.XXXXXX")"
echo "Kaynak dizin: $WORK"
git clone --depth 1 https://github.com/black1771/ByTelli-Termux.git "$WORK/repo"
cd "$WORK/repo/examples/ByBlackDeviceLab"

# Set native ARM64 AAPT2 path (replace instead of appending duplicates).
sed -i '/^android.aapt2FromMavenOverride=/d' gradle.properties
printf 'android.aapt2FromMavenOverride=%s\n' "$(command -v aapt2)" >> gradle.properties

# Termux builds can use pre-cached AGP artifacts or download them on first run.
echo "Java: $(java -version 2>&1 | head -1)"
echo "Gradle: $(gradle --version | grep -m1 Gradle || true)"
echo "SDK: $ANDROID_HOME"
echo "Gradle derlemesi basliyor (ilk kez internet gerekebilir)..."
gradle --no-daemon assembleDebug

APK="app/build/outputs/apk/debug/app-debug.apk"
[[ -s "$APK" ]] || { echo "HATA: APK olusmadi."; exit 1; }

OUT="/storage/emulated/0/Download/ByBlack-DeviceLab-V2-PRO.apk"
if [[ -d /storage/emulated/0/Download && -w /storage/emulated/0/Download ]]; then
  cp "$APK" "$OUT"
  echo "BASARILI: $OUT"
else
  echo "BASARILI: $PWD/$APK"
  echo "Indirilenlere kopyalamak icin termux-setup-storage izni verin."
fi
echo "Derleme kaynaklari: $WORK"
