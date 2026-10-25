package com.skingold.market

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

enum class Rarity(val tr: String, val color: Color, val base: Int) {
    SIRADAN("Sıradan", Color(0xFF9AA4B2), 20),
    SIRA_DISI("Sıra dışı", Color(0xFF2ECC8F), 55),
    NADIR("Nadir", Color(0xFF2EA8FF), 140),
    DESTANSI("Destansı", Color(0xFFA55BFF), 380),
    EFSANEVI("Efsanevi", Color(0xFFFF4545), 1100),
    GIZLI("Gizli", Color(0xFFFF4FD8), 3200),
    OZEL("Özel Seri", Color(0xFFFFC93C), 9000)
}

enum class Category(val tr: String) {
    TABANCA("Tabanca"), SMG("Hafif Makineli"), TUFEK("Tüfek"), KESKIN("Keskin Nişancı"),
    AGIR("Ağır Silah"), BICAK("Bıçak"), ELDIVEN("Eldiven")
}

data class Weapon(val name: String, val cat: Category)

data class Crate(
    val id: String, val idx: Int, val name: String, val price: Int, val collection: String,
    val c1: Color, val c2: Color, val theme: String
)

data class Skin(
    val id: String, val weapon: Weapon, val skinName: String, val rarity: Rarity,
    val crateIdx: Int, val slot: Int, val value: Int
) {
    val fullName: String get() = "${weapon.name} | $skinName"
    val pattern: Int get() = (slot * 7 + crateIdx * 3) % 10
    val collection: String get() = Catalog.crates[crateIdx].collection
}

data class QuestDef(val id: String, val title: String, val target: Int, val reward: Int)
data class AchDef(val id: String, val title: String, val desc: String, val reward: Int)

fun levelFor(xp: Int): Int = sqrt(xp / 50.0).toInt() + 1
fun xpForLevel(level: Int): Int = 50 * (level - 1) * (level - 1)

object Catalog {
    private val weapons = listOf(
        Weapon("Vektör P9", Category.TABANCA), Weapon("Kor X2", Category.TABANCA),
        Weapon("Sessiz 7", Category.TABANCA), Weapon("Çakmak M1", Category.TABANCA),
        Weapon("Şahin Mk3", Category.TABANCA),
        Weapon("Karınca SMG", Category.SMG), Weapon("Hızır 45", Category.SMG),
        Weapon("Fırtına PDW", Category.SMG), Weapon("Sivri-9", Category.SMG),
        Weapon("Kartal AR", Category.TUFEK), Weapon("Bozkurt 47", Category.TUFEK),
        Weapon("Yıldırım R5", Category.TUFEK), Weapon("Atlas BR", Category.TUFEK),
        Weapon("Gözcü S1", Category.KESKIN), Weapon("Kuğu 338", Category.KESKIN),
        Weapon("Menzil X", Category.KESKIN),
        Weapon("Dev 12", Category.AGIR), Weapon("Mancınık H", Category.AGIR),
        Weapon("Tufan LMG", Category.AGIR)
    )
    private val knives = listOf(
        Weapon("Hançer", Category.BICAK), Weapon("Pala", Category.BICAK), Weapon("Kama", Category.BICAK),
        Weapon("Pençe", Category.BICAK), Weapon("Kılıç", Category.BICAK)
    )
    private val gloves = listOf(
        Weapon("Boksör Eldiveni", Category.ELDIVEN), Weapon("Sürücü Eldiveni", Category.ELDIVEN),
        Weapon("Avcı Eldiveni", Category.ELDIVEN), Weapon("Taktik Eldiven", Category.ELDIVEN)
    )

    // Her kasada 12 kaplama: 3 Sıradan, 2 Sıra dışı, 2 Nadir, 2 Destansı, 1 Efsanevi, 1 Gizli, 1 Özel Seri
    private val slotRarity = intArrayOf(0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 5, 6)

