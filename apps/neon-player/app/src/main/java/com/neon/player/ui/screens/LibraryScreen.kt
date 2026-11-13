package com.neon.player.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neon.player.LibraryState
import com.neon.player.PlaybackState
import com.neon.player.data.Track
import com.neon.player.ui.components.CyberBackground
import com.neon.player.ui.components.GlitchText
import com.neon.player.ui.components.MiniPlayer
import com.neon.player.ui.components.NeonIconButton
import com.neon.player.ui.components.TrackRow
import com.neon.player.ui.components.glassPanel
import com.neon.player.ui.components.neonBorder
import com.neon.player.ui.theme.Hud
import com.neon.player.ui.theme.Neon
import com.neon.player.ui.theme.NeonIcons
import com.neon.player.ui.theme.Orbitron
import com.neon.player.ui.theme.glow
import com.neon.player.util.formatTime
import com.neon.player.util.tracksLabel

@Composable
fun LibraryScreen(
    library: LibraryState,
    tracks: List<Track>,
    query: String,
    onQueryChange: (String) -> Unit,
    playback: PlaybackState,
    onPlay: (Track) -> Unit,
    onShuffleAll: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onOpenPlayer: () -> Unit,
) {
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val hasMini = playback.current != null

    CyberBackground(dim = true) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = top + 12.dp, bottom = bottom + if (hasMini) 112.dp else 24.dp),
        ) {
            item(key = "header") { Header(library) }
            item(key = "search") { SearchField(query, onQueryChange) }
            item(key = "actions") {
                ActionsRow(
                    enabled = tracks.isNotEmpty(),
                    totalMs = tracks.sumOf { it.durationMs },
                    shown = tracks.size,
                    onShuffleAll = onShuffleAll,
                )
            }
            when {
                library.loading -> item(key = "scan") { Scanning() }
                tracks.isEmpty() -> item(key = "empty") { EmptyState(query) }
                else -> itemsIndexed(tracks, key = { _, t -> t.id }) { i, t ->
                    TrackRow(
                        index = i,
                        track = t,
                        isCurrent = playback.current?.id == t.id,
                        isPlaying = playback.isPlaying,
                        onClick = { onPlay(t) },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = hasMini,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            MiniPlayer(
                state = playback,
                onTogglePlay = onTogglePlay,
                onNext = onNext,
                onOpen = onOpenPlayer,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun Header(library: LibraryState) {
    val transition = rememberInfiniteTransition(label = "online")
    val blink by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "blink",
    )
    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text("◢ SYS.AUDIO // NODE-01", style = Hud)
        Spacer(Modifier.height(6.dp))
        GlitchText(
            "NEON//PLAYER",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = Orbitron,
                fontWeight = FontWeight.Black,
                fontSize = 30.sp,
                letterSpacing = 2.sp,
            ),
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(7.dp)
                    .alpha(blink)
                    .background(Neon.Cyan, CircleShape),
            )
            Spacer(Modifier.width(8.dp))
            Text("ONLINE", style = Hud.copy(color = Neon.Cyan).glow(Neon.Cyan, 10f))
            Spacer(Modifier.width(12.dp))
            Text(
                if (library.loading) "Медиатека загружается" else "Медиатека · ${tracksLabel(library.tracks.size)}",
                style = MaterialTheme.typography.bodySmall.copy(color = Neon.TextLo),
            )
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val focus = LocalFocusManager.current
    Row(
        Modifier
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .fillMaxWidth()
            .height(50.dp)
            .glassPanel(Neon.ChamferSmall, glow = 0.4f)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(NeonIcons.Search, contentDescription = null, tint = Neon.Cyan, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(
                    "Поиск: трек, артист, альбом",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Neon.TextLo),
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Neon.TextHi),
                cursorBrush = SolidColor(Neon.Cyan),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            NeonIconButton(NeonIcons.Close, "Очистить поиск", { onQueryChange("") }, size = 36.dp, iconSize = 18.dp)
        }
    }
}

@Composable
private fun ActionsRow(enabled: Boolean, totalMs: Long, shown: Int, onShuffleAll: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            Modifier
                .alpha(if (enabled) 1f else 0.4f)
                .background(
                    Brush.horizontalGradient(listOf(Neon.Cyan.copy(alpha = 0.18f), Neon.Magenta.copy(alpha = 0.18f))),
                    Neon.ChamferSmall,
                )
                .neonBorder(Neon.ChamferSmall, glow = 0.8f)
                .clickable(enabled = enabled, role = Role.Button, onClick = onShuffleAll)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(NeonIcons.Shuffle, contentDescription = null, tint = Neon.TextHi, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text("ПЕРЕМЕШАТЬ ВСЁ", style = MaterialTheme.typography.labelLarge.copy(color = Neon.TextHi))
        }
        if (shown > 0) {
            Column(horizontalAlignment = Alignment.End) {
                Text("ВРЕМЯ", style = Hud.copy(fontSize = 9.sp))
                Text(formatTime(totalMs), style = Hud.copy(fontSize = 13.sp, letterSpacing = 1.sp, color = Neon.Magenta).glow(Neon.Magenta, 10f))
            }
        }
    }
}

@Composable
private fun Scanning() {
    val transition = rememberInfiniteTransition(label = "scan")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing)),
        label = "sweep",
    )
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(4.dp),
        ) {
            drawLine(Neon.TextHi.copy(alpha = 0.08f), Offset(0f, center.y), Offset(size.width, center.y), size.height)
            val x = size.width * sweep
            drawLine(
                Brush.horizontalGradient(listOf(Color.Transparent, Neon.Cyan, Color.Transparent), startX = x - 160f, endX = x + 160f),
                Offset((x - 160f).coerceAtLeast(0f), center.y),
                Offset((x + 160f).coerceAtMost(size.width), center.y),
                size.height,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text("СКАНИРОВАНИЕ МЕДИАТЕКИ…", style = Hud.copy(color = Neon.Cyan))
    }
}

@Composable
private fun EmptyState(query: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(NeonIcons.Note, contentDescription = null, tint = Neon.Magenta, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            if (query.isBlank()) "Сигнал не найден" else "Ничего не найдено",
            style = MaterialTheme.typography.titleLarge.copy(color = Neon.TextHi),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (query.isBlank()) {
                "На устройстве нет музыки. Скопируйте аудиофайлы, например в папку Music, и вернитесь сюда."
            } else {
                "По запросу «${query.trim()}» нет треков, артистов или альбомов."
            },
            style = MaterialTheme.typography.bodyMedium.copy(color = Neon.TextLo),
            textAlign = TextAlign.Center,
        )
    }
}
