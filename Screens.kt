package com.skingold.market

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

fun pct(v: Double): String = String.format(Locale("tr", "TR"), "%.2f", v) + "%"

private val PAD = PaddingValues(16.dp)

// ====================== KÖK ======================
@Composable
fun AppRoot(vm: GameVM) {
    val ctx = LocalContext.current
    val tab = remember { mutableIntStateOf(0) }
    val stack = remember { mutableStateListOf<Route>() }
    LaunchedEffect(vm.toast) {
        vm.toast?.let { Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show(); vm.toast = null }
    }
    BackHandler(enabled = stack.isNotEmpty() || tab.intValue != 0) {
        if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex) else tab.intValue = 0
    }
    val s = vm.snap
    if (s == null) {
        Box(Modifier.fillMaxSize().background(C.bg), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.gold) }
        return
    }
    val nav = Nav(tab, stack)
    Scaffold(
        containerColor = C.bg,
        bottomBar = { if (stack.isEmpty()) BottomNav(tab.intValue) { nav.toTab(it) } }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (val top = stack.lastOrNull()) {
                null -> when (tab.intValue) {
                    0 -> HomeScreen(vm, s, nav)
                    1 -> CratesScreen(s, nav)
                    2 -> InventoryScreen(s, nav)
                    3 -> MarketScreen(vm, s, nav)
                    else -> TradeScreen(s, nav)
                }
                Route.Daily -> DailyScreen(vm, s, nav)
                Route.Quests -> QuestsScreen(vm, s, nav)
                Route.Collection -> CollectionScreen(s, nav)
                Route.Vp -> VpScreen(s, nav)
                Route.Settings -> SettingsScreen(vm, s, nav)
                is Route.Detail -> DetailScreen(vm, s, nav, top.skinId, top.uid)
                is Route.CrateView -> CrateScreen(vm, s, nav, top.id)
                is Route.TradeView -> TradeViewScreen(vm, s, nav, top.id)
            }
        }
    }
}

