package com.neon.player.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {
    @Test
    fun formatsMinutesAndHours() {
        assertEquals("0:00", formatTime(0))
        assertEquals("0:00", formatTime(-500))
        assertEquals("3:07", formatTime(187_400))
        assertEquals("12:45", formatTime(765_000))
        assertEquals("1:02:09", formatTime(3_729_000))
    }

    @Test
    fun russianPluralsForTrackCount() {
        assertEquals("1 трек", tracksLabel(1))
        assertEquals("3 трека", tracksLabel(3))
        assertEquals("5 треков", tracksLabel(5))
        assertEquals("11 треков", tracksLabel(11))
        assertEquals("14 треков", tracksLabel(14))
        assertEquals("21 трек", tracksLabel(21))
        assertEquals("104 трека", tracksLabel(104))
        assertEquals("0 треков", tracksLabel(0))
    }
}