    private val skinNames = listOf(
        listOf("Tozlu Yol", "Çelik Gri", "Çırak Yeşili", "Sabah Sisi", "Bakır Damar", "Taş Desen", "Orman Çizgisi", "Doğan Güneş", "Çelik Sürü", "Şafak Hükümdarı", "Gümüş Tılsım", "Kurucu Mührü"),
        listOf("Neon Phantom", "Mor Işık", "Pembe Sinyal", "Elektrik Dalgası", "Flüoresan Ağ", "Gece Tabelası", "Lazer Çizgisi", "Neon Ejder", "Prizma Şehri", "Voltaj Kralı", "Kuantum Parıltı", "Neon Hükümdar"),
        listOf("Don Çiçeği", "Kar Tanesi", "Buzul Mavisi", "Kutup Hayaleti", "Kırağı Ağı", "Donmuş Nehir", "Aurora Şeridi", "Kristal Patlama", "Çığ Savaşçısı", "Buzul Efendisi", "Mutlak Sıfır", "Kış Hükümdarı"),
        listOf("Gece Perdesi", "Sis Kuşağı", "Karanlık Ağ", "Kuzgun Tüyü", "Gölge Avcısı", "Sessiz Mezar", "Karanlık Ay", "Hayalet Pusu", "Obsidyen Dalga", "Gölge İmparatoru", "Karaduvar", "Sonsuz Karanlık"),
        listOf("Devre Kartı", "Piksel Yağmuru", "Veri Akışı", "Kod Kırıcı", "Hologram Ağ", "Siber Ejder", "Devre Kesici", "Sinyal Kaybı", "Matris Çekirdek", "Ana Sistem", "Sıfır Gün", "Çekirdek Çöküşü"),
        listOf("Kor Çizgisi", "Kül Fırtınası", "Magma Damarı", "Kızıl Fırtına", "Ateş Böceği", "Yanık Çelik", "Volkan Dişi", "Alev Kanadı", "Cehennem Hattı", "Alev Efendisi", "Güneş Patlaması", "Alevlerin Kralı"),
        listOf("Bilinmeyen Rün", "Sis Bulmacası", "Mühürlü Harf", "Gizli Yol", "Zehirli Akım", "Kayıp Hatıra", "Esrar Perdesi", "Mor Galaksi", "Simya Halkası", "Gizem Ustası", "Yıldız Haritası", "Büyücü Mührü"),
        listOf("Altın Tozu", "Bal Peteği", "Sarı Dalga", "Hazine Haritası", "Pirinç Desen", "Altın Yaprak", "Sultan Işığı", "Altın İmparator", "Kral Mührü", "Altın Çağ", "Midas Dokunuşu", "Saf Altın Tahtı"),
        listOf("Efsane Başlangıcı", "Kahraman Yolu", "Destan Çizgisi", "Kadim Rün", "Savaşçı Ruhu", "Ejderha Pulu", "Titan Zırhı", "Kozmik Hükümdar", "Yıldız Kıran", "Ölümsüz", "Ebedi Alev", "Efsanelerin Efendisi"),
        listOf("Seçkin Gri", "Platin Çizgi", "Elmas Kesim", "Kraliyet Mavisi", "Zümrüt Saray", "Yakut Taç", "Safir Gecesi", "Seçkin Ejder", "Opal Gökyüzü", "Koleksiyoncu Gururu", "Müze Parçası", "Mutlak Seçkin")
    )

    private val cheapOdds = doubleArrayOf(60.0, 25.0, 10.0, 3.5, 1.2, 0.25, 0.05)
    private val richOdds = doubleArrayOf(8.0, 20.0, 34.0, 24.0, 10.0, 3.5, 0.5)

    /** Kasa sırasına göre nadirlik olasılıkları (yüzde, toplam = 100). */
    fun odds(crateIdx: Int): DoubleArray =
        DoubleArray(7) { cheapOdds[it] + (richOdds[it] - cheapOdds[it]) * crateIdx / 9.0 }

    val crates: List<Crate> = listOf(
        Crate("c0", 0, "Başlangıç Kasası", 100, "Başlangıç Koleksiyonu", Color(0xFF5C6B7A), Color(0xFF9AA4B2), "Yeni başlayanlar için ucuz ve güvenli"),
        Crate("c1", 1, "Neon Kasası", 250, "Neon Koleksiyonu", Color(0xFF00E5FF), Color(0xFFFF2BD6), "Parlayan şehir ışıkları"),
        Crate("c2", 2, "Buz Kasası", 400, "Buz Koleksiyonu", Color(0xFF7FDBFF), Color(0xFF3A6EA5), "Kutup soğuğu ve kristaller"),
        Crate("c3", 3, "Gölge Kasası", 500, "Gölge Koleksiyonu", Color(0xFF6A4DB8), Color(0xFF241A3A), "Karanlıkta saklanan desenler"),
        Crate("c4", 4, "Siber Kasası", 650, "Siber Koleksiyonu", Color(0xFF00FFA3), Color(0xFF2E6BFF), "Devreler, hologramlar, veri"),
        Crate("c5", 5, "Alev Kasası", 800, "Alev Koleksiyonu", Color(0xFFFF5A1F), Color(0xFFB80F0F), "Magma ve ateş"),
        Crate("c6", 6, "Gizem Kasası", 1000, "Gizem Koleksiyonu", Color(0xFF9B4DFF), Color(0xFF2ED1A0), "Rünler ve esrarengiz akımlar"),
        Crate("c7", 7, "Altın Kasası", 1200, "Altın Koleksiyonu", Color(0xFFFFC93C), Color(0xFFB8860B), "Saf altın işlemeler"),
        Crate("c8", 8, "Efsane Kasası", 1500, "Efsane Koleksiyonu", Color(0xFFFF4545), Color(0xFFFFC93C), "Destanlardan gelen kaplamalar"),
        Crate("c9", 9, "Seçkin Koleksiyon Kasası", 2500, "Seçkin Koleksiyon", Color(0xFFE8EEF7), Color(0xFF2EA8FF), "En değerli ve en seçkin eşyalar")
    )

