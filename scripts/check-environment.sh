#!/data/data/com.termux/files/usr/bin/sh
# Read-only diagnostic for standard Termux
set -u
echo "=== Standard Termux / Normal Termux ==="
printf 'Architecture: '; uname -m
printf 'Android: '; getprop ro.build.version.release 2>/dev/null || true
printf 'SDK: '; getprop ro.build.version.sdk 2>/dev/null || true
printf 'PREFIX: %s\n' "${PREFIX:-<unset>}"
if [ "${PREFIX:-}" != "/data/data/com.termux/files/usr" ]; then
  echo "NOTE: This does not appear to be the standard Termux prefix."
fi
for cmd in java javac python python3 clang make cmake git gradle aapt2; do
  if command -v "$cmd" >/dev/null 2>&1; then
    printf '%-10s %s\n' "$cmd" "$(command -v "$cmd")"
  else
    printf '%-10s NOT FOUND\n' "$cmd"
  fi
done
echo "No changes made; no data uploaded."
