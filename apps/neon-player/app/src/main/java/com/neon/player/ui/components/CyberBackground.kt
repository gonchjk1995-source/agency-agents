package com.neon.player.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import com.neon.player.ui.theme.Neon
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

private class Star(val x: Float, val y: Float, val size: Float, val phase: Float)

/**
 * Synthwave backdrop: star field, striped sun on the horizon, a perspective grid
 * scrolling towards the viewer and CRT scanlines. [energy] (0..1) pulses the glow.
 */
@Composable
fun CyberBackground(
    modifier: Modifier = Modifier,
    dim: Boolean = false,
    energy: Float = 0f,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val transition = rememberInfiniteTransition(label = "bg")
    val scroll by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "grid",
    )
    val twinkle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "stars",
    )
    val stars = remember {
        val rnd = Random(7)
        List(80) { Star(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat() * 1.6f + 0.5f, rnd.nextFloat()) }
    }
    val strength = if (dim) 0.55f else 1f

    Box(
        modifier
            .fillMaxSize()
            .drawBehind {
                val horizon = size.height * if (dim) 0.70f else 0.64f
                drawSky(horizon)
                drawStars(stars, horizon, twinkle, strength)
                drawSun(horizon, strength, energy, disk = !dim)
                drawGrid(horizon, scroll, strength, energy)
                drawScanlines()
            },
        content = content,
    )
}

private fun DrawScope.drawSky(horizon: Float) {
    drawRect(
        Brush.verticalGradient(
            0f to Neon.Void,
            0.55f to Neon.Abyss,
            1f to Neon.Deep,
            endY = horizon,
        ),
        size = Size(size.width, horizon),
    )
    drawRect(
        Brush.verticalGradient(listOf(Color(0xFF12062B), Neon.Void), startY = horizon, endY = size.height),
        topLeft = Offset(0f, horizon),
        size = Size(size.width, size.height - horizon),
    )
}

private fun DrawScope.drawStars(stars: List<Star>, horizon: Float, twinkle: Float, strength: Float) {
    stars.forEach { s ->
        val y = s.y * horizon * 0.9f
        val a = (0.35f + 0.65f * (0.5f + 0.5f * sin((twinkle + s.phase) * 2 * PI.toFloat() * 2))) * strength
        drawCircle(Neon.TextHi.copy(alpha = a * 0.8f), radius = s.size, center = Offset(s.x * size.width, y))
    }
}

private fun DrawScope.drawSun(horizon: Float, strength: Float, energy: Float, disk: Boolean) {
    val r = size.width * 0.30f
    val center = Offset(size.width / 2, horizon - r * 0.15f)
    val glow = (0.30f + 0.35f * energy) * strength
    drawCircle(
        Brush.radialGradient(
            listOf(Neon.Magenta.copy(alpha = glow), Color.Transparent),
            center = center,
            radius = r * 2.4f,
        ),
        radius = r * 2.4f,
        center = center,
    )
    // Behind a text-heavy list the disk hurts legibility; keep only its glow there.
    if (!disk) return
    clipRect(bottom = horizon) {
        drawCircle(
            Brush.verticalGradient(
                listOf(Neon.Amber.copy(alpha = 0.85f * strength), Neon.Magenta.copy(alpha = 0.75f * strength), Neon.Violet.copy(alpha = 0.4f * strength)),
                startY = center.y - r,
                endY = center.y + r * 0.4f,
            ),
            radius = r,
            center = center,
        )
        // Classic sun slits, wider towards the horizon.
        var y = center.y - r * 0.15f
        var gap = 2f
        while (y < horizon) {
            drawRect(Color(0xFF12062B), topLeft = Offset(0f, y), size = Size(size.width, gap))
            y += gap + 10f
            gap += 2.2f
        }
    }
}

private fun DrawScope.drawGrid(horizon: Float, scroll: Float, strength: Float, energy: Float) {
    val floor = size.height - horizon
    val lineAlpha = (0.55f + 0.3f * energy) * strength
    val rows = 14
    for (i in 0 until rows) {
        val p = ((i + scroll) / rows).pow(2.1f)
        val y = horizon + floor * p
        drawLine(
            Neon.Magenta.copy(alpha = lineAlpha * (0.15f + 0.85f * p)),
            Offset(0f, y),
            Offset(size.width, y),
            strokeWidth = 1f + 2f * p,
        )
    }
    val cx = size.width / 2
    val cols = 11
    for (k in -cols..cols) {
        val bottomX = cx + k * size.width * 0.16f
        drawLine(
            Brush.verticalGradient(
                listOf(Color.Transparent, Neon.Cyan.copy(alpha = lineAlpha * 0.55f)),
                startY = horizon,
                endY = size.height,
            ),
            Offset(cx + k * size.width * 0.012f, horizon),
            Offset(bottomX, size.height),
            strokeWidth = 1.4f,
        )
    }
    // Horizon line with a soft bloom.
    drawLine(Neon.Cyan.copy(alpha = 0.18f * strength), Offset(0f, horizon), Offset(size.width, horizon), strokeWidth = 14f)
    drawLine(Neon.Cyan.copy(alpha = 0.8f * strength), Offset(0f, horizon), Offset(size.width, horizon), strokeWidth = 2f)
}

private fun DrawScope.drawScanlines() {
    val period = 3f * density
    drawRect(
        Brush.verticalGradient(
            0f to Color.Transparent,
            0.5f to Color.Transparent,
            0.5f to Color.Black.copy(alpha = 0.16f),
            1f to Color.Black.copy(alpha = 0.16f),
            startY = 0f,
            endY = period,
            tileMode = TileMode.Repeated,
        ),
    )
}