    private fun weaponFor(c: Int, i: Int): Weapon = when {
        i < 9 -> weapons[(c * 9 + i) % weapons.size]
        i == 9 -> knives[c % knives.size]
        i == 10 -> gloves[c % gloves.size]
        else -> if (c % 2 == 0) knives[(c + 2) % knives.size] else gloves[(c + 1) % gloves.size]
    }

    /** Kasa fiyatına göre değer çarpanı: ortalama beklenen değer ≈ fiyatın %80'i (Gold gideri). */
    private fun valueMult(c: Crate): Double {
        val o = odds(c.idx)
        var ev = 0.0
        for (r in 0..6) ev += o[r] / 100.0 * Rarity.values()[r].base
        return 0.8 * c.price / ev
    }

    val skins: List<Skin> = crates.flatMap { c ->
        val m = valueMult(c)
        (0 until 12).map { i ->
            val r = Rarity.values()[slotRarity[i]]
            val h = Random(c.idx * 100 + i).nextDouble(0.9, 1.1)
            Skin("${c.id}_$i", weaponFor(c.idx, i), skinNames[c.idx][i], r, c.idx, i, maxOf(5, (r.base * m * h).roundToInt()))
        }
    }

    val skinMap: Map<String, Skin> = skins.associateBy { it.id }
    val crateMap: Map<String, Crate> = crates.associateBy { it.id }

    fun pool(c: Crate): List<Skin> = skins.filter { it.crateIdx == c.idx }

    /** Eşya başına olasılık (%). */
    fun chanceOf(s: Skin): Double {
        val sameRarity = skins.count { it.crateIdx == s.crateIdx && it.rarity == s.rarity }
        return odds(s.crateIdx)[s.rarity.ordinal] / sameRarity
    }

    fun roll(c: Crate, rnd: Random): Skin {
        val o = odds(c.idx)
        var x = rnd.nextDouble() * 100.0
        var r = 6
        for (i in 0..6) {
            if (x < o[i]) { r = i; break }
            x -= o[i]
        }
        val cands = pool(c).filter { it.rarity.ordinal == r }
        return cands[rnd.nextInt(cands.size)]
    }

    fun marketSkin(rnd: Random): Skin {
        var x = rnd.nextDouble() * 100.0
        var r = 0
        for (i in 0..6) {
            r = i
            if (x < cheapOdds[i]) break
            x -= cheapOdds[i]
        }
        val cands = skins.filter { it.rarity.ordinal == r }
        return cands[rnd.nextInt(cands.size)]
    }

    val sellers = listOf("Demo_Kuzgun", "Demo_Yıldız", "Demo_Boran", "Demo_Selin", "Demo_Atlas", "Demo_Mira", "Demo_Tufan", "Demo_Deniz")

    val quests = listOf(
        QuestDef("open", "Bir kasa aç", 1, 80),
        QuestDef("add", "Bir eşyayı envanterine ekle", 1, 50),
        QuestDef("sell", "Bir eşyayı sat", 1, 60),
        QuestDef("market", "Pazarı ziyaret et", 1, 40),
        QuestDef("newskin", "Koleksiyonuna yeni bir kaplama ekle", 1, 100)
    )

    val achs = listOf(
        AchDef("open1", "İlk Kasa", "İlk kasanı aç", 100),
        AchDef("open10", "Kasa Avcısı", "10 kasa aç", 300),
        AchDef("open50", "Kasa Ustası", "50 kasa aç", 1000),
        AchDef("sell5", "Tüccar", "5 eşya sat", 150),
        AchDef("coll25", "Koleksiyoncu", "25 farklı kaplama topla", 500),
        AchDef("leg", "Efsane Avcısı", "Efsanevi veya üstü bir kaplama kazan", 400)
    )

    fun dailyLabel(day: Int): String = when (day) {
        1 -> "100 Gold"
        2 -> "150 Gold"
        3 -> "Ücretsiz Başlangıç Kasası"
        4 -> "200 Gold"
        5 -> "Özel kaplama (Destansı veya Efsanevi)"
        6 -> "300 Gold"
        else -> "Ücretsiz Altın Kasası (Nadir kasa)"
    }
}
