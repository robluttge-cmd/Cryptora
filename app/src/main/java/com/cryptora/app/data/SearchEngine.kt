package com.cryptora.app.data

import java.text.Normalizer

/**
 * Android-onafhankelijke woordzoeker. Alle filters gebruiken hetzelfde
 * puzzelalfabet: accentletters worden basisletters, leestekens tellen niet
 * mee en Nederlandse ij is één puzzelletter. De oorspronkelijke schrijfwijze
 * van kandidaten blijft behouden in de resultaten.
 */
object SearchEngine {
    const val MAX_RESULTS = 500
    private const val FUZZY_MAX_AFWIJKINGEN = 1

    data class Query(
        val pattern: String = "",
        val searchTerm: String = "",
        val knownLetters: String = "",
        val excludedLetters: String = "",
        val exactLength: Int? = null,
        val anagramMode: Boolean = false,
        val customWords: String = "",
        val ijAsLetter: Boolean = true
    ) {
        val isBlank: Boolean
            get() = pattern.isBlank() && searchTerm.isBlank() &&
                knownLetters.isBlank() && excludedLetters.isBlank() &&
                exactLength == null && customWords.isBlank()
    }

    data class Result(
        val totalMatches: Int,
        val words: List<String>,
        val isApproximate: Boolean = false
    ) {
        val isTruncated: Boolean get() = totalMatches > words.size

        companion object {
            fun empty() = Result(0, emptyList())
        }
    }

    /** Woorden bewaren hun accenten; dubbele invoer en losse leestekens vallen weg. */
    fun parseCustomWords(raw: String): List<String> =
        raw.split(Regex("[,\\s]+"))
            .map { Normalizer.normalize(it.trim().lowercase(), Normalizer.Form.NFC) }
            .filter { it.any(Char::isLetterOrDigit) }
            .distinct()

    fun search(query: Query, source: WordSource): Result {
        if (query.isBlank) return Result.empty()

        val pattern = PuzzleText.patternKey(query.pattern, query.ijAsLetter)
        val term = PuzzleText.key(query.searchTerm, query.ijAsLetter)
        val letters = PuzzleText.key(query.knownLetters, query.ijAsLetter)
        val lettersSet = letters.toSet()
        val signature = letters.toCharArray().sortedArray().concatToString()
        val excluded = PuzzleText.key(query.excludedLetters, query.ijAsLetter).toSet()
        val requiredLength = when {
            pattern.isNotEmpty() -> pattern.length
            query.anagramMode && letters.isNotEmpty() -> letters.length
            else -> query.exactLength
        }

        val custom = parseCustomWords(query.customWords)
            .map { WordEntry(it, PuzzleText.key(it, query.ijAsLetter)) }
            .filter { requiredLength == null || it.key.length == requiredLength }
        val customNames = custom.map { it.word.lowercase() }.toSet()
        val base = source.candidates(requiredLength, query.ijAsLetter)

        fun collect(allowedDifferences: Int): Result {
            val matches = ArrayList<String>()
            var total = 0
            // Eigen woorden eerst: ze blijven ook bij brede zoekopdrachten zichtbaar.
            val candidates = custom.asSequence() + base.asSequence()
                .filterNot { it.word.lowercase() in customNames }
            for ((word, key) in candidates) {
                if (key.isEmpty()) continue
                if (pattern.isNotEmpty() && !matchesPattern(pattern, key, allowedDifferences)) continue
                if (term.isNotEmpty() && term !in key) continue
                if (pattern.isEmpty() && query.exactLength != null && key.length != query.exactLength) continue
                if (letters.isNotEmpty()) {
                    if (query.anagramMode) {
                        if (signature != key.toCharArray().sortedArray().concatToString()) continue
                    } else if (!lettersSet.all { it in key }) continue
                }
                if (key.any { it in excluded }) continue
                total++
                if (matches.size < MAX_RESULTS) matches.add(word)
            }
            return Result(total, matches)
        }

        val exact = collect(0)
        if (exact.totalMatches > 0 || pattern.isEmpty()) return exact
        val approximate = collect(FUZZY_MAX_AFWIJKINGEN)
        return if (approximate.totalMatches > 0) approximate.copy(isApproximate = true) else exact
    }

    private fun matchesPattern(pattern: String, word: String, allowedDifferences: Int): Boolean {
        if (pattern.length != word.length) return false
        var differences = 0
        for (i in pattern.indices) {
            if (pattern[i] != '.' && pattern[i] != word[i]) {
                differences++
                if (differences > allowedDifferences) return false
            }
        }
        return true
    }
}
