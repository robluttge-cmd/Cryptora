package com.cryptora.app

import com.cryptora.app.data.MatchHighlighter
import org.junit.Assert.assertEquals
import org.junit.Test

class MatchHighlighterTest {

    @Test
    fun `patroon markeert alleen de niet-wildcard posities`() {
        // "s?r?er" tegen "server": posities 0, 2, 4, 5 zijn letterlijk (s,r,e,r),
        // posities 1 en 3 zijn wildcards (?) en worden dus niet gemarkeerd.
        val indices = MatchHighlighter.matchedIndices(woord = "server", patroon = "s?r?er")
        assertEquals(setOf(0, 2, 4, 5), indices)
    }

    @Test
    fun `leeg patroon (bijv anagram-modus) geeft geen markering`() {
        val indices = MatchHighlighter.matchedIndices(woord = "stern", patroon = "")
        assertEquals(emptySet<Int>(), indices)
    }

    @Test
    fun `hoofdletterongevoelig`() {
        val indices = MatchHighlighter.matchedIndices(woord = "Server", patroon = "S?R?ER")
        assertEquals(setOf(0, 2, 4, 5), indices)
    }

    @Test
    fun `patroon korter dan het woord markeert alleen overlappende posities`() {
        val indices = MatchHighlighter.matchedIndices(woord = "servers", patroon = "serv")
        assertEquals(setOf(0, 1, 2, 3), indices)
    }
}
