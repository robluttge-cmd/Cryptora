# Cryptora voorbereiden voor Google Play

## Status en grenzen

Dit project is voorbereid voor een release met `applicationId` `com.cryptora.app`,
`compileSdk`/`targetSdk` 36 en minimaal Android 8 (API 26).
Versie: `1.0.0`, `versionCode` 1. Dit is alleen bruikbaar als code 1 nog niet
is geüpload voor deze app. Verhoog de code vóór iedere volgende Play-upload.
Verander het applicationId niet als Cryptora al in Play Console bestaat.

Een geslaagde technische build is geen publicatie of Play-goedkeuring.
De ongetekende `app-release.aab` uit de technische controle is **niet uploadklaar**.
Er is geen uploadkey aangemaakt en er zijn geen wachtwoorden opgeslagen.

## Lokaal gecontroleerd op 25 oktober 2026

| Controle | Uitkomst |
| --- | --- |
| JVM-tests (`testDebugUnitTest`) | 44 geslaagd, 0 fouten, 0 overgeslagen |
| Releasebuild met R8 en resource shrinking | Geslaagd |
| Release-lint | 0 fouten, 18 waarschuwingen; rapport beschikbaar |
| App Bundle-validatie met bundletool 1.18.2 | Geslaagd |
| Releasemanifest | Package correct, API 36, niet debuggable, geen internetpermissie |
| Woordenlijsten en licenties in bundle | Vier woordenlijsten en beide licentieteksten aanwezig |
| Native ELF LOAD-segmenten | Minimaal 16 KB-uitlijning voor alle vier ABI's |
| Bundleconfiguratie | `PAGE_ALIGNMENT_16K` |
| Ontbrekende signing | Verificatietaak weigert de uploadvoorbereiding zoals bedoeld |
| Ondertekening huidige AAB | Geen; niet uploadklaar |
| Toesteltest en Play Console-indiening | Niet uitgevoerd |
| GitHub Actions-run | Niet vanuit deze omgeving geverifieerd |

De ongetekende AAB van deze controle is 5.919.965 bytes groot, met SHA-256:
`c861f23837be2473c1ede3c8b7c7a996007901bc0485876955b14895436fc470`.
De buildbestanden staan buiten Git. Een latere build kan een andere checksum hebben.

De eerste releasecontrole vond een ontbrekende annotatie in `review-ktx:2.0.2`.
De beoordelingsfunctie gebruikt daarom nu rechtstreeks de publieke Task-API van
`review:2.0.2`. De release slaagt zonder ontbrekende-klassenwaarschuwingen te
onderdrukken. De echte beoordelingsdialoog moet nog via Google Play worden getest.

## Bouw en controle

Gebruik een volledige JDK 21 (de daemon is daarop vastgezet), Android SDK 36
met Build Tools 36.0.0 en de meegeleverde Gradle-wrapper:

```sh
./gradlew --max-workers=2 :app:testDebugUnitTest :app:lintRelease :app:bundleRelease
```

Op Windows: gebruik `gradlew.bat`. In de ingerichte cloudomgeving:

```sh
bash /workspace/.setup/cryptora-gradle :app:testDebugUnitTest :app:lintRelease :app:bundleRelease
```

Deze controles draaien de bestaande JVM-tests en bouwen de release met R8 en
resource shrinking. Deze AGP-configuratie biedt geen `testReleaseUnitTest`-taak;
de JVM-tests draaien tegen debugcode en vervangen geen test van de verkleinde
release op een toestel. Uitvoer:

- `app/build/outputs/bundle/release/app-release.aab`
- `app/build/outputs/mapping/release/mapping.txt` (bewaren per gepubliceerde versie)
- `app/build/reports/lint-results-release.html`
- `app/build/test-results/testDebugUnitTest/`

De GitHub Actions-workflow voert dezelfde technische controles uit en bewaart
het **ongetekende** bundle, R8-mapping en rapporten als workflow-artifacts. Het
bestaan van die workflow bewijst nog niet dat hij op GitHub is geslaagd.

## Ondertekenen met je eigen uploadkey

Gebruik voor een bestaande Play-app de geregistreerde uploadkey. Maak daarvoor
niet zomaar een nieuwe sleutel. Voor een eerste publicatie kun je lokaal via
Android Studio → Build → Generate Signed App Bundle / APK een uploadkeystore
maken en veilig bewaren. Kies Android App Bundle en de releasevariant.
Gebruik Play App Signing voor het beheer van de app signing key.

Als alternatief ondersteunt Gradle vier omgevingsvariabelen:

| Naam | Betekenis |
| --- | --- |
| `CRYPTORA_KEYSTORE_PATH` | Absoluut pad naar je bestaande uploadkeystore |
| `CRYPTORA_STORE_PASSWORD` | Wachtwoord van de keystore |
| `CRYPTORA_KEY_ALIAS` | Alias van de uploadkey |
| `CRYPTORA_KEY_PASSWORD` | Wachtwoord van de sleutel |

