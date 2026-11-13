package com.neon.player.ui.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.neon.player.PlaybackState
import com.neon.player.ui.theme.Neon
import com.neon.player.ui.theme.NeonIcons

@Composable
fun MiniPlayer(
    state: PlaybackState,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = state.current ?: return
    val progress = if (state.durationMs > 0) (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f) else 0f
    val gradient = Brush.horizontalGradient(listOf(Neon.Cyan, Neon.Violet, Neon.Magenta))

    Row(
        modifier
            .fillMaxWidth()
            .height(76.dp)
            .glassPanel(Neon.Chamfer, glow = 0.9f)
            .drawWithContent {
                drawContent()
                if (progress > 0f) {
                    // Progress runs along the top edge, starting after the chamfer.
                    val y = 1.5f * density
                    val startX = 16f * density
                    val end = Offset(startX + (size.width - startX) * progress, y)
                    drawLine(gradient, Offset(startX, y), end, 6f * density, alpha = 0.25f)
                    drawLine(gradient, Offset(startX, y), end, 2.5f * density)
                }
            }
            .semantics { onClick(label = "Открыть плеер") { onOpen(); true } }
            .clickable(onClick = onOpen)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlbumArt(track, Neon.ChamferSmall, Modifier.size(52.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                style = MaterialTheme.typography.titleMedium.copy(color = Neon.TextHi),
                maxLines = 1,
                modifier = Modifier.basicMarquee(),
            )
            Text(
                track.artist,
                style = MaterialTheme.typography.bodySmall.copy(color = Neon.Magenta.copy(alpha = 0.9f)),
                maxLines = 1,
            )
        }
        PlayButton(isPlaying = state.isPlaying, onClick = onTogglePlay, size = 44.dp, modifier = Modifier.padding(horizontal = 8.dp))
        NeonIconButton(NeonIcons.Next, "Следующий трек", onNext, size = 40.dp, iconSize = 20.dp)
    }
}
