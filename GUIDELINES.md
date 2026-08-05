ZYROA — PROJE YÖNERGESİ (GUIDELINES)

Amaç
- "Bad Day Insomnia" prototipini Godot 4 + GDScript ile geliştirmek.
- Hedef: gerçekçi "indie korku" atmosferi; platformlar: PC (Windows/macOS/Linux) + Android + iOS.
- Oyun ücretsiz dağıtılacak (ticari amaç yok).

Varlık (asset) lisans ve seçim kuralları
1) Tercih sırası ve izinler:
   - Öncelik: CC0 (public domain) ve açık lisanslı (ticari kullanım izinli) varlıklar.
   - Kabul edilebilir: CC-BY (atıf ile kullanım) ve CC-BY-NC (sadece ticari olmayan kullanımlar) — projenin ücretsiz olması sebebiyle CC-BY-NC modeller kullanılabilir.
   - Tercih edilmeyecek: Satın alma veya özel lisans gerektiren ("for editorial use only" ya da "no-redistribution") varlıklar.
2) Kaynaklar:
   - 3D modeller: Sketchfab (lisans filtresiyle), Poly Pizza
   - Sesler: Freesound, FreeSoundEffects, OpenGameArt
   - Müzik: Free Music Archive, Incompetech (Incompetech CC-BY gerektirir, atıf ekle)
3) Atıf ve izleme:
   - Her varlık assets/ altında uygun klasöre kopyalanacak (assets/models/, assets/sounds/, assets/music/).
   - assets/ATTRIBUTIONS.md dosyası her varlık için şu formatta satır içerecek:
     - assets/models/elevator_door.glb — "Elevator Door" by SomeAuthor — https://sketchfab.com/models/xxxx — License (e.g., CC0)
   - Eğer bir varlık CC-BY veya CC-BY-NC ise, atıf metni oy içinde veya README/ATTRIBUTIONS dosyasında açıkça yer alacaktır.

Çeviriler / Lokalizasyon
- Diller: Türkçe (TR), İngilizce (EN), Rusça (RU), Çince (ZH), Portekizce (PT), İspanyolca (ES)
- Form: Godot .po dosyaları (i18n/*.po). Master kaynak EN veya TR olabilir; sonraki çeviriler .po dosyalarına eklenecek.
- Akış: Metinleri önce TR (orijinal) ve EN'ye çevirip, otomatik çeviri taslakları ile RU/ZH/PT/ES oluşturulur; release öncesi insan düzeltmesi gereklidir.

Geliştirme iş akışı
- Tüm geliştirme feature/proto-bad-day-insomnia dalında yapılacak. Ana dâle merge işlemi, yeterli kalite ve testten sonra PR ile yapılır.
- Her büyük görev için bir Issue oluşturulacak ve küçük parçalara bölünecek (ör: asansör mekaniği, tünel el feneri, koridor cutscene, market etkileşimi, final cutscene).
- Commit mesajları kısa ve açıklayıcı olmalı: "feat: add elevator scene and transition" veya "fix: flashlight battery drain".

Branching ve sürüm yönetimi
- Branch: feature/<isim> şeklinde yeni özellikler için dallan.
- Merge: PR ile ana dâle (özellikle test sonrası).
- Tagging: her kararlı sürüm için semver kullan (v0.1.0, v0.2.0 ...).

Platforma özel notlar
- Android: Godot export preset eklenecek; Android SDK/NDK ve Godot export templates gereklidir.
- iOS: Apple Developer hesabı, sertifikalar ve provisioning profile gereklidir; bu bilgiler geliştirici/owner tarafından sağlanmalı.

Kalite hedefleri ve oyun hissi
- Realistic indie-horror: ortam sesleri, ışık/karanlık kontrastı, düşük ışık materyalleri, az ama etkili jump-scare kullanımı.
- Performans: hedef 30–60 FPS mobil ve PC; büyük modellerin poly count'u optimize edilecek.

Veri gizliliği ve telemetri
- Proje sahibi isteği doğrultusunda; bu prototipte herhangi bir crash reporting veya otomatik hata gönderimi olmayacak.

Test ve QA
- Her sahne için minimal test adımları yazılacak (ör: asansöre bin, tünele in, el fenerini aç, adım sesini dinle — monster görünmüyor).
- Mobil testler: Android cihaz üzerinde dokunmatik kontroller doğrulanacak; iOS testleri için TestFlight yönergeleri.

Yapılacak ilk işler (kısa)
1) assets/ içindeki gerekli modelleri Sketchfab/PolyPizza'dan lisans kontrolü yaparak indirip ekle (asansör kapısı, tünel duvarları, el feneri objesi, kaset objesi, market rafları, banka bankı vs.).
2) Freesound'dan koridor ambiyansı, ayak sesleri, uzak uğultu gibi SFXleri topla.
3) main.tscn ile asansör->tünel->koridor akışının çalışır bir prototipini yap.
4) i18n .po dosyalarını gözden geçir ve otomatik çeviri taslaklarını düzelt.

İletişim ve onaylar
- Her adımda değişiklikleri feature/proto-bad-day-insomnia dalına commitleyeceğim.
- İstersen her büyük adımda (ör: asset toplama tamamlandığında) bir PR isteği açıp onayını bekleyebilirim.

