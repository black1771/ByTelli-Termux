# Android APK from zero to installed — Standard Termux (ARM64)
# Sıfırdan telefonda APK üretme ve kurma

> **Tested reference:** Byblack Developer Lab V1.0 was built, installed and opened on one Samsung ARM64 Android 16 phone using standard Termux, OpenJDK 25, Gradle 9.6, AGP 8.7.3, SDK platform 34 and Termux-native AAPT2. **Other devices are not guaranteed.**
>
> **Önemli:** Bu rehber Android ARM64 içindir. Android SDK'nın Google tarafından sunulan bazı araçları Linux x86_64 içindir; Android telefonunda doğrudan çalışmayabilir. Özellikle ARM64 `aapt2` gereklidir. Buradaki komutları sırayla uygulayın; kurulum paketlerinin kullanılabilirliği Termux sürümüne göre değişebilir.

## 0 — Requirements / Gerekenler

- ARM64 Android phone (check `uname -m` returns `aarch64`)
- Reliable Wi-Fi and several GB of free storage
- Official standard Termux: https://github.com/termux/termux-app#installation
- No root, PC, custom Termux fork or wallet required
- A browser for downloading the **official Google Android command-line tools ZIP**

**Security:** Only download Android SDK command-line tools from `developer.android.com`. Do not run random `curl | sh` installers or share signing keys.

## 1 — Prepare Termux / Termux'u hazırlayın

Open Termux and run:

```sh
pkg update && pkg upgrade -y
pkg install -y git curl wget unzip zip python clang make cmake
termux-setup-storage
uname -m
```

Grant Android file access if prompted. Expected architecture: `aarch64`. If it is not ARM64, this exact tested setup does not apply.

## 2 — Java, Gradle, ARM64 AAPT2 / Araçları kurun

Check package names **before installing**; repositories change:

```sh
pkg search openjdk
pkg search gradle
pkg search aapt2
```

On a repository offering these packages, try:

```sh
pkg install -y openjdk-25 gradle aapt2
java -version
gradle --version
command -v aapt2
aapt2 version
```