Voer waarden uitsluitend lokaal of via beveiligde CI-instellingen in. Zet nooit
sleutels of wachtwoorden in Git, een issue of een chatbericht. Bewaar een
beveiligde back-up van de keystore en de bijbehorende gegevens.

```sh
./gradlew :app:bundlePlayRelease
```

Deze taak weigert een Play-bundle te bouwen als variabelen ontbreken of het
keystorebestand niet bestaat. Er is geen terugval op een debugkey. Controleer
vóór upload bovendien dat `jarsigner -verify` de ondertekening van de AAB bevestigt
en dat deze sleutel bij de Play-app hoort; alleen een exitcode is onvoldoende
om een ongetekend JAR-bestand uit te sluiten.

## Privacy en storegegevens afronden

- Vul de openbare ontwikkelaarsnaam en het support-e-mailadres in in
  `PRIVACY_POLICY.md` en `docs/privacy.html`. De huidige tekst is een concept
  zolang de invulvelden bestaan. Publiceer die conceptversie niet.
- Host de definitieve HTML-privacyverklaring op een openbare HTTPS-URL. Een
  mogelijkheid is GitHub Pages met deze repository en de map `/docs` als bron.
  De URL is pas bruikbaar nadat Pages daadwerkelijk is ingeschakeld en getest.
- Geef dezelfde geldige URL op in Play Console. In de app staat een lokale
  privacyuitleg onder Instellingen → Privacy; voeg bij afronding ook de definitieve
  openbare verklaring/contactgegevens toe.
- De app bewaart geschiedenis, eigen woorden en voorkeuren lokaal. Eigen woorden
  blijven bewaard na afsluiten. Automatische back-up is uitgeschakeld en de
  voorkeuren zijn uitgesloten van cloudback-up en toesteloverdracht.
- De app bevat geen advertenties, account, analytics of internetpermissie.
  De eventuele review-flow loopt via Google Play. Controleer de definitieve
  dependencies en de actuele Data safety-vragen vóór je verklaringen bevestigt.
- Gebruik `STORE_LISTING.md` voor de vier talen. Er is geen volledige automatische
  interpretatie van cryptogramomschrijvingen: de tekst belooft alleen woordzoeken.
- Maak echte screenshots van de definitieve release: invoer, resultaten en eigen
  woorden. Gebruik geen mockups als bewijs van werkende schermen.
- Vul categorie, doelgroep, contentclassificatie, advertentieverklaring en app-toegang
  in volgens het daadwerkelijke gebruik. Een leeftijdsclassificatie ontstaat uit
  de officiële vragenlijst; een algemene woordenlijst garandeert geen categorie
  ‘alle leeftijden’ en maakt de app niet automatisch kindgericht.

## Testen vóór indiening

Test een via de interne testtrack geïnstalleerde, ondertekende release op ten
minste een ondersteund ouder toestel/API en een recente Android-versie:

- Eerste start, woordenlijst laden en geen internetverbinding.
- `ij.` → `ijs`; `cafe` → `café`; lengte 2 vindt `ijs`.
- Anagram `sij` → `ijs`, gecombineerd met patronen en uitgesloten letters.
- Eigen woorden toevoegen, herstarten, verwijderen en wisselen van taal.
- Wis invoer behoudt eigen woorden; ongedaan maken herstelt de invoer.
- Grote systeemtekst, toetsenbord, schermranden en recente grote schermen.
- Resultaten kopiëren, geschiedenis wissen en privacyuitleg openen.
- Geen crash bij optimalisatie met R8, ook na een koude start.
- Play Console/pre-launch report en, indien beschikbaar, een Android-apparaat
  met 16 KB-geheugenpagina's. Statische ELF-/packagingcontrole is geen toesteltest.

Voor bepaalde nieuwe persoonlijke ontwikkelaarsaccounts stelt Google eisen aan
een gesloten test en aanvraag voor productietoegang. Controleer de eisen en
testduur in je eigen Play Console; ze zijn accountafhankelijk.

## Actuele Google-eisen controleren

Officiële bronnen:

- https://support.google.com/googleplay/android-developer/answer/11926878
- https://developer.android.com/guide/practices/page-sizes
- https://support.google.com/googleplay/android-developer/answer/14151465
- https://support.google.com/googleplay/android-developer/answer/10144311

Deze pagina's gaven op 25 oktober 2026 vanuit de cloudomgeving HTTP 403.
Daarom is hier geen onbevestigde actuele minimum-API of accountgebonden
publicatie-eis als feit overgenomen. API 36 is de gekozen technische basis;
controleer de actuele voorwaarden bij indiening in Play Console.

## Wat nog buiten deze voorbereiding valt

Een eigen ondertekening, echte toestel-/Play-tracktests, screenshots, definitieve
openbare contactgegevens en privacy-URL, accountverklaringen en uiteindelijke
indiening zijn nog nodig. Het project maakt of publiceert geen Play Console-app.
