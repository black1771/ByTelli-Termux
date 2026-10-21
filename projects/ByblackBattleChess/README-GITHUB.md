# Byblack Battle Chess — Android / Termux

> **Kaynak durumu:** Bu depo paketi, eldeki V131 oyun motoru ve V132 GameplayPatch Java dosyalarından **yeniden oluşturulmuş V132 kaynak adayıdır**. V133 menü düzenlemesi dahil değildir. Bu ortamda APK derlemesi veya telefonda çalışma testi doğrulanmamıştır.

## Proje
Android için Java + OpenGL ES 2.0 tabanlı 3B satranç ve taş alma sahneleri. Uygulama paket adı: `com.byblack.battlechess`. Arayüzde yeni oyun, iki kişilik, CPU kolay/orta/zor, kayıt ve yükleme bulunur. Kaynak içindeki `Arena3D.java` 3B sahneyi yönetir.

## İlham / referans
Eski Pocket PC/Windows Mobile döneminin animasyonlu satranç oyunları, kullanıcının gösterdiği Chesscapade/CAB görüntüleri ve yakın plan taş alma animasyonu fikirleri **görsel/tasarım referansıdır**. Orijinal CAB oyun kodu, karakterleri veya dosyalarının kopyalandığı doğrulanmamıştır. Orijinal oyunla resmi bağlantı iddia edilmez.

## Kullanılan varlıklar ve kaynak geçmişi
- `KnightCharacter.fbx`: geçmiş geliştirme aşamasında analiz edilen bir model; 12 animasyon yığını görüldü. **Bu pakete dahil değil.** Kaynak yaratıcısı/lisansı doğrulanmadı.
- `Bestiary - Dungeon Monsters Kit[Standard].zip`: Imp ve Puglin modelleri geçmişte değerlendirilmek üzere sağlandı; **bu pakete dahil değil ve oyunda kullanıldıkları doğrulanmadı**. Yaratıcı ve lisans arşivden doğrulanmalı.
- `Arena3D.java` kaynak notu: içe aktarılan FBX iskeleti bu sahnede kullanılmıyor; hareketler prosedürel.
- Kenney gibi CC0 varlık sağlayıcıları alternatif olarak incelenebilir; **Kenney varlıklarının mevcut oyunda kullanıldığı iddia edilmez**. Kaynak: https://kenney.nl/support
- Üçüncü taraf varlıkların lisansı doğrulanmadan repoya eklenmemelidir.

## Gereksinimler
Android üzerinde ARM64 Termux; OpenJDK 21, Gradle, Android SDK platform 34 / build-tools, ARM64 üzerinde çalışan `aapt2`; internet bağlantısı ilk bağımlılık indirmelerinde gerekir. Android Gradle Plugin 8.7.3 ve Gradle sürüm uyumu kontrol edilmelidir.

## Tek komutla yerel kurulum
1. `ByblackBattleChess-Source-V132.zip` dosyasını telefonun `Download` klasörüne koyun.
2. `ByblackBattleChess-TERMUX-KUR.sh` dosyasını aynı klasöre koyun.
3. Termux'ta: `bash /sdcard/Download/ByblackBattleChess-TERMUX-KUR.sh`

Script mevcut Java/SDK/aapt2'yi kontrol eder, kaynakları açar, Gradle ile derlemeyi dener ve başarılı olursa APK'yı Download'a kopyalar. Eksik SDK'yı sıfırdan otomatik indirmez; hatayı açıkça gösterir. İlk Gradle bağımlılık indirmesi internet gerektirebilir.

## Bilinen sınırlamalar
- V133 henüz doğrulanmış bir sürüm değil.
- Derleme/oynanış testi bu teslimatta yapılmadı.
- Satranç kuralları, yapay zekâ, kayıt/yükleme ve animasyonların tüm uç durumları test edilmedi.
- Video `demo/ByblackBattleChess-Demo.mp4` içindedir; kaynak kanıtı yerine oynanış örneğidir.

## Lisans
Bu kaynak paketi için telif sahibi tarafından açık lisans seçilmemiştir. Bu nedenle **MIT/GPL/CC0 lisanslı olduğu iddia edilmez**. Açık kaynak olarak yayımlamadan önce kod sahibi LICENSE dosyası eklemelidir. Üçüncü taraf karakterler ayrıca lisans denetimine tabidir.


## Geliştirme ve hata geçmişi
Sürüm bazında doğrulanabilen hatalar, düzeltmeler ve sayım sınırları için [GELISTIRME-HATA-TEST-GECMISI.md](GELISTIRME-HATA-TEST-GECMISI.md) dosyasına bakın. Kurulum/kaldırma toplamları kayıtlarla doğrulanamadığından verilmemiştir.
