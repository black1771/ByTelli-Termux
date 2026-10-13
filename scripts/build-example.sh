#!/data/data/com.termux/files/usr/bin/bash
# Build the ByTelli Developer Lab example using the verified Termux setup.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PROJECT="$ROOT/examples/ByTelliDeveloperLabV1"
cd "$PROJECT"

[ -f local.properties ] || { echo "ERROR: run scripts/setup-android-build.sh first."; exit 1; }
command -v gradle >/dev/null 2>&1 || { echo "ERROR: gradle not found."; exit 1; }
command -v aapt2 >/dev/null 2>&1 || { echo "ERROR: ARM64 aapt2 not found."; exit 1; }

echo "=== Building ByTelli Developer Lab example ==="
gradle --no-daemon assembleDebug
APK="$PROJECT/app/build/outputs/apk/debug/app-debug.apk"
[ -s "$APK" ] || { echo "ERROR: APK was not created."; exit 1; }
echo "BUILD OK: $APK"

if [ -d "$HOME/storage/downloads" ]; then
  OUT="$HOME/storage/downloads/ByTelliDeveloperLab-V1-debug.apk"
  cp "$APK" "$OUT"
  echo "COPIED: $OUT"
else
  echo "TIP: run termux-setup-storage to copy APKs to Downloads."
fi
