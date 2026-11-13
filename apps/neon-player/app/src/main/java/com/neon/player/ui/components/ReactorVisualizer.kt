package com.neon.player.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import com.neon.player.data.Track
import com.neon.player.ui.theme.Neon
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * The "reactor": cover art spinning like a record inside a ring of mirrored
 * spectrum bars, wrapped in rotating HUD rings.
 */
@Composable
fun ReactorVisualizer(
    bands: FloatArray,
    isPlaying: Boolean,
    track: Track?,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "reactor")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(30_000, easing = LinearEasing)),
        label = "spin",
    )
    var artAngle by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                artAngle = (artAngle + (now - last) / 1_000_000_000f * 16f) % 360f
                last = now
            }
        }
    }
    val energy = bands.bassEnergy()
    val sweep = remember { Brush.sweepGradient(listOf(Neon.Cyan, Neon.Violet, Neon.Magenta, Neon.Violet, Neon.Cyan)) }

    Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            val artR = radius * 0.52f
            drawCircle(
                Brush.radialGradient(
                    listOf(Neon.Magenta.copy(alpha = 0.18f + 0.4f * energy), Color.Transparent),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
            )
            drawBars(bands, artR + radius * 0.07f, radius, spin, sweep)
            drawCircle(Neon.Cyan.copy(alpha = 0.7f), radius = artR + radius * 0.035f, style = Stroke(1.5f * density))
            drawCircle(Neon.Cyan.copy(alpha = 0.15f), radius = artR + radius * 0.035f, style = Stroke(8f * density))
            drawHud(radius, spin)
        }
        AlbumArt(
            track = track,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxSize(0.52f)
                .graphicsLayer { rotationZ = artAngle },
        )
    }
}

private fun DrawScope.drawBars(bands: FloatArray, inner: Float, radius: Float, spin: Float, brush: Brush) {
    val n = bands.size
    if (n == 0) return
    val total = n * 2
    val step = 2 * PI / total
    val barWidth = (2 * PI * inner / total).toFloat() * 0.55f
    val maxLen = radius * 0.86f - inner
    val idlePhase = spin / 360f * 2 * PI * 6
    for (i in 0 until total) {
        // Mirror so bass meets at the top and treble at the bottom.
        val band = if (i < n) i else total - 1 - i
        val idle = 0.035f + 0.025f * sin(idlePhase + i * 0.45).toFloat()
        // A gentle curve keeps loud passages from flattening into a uniform ring.
        val v = maxOf(bands[band].pow(1.6f), idle)
        val len = radius * 0.02f + v * maxLen
        val a = -PI / 2 + i * step
        val dx = cos(a).toFloat()
        val dy = sin(a).toFloat()
        val start = Offset(center.x + dx * inner, center.y + dy * inner)
        val end = Offset(center.x + dx * (inner + len), center.y + dy * (inner + len))
        drawLine(brush, start, end, strokeWidth = barWidth * 2.6f, cap = StrokeCap.Round, alpha = 0.18f)
        drawLine(brush, start, end, strokeWidth = barWidth, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawHud(radius: Float, spin: Float) {
    val dashR = radius * 0.90f
    rotate(spin) {
        for (k in 0 until 24) {
            drawArc(
                Neon.Cyan.copy(alpha = 0.35f),
                startAngle = k * 15f,
                sweepAngle = 8f,
                useCenter = false,
                topLeft = center - Offset(dashR, dashR),
                size = Size(dashR * 2, dashR * 2),
                style = Stroke(1.4f * density),
            )
        }
    }
    val tickR = radius * 0.985f
    rotate(-spin * 0.5f) {
        for (k in 0 until 72) {
            val a = k * 5.0 * PI / 180
            val long = k % 6 == 0
            val len = if (long) radius * 0.05f else radius * 0.02f
            val dx = cos(a).toFloat()
            val dy = sin(a).toFloat()
            drawLine(
                (if (long) Neon.Magenta else Neon.TextLo).copy(alpha = if (long) 0.9f else 0.4f),
                Offset(center.x + dx * tickR, center.y + dy * tickR),
                Offset(center.x + dx * (tickR - len), center.y + dy * (tickR - len)),
                strokeWidth = 1.2f * density,
            )
        }
    }
    val orbitR = radius * 0.945f
    rotate(-spin * 2f) {
        drawArc(
            Neon.Magenta,
            startAngle = 0f,
            sweepAngle = 64f,
            useCenter = false,
            topLeft = center - Offset(orbitR, orbitR),
            size = Size(orbitR * 2, orbitR * 2),
            style = Stroke(2.2f * density, cap = StrokeCap.Round),
        )
        val end = 64.0 * PI / 180
        val dot = Offset(center.x + cos(end).toFloat() * orbitR, center.y + sin(end).toFloat() * orbitR)
        drawCircle(Neon.Magenta.copy(alpha = 0.35f), radius = 7f * density, center = dot)
        drawCircle(Neon.TextHi, radius = 2.6f * density, center = dot)
    }
    rotate(spin * 1.4f + 180f) {
        drawArc(
            Neon.Cyan,
            startAngle = 0f,
            sweepAngle = 38f,
            useCenter = false,
            topLeft = center - Offset(orbitR, orbitR),
            size = Size(orbitR * 2, orbitR * 2),
            style = Stroke(2.2f * density, cap = StrokeCap.Round),
        )
    }
}
