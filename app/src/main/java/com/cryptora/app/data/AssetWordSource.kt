package com.cryptora.app.data

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 * Laadt een meegeleverde woordenlijst (één woord per regel, van meest naar
 * minst voorkomend gesorteerd) één keer in het geheugen en houdt daarnaast
 * een index per woordlengte bij, zodat een zoekopdracht met een patroon niet
 * door de hele lijst hoeft te bladeren.
 *
 * Er is per taal ([com.cryptora.app.data.Taal]) een eigen bronbestand
 * ([assetName], bv. `assets/woorden.txt.gz` voor Nederlands en
 * `assets/words_en.txt.gz` voor Engels) — gzip-gecomprimeerd voor een
 * kleinere repo en, bij distributie via een Android App Bundle/Play Store,
 * ook een kleinere download. Een ".gz"-bestand in de assets-map is een door
 * Android Gradle Plugin herkende speciale naam: het wordt automatisch
 * gedecomprimeerd tijdens het bouwen, en is in de app terug te vinden ONDER
 * DE NAAM ZONDER ".gz" — dus hieronder wordt [assetName] zonder ".gz"-suffix
 * verwacht, zonder zelf te hoeven decomprimeren.
 *
 * Bron: woordfrequentielijsten uit het pyspellchecker-project (MIT-licentie,
 * © Tyler Barrus), zie README.md voor de volledige attributie.
 */
class AssetWordSource(context: Context, private val assetName: String = "woorden.txt") : WordSource {

    private val appContext = context.applicationContext

    private val words: List<String> by lazy { loadWords() }

    private val byLength: Map<Int, List<String>> by lazy {
        words.groupBy { it.length }
    }

    private fun loadWords(): List<String> {
        val result = ArrayList<String>(350_000)
        val reader = BufferedReader(
            InputStreamReader(appContext.assets.open(assetName), StandardCharsets.UTF_8)
        )
        reader.useLines { lines ->
            for (line in lines) {
                val word = line.trim()
                if (word.isNotEmpty()) result.add(word)
            }
        }
        return result
    }

    override fun all(): List<String> = words

    override fun ofLength(length: Int): List<String> = byLength[length].orEmpty()

    override fun size(): Int = words.size
}
