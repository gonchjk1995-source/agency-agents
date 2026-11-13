package com.neon.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neon.player.ui.theme.Neon

val NeonBorderBrush = Brush.linearGradient(listOf(Neon.Cyan, Neon.Violet, Neon.Magenta))

/** Outline with a layered bloom so it reads as a lit tube rather than a flat stroke. */
fun Modifier.neonBorder(
    shape: Shape,
    brush: Brush = NeonBorderBrush,
    width: Dp = 1.2.dp,
    glow: Float = 1f,
): Modifier = drawWithCache {
    val path = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache)) }
    val w = width.toPx()
    onDrawWithContent {
        drawContent()
        if (glow > 0f) {
            drawPath(path, brush, alpha = 0.10f * glow, style = Stroke(w * 7))
            drawPath(path, brush, alpha = 0.22f * glow, style = Stroke(w * 3.5f))
        }
        drawPath(path, brush, style = Stroke(w))
    }
}

/** Dark translucent glass panel with a neon edge. */
fun Modifier.glassPanel(shape: Shape = Neon.Chamfer, glow: Float = 0.7f): Modifier =
    this
        .background(
            Brush.linearGradient(listOf(Neon.Panel, Neon.Deep.copy(alpha = 0.75f))),
            shape,
        )
        .neonBorder(shape, glow = glow)