@Composable
fun BottomNav(selected: Int, onSelect: (Int) -> Unit) {
    val items = listOf("🏠" to "Ana", "📦" to "Kasalar", "🎒" to "Envanter", "🛒" to "Pazar", "🔁" to "Takas")
    NavigationBar(containerColor = C.surface) {
        items.forEachIndexed { i, (ic, label) ->
            NavigationBarItem(
                selected = selected == i, onClick = { onSelect(i) },
                icon = { Text(ic, fontSize = 20.sp) },
                label = { Text(label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedTextColor = C.gold, unselectedTextColor = C.dim, indicatorColor = C.gold.copy(alpha = 0.18f)
                )
            )
        }
    }
}

// ====================== ANA MENÜ ======================
@Composable
fun HomeScreen(vm: GameVM, s: Snap, nav: Nav) {
    val lvl = levelFor(s.xp)
    val lo = xpForLevel(lvl)
    val hi = xpForLevel(lvl + 1)
    val dayOfSkin = remember { Catalog.skins[(LocalDate.now().toEpochDay() % Catalog.skins.size).toInt()] }
    Column(Modifier.fillMaxSize()) {
        TopBar("Skin Gold Market", s.gold)
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PAD, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Panel(border = C.purple.copy(alpha = 0.6f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(56.dp).clip(CircleShape).background(Brush.linearGradient(listOf(C.purple, C.blue))),
                            contentAlignment = Alignment.Center
                        ) { Text(s.name.take(1).uppercase(), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f).clickable { nav.go(Route.Settings) }) {
                            Text(s.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Seviye $lvl • ${s.xp} / $hi XP", color = C.dim, fontSize = 12.sp)
                            Spacer(Modifier.height(6.dp))
                            Bar((s.xp - lo).toFloat() / (hi - lo), C.purple)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Bakiye", color = C.dim, modifier = Modifier.weight(1f))
                        GoldText(s.gold, 26.sp)
                    }
                }
            }
            item {
                Panel(border = if (s.canClaim) C.gold else null) {
                    Text("🎁 Günlük Ödül", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (s.canClaim) "Gün ${s.nextDay}: ${Catalog.dailyLabel(s.nextDay)}"
                        else "Bugünün ödülü alındı. Yarın: Gün ${s.nextDay} • ${Catalog.dailyLabel(s.nextDay)}",
                        color = C.silver, fontSize = 13.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    BigButton(if (s.canClaim) "Ödülü Al" else "Ödül Takvimi", modifier = Modifier.fillMaxWidth()) { nav.go(Route.Daily) }
                }
            }
            item {
              Column {
                H2("Öne Çıkan Kasalar")
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(listOf(1, 6, 8)) { idx ->
                        val c = Catalog.crates[idx]
                        Column(
                            Modifier.width(170.dp).clip(RoundedCornerShape(16.dp)).background(C.card)
                                .clickable { nav.go(Route.CrateView(c.id)) }.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CrateArt(c, Modifier.fillMaxWidth())
                            Text(c.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                            GoldText(c.price)
                        }
                    }
                }
              }
            }
            item {
              Column {
                H2("Günün Kaplaması")
                Spacer(Modifier.height(8.dp))
                SkinCard(dayOfSkin, Modifier.fillMaxWidth(), onClick = { nav.go(Route.Detail(dayOfSkin.id)) })
              }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatBox("Envanter", "${s.inventory.size} eşya", Modifier.weight(1f))
                    StatBox("Açılan Kasa", "${s.opened}", Modifier.weight(1f))
                    StatBox("Satılan", "${s.sold}", Modifier.weight(1f))
                }
            }
            item {
                Panel {
                    Row {
                        Text("Koleksiyon İlerlemesi", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${s.collection.size} / ${Catalog.skins.size}", color = C.silver)
                    }
                    Spacer(Modifier.height(8.dp))
                    Bar(s.collection.size.toFloat() / Catalog.skins.size, C.green)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { nav.go(Route.Collection) }) { Text("Koleksiyonu Aç →", color = C.gold) }
                }
            }
            item {
              Column {
                H2("Son Kazanılan Eşyalar")
                Spacer(Modifier.height(8.dp))
                if (s.recent.isEmpty()) {
                    Panel { Text("Henüz kasa açmadın. Kasalar sekmesinden ilk kasanı aç!", color = C.silver) }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(s.recent) { sk ->
                            val inst = s.inventory.firstOrNull { it.skin.id == sk.id }
                            SkinCard(sk, Modifier.width(150.dp), onClick = { nav.go(Route.Detail(sk.id, inst?.uid ?: 0L)) })
                        }
                    }
                }
              }
            }
            item {
              Column {
                H2("Kısayollar")
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Shortcut("🛒 Pazar", Modifier.weight(1f)) { nav.toTab(3) }
                    Shortcut("🔁 Takas", Modifier.weight(1f)) { nav.toTab(4) }
                    Shortcut("📋 Görevler", Modifier.weight(1f)) { nav.go(Route.Quests) }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Shortcut("📚 Koleksiyon", Modifier.weight(1f)) { nav.go(Route.Collection) }
                    Shortcut("🎁 VP Mağazası", Modifier.weight(1f)) { nav.go(Route.Vp) }
                    Shortcut("⚙ Ayarlar", Modifier.weight(1f)) { nav.go(Route.Settings) }
                }
              }
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(14.dp)).background(C.card).padding(12.dp)) {
        Text(label, color = C.dim, fontSize = 11.sp)
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Shortcut(text: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(12.dp)).background(C.card).clickable(onClick = onClick).padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = C.silver, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center) }
}

