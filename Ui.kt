package com.skingold.market

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

object C {
    val bg = Color(0xFF0B0D12)
    val surface = Color(0xFF141821)
    val card = Color(0xFF1B202B)
    val silver = Color(0xFFC9D1DC)
    val gold = Color(0xFFFFC93C)
    val blue = Color(0xFF2EA8FF)
    val purple = Color(0xFFA55BFF)
    val red = Color(0xFFFF4545)
    val green = Color(0xFF2ECC8F)
    val dim = Color(0xFF7C8696)
}

sealed class Route {
    object Daily : Route()
    object Quests : Route()
    object Collection : Route()
    object Vp : Route()
    object Settings : Route()
    data class Detail(val skinId: String, val uid: Long = 0L) : Route()
    data class CrateView(val id: String) : Route()
    data class TradeView(val id: Long) : Route()
}

class Nav(val tab: MutableIntState, val stack: SnapshotStateList<Route>) {
    fun go(r: Route) { stack.add(r) }
    fun back() { if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex) }
    fun toTab(i: Int) { stack.clear(); tab.intValue = i }
}

private val nf: NumberFormat = NumberFormat.getIntegerInstance(Locale("tr", "TR"))
fun fmt(v: Int): String = nf.format(v)

@Composable
fun GoldText(v: Int, size: TextUnit = 13.sp, bold: Boolean = true) {
    Text("${fmt(v)} Gold", color = C.gold, fontSize = size, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
}

@Composable
fun Pill(text: String, color: Color) {
    Text(
        text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.16f)).padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text, fontSize = 12.sp, color = if (selected) Color.Black else C.silver, fontWeight = FontWeight.Medium,
        modifier = Modifier.clip(RoundedCornerShape(50))
            .background(if (selected) C.gold else C.card)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

@Composable
fun Panel(modifier: Modifier = Modifier, border: Color? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(C.card)
            .then(if (border != null) Modifier.border(BorderStroke(1.2.dp, border), RoundedCornerShape(16.dp)) else Modifier)
            .padding(14.dp)
    ) { content() }
}

@Composable
fun Bar(progress: Float, color: Color = C.gold, height: Dp = 8.dp) {
    Box(Modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(50)).background(Color(0x33FFFFFF))) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().background(color))
    }
}

@Composable
fun TopBar(title: String, gold: Int, onBack: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().background(C.surface).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Text("←", color = C.silver, fontSize = 24.sp, modifier = Modifier.clickable(onClick = onBack).padding(end = 12.dp, start = 4.dp))
        }
        Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            fmt(gold) + " Gold", color = C.gold, fontWeight = FontWeight.Bold, fontSize = 14.sp,
            modifier = Modifier.clip(RoundedCornerShape(50)).background(C.gold.copy(alpha = 0.14f)).padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun SkinCard(
    skin: Skin, modifier: Modifier = Modifier, isNew: Boolean = false, pinned: Boolean = false,
    selected: Boolean = false, extra: String? = null, dim: Boolean = false, onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(14.dp)
    val rc = skin.rarity.color
    val borderColor: Color = if (skin.rarity.ordinal >= 4) {
        val t = rememberInfiniteTransition(label = "glow")
        val a by t.animateFloat(0.45f, 1f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "a")
        rc.copy(alpha = a)
    } else rc.copy(alpha = 0.75f)
    val bw = when {
        selected -> 3.dp
        skin.rarity.ordinal >= 5 -> 2.5.dp
        skin.rarity.ordinal >= 3 -> 2.dp
        else -> 1.2.dp
    }
    Column(
        modifier.clip(shape).background(C.card)
            .border(BorderStroke(bw, if (selected) C.gold else borderColor), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(8.dp)
    ) {
        Box {
            SkinArt(skin, Modifier.fillMaxWidth().then(if (dim) Modifier.background(Color(0x99000000)) else Modifier))
            Row(Modifier.align(Alignment.TopStart), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (isNew) Pill("YENİ", C.green)
                if (pinned) Pill("SABİT", C.blue)
            }
        }
        Text(skin.weapon.name, color = C.dim, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(skin.skinName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(skin.rarity.tr, color = rc, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1)
            if (extra != null) Text(extra, color = C.silver, fontSize = 11.sp) else GoldText(skin.value, 11.sp)
        }
    }
}

@Composable
fun BigButton(text: String, enabled: Boolean = true, color: Color = C.gold, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(12.dp))
            .background(if (enabled) color else Color(0xFF3A404C))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (enabled) Color.Black else C.dim, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
fun ConfirmDialog(title: String, text: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = C.surface,
        title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold) },
        text = { Text(text, color = C.silver) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirm, color = C.gold, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç", color = C.silver) } }
    )
}

@Composable
fun H2(text: String) {
    Text(text, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
}

@Composable
fun Dot(color: Color) {
    Box(Modifier.size(10.dp).clip(CircleShape).background(color))
}

@Composable
fun SpacerW(w: Dp) { Spacer(Modifier.width(w)) }
