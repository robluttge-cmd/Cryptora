package com.cryptora.app.data

/**
 * Abstractie over "een lijst met Nederlandse woorden, van meest naar minst
 * voorkomend gesorteerd". Door dit als interface te definiëren blijft
 * [com.cryptora.app.data.SearchEngine] volledig los van Android en
 * dus triviaal te unit-testen (zie src/test).
 */
interface WordSource {
    /** Alle woorden, gesorteerd van meest naar minst gebruikt. */
    fun all(): List<String>

    /** Alle woorden met exact [length] puzzelletters, zelfde volgorde als [all]. */
    fun ofLength(length: Int): List<String>

    /** Default for small/test sources; production sources cache these normalized entries. */
    fun candidates(length: Int?, ijAsLetter: Boolean = true): List<WordEntry> =
        all().map { WordEntry(it, PuzzleText.key(it, ijAsLetter)) }
            .filter { length == null || it.key.length == length }

    /** Totaal aantal woorden in de bron. */
    fun size(): Int
}

data class WordEntry(val word: String, val key: String)
