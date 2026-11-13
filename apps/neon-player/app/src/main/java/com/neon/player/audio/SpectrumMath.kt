package com.neon.player.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure spectrum maths: windowed FFT folded into logarithmic frequency bands,
 * each scaled to 0..1 on a decibel range. No Android dependencies, so it is
 * unit-tested on the JVM.
 */
object SpectrumMath {
    const val FFT_SIZE = 1024
    private const val MIN_HZ = 40.0
    private const val MAX_HZ = 16_000.0
    private const val FLOOR_DB = -60f

    private val window = FloatArray(FFT_SIZE) { i ->
        (0.5 - 0.5 * cos(2 * PI * i / (FFT_SIZE - 1))).toFloat()
    }

    /** Band boundaries in Hz, spaced evenly on a log scale; size is [bandCount] + 1. */
    fun bandEdgesHz(bandCount: Int): DoubleArray {
        val lo = ln(MIN_HZ)
        val hi = ln(MAX_HZ)
        return DoubleArray(bandCount + 1) { exp(lo + (hi - lo) * it / bandCount) }
    }

    /**
     * @param samples exactly [FFT_SIZE] mono samples in -1..1
     * @return [bandCount] levels in 0..1
     */
    fun analyze(samples: FloatArray, sampleRate: Int, bandCount: Int): FloatArray {
        require(samples.size == FFT_SIZE) { "expected $FFT_SIZE samples, got ${samples.size}" }
        val re = FloatArray(FFT_SIZE) { samples[it] * window[it] }
        val im = FloatArray(FFT_SIZE)
        fft(re, im)

        // A full-scale sine through a Hann window peaks at N/4.
        val norm = FFT_SIZE / 4f
        val half = FFT_SIZE / 2
        val mags = FloatArray(half) { sqrt(re[it] * re[it] + im[it] * im[it]) / norm }

        val binHz = sampleRate.toDouble() / FFT_SIZE
        val edges = bandEdgesHz(bandCount)
        return FloatArray(bandCount) { b ->
            var from = (edges[b] / binHz).toInt().coerceIn(1, half - 1)
            val to = (edges[b + 1] / binHz).toInt().coerceIn(from, half - 1)
            var peak = 0f
            while (from <= to) {
                if (mags[from] > peak) peak = mags[from]
                from++
            }
            toUnit(peak)
        }
    }

    private fun toUnit(magnitude: Float): Float {
        if (magnitude <= 1e-7f) return 0f
        val db = 20f * log10(magnitude)
        return ((db - FLOOR_DB) / -FLOOR_DB).coerceIn(0f, 1f)
    }

    /** In-place iterative radix-2 Cooley–Tukey FFT. */
    private fun fft(re: FloatArray, im: FloatArray) {
        val n = re.size
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit
            if (i < j) {
                var t = re[i]; re[i] = re[j]; re[j] = t
                t = im[i]; im[i] = im[j]; im[j] = t
            }
        }
        var len = 2
        while (len <= n) {
            val angle = -2 * PI / len
            val wRe = cos(angle).toFloat()
            val wIm = sin(angle).toFloat()
            var start = 0
            while (start < n) {
                var curRe = 1f
                var curIm = 0f
                for (k in 0 until len / 2) {
                    val a = start + k
                    val b = a + len / 2
                    val tRe = re[b] * curRe - im[b] * curIm
                    val tIm = re[b] * curIm + im[b] * curRe
                    re[b] = re[a] - tRe
                    im[b] = im[a] - tIm
                    re[a] += tRe
                    im[a] += tIm
                    val nextRe = curRe * wRe - curIm * wIm
                    curIm = curRe * wIm + curIm * wRe
                    curRe = nextRe
                }
                start += len
            }
            len = len shl 1
        }
    }
}
