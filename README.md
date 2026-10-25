# Cryptora

Native Android-app (Kotlin + Jetpack Compose) om woorden te zoeken voor
cryptogrammen, in het Nederlands, Engels, Spaans en Frans. Eén invoerscherm
met twee losse hoofdvelden (Patroon en Anagram, elk met een realtime
teller), "Eigen woorden" en "Meer opties" (zoekterm, lengte, uitgesloten
letters) achter een eigen knop/dialoog, en een apart resultatenscherm —
volledig offline.

(Projectmap, `applicationId`/`namespace` en Kotlin-package heten nu
consistent "Cryptora"/"com.cryptora.app" — geen losse technische naam meer
naast de zichtbare appnaam.)

## Belangrijkste eigenschappen

- **Native Android app** (Kotlin/Compose) — vlot, werkt altijd offline,
  installeerbaar via de Play Store.
- **Vier taal-/woordenlijstversies**: Nederlands, Engels, Spaans en Frans,
  met een kiezer in Instellingen die zowel de UI-teksten als de doorzochte
  woordenlijst omzet — instant, zonder herstart van de app.
- **Grotere, slimmere woordenlijsten**: ± 430.000 Nederlandse, ± 160.000
  Engelse, ± 86.000 Spaanse en ± 140.000 Franse woorden, allemaal gesorteerd
  op gebruiksfrequentie, zodat veelvoorkomende woorden bovenaan de
  resultaten verschijnen in plaats van willekeurige volgorde.
- **Tik-om-te-kopiëren** op elk resultaat, met bevestiging.
- **Onthoudt je invoer** bij schermrotatie (state overleeft configuratie-
  wijzigingen, dankzij een ViewModel).
- **Volledig offline**: geen enkele netwerkpermissie, dus ook geen
  privacyrisico's — belangrijk als je de app breder wilt aanbieden.
- Kern-zoeklogica zit in een puur Kotlin-object (`SearchEngine`) zonder
  Android-afhankelijkheden, met unit tests (`app/src/test`) zodat je met
  vertrouwen wijzigingen kan doorvoeren.

## Zoekregels en eigen woorden

- In het Nederlands is `ij` één puzzelletter: `ijs` telt als 2 en `bijl`
  als 3. Een punt kan ook de hele `ij` vervangen (`.s` vindt `ijs`).
  In anagrammen blijft `ij` bij elkaar: `sij` vindt `ijs`, `isj` niet.
  Bij de andere talen blijven `i` en `j` afzonderlijke letters.
- Accentletters worden voor alle zoekfilters als basisletters gelezen:
  `cafe` vindt `café` en `naief` vindt `naïef`. Resultaten behouden hun
  oorspronkelijke schrijfwijze. Apostrofs en koppeltekens in woorden
  nemen geen puzzelvakje in: `fotos` vindt `foto's`.
- Patroon, anagram, lengte, zoekterm en uitgesloten letters gebruiken
  dezelfde regels. De tellers en lettermarkering volgen die ook.
  `?`, `_` en `-` worden in het patroonveld omgezet naar een punt.
- Zoeken en Wis invoer staan direct bij beide hoofdvelden. Zoeken kan
  ook via de zoektoets op het toetsenbord.
- Open Eigen woorden, voer een of meer woorden in en tik op Toevoegen.
  De woorden blijven per taal op het toestel bewaard, worden automatisch
  meegenomen bij het zoeken en verschijnen vóór de standaardwoorden.
  Verwijder een bewaard woord met het kruisje ernaast. Wis invoer wist
  alleen de zoekvelden en filters, niet de bewaarde woorden.

## Vereisten