**Stop if any required package is unavailable.** Use `pkg search` to locate an Android-compatible alternative and report the result in an [Issue](https://github.com/black1771/ByTelli-Termux/issues). Do **not** substitute an x86_64 Linux `aapt2`. The verified build used **Java 25.0.3** and **Gradle 9.6**; other versions may require adjustments. Java 21 crashed on the tested Android 16 phone, so it is not the verified baseline.

**Türkçe:** `openjdk-25`, `gradle` veya `aapt2` bulunamazsa rastgele paket indirip çalıştırmayın. Kurulumun kalanına geçmeden doğru Android ARM64 paketini bulun. Rehber henüz her Termux dağıtımı için otomatik kurulum garantisi vermez.

## 3 — Download the official Android SDK tools / SDK'yı indirin

1. Open Google's official page: https://developer.android.com/studio#command-tools
2. Under **Command line tools only**, download the **Linux** command-line tools ZIP. This contains mostly Java-based `sdkmanager` tools, **but its Linux host binaries are not necessarily Android ARM64-compatible**.
3. Save the ZIP in your phone's **Downloads** folder. The ZIP filename changes over time.

Back in Termux:

```sh
export ANDROID_HOME="$HOME/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
mkdir -p "$ANDROID_HOME/cmdline-tools/latest"
ls "$HOME/storage/downloads"/commandlinetools-linux-*.zip
```

If the `ls` command shows no file, find the downloaded ZIP's real name/path before continuing.

```sh
mkdir -p "$HOME/sdk-unpack"
unzip -o "$HOME"/storage/downloads/commandlinetools-linux-*.zip -d "$HOME/sdk-unpack"
test -f "$HOME/sdk-unpack/cmdline-tools/bin/sdkmanager" || {
  echo "SDK ZIP layout unexpected: inspect the extracted files"; exit 1;
}
cp -R "$HOME/sdk-unpack/cmdline-tools/." "$ANDROID_HOME/cmdline-tools/latest/"
chmod +x "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
```

To keep variables across new Termux sessions:

```sh
cat >> "$HOME/.bashrc" <<'EOF'
export ANDROID_HOME="$HOME/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
EOF
source "$HOME/.bashrc"
```

## 4 — Install SDK Platform 34 / Android 34 platformunu kurun

```sh
sdkmanager --sdk_root="$ANDROID_HOME" "platforms;android-34" "build-tools;34.0.0"
sdkmanager --sdk_root="$ANDROID_HOME" --licenses
test -f "$ANDROID_HOME/platforms/android-34/android.jar" && echo "SDK PLATFORM 34 OK"
```

Read and accept Google's license prompts if you agree. An SDK package download **does not make its bundled x86_64 executables usable on ARM64**; the next step supplies native AAPT2.

If `sdkmanager` fails with an architecture or Java error, stop and include the exact error in an Issue; this guide does not claim a universal workaround.

## 5 — Get the actual sample app source / Örnek projeyi indirin

```sh
cd "$HOME"
git clone https://github.com/black1771/ByTelli-Termux.git
cd "$HOME/ByTelli-Termux/examples/ByTelliDeveloperLabV1"
ls app/src/main/java/com/bytelli/developerlab/MainActivity.java
```

If already cloned, use `cd "$HOME/ByTelli-Termux" && git pull` rather than cloning again.

## 6 — Connect SDK and ARM64 AAPT2 / SDK ve AAPT2 ayarları

From **the `ByblackDeveloperLabV1` project directory**:

```sh
export JAVA_HOME="$PREFIX/lib/jvm/java-25-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$HOME/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
test -x "$JAVA_HOME/bin/java" || { echo "Java 25 location missing"; exit 1; }
test -x "$PREFIX/bin/aapt2" || { echo "Termux ARM64 aapt2 missing"; exit 1; }
test -f "$ANDROID_HOME/platforms/android-34/android.jar" || { echo "SDK 34 missing"; exit 1; }
printf 'sdk.dir=%s\n' "$ANDROID_HOME" > local.properties
printf 'android.aapt2FromMavenOverride=%s\n' "$PREFIX/bin/aapt2" > gradle.properties
java -version
aapt2 version
```

`local.properties` and `gradle.properties` contain **device-local paths**; do not commit them to a shared repository.

## 7 — Compile the APK / APK derleyin

```sh
gradle --no-daemon assembleDebug
```

Look for **`BUILD SUCCESSFUL`**. Confirm the APK:

```sh
test -s app/build/outputs/apk/debug/app-debug.apk && echo "APK CREATED"
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

This project currently uses **`compileSdk 34`**. Do not change it to 35/36 merely to use a newer platform: the tested Termux AAPT2 was compatible with 34 but failed with newer platforms.

**Troubleshooting / Hatalar:**

| Message | What to check |
| --- | --- |
| `SDK location not found` | `local.properties` and `ANDROID_HOME` |
| `aapt2` cannot run / exec format error | Is it really the Termux ARM64 executable? |
| `Android Gradle plugin requires Java...` | Verify Java / Gradle / AGP versions |
| `Unsupported class file major version` | JDK/Gradle compatibility |
| SDK license not accepted | `sdkmanager --licenses` |
| `BUILD FAILED` | Read the **first actual error**, not only the last line |

## 8 — Copy APK to Downloads / İndirilenler'e kopyalayın

```sh
termux-setup-storage
mkdir -p "$HOME/storage/downloads"
cp app/build/outputs/apk/debug/app-debug.apk \
  "$HOME/storage/downloads/ByblackDeveloperLab-V1-debug.apk"
ls -lh "$HOME/storage/downloads/ByblackDeveloperLab-V1-debug.apk"
```

**Do not use `mkdir` on `/storage/emulated/0` or change its permissions.** If storage permission is denied, enable Termux storage access in Android Settings.

## 9 — Install and launch / Telefona yükleyin

1. Open Android **My Files / Dosyalarım**.
2. Go to **Downloads / İndirilenler**.
3. Tap `ByblackDeveloperLab-V1-debug.apk`.
4. If Android asks, allow **Install unknown apps** for the file manager you are using; only grant this to a trusted installer.
5. Confirm installation, then tap **Open / Aç**.
6. Create a project, create `Main.java`, save, close the app, reopen and verify persistence.

**Note:** This is a **debug-signed APK** for learning/testing, not a Play Store production release. Installing an APK from a different signing key over an existing app with the same package name can fail; back up your app data before uninstalling.

## 10 — If you only want to install the app / Sadece APK kurmak isteyenler

You **do not need Termux, Java, Gradle or SDK** just to install the already built APK:

**[Download the V1.0 APK from GitHub Releases](https://github.com/black1771/ByTelli-Termux/releases/tag/v1.0.0)**

Open the downloaded APK in your file manager and install it. The application is a **Java source editor**, not an APK compiler.

## Help improve the guide / Rehberi geliştirin

This is a **reproducible reference for one tested ARM64 Android 16 setup**, not a universal automatic SDK installer. If you complete the guide, please report your device model, Android version, `uname -m`, `java -version`, `gradle --version`, `aapt2 version`, and whether the APK installed successfully. **Never post private keys, tokens, signing secrets or personal files.**

- [Report installation problems](https://github.com/black1771/ByTelli-Termux/issues/new)
- [Byblack source code](https://github.com/black1771/ByTelli-Termux)
- [Support the project](../SUPPORT.md)
