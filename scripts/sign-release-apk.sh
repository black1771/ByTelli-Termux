#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
echo "=== BYBLACK RELEASE APK HELPER ==="
echo "Builds a release APK. Secrets are never written to this repository."
read -r -p "Android project directory: " PROJECT
[ -d "$PROJECT" ] || { echo "ERROR: directory not found"; exit 1; }
cd "$PROJECT"
[ -f app/build.gradle ] || { echo "ERROR: app/build.gradle not found"; exit 1; }
command -v gradle >/dev/null || { echo "ERROR: gradle missing"; exit 1; }
command -v keytool >/dev/null || { echo "ERROR: keytool missing"; exit 1; }
command -v apksigner >/dev/null || { echo "ERROR: apksigner missing. Install/check Android build tools."; exit 1; }
read -r -p "Keystore path (existing or new .jks): " KEYSTORE
read -r -p "Key alias [byblack]: " ALIAS
ALIAS="${ALIAS:-byblack}"
if [ ! -f "$KEYSTORE" ]; then
 echo "Creating a new signing key. Keep this file backed up and private."
 keytool -genkeypair -v -keystore "$KEYSTORE" -alias "$ALIAS" -keyalg RSA -keysize 4096 -validity 10000
fi
echo "Building unsigned release APK..."
gradle --no-daemon assembleRelease
APK="$(find app/build/outputs/apk/release -maxdepth 1 -type f -name "*.apk" | head -1 || true)"
[ -n "$APK" ] || { echo "ERROR: release APK not found"; exit 1; }
OUT="${APK%.apk}-signed.apk"
echo "Signing APK. apksigner will request the keystore password securely."
apksigner sign --ks "$KEYSTORE" --ks-key-alias "$ALIAS" --out "$OUT" "$APK"
apksigner verify --verbose "$OUT"
echo "SIGNED APK: $PROJECT/$OUT"
echo "IMPORTANT: Back up your keystore. Losing it can prevent future updates signed with the same key."
