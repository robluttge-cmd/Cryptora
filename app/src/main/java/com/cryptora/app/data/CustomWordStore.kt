package com.cryptora.app.data

/** Persistent per-language vocabulary; storage is injected so it can be tested on the JVM. */
class CustomWordStore(
    private val read: (String) -> String?,
    private val write: (String, String) -> Unit
) {
    fun words(language: String): List<String> = SearchEngine.parseCustomWords(read(language).orEmpty())

    fun add(language: String, input: String): List<String> =
        save(language, words(language) + SearchEngine.parseCustomWords(input))

    fun remove(language: String, word: String): List<String> =
        save(language, words(language).filterNot { it == word })

    private fun save(language: String, words: List<String>): List<String> {
        val unique = words.distinct()
        write(language, unique.joinToString("\n"))
        return unique
    }
}