// ====================== KASALAR ======================
@Composable
fun CratesScreen(s: Snap, nav: Nav) {
    Column(Modifier.fillMaxSize()) {
        TopBar("Kasalar", s.gold)
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PAD, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(Catalog.crates) { c ->
                val tokens = s.tokens[c.id] ?: 0
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(C.card)
                        .clickable { nav.go(Route.CrateView(c.id)) }.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CrateArt(c, Modifier.width(110.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(c.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(c.theme, color = C.dim, fontSize = 12.sp)
                        Spacer(Modifier.height(6.dp))
                        GoldText(c.price, 15.sp)
                        if (tokens > 0) Text("🎁 $tokens ücretsiz hak", color = C.green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("›", color = C.silver, fontSize = 28.sp)
                }
            }
        }
    }
}

@Composable
fun Reel(crate: Crate, winner: Skin, onDone: () -> Unit) {
    val density = LocalDensity.current
    val itemW = 120.dp
    val gap = 8.dp
    val items = remember(winner) {
        val rnd = Random(System.nanoTime())
        List(36) { i -> if (i == 30) winner else Catalog.roll(crate, rnd) }
    }
    val stepPx = with(density) { (itemW + gap).toPx() }
    val itemPx = with(density) { itemW.toPx() }
    val off = remember(winner) { Animatable(0f) }
    var boxW by remember { mutableIntStateOf(0) }
    val done by rememberUpdatedState(onDone)
    LaunchedEffect(winner, boxW) {
        if (boxW == 0) return@LaunchedEffect
        val jitter = (Random.nextFloat() - 0.5f) * itemPx * 0.6f
        val target = 30 * stepPx + itemPx / 2f - boxW / 2f + jitter
        off.animateTo(target, tween(2800, easing = CubicBezierEasing(0.08f, 0.7f, 0.15f, 1f)))
        done()
    }
    Box(
        Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(16.dp)).background(C.surface).onSizeChanged { boxW = it.width }
    ) {
        Row(
            Modifier.align(Alignment.CenterStart).wrapContentWidth(Alignment.Start, unbounded = true)
                .offset { IntOffset(-off.value.roundToInt(), 0) },
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            items.forEach { SkinCard(it, Modifier.width(itemW)) }
        }
        Box(Modifier.align(Alignment.Center).width(3.dp).fillMaxHeight().background(C.gold))
    }
}

@Composable
fun CrateScreen(vm: GameVM, s: Snap, nav: Nav, crateId: String) {
    val crate = Catalog.crateMap.getValue(crateId)
    val tokens = s.tokens[crateId] ?: 0
    var phase by remember { mutableIntStateOf(0) }
    var won by remember { mutableStateOf<OpResult?>(null) }
    var busy by remember { mutableStateOf(false) }
    val odds = remember { Catalog.odds(crate.idx) }
    val pool = remember { Catalog.pool(crate).sortedByDescending { it.rarity.ordinal } }
    val canAfford = tokens > 0 || s.gold >= crate.price

    Column(Modifier.fillMaxSize()) {
        TopBar(crate.name, s.gold) { nav.back() }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PAD, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CrateArt(crate, Modifier.width(220.dp)) }
            }
            item {
                when (phase) {
                    0 -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(crate.theme, color = C.silver, fontSize = 13.sp)
                        Spacer(Modifier.height(10.dp))
                        BigButton(
                            if (tokens > 0) "Ücretsiz Aç ($tokens hak)" else "Kasayı Aç • ${fmt(crate.price)} Gold",
                            enabled = canAfford && !busy, modifier = Modifier.fillMaxWidth()
                        ) {
                            busy = true
                            vm.open(crateId) { r ->
                                busy = false
                                if (r.ok) { won = r; phase = if (s.anim) 1 else 2 }
                            }
                        }
                        if (!canAfford) Text("Yetersiz Gold: ${fmt(crate.price - s.gold)} Gold daha gerekiyor.", color = C.red, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                    }
                    1 -> Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        val w = Catalog.skinMap.getValue(won!!.skinId!!)
                        Reel(crate, w) { phase = 2 }
                        TextButton(onClick = { phase = 2 }) { Text("Atla", color = C.silver) }
                    }
                    else -> {
                        val r = won
                        val skin = r?.skinId?.let { Catalog.skinMap[it] }
                        if (r != null && skin != null) {
                            val inInv = s.inventory.any { it.uid == r.uid }
                            Panel(border = skin.rarity.color) {
                                Text("Kazandın!", color = C.gold, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                                SkinArt(skin, Modifier.fillMaxWidth())
                                Text(skin.fullName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Pill(skin.rarity.tr, skin.rarity.color)
                                    Spacer(Modifier.width(8.dp))
                                    Text(skin.weapon.cat.tr, color = C.dim, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    GoldText(skin.value, 18.sp)
                                }
                                Spacer(Modifier.height(12.dp))
                                if (inInv) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        BigButton("Envantere Ekle", color = C.green, modifier = Modifier.weight(1f)) {
                                            vm.toast = "Eşya envanterine eklendi."; phase = 0
                                        }
                                        BigButton("Hemen Sat", color = C.gold, modifier = Modifier.weight(1f)) {
                                            vm.sell(r.uid) { res -> if (res.ok) phase = 0 }
                                        }
                                    }
                                } else {
                                    BigButton("Tekrar Aç", modifier = Modifier.fillMaxWidth()) { phase = 0 }
                                }
                            }
                        } else {
                            BigButton("Tekrar Dene", modifier = Modifier.fillMaxWidth()) { phase = 0 }
                        }
                    }
                }
            }
            item { H2("Kasa İçeriği ve Olasılıklar") }
            item {
                Panel {
                    Rarity.values().forEachIndexed { i, r ->
                        Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Dot(r.color); Spacer(Modifier.width(8.dp))
                            Text(r.tr, color = r.color, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(pct(odds[i]), color = C.silver)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Olasılıklar sabittir ve her açılış bağımsızdır. Aynı eşya tekrar çıkabilir.", color = C.dim, fontSize = 11.sp)
                }
            }
            items(pool.chunked(2)) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { sk ->
                        SkinCard(sk, Modifier.weight(1f), extra = pct(Catalog.chanceOf(sk)), onClick = { nav.go(Route.Detail(sk.id)) })
                    }
                    if (row.size < 2) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

// ====================== ENVANTER ======================
@Composable
fun InventoryScreen(s: Snap, nav: Nav) {
    var q by remember { mutableStateOf("") }
    var rf by remember { mutableStateOf<Rarity?>(null) }
    var cf by remember { mutableStateOf<Category?>(null) }
    var sort by remember { mutableIntStateOf(0) }
    var onlyNew by remember { mutableStateOf(false) }
    val list = remember(s.inventory, q, rf, cf, sort, onlyNew) {
        val f = s.inventory.filter {
            (rf == null || it.skin.rarity == rf) && (cf == null || it.skin.weapon.cat == cf) &&
                (!onlyNew || it.isNew) && (q.isBlank() || it.skin.fullName.contains(q.trim(), ignoreCase = true))
        }
        val sorted = when (sort) {
            0 -> f.sortedByDescending { it.skin.value }
            1 -> f.sortedBy { it.skin.value }
            2 -> f.sortedByDescending { it.skin.rarity.ordinal }
            else -> f.sortedByDescending { it.acquired }
        }
        sorted.sortedByDescending { it.pinned }
    }
    val total = remember(s.inventory) { s.inventory.sumOf { it.skin.value } }
    Column(Modifier.fillMaxSize()) {
        TopBar("Envanter", s.gold)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), contentPadding = PAD,
            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = q, onValueChange = { q = it }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Eşya ara…") }
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item { Chip("Tüm Nadirlikler", rf == null) { rf = null } }
                        items(Rarity.values().toList()) { r -> Chip(r.tr, rf == r) { rf = if (rf == r) null else r } }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item { Chip("Tüm Silahlar", cf == null) { cf = null } }
                        items(Category.values().toList()) { c -> Chip(c.tr, cf == c) { cf = if (cf == c) null else c } }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item { Chip("Değer ↓", sort == 0) { sort = 0 } }
                        item { Chip("Değer ↑", sort == 1) { sort = 1 } }
                        item { Chip("Nadirlik ↓", sort == 2) { sort = 2 } }
                        item { Chip("En Yeni", sort == 3) { sort = 3 } }
                        item { Chip("Sadece Yeniler", onlyNew) { onlyNew = !onlyNew } }
                    }
                    Row {
                        Text("${list.size} / ${s.inventory.size} eşya", color = C.dim, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text("Toplam değer: ${fmt(total)} Gold", color = C.gold, fontSize = 12.sp)
                    }
                }
            }
            if (list.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Panel {
                        Text(
                            if (s.inventory.isEmpty()) "Envanterin boş. Kasalar sekmesinden kasa aç ya da pazardan eşya al."
                            else "Filtrene uyan eşya yok.", color = C.silver
                        )
                    }
                }
            }
            items(list, key = { it.uid }) { it ->
                SkinCard(it.skin, isNew = it.isNew, pinned = it.pinned, onClick = { nav.go(Route.Detail(it.skin.id, it.uid)) })
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, color = C.dim, modifier = Modifier.weight(1f), fontSize = 13.sp)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
    }
}

