package com.cryptora.app.data

import java.text.Normalizer

/** Shared puzzle alphabet for searching, length counters and result highlighting. */
object PuzzleText {
    private const val IJ = 'ĳ'
    private val wildcards = setOf('.', '?', '_', '-')

    private fun isMark(c: Char): Boolean = when (Character.getType(c)) {
        Character.NON_SPACING_MARK.toInt(),
        Character.COMBINING_SPACING_MARK.toInt(),
        Character.ENCLOSING_MARK.toInt() -> true
        else -> false
    }

    private fun baseLetters(text: String): String =
        Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .filterNot(::isMark)
            .replace("ĳ", "ij")

    /** Keep readable input; never expose the internal one-character IJ representation. */
    fun lettersInput(text: String): String = baseLetters(text).filter { it.isLetterOrDigit() }

    fun patternInput(text: String): String = buildString {
        for (c in baseLetters(text)) {
            when {
                c in wildcards -> append('.')
                c.isLetterOrDigit() -> append(c)
            }
        }
    }

    /** Accents and punctuation do not occupy puzzle cells. Dutch IJ occupies one. */
    fun key(text: String, ijAsLetter: Boolean = true): String =
        collapseIj(lettersInput(text), ijAsLetter)

    fun patternKey(text: String, ijAsLetter: Boolean = true): String =
        collapseIj(patternInput(text), ijAsLetter)

    fun letterCount(text: String, ijAsLetter: Boolean = true): Int = key(text, ijAsLetter).length

    private fun collapseIj(text: String, ijAsLetter: Boolean): String =
        if (ijAsLetter) text.replace("ij", IJ.toString()) else text

    data class Letter(val value: Char, val originalIndices: IntRange)

    /** Map normalized puzzle cells back onto the original, accented display word. */
    fun lettersWithIndices(text: String, ijAsLetter: Boolean = true): List<Letter> {
        val letters = mutableListOf<Letter>()
        var offset = 0
        while (offset < text.length) {
            val end = offset + Character.charCount(text.codePointAt(offset))
            val part = text.substring(offset, end)
            val normalized = lettersInput(part)
            if (normalized.isEmpty() && part.all(::isMark) && letters.isNotEmpty()) {
                val last = letters.removeAt(letters.lastIndex)
                letters.add(last.copy(originalIndices = last.originalIndices.first until end))
            } else {
                for (c in normalized) letters.add(Letter(c, offset until end))
            }
            offset = end
        }
        if (!ijAsLetter) return letters
        val result = mutableListOf<Letter>()
        var i = 0
        while (i < letters.size) {
            val current = letters[i]
            if (current.value == 'i' && letters.getOrNull(i + 1)?.value == 'j') {
                result.add(Letter(IJ, current.originalIndices.first..letters[i + 1].originalIndices.last))
                i += 2
            } else {
                result.add(current)
                i++
            }
        }
        return result
    }
}
