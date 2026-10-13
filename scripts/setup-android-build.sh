#!/data/data/com.termux/files/usr/bin/bash
# Byblack guided setup for standard Termux on ARM64 Android.
# Google SDK tools are NOT downloaded automatically. User supplies the official ZIP.
set -euo pipefail

echo "=== Byblack Android Setup ==="
[ "$(uname -m)" = "aarch64" ] || { echo "ERROR: tested setup requires ARM64/aarch64."; exit 1; }
[ "${PREFIX:-}" = "/data/data/com.termux/files/usr" ] || { echo "ERROR: use standard Termux."; exit 1; }

echo "[1/6] Checking required Termux tools..."
for c in java gradle aapt2 unzip git; do
  command -v "$c" >/dev/null 2>&1 || { echo "MISSING: $c"; echo "See docs/ANDROID-APK-BUILD.md"; exit 1; }
done

ZIP="${1:-}"
if [ -z "$ZIP" ]; then
  ZIP="$(ls -1t "$HOME"/storage/downloads/commandlinetools-linux-*.zip 2>/dev/null | head -1 || true)"
fi
[ -n "$ZIP" ] && [ -f "$ZIP" ] || {
  echo "ERROR: official Android command-line-tools ZIP not found."
  echo "Download it from: https://developer.android.com/studio#command-tools"
  echo "Then rerun this script, optionally passing the ZIP path."
  exit 1
}

ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
export ANDROID_HOME
export ANDROID_SDK_ROOT="$ANDROID_HOME"
TMP="$HOME/.bytelli-sdk-unpack"

echo "[2/6] Preparing Android command-line tools..."
rm -rf "$TMP"
mkdir -p "$TMP" "$ANDROID_HOME/cmdline-tools/latest"
unzip -q -o "$ZIP" -d "$TMP"
[ -f "$TMP/cmdline-tools/bin/sdkmanager" ] || { echo "ERROR: unexpected SDK ZIP layout."; exit 1; }
rm -rf "$ANDROID_HOME/cmdline-tools/latest"/*
cp -R "$TMP/cmdline-tools/." "$ANDROID_HOME/cmdline-tools/latest/"
rm -rf "$TMP"
SDKMANAGER="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
chmod +x "$SDKMANAGER"

echo "[3/6] Installing verified SDK 34 packages..."
"$SDKMANAGER" --sdk_root="$ANDROID_HOME" "platforms;android-34" "build-tools;34.0.0"

echo "[4/6] Saving environment variables..."
PROFILE="$HOME/.bashrc"
if ! grep -q 'BYBLACK_ANDROID_ENV' "$PROFILE" 2>/dev/null; then
cat >> "$PROFILE" <<'EOF'
# BYBLACK_ANDROID_ENV
export ANDROID_HOME="$HOME/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
EOF
fi

echo "[5/6] Configuring the example project..."
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PROJECT="$ROOT/examples/ByTelliDeveloperLabV1"
printf "sdk.dir=%s\n" "$ANDROID_HOME" > "$PROJECT/local.properties"
printf "android.aapt2FromMavenOverride=%s\n" "$PREFIX/bin/aapt2" > "$PROJECT/gradle.properties"

echo "[6/6] Ready."
echo
echo "Google licenses must be reviewed by you. Run:"
echo "  $SDKMANAGER --sdk_root=$ANDROID_HOME --licenses"
echo
echo "Then build the example APK:"
echo "  cd $PROJECT"
echo "  bash $ROOT/scripts/build-example.sh"
echo
echo "Byblack never accepts licenses or installs APKs without your confirmation."
