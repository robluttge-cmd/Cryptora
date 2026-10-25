package com.cryptora.app

import com.cryptora.app.data.IndexedWordSource
import com.cryptora.app.data.MatchHighlighter
import com.cryptora.app.data.PuzzleText
import com.cryptora.app.data.SearchEngine
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.zip.GZIPInputStream

class PuzzleSearchTest {
    private val source = IndexedWordSource(
        listOf("ijs", "rijst", "bijl", "vis", "jas", "café", "naïef", "foto's", "a-b")
    )

    @Test
    fun `ij occupies one cell in counters length index and wildcard matching`() {
        assertEquals(2, PuzzleText.letterCount("IJS"))
        assertEquals(4, PuzzleText.letterCount("rijst"))
        assertEquals(listOf("ijs", "a-b"), source.ofLength(2))
        assertEquals(listOf("ijs"), SearchEngine.search(SearchEngine.Query(pattern = ".s"), source).words)
        assertEquals(listOf("bijl"), SearchEngine.search(SearchEngine.Query(pattern = ".ij."), source).words)
        assertTrue(SearchEngine.search(SearchEngine.Query(exactLength = 2), source).words.contains("ijs"))
    }

    @Test
    fun `accented and unaccented input find original display spellings`() {
        for (input in listOf("cafe", "CAFÉ", "cafe\u0301")) {
            val result = SearchEngine.search(SearchEngine.Query(pattern = input), source)
            assertEquals(listOf("café"), result.words)
            assertFalse(result.isApproximate)
        }
        assertEquals(listOf("naïef"), SearchEngine.search(SearchEngine.Query(pattern = "naief"), source).words)
    }

    @Test
    fun `anagrams use base letters and keep ij together as one puzzle letter`() {
        assertEquals(listOf("ijs"), SearchEngine.search(
            SearchEngine.Query(knownLetters = "sij", anagramMode = true), source
        ).words)
        assertTrue(SearchEngine.search(
            SearchEngine.Query(knownLetters = "isj", anagramMode = true), source
        ).words.isEmpty())
        assertEquals(listOf("café"), SearchEngine.search(
            SearchEngine.Query(knownLetters = "EFAC", anagramMode = true), source
        ).words)
    }

    @Test
    fun `repeated anagram letters must have the right multiplicity`() {
        val words = IndexedWordSource(listOf("appel", "papel", "peela"))
        assertEquals(listOf("appel", "papel"), SearchEngine.search(
            SearchEngine.Query(knownLetters = "pleap", anagramMode = true), words
        ).words)
    }

    @Test
    fun `substring and exclusions use the same alphabet as the pattern`() {
        assertEquals(listOf("café"), SearchEngine.search(SearchEngine.Query(searchTerm = "FÉ"), source).words)
        assertFalse(SearchEngine.search(
            SearchEngine.Query(exactLength = 4, excludedLetters = "é"), source
        ).words.contains("café"))
        assertFalse(SearchEngine.search(
            SearchEngine.Query(searchTerm = "ij", excludedLetters = "ij"), source
        ).words.contains("ijs"))
        assertTrue(SearchEngine.search(
            SearchEngine.Query(searchTerm = "ij", excludedLetters = "i"), source
        ).words.contains("ijs"))
    }

    @Test
    fun `apostrophes and hyphens do not occupy puzzle cells`() {
        assertEquals(listOf("foto's"), SearchEngine.search(SearchEngine.Query(pattern = "fotos"), source).words)
        assertEquals(listOf("foto's"), SearchEngine.search(
            SearchEngine.Query(knownLetters = "stfoo", anagramMode = true), source
        ).words)
        assertEquals(listOf("a-b"), SearchEngine.search(SearchEngine.Query(pattern = "ab"), source).words)
    }

