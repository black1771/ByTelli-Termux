#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
SRC="/sdcard/Download/ByblackBattleChess-Source-V132.zip"
DEST="$HOME/ByblackBattleChess-V132-GitHub"
echo '=== BYBLACK BATTLE CHESS TERMUX BUILD ==='
[ -f "$SRC" ] || { echo "Kaynak ZIP bulunamadı: $SRC"; exit 1; }
command -v unzip >/dev/null || pkg install -y unzip
command -v gradle >/dev/null || pkg install -y gradle
command -v java >/dev/null || { echo 'Java yok. Önce pkg install openjdk-21'; exit 1; }
if [ -z "${JAVA_HOME:-}" ] && [ -d "$PREFIX/lib/jvm/java-21-openjdk" ]; then export JAVA_HOME="$PREFIX/lib/jvm/java-21-openjdk"; fi
export ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
[ -d "$ANDROID_HOME/platforms/android-34" ] || { echo "SDK platform 34 eksik: $ANDROID_HOME"; exit 1; }
AAPT="$ANDROID_HOME/build-tools/35.0.0/aapt2"
[ -x "$AAPT" ] || AAPT="$(command -v aapt2 || true)"
[ -n "$AAPT" ] && [ -x "$AAPT" ] || { echo 'ARM64 aapt2 bulunamadı'; exit 1; }
mkdir -p "$DEST"; unzip -oq "$SRC" -d "$DEST"
cd "$DEST"
printf 'sdk.dir=%s\n' "$ANDROID_HOME" > local.properties
mkdir -p "$HOME/.gradle"
printf '\nandroid.aapt2FromMavenOverride=%s\n' "$AAPT" >> "$HOME/.gradle/gradle.properties"
gradle --no-daemon assembleDebug
APK="$DEST/app/build/outputs/apk/debug/app-debug.apk"
[ -f "$APK" ] || { echo 'APK üretilmedi'; exit 1; }
cp "$APK" /sdcard/Download/ByblackBattleChess-V132-Demo.apk
sha256sum /sdcard/Download/ByblackBattleChess-V132-Demo.apk
echo 'APK: /sdcard/Download/ByblackBattleChess-V132-Demo.apk'
