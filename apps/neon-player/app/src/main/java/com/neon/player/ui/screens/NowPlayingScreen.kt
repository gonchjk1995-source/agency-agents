package com.neon.player.ui.screens

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neon.player.PlaybackState
import com.neon.player.RepeatMode
import com.neon.player.ui.components.CyberBackground
import com.neon.player.ui.components.NeonIconButton
import com.neon.player.ui.components.NeonSeekBar
import com.neon.player.ui.components.PlayButton
import com.neon.player.ui.components.ReactorVisualizer
import com.neon.player.ui.components.bassEnergy
import com.neon.player.ui.components.rememberSpectrum
import com.neon.player.ui.theme.Hud
import com.neon.player.ui.theme.Neon
import com.neon.player.ui.theme.NeonIcons
import com.neon.player.ui.theme.glow

@Composable
fun NowPlayingScreen(
    state: PlaybackState,
    onClose: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
) {
    val spectrum by rememberSpectrum()
    val track = state.current

    CyberBackground(
        energy = spectrum.bassEnergy(),
        // Swallow taps so nothing reaches the library underneath.
        modifier = Modifier.pointerInput(Unit) { detectTapGestures { } },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NeonIconButton(NeonIcons.ChevronDown, "Свернуть плеер", onClose)
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("СЕЙЧАС ИГРАЕТ", style = Hud.copy(fontSize = 11.sp, letterSpacing = 4.sp, color = Neon.Cyan).glow(Neon.Cyan, 12f))
                    if (state.queueSize > 0) {
                        Text(
                            "TRACK %02d / %02d".format(state.queueIndex + 1, state.queueSize),
                            style = Hud.copy(fontSize = 9.sp),
                        )
                    }
                }
                Spacer(Modifier.size(48.dp))
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                ReactorVisualizer(
                    bands = spectrum,
                    isPlaying = state.isPlaying,
                    track = track,
                    modifier = Modifier.widthIn(max = 440.dp),
                )
            }

            Text(
                track?.title.orEmpty(),
                style = MaterialTheme.typography.headlineSmall.copy(color = Neon.TextHi).glow(Neon.Cyan, 22f),
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.basicMarquee(),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                track?.artist.orEmpty(),
                style = MaterialTheme.typography.titleMedium.copy(color = Neon.Magenta).glow(Neon.Magenta, 14f),
                maxLines = 1,
            )
            Text(
                track?.album.orEmpty(),
                style = MaterialTheme.typography.bodySmall.copy(color = Neon.TextLo),
                maxLines = 1,
            )
            Spacer(Modifier.height(20.dp))
            NeonSeekBar(
                positionMs = state.positionMs,
                durationMs = state.durationMs,
                onSeek = onSeek,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NeonIconButton(
                    NeonIcons.Shuffle,
                    if (state.shuffle) "Перемешивание включено" else "Перемешивание выключено",
                    onToggleShuffle,
                    active = state.shuffle,
                )
                NeonIconButton(NeonIcons.Previous, "Предыдущий трек", onPrevious, size = 56.dp, iconSize = 28.dp)
                PlayButton(isPlaying = state.isPlaying, onClick = onTogglePlay)
                NeonIconButton(NeonIcons.Next, "Следующий трек", onNext, size = 56.dp, iconSize = 28.dp)
                NeonIconButton(
                    if (state.repeat == RepeatMode.ONE) NeonIcons.RepeatOne else NeonIcons.Repeat,
                    when (state.repeat) {
                        RepeatMode.OFF -> "Повтор выключен"
                        RepeatMode.ALL -> "Повтор всех треков"
                        RepeatMode.ONE -> "Повтор одного трека"
                    },
                    onCycleRepeat,
                    active = state.repeat != RepeatMode.OFF,
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
