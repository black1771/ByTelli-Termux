#!/data/data/com.termux/files/usr/bin/bash
set -u
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
while true; do
 clear
 echo "=== BYBLACK DEV TOOL | Android APK Builder / ARM64 ==="
 echo "1) Check environment"
 echo "2) Guided SDK setup"
 echo "3) Create Android Java project"
 echo "4) Build a project"
 echo "5) Build example app"
 echo "6) Show APK files"
 echo "7) Byblack Doctor"
 echo "8) Build + sign release APK"
 echo "9) Create diagnostic report"
 echo "0) Exit"
 printf "\nSelect: "
 read -r x || exit 0
 case "$x" in
  1) bash "$ROOT/scripts/check-environment.sh";;
  2) bash "$ROOT/scripts/setup-android-build.sh";;
  3) bash "$ROOT/scripts/new-android-project.sh";;
  4) printf "Project directory: "; read -r p || continue; if [ -d "$p" ]; then (cd "$p" && gradle --no-daemon assembleDebug); else echo "Directory not found"; fi;;
  5) bash "$ROOT/scripts/build-example.sh";;
  6) find "$HOME" -path "*/build/outputs/apk/*.apk" -type f 2>/dev/null | head -50;;
  7) bash "$ROOT/scripts/byblack-doctor.sh";;
  8) bash "$ROOT/scripts/sign-release-apk.sh";;
  9) bash "$ROOT/scripts/byblack-report.sh";;
  0) exit 0;;
  *) echo "Invalid selection";;
 esac
 printf "\nPress Enter..."
 read -r _ || exit 0
done
