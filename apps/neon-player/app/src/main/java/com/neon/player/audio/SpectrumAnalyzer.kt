package com.neon.player.audio

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.TeeAudioProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Receives a copy of the decoded PCM stream from ExoPlayer (via [TeeAudioProcessor])
 * and publishes spectrum bands for the visualizer. Runs on the playback thread,
 * so it never needs the microphone permission that android.media.audiofx.Visualizer does.
 */
@OptIn(UnstableApi::class)
object SpectrumAnalyzer : TeeAudioProcessor.AudioBufferSink {
    const val BANDS = 48

    private val _bands = MutableStateFlow(FloatArray(BANDS))
    val bands: StateFlow<FloatArray> = _bands.asStateFlow()

    @Volatile private var sampleRate = 44_100
    @Volatile private var channels = 2
    @Volatile private var encoding = C.ENCODING_PCM_16BIT

    private val frame = FloatArray(SpectrumMath.FFT_SIZE)
    private var filled = 0

    override fun flush(sampleRateHz: Int, channelCount: Int, encoding: Int) {
        sampleRate = sampleRateHz
        channels = channelCount.coerceAtLeast(1)
        this.encoding = encoding
        filled = 0
    }

    override fun handleBuffer(buffer: ByteBuffer) {
        val data = buffer.duplicate().order(ByteOrder.nativeOrder())
        val ch = channels
        when (encoding) {
            C.ENCODING_PCM_16BIT -> {
                val frameBytes = 2 * ch
                while (data.remaining() >= frameBytes) {
                    var sum = 0f
                    repeat(ch) { sum += data.short / 32768f }
                    push(sum / ch)
                }
            }
            C.ENCODING_PCM_FLOAT -> {
                val frameBytes = 4 * ch
                while (data.remaining() >= frameBytes) {
                    var sum = 0f
                    repeat(ch) { sum += data.float }
                    push(sum / ch)
                }
            }
            else -> Unit // other encodings are rare after ExoPlayer's own conversion; skip them
        }
    }

    private fun push(sample: Float) {
        frame[filled++] = sample
        if (filled == frame.size) {
            filled = 0
            _bands.value = SpectrumMath.analyze(frame, sampleRate, BANDS)
        }
    }

    /** Called when playback stops so the visualizer can fall back to idle. */
    fun clear() {
        filled = 0
        _bands.value = FloatArray(BANDS)
    }
}
