package com.cryptora.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.cryptora.app.data.AppPreferences
import com.cryptora.app.data.AssetWordSource
import com.cryptora.app.data.PuzzleText
import com.cryptora.app.data.RecentSearch
import com.cryptora.app.data.SearchEngine
import com.cryptora.app.data.SorteerOptie
import com.cryptora.app.data.Taal
import com.cryptora.app.data.TekstGrootte
import com.cryptora.app.data.ThemaKeuze
import com.cryptora.app.data.WordSource
import com.cryptora.app.ui.Strings
import com.cryptora.app.ui.StringsEN
import com.cryptora.app.ui.StringsES
import com.cryptora.app.ui.StringsFR
import com.cryptora.app.ui.StringsNL

/** Aantal succesvolle zoekopdrachten voordat de In-App Review-flow (vriendelijk) wordt aangeboden. */
private const val REVIEW_DREMPEL = 5

class CryptogramViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = AppPreferences(application)

    /** Wordt pas gevuld nadat de woordenlijst op een achtergrondthread is ingeladen. */
    private var wordSource: WordSource? = null

    /** Eén keer geladen woordenlijst per taal, zodat wisselen na de eerste keer instant is. */
    private val woordenlijstPerTaal = mutableMapOf<Taal, WordSource>()

    var woordenlijstGereed by mutableStateOf(false)
        private set
    var woordenboekGrootte by mutableIntStateOf(0)
        private set
    /** True als het laden van de woordenlijst is mislukt (bv. corrupt asset-bestand). */
    var laadFout by mutableStateOf(false)
        private set

    /** Actieve UI-/woordenlijsttaal. Zie [Taal] en [Strings]. */
    var taal by mutableStateOf(preferences.taal)
        private set

    /** Alle UI-teksten in de actief gekozen taal. */
    val strings: Strings get() = when (taal) {
        Taal.NEDERLANDS -> StringsNL
        Taal.ENGELS -> StringsEN
        Taal.SPAANS -> StringsES
        Taal.FRANS -> StringsFR
    }

    /** Patroon in leesbare vorm; ij telt als één Nederlandse puzzelletter. */
    var patroon by mutableStateOf("")
        private set

    /**
     * Anagram-veld: alle letters die in het woord moeten voorkomen, in
     * willekeurige volgorde (zelfde aantal, zelfde letters). Onafhankelijk
     * van [patroon] — beide velden mogen tegelijk ingevuld zijn. Alleen
     * letters zijn toegestaan.
     */
    var anagram by mutableStateOf("")
        private set
    var zoekterm by mutableStateOf("")
        private set
    var uitgeslotenLetters by mutableStateOf("")
        private set
    var lengte by mutableStateOf("")
        private set
    var eigenWoorden by mutableStateOf(preferences.eigenWoorden.words(taal.name))
        private set

    var result by mutableStateOf(SearchEngine.Result.empty())
        private set
    var heeftGezocht by mutableStateOf(false)
        private set

    var tekstGrootte by mutableStateOf(preferences.tekstGrootte)
        private set

    var themaKeuze by mutableStateOf(preferences.themaKeuze)
        private set

    /** Alleen de recente zoekopdrachten van de actief gekozen taal (zie [RecentSearch.taal]). */
    var recenteZoekopdrachten by mutableStateOf(recenteZoekopdrachtenVoorHuidigeTaal())
        private set

    var sorteerOptie by mutableStateOf(preferences.sorteerOptie)
        private set

    /** True zolang de eenmalige onboarding-uitleg nog niet gezien/gesloten is. */
    var toonOnboarding by mutableStateOf(!preferences.heeftOnboardingGezien)
        private set

    /** Eenmalig (per installatie) "true" zodra het moment daar is om de In-App Review-flow te starten. */
    var vraagOmBeoordeling by mutableStateOf(false)
        private set

    init {
        laadEnActiveer(taal)
    }

    /**
     * Laadt de woordenlijst voor [doelTaal] (of gebruikt de al-geladen versie
     * uit [woordenlijstPerTaal]) en maakt hem actief. Het inlezen gebeurt
     * bewust niet op de main thread: dat zou op oudere/langzamere toestellen
     * een korte hapering kunnen geven — vooral de eerste keer per taal.
     */
    private fun laadEnActiveer(doelTaal: Taal) {
        val gecachet = woordenlijstPerTaal[doelTaal]
        if (gecachet != null) {
            wordSource = gecachet
            woordenboekGrootte = gecachet.size()
            woordenlijstGereed = true
            laadFout = false
            zoekLive()
            return
        }

        woordenlijstGereed = false
        wordSource = null
        laadFout = false
        result = SearchEngine.Result.empty()
        viewModelScope.launch {
            try {
                val source = withContext(Dispatchers.IO) {
                    // .size() forceert het inlezen van het bestand, .ofLength(0)
                    // forceert daarnaast de index-per-woordlengte — allebei nu
                    // vast op de IO-thread, zodat de eerste zoekopdracht niets
                    // meer hoeft te berekenen op de main thread.
                    AssetWordSource(
                        getApplication(),
                        assetName = doelTaal.assetBestand,
                        ijAsLetter = doelTaal == Taal.NEDERLANDS
                    ).also {
                        it.size()
                        it.ofLength(0)
                    }
                }
                woordenlijstPerTaal[doelTaal] = source
                // Alleen toepassen als de gebruiker ondertussen niet alweer
                // naar een andere taal is gewisseld terwijl dit nog laadde.
                if (taal == doelTaal) {
                    wordSource = source
                    woordenboekGrootte = source.size()
                    woordenlijstGereed = true
                    laadFout = false
                    zoekLive()
                }
            } catch (e: Exception) {
                // Bijvoorbeeld een corrupt of ontbrekend woordenlijst-bestand:
                // laat de UI een nette foutmelding tonen in plaats van de app te
                // laten crashen. Log de echte oorzaak naar Logcat (tag "Cryptora")
                // zodat die te vinden is tijdens het ontwikkelen/debuggen.
                android.util.Log.e("Cryptora", "Kon woordenlijst niet laden voor $doelTaal", e)
                if (taal == doelTaal) {
                    laadFout = true
                    // Voorkom dat er op de achtergrond nog steeds tegen de
                    // woordenlijst van de vorige taal gezocht wordt: zonder dit
                    // zou zoekLive() stiekem resultaten in de verkeerde taal
                    // blijven berekenen terwijl de UI de foutmelding toont.
                    wordSource = null
                    woordenlijstGereed = false
                }
            }
        }
    }

    /** Wisselt de UI-/woordenlijsttaal en past de zoekresultaten meteen aan. */
    fun zetTaal(nieuweTaal: Taal) {
        if (nieuweTaal == taal) return
        taal = nieuweTaal
        preferences.taal = nieuweTaal
        eigenWoorden = preferences.eigenWoorden.words(nieuweTaal.name)
        recenteZoekopdrachten = recenteZoekopdrachtenVoorHuidigeTaal()
        laadEnActiveer(nieuweTaal)
    }

    /** Alleen de bewaarde zoekopdrachten die bij de actief gekozen taal horen. */
    private fun recenteZoekopdrachtenVoorHuidigeTaal(): List<RecentSearch> =
        preferences.recenteZoekopdrachten().filter { it.taal == taal }

    /** Accentletters naar basisletters, alle ondersteunde jokers naar punten. */
    fun onPatroonChange(value: String) {
        patroon = PuzzleText.patternInput(value)
    }

    /** Dezelfde basisletters als de zoekmachine, met leesbare ij in de invoer. */
    fun onAnagramChange(value: String) {
        anagram = PuzzleText.lettersInput(value)
    }

    fun onZoektermChange(value: String) { zoekterm = value }
    fun onUitgeslotenLettersChange(value: String) { uitgeslotenLetters = value }

    fun onLengteChange(value: String) {
        // Alleen cijfers, maximaal 3 tekens (woorden zijn nooit langer dan circa 25 letters).
        if (value.length <= 3 && value.all { it.isDigit() }) lengte = value
    }

    fun voegEigenWoordenToe(value: String) {
        eigenWoorden = preferences.eigenWoorden.add(taal.name, value)
        zoekLive()
    }

    fun verwijderEigenWoord(woord: String) {
        eigenWoorden = preferences.eigenWoorden.remove(taal.name, woord)
        zoekLive()
    }

    private fun huidigeQuery(): SearchEngine.Query = SearchEngine.Query(
        pattern = patroon,
        searchTerm = zoekterm,
        knownLetters = anagram,
        excludedLetters = uitgeslotenLetters,
        exactLength = lengte.toIntOrNull(),
        // Het Anagram-veld is nu een zelfstandig veld (geen schakelaar meer):
        // zodra het gevuld is, betekent dat altijd "exact deze letters",
        // nooit meer het oude "bevat deze letters"-gedrag.
        anagramMode = true,
        customWords = eigenWoorden.joinToString("\n"),
        ijAsLetter = taal == Taal.NEDERLANDS
    )

    /** Draait live (met debounce) terwijl de gebruiker typt. */
    fun zoekLive() {
        val source = wordSource ?: return
        val query = huidigeQuery()
        // Opgeslagen woorden zijn kandidaten, geen zoekopdracht op zichzelf.
        val leeg = query.copy(customWords = "").isBlank
        heeftGezocht = !leeg
        val gevonden = if (leeg) SearchEngine.Result.empty() else SearchEngine.search(query, source)
        result = if (sorteerOptie == SorteerOptie.ALFABETISCH && gevonden.words.isNotEmpty()) {
            gevonden.copy(words = gevonden.words.sorted())
        } else {
            gevonden
        }
    }

    fun zetSorteerOptie(optie: SorteerOptie) {
        sorteerOptie = optie
        preferences.sorteerOptie = optie
        zoekLive()
    }

    fun onOnboardingGezien() {
        preferences.heeftOnboardingGezien = true
        toonOnboarding = false
    }

    /** Bewaart de huidige zoekopdracht in de historie (na een langere stilte tijdens het typen). */
    fun onZoekopdrachtBevestigd() {
        if (!heeftGezocht || result.totalMatches == 0) return
        val recent = RecentSearch(
            pattern = patroon.trim(),
            searchTerm = zoekterm.trim(),
            knownLetters = anagram.trim(),
            excludedLetters = uitgeslotenLetters.trim(),
            exactLength = lengte.toIntOrNull(),
            anagramMode = true,
            taal = taal
        )
        preferences.voegRecenteZoekopdrachtToe(recent)
        recenteZoekopdrachten = recenteZoekopdrachtenVoorHuidigeTaal()
        registreerSuccesvolleZoekopdracht()
    }

    /**
     * Telt succesvolle zoekopdrachten en zet [vraagOmBeoordeling] op "true"
     * zodra iemand de app een paar keer met succes heeft gebruikt — nooit bij
     * de allereerste zoekopdracht, en nooit meer dan één keer per installatie.
     */
    private fun registreerSuccesvolleZoekopdracht() {
        if (preferences.heeftOmBeoordelingGevraagd) return
        val nieuwAantal = preferences.succesvolleZoekopdrachten + 1
        preferences.succesvolleZoekopdrachten = nieuwAantal
        if (nieuwAantal >= REVIEW_DREMPEL) {
            preferences.heeftOmBeoordelingGevraagd = true
            vraagOmBeoordeling = true
        }
    }

    /** Door de UI aan te roepen nadat de In-App Review-flow is afgehandeld (gelukt, mislukt of overgeslagen). */
    fun beoordelingAfgehandeld() {
        vraagOmBeoordeling = false
    }

    fun pasRecenteZoekopdrachtToe(zoekopdracht: RecentSearch) {
        patroon = zoekopdracht.pattern
        anagram = zoekopdracht.knownLetters
        zoekterm = zoekopdracht.searchTerm
        uitgeslotenLetters = zoekopdracht.excludedLetters
        lengte = zoekopdracht.exactLength?.toString().orEmpty()
        zoekLive()
    }

    fun wisRecenteZoekopdrachten() {
        preferences.wisRecenteZoekopdrachten(taal)
        recenteZoekopdrachten = emptyList()
    }

    fun zetTekstGrootte(grootte: TekstGrootte) {
        tekstGrootte = grootte
        preferences.tekstGrootte = grootte
    }

    fun zetThemaKeuze(keuze: ThemaKeuze) {
        themaKeuze = keuze
        preferences.themaKeuze = keuze
    }

    /** Momentopname van de invoer, bewaard zodat "Alles wissen" ongedaan gemaakt kan worden. */
    private data class GewisteInvoer(
        val patroon: String,
        val anagram: String,
        val zoekterm: String,
        val uitgeslotenLetters: String,
        val lengte: String
    )

    private var laatsteWissing: GewisteInvoer? = null

    fun wisAlles() {
        laatsteWissing = GewisteInvoer(
            patroon = patroon,
            anagram = anagram,
            zoekterm = zoekterm,
            uitgeslotenLetters = uitgeslotenLetters,
            lengte = lengte
        )
        patroon = ""
        anagram = ""
        zoekterm = ""
        uitgeslotenLetters = ""
        lengte = ""
        result = SearchEngine.Result.empty()
        heeftGezocht = false
    }

    /** Herstelt de invoer van vóór de laatste "Alles wissen"-actie, indien beschikbaar. */
    fun herstelLaatsteWissing() {
        val vorige = laatsteWissing ?: return
        patroon = vorige.patroon
        anagram = vorige.anagram
        zoekterm = vorige.zoekterm
        uitgeslotenLetters = vorige.uitgeslotenLetters
        lengte = vorige.lengte
        laatsteWissing = null
        zoekLive()
    }
}