@Composable
fun DetailScreen(vm: GameVM, s: Snap, nav: Nav, skinId: String, uid: Long) {
    val skin = Catalog.skinMap.getValue(skinId)
    val inv = s.inventory.firstOrNull { it.uid == uid }
    var confirm by remember { mutableStateOf(false) }
    LaunchedEffect(uid) { if (inv?.isNew == true) vm.seen(uid) }
    Column(Modifier.fillMaxSize()) {
        TopBar("Eşya Ayrıntısı", s.gold) { nav.back() }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PAD, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Panel(border = skin.rarity.color) {
                    SkinArt(skin, Modifier.fillMaxWidth())
                    Text(skin.fullName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Pill(skin.rarity.tr, skin.rarity.color)
                        Spacer(Modifier.weight(1f))
                        GoldText(skin.value, 20.sp)
                    }
                }
            }
            item {
                Panel {
                    InfoRow("Silah kategorisi", skin.weapon.cat.tr)
                    InfoRow("Silah", skin.weapon.name)
                    InfoRow("Kaplama", skin.skinName)
                    InfoRow("Koleksiyon", skin.collection)
                    InfoRow("Eşya kimliği", skin.id)
                    InfoRow("Envanterde", if (s.inventory.any { it.skin.id == skinId }) "Evet (${s.inventory.count { it.skin.id == skinId }} adet)" else "Hayır")
                    InfoRow("Koleksiyona eklendi", if (skinId in s.collection) "Evet" else "Hayır")
                    if (inv != null) InfoRow("Satılabilir", if (inv.pinned) "Hayır (sabitlenmiş)" else "Evet")
                    InfoRow("Çıkma olasılığı", pct(Catalog.chanceOf(skin)) + " (${Catalog.crates[skin.crateIdx].name})")
                }
            }
            if (inv != null) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BigButton(if (inv.pinned) "Sabitlemeyi Kaldır" else "Koleksiyona Sabitle", color = C.blue, modifier = Modifier.weight(1f)) { vm.pin(uid, !inv.pinned) }
                        BigButton("Sat • ${fmt(skin.value)}", enabled = !inv.pinned, modifier = Modifier.weight(1f)) { confirm = true }
                    }
                }
            } else {
                item { Panel { Text("Bu eşya şu an envanterinde değil.", color = C.silver) } }
            }
        }
    }
    if (confirm) {
        ConfirmDialog("Eşyayı sat", "${skin.fullName} eşyasını ${fmt(skin.value)} Gold karşılığında satmak istiyor musun?", "Sat",
            onConfirm = { confirm = false; vm.sell(uid) { if (it.ok) nav.back() } }, onDismiss = { confirm = false })
    }
}

