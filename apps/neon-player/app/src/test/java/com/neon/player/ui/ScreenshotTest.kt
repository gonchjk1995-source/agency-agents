package com.neon.player.ui

import android.net.Uri
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.media3.common.C
import com.github.takahirom.roborazzi.captureRoboImage
import com.neon.player.LibraryState
import com.neon.player.PlaybackState
import com.neon.player.RepeatMode
import com.neon.player.audio.SpectrumAnalyzer
import com.neon.player.data.Track
import com.neon.player.ui.screens.LibraryScreen
import com.neon.player.ui.screens.NowPlayingScreen
import com.neon.player.ui.screens.PermissionScreen
import com.neon.player.ui.theme.NeonTheme
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

/**
 * Renders the main screens to PNG for design review. Skipped unless run with
 * `./gradlew :app:testDebugUnitTest -Pscreenshots`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi", sdk = [35])
class ScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val tracks = listOf(
        track(1, "Neon Rain", "Kaito Mirage", "Night Drive", 214_000),
        track(2, "Огни ночного города", "Синтетика", "Протокол 7", 187_000),
        track(3, "Chrome Hearts", "VXLT", "Overclock", 241_000),
        track(4, "Квантовый прыжок", "Аврора-9", "Горизонт событий", 263_000),
        track(5, "Midnight Protocol", "Lumen Drift", "Night Drive", 199_000),
        track(6, "Пульс", "Неонавт", "Сигнал", 176_000),
        track(7, "Holo Garden", "Kaito Mirage", "Night Drive", 228_000),
        track(8, "Звёздная пыль", "Аврора-9", "Горизонт событий", 205_000),
        track(9, "Afterglow.exe", "VXLT", "Overclock", 192_000),
    )

    @Before
    fun onlyWhenAsked() {
        assumeTrue(System.getProperty("neon.screenshots") == "true")
        compose.mainClock.autoAdvance = false
    }

    @Test
    fun library() {
        val playing = PlaybackState(current = tracks[1], isPlaying = true, positionMs = 72_000, durationMs = 187_000, queueIndex = 1, queueSize = tracks.size)
        compose.setContent {
            NeonTheme {
                LibraryScreen(
                    library = LibraryState(loading = false, tracks = tracks),
                    tracks = tracks,
                    query = "",
                    onQueryChange = {},
                    playback = playing,
                    onPlay = {},
                    onShuffleAll = {},
                    onTogglePlay = {},
                    onNext = {},
                    onOpenPlayer = {},
                )
            }
        }
        settle()
        compose.onRoot().captureRoboImage("screenshots/library.png")
    }

    @Test
    fun nowPlaying() {
        feedMusicLikeSignal()
        val state = PlaybackState(
            current = tracks[0],
            isPlaying = true,
            positionMs = 83_000,
            durationMs = 214_000,
            shuffle = true,
            repeat = RepeatMode.ALL,
            queueIndex = 0,
            queueSize = tracks.size,
        )
        compose.setContent {
            NeonTheme {
                NowPlayingScreen(state, {}, {}, {}, {}, {}, {}, {})
            }
        }
        settle()
        compose.onRoot().captureRoboImage("screenshots/now_playing.png")
    }

    @Test
    fun permission() {
        compose.setContent {
            NeonTheme { PermissionScreen(openSettingsInstead = false, onRequest = {}, onOpenSettings = {}) }
        }
        settle()
        compose.onRoot().captureRoboImage("screenshots/permission.png")
    }

    private fun settle() {
        repeat(40) { compose.mainClock.advanceTimeByFrame() }
        compose.mainClock.advanceTimeBy(1_200)
    }

    /** Pushes a chord with a kick-drum-ish low end through the real analyzer path. */
    private fun feedMusicLikeSignal() {
        val rate = 44_100
        SpectrumAnalyzer.flush(rate, 2, C.ENCODING_PCM_16BIT)
        val frames = 1024
        val buf = ByteBuffer.allocate(frames * 4).order(ByteOrder.nativeOrder())
        val partials = listOf(55.0 to 0.55, 110.0 to 0.35, 220.0 to 0.2, 330.0 to 0.12, 660.0 to 0.08, 1320.0 to 0.05, 2640.0 to 0.03, 5280.0 to 0.015, 9000.0 to 0.008)
        for (i in 0 until frames) {
            var v = 0.0
            partials.forEach { (f, a) -> v += a * sin(2 * PI * f * i / rate) }
            val s = (v.coerceIn(-1.0, 1.0) * 32000).toInt().toShort()
            buf.putShort(s)
            buf.putShort(s)
        }
        buf.flip()
        SpectrumAnalyzer.handleBuffer(buf)
    }

    private fun track(id: Long, title: String, artist: String, album: String, duration: Long) =
        Track(id, title, artist, album, albumId = id, durationMs = duration, uri = Uri.EMPTY, artUri = Uri.EMPTY)
}
