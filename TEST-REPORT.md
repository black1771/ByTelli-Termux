# Android APK Test Reports / Android APK Test Raporları

This log records **observed** CI smoke-test results and failures. Do not interpret a passing smoke test as proof of complete feature compatibility. GitHub Actions keeps the original logs linked below.

Bu kayıt, gözlemlenen test sonuçlarını ve hataları içerir. Başarılı temel test, tüm uygulama özelliklerinin çalıştığını garanti etmez.

## Build environment versus Android runtime / Derleme ortamı ve Android sürümü

**Important:** The Android version under test is the **emulator runtime**, not the operating system on which Gradle or Java runs.

| Component / Bileşen | GitHub Actions Android 10–15 smoke tests |
| --- | --- |
| Build host / Derleme sunucusu | GitHub-hosted Ubuntu Linux |
| Build Java / Derleme Java | Temurin JDK **21** |
| Build Gradle | **9.6.0** |
| Android SDK platform used by example / Örnek projenin derleme SDK'sı | **34** |
| Emulator Android / Emülatör Android | **API 29, 30, 31, 33, 34, 35** (tested in separate runs) |
| Emulator architecture / Mimari | **x86_64** |
| App under test / Test uygulaması | `examples/ByTelliDeveloperLabV1` debug APK |

**Java 26 was not used in these GitHub emulator jobs.** A separate real-phone **Android 16 / ARM64 Termux** build was performed with **Java 26.0.2 and Gradle 9.6.0**; that does not establish Java 26 compatibility on Android 10 or 11. The APK is compiled on Ubuntu and then installed into each Android emulator.

**Türkçe:** GitHub'daki Android 10–15 testlerinde Java 21 ve Gradle 9.6 kullanılıyor. Java 26 + Gradle 9.6 testi ayrı olarak Android 16 ARM64 Termux telefonunda yapıldı. Android 10 emülatöründe Java 26 çalıştırıldığı anlamına gelmez.

## 2026-10-15 — Android 13, 14, 15 (API 33, 34, 35)

**Result / Sonuç:** PASS on all three emulator jobs.

**Evidence / Kanıt:** https://github.com/black1771/ByTelli-Termux/actions/runs/38709534580

**Scope / Kapsam:** Debug APK compiled from `examples/ByTelliDeveloperLabV1`, installed on GitHub-hosted x86_64 Android emulator, launched and process-alive checked.

## 2026-10-15 — Android 10, 11, 12 (API 29, 30, 31)

**Evidence / Kanıt:** https://github.com/black1771/ByTelli-Termux/actions/runs/38712216857

| Android | API | Observed result / Gözlemlenen sonuç |
| --- | --- | --- |
| 10 | 29 | PASS |
| 11 | 30 | FAIL — emulator setup, before APK smoke test |
| 12 | 31 | PASS |

### Android 11 failure details / Android 11 hata ayrıntıları

- **Observed error:** `java.lang.IllegalStateException: android.os.ServiceManager$ServiceNotFoundException: No service published for: input`
- **Where:** Emulator runner attempted `adb shell input keyevent 82` after reporting emulator boot; the Android `input` service was not available.
- **Interpretation:** CI emulator initialization failure; **not evidence of an app crash or Android 11 app incompatibility**.
- **Follow-up:** Commit `62c90cf` changed the manual test matrix to Android 11 only (API 30) and disabled the runner's animation-disabling commands. **Retest outcome pending; do not mark Android 11 as passed until its job succeeds.**

## How to add a new result / Yeni sonuç ekleme

For each test, record: date, Android/API version, pass/fail, GitHub Actions run link, exact failing step and error text, likely cause (clearly labeled as hypothesis if unconfirmed), fix commit, and retest result.

**Note:** GitHub Actions automatically stores logs, but this Markdown report is **maintained manually**; it does not update itself after new runs.

**Not:** GitHub Actions logları otomatik saklar, ancak bu Markdown raporu **elle güncellenir**; yeni testlerden sonra kendiliğinden değişmez.

## 2026-10-15 — Android 11 final retest: PASS

GitHub Actions run: https://github.com/black1771/ByTelli-Termux/actions/runs/38725970949

Verified: APK build PASS; APK install on Android 11 (API 30) emulator PASS; launch and app-process verification PASS.

Earlier failures were CI environment problems, including missing KVM permissions. Commit `c8d3d7e` added KVM access. This is a basic smoke test, not comprehensive feature compatibility.

Android 10 and 12 passed: https://github.com/black1771/ByTelli-Termux/actions/runs/38712216857
Android 13, 14 and 15 passed: https://github.com/black1771/ByTelli-Termux/actions/runs/38709534580
