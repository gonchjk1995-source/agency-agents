package com.neon.player.ui.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neon.player.ui.theme.Hud
import com.neon.player.ui.theme.Neon
import com.neon.player.ui.theme.glow
import com.neon.player.util.formatTime

@Composable
fun NeonSeekBar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val duration by rememberUpdatedState(durationMs)
    val seek by rememberUpdatedState(onSeek)
    val played = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val fraction = dragFraction ?: played
    val gradient = Brush.horizontalGradient(listOf(Neon.Cyan, Neon.Violet, Neon.Magenta))

    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(36.dp)
                .semantics {
                    contentDescription = "Позиция трека"
                    progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                    setProgress { target ->
                        seek((target.coerceIn(0f, 1f) * duration).toLong())
                        true
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (duration > 0) seek(((offset.x / size.width).coerceIn(0f, 1f) * duration).toLong())
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragFraction = (it.x / size.width).coerceIn(0f, 1f) },
                        onDragEnd = {
                            dragFraction?.let { if (duration > 0) seek((it * duration).toLong()) }
                            dragFraction = null
                        },
                        onDragCancel = { dragFraction = null },
                    ) { change, _ ->
                        change.consume()
                        dragFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                    }
                }
                .drawBehind {
                    val y = size.height / 2
                    val x = size.width * fraction
                    val track = 3.dp.toPx()
                    drawLine(Neon.TextHi.copy(alpha = 0.12f), Offset(0f, y), Offset(size.width, y), track, StrokeCap.Round)
                    // HUD ticks every 5 %
                    for (i in 0..20) {
                        val tx = size.width * i / 20f
                        val h = if (i % 5 == 0) 7.dp.toPx() else 3.dp.toPx()
                        drawLine(Neon.TextLo.copy(alpha = 0.35f), Offset(tx, y + 6.dp.toPx()), Offset(tx, y + 6.dp.toPx() + h), 1f)
                    }
                    if (x > 0f) {
                        drawLine(gradient, Offset(0f, y), Offset(x, y), track * 5, StrokeCap.Round, alpha = 0.18f)
                        drawLine(gradient, Offset(0f, y), Offset(x, y), track, StrokeCap.Round)
                    }
                    val r = (if (dragFraction != null) 9 else 7).dp.toPx()
                    drawCircle(Neon.Magenta.copy(alpha = 0.35f), radius = r * 2.2f, center = Offset(x, y))
                    val diamond = Path().apply {
                        moveTo(x, y - r); lineTo(x + r, y); lineTo(x, y + r); lineTo(x - r, y); close()
                    }
                    drawPath(diamond, Neon.TextHi)
                },
        )
        val shownMs = dragFraction?.let { (it * durationMs).toLong() } ?: positionMs
        Row(Modifier.fillMaxWidth()) {
            Text(formatTime(shownMs), style = Hud.copy(fontSize = 12.sp, letterSpacing = 1.sp, color = Neon.Cyan).glow(Neon.Cyan, 12f))
            Spacer(Modifier.weight(1f))
            Text("-" + formatTime(durationMs - shownMs), style = Hud.copy(fontSize = 12.sp, letterSpacing = 1.sp))
        }
    }
}
