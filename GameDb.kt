package com.skingold.market

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlin.random.Random

data class OpResult(val ok: Boolean, val message: String = "", val skinId: String? = null, val uid: Long = 0L)

data class InvItem(val uid: Long, val skin: Skin, val acquired: Long, val isNew: Boolean, val pinned: Boolean)
data class Listing(val id: Long, val skin: Skin, val seller: String, val price: Int, val created: Long)
data class Offer(val id: Long, val partner: String, val skins: List<Skin>)
data class TradeRec(val ts: Long, val partner: String, val gave: String, val got: String, val gaveVal: Int, val gotVal: Int)
data class QuestState(val def: QuestDef, val progress: Int, val claimed: Boolean)
data class AchState(val def: AchDef, val progress: Int, val target: Int, val claimed: Boolean)

data class Snap(
    val gold: Int, val xp: Int, val name: String,
    val inventory: List<InvItem>, val recent: List<Skin>, val tokens: Map<String, Int>,
    val streak: Int, val canClaim: Boolean, val nextDay: Int, val claimedDays: Int,
    val quests: List<QuestState>, val achs: List<AchState>, val collection: Set<String>,
    val listings: List<Listing>, val offers: List<Offer>, val trades: List<TradeRec>,
    val opened: Int, val sold: Int, val anim: Boolean
)

/**
 * Yerel oyun veritabanı. Tüm Gold / envanter değişiklikleri tek bir transaction içinde yapılır;
 * hata olursa hiçbir değişiklik uygulanmaz.
 */
class GameDb(ctx: Context, name: String? = "skingold.db") : SQLiteOpenHelper(ctx, name, null, 1) {

