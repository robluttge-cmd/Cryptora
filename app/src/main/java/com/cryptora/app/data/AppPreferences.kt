package com.cryptora.app.data

import android.content.Context

/**
 * Tekstgrootte-opties, met een schaalfactor die op de systeemlettergrootte
 * wordt toegepast. De weergavenaam hangt af van de actieve taal en wordt
 * daarom NIET hier bewaard, maar in [com.cryptora.app.ui.Strings]
 * (zie ook [ThemaKeuze] en [SorteerOptie], zelfde reden).
 */
enum class TekstGrootte(val schaal: Float) {
    KLEIN(0.9f),
    NORMAAL(1.0f),
    GROOT(1.25f)
}

/** Thema-opties. "Systeem" volgt de donker/licht-instelling van het toestel. */
enum class ThemaKeuze {
    SYSTEEM,
    LICHT,
    DONKER
}

/** Sorteervolgorde van de resultatenlijst. */
enum class SorteerOptie {
    FREQUENTIE,
    ALFABETISCH
}

/**
 * De taal van de app-inhoud: bepaalt zowel de UI-teksten
 * ([com.cryptora.app.ui.Strings]) als welke woordenlijst wordt
 * doorzocht. [ownName] is bewust NIET vertaald: een taalkeuzemenu toont
 * elke taal traditioneel in zijn eigen taal, zodat je 'm ook herkent als de
 * huidige UI-taal (nog) niet de jouwe is.
 */
enum class Taal(val ownName: String, val assetBestand: String) {
    NEDERLANDS("Nederlands", "woorden.txt"),
    ENGELS("English", "words_en.txt"),
    SPAANS("Español", "words_es.txt"),
    FRANS("Français", "words_fr.txt")
}

/**
 * Kleine SharedPreferences-wrapper voor instellingen die geen eigen
 * datastore-library rechtvaardigen: tekstgrootte, taal en recente
 * zoekopdrachten.
 */
class AppPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var tekstGrootte: TekstGrootte
        get() {
            val naam = prefs.getString(KEY_TEKSTGROOTTE, null) ?: return TekstGrootte.NORMAAL
            return runCatching { TekstGrootte.valueOf(naam) }.getOrDefault(TekstGrootte.NORMAAL)
        }
        set(value) {
            prefs.edit().putString(KEY_TEKSTGROOTTE, value.name).apply()
        }

    var themaKeuze: ThemaKeuze
        get() {
            val naam = prefs.getString(KEY_THEMA, null) ?: return ThemaKeuze.SYSTEEM
            return runCatching { ThemaKeuze.valueOf(naam) }.getOrDefault(ThemaKeuze.SYSTEEM)
        }
        set(value) {
            prefs.edit().putString(KEY_THEMA, value.name).apply()
        }

    var sorteerOptie: SorteerOptie
        get() {
            val naam = prefs.getString(KEY_SORTEEROPTIE, null) ?: return SorteerOptie.FREQUENTIE
            return runCatching { SorteerOptie.valueOf(naam) }.getOrDefault(SorteerOptie.FREQUENTIE)
        }
        set(value) {
            prefs.edit().putString(KEY_SORTEEROPTIE, value.name).apply()
        }

    var taal: Taal
        get() {
            val naam = prefs.getString(KEY_TAAL, null) ?: return Taal.NEDERLANDS
            return runCatching { Taal.valueOf(naam) }.getOrDefault(Taal.NEDERLANDS)
        }
        set(value) {
            prefs.edit().putString(KEY_TAAL, value.name).apply()
        }

    /** True zodra de gebruiker de eenmalige onboarding-uitleg heeft gezien/gesloten. */
    var heeftOnboardingGezien: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_GEZIEN, false)
        set(value) {
            prefs.edit().putBoolean(KEY_ONBOARDING_GEZIEN, value).apply()
        }

    /** Aantal succesvol afgeronde zoekopdrachten, gebruikt om het juiste moment te bepalen voor de Play Store-beoordelingsvraag. */
    var succesvolleZoekopdrachten: Int
        get() = prefs.getInt(KEY_SUCCESVOLLE_ZOEKOPDRACHTEN, 0)
        set(value) {
            prefs.edit().putInt(KEY_SUCCESVOLLE_ZOEKOPDRACHTEN, value).apply()
        }

    /** True zodra er al eenmaal (per installatie) om een beoordeling is gevraagd. */
    var heeftOmBeoordelingGevraagd: Boolean
        get() = prefs.getBoolean(KEY_BEOORDELING_GEVRAAGD, false)
        set(value) {
            prefs.edit().putBoolean(KEY_BEOORDELING_GEVRAAGD, value).apply()
        }

    fun recenteZoekopdrachten(): List<RecentSearch> {
        val raw = prefs.getString(KEY_RECENTE, null) ?: return emptyList()
        if (raw.isEmpty()) return emptyList()
        return raw.split(ITEM_SEPARATOR).mapNotNull { RecentSearch.deserialize(it) }
    }

    fun voegRecenteZoekopdrachtToe(zoekopdracht: RecentSearch, maxAantal: Int = 8) {
        val huidige = recenteZoekopdrachten().toMutableList()
        huidige.removeAll { it == zoekopdracht }
        huidige.add(0, zoekopdracht)
        val beperkt = huidige.take(maxAantal)
        prefs.edit()
            .putString(KEY_RECENTE, beperkt.joinToString(ITEM_SEPARATOR) { it.serialize() })
            .apply()
    }

    /**
     * Wist alleen de bewaarde zoekopdrachten van [taal] — de geschiedenis van
     * de andere taal blijft intact, zodat "Wis geschiedenis" in Instellingen
     * niet per ongeluk ook de opgebouwde geschiedenis van de andere taal
     * weggooit.
     */
    fun wisRecenteZoekopdrachten(taal: Taal) {
        val overig = recenteZoekopdrachten().filterNot { it.taal == taal }
        if (overig.isEmpty()) {
            prefs.edit().remove(KEY_RECENTE).apply()
        } else {
            prefs.edit()
                .putString(KEY_RECENTE, overig.joinToString(ITEM_SEPARATOR) { it.serialize() })
                .apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "cryptora_prefs"
        private const val KEY_TEKSTGROOTTE = "tekstgrootte"
        private const val KEY_THEMA = "thema_keuze"
        private const val KEY_RECENTE = "recente_zoekopdrachten"
        private const val KEY_SORTEEROPTIE = "sorteer_optie"
        private const val KEY_ONBOARDING_GEZIEN = "onboarding_gezien"
        private const val KEY_TAAL = "taal"
        private const val KEY_SUCCESVOLLE_ZOEKOPDRACHTEN = "succesvolle_zoekopdrachten"
        private const val KEY_BEOORDELING_GEVRAAGD = "beoordeling_gevraagd"

        // Scheidingsteken tussen bewaarde zoekopdrachten — een teken dat in
        // de praktijk nooit in patronen/letters/zoektermen voorkomt.
        private const val ITEM_SEPARATOR = "§§"
    }
}
