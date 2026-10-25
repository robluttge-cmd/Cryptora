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

    /** Alle woorden met exact [length] letters, zelfde volgorde als [all]. */
    fun ofLength(length: Int): List<String>

    /** Totaal aantal woorden in de bron. */
    fun size(): Int
}
