#!/data/data/com.termux/files/usr/bin/bash
# Build the Byblack Developer Lab example using standard Termux.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PROJECT="$ROOT/examples/ByTelliDeveloperLabV1"
[ "${PREFIX:-}" = "/data/data/com.termux/files/usr" ] || { echo "ERROR: use standard Termux."; exit 1; }
for cmd in java javac gradle aapt2; do
 command -v "$cmd" >/dev/null 2>&1 || { echo "ERROR: missing $cmd"; exit 1; }
done
SDK="${ANDROID_HOME:-$HOME/android-sdk}"
[ -f "$SDK/platforms/android-34/android.jar" ] || { echo "ERROR: SDK 34 missing at $SDK"; exit 1; }
[ -x "$PREFIX/bin/aapt2" ] || { echo "ERROR: native Termux aapt2 missing."; exit 1; }
cd "$PROJECT"
printf 'sdk.dir=%s\n' "$SDK" > local.properties
printf 'android.aapt2FromMavenOverride=%s\n' "$PREFIX/bin/aapt2" > gradle.properties
APK="$PROJECT/app/build/outputs/apk/debug/app-debug.apk"
# Remove the old output to avoid mistaking a stale APK for a fresh build.
rm -f "$APK"
echo "=== Building Byblack Developer Lab example ==="
gradle --no-daemon assembleDebug
[ -s "$APK" ] || { echo "ERROR: fresh APK was not created."; exit 1; }
echo "BUILD OK: $APK"
if [ -d "$HOME/storage/downloads" ]; then
 OUT="$HOME/storage/downloads/ByblackDeveloperLab-V1-debug.apk"
 cp "$APK" "$OUT"
 echo "COPIED: $OUT"
else
 echo "TIP: run termux-setup-storage to copy APKs to Downloads."
fi
