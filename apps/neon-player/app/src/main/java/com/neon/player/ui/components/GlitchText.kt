package com.neon.player.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import com.neon.player.ui.theme.Neon
import com.neon.player.ui.theme.glow
import kotlinx.coroutines.delay
import kotlin.random.Random

/** Text with a constant RGB split that occasionally jitters like a bad signal. */
@Composable
fun GlitchText(text: String, style: TextStyle, modifier: Modifier = Modifier) {
    var jitter by remember { mutableFloatStateOf(0f) }
    var slice by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(2200, 5200))
            repeat(5) {
                jitter = Random.nextFloat() * 10f - 5f
                slice = Random.nextFloat() * 4f - 2f
                delay(45)
            }
            jitter = 0f
            slice = 0f
        }
    }
    Box(modifier.semantics { contentDescription = text }) {
        Text(
            text,
            style = style.copy(color = Neon.Magenta.copy(alpha = 0.85f)),
            modifier = Modifier
                .clearAndSetSemantics { }
                .graphicsLayer { translationX = -2.5f * density - jitter; translationY = slice },
        )
        Text(
            text,
            style = style.copy(color = Neon.Cyan.copy(alpha = 0.85f)),
            modifier = Modifier
                .clearAndSetSemantics { }
                .graphicsLayer { translationX = 2.5f * density + jitter; translationY = -slice },
        )
        Text(
            text,
            style = style.copy(color = Neon.TextHi).glow(Neon.Cyan, 24f),
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}
