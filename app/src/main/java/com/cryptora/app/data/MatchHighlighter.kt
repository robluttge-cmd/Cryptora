package com.cryptora.app.data

/** Koppelt passende puzzelletters aan de oorspronkelijke (eventueel geaccentueerde) tekst. */
object MatchHighlighter {
    fun matchedIndices(woord: String, patroon: String, ijAsLetter: Boolean = true): Set<Int> {
        val pattern = PuzzleText.patternKey(patroon, ijAsLetter)
        if (pattern.isEmpty()) return emptySet()
        return buildSet {
            for ((i, letter) in PuzzleText.lettersWithIndices(woord, ijAsLetter).withIndex()) {
                if (i < pattern.length && pattern[i] != '.' && pattern[i] == letter.value) {
                    addAll(letter.originalIndices)
                }
            }
        }
    }
}
