package com.cryptora.app

import com.cryptora.app.data.SearchEngine
import com.cryptora.app.data.WordSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Kleine, in-memory woordenlijst zodat deze test geen Android-context nodig
 * heeft en dus als gewone JVM-test draait (Run > SearchEngineTest, of
 * `./gradlew test`).
 */
private class FakeWordSource(words: List<String>) : WordSource {
    private val all = words
    private val byLength = words.groupBy { it.length }
    override fun all(): List<String> = all
    override fun ofLength(length: Int): List<String> = byLength[length].orEmpty()
    override fun size(): Int = all.size
}

class SearchEngineTest {

    private val source = FakeWordSource(
        listOf(
            "server", "surfer", "sorber", "reserve", "service",
            "ernst", "trans", "nerts", "stern", "snert",
            "appel", "banaan", "computer", "test"
        )
    )

    @Test
    fun `leeg zoekopdracht geeft geen resultaten`() {
        val result = SearchEngine.search(SearchEngine.Query(), source)
        assertEquals(0, result.totalMatches)
        assertTrue(result.words.isEmpty())
    }

    @Test
    fun `patroon met wildcards matcht juiste lengte en letters`() {
        // ?, ., _ en - moeten alle vier als wildcard werken.
        for (wildcard in listOf("?", ".", "_", "-")) {
            val pattern = "s${wildcard}r${wildcard}er"
            val result = SearchEngine.search(SearchEngine.Query(pattern = pattern), source)
            assertEquals("wildcard '$wildcard' faalde", 3, result.totalMatches)
            assertTrue(result.words.containsAll(listOf("server", "surfer", "sorber")))
        }
    }

    @Test
    fun `zoekterm is een substring-filter`() {
        val result = SearchEngine.search(SearchEngine.Query(searchTerm = "serv"), source)
        assertTrue(result.words.contains("server"))
        assertTrue(result.words.contains("reserve"))
        assertTrue(result.words.contains("service"))
        assertFalse(result.words.contains("appel"))
    }

    @Test
    fun `bekende letters moeten allemaal voorkomen, positie maakt niet uit`() {
        val result = SearchEngine.search(SearchEngine.Query(knownLetters = "rstn"), source)
        assertTrue(result.words.containsAll(listOf("ernst", "trans", "nerts", "stern", "snert")))
        assertFalse(result.words.contains("appel"))
    }

    @Test
    fun `patroon en bekende letters worden gecombineerd met AND`() {
        val result = SearchEngine.search(
            SearchEngine.Query(pattern = "?????", knownLetters = "rstn"),
            source
        )
        assertTrue(result.words.containsAll(listOf("ernst", "trans", "nerts", "stern", "snert")))
        assertFalse(result.words.contains("service")) // 7 letters, valt af op patroonlengte
    }

    @Test
    fun `eigen woorden worden meegenomen als ze aan de filters voldoen`() {
        val result = SearchEngine.search(
            SearchEngine.Query(pattern = "???????", customWords = "zzcode1, xxfabrieknaam"),
            source
        )
        assertTrue(result.words.contains("zzcode1"))
        assertFalse(result.words.contains("xxfabrieknaam")) // verkeerde lengte, dus uitgesloten
    }

    @Test
    fun `resultaten blijven gesorteerd zoals in de bron (frequentie-volgorde)`() {
        val result = SearchEngine.search(SearchEngine.Query(searchTerm = "e"), source)
        // "server" staat vóór "reserve" in de invoerlijst, dus ook in de output.
        val indexServer = result.words.indexOf("server")
        val indexReserve = result.words.indexOf("reserve")
        assertTrue(indexServer < indexReserve)
    }

    @Test
    fun `parseCustomWords splitst op spaties, komma's en regeleinden`() {
        val parsed = SearchEngine.parseCustomWords("Appel, peer\ntest  banaan")
        assertEquals(listOf("appel", "peer", "test", "banaan"), parsed)
    }

