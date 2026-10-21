# ByblackBattleChess — Geliştirme, hata ve test geçmişi

## Sayım yöntemi ve sınırlar
Sürüm numarası kurulum sayısı değildir. APK dosyasının oluşturulması, telefona kurulması, kaldırılması ve oyunun test edilmesi ayrı olaylardır. Mevcut konuşma, dosya ve derleme kayıtları tüm olayları kapsamadığı için **kesin kurulum, kaldırma, başarısız derleme, hata ve video sayıları bilinmiyor**. Bu rakamlar tahmin edilerek doldurulmadı.

## Doğrulanabilen kilometre taşları
| Sürüm | Kayıt / sonuç |
|---|---|
| V0.2 | Telefonda açıldı; otomatik sıfırlanma ve orijinal CAB oyununa benzememe bildirildi. |
| V6 | 19 KB APK raporlandı. |
| V7 | `Çalışan Gradle projesi bulunamadı` kurulum betiği hatası. |
| V8 | Gradle derlemesi başarılı; 17 KB APK. |
| V13 | Kamera ve seçili taş durumu için düzeltme betiği. |
| V17 | Kamera, karo ve raycast düzeltme betiği. |
| V32 | 3B model dönüş, ölçek ve konum düzeltme betiği. |
| V34-FIX2 | Boştaki taşların hareket etmesi düzeltildi; karakter görünümü yeterli değildi. |
| V36 | `warrior(...)` metodunda `role` iki kez tanımlandığı için Java derleme hatası; 3 uyarı. |
| FINAL-TEST | Java 26 / Gradle 9.6 ile başarılı APK derlemesi; önceki `ProtoLogNative` sorunları raporlandı. |
| V102 | 16 MB APK derlendi; video incelemesinde kamera ve karakter animasyonları hedefe ulaşmadı. |
| V129–V132 | Görüntü, kamera, oyun motoru ve oynanış üzerinde ardışık değişiklikler. V130 ve V132 derlemeleri başarılı. |
| V133 | Menü güncellemesi hazırlandı; telefonda kurulum ve test doğrulanmadı. |

## Gözlenen sorun grupları
- **Derleme ve ortam:** Yanlış proje yolu, Java/Gradle uyumsuzluğu, `ProtoLogNative`, Java değişken çakışması.
- **Kamera ve sahne:** İstenen yandan görünüş/zoomun tutmaması, karo ve raycast ayarları.
- **Karakterler ve animasyon:** Taşların yere düşmesi, kaybolması, kendiliğinden hareket etmesi; saldırı/savaş animasyonunun CAB örneğine benzememesi.
- **Oyun davranışı:** V0.2 otomatik sıfırlanma ve çeşitli oynanış iyileştirmeleri.

## Kesin sayılar neden verilemiyor?
Eski APK envanterlerinde V2–V103 arası pek çok dosya var; bu dosyalar birden çok kez kurulmuş veya hiç kurulmamış olabilir. Bazı eski APK'lar başka klasöre taşındı; bu uygulama kaldırma kaydı değildir. Tüm Gradle logları, Android paket yöneticisi kayıtları ve test videoları tek bir doğrulanmış arşivde olmadığı için toplamları hesaplamak mümkün değil.

## İleride kesin sayım için
Her derlemede zaman, sürüm, başarılı/başarısız durum, log yolu ve APK SHA256; her telefon testinde kurulum/kaldırma olayı, cihaz sürümü, video ve gözlenen sorun ayrı CSV/Markdown günlüklerine yazılmalıdır.
