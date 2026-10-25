package com.skingold.market

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.time.LocalDate
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GameTest {
    private lateinit var db: GameDb

    @Before
    fun setUp() {
        db = GameDb(RuntimeEnvironment.getApplication(), null)
        db.ensureWorld()
    }

    @Test fun catalogHasAtLeast100UniqueSkinsAnd10Crates() {
        assertTrue(Catalog.skins.size >= 100)
        assertEquals(Catalog.skins.size, Catalog.skins.map { it.id }.toSet().size)
        assertEquals(Catalog.skins.size, Catalog.skins.map { it.fullName }.toSet().size)
        assertTrue(Catalog.crates.size >= 10)
        assertEquals(7, Catalog.skins.map { it.rarity }.toSet().size)
        assertEquals(7, Catalog.skins.map { it.weapon.cat }.toSet().size)
    }

    @Test fun oddsSumTo100ForEveryCrate() {
        for (c in Catalog.crates) assertEquals(100.0, Catalog.odds(c.idx).sum(), 1e-9)
    }

    @Test fun rollFollowsOddsRoughly() {
        val c = Catalog.crates[0]
        val rnd = Random(42)
        val n = 20000
        val common = (1..n).count { Catalog.roll(c, rnd).rarity == Rarity.SIRADAN }
        assertEquals(0.60, common.toDouble() / n, 0.02)
    }

    @Test fun startsWith1000Gold() {
        assertEquals(1000, db.snapshot().gold)
    }

    @Test fun openingCrateDeductsGoldAndAddsItem() {
        val r = db.openCrate("c0")
        assertTrue(r.ok)
        val s = db.snapshot()
        assertEquals(900, s.gold)
        assertEquals(1, s.inventory.size)
        assertEquals(r.skinId, s.inventory[0].skin.id)
    }

    @Test fun cannotOpenCrateWithoutEnoughGold() {
        db.setGoldForTest(50)
        val r = db.openCrate("c0")
        assertFalse(r.ok)
        val s = db.snapshot()
        assertEquals(50, s.gold)
        assertEquals(0, s.inventory.size)
    }

    @Test fun sellingTwiceOnlyPaysOnce() {
        val r = db.openCrate("c0")
        val value = Catalog.skinMap.getValue(r.skinId!!).value
        assertTrue(db.sell(r.uid).ok)
        assertFalse(db.sell(r.uid).ok)
        val s = db.snapshot()
        assertEquals(900 + value, s.gold)
        assertEquals(0, s.inventory.size)
    }

    @Test fun pinnedItemCannotBeSold() {
        val r = db.openCrate("c0")
        assertTrue(db.setPinned(r.uid, true).ok)
        assertFalse(db.sell(r.uid).ok)
        assertEquals(1, db.snapshot().inventory.size)
    }

    @Test fun listingCanOnlyBeBoughtOnce() {
        db.setGoldForTest(1_000_000)
        val l = db.snapshot().listings.first()
        assertTrue(db.buyListing(l.id).ok)
        assertFalse(db.buyListing(l.id).ok)
        val s = db.snapshot()
        assertEquals(1_000_000 - l.price, s.gold)
        assertEquals(1, s.inventory.size)
        assertTrue(s.listings.none { it.id == l.id })
    }

    @Test fun cannotBuyWithoutGold() {
        db.setGoldForTest(0)
        val l = db.snapshot().listings.first()
        assertFalse(db.buyListing(l.id).ok)
        assertEquals(0, db.snapshot().inventory.size)
    }

    @Test fun dailyRewardOncePerDayAndStreakContinues() {
        var today = LocalDate.of(2026, 1, 1)
        db.clock = { today }
        assertTrue(db.claimDaily().ok)
        assertEquals(1100, db.snapshot().gold)
        assertFalse(db.claimDaily().ok)
        today = today.plusDays(1)
        assertTrue(db.claimDaily().ok)
        assertEquals(1250, db.snapshot().gold)
        today = today.plusDays(1)
        assertTrue(db.claimDaily().ok)
        assertEquals(1, db.snapshot().tokens["c0"])
        today = today.plusDays(3) // seri bozuldu
        assertTrue(db.claimDaily().ok)
        assertEquals(1, db.snapshot().streak)
    }

    @Test fun freeCrateTokenIsConsumedInsteadOfGold() {
        var today = LocalDate.of(2026, 1, 1)
        db.clock = { today }
        db.claimDaily(); today = today.plusDays(1); db.claimDaily(); today = today.plusDays(1); db.claimDaily()
        val gold = db.snapshot().gold
        assertTrue(db.openCrate("c0").ok)
        val s = db.snapshot()
        assertEquals(gold, s.gold)
        assertEquals(null, s.tokens["c0"])
    }

    @Test fun questRewardCanBeClaimedOnlyOnce() {
        db.openCrate("c0")
        val gold = db.snapshot().gold
        assertTrue(db.claimQuest("open").ok)
        assertFalse(db.claimQuest("open").ok)
        assertEquals(gold + 80, db.snapshot().gold)
    }

    @Test fun tradeRejectsItemsNotOwned() {
        val offer = db.snapshot().offers.first()
        val r = db.trade(offer.id, listOf(99999L))
        assertFalse(r.ok)
        assertEquals(0, db.snapshot().inventory.size)
    }

    @Test fun tradeWithEnoughValueSwapsItems() {
        db.setGoldForTest(1_000_000)
        val offer = db.snapshot().offers.first()
        val need = offer.skins.sumOf { it.value }
        val mine = ArrayList<Long>()
        var total = 0
        while (total < need) {
            val r = db.openCrate("c9")
            mine.add(r.uid); total += Catalog.skinMap.getValue(r.skinId!!).value
        }
        val before = db.snapshot().inventory.size
        val res = db.trade(offer.id, mine)
        assertTrue(res.message, res.ok)
        val s = db.snapshot()
        assertEquals(before - mine.size + offer.skins.size, s.inventory.size)
        assertFalse(db.trade(offer.id, mine).ok) // aynı teklif ikinci kez kullanılamaz
        assertEquals(1, s.trades.size)
    }

    @Test fun levelCurveIsMonotonic() {
        assertEquals(1, levelFor(0))
        assertEquals(2, levelFor(50))
        assertTrue(levelFor(10000) > levelFor(1000))
    }
}
