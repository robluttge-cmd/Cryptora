package com.cryptora.app.data

/** Normalize once at load time, retaining the spelling and source order for display. */
class IndexedWordSource(
    private val words: List<String>,
    private val ijAsLetter: Boolean = true
) : WordSource {
    private val entries = words.map { WordEntry(it, PuzzleText.key(it, ijAsLetter)) }
    private val byLength = entries.groupBy { it.key.length }

    override fun all(): List<String> = words
    override fun ofLength(length: Int): List<String> = byLength[length].orEmpty().map { it.word }
    override fun size(): Int = words.size

    override fun candidates(length: Int?, ijAsLetter: Boolean): List<WordEntry> =
        if (ijAsLetter != this.ijAsLetter) super.candidates(length, ijAsLetter)
        else if (length == null) entries else byLength[length].orEmpty()
}
