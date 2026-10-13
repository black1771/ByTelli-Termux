#!/data/data/com.termux/files/usr/bin/bash
set -u
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
while true; do
 clear
 echo "=== BYBLACK DEV TOOL | Android APK Builder / ARM64 ==="
 echo "1) Check environment"\necho "7) Byblack Doctor"\necho "8) Build + sign release APK"
 echo "2) Guided SDK setup"
 echo "3) Create Android Java project"
 echo "4) Build a project"
 echo "5) Build example app"
 echo "6) Show APK files"
 echo "0) Exit"
 printf "\nSelect: "; read -r x
 case "$x" in
  1) bash "$ROOT/scripts/check-environment.sh";;
  2) bash "$ROOT/scripts/setup-android-build.sh";;
  3) bash "$ROOT/scripts/new-android-project.sh";;
  4) printf "Project directory: "; read -r p; [ -d "$p" ] && (cd "$p" && gradle --no-daemon assembleDebug) || echo "Directory not found";;
  5) bash "$ROOT/scripts/build-example.sh";;
  6) find "$HOME" -path "*/build/outputs/apk/*.apk" -type f 2>/dev/null | head -50;;\n  7) bash "$ROOT/scripts/byblack-doctor.sh";;\n  8) bash "$ROOT/scripts/sign-release-apk.sh";;
  0) exit 0;;
  *) echo "Invalid selection";;
 esac
 printf "\nPress Enter..."; read -r _
done
