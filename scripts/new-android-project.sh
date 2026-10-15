#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

[ "${PREFIX:-}" = "/data/data/com.termux/files/usr" ] || { echo "ERROR: use standard Termux."; exit 1; }
command -v gradle >/dev/null 2>&1 || { echo "ERROR: install Gradle first."; exit 1; }
command -v aapt2 >/dev/null 2>&1 || { echo "ERROR: install ARM64 aapt2 first."; exit 1; }
read -r -p "App name (example MyApp): " APP
read -r -p "Package (example com.example.myapp): " PKG
[[ "$APP" =~ ^[A-Za-z][A-Za-z0-9_]*$ ]] || { echo "Invalid app name"; exit 1; }
[[ "$PKG" =~ ^[a-zA-Z][a-zA-Z0-9_]*(\.[a-zA-Z][a-zA-Z0-9_]*)+$ ]] || { echo "Invalid package"; exit 1; }
DIR="$HOME/$APP"
[ ! -e "$DIR" ] || { echo "Already exists: $DIR"; exit 1; }
JAVA_DIR="$DIR/app/src/main/java/${PKG//./\/}"
mkdir -p "$JAVA_DIR" "$DIR/app/src/main"
cat > "$DIR/settings.gradle" <<EOF
pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral() } }
rootProject.name = '$APP'
include ':app'
EOF
cat > "$DIR/build.gradle" <<EOF
plugins { id 'com.android.application' version '8.7.3' apply false }
EOF
cat > "$DIR/app/build.gradle" <<EOF
plugins { id 'com.android.application' }
android {
 namespace '$PKG'
 compileSdk 34
 defaultConfig {
  applicationId '$PKG'
  minSdk 26
  targetSdk 28
  versionCode 1
  versionName '1.0'
 }
}
EOF
cat > "$DIR/app/src/main/AndroidManifest.xml" <<EOF
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
 <application android:theme="@android:style/Theme.Material.Light.NoActionBar" android:label="$APP">
  <activity android:name=".MainActivity" android:exported="true">
   <intent-filter>
    <action android:name="android.intent.action.MAIN"/>
    <category android:name="android.intent.category.LAUNCHER"/>
   </intent-filter>
  </activity>
 </application>
</manifest>
EOF
cat > "$JAVA_DIR/MainActivity.java" <<EOF
package $PKG;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
public class MainActivity extends Activity {
 @Override public void onCreate(Bundle b) {
  super.onCreate(b);
  TextView v=new TextView(this);
  v.setText("Hello from $APP - built on Android with Byblack!");
  v.setTextSize(22);
  v.setPadding(32,64,32,32);
  setContentView(v);
 }
}
EOF
ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
[ -d "$ANDROID_HOME/platforms/android-34" ] || { echo "ERROR: SDK platform android-34 missing at $ANDROID_HOME"; exit 1; }
printf 'sdk.dir=%s\n' "$ANDROID_HOME" > "$DIR/local.properties"
printf 'android.aapt2FromMavenOverride=%s\n' "$PREFIX/bin/aapt2" > "$DIR/gradle.properties"
echo "CREATED: $DIR"
echo "BUILD: cd $DIR && gradle --no-daemon assembleDebug"
echo "NOTE: Generated APK is a debug build; test on your device."
