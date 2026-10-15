# ByBlack Calculator V2

Scientific Android calculator built with Java, Android Gradle Plugin 8.7.3 and Android SDK 35.

Features: dark UI, DEG/RAD trigonometry, square root, powers, logarithms, constants, percentages, saved calculation history and answer recall.

![Real Android screenshot](../../Screenshot_20261015_165133.jpg)

## Build / Derleme

Prerequisites: Java 17+ (compatible with your Gradle setup), Android SDK Platform 35, Android SDK build tools and Gradle 8.9+ or a compatible wrapper.

From this directory, set `ANDROID_HOME` to your installed SDK, then run:

```sh
gradle --no-daemon assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

### Termux ARM64

The original phone used a Termux-native AAPT2. The original `gradle.properties` contained an absolute path specific to that phone, so it has been removed from the public source. If Maven's Linux AAPT2 cannot run on your ARM64 Termux, find your native AAPT2 and add this line to your **local** `gradle.properties`:

```properties
android.aapt2FromMavenOverride=/your/actual/path/to/aapt2
```

Also create a local `local.properties` with `sdk.dir=/your/actual/android-sdk` if required.

**Türkçe:** Android SDK 35, Java, Gradle ve ARM64 uyumlu AAPT2 gerekir. SDK ve AAPT2 yollarını kendi cihazına göre ayarla. Bu kaynak koddan üretilen APK test/debug sürümüdür.

**Note:** This is the original V2 app source supplied by the author, excluding the V1 backup and machine-specific paths. No new full build was run on GitHub after publication.


## Complete source code / Tam kaynak kod

All project files are reproduced below so they can be viewed and copied directly from this page. For compiling, keep each file at the path shown in its heading.

**Türkçe:** Aşağıda bütün proje dosyaları, tam içerikleriyle yer alıyor. Derlemek için kodları belirtilen dosya yollarına ayrı ayrı kaydedin. Üstteki GitHub klasöründen dosyaları doğrudan da açabilirsiniz.

### `app/src/main/java/com/byblack/calculator/MainActivity.java`

```java
package com.byblack.calculator;

import android.app.*;
import android.os.*;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import android.content.SharedPreferences;
import java.util.*;

