# Android APK Test Reports / Android APK Test Raporları

This log records **observed** CI smoke-test results and failures. Do not interpret a passing smoke test as proof of complete feature compatibility. GitHub Actions keeps the original logs linked below.

Bu kayıt, gözlemlenen test sonuçlarını ve hataları içerir. Başarılı temel test, tüm uygulama özelliklerinin çalıştığını garanti etmez.

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
