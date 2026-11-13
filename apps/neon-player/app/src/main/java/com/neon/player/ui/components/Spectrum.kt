package com.neon.player.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import com.neon.player.audio.SpectrumAnalyzer

/**
 * Per-frame smoothed copy of the analyzer output: fast attack, slow release,
 * so bars snap up on a beat and fall back gracefully. Stops updating at rest.
 */
@Composable
fun rememberSpectrum(): State<FloatArray> {
    val state = remember { mutableStateOf(FloatArray(SpectrumAnalyzer.BANDS)) }
    LaunchedEffect(Unit) {
        val current = FloatArray(SpectrumAnalyzer.BANDS)
        while (true) {
            withFrameNanos { }
            val target = SpectrumAnalyzer.bands.value
            var moving = false
            for (i in current.indices) {
                val t = target.getOrElse(i) { 0f }
                val c = current[i]
                current[i] = if (t > c) c + (t - c) * 0.55f else c * 0.90f + t * 0.10f
                if (current[i] > 0.002f || t > 0f) moving = true
            }
            if (moving) state.value = current.copyOf()
        }
    }
    return state
}

/** Mean of the lowest bands, 0..1: drives the background pulse. */
fun FloatArray.bassEnergy(): Float {
    if (isEmpty()) return 0f
    val n = (size / 6).coerceAtLeast(1)
    var sum = 0f
    for (i in 0 until n) sum += this[i]
    return (sum / n).coerceIn(0f, 1f)
}