    @Test
    fun `fallback counts an ij cell as one difference`() {
        val result = SearchEngine.search(SearchEngine.Query(pattern = "us"), IndexedWordSource(listOf("ijs")))
        assertEquals(listOf("ijs"), result.words)
        assertTrue(result.isApproximate)
    }

    @Test
    fun `pattern anagram and length filters stay consistent`() {
        val result = SearchEngine.search(
            SearchEngine.Query(pattern = ".ij.", knownLetters = "lijb", anagramMode = true), source
        )
        assertEquals(listOf("bijl"), result.words)
        assertFalse(result.isApproximate)
        assertTrue(SearchEngine.search(
            SearchEngine.Query(knownLetters = "sij", anagramMode = true, exactLength = 3), source
        ).words.isEmpty())
    }

    @Test
    fun `ij ligature and decomposed accents use the same cells`() {
        assertEquals(PuzzleText.key("ijs"), PuzzleText.key("Ĳs"))
        assertEquals(5, PuzzleText.letterCount("nai\u0308ef"))
        assertEquals("s.r.er", PuzzleText.patternInput("S?R_ER"))
        assertEquals("naief", PuzzleText.lettersInput("nai\u0308ef"))
    }

    @Test
    fun `non Dutch sources keep separate i and j letters`() {
        val words = IndexedWordSource(listOf("bijou"), ijAsLetter = false)
        assertEquals(5, PuzzleText.letterCount("bijou", ijAsLetter = false))
        assertEquals(listOf("bijou"), SearchEngine.search(
            SearchEngine.Query(pattern = ".....", ijAsLetter = false), words
        ).words)
    }

    @Test
    fun `highlighting covers original ij characters accents and subsequent positions`() {
        assertEquals(setOf(0, 1, 2, 3), MatchHighlighter.matchedIndices("bijl", "bijl"))
        assertEquals(setOf(0, 1, 2), MatchHighlighter.matchedIndices("ijs", "ijs"))
    }

    @Test
    fun `highlighting leaves wildcards and different cells unmarked`() {
        assertEquals(setOf(1, 2, 3), MatchHighlighter.matchedIndices("bijl", ".ijl"))
        assertEquals(setOf(3), MatchHighlighter.matchedIndices("café", "...e"))
        assertEquals(setOf(3, 4), MatchHighlighter.matchedIndices("cafe\u0301", "...é"))
        assertEquals(setOf(0, 1, 2, 3, 5), MatchHighlighter.matchedIndices("foto's", "fotos"))
    }

    @Test
    fun `custom words obey the same rules and cannot be hidden behind 500 base results`() {
        val broad = IndexedWordSource((1..600).map { "woord$it" })
        val result = SearchEngine.search(
            SearchEngine.Query(searchTerm = "woord", customWords = "Caféwoord caféwoord, woord1"), broad
        )
        assertEquals(601, result.totalMatches)
        assertEquals(500, result.words.size)
        assertEquals(listOf("caféwoord", "woord1"), result.words.take(2))
        assertEquals(1, result.words.count { it == "woord1" })
        assertEquals(listOf("ijscafé"), SearchEngine.search(
            SearchEngine.Query(pattern = "ijscafe", customWords = "ijscafé IJSCafé"), IndexedWordSource(emptyList())
        ).words)
    }

    @Test
    fun `bundled Dutch dictionary supports normalized queries with its production index`() {
        val file = File("src/main/assets/woorden.txt.gz")
        assertTrue("Bundled Dutch dictionary is required", file.isFile)
        val words = GZIPInputStream(file.inputStream()).bufferedReader(Charsets.UTF_8).use { it.readLines() }
        val dictionary = IndexedWordSource(words)
        for ((pattern, expected) in listOf("ij." to "ijs", "cafe" to "café", "een" to "één", "fotos" to "foto's")) {
            val result = SearchEngine.search(SearchEngine.Query(pattern = pattern), dictionary)
            assertTrue("$pattern must find $expected", expected in result.words)
            assertFalse(result.isApproximate)
        }
    }
}
