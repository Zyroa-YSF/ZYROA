package com.skingold.market

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

// Silah silüetleri 100x50 birimlik alanda çokgenlerle tanımlanır.
private val shapes: Map<Category, List<FloatArray>> = mapOf(
    Category.TABANCA to listOf(
        floatArrayOf(18f, 12f, 82f, 12f, 82f, 24f, 48f, 24f, 44f, 46f, 30f, 46f, 32f, 24f, 18f, 24f),
        floatArrayOf(82f, 15f, 92f, 15f, 92f, 20f, 82f, 20f)
    ),
    Category.SMG to listOf(
        floatArrayOf(8f, 14f, 84f, 14f, 84f, 26f, 52f, 26f, 52f, 44f, 40f, 44f, 40f, 26f, 30f, 26f, 26f, 40f, 16f, 40f, 18f, 26f, 8f, 26f),
        floatArrayOf(84f, 17f, 96f, 17f, 96f, 22f, 84f, 22f)
    ),
    Category.TUFEK to listOf(
        floatArrayOf(2f, 16f, 24f, 16f, 24f, 30f, 8f, 34f),
        floatArrayOf(24f, 14f, 76f, 14f, 76f, 26f, 52f, 26f, 48f, 42f, 38f, 42f, 40f, 26f, 24f, 26f),
        floatArrayOf(76f, 17f, 98f, 17f, 98f, 21f, 76f, 21f),
        floatArrayOf(40f, 9f, 60f, 9f, 60f, 14f, 40f, 14f)
    ),
    Category.KESKIN to listOf(
        floatArrayOf(2f, 18f, 22f, 18f, 22f, 32f, 6f, 36f),
        floatArrayOf(22f, 18f, 70f, 18f, 70f, 28f, 44f, 28f, 40f, 42f, 32f, 42f, 34f, 28f, 22f, 28f),
        floatArrayOf(70f, 21f, 98f, 21f, 98f, 25f, 70f, 25f),
        floatArrayOf(30f, 7f, 58f, 7f, 58f, 15f, 30f, 15f)
    ),
    Category.AGIR to listOf(
        floatArrayOf(6f, 12f, 74f, 12f, 74f, 32f, 6f, 32f),
        floatArrayOf(36f, 32f, 62f, 32f, 62f, 46f, 36f, 46f),
        floatArrayOf(74f, 17f, 98f, 17f, 98f, 25f, 74f, 25f),
        floatArrayOf(14f, 32f, 26f, 32f, 22f, 46f, 10f, 46f)
    ),
    Category.BICAK to listOf(
        floatArrayOf(28f, 14f, 72f, 14f, 96f, 28f, 28f, 34f),
        floatArrayOf(22f, 10f, 28f, 10f, 28f, 38f, 22f, 38f),
        floatArrayOf(2f, 18f, 22f, 18f, 22f, 30f, 2f, 30f)
    ),
    Category.ELDIVEN to listOf(
        floatArrayOf(26f, 22f, 70f, 22f, 70f, 46f, 26f, 46f),
        floatArrayOf(26f, 6f, 34f, 6f, 34f, 22f, 26f, 22f),
        floatArrayOf(36f, 2f, 45f, 2f, 45f, 22f, 36f, 22f),
        floatArrayOf(47f, 4f, 56f, 4f, 56f, 22f, 47f, 22f),
        floatArrayOf(58f, 8f, 67f, 8f, 67f, 22f, 58f, 22f),
        floatArrayOf(14f, 24f, 26f, 22f, 26f, 36f, 12f, 34f),
        floatArrayOf(28f, 46f, 68f, 46f, 68f, 50f, 28f, 50f)
    )
)

private fun hueShift(c: Color, deg: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(c.toArgb(), hsv)
    hsv[0] = ((hsv[0] + deg) % 360f + 360f) % 360f
    return Color(android.graphics.Color.HSVToColor(hsv))
}

private fun palette(s: Skin): Triple<Color, Color, Color> {
    val c = Catalog.crates[s.crateIdx]
    val shift = (s.slot % 4) * 24f
    val a = hueShift(c.c1, shift)
    val b = if (s.slot % 2 == 0) hueShift(c.c2, -shift) else lerp(c.c2, Color.Black, 0.45f)
    val acc = lerp(Color.White, s.rarity.color, 0.55f)
    return Triple(a, b, acc)
}

