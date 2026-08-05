# ZYROA — BAD DAY INSOMNIA (Prototype)

Kısa: Godot (GDScript) ile yapılacak gerçekçi indie-korku prototipi.

Özellikler:
- Motor: Godot 4 (GDScript)
- Platformlar: PC (Windows/macOS/Linux), Android, iOS
- Çok dilli altyazı/çeviri: otomatik çeviri taslakları eklendi; daha sonra insan çevirileriyle değiştirilecek.
- Varlıklar: Sketchfab ve Poly Pizza öncelikli; Freesound vb. ücretsiz ses kaynakları.

Notlar:
- Proje sahibi isteğiyle bu prototipte crash reporting / hata gönderimi bulunmayacak. scripts/crash_reporter.gd dosyası devre dışı bırakıldı.
- Proje ücretsiz olarak dağıtılacak ve ticari amaç gütmeyecek; varlık seçimi buna göre (ücretsiz/royalty-free/CC-BY/CC0/CC-BY-NC gibi lisanslar) yapılacaktır.

Proje yapısı (kısaca):
- scenes/ — .tscn sahne dosyaları
- scripts/ — GDScript dosyaları
- assets/ — modeller, ses, müzik, atıf dosyası
- i18n/ — .po çeviri dosyaları
- GUIDELINES.md — proje yönergesi ve varlık lisans kuralları

Bir sonraki adım olarak varlık toplama (Sketchfab/PolyPizza/Freesound) ve sahne implementasyonuna başlayacağım; varlıklar assets/ altında toplanacak ve assets/ATTRIBUTIONS.md dosyası güncellenecektir.
