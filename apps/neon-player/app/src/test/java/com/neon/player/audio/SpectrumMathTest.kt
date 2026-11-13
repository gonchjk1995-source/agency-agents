package com.neon.player.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class SpectrumMathTest {

    private val sampleRate = 44_100
    private val size = SpectrumMath.FFT_SIZE

    private fun sine(freq: Double, amplitude: Float = 1f) =
        FloatArray(size) { i -> (amplitude * sin(2 * PI * freq * i / sampleRate)).toFloat() }

    @Test
    fun silenceProducesFlatZeroSpectrum() {
        val bands = SpectrumMath.analyze(FloatArray(size), sampleRate, 32)
        assertEquals(32, bands.size)
        bands.forEach { assertEquals(0f, it, 1e-6f) }
    }

    @Test
    fun sineToneLightsUpTheBandHoldingItsFrequency() {
        val bandCount = 32
        val freq = 1_000.0
        val bands = SpectrumMath.analyze(sine(freq), sampleRate, bandCount)
        val edges = SpectrumMath.bandEdgesHz(bandCount)
        val loudest = bands.indices.maxBy { bands[it] }
        assertTrue(
            "1 kHz should land in band $loudest (${edges[loudest]}..${edges[loudest + 1]} Hz)",
            freq >= edges[loudest] && freq < edges[loudest + 1],
        )
        assertTrue("full-scale tone should be near the top of the range", bands[loudest] > 0.8f)
    }

    @Test
    fun quieterToneGivesLowerLevel() {
        val loud = SpectrumMath.analyze(sine(440.0, 1f), sampleRate, 32).max()
        val quiet = SpectrumMath.analyze(sine(440.0, 0.05f), sampleRate, 32).max()
        assertTrue("quiet=$quiet loud=$loud", quiet < loud)
        assertTrue(quiet > 0f)
    }

    @Test
    fun valuesStayInsideUnitRange() {
        val noisy = FloatArray(size) { if (it % 2 == 0) 1f else -1f }
        SpectrumMath.analyze(noisy, sampleRate, 48).forEach { assertTrue(it in 0f..1f) }
    }
}