private fun DrawScope.drawPattern(kind: Int, sc: Float, acc: Color, dark: Color, seed: Int) {
    val rnd = Random(seed)
    val w = 100f * sc
    val h = 50f * sc
    when (kind) {
        0 -> { // çapraz şeritler
            var x = -50f
            while (x < 150f) {
                drawLine(acc.copy(alpha = 0.55f), Offset(x * sc, h), Offset((x + 30f) * sc, 0f), strokeWidth = 2.6f * sc)
                x += 9f
            }
        }
        1 -> { // kamuflaj lekeleri
            repeat(30) {
                drawCircle(if (it % 2 == 0) acc.copy(alpha = 0.5f) else dark.copy(alpha = 0.6f),
                    radius = (3f + rnd.nextFloat() * 5f) * sc, center = Offset(rnd.nextFloat() * w, rnd.nextFloat() * h))
            }
        }
        2 -> { // ızgara
            var x = 0f
            while (x < 100f) { drawLine(acc.copy(alpha = 0.4f), Offset(x * sc, 0f), Offset(x * sc, h), strokeWidth = 0.9f * sc); x += 7f }
            var y = 0f
            while (y < 50f) { drawLine(acc.copy(alpha = 0.4f), Offset(0f, y * sc), Offset(w, y * sc), strokeWidth = 0.9f * sc); y += 7f }
        }
        3 -> { // alev dilleri
            var x = -4f
            while (x < 100f) {
                val p = Path()
                p.moveTo(x * sc, h)
                p.lineTo((x + 5f) * sc, h - (12f + rnd.nextFloat() * 22f) * sc)
                p.lineTo((x + 10f) * sc, h)
                p.close()
                drawPath(p, acc.copy(alpha = 0.6f))
                x += 9f
            }
        }
        4 -> { // dalgalar
            var y = 4f
            while (y < 54f) {
                val p = Path()
                p.moveTo(0f, y * sc)
                var x = 0f
                while (x <= 100f) { p.lineTo(x * sc, (y + 3.5f * sin(x / 100f * 4f * PI).toFloat()) * sc); x += 2f }
                drawPath(p, acc.copy(alpha = 0.55f), style = Stroke(width = 1.6f * sc))
                y += 8f
            }
        }
        5 -> { // devre yolları
            repeat(16) {
                val x1 = rnd.nextFloat() * 100f; val y1 = rnd.nextFloat() * 50f
                val x2 = x1 + (rnd.nextFloat() * 30f - 8f); val y2 = y1 + (rnd.nextFloat() * 20f - 10f)
                drawLine(acc.copy(alpha = 0.7f), Offset(x1 * sc, y1 * sc), Offset(x2 * sc, y1 * sc), strokeWidth = 1.2f * sc)
                drawLine(acc.copy(alpha = 0.7f), Offset(x2 * sc, y1 * sc), Offset(x2 * sc, y2 * sc), strokeWidth = 1.2f * sc)
                drawCircle(acc, 1.7f * sc, Offset(x2 * sc, y2 * sc))
            }
        }
        6 -> { // yıldız parıltıları
            repeat(22) {
                val cx = rnd.nextFloat() * w; val cy = rnd.nextFloat() * h; val r = (1.5f + rnd.nextFloat() * 3f) * sc
                drawLine(acc, Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth = 0.9f * sc)
                drawLine(acc, Offset(cx, cy - r), Offset(cx, cy + r), strokeWidth = 0.9f * sc)
            }
        }
        7 -> { // dama
            var yi = 0
            var y = 0f
            while (y < 50f) {
                var xi = 0
                var x = 0f
                while (x < 100f) {
                    if ((xi + yi) % 2 == 0) drawRect(acc.copy(alpha = 0.35f), Offset(x * sc, y * sc), Size(8f * sc, 8f * sc))
                    x += 8f; xi++
                }
                y += 8f; yi++
            }
        }
        8 -> { // kristaller
            repeat(14) {
                val cx = rnd.nextFloat() * w; val cy = rnd.nextFloat() * h; val r = (4f + rnd.nextFloat() * 7f) * sc
                val p = Path()
                p.moveTo(cx, cy - r); p.lineTo(cx + r * 0.6f, cy + r * 0.5f); p.lineTo(cx - r * 0.6f, cy + r * 0.5f); p.close()
                drawPath(p, (if (it % 2 == 0) acc else dark).copy(alpha = 0.45f))
            }
        }
        else -> { // şevron
            var x = -6f
            while (x < 110f) {
                val p = Path()
                p.moveTo(x * sc, 0f); p.lineTo((x + 12f) * sc, 25f * sc); p.lineTo(x * sc, 50f * sc)
                drawPath(p, acc.copy(alpha = 0.5f), style = Stroke(width = 2.2f * sc))
                x += 11f
            }
        }
    }
}

