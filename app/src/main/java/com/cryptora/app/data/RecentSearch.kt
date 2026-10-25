package com.cryptora.app.data

/**
 * Eén bewaarde zoekopdracht voor de "recente zoekopdrachten"-rij. Bewust
 * zonder "eigen woorden" (die zijn sessie-gebonden en hoeven niet blijvend
 * bewaard te worden).
 *
 * [taal] onthoudt in welke taal deze zoekopdracht is gedaan, zodat de
 * geschiedenis per taal apart getoond kan worden: een patroon dat in het
 * Engels is opgezocht heeft in het Nederlands geen betekenis (en andersom),
 * dus tonen we na een taalwissel alleen de zoekopdrachten die bij de actieve
 * taal horen. Standaardwaarde is Nederlands, zodat bestaande tests en oude
 * bewaarde zoekopdrachten (van vóór deze taal-uitbreiding) ongewijzigd
 * blijven werken.
 */
data class RecentSearch(
    val pattern: String,
    val searchTerm: String,
    val knownLetters: String,
    val excludedLetters: String,
    val exactLength: Int?,
    val anagramMode: Boolean,
    val taal: Taal = Taal.NEDERLANDS
) {
    /**
     * Korte, leesbare samenvatting voor een "chip" in de UI. De verbindende
     * woorden ("anagram", "letters", "niet") zijn injecteerbaar zodat de
     * aanroeper ze kan vertalen (zie [com.cryptora.app.ui.Strings]);
     * de standaardwaarden zijn Nederlands, zodat bestaande tests ongewijzigd
     * blijven werken.
     */
    fun label(
        anagramPrefix: String = "anagram",
        lettersPrefix: String = "letters",
        excludedPrefix: String = "niet",
        lettersSuffix: String = "letters"
    ): String {
        val delen = mutableListOf<String>()
        if (pattern.isNotBlank()) delen.add(pattern)
        if (searchTerm.isNotBlank()) delen.add("\"$searchTerm\"")
        if (knownLetters.isNotBlank()) {
            delen.add(if (anagramMode) "$anagramPrefix: $knownLetters" else "$lettersPrefix: $knownLetters")
        }
        if (excludedLetters.isNotBlank()) delen.add("$excludedPrefix: $excludedLetters")
        if (exactLength != null) delen.add("$exactLength $lettersSuffix")
        return if (delen.isEmpty()) "—" else delen.joinToString(" · ")
    }

    fun serialize(): String = listOf(
        pattern,
        searchTerm,
        knownLetters,
        excludedLetters,
        exactLength?.toString().orEmpty(),
        if (anagramMode) "1" else "0",
        taal.name
    ).joinToString(FIELD_SEPARATOR)

    companion object {
        private const val FIELD_SEPARATOR = ""

        fun deserialize(raw: String): RecentSearch? {
            val parts = raw.split(FIELD_SEPARATOR)
            // 6 velden = oude opslag van vóór de taal-uitbreiding (altijd Nederlands);
            // 7 velden = huidige opslag inclusief taal.
            if (parts.size != 6 && parts.size != 7) return null
            val taal = if (parts.size == 7) {
                runCatching { Taal.valueOf(parts[6]) }.getOrDefault(Taal.NEDERLANDS)
            } else {
                Taal.NEDERLANDS
            }
            return RecentSearch(
                pattern = parts[0],
                searchTerm = parts[1],
                knownLetters = parts[2],
                excludedLetters = parts[3],
                exactLength = parts[4].toIntOrNull(),
                anagramMode = parts[5] == "1",
                taal = taal
            )
        }
    }
}
