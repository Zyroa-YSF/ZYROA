# ZYROA — BAD DAY INSOMNIA (Prototype)

Kısa: Godot (GDScript) ile yapılacak çokdilli (EN/TR/RU/ZH/PT/ES) hayatta kalma/korku prototipi.

Özellikler:
- Motor: Godot 4 (GDScript)
- Platformlar: PC (Windows/macOS/Linux), Android, iOS
- Çok dilli altyazı/çeviri: otomatik çeviri taslakları eklendi; daha sonra insan çevirileriyle değiştirilecek.
- Varlıklar: İlk aşamada placeholder ve ücretsiz/royalty-free varlıklar kullanılacak. Atıf/izin dosyaları assets/ATTRIBUTIONS.md içinde tutulacak.

Bu dal: feature/proto-bad-day-insomnia

Not: Proje sahibinin isteğiyle bu repo için explicit bir lisans dosyası eklenmedi.

İlk eklenenler:
- Basit proje iskeleti (project.godot, .gitignore)
- Sahne ve script taslakları (scenes/, scripts/)
- i18n çeviri dosyaları (i18n/)
- Export preset örneği (Android ve iOS) — kullanıcı ayarları gerektiren yerler yorumlandı.

İleri adımlar (yapacağım):
1) Placeholder varlıkları (3D/2D/ses) toplayıp assets/ altında ekleyeceğim ve atıf/izinleri not edeceğim.
2) Temel oynanış mekanikleri: karakter hareketi, el feneri ışığı, cutscene triggerları, NPC davranışları.
3) Lokalleştirme geliştirmesi: otomatik çeviri taslaklarını gözden geçirip insan çevirilerine hazırlama.
4) Android/iOS için export testleri ve README'e kurulum/derleme talimatları ekleme.

