package com.cryptora.app.data

/**
 * De volledige zoeklogica van de app, met opzet zonder enige Android-import
 * zodat hij als gewone JVM unit test (src/test) geverifieerd kan worden.
 *
 * Ondersteunde velden, allemaal optioneel en met elkaar combineerbaar
 * (AND-logica):
 *  - patroon: bv. "s?r?er" — ?, ., _ en - staan alle vier voor "onbekende
 *    letter op deze positie"; overige tekens moeten letterlijk overeenkomen.
 *    Bepaalt ook de woordlengte.
 *  - zoekterm: het woord moet deze tekenreeks ergens bevatten.
 *  - bekende letters: het woord moet elk van deze letters minstens één keer
 *    bevatten (positie maakt niet uit) — tenzij anagramMode aan staat, dan
 *    moet het woord *exact* uit deze letters bestaan (zelfde aantal, zelfde
 *    lengte, in willekeurige volgorde).
 *  - uitgesloten letters: het woord mag geen van deze letters bevatten.
 *  - exacte lengte: alleen relevant als er geen patroon is ingevuld (het
 *    patroon bepaalt anders al de lengte).
 *  - eigen woorden: extra kandidaat-woorden (los van het woordenboek) die
 *    ook tegen dezelfde filters worden getoetst, handig voor namen of
 *    jargon dat niet in de standaardlijst staat.
 *
 * "ij" telt bewust als twee losse tekens (i en j), niet als één letter of
 * ligatuur: de woordenlijst zelf bestaat uit gewone Kotlin-Strings, en
 * lengte-/positiematching (patroon, exacte lengte) werkt op basis van
 * String.length en indexering. Dat is intern altijd consistent, dus een
 * patroon van bv. 6 tekens matcht altijd woorden van 6 tekens, ook als
 * daar een "ij" in voorkomt.
 */
object SearchEngine {

    /** Tekens die in een patroon een onbekende letter aanduiden. */
    private val WILDCARDS = setOf('?', '.', '_', '-')

    /** Toon nooit meer dan dit aantal resultaten in de UI (performance). */
    const val MAX_RESULTS = 500

    /** Aantal afwijkende letters dat de foutentolerante fallback toestaat. */
    private const val FUZZY_MAX_AFWIJKINGEN = 1

    data class Query(
        val pattern: String = "",
        val searchTerm: String = "",
        val knownLetters: String = "",
        val excludedLetters: String = "",
        val exactLength: Int? = null,
        val anagramMode: Boolean = false,
        val customWords: String = ""
    ) {
        val isBlank: Boolean
            get() = pattern.isBlank() && searchTerm.isBlank() &&
                knownLetters.isBlank() && excludedLetters.isBlank() &&
                exactLength == null && customWords.isBlank()
    }

    data class Result(
        val totalMatches: Int,
        val words: List<String>,
        /** True als dit geen exacte match is op het patroon, maar een foutentolerante ("lijkt op") match. */
        val isApproximate: Boolean = false
    ) {
        val isTruncated: Boolean get() = totalMatches > words.size

        companion object {
            fun empty() = Result(0, emptyList())
        }
    }

    /** Zet een patroon (met wildcards) om naar een regex die een heel woord matcht. */
    fun buildPatternRegex(pattern: String): Regex {
        val sb = StringBuilder()
        for (c in pattern.lowercase()) {
            sb.append(if (c in WILDCARDS) "." else Regex.escape(c.toString()))
        }
        return Regex(sb.toString())
    }

    /** Splitst de "eigen woorden"-tekst op spaties, komma's en regeleinden. */
    fun parseCustomWords(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return raw.split(Regex("[,\\s]+"))
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
    }

    /** Zelfde letters, zelfde aantal, willekeurige volgorde. */
    private fun isAnagramMatch(letters: String, word: String): Boolean =
        letters.toList().sorted() == word.toList().sorted()

    /**
     * True als [word] op precies dezelfde lengte als [pattern] hooguit
     * [maxAfwijkingen] niet-wildcard posities anders heeft. Gebruikt voor de
     * foutentolerante fallback wanneer een patroon geen exacte match geeft
     * (bv. omdat je een letter van het cryptogram verkeerd hebt geraden).
     */
    private fun matchesApproximately(pattern: String, word: String, maxAfwijkingen: Int): Boolean {
        if (pattern.length != word.length) return false
        var afwijkingen = 0
        for (i in pattern.indices) {
            val p = pattern[i].lowercaseChar()
            if (p in WILDCARDS) continue
            if (p != word[i].lowercaseChar()) {
                afwijkingen++
                if (afwijkingen > maxAfwijkingen) return false
            }
        }
        return true
    }

    fun search(query: Query, source: WordSource): Result {
        if (query.isBlank) return Result.empty()

        val exact = zoek(query, source, approximate = false)
        // Alleen bij een patroon is een foutentolerante fallback zinvol
        // (bij zoekterm/bekende letters/anagram is er niets om "op te lijken").
        if (exact.totalMatches > 0 || query.pattern.isBlank()) return exact

        val approximate = zoek(query, source, approximate = true)
        return if (approximate.totalMatches > 0) approximate.copy(isApproximate = true) else exact
    }

    private fun zoek(query: Query, source: WordSource, approximate: Boolean): Result {
        val pattern = query.pattern.trim()
        val term = query.searchTerm.trim().lowercase()
        val knownLetters = query.knownLetters.trim().lowercase().filter { it.isLetter() }
        val knownLettersSet = knownLetters.toSet()
        val excludedLetters = query.excludedLetters.trim().lowercase()
            .filter { it.isLetter() }
            .toSet()

        // Bepaal de vereiste woordlengte (patroon > anagram-letters), voor
        // zowel de kandidatenselectie als het filteren van eigen woorden.
        val requiredLength: Int? = when {
            pattern.isNotEmpty() -> pattern.length
            query.anagramMode && knownLetters.isNotEmpty() -> knownLetters.length
            query.exactLength != null -> query.exactLength
            else -> null
        }

        val baseCandidates: List<String> =
            if (requiredLength != null) source.ofLength(requiredLength) else source.all()

        val customWords = parseCustomWords(query.customWords)
            .filter { requiredLength == null || it.length == requiredLength }

        val candidates: List<String> = if (customWords.isEmpty()) {
            baseCandidates
        } else {
            val known = HashSet(baseCandidates)
            baseCandidates + customWords.filterNot { it in known }
        }

        val regex = if (!approximate) pattern.takeIf { it.isNotEmpty() }?.let(::buildPatternRegex) else null

        val matches = ArrayList<String>()
        var total = 0
        for (word in candidates) {
            if (approximate) {
                if (pattern.isNotEmpty() && !matchesApproximately(pattern, word, FUZZY_MAX_AFWIJKINGEN)) continue
            } else {
                if (regex != null && !regex.matches(word)) continue
            }
            if (term.isNotEmpty() && term !in word) continue

            // De losse lengtefilter is alleen zinvol zonder patroon (het
            // patroon bepaalt de lengte al ondubbelzinnig).
            if (pattern.isEmpty() && query.exactLength != null && word.length != query.exactLength) continue

            if (knownLetters.isNotEmpty()) {
                val voldoetAanLetters = if (query.anagramMode) {
                    isAnagramMatch(knownLetters, word)
                } else {
                    knownLettersSet.all { it in word }
                }
                if (!voldoetAanLetters) continue
            }

            if (excludedLetters.isNotEmpty() && word.any { it in excludedLetters }) continue

            total++
            if (matches.size < MAX_RESULTS) matches.add(word)
        }

        return Result(totalMatches = total, words = matches)
    }
}