// ====================== PAZAR ======================
@Composable
fun MarketScreen(vm: GameVM, s: Snap, nav: Nav) {
    LaunchedEffect(Unit) { vm.visitMarket() }
    var q by remember { mutableStateOf("") }
    var rf by remember { mutableStateOf<Rarity?>(null) }
    var sort by remember { mutableIntStateOf(0) }
    val now = remember { System.currentTimeMillis() }
    val list = remember(s.listings, q, rf, sort) {
        val f = s.listings.filter { (rf == null || it.skin.rarity == rf) && (q.isBlank() || it.skin.fullName.contains(q.trim(), ignoreCase = true)) }
        when (sort) {
            1 -> f.sortedBy { it.price }
            2 -> f.sortedByDescending { it.price }
            else -> f.sortedByDescending { it.created }
        }
    }
    Column(Modifier.fillMaxSize()) {
        TopBar("Pazar", s.gold)
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PAD, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Panel(border = C.blue.copy(alpha = 0.6f)) {
                    Text("Çevrimdışı pazar simülasyonu", color = C.blue, fontWeight = FontWeight.Bold)
                    Text("Satıcılar gerçek oyuncu değildir; hepsi demo profilidir. İlan fiyatları yerel ekonomi sistemiyle belirlenir.", color = C.silver, fontSize = 12.sp)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = q, onValueChange = { q = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), placeholder = { Text("İlan ara…") })
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item { Chip("Yeni İlanlar", sort == 0) { sort = 0 } }
                        item { Chip("En Ucuz → Pahalı", sort == 1) { sort = 1 } }
                        item { Chip("En Pahalı → Ucuz", sort == 2) { sort = 2 } }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item { Chip("Tüm Nadirlikler", rf == null) { rf = null } }
                        items(Rarity.values().toList()) { r -> Chip(r.tr, rf == r) { rf = if (rf == r) null else r } }
                    }
                }
            }
            if (list.isEmpty()) item { Panel { Text("Filtrene uyan ilan yok.", color = C.silver) } }
            items(list, key = { it.id }) { l ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(C.card)
                        .clickable { nav.go(Route.Detail(l.skin.id)) }.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkinArt(l.skin, Modifier.width(110.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(l.skin.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Pill(l.skin.rarity.tr, l.skin.rarity.color)
                            if (now - l.created < 3 * 3600_000L) Pill("YENİ", C.green)
                        }
                        Text("${l.seller} • DEMO", color = C.dim, fontSize = 11.sp)
                        GoldText(l.price, 15.sp)
                    }
                    BigButton("Satın Al", enabled = s.gold >= l.price, modifier = Modifier.padding(start = 6.dp)) { vm.buy(l.id) }
                }
            }
        }
    }
}

