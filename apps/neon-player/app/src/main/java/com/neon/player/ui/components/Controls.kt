package com.neon.player.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.neon.player.ui.theme.Neon
import com.neon.player.ui.theme.NeonIcons
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

val HexagonShape = GenericShape { size: Size, _: LayoutDirection ->
    val r = min(size.width, size.height) / 2
    val cx = size.width / 2
    val cy = size.height / 2
    for (i in 0 until 6) {
        val a = PI / 3 * i - PI / 2
        val x = cx + r * cos(a).toFloat()
        val y = cy + r * sin(a).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

@Composable
fun NeonIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
) {
    val tint = if (active) Neon.Cyan else Neon.TextHi.copy(alpha = 0.85f)
    Box(
        modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2, color = Neon.Cyan),
                role = Role.Button,
                onClickLabel = contentDescription,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(iconSize))
        if (active) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-4).dp)
                    .size(4.dp)
                    .background(Neon.Cyan, CircleShape),
            )
        }
    }
}

/** Hexagonal reactor-core play button; its halo breathes and spins while music plays. */
@Composable
fun PlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 84.dp,
) {
    val transition = rememberInfiniteTransition(label = "core")
    val pulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse",
    )
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing)),
        label = "spin",
    )
    val live by animateFloatAsState(if (isPlaying) 1f else 0f, label = "live")
    val fill = Brush.linearGradient(listOf(Neon.Cyan, Neon.Violet, Neon.Magenta))
    val label = if (isPlaying) "Пауза" else "Играть"

    Box(
        modifier
            .size(size)
            .drawWithCache {
                val hex = Path().apply { addOutline(HexagonShape.createOutline(this@drawWithCache.size, layoutDirection, this@drawWithCache)) }
                val ring = this.size.minDimension / 2 * 1.22f
                onDrawBehind {
                    val glow = 0.25f + 0.35f * live * pulse
                    drawPath(hex, fill, alpha = glow * 0.5f, style = Stroke(26f))
                    drawPath(hex, fill, alpha = glow, style = Stroke(10f))
                    rotate(spin * live) {
                        for (i in 0 until 3) {
                            drawArc(
                                fill,
                                startAngle = i * 120f,
                                sweepAngle = 70f,
                                useCenter = false,
                                topLeft = center - androidx.compose.ui.geometry.Offset(ring, ring),
                                size = Size(ring * 2, ring * 2),
                                alpha = 0.35f + 0.5f * live,
                                style = Stroke(2.5f),
                            )
                        }
                    }
                }
            }
            .background(fill, HexagonShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2, color = Neon.TextHi),
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (isPlaying) NeonIcons.Pause else NeonIcons.Play,
            contentDescription = label,
            tint = Neon.Void,
            modifier = Modifier.size(size * 0.38f),
        )
    }
}
