package com.neon.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.neon.player.data.Track
import com.neon.player.ui.theme.Neon
import com.neon.player.ui.theme.NeonIcons
import kotlin.math.abs

private val palettes = listOf(
    listOf(Neon.Cyan, Neon.Violet),
    listOf(Neon.Magenta, Neon.Violet),
    listOf(Neon.Violet, Neon.Cyan),
    listOf(Neon.Amber, Neon.Magenta),
)

/** Cover art, or a neon gradient with a note glyph when the file has none. */
@Composable
fun AlbumArt(track: Track?, shape: Shape, modifier: Modifier = Modifier) {
    val colors = remember(track?.albumId, track?.title) {
        palettes[abs((track?.album ?: "").hashCode()) % palettes.size]
    }
    val context = LocalContext.current
    Box(
        modifier
            .clip(shape)
            .background(Brush.linearGradient(listOf(colors[0].copy(alpha = 0.55f), Neon.Void, colors[1].copy(alpha = 0.6f)))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            NeonIcons.Note,
            contentDescription = null,
            tint = Neon.TextHi.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxSize(0.42f),
        )
        if (track != null) {
            AsyncImage(
                model = remember(track.artUri) {
                    ImageRequest.Builder(context).data(track.artUri).crossfade(250).build()
                },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
