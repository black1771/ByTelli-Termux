#!/usr/bin/env sh
# ByTelli-Termux environment diagnostics (read-only)
set -u
echo "=== ByTelli-Termux environment ==="
printf 'Date: '; date
printf 'Architecture: '; uname -m
printf 'Android release: '; getprop ro.build.version.release 2>/dev/null || true
printf 'Android SDK: '; getprop ro.build.version.sdk 2>/dev/null || true
printf 'PREFIX: %s\n' "${PREFIX:-<unset>}"
printf 'HOME: %s\n' "${HOME:-<unset>}"
for tool in java javac python3 clang gcc make cmake gradle git apt dpkg aapt2; do
  if command -v "$tool" >/dev/null 2>&1; then
    printf '%-10s %s\n' "$tool" "$(command -v "$tool")"
  else
    printf '%-10s %s\n' "$tool" "NOT FOUND"
  fi
done
echo "=== Versions (where available) ==="
java -version 2>&1 | head -n 3 || true
python3 --version 2>&1 || true
clang --version 2>&1 | head -n 1 || true
cmake --version 2>&1 | head -n 1 || true
echo "This script does not install packages, change settings or upload data."