    @Test
    fun `uitgesloten letters sluiten woorden met die letters uit`() {
        val result = SearchEngine.search(
            SearchEngine.Query(knownLetters = "rstn", excludedLetters = "a"),
            source
        )
        // "trans" bevat een 'a' en moet dus wegvallen, de rest niet.
        assertTrue(result.words.containsAll(listOf("ernst", "nerts", "stern", "snert")))
        assertFalse(result.words.contains("trans"))
    }

    @Test
    fun `losse lengtefilter werkt zonder patroon`() {
        val result = SearchEngine.search(SearchEngine.Query(exactLength = 5), source)
        assertTrue(result.words.containsAll(listOf("appel", "ernst", "trans", "nerts", "stern", "snert")))
        assertFalse(result.words.contains("test")) // 4 letters
        assertFalse(result.words.contains("server")) // 6 letters
    }

    @Test
    fun `patroon overstemt de losse lengtefilter bij conflict`() {
        // Patroon van 6 letters + tegenstrijdige lengte van 5: patroon wint,
        // lengtefilter wordt genegeerd (in plaats van altijd 0 resultaten).
        val result = SearchEngine.search(
            SearchEngine.Query(pattern = "??????", exactLength = 5),
            source
        )
        assertTrue(result.words.contains("server"))
    }

    @Test
    fun `anagram-modus vereist exact dezelfde letters, niet alleen bevatten`() {
        val result = SearchEngine.search(
            SearchEngine.Query(knownLetters = "ernst", anagramMode = true),
            source
        )
        // Zelfde 5 letters (e,n,r,s,t) in een andere volgorde: wel een match.
        assertTrue(result.words.containsAll(listOf("ernst", "stern", "nerts", "snert")))
        // "trans" bevat wel r,s,t,n maar heeft een 'a' i.p.v. een 'e': geen anagram.
        assertFalse(result.words.contains("trans"))
    }

    @Test
    fun `anagram-modus uit gedraagt zich als voorheen (bevat-filter)`() {
        val result = SearchEngine.search(
            SearchEngine.Query(knownLetters = "ernst", anagramMode = false),
            source
        )
        // Nu telt alleen of de letters e,n,r,s,t allemaal voorkomen — "trans"
        // bevat ze niet allemaal (geen 'e'), dus valt nog steeds af, maar om
        // een andere reden dan bij anagram-modus.
        assertFalse(result.words.contains("trans"))
        assertTrue(result.words.contains("ernst"))
    }

    @Test
    fun `exacte match wint altijd van de foutentolerante fallback`() {
        // "server" bestaat exact, dus mag isApproximate niet aan komen te staan.
        val result = SearchEngine.search(SearchEngine.Query(pattern = "server"), source)
        assertEquals(1, result.totalMatches)
        assertFalse(result.isApproximate)
    }

    @Test
    fun `patroon met 1 afwijkende letter valt terug op een foutentolerante match`() {
        // "servor" bestaat niet, maar wijkt maar 1 letter af van "server".
        val result = SearchEngine.search(SearchEngine.Query(pattern = "servor"), source)
        assertTrue(result.isApproximate)
        assertTrue(result.words.contains("server"))
    }

    @Test
    fun `patroon met 2 of meer afwijkende letters geeft geen fallback-match`() {
        // "servoz" wijkt op 2 posities af van "server" (o i.p.v. e, z i.p.v. r).
        val result = SearchEngine.search(SearchEngine.Query(pattern = "servoz"), source)
        assertEquals(0, result.totalMatches)
        assertFalse(result.isApproximate)
    }

    @Test
    fun `foutentolerante fallback respecteert nog steeds de overige filters`() {
        // "servor" lijkt op "server", maar de uitgesloten letter 'r' sluit 'm alsnog uit.
        val result = SearchEngine.search(
            SearchEngine.Query(pattern = "servor", excludedLetters = "r"),
            source
        )
        assertEquals(0, result.totalMatches)
    }

    @Test
    fun `foutentolerante fallback is niet van toepassing zonder patroon`() {
        // Zonder patroon is er niets om "op te lijken"; 0 resultaten blijft 0.
        val result = SearchEngine.search(SearchEngine.Query(searchTerm = "zzzznietbestaand"), source)
        assertEquals(0, result.totalMatches)
        assertFalse(result.isApproximate)
    }
}
