# Byblack File Manager V1

A small Android Java file-manager example for the Byblack phone-only development toolkit.

## V1 features
- Choose a folder with Android Storage Access Framework (SAF)
- Persistent folder permission
- Browse files and folders
- Open files with compatible Android apps
- Create folders
- Rename files/folders
- Delete with confirmation
- No broad storage permission and no network permission

Long-press a file or folder for Rename/Delete.

## Build on Termux
Use the repository's SDK 34 / ARM64 AAPT2 setup, then from this directory:

```sh
printf 'sdk.dir=%s\n' "$HOME/android-sdk" > local.properties
printf 'android.aapt2FromMavenOverride=%s\n' "$PREFIX/bin/aapt2" > gradle.properties
gradle --no-daemon assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

This is an example project. Test file operations on non-important files first.