public class MainActivity extends Activity {
  LinearLayout root, historyPanel; TextView display, expression, modeLabel; String input="", lastAnswer="0"; boolean degrees=true, showingHistory=false;
  final int BG=0xff090b10, PANEL=0xff141923, KEY=0xff1d2430, ACCENT=0xff75e0c4, TEXT=0xfff4f7fb, MUTED=0xff9daabd;
  final ArrayList<String> history=new ArrayList<>();
  int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
  GradientDrawable shape(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g;}
  TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(size);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);getWindow().getDecorView().setSystemUiVisibility(0);loadHistory();build();}
  void build(){
    root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(16),dp(12),dp(16),dp(12));root.setBackgroundColor(BG);setContentView(root);
    LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);root.addView(top,new LinearLayout.LayoutParams(-1,dp(52)));
    TextView brand=text("ByBlack",23,TEXT);brand.setTypeface(null,Typeface.BOLD);top.addView(brand,new LinearLayout.LayoutParams(0,-1,1));
    TextView hist=text("◷  History",14,ACCENT);hist.setGravity(Gravity.CENTER);hist.setBackground(shape(PANEL,14));hist.setPadding(dp(12),0,dp(12),0);top.addView(hist,new LinearLayout.LayoutParams(-2,dp(40)));hist.setOnClickListener(v->toggleHistory());
    modeLabel=text("DEG  •  SCIENTIFIC",12,MUTED);modeLabel.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);root.addView(modeLabel,new LinearLayout.LayoutParams(-1,dp(34)));
    expression=text("0",26,MUTED);expression.setGravity(Gravity.RIGHT|Gravity.BOTTOM);expression.setSingleLine(true);expression.setEllipsize(android.text.TextUtils.TruncateAt.START);root.addView(expression,new LinearLayout.LayoutParams(-1,dp(65)));
    display=text("0",42,TEXT);display.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);display.setTypeface(Typeface.DEFAULT,Typeface.BOLD);display.setSingleLine(true);display.setAutoSizeTextTypeUniformWithConfiguration(18,46,2,1);root.addView(display,new LinearLayout.LayoutParams(-1,dp(92)));
    historyPanel=new LinearLayout(this);historyPanel.setOrientation(1);historyPanel.setBackground(shape(PANEL,16));historyPanel.setPadding(dp(12),dp(8),dp(12),dp(8));historyPanel.setVisibility(View.GONE);root.addView(historyPanel,new LinearLayout.LayoutParams(-1,0,3));
    String[][] rows={{"DEG","sin","cos","tan","⌫"},{"√","x²","ln","log","C"},{"(",")","%","÷","×"},{"7","8","9","−","+"},{"4","5","6","π","e"},{"1","2","3","ANS","="},{"±","0",".","^","AC"}};
    for(String[] row:rows){LinearLayout line=new LinearLayout(this);line.setOrientation(0);root.addView(line,new LinearLayout.LayoutParams(-1,0,1));for(String key:row){TextView button=text(key,key.length()>2?15:20,key.equals("=")?BG:((key.equals("C")||key.equals("AC")||key.equals("⌫"))?0xffffb4a9:TEXT));button.setGravity(Gravity.CENTER);button.setTypeface(null,Typeface.BOLD);button.setBackground(shape(key.equals("=")?ACCENT:("÷×−+^".contains(key)&&key.length()==1?0xff263d43:KEY),13));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1);p.setMargins(dp(3),dp(3),dp(3),dp(3));line.addView(button,p);button.setOnClickListener(v->press(key));}}
    update();
  }
  void loadHistory(){
    SharedPreferences p=getSharedPreferences("byblack_history",MODE_PRIVATE);
    lastAnswer=p.getString("answer","0");String raw=p.getString("items","");
    if(!raw.isEmpty())for(String line:raw.split(String.valueOf((char)10)))if(!line.isEmpty())history.add(line);
  }
  void saveHistory(){
    StringBuilder b=new StringBuilder();for(String line:history)b.append(line).append((char)10);
    getSharedPreferences("byblack_history",MODE_PRIVATE).edit().putString("items",b.toString()).putString("answer",lastAnswer).apply();
  }
  void toggleHistory(){
    showingHistory=!showingHistory;historyPanel.setVisibility(showingHistory?View.VISIBLE:View.GONE);
    if(showingHistory){historyPanel.removeAllViews();TextView title=text("RECENT CALCULATIONS",13,ACCENT);historyPanel.addView(title);ScrollView scroll=new ScrollView(this);LinearLayout list=new LinearLayout(this);list.setOrientation(1);scroll.addView(list);historyPanel.addView(scroll);for(int i=history.size()-1;i>=0;i--){String item=history.get(i);TextView t=text(item,16,TEXT);t.setPadding(dp(8),dp(12),dp(8),dp(12));list.addView(t);t.setOnClickListener(v->{input=item.substring(item.lastIndexOf('=')+1).trim();toggleHistory();update();});}if(history.isEmpty())list.addView(text("No calculations yet",16,MUTED));}
  }
  void press(String k){try{
    switch(k){
      case "C":case "AC":input="";break;
      case "⌫":if(!input.isEmpty())input=input.substring(0,input.length()-1);break;
      case "DEG":degrees=!degrees;modeLabel.setText((degrees?"DEG":"RAD")+"  •  SCIENTIFIC");break;
      case "=":if(input.isEmpty())return;double value=new Parser(input,degrees).parse();String result=fmt(value);history.add(input+" = "+result);if(history.size()>40)history.remove(0);lastAnswer=result;input=result;saveHistory();break;
      case "±":if(input.isEmpty())input="-";else input="-("+input+")";break;
      case "x²":input+="^2";break;case "√":input+="sqrt(";break;
      case "sin":case "cos":case "tan":case "ln":case "log":input+=k+"(";break;
      case "ANS":input+=lastAnswer;break;case "π":input+="π";break;
      case "÷":input+="/";break;case "×":input+="*";break;case "−":input+="-";break;
      case "%":input+="%";break;default:input+=k;
    }update();
  }catch(Exception e){display.setText("Error");expression.setText("Check expression");}}
  String fmt(double x){if(Double.isNaN(x)||Double.isInfinite(x))throw new ArithmeticException();if(Math.abs(x)<1e-12)x=0;return java.math.BigDecimal.valueOf(x).stripTrailingZeros().toPlainString();}
  void update(){expression.setText(input.isEmpty()?"0":input.replace('*','×').replace('/','÷'));if(input.isEmpty()){display.setText("0");return;}try{double v=new Parser(input,degrees).parse();display.setText(fmt(v));}catch(Exception ex){display.setText("…");}}
  static class Parser{
    final String s;final boolean deg;int pos=0;
    Parser(String s,boolean deg){this.s=s;this.deg=deg;}
    void ws(){while(pos<s.length()&&Character.isWhitespace(s.charAt(pos)))pos++;}
    boolean eat(char c){ws();if(pos<s.length()&&s.charAt(pos)==c){pos++;return true;}return false;}
    double parse(){double v=expr();ws();if(pos!=s.length())throw new IllegalArgumentException();return v;}
    double expr(){double v=term();while(true){if(eat('+'))v+=term();else if(eat('-'))v-=term();else return v;}}
    double term(){double v=unary();while(true){if(eat('*'))v*=unary();else if(eat('/')){double d=unary();if(d==0)throw new ArithmeticException();v/=d;}else return v;}}
    double unary(){if(eat('+'))return unary();if(eat('-'))return -unary();return power();}
    double power(){double v=postfix();if(eat('^'))v=Math.pow(v,unary());return v;}
    double postfix(){double v=atom();while(eat('%'))v/=100;return v;}
    double atom(){
      if(eat('(')){double v=expr();if(!eat(')'))throw new IllegalArgumentException();return v;}
      if(eat('π'))return Math.PI;if(pos<s.length()&&s.charAt(pos)=='e'){pos++;return Math.E;}
      ws();int start=pos;while(pos<s.length()&&Character.isLetter(s.charAt(pos)))pos++;
      if(pos>start){String f=s.substring(start,pos);if(!eat('('))throw new IllegalArgumentException();double v=expr();if(!eat(')'))throw new IllegalArgumentException();switch(f){case "sin":return Math.sin(deg?Math.toRadians(v):v);case "cos":return Math.cos(deg?Math.toRadians(v):v);case "tan":return Math.tan(deg?Math.toRadians(v):v);case "sqrt":return Math.sqrt(v);case "ln":return Math.log(v);case "log":return Math.log10(v);default:throw new IllegalArgumentException();}}
      start=pos;boolean dot=false;while(pos<s.length()){char c=s.charAt(pos);if(Character.isDigit(c))pos++;else if(c=='.'&&!dot){dot=true;pos++;}else break;}
      if(start==pos)throw new IllegalArgumentException();return Double.parseDouble(s.substring(start,pos));
    }
  }
}
```

### `app/src/main/AndroidManifest.xml`

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"><application android:icon="@drawable/byblack_icon" android:label="ByBlack Calculator" android:theme="@android:style/Theme.Material.NoActionBar" android:usesCleartextTraffic="false"><activity android:name=".MainActivity" android:exported="true"><intent-filter><action android:name="android.intent.action.MAIN"/><category android:name="android.intent.category.LAUNCHER"/></intent-filter></activity></application></manifest>
```

### `app/build.gradle`

```groovy
plugins { id 'com.android.application' }
android { namespace 'com.byblack.calculator'; compileSdk 35
 defaultConfig { applicationId 'com.byblack.calculator'; minSdk 26; targetSdk 28; versionCode 2; versionName '2.0' }
}
```

### `build.gradle`

```groovy
plugins { id 'com.android.application' version '8.7.3' apply false }
```

### `settings.gradle`

```groovy
pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral() } }
rootProject.name='ByBlackCalculator'
include ':app'
```

### `gradle.properties`

```properties
org.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8
org.gradle.daemon=false
android.useAndroidX=true
```

### `app/src/main/res/drawable/byblack_icon.xml`

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
 android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
 <path android:fillColor="#090B10" android:pathData="M0,0h108v108h-108z"/>
 <path android:fillColor="#75E0C4" android:pathData="M31,21h24c18,0 25,10 25,20c0,7 -4,12 -10,15c8,3 12,9 12,17c0,14 -11,22 -28,22H31zM45,34v17h10c7,0 11,-3 11,-9c0,-5 -4,-8 -11,-8zM45,63v19h11c8,0 12,-3 12,-10c0,-6 -4,-9 -12,-9z"/>
</vector>
```