fun DrawScope.drawSkin(skin: Skin) {
    val sc = size.width / 100f
    val path = Path()
    for (poly in shapes.getValue(skin.weapon.cat)) {
        path.moveTo(poly[0] * sc, poly[1] * sc)
        var i = 2
        while (i < poly.size) { path.lineTo(poly[i] * sc, poly[i + 1] * sc); i += 2 }
        path.close()
    }
    val (a, b, acc) = palette(skin)
    drawCircle(
        Brush.radialGradient(listOf(skin.rarity.color.copy(alpha = 0.28f), Color.Transparent), center = center, radius = size.width * 0.5f),
        radius = size.width * 0.5f, center = center
    )
    clipPath(path) {
        drawRect(Brush.linearGradient(listOf(a, b), start = Offset.Zero, end = Offset(size.width, size.height)))
        drawPattern(skin.pattern, sc, acc, b, skin.id.hashCode())
    }
    drawPath(path, skin.rarity.color.copy(alpha = 0.9f), style = Stroke(width = 1.1f * sc))
}

@Composable
fun SkinArt(skin: Skin, modifier: Modifier = Modifier) {
    Canvas(modifier.aspectRatio(2f)) { drawSkin(skin) }
}

@Composable
fun CrateArt(c: Crate, modifier: Modifier = Modifier) {
    Canvas(modifier.aspectRatio(1.2f)) {
        val w = size.width
        val h = size.height
        drawCircle(
            Brush.radialGradient(listOf(c.c1.copy(alpha = 0.35f), Color.Transparent), center = Offset(w / 2, h / 2), radius = w * 0.5f),
            radius = w * 0.5f, center = Offset(w / 2, h / 2)
        )
        val bx = w * 0.18f; val bw = w * 0.64f; val by = h * 0.38f; val bh = h * 0.46f
        drawRoundRect(Brush.verticalGradient(listOf(c.c1, c.c2), startY = by, endY = by + bh), Offset(bx, by), Size(bw, bh), CornerRadius(w * 0.04f))
        drawRoundRect(Brush.verticalGradient(listOf(lerp(c.c1, Color.White, 0.3f), c.c1), startY = h * 0.24f, endY = h * 0.42f),
            Offset(bx - w * 0.02f, h * 0.24f), Size(bw + w * 0.04f, h * 0.18f), CornerRadius(w * 0.04f))
        drawRect(Color(0x66000000), Offset(w / 2 - w * 0.04f, h * 0.24f), Size(w * 0.08f, bh + h * 0.14f))
        drawCircle(Color(0xFFFFC93C), w * 0.05f, Offset(w / 2, h * 0.52f))
        drawCircle(Color(0xFF7A5A00), w * 0.02f, Offset(w / 2, h * 0.52f))
        drawRoundRect(Color(0xCCC9D1DC), Offset(bx, by), Size(bw, bh), CornerRadius(w * 0.04f), style = Stroke(w * 0.008f))
        drawRoundRect(Color(0xCCC9D1DC), Offset(bx - w * 0.02f, h * 0.24f), Size(bw + w * 0.04f, h * 0.18f), CornerRadius(w * 0.04f), style = Stroke(w * 0.008f))
    }
}
