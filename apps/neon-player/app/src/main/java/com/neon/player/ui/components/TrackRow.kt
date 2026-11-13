package com.neon.player.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neon.player.data.Track
import com.neon.player.ui.theme.Hud
import com.neon.player.ui.theme.Neon
import com.neon.player.ui.theme.glow
import com.neon.player.util.formatTime

@Composable
fun TrackRow(
    index: Int,
    track: Track,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(
                if (isCurrent) {
                    Brush.horizontalGradient(listOf(Neon.Cyan.copy(alpha = 0.14f), Color.Transparent))
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                },
            )
            .clickable(onClick = onClick)
            .padding(start = 0.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(width = 3.dp, height = 40.dp)
                .background(if (isCurrent) Neon.Cyan else Color.Transparent),
        )
        Text(
            "%03d".format(index + 1),
            style = Hud.copy(
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = if (isCurrent) Neon.Cyan else Neon.TextLo.copy(alpha = 0.6f),
            ),
            modifier = Modifier
                .padding(start = 14.dp)
                .width(34.dp),
        )
        AlbumArt(
            track = track,
            shape = Neon.ChamferSmall,
            modifier = Modifier
                .size(48.dp)
                .then(if (isCurrent) Modifier.neonBorder(Neon.ChamferSmall, glow = 0.8f) else Modifier),
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                style = MaterialTheme.typography.titleMedium.let {
                    if (isCurrent) it.copy(color = Neon.Cyan).glow(Neon.Cyan, 14f) else it.copy(color = Neon.TextHi)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${track.artist} · ${track.album}",
                style = MaterialTheme.typography.bodySmall.copy(color = Neon.TextLo),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(12.dp))
        if (isCurrent) {
            EqualizerBars(active = isPlaying, modifier = Modifier.size(18.dp))
        } else {
            Text(formatTime(track.durationMs), style = Hud.copy(fontSize = 11.sp, letterSpacing = 1.sp))
        }
    }
}

/** Small bouncing bars marking the track that is playing. */
@Composable
fun EqualizerBars(active: Boolean, modifier: Modifier = Modifier, color: Color = Neon.Cyan) {
    val transition = rememberInfiniteTransition(label = "eq")
    val heights = listOf(520, 380, 610, 450).mapIndexed { i, ms ->
        val h by transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(ms), RepeatMode.Reverse),
            label = "bar$i",
        )
        h
    }
    Canvas(modifier) {
        val gap = size.width * 0.12f
        val w = (size.width - gap * 3) / 4
        heights.forEachIndexed { i, h ->
            val v = if (active) h else if (i % 2 == 0) 0.3f else 0.45f
            val barH = size.height * v
            drawRoundRect(
                color,
                topLeft = Offset(i * (w + gap), size.height - barH),
                size = Size(w, barH),
                cornerRadius = CornerRadius(w / 3),
            )
        }
    }
}
