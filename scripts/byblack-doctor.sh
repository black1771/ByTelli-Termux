#!/data/data/com.termux/files/usr/bin/bash
set -u
PASS=0; WARN=0; FAIL=0
ok(){ echo "[OK]   $*"; PASS=$((PASS+1)); }
warn(){ echo "[WARN] $*"; WARN=$((WARN+1)); }
fail(){ echo "[FAIL] $*"; FAIL=$((FAIL+1)); }
echo "=== BYBLACK DOCTOR ==="
echo "Phone-only Android build diagnostics"
echo
ARCH="$(uname -m 2>/dev/null || true)"
[ "$ARCH" = "aarch64" ] && ok "Architecture: aarch64" || fail "Architecture: ${ARCH:-unknown} (verified workflow requires ARM64/aarch64)"
if [ "${PREFIX:-}" = "/data/data/com.termux/files/usr" ]; then ok "Standard Termux prefix"; else warn "PREFIX=${PREFIX:-unset}; verified baseline is standard Termux"; fi
if command -v java >/dev/null 2>&1; then
 J="$(java -version 2>&1 | head -1)"; ok "Java: $J"
else fail "Java missing. Run: pkg search openjdk"; fi
if command -v gradle >/dev/null 2>&1; then
 G="$(gradle --version 2>/dev/null | grep -m1 "^Gradle " || true)"; ok "${G:-Gradle found}"
else fail "Gradle missing. Run: pkg search gradle"; fi
if command -v aapt2 >/dev/null 2>&1; then
 A="$(command -v aapt2)"; F="$(file "$A" 2>/dev/null || true)"
 echo "$F" | grep -qiE "ARM aarch64|ARM64" && ok "ARM64 AAPT2: $A" || warn "AAPT2 found but architecture could not be confirmed: $F"
else fail "aapt2 missing. Run: pkg search aapt2"; fi
ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
if [ -f "$ANDROID_HOME/platforms/android-34/android.jar" ]; then ok "Android SDK platform 34"; else fail "SDK 34 missing: $ANDROID_HOME/platforms/android-34/android.jar"; fi
if [ -x "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]; then ok "sdkmanager available"; else warn "sdkmanager missing from cmdline-tools/latest"; fi
FREE="$(df -Pk "$HOME" 2>/dev/null | awk "NR==2 {print \$4}" || true)"
if [ -n "$FREE" ]; then
 if [ "$FREE" -ge 3145728 ] 2>/dev/null; then ok "Free storage: $((FREE/1024/1024)) GB"; else warn "Low free storage: $((FREE/1024)) MB"; fi
fi
[ -d "$HOME/storage/downloads" ] && ok "Android Downloads storage link available" || warn "Storage link missing. Run: termux-setup-storage"
echo
echo "=== RESULT ==="
echo "OK=$PASS WARN=$WARN FAIL=$FAIL"
if [ "$FAIL" -eq 0 ]; then echo "Environment looks ready for the verified Byblack workflow."; exit 0; else echo "Fix FAIL items first, then run the doctor again."; exit 1; fi
