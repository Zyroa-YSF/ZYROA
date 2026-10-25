# Skin Gold Market

Çevrimdışı çalışan, Kotlin + Jetpack Compose ile yazılmış Android kasa açma / koleksiyon / pazar / takas oyunu.
Gold yalnızca oyun içi sanal paradır. Pazar ve takastaki oyuncular **demo profilleridir**.

## APK'yı almak (kod yazmadan)
1. github.com'da yeni bir **boş depo** (repository) oluştur.
2. Bu klasörün **içindekileri** (app, .github, build.gradle.kts, settings.gradle.kts ...) depoya yükle:
   "uploading an existing file" bağlantısı → dosya ve klasörleri sürükle → **Commit changes**.
3. Depoda **Actions** sekmesine gir; "APK Derle" çalışmasının bitmesini bekle (3-6 dk).
4. APK iki yerden indirilir:
   - Sağ taraftaki **Releases** bölümü → `app-debug.apk` (telefondan en kolayı)
   - Actions → çalışma → **Artifacts** → `SkinGoldMarket-APK`
5. Telefonda APK'ya dokun; "bilinmeyen kaynaklardan yükleme" iznini ver ve kur.

`.github` klasörü sürükle-bırak ile gelmezse: Add file → Create new file → ad kısmına
`.github/workflows/build.yml` yaz ve dosyanın içeriğini yapıştır.

## Yerelde derleme
Android Studio ile aç ve Run'a bas; veya JDK 17 + Gradle 8.7 ile `gradle assembleDebug`.

## Mimari
- `Catalog.kt` : 10 kasa, 120 kaplama, nadirlik olasılıkları, değer hesabı (statik, deterministik)
- `GameDb.kt`  : SQLite; tüm Gold/envanter işlemleri tek transaction (hata olursa geri alınır)
- `GameVM.kt`  : işlemleri tek iş parçacığında sıralar, arayüz donmaz
- `Art.kt`     : kaplama ve kasa görselleri Canvas ile çizilir (harici resim yok)
- `Screens.kt` / `Ui.kt` : Compose arayüzü
- İleride çevrimiçi sürüm için `GameDb` işlemleri bir sunucu API'sinin arkasına alınabilir.
