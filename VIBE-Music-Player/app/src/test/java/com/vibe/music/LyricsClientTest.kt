package com.vibe.music

import org.junit.Assert.assertEquals
import org.junit.Test

class LyricsClientTest {
    @Test fun parsesTimedLrcLinesAndFractionalSeconds() {
        val lines = LyricsClient.parseLrc("[00:01.2]First line\n[00:02.34]Second line")
        assertEquals(2, lines.size)
        assertEquals(1200L, lines[0].timeMs)
        assertEquals("First line", lines[0].text)
        assertEquals(2340L, lines[1].timeMs)
    }

    @Test fun supportsMultipleTimestampsOnOneLine() {
        val lines = LyricsClient.parseLrc("[00:01.00][00:02.00]Repeated line")
        assertEquals(listOf(1000L, 2000L), lines.map { it.timeMs })
        assertEquals(listOf("Repeated line", "Repeated line"), lines.map { it.text })
    }
}