    var clock: () -> LocalDate = { LocalDate.now() }
    private fun day(): String = clock().toString()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE meta(k TEXT PRIMARY KEY, v TEXT NOT NULL)")
        db.execSQL("CREATE TABLE inventory(uid INTEGER PRIMARY KEY AUTOINCREMENT, skin_id TEXT NOT NULL, acquired INTEGER NOT NULL, is_new INTEGER NOT NULL DEFAULT 1, pinned INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("CREATE TABLE collection(skin_id TEXT PRIMARY KEY)")
        db.execSQL("CREATE TABLE opened(id INTEGER PRIMARY KEY AUTOINCREMENT, crate_id TEXT NOT NULL, skin_id TEXT NOT NULL, ts INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE listings(id INTEGER PRIMARY KEY AUTOINCREMENT, skin_id TEXT NOT NULL, seller TEXT NOT NULL, price INTEGER NOT NULL, created INTEGER NOT NULL, status INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("CREATE TABLE offers(id INTEGER PRIMARY KEY AUTOINCREMENT, partner TEXT NOT NULL, skins TEXT NOT NULL, status INTEGER NOT NULL DEFAULT 0, created INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE trades(id INTEGER PRIMARY KEY AUTOINCREMENT, ts INTEGER NOT NULL, partner TEXT NOT NULL, gave TEXT NOT NULL, got TEXT NOT NULL, gave_val INTEGER NOT NULL, got_val INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE quests(day TEXT NOT NULL, qid TEXT NOT NULL, progress INTEGER NOT NULL DEFAULT 0, claimed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(day, qid))")
        db.execSQL("CREATE TABLE tokens(crate_id TEXT PRIMARY KEY, n INTEGER NOT NULL)")
        initMeta(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    // ---------- yardımcılar ----------
    private fun initMeta(db: SQLiteDatabase) {
        sm(db, "gold", "1000"); sm(db, "xp", "0"); sm(db, "name", "Oyuncu")
        sm(db, "streak", "0"); sm(db, "lastClaim", ""); sm(db, "opened", "0"); sm(db, "sold", "0")
        sm(db, "best", "0"); sm(db, "anim", "1"); sm(db, "marketDay", "")
    }

    private fun gm(db: SQLiteDatabase, k: String, def: String = ""): String {
        return db.rawQuery("SELECT v FROM meta WHERE k=?", arrayOf(k)).use { if (it.moveToFirst()) it.getString(0) else def }
    }

    private fun sm(db: SQLiteDatabase, k: String, v: String) {
        exec(db, "INSERT OR REPLACE INTO meta(k,v) VALUES(?,?)", k, v)
    }

    private fun gi(db: SQLiteDatabase, k: String): Int = gm(db, k, "0").toIntOrNull() ?: 0

    private fun exec(db: SQLiteDatabase, sql: String, vararg args: Any): Int {
        return db.compileStatement(sql).use { st ->
            args.forEachIndexed { i, a ->
                if (a is Long || a is Int) st.bindLong(i + 1, (a as Number).toLong()) else st.bindString(i + 1, a.toString())
            }
            st.executeUpdateDelete()
        }
    }

    private fun cnt(db: SQLiteDatabase, sql: String, vararg args: String): Int {
        return db.rawQuery(sql, arrayOf(*args)).use { it.moveToFirst(); it.getInt(0) }
    }

    private fun addGold(db: SQLiteDatabase, n: Int) = sm(db, "gold", (gi(db, "gold") + n).toString())
    private fun addXp(db: SQLiteDatabase, n: Int) = sm(db, "xp", (gi(db, "xp") + n).toString())

    private inline fun tx(block: (SQLiteDatabase) -> OpResult): OpResult {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val r = block(db)
            if (r.ok) db.setTransactionSuccessful()
            return r
        } finally {
            db.endTransaction()
        }
    }

    private fun questProgress(db: SQLiteDatabase, qid: String, n: Int = 1) {
        val d = day()
        val tgt = Catalog.quests.first { it.id == qid }.target
        exec(db, "INSERT OR IGNORE INTO quests(day,qid,progress,claimed) VALUES(?,?,0,0)", d, qid)
        exec(db, "UPDATE quests SET progress=MIN(progress+?,?) WHERE day=? AND qid=?", n, tgt, d, qid)
    }

    private fun addItem(db: SQLiteDatabase, skinId: String): Long {
        val cv = ContentValues().apply {
            put("skin_id", skinId); put("acquired", System.currentTimeMillis()); put("is_new", 1); put("pinned", 0)
        }
        val uid = db.insertOrThrow("inventory", null, cv)
        val c = ContentValues().apply { put("skin_id", skinId) }
        val isNewColl = db.insertWithOnConflict("collection", null, c, SQLiteDatabase.CONFLICT_IGNORE) != -1L
        if (isNewColl) questProgress(db, "newskin")
        questProgress(db, "add")
        val r = Catalog.skinMap.getValue(skinId).rarity.ordinal
        if (r > gi(db, "best")) sm(db, "best", r.toString())
        return uid
    }

    private fun addToken(db: SQLiteDatabase, crateId: String) {
        exec(db, "INSERT OR IGNORE INTO tokens(crate_id,n) VALUES(?,0)", crateId)
        exec(db, "UPDATE tokens SET n=n+1 WHERE crate_id=?", crateId)
    }

    // ---------- dünya (pazar ilanları, takas teklifleri, günlük görev satırları) ----------
    fun ensureWorld() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val d = day()
            val rnd = Random(System.nanoTime())
            var toAdd = 0
            val active = cnt(db, "SELECT COUNT(*) FROM listings WHERE status=0")
            if (active < 30) toAdd = 30 - active
            if (gm(db, "marketDay") != d) { toAdd += 6; sm(db, "marketDay", d) }
            repeat(toAdd) { genListing(db, rnd) }
            val pend = cnt(db, "SELECT COUNT(*) FROM offers WHERE status=0")
            repeat(maxOf(0, 3 - pend)) { genOffer(db, rnd) }
            for (q in Catalog.quests) exec(db, "INSERT OR IGNORE INTO quests(day,qid,progress,claimed) VALUES(?,?,0,0)", d, q.id)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun genListing(db: SQLiteDatabase, rnd: Random) {
        val s = Catalog.marketSkin(rnd)
        val price = (s.value * rnd.nextDouble(0.95, 1.25)).roundToInt().coerceAtLeast(5)
        exec(db, "INSERT INTO listings(skin_id,seller,price,created,status) VALUES(?,?,?,?,0)",
            s.id, Catalog.sellers.random(rnd), price, System.currentTimeMillis() - rnd.nextLong(0, 6 * 3600_000L))
    }

    private fun genOffer(db: SQLiteDatabase, rnd: Random) {
        val n = if (rnd.nextInt(3) == 0) 2 else 1
        val ids = (0 until n).map { Catalog.marketSkin(rnd).id }
        exec(db, "INSERT INTO offers(partner,skins,status,created) VALUES(?,?,0,?)",
            Catalog.sellers.random(rnd), ids.joinToString(","), System.currentTimeMillis())
    }

    // ---------- oyun işlemleri ----------
    fun openCrate(crateId: String, rnd: Random = Random.Default): OpResult = tx { db ->
        val crate = Catalog.crateMap[crateId] ?: return@tx OpResult(false, "Kasa bulunamadı.")
        val tokens = db.rawQuery("SELECT n FROM tokens WHERE crate_id=?", arrayOf(crateId)).use { if (it.moveToFirst()) it.getInt(0) else 0 }
        if (tokens > 0) {
            exec(db, "UPDATE tokens SET n=n-1 WHERE crate_id=?", crateId)
        } else {
            val gold = gi(db, "gold")
            if (gold < crate.price) return@tx OpResult(false, "Yetersiz Gold. Bu kasa için ${crate.price} Gold gerekiyor.")
            sm(db, "gold", (gold - crate.price).toString())
        }
        val skin = Catalog.roll(crate, rnd)
        val uid = addItem(db, skin.id)
        exec(db, "INSERT INTO opened(crate_id,skin_id,ts) VALUES(?,?,?)", crateId, skin.id, System.currentTimeMillis())
        sm(db, "opened", (gi(db, "opened") + 1).toString())
        questProgress(db, "open")
        addXp(db, 20)
        OpResult(true, "", skin.id, uid)
    }

    fun sell(uid: Long): OpResult = tx { db ->
        val row = db.rawQuery("SELECT skin_id,pinned FROM inventory WHERE uid=?", arrayOf(uid.toString())).use {
            if (it.moveToFirst()) it.getString(0) to it.getInt(1) else null
        } ?: return@tx OpResult(false, "Bu eşya envanterde bulunamadı. Zaten satılmış olabilir.")
        if (row.second == 1) return@tx OpResult(false, "Sabitlenmiş eşya satılamaz. Önce sabitlemeyi kaldır.")
        val skin = Catalog.skinMap[row.first] ?: return@tx OpResult(false, "Eşya verisi bulunamadı.")
        if (exec(db, "DELETE FROM inventory WHERE uid=?", uid) != 1) return@tx OpResult(false, "Satış tamamlanamadı.")
        addGold(db, skin.value)
        sm(db, "sold", (gi(db, "sold") + 1).toString())
        questProgress(db, "sell")
        addXp(db, 5)
        OpResult(true, "${skin.fullName} satıldı: +${skin.value} Gold", skin.id)
    }

    fun buyListing(id: Long): OpResult = tx { db ->
        val row = db.rawQuery("SELECT skin_id,price FROM listings WHERE id=? AND status=0", arrayOf(id.toString())).use {
            if (it.moveToFirst()) it.getString(0) to it.getInt(1) else null
        } ?: return@tx OpResult(false, "Bu ilan artık satışta değil.")
        val gold = gi(db, "gold")
        if (gold < row.second) return@tx OpResult(false, "Yetersiz Gold. ${row.second - gold} Gold daha gerekiyor.")
        if (exec(db, "UPDATE listings SET status=1 WHERE id=? AND status=0", id) != 1) return@tx OpResult(false, "Bu ilan artık satışta değil.")
        sm(db, "gold", (gold - row.second).toString())
        val uid = addItem(db, row.first)
        addXp(db, 10)
        OpResult(true, "Satın alındı: ${Catalog.skinMap.getValue(row.first).fullName}", row.first, uid)
    }

    fun visitMarket(): OpResult = tx { db -> questProgress(db, "market"); OpResult(true) }

    fun trade(offerId: Long, uids: List<Long>): OpResult = tx { db ->
        val ids = uids.distinct()
        if (ids.isEmpty()) return@tx OpResult(false, "Takas için en az bir eşya seçmelisin.")
        val offer = db.rawQuery("SELECT partner,skins FROM offers WHERE id=? AND status=0", arrayOf(offerId.toString())).use {
            if (it.moveToFirst()) it.getString(0) to it.getString(1) else null
        } ?: return@tx OpResult(false, "Bu teklif artık geçerli değil.")
        val theirs = offer.second.split(",").mapNotNull { Catalog.skinMap[it] }
        val mine = ArrayList<Skin>()
        for (u in ids) {
            val r = db.rawQuery("SELECT skin_id,pinned FROM inventory WHERE uid=?", arrayOf(u.toString())).use {
                if (it.moveToFirst()) it.getString(0) to it.getInt(1) else null
            } ?: return@tx OpResult(false, "Seçtiğin eşyalardan biri artık envanterinde değil.")
            if (r.second == 1) return@tx OpResult(false, "Sabitlenmiş eşyalar takasa konamaz.")
            mine.add(Catalog.skinMap.getValue(r.first))
        }
        val mv = mine.sumOf { it.value }
        val tv = theirs.sumOf { it.value }
        if (mv < tv * 0.9) return@tx OpResult(false, "Demo oyuncu teklifi kabul etmedi: verdiğin eşyaların değeri yetersiz.")
        for (u in ids) if (exec(db, "DELETE FROM inventory WHERE uid=?", u) != 1) return@tx OpResult(false, "Takas sırasında hata oluştu.")
        theirs.forEach { addItem(db, it.id) }
        exec(db, "UPDATE offers SET status=1 WHERE id=?", offerId)
        exec(db, "INSERT INTO trades(ts,partner,gave,got,gave_val,got_val) VALUES(?,?,?,?,?,?)",
            System.currentTimeMillis(), offer.first, mine.joinToString(", ") { it.fullName }, theirs.joinToString(", ") { it.fullName }, mv, tv)
        addXp(db, 15)
        OpResult(true, "Demo takas tamamlandı!")
    }

    fun declineOffer(offerId: Long): OpResult = tx { db ->
        if (exec(db, "UPDATE offers SET status=2 WHERE id=? AND status=0", offerId) != 1) return@tx OpResult(false, "Teklif zaten kapalı.")
        val rnd = Random(System.nanoTime())
        genOffer(db, rnd)
        OpResult(true, "Teklif reddedildi.")
    }

    fun claimDaily(rnd: Random = Random.Default): OpResult = tx { db ->
        val d = day()
        val last = gm(db, "lastClaim")
        if (last == d) return@tx OpResult(false, "Bugünün ödülü zaten alındı.")
        val prev = gi(db, "streak")
        val yesterday = clock().minusDays(1).toString()
        val streak = if (last == yesterday) (prev % 7) + 1 else 1
        var msg = ""
        when (streak) {
            1 -> { addGold(db, 100); msg = "+100 Gold" }
            2 -> { addGold(db, 150); msg = "+150 Gold" }
            3 -> { addToken(db, "c0"); msg = "Ücretsiz Başlangıç Kasası kazandın!" }
            4 -> { addGold(db, 200); msg = "+200 Gold" }
            5 -> {
                val s = Catalog.skins.filter { it.rarity.ordinal in 3..4 }.random(rnd)
                addItem(db, s.id); msg = "Özel kaplama: ${s.fullName}"
            }
            6 -> { addGold(db, 300); msg = "+300 Gold" }
            else -> { addToken(db, "c7"); msg = "Ücretsiz Altın Kasası kazandın!" }
        }
        sm(db, "streak", streak.toString()); sm(db, "lastClaim", d)
        addXp(db, 10)
        OpResult(true, "Gün $streak ödülü: $msg")
    }

    fun claimQuest(qid: String): OpResult = tx { db ->
        val q = Catalog.quests.firstOrNull { it.id == qid } ?: return@tx OpResult(false, "Görev bulunamadı.")
        val n = exec(db, "UPDATE quests SET claimed=1 WHERE day=? AND qid=? AND claimed=0 AND progress>=?", day(), qid, q.target)
        if (n != 1) return@tx OpResult(false, "Bu görev ödülü alınamaz veya zaten alındı.")
        addGold(db, q.reward); addXp(db, 25)
        OpResult(true, "+${q.reward} Gold görev ödülü alındı!")
    }

    private fun achProgress(db: SQLiteDatabase, id: String): Pair<Int, Int> = when (id) {
        "open1" -> minOf(gi(db, "opened"), 1) to 1
        "open10" -> minOf(gi(db, "opened"), 10) to 10
        "open50" -> minOf(gi(db, "opened"), 50) to 50
        "sell5" -> minOf(gi(db, "sold"), 5) to 5
        "coll25" -> minOf(cnt(db, "SELECT COUNT(*) FROM collection"), 25) to 25
        else -> (if (gi(db, "best") >= 4) 1 else 0) to 1
    }

    fun claimAch(id: String): OpResult = tx { db ->
        val def = Catalog.achs.firstOrNull { it.id == id } ?: return@tx OpResult(false, "Başarım bulunamadı.")
        if (gm(db, "ach_$id") == "1") return@tx OpResult(false, "Bu başarım ödülü zaten alındı.")
        val (p, t) = achProgress(db, id)
        if (p < t) return@tx OpResult(false, "Bu başarım henüz tamamlanmadı.")
        sm(db, "ach_$id", "1")
        addGold(db, def.reward); addXp(db, 30)
        OpResult(true, "+${def.reward} Gold başarım ödülü alındı!")
    }

    fun setPinned(uid: Long, pinned: Boolean): OpResult = tx { db ->
        if (exec(db, "UPDATE inventory SET pinned=? WHERE uid=?", if (pinned) 1 else 0, uid) != 1) return@tx OpResult(false, "Eşya bulunamadı.")
        OpResult(true, if (pinned) "Eşya koleksiyona sabitlendi." else "Sabitleme kaldırıldı.")
    }

    fun markSeen(uid: Long): OpResult = tx { db -> exec(db, "UPDATE inventory SET is_new=0 WHERE uid=?", uid); OpResult(true) }

    fun rename(name: String): OpResult = tx { db ->
        val n = name.trim()
        if (n.length !in 2..16) return@tx OpResult(false, "İsim 2 ile 16 karakter arasında olmalı.")
        sm(db, "name", n)
        OpResult(true, "İsim güncellendi.")
    }

    fun setAnim(on: Boolean): OpResult = tx { db -> sm(db, "anim", if (on) "1" else "0"); OpResult(true) }

    fun resetAll(): OpResult = tx { db ->
        for (t in listOf("meta", "inventory", "collection", "opened", "listings", "offers", "trades", "quests", "tokens"))
            db.execSQL("DELETE FROM $t")
        initMeta(db)
        OpResult(true, "Oyun sıfırlandı.")
    }

    /** Yalnızca testler için. */
    internal fun setGoldForTest(n: Int) { val db = writableDatabase; sm(db, "gold", n.toString()) }

    // ---------- anlık görüntü ----------
    fun snapshot(): Snap {
        val db = readableDatabase
        val inv = ArrayList<InvItem>()
        db.rawQuery("SELECT uid,skin_id,acquired,is_new,pinned FROM inventory ORDER BY uid DESC", null).use { c ->
            while (c.moveToNext()) {
                val s = Catalog.skinMap[c.getString(1)] ?: continue
                inv.add(InvItem(c.getLong(0), s, c.getLong(2), c.getInt(3) == 1, c.getInt(4) == 1))
            }
        }
        val recent = ArrayList<Skin>()
        db.rawQuery("SELECT skin_id FROM opened ORDER BY id DESC LIMIT 8", null).use { c ->
            while (c.moveToNext()) Catalog.skinMap[c.getString(0)]?.let { recent.add(it) }
        }
        val tokens = HashMap<String, Int>()
        db.rawQuery("SELECT crate_id,n FROM tokens WHERE n>0", null).use { c ->
            while (c.moveToNext()) tokens[c.getString(0)] = c.getInt(1)
        }
        val today = day()
        val yesterday = clock().minusDays(1).toString()
        val last = gm(db, "lastClaim")
        val streak = gi(db, "streak")
        val canClaim = last != today
        val nextDay = if (canClaim) (if (last == yesterday) streak % 7 + 1 else 1) else streak % 7 + 1
        val claimedDays = if (canClaim) (if (last == yesterday && streak < 7) streak else 0) else streak

        val quests = Catalog.quests.map { q ->
            db.rawQuery("SELECT progress,claimed FROM quests WHERE day=? AND qid=?", arrayOf(today, q.id)).use {
                if (it.moveToFirst()) QuestState(q, it.getInt(0), it.getInt(1) == 1) else QuestState(q, 0, false)
            }
        }
        val achs = Catalog.achs.map { a ->
            val (p, t) = achProgress(db, a.id)
            AchState(a, p, t, gm(db, "ach_${a.id}") == "1")
        }
        val coll = HashSet<String>()
        db.rawQuery("SELECT skin_id FROM collection", null).use { c -> while (c.moveToNext()) coll.add(c.getString(0)) }

        val listings = ArrayList<Listing>()
        db.rawQuery("SELECT id,skin_id,seller,price,created FROM listings WHERE status=0 ORDER BY created DESC", null).use { c ->
            while (c.moveToNext()) {
                val s = Catalog.skinMap[c.getString(1)] ?: continue
                listings.add(Listing(c.getLong(0), s, c.getString(2), c.getInt(3), c.getLong(4)))
            }
        }
        val offers = ArrayList<Offer>()
        db.rawQuery("SELECT id,partner,skins FROM offers WHERE status=0 ORDER BY id DESC", null).use { c ->
            while (c.moveToNext()) {
                val sk = c.getString(2).split(",").mapNotNull { Catalog.skinMap[it] }
                if (sk.isNotEmpty()) offers.add(Offer(c.getLong(0), c.getString(1), sk))
            }
        }
        val trades = ArrayList<TradeRec>()
        db.rawQuery("SELECT ts,partner,gave,got,gave_val,got_val FROM trades ORDER BY id DESC LIMIT 20", null).use { c ->
            while (c.moveToNext()) trades.add(TradeRec(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4), c.getInt(5)))
        }
        return Snap(
            gi(db, "gold"), gi(db, "xp"), gm(db, "name", "Oyuncu"), inv, recent, tokens,
            streak, canClaim, nextDay, claimedDays, quests, achs, coll, listings, offers, trades,
            gi(db, "opened"), gi(db, "sold"), gm(db, "anim", "1") == "1"
        )
    }
}
