package com.cryptora.app

import com.cryptora.app.data.RecentSearch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecentSearchTest {

    @Test
    fun `serialiseren en terug lezen levert hetzelfde object op`() {
        val origineel = RecentSearch(
            pattern = "s?r?er",
            searchTerm = "serv",
            knownLetters = "rstn",
            excludedLetters = "xyz",
            exactLength = 6,
            anagramMode = true
        )
        val teruggelezen = RecentSearch.deserialize(origineel.serialize())
        assertEquals(origineel, teruggelezen)
    }

    @Test
    fun `lege velden en ontbrekende lengte worden correct bewaard`() {
        val origineel = RecentSearch(
            pattern = "",
            searchTerm = "",
            knownLetters = "ernst",
            excludedLetters = "",
            exactLength = null,
            anagramMode = false
        )
        val teruggelezen = RecentSearch.deserialize(origineel.serialize())
        assertEquals(origineel, teruggelezen)
    }

    @Test
    fun `onherkenbare data levert null op in plaats van een crash`() {
        assertNull(RecentSearch.deserialize("dit is geen geldige serialisatie"))
    }

    @Test
    fun `label toont een korte samenvatting`() {
        val zoekopdracht = RecentSearch(
            pattern = "s?r?er",
            searchTerm = "",
            knownLetters = "",
            excludedLetters = "",
            exactLength = null,
            anagramMode = false
        )
        assertEquals("s?r?er", zoekopdracht.label())
    }
}
