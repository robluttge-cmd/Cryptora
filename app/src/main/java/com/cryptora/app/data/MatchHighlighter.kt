package com.cryptora.app.data

/**
 * Bepaalt welke letter-indices van een gevonden woord visueel gemarkeerd
 * mogen worden in de resultatenlijst (bv. vetgedrukt), omdat ze duidelijk
 * "de reden" zijn waarom het woord matcht. Puur Kotlin, geen Compose/Android
 * nodig, zodat dit met gewone JVM unit tests te verifiëren is (zie
 * src/test/.../MatchHighlighterTest.kt).
 */
object MatchHighlighter {

    private val WILDCARD_CHARS = setOf('?', '.', '_', '-')

    /**
     * Geeft de indices in [woord] die overeenkomen met een letterlijke
     * (niet-wildcard) positie in [patroon]. Geef in anagram-modus een lege
     * string mee als [patroon]: het hele woord bestaat daar toch al exact
     * uit de opgegeven letters, dus is er niets zinvols te markeren.
     */
    fun matchedIndices(woord: String, patroon: String): Set<Int> {
        val patroonTrim = patroon.trim()
        if (patroonTrim.isEmpty()) return emptySet()

        val resultaat = mutableSetOf<Int>()
        for (i in woord.indices) {
            val ch = woord[i]
            val isPatroonMatch = i < patroonTrim.length &&
                patroonTrim[i] !in WILDCARD_CHARS &&
                patroonTrim[i].lowercaseChar() == ch.lowercaseChar()
            if (isPatroonMatch) resultaat.add(i)
        }
        return resultaat
    }
}