- [Android Studio](https://developer.android.com/studio), met ondersteuning
  voor Android Gradle Plugin 9.2.1.
- Een volledige JDK 21 voor de vastgezette Gradle-daemon; Java-broncode blijft
  gecompileerd met Java 17-compatibiliteit.
- Android SDK 36 en Build Tools 36.0.0.

## Project openen

1. Open Android Studio → **Open** → kies de map `Cryptora`.
2. Laat Gradle synchroniseren. De wrapper staat in de repository en downloadt
   de vastgezette Gradle-versie. Installeer de gevraagde SDK-componenten.
   De eerste synchronisatie en build kunnen enkele minuten duren.
3. Run op een emulator of fysiek toestel via de groene ▶-knop.

## Projectstructuur

```
app/src/main/
  java/com/cryptora/app/
    MainActivity.kt              - opstartpunt, zet het Compose-thema neer
    CryptogramScreen.kt          - het volledige (enige) scherm
    CryptogramViewModel.kt       - status + verbindt UI met zoeklogica
    data/
      SearchEngine.kt            - patroon/zoekterm/letters-matching, incl. foutentolerante fallback (los van Android)
      MatchHighlighter.kt        - bepaalt welke letters in een resultaat gemarkeerd worden (los van Android)
      WordSource.kt              - interface voor "een woordenlijst"
      AssetWordSource.kt         - laadt een woordenlijst uit assets/ (parametriseerbaar per taal)
      AppPreferences.kt          - tekstgrootte, thema, taal, sorteeroptie, onboarding en recente zoekopdrachten
      RecentSearch.kt            - (de)serialisatie van een bewaarde zoekopdracht
    ui/
      Strings.kt                 - alle UI-teksten in NL/EN/ES/FR (zie "Taal & vertalingen")
      theme/                      - kleuren, typografie (Manrope), licht/donker thema
  assets/
    woorden.txt.gz                - de Nederlandse woordenlijst, gzip-gecomprimeerd (± 430k woorden)
    words_en.txt.gz                - de Engelse woordenlijst, gzip-gecomprimeerd (± 160k woorden)
    words_es.txt.gz                - de Spaanse woordenlijst, gzip-gecomprimeerd (± 86k woorden)
    words_fr.txt.gz                - de Franse woordenlijst, gzip-gecomprimeerd (± 140k woorden)
  res/font/                        - Manrope-lettertype (statisch gebundeld, 4 gewichten)
  res/xml/shortcuts.xml           - statische app-snelkoppeling ("Nieuwe zoekopdracht")
  res/drawable/ic_launcher_foreground.xml - app-icoon: vergrootglas/"C"-vorm (zie "Branding & merk")
  res/drawable/ic_cryptora_badge.xml      - zelfde beeldmerk + eigen kaartje, hergebruikt in de UI
app/src/test/                     - JVM unit tests voor SearchEngine, MatchHighlighter en RecentSearch
play-store-assets/                - 512×512 store-icoon + 1024×500 feature graphic
PRIVACY_POLICY.md                 - concept-privacyverklaring
STORE_LISTING.md                  - concepttekst voor de Play Store-vermelding
```

## Woordenlijst & attributie

`assets/woorden.txt.gz` (Nederlands), `assets/words_en.txt.gz` (Engels),
`assets/words_es.txt.gz` (Spaans) en `assets/words_fr.txt.gz` (Frans) komen
(groten)deels uit het open source
[pyspellchecker](https://github.com/barrust/pyspellchecker)-project
(MIT-licentie, © Tyler Barrus): de "resources"-woordenboeken van dat
project (niet de grotere, ongefilterde "full"-varianten), gesorteerd op
gebruiksfrequentie (alleen losse woorden met een gebruiksfrequentie, geen
brontekst zelf; meerwoordige combinaties zijn uitgefilterd). Anders dan bij
de Engelse lijst zijn bij Spaans en Frans diakritische tekens (á, é, ñ, ç,
…) bewust behouden, omdat die daar gewone letters zijn, geen incidentele
leenwoord-schrijfwijze. De MIT-licentie vereist dat deze attributie
behouden blijft — die staat
daarom zowel in de app (Instellingen → Over) als in `STORE_LISTING.md`.
Laat deze vermelding staan als je de app aanpast of doorontwikkelt.

Wil je zelf woorden toevoegen of verwijderen: pak het bestand uit
(`gunzip woorden.txt.gz`, `gunzip words_en.txt.gz`, `gunzip words_es.txt.gz`
resp. `gunzip words_fr.txt.gz`), bewerk het (gewoon een tekstbestand, één
woord per regel, van meest naar minst gebruikt gesorteerd — de volgorde
bepaalt welke resultaten bovenaan verschijnen) en comprimeer het weer terug
(`gzip -9 <bestand>.txt`).

## Taal & vertalingen

De app-inhoud (alle UI-teksten) bestaat in het Nederlands, Engels, Spaans
en Frans. De gebruiker kiest hiertussen met een kiezer in Instellingen
(`Taal`-enum in `AppPreferences.kt`), volledig los van de systeemtaal van
het toestel — dat is bewust zo gekozen omdat het instant moet voelen,
zonder dat de Activity opnieuw hoeft op te starten (zoals bij Android's
per-app-taal-mechanisme via `AppCompatDelegate` wel het geval zou zijn).

Alle vertaalbare tekst staat daarom in `ui/Strings.kt` (vier complete
`Strings`-objecten: `StringsNL`, `StringsEN`, `StringsES` en `StringsFR`,
dezelfde 69 velden in elk), beschikbaar in elk scherm via de
`LocalStrings`-CompositionLocal. Dit is bewust GEEN gebruik van Android's
`values(-en)/strings.xml`-mechanisme voor de in-app teksten. Twee kleine
strings.xml-bestanden blijven wel bestaan (`app_name`, de
snelkoppeling-labels): die worden door het besturingssysteem zelf getoond
(app-naam, lang-indrukken-menu) en volgen daarom terecht de systeemtaal van
het toestel, los van de taalkeuze in de app.

De `TaalKiezer` in Instellingen is een horizontaal scrollbare rij (nodig nu
er vier taalnamen naast elkaar staan, wat op smalle schermen niet altijd
past) en itereert generiek over `Taal.entries` — dus verschijnt een nieuwe
taal daar automatisch.

Wil je een vijfde taal toevoegen: voeg een nieuwe waarde toe aan de `Taal`-
enum (met een woordenlijst-bestand in `assets/`), maak een nieuw
`Strings`-object met dezelfde 69 velden als de bestaande vier, en breid de
`when`-logica in `CryptogramViewModel.strings` uit (de compiler dwingt die
exhaustiviteit al af). `TaalKiezer` zelf hoeft niet aangepast te worden.

## Branding & merk

Het app-icoon (`res/drawable/ic_launcher_foreground.xml`) is een vergrootglas
waarvan de lens bewust een opening heeft, zodat de vorm tegelijk als een "C"
(Cryptora) én als vergrootglas leest — de ring in bijna-wit, het handvat in
het merk-blauw als accent. Bewust minimalistisch gehouden voor leesbaarheid
op klein formaat (app-lade, Play Store-zoekresultaten). Een eerdere versie
had ook nog twee losse "letter-tegel"-vormen als knipoog naar het
woordpuzzel-karakter; die zijn geschrapt omdat ze in de praktijk als
rommelige losse blokjes oogden in plaats van als subtiele branding.

Hetzelfde beeldmerk komt terug in de app zelf, als herkenningspunt, via
`ic_cryptora_badge.xml`: dezelfde ring+handvat-vorm, nu gecombineerd met een
eigen afgeronde-vierkant "kaartje" in dezelfde twee marineblauwe tinten als
het app-icoon (nodig omdat er in de header/Instellingen/opstartscherm geen
losse adaptive-icon-achtergrondlaag bestaat om op te tekenen). Gebruikt via
`Image(...)` (dus met de eigen merkkleuren, bewust NIET getint met de
huidige themakleur — een logo hoort niet mee te veranderen met licht/donker)
in de header naast "Cryptora", in de "Over"-sectie van Instellingen, én op
het opstartscherm.

**Opstartscherm**: via de AndroidX SplashScreen-compatibiliteitslibrary
(`androidx.core:core-splashscreen`), werkt ook op Android 12 en ouder
(minSdk 26). `MainActivity` roept `installSplashScreen()` aan vóór
`super.onCreate()`; `themes.xml` bevat een los `Theme.Cryptora.Starting`
(parent `Theme.SplashScreen`) dat via `postSplashScreenTheme` automatisch
terugschakelt naar het echte `Theme.Cryptora` zodra de app klaar is —
precies de door de library gedocumenteerde opzet, in plaats van de
splash-attributen los in het hoofdthema te zetten.

**Play Store-beoordeling**: na een paar succesvolle zoekopdrachten
(zie `REVIEW_DREMPEL` in `CryptogramViewModel.kt`) start de app eenmalig
(per installatie) de officiële In-App Review-flow
(`com.google.android.play:review`, via de publieke Task-API). Dit is bewust geen eigen
popup: Google bepaalt zelf of/wanneer de systeemdialoog daadwerkelijk
verschijnt, dus dit voelt nooit opdringerig. Test dit via de instructies op
<https://developer.android.com/guide/playcore/in-app-review/test> — in een
gewone debug-installatie zal de echte beoordelingsdialoog vaak niet
verschijnen, dat is normaal gedrag van de Play Core-testtools.

## Lettertype & attributie

De app gebruikt **Manrope** (open source, SIL Open Font License) als
lettertype, statisch gebundeld in `app/src/main/res/font/` in vier
gewichten (Regular, SemiBold, Bold, ExtraBold) zodat de app volledig
offline blijft werken — geen "downloadable fonts" via Google Play
Services. De volledige licentietekst staat in
`licenses/Manrope-OFL.txt`; die vermelding staat ook in de app zelf
(Instellingen → Over).

Wil je het lettertype vervangen: haal de gewenste gewichten op (bv. via
Google Fonts), zet ze in `app/src/main/res/font/` en pas de
`FontFamily` in `ui/theme/Type.kt` aan.

**Let op voor wie hieraan verder bouwt**: een bestand met de extensie
`.gz` in de `assets`-map is voor Android Gradle Plugin een speciale naam
— het wordt automatisch gedecomprimeerd tijdens het bouwen, en de app moet
het dus openen ZONDER de `.gz`-extensie (zie `AssetWordSource.kt`, dat een
`assetName`-parameter accepteert zoals `"woorden.txt"` of `"words_en.txt"`,
nooit met `.gz`-suffix). Er hoeft dus ook geen `GZIPInputStream` in de code
te zitten. Dit is precies waar we tijdens het bouwen van deze app
tegenaan liepen (een `FileNotFoundException` doordat de code wél naar
`"woorden.txt.gz"` zocht) — hou hier rekening mee als je een
woordenlijst-bestand ooit hernoemt of vervangt.

## Tests draaien

```
./gradlew test
```

Dit draait onder meer `PuzzleSearchTest` (ij, accenten, leestekens,
patroon/anagram-combinaties en de echte Nederlandse woordenlijst),
`CustomWordStoreTest` (bewaren, opnieuw openen, verwijderen en taalscheiding),
`SearchEngineTest` (patronen met wildcards, zoektermen, bekende
letters, eigen woorden, sortering, foutentolerante fallback) en
`MatchHighlighterTest` (letter-markering in de resultaten), zonder
emulator nodig te hebben.

## Voorbereiding voor Google Play

Zie [PLAY_RELEASE.md](PLAY_RELEASE.md) voor de releasebuild, je eigen uploadkey,
Play App Signing, privacyverklaring, toesteltests en de resterende Console-stappen.
De technische releasecontrole gebruikt API 36, R8 en resource shrinking.

```sh
./gradlew --max-workers=2 :app:testDebugUnitTest :app:lintRelease :app:bundleRelease
```

Zonder eigen signing-instellingen is de resulterende AAB ongetekend en niet klaar
voor upload. `./gradlew :app:bundlePlayRelease` controleert de vereiste
ondertekening via omgevingsvariabelen en weigert te bouwen als die ontbreekt.
Bewaar de keystore buiten Git. `.gitignore` sluit lokale builds en sleutelbestanden uit.

- [STORE_LISTING.md](STORE_LISTING.md): bijgewerkte teksten voor vier talen en
  de nog te maken screenshots.
- [PRIVACY_POLICY.md](PRIVACY_POLICY.md): concept met de huidige lokale opslag;
  ontwikkelaarsnaam en supportadres moeten nog worden ingevuld.
- [docs/privacy.html](docs/privacy.html): bijbehorende conceptwebpagina, pas na
  invullen publiceren op een openbare URL.
- Instellingen → Privacy: lokale uitleg over gegevensgebruik en verwijderen.
- `.github/workflows/android-release.yml`: tests, release-lint en een ongetekende
  App Bundle bij wijzigingen op `main` en pull requests. Publiceert niets naar Play.

De actuele Play-eisen moeten vóór indiening in Play Console worden bevestigd;
een geslaagde lokale build vervangt geen ondertekende toesteltest of goedkeuring.