// ====================== TAKAS ======================
@Composable
fun TradeScreen(s: Snap, nav: Nav) {
    Column(Modifier.fillMaxSize()) {
        TopBar("Takas", s.gold)
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PAD, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Panel(border = C.blue.copy(alpha = 0.6f)) {
                    Text("Demo takas simülasyonu", color = C.blue, fontWeight = FontWeight.Bold)
                    Text("Teklif eden oyuncular gerçek kullanıcı değildir; demo profilleridir. Gerçek oyuncular arasında takas ileride çevrimiçi sürümde eklenecek.", color = C.silver, fontSize = 12.sp)
                }
            }
            item { H2("Gelen Demo Teklifler") }
            items(s.offers, key = { it.id }) { o ->
                Panel {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${o.partner} • DEMO", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        GoldText(o.skins.sumOf { it.value })
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        o.skins.forEach { SkinCard(it, Modifier.weight(1f)) }
                        if (o.skins.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    BigButton("Teklifi İncele", modifier = Modifier.fillMaxWidth()) { nav.go(Route.TradeView(o.id)) }
                }
            }
            item { H2("Takas Geçmişi") }
            if (s.trades.isEmpty()) item { Panel { Text("Henüz takas yapmadın.", color = C.silver) } }
            items(s.trades) { t ->
                Panel {
                    Text("${t.partner} • DEMO", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Verdin: ${t.gave} (${fmt(t.gaveVal)} Gold)", color = C.silver, fontSize = 12.sp)
                    Text("Aldın: ${t.got} (${fmt(t.gotVal)} Gold)", color = C.green, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun TradeViewScreen(vm: GameVM, s: Snap, nav: Nav, offerId: Long) {
    val selected = remember { mutableStateListOf<Long>() }
    var confirm by remember { mutableStateOf(false) }
    val offer = s.offers.firstOrNull { it.id == offerId }
    if (offer == null) {
        LaunchedEffect(Unit) { nav.back() }
        return
    }
    val theirs = offer.skins.sumOf { it.value }
    val mineItems = s.inventory.filter { it.uid in selected }
    val mine = mineItems.sumOf { it.skin.value }
    val verdict = when {
        selected.isEmpty() -> "Takas için kendi eşyalarından seç."
        mine < theirs * 0.9 -> "Değer yetersiz: demo oyuncu bu takası kabul etmez."
        mine > theirs * 1.15 -> "Dikkat: karşı tarafa fazla değer veriyorsun."
        else -> "Adil bir takas."
    }
    val verdictColor = when {
        selected.isEmpty() -> C.dim
        mine < theirs * 0.9 -> C.red
        mine > theirs * 1.15 -> C.gold
        else -> C.green
    }
    Column(Modifier.fillMaxSize()) {
        TopBar("Takas Teklifi", s.gold) { nav.back() }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), contentPadding = PAD,
            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { H2("${offer.partner} (DEMO) şunları veriyor") }
            items(offer.skins) { sk -> SkinCard(sk, onClick = { nav.go(Route.Detail(sk.id)) }) }
            item(span = { GridItemSpan(maxLineSpan) }) { H2("Senin eşyaların (vermek istediklerini seç)") }
            val tradable = s.inventory.filter { !it.pinned }
            if (tradable.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) { Panel { Text("Takas edilebilir eşyan yok.", color = C.silver) } }
            items(tradable, key = { it.uid }) { it ->
                SkinCard(it.skin, selected = it.uid in selected, pinned = false) {
                    if (it.uid in selected) selected.remove(it.uid) else selected.add(it.uid)
                }
            }
        }
        Column(Modifier.fillMaxWidth().background(C.surface).padding(14.dp)) {
            Row {
                Column(Modifier.weight(1f)) { Text("Vereceğin", color = C.dim, fontSize = 11.sp); GoldText(mine) }
                Column(Modifier.weight(1f)) { Text("Alacağın", color = C.dim, fontSize = 11.sp); GoldText(theirs) }
            }
            Text(verdict, color = verdictColor, fontSize = 12.sp, modifier = Modifier.padding(vertical = 6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BigButton("Reddet", color = C.red, modifier = Modifier.weight(1f)) { vm.decline(offerId) { nav.back() } }
                BigButton("Takası Onayla", enabled = selected.isNotEmpty(), modifier = Modifier.weight(1f)) { confirm = true }
            }
        }
    }
    if (confirm) {
        val msg = "Vereceksin:\n" + mineItems.joinToString("\n") { "• ${it.skin.fullName} (${fmt(it.skin.value)})" } +
            "  \nToplam: ${fmt(mine)} Gold\n\nAlacaksın:\n" + offer.skins.joinToString("\n") { "• ${it.fullName} (${fmt(it.value)})" } +
            "\nToplam: ${fmt(theirs)} Gold\n\nBu bir demo takastır; karşı taraf gerçek bir oyuncu değildir."
        ConfirmDialog("Takası onayla", msg, "Onayla",
            onConfirm = { confirm = false; vm.trade(offerId, selected.toList()) { if (it.ok) nav.back() } },
            onDismiss = { confirm = false })
    }
}

// ====================== GÖREVLER / GÜNLÜK ÖDÜL ======================
@Composable
fun DailyScreen(vm: GameVM, s: Snap, nav: Nav) {
    Column(Modifier.fillMaxSize()) {
        TopBar("Günlük Ödül", s.gold) { nav.back() }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PAD, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Her gün gir, ödülünü al. Bir gün atlarsan seri başa döner.", color = C.silver, fontSize = 13.sp)
            }
            items((1..7).toList().chunked(2)) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { d ->
                        val claimed = d <= s.claimedDays
                        val today = s.canClaim && d == s.nextDay
                        Column(
                            Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(if (claimed) Color(0xFF1F3A2E) else C.card)
                                .then(if (today) Modifier.background(C.gold.copy(alpha = 0.18f)) else Modifier).padding(12.dp)
                        ) {
                            Text("Gün $d", color = if (today) C.gold else Color.White, fontWeight = FontWeight.Bold)
                            Text(Catalog.dailyLabel(d), color = C.silver, fontSize = 12.sp)
                            Text(if (claimed) "✓ Alındı" else if (today) "Bugün" else "", color = if (claimed) C.green else C.gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (row.size < 2) Spacer(Modifier.weight(1f))
                }
            }
            item {
                BigButton(if (s.canClaim) "Gün ${s.nextDay} Ödülünü Al" else "Bugünün ödülü alındı", enabled = s.canClaim, modifier = Modifier.fillMaxWidth()) { vm.claimDaily() }
            }
        }
    }
}

@Composable
fun QuestsScreen(vm: GameVM, s: Snap, nav: Nav) {
    Column(Modifier.fillMaxSize()) {
        TopBar("Görevler", s.gold) { nav.back() }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PAD, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { H2("Günlük Görevler") }
            items(s.quests) { q ->
                val done = q.progress >= q.def.target
                Panel {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(q.def.title, color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Bar(q.progress.toFloat() / q.def.target, C.green)
                            Text("${q.progress} / ${q.def.target}", color = C.dim, fontSize = 11.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        BigButton(if (q.claimed) "Alındı" else "+${q.def.reward} Gold", enabled = done && !q.claimed) { vm.claimQuest(q.def.id) }
                    }
                }
            }
            item { H2("Başarımlar") }
            items(s.achs) { a ->
                val done = a.progress >= a.target
                Panel {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(a.def.title, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(a.def.desc, color = C.dim, fontSize = 12.sp)
                            Spacer(Modifier.height(6.dp))
                            Bar(a.progress.toFloat() / a.target, C.purple)
                            Text("${a.progress} / ${a.target}", color = C.dim, fontSize = 11.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        BigButton(if (a.claimed) "Alındı" else "+${a.def.reward} Gold", enabled = done && !a.claimed) { vm.claimAch(a.def.id) }
                    }
                }
            }
        }
    }
}

// ====================== KOLEKSİYON ======================
@Composable
fun CollectionScreen(s: Snap, nav: Nav) {
    Column(Modifier.fillMaxSize()) {
        TopBar("Koleksiyon", s.gold) { nav.back() }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PAD, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Panel {
                    Text("Toplam ilerleme: ${s.collection.size} / ${Catalog.skins.size}", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Bar(s.collection.size.toFloat() / Catalog.skins.size, C.green)
                    Text("Bir kaplamayı en az bir kez kazandığında koleksiyona eklenir; sonradan satsan bile kalır.", color = C.dim, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                }
            }
            items(Catalog.crates) { c ->
                val skins = Catalog.pool(c)
                val owned = skins.count { it.id in s.collection }
                Panel {
                    Row {
                        Text(c.collection, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("$owned / ${skins.size}", color = C.silver)
                    }
                    Spacer(Modifier.height(6.dp))
                    Bar(owned.toFloat() / skins.size, c.c1)
                    Spacer(Modifier.height(10.dp))
                    skins.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 6.dp)) {
                            row.forEach { sk ->
                                val has = sk.id in s.collection
                                SkinCard(sk, Modifier.weight(1f), dim = !has, extra = if (has) null else "???", onClick = { nav.go(Route.Detail(sk.id)) })
                            }
                        }
                    }
                }
            }
        }
    }
}

// ====================== VP MAĞAZASI (TASLAK) ======================
@Composable
fun VpScreen(s: Snap, nav: Nav) {
    Column(Modifier.fillMaxSize()) {
        TopBar("VP Ödül Mağazası", s.gold) { nav.back() }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PAD, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Panel(border = C.red) {
                    Text("⚠ Bu bölüm henüz etkin değil", color = C.red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Gerçek ödüller henüz etkinleştirilmemiştir. Bu ekran yalnızca bir taslaktır; hiçbir satın alma yapılamaz ve hiçbir VP gönderilmez.", color = C.silver, fontSize = 13.sp)
                }
            }
            item { Panel { Row { Text("Mevcut Gold bakiyen", color = C.dim, modifier = Modifier.weight(1f)); GoldText(s.gold, 16.sp) } } }
            items(listOf("VP Ödülü — Seçenek 1", "VP Ödülü — Seçenek 2", "VP Ödülü — Seçenek 3")) { name ->
                Panel {
                    Text(name, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Yer tutucu • Henüz tanımlanmadı", color = C.dim, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    Row { Text("Gerekli Gold:", color = C.dim, modifier = Modifier.weight(1f)); Text("Belirlenmedi", color = C.silver) }
                    Spacer(Modifier.height(8.dp))
                    BigButton("Yakında", enabled = false, modifier = Modifier.fillMaxWidth()) {}
                }
            }
            item {
                Panel {
                    Text("Satın alma koşulları", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "• Gerçek ödül teslimatı yalnızca Riot Games'in izin verdiği, yetkili ve doğrulanabilir yöntemlerle yapılabilir.\n" +
                            "• Bu uygulama Riot Games ile bağlantılı değildir ve onun sistemlerine erişmez.\n" +
                            "• Gold yalnızca oyun içi sanal para birimidir; gerçek paraya çevrilemez.",
                        color = C.silver, fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ====================== AYARLAR ======================
@Composable
fun SettingsScreen(vm: GameVM, s: Snap, nav: Nav) {
    var name by remember { mutableStateOf(s.name) }
    var confirm by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        TopBar("Ayarlar", s.gold) { nav.back() }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PAD, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Panel {
                    Text("Oyuncu adı", color = Color.White, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    BigButton("Kaydet", modifier = Modifier.fillMaxWidth()) { vm.rename(name) }
                }
            }
            item {
                Panel {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Kasa açma animasyonu", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Kapalıyken sonuç anında gösterilir.", color = C.dim, fontSize = 12.sp)
                        }
                        Switch(checked = s.anim, onCheckedChange = { vm.setAnim(it) })
                    }
                }
            }
            item {
                Panel {
                    Text("Hakkında", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        "Skin Gold Market 1.0 • Tamamen çevrimdışı. Gold yalnızca oyun içi sanal para birimidir. " +
                            "Pazar ve takastaki oyuncular demo profilleridir. Gerçek oyuncular arası özellikler ileride çevrimiçi sürümde, sunucu desteğiyle eklenecek.",
                        color = C.silver, fontSize = 12.sp
                    )
                }
            }
            item {
                BigButton("Oyunu Sıfırla", color = C.red, modifier = Modifier.fillMaxWidth()) { confirm = true }
            }
        }
    }
    if (confirm) {
        ConfirmDialog("Oyunu sıfırla", "Tüm envanter, Gold, ilerleme ve geçmiş silinecek ve 1.000 Gold ile baştan başlayacaksın. Bu işlem geri alınamaz.", "Sıfırla",
            onConfirm = { confirm = false; vm.reset { nav.stack.clear(); nav.tab.intValue = 0 } }, onDismiss = { confirm = false })
    }
}
