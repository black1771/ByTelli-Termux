#!/data/data/com.termux/files/usr/bin/bash
set -u
OUT="${1:-$HOME/byblack-diagnostic-report.txt}"
{
echo "Byblack Diagnostic Report"; echo "Generated: $(date)"; echo
echo "== SYSTEM =="; uname -a 2>/dev/null || true; echo "PREFIX=${PREFIX:-unset}"; echo
echo "== TOOLS ==";
java -version 2>&1 | head -3 || echo "java: missing";
gradle --version 2>/dev/null | grep -E "^(Gradle|JVM|OS)" || echo "gradle: missing";
if command -v aapt2 >/dev/null 2>&1; then file "$(command -v aapt2)" 2>/dev/null; aapt2 version 2>&1 | head -2; else echo "aapt2: missing"; fi;
echo; echo "== ANDROID SDK =="; AH="${ANDROID_HOME:-$HOME/android-sdk}"; echo "ANDROID_HOME=$AH";
[ -f "$AH/platforms/android-34/android.jar" ] && echo "android-34: present" || echo "android-34: missing";
[ -x "$AH/cmdline-tools/latest/bin/sdkmanager" ] && echo "sdkmanager: present" || echo "sdkmanager: missing";
echo; echo "== STORAGE =="; df -h "$HOME" 2>/dev/null | tail -1 || true;
echo; echo "== PRIVACY =="; echo "This report intentionally excludes wallet keys, tokens, passwords, signing keys, app data and personal files.";
} > "$OUT"
echo "REPORT CREATED: $OUT"
echo "Review it before sharing. Do not add secrets manually."
