package com.cryptora.app.ui

import com.cryptora.app.data.SorteerOptie
import com.cryptora.app.data.TekstGrootte
import com.cryptora.app.data.ThemaKeuze

/**
 * Alle tekst die in de Compose-UI zichtbaar is, per taal. Bewust GEEN
 * Android string-resources (values/values-en): de gebruiker schakelt de
 * taal handmatig om via een schakelaar in Instellingen, los van de
 * systeemtaal van het toestel, en dat moet instant voelen zonder dat er een
 * Activity opnieuw opgestart hoeft te worden. `app_name` en de
 * snelkoppeling-labels in res/values(-en)/strings.xml blijven wel gewone
 * resources: die worden door het besturingssysteem zelf getoond (app-naam,
 * lang-indrukken-menu) en volgen daarom terecht de systeemtaal.
 *
 * Zie [com.cryptora.app.data.Taal] voor hoe deze taal gekoppeld is
 * aan welke woordenlijst er wordt doorzocht.
 */
data class Strings(
    val headerSubtitle: String,
    val helpContentDescription: String,
    val settingsContentDescription: String,

    val patternTitle: String,
    val patternPlaceholder: String,
    val patternHelper: String,
    val patternCounter: (Int) -> String,

    val anagramTitle: String,
    val anagramPlaceholder: String,
    val anagramHelper: String,
    val anagramCounter: (Int) -> String,

    val customWordsTitle: String,
    val customWordsSubtitle: String,
    val moreOptionsTitle: String,
    val moreOptionsSubtitle: String,

    val searchButton: String,
    val clearAllButton: String,
    val clearAllContentDescription: String,

    val recentLabel: String,
    val recentClear: String,

    val onboardingGetStarted: String,
    val onboardingTitle: String,
    val onboardingIntro: String,
    val onboardingPatternBullet: String,
    val onboardingAnagramBullet: String,
    val onboardingCopyBullet: String,
    val onboardingReopenHint: String,

    val settingsTitle: String,
    val settingsClose: String,
    val settingsThemeTitle: String,
    val settingsTextSizeTitle: String,
    val settingsLanguageTitle: String,
    val settingsAboutTitle: String,
    val settingsAboutBody: String,
    val settingsVersionPrefix: String,
    val privacyTitle: String,
    val privacyBody: String,

    val moreOptionsSearchTermLabel: String,
    val moreOptionsSearchTermPlaceholder: String,
    val moreOptionsSearchTermHelper: String,
    val moreOptionsLengthLabel: String,
    val moreOptionsLengthPlaceholder: String,
    val moreOptionsLengthHelper: String,
    val moreOptionsExcludedLabel: String,
    val moreOptionsExcludedPlaceholder: String,
    val moreOptionsExcludedHelper: String,

    val customWordsDialogTitle: String,
    val customWordsDialogBody: String,
    val customWordsDialogPlaceholder: String,
    val customWordsAddButton: String,
    val customWordsEmptyText: String,
    val customWordsCount: (Int) -> String,
    val customWordsRemoveDescription: (String) -> String,

    val resultsBackContentDescription: String,
    val resultsTitle: String,
    val resultsLoadErrorText: String,
    val resultsLoadingText: String,
    val resultsStartTypingText: String,
    val resultsNoneWithPatternText: String,
    val resultsNoneGenericText: String,
    val resultsApproximateHeader: String,
    val resultsTruncatedHeader: (shown: Int, total: Int) -> String,
    val resultsTapToCopyText: String,
    val copyContentDescription: (String) -> String,
    val copiedSnackbarText: (String) -> String,

    val footerText: (Int) -> String,

    val undoSnackbarText: String,
    val undoSnackbarAction: String,

    val themaLabel: (ThemaKeuze) -> String,
    val tekstGrootteLabel: (TekstGrootte) -> String,
    val sorteerOptieLabel: (SorteerOptie) -> String,

    val recentAnagramPrefix: String,
    val recentLettersPrefix: String,
    val recentExcludedPrefix: String,
    val recentLettersSuffix: String
)

val StringsNL = Strings(
    headerSubtitle = "Vul één of meer velden hieronder in en druk op Zoeken.",
    helpContentDescription = "Uitleg",
    settingsContentDescription = "Instellingen",

    patternTitle = "Patroon",
    patternPlaceholder = "bijv. s.r.er of ......",
    patternHelper = "Punt = onbekende letter. IJ telt als één letter; é = e, ï = i.",
    patternCounter = { n -> if (n == 1) "1 letter" else "$n letters" },

    anagramTitle = "Anagram",
    anagramPlaceholder = "bijv. server",
    anagramHelper = "Gebruik alle letters. Houd ij bij elkaar als één letter; accenten tellen niet mee.",
    anagramCounter = { n -> if (n == 1) "1 letter" else "$n letters" },

    customWordsTitle = "Eigen woorden",
    customWordsSubtitle = "Beheer en zoek in je eigen woorden.",
    moreOptionsTitle = "Meer opties",
    moreOptionsSubtitle = "Zoekterm, lengte, uitgesloten letters",

    searchButton = "Zoeken",
    clearAllButton = "Wis invoer",
    clearAllContentDescription = "Wis invoer",

    recentLabel = "Recent",
    recentClear = "Wissen",

    onboardingGetStarted = "Aan de slag",
    onboardingTitle = "Welkom bij Cryptora",
    onboardingIntro = "Vul één of meer velden in, je hoeft niet alles te weten. " +
        "Druk op \"Zoeken\" om de bijpassende woorden te zien.",
    onboardingPatternBullet = "• Patroon: typ het woord met bekende letters, en gebruik een " +
        "punt (.) voor elke letter die je nog niet weet (bijv. \"s.r.er\").",
    onboardingAnagramBullet = "• Anagram: heb je alle letters, maar weet je niet de volgorde? " +
        "Typ ze allemaal in het Anagram-veld (bijv. \"server\"). Patroon en " +
        "Anagram mogen ook tegelijk ingevuld zijn.",
    onboardingCopyBullet = "• Tik op een gevonden woord om het direct te kopiëren.",
    onboardingReopenHint = "Deze uitleg kan je later altijd terugvinden via het ?-icoon.",

    settingsTitle = "Instellingen",
    settingsClose = "Sluiten",
    settingsThemeTitle = "Thema",
    settingsTextSizeTitle = "Tekstgrootte",
    settingsLanguageTitle = "Taal",
    settingsAboutTitle = "Over",
    settingsAboutBody = "Cryptora werkt volledig offline en verzamelt geen gegevens. " +
        "De woordenlijsten zijn gebaseerd op woordfrequentiedata uit het open " +
        "source pyspellchecker-project (MIT-licentie, © Tyler Barrus). Het " +
        "lettertype is Manrope (SIL Open Font License, © The Manrope Project " +
        "Authors).",
    settingsVersionPrefix = "Versie",
    privacyTitle = "Privacy",
    privacyBody = "Cryptora doorzoekt woorden volledig op je toestel. De app heeft geen internetpermissie, advertenties of analytics. De ontwikkelaar ontvangt geen zoekopdrachten of eigen woorden.\n\nDe app bewaart recente zoekopdrachten, eigen woorden per taal en je instellingen lokaal. Wis recente opdrachten via Recent → Wissen en verwijder eigen woorden met het kruisje. Wis invoer verwijdert je bewaarde woorden niet. Je kunt alle appgegevens verwijderen via de Android-instellingen of door de app te verwijderen. Automatische back-up van deze gegevens is uitgeschakeld.\n\nEen eventuele beoordelingsvraag wordt afgehandeld door Google Play; daarop is het privacybeleid van Google van toepassing.",

    moreOptionsSearchTermLabel = "Zoekterm",
    moreOptionsSearchTermPlaceholder = "bijv. serv",
    moreOptionsSearchTermHelper = "Het woord moet deze letters ergens bevatten.",
    moreOptionsLengthLabel = "Lengte",
    moreOptionsLengthPlaceholder = "bijv. 7",
    moreOptionsLengthHelper = "Alleen gebruikt als je geen patroon invult.",
    moreOptionsExcludedLabel = "Uitgesloten letters",
    moreOptionsExcludedPlaceholder = "bijv. xyz",
    moreOptionsExcludedHelper = "Woorden met deze letters worden uitgesloten.",

    customWordsDialogTitle = "Eigen woorden",
    customWordsDialogBody = "Voeg woorden toe met de knop Toevoegen. Ze worden per taal bewaard en automatisch meegenomen bij het zoeken. Wis invoer verwijdert je bewaarde woorden niet.",
    customWordsDialogPlaceholder = "Woorden gescheiden door spaties, komma's of nieuwe regels.",
    customWordsAddButton = "Toevoegen",
    customWordsEmptyText = "Je hebt nog geen eigen woorden toegevoegd.",
    customWordsCount = { n -> if (n == 1) "1 eigen woord bewaard" else "$n eigen woorden bewaard" },
    customWordsRemoveDescription = { woord -> "Verwijder $woord" },

    resultsBackContentDescription = "Terug naar zoekvelden",
    resultsTitle = "Resultaten",
    resultsLoadErrorText = "Kon de woordenlijst niet laden. Herstart de app; blijft dit " +
        "gebeuren, laat het ons weten via Instellingen.",
    resultsLoadingText = "Woordenlijst wordt geladen…",
    resultsStartTypingText = "Begin met typen om te zoeken.",
    resultsNoneWithPatternText = "Geen woorden gevonden, ook niet bij één afwijkende letter. " +
        "Probeer een ander patroon.",
    resultsNoneGenericText = "Geen woorden gevonden. Probeer andere velden.",
    resultsApproximateHeader = "Geen exacte match voor je patroon. Dit lijkt erop (1 letter anders):",
    resultsTruncatedHeader = { shown, total ->
        "Eerste $shown van $total resultaten (tik op een woord om te kopiëren)."
    },
    resultsTapToCopyText = "Tik op een woord om het te kopiëren.",
    copyContentDescription = { woord -> "Kopieer $woord" },
    copiedSnackbarText = { woord -> "'$woord' gekopieerd" },

    footerText = { n -> "Doorzoekt ± $n Nederlandse woorden, volledig offline op je toestel." },

    undoSnackbarText = "Zoekinvoer gewist",
    undoSnackbarAction = "Ongedaan maken",

    themaLabel = { keuze ->
        when (keuze) {
            ThemaKeuze.SYSTEEM -> "Systeem"
            ThemaKeuze.LICHT -> "Licht"
            ThemaKeuze.DONKER -> "Donker"
        }
    },
    tekstGrootteLabel = { optie ->
        when (optie) {
            TekstGrootte.KLEIN -> "Klein"
            TekstGrootte.NORMAAL -> "Normaal"
            TekstGrootte.GROOT -> "Groot"
        }
    },
    sorteerOptieLabel = { optie ->
        when (optie) {
            SorteerOptie.FREQUENTIE -> "Meest gebruikt"
            SorteerOptie.ALFABETISCH -> "A-Z"
        }
    },

    recentAnagramPrefix = "anagram",
    recentLettersPrefix = "letters",
    recentExcludedPrefix = "niet",
    recentLettersSuffix = "letters"
)

val StringsEN = Strings(
    headerSubtitle = "Fill in one or more fields below and press Search.",
    helpContentDescription = "Help",
    settingsContentDescription = "Settings",

    patternTitle = "Pattern",
    patternPlaceholder = "e.g. s.r.er or ......",
    patternHelper = "Use dots for unknown letters. Accents are treated as base letters.",
    patternCounter = { n -> if (n == 1) "1 character" else "$n characters" },

    anagramTitle = "Anagram",
    anagramPlaceholder = "e.g. server",
    anagramHelper = "Type all the letters in any order. Accents are treated as base letters.",
    anagramCounter = { n -> if (n == 1) "1 letter" else "$n letters" },

    customWordsTitle = "Custom words",
    customWordsSubtitle = "Manage and search your custom words.",
    moreOptionsTitle = "More options",
    moreOptionsSubtitle = "Search term, length, excluded letters",

    searchButton = "Search",
    clearAllButton = "Clear input",
    clearAllContentDescription = "Clear input",

    recentLabel = "Recent",
    recentClear = "Clear",

    onboardingGetStarted = "Get started",
    onboardingTitle = "Welcome to Cryptora",
    onboardingIntro = "Fill in one or more fields, you don't need to know everything. " +
        "Press \"Search\" to see the matching words.",
    onboardingPatternBullet = "• Pattern: type the word with the letters you know, and use a " +
        "dot (.) for every letter you don't know yet (e.g. \"s.r.er\").",
    onboardingAnagramBullet = "• Anagram: have all the letters but not sure of the order? " +
        "Type them all into the Anagram field (e.g. \"server\"). Pattern and " +
        "Anagram can also be filled in at the same time.",
    onboardingCopyBullet = "• Tap a matching word to copy it instantly.",
    onboardingReopenHint = "You can always find this explanation again via the ? icon.",

    settingsTitle = "Settings",
    settingsClose = "Close",
    settingsThemeTitle = "Theme",
    settingsTextSizeTitle = "Text size",
    settingsLanguageTitle = "Language",
    settingsAboutTitle = "About",
    settingsAboutBody = "Cryptora works fully offline and doesn't collect any data. " +
        "The word lists are based on word frequency data from the open source " +
        "pyspellchecker project (MIT license, © Tyler Barrus). The font is " +
        "Manrope (SIL Open Font License, © The Manrope Project Authors).",
    settingsVersionPrefix = "Version",
    privacyTitle = "Privacy",
    privacyBody = "Cryptora searches words entirely on your device. It has no internet permission, ads or analytics. The developer receives no searches or custom words.\n\nRecent searches, custom words per language and preferences are stored locally. Clear recent searches with Recent → Clear and remove custom words with the cross. Clear input keeps saved words. Remove all app data in Android settings or uninstall the app. Automatic backup of this data is disabled.\n\nAn optional rating prompt is handled by Google Play under Google’s privacy policy.",

    moreOptionsSearchTermLabel = "Search term",
    moreOptionsSearchTermPlaceholder = "e.g. serv",
    moreOptionsSearchTermHelper = "The word must contain these letters somewhere.",
    moreOptionsLengthLabel = "Length",
    moreOptionsLengthPlaceholder = "e.g. 7",
    moreOptionsLengthHelper = "Only used if you don't enter a pattern.",
    moreOptionsExcludedLabel = "Excluded letters",
    moreOptionsExcludedPlaceholder = "e.g. xyz",
    moreOptionsExcludedHelper = "Words containing these letters are excluded.",

    customWordsDialogTitle = "Custom words",
    customWordsDialogBody = "Use Add to save words for this language. They are automatically included in searches and kept when you clear the search input.",
    customWordsDialogPlaceholder = "Words separated by spaces, commas or new lines.",
    customWordsAddButton = "Add",
    customWordsEmptyText = "No custom words saved yet.",
    customWordsCount = { n -> if (n == 1) "1 custom word saved" else "$n custom words saved" },
    customWordsRemoveDescription = { word -> "Remove $word" },

    resultsBackContentDescription = "Back to search fields",
    resultsTitle = "Results",
    resultsLoadErrorText = "Couldn't load the word list. Restart the app; if this keeps " +
        "happening, let us know via Settings.",
    resultsLoadingText = "Loading word list…",
    resultsStartTypingText = "Start typing to search.",
    resultsNoneWithPatternText = "No words found, not even with one letter off. Try a " +
        "different pattern.",
    resultsNoneGenericText = "No words found. Try different fields.",
    resultsApproximateHeader = "No exact match for your pattern. This looks close (1 letter off):",
    resultsTruncatedHeader = { shown, total ->
        "First $shown of $total results (tap a word to copy it)."
    },
    resultsTapToCopyText = "Tap a word to copy it.",
    copyContentDescription = { woord -> "Copy $woord" },
    copiedSnackbarText = { woord -> "'$woord' copied" },

    footerText = { n -> "Searches ± $n English words, fully offline on your device." },

    undoSnackbarText = "Cleared everything",
    undoSnackbarAction = "Undo",

    themaLabel = { keuze ->
        when (keuze) {
            ThemaKeuze.SYSTEEM -> "System"
            ThemaKeuze.LICHT -> "Light"
            ThemaKeuze.DONKER -> "Dark"
        }
    },
    tekstGrootteLabel = { optie ->
        when (optie) {
            TekstGrootte.KLEIN -> "Small"
            TekstGrootte.NORMAAL -> "Normal"
            TekstGrootte.GROOT -> "Large"
        }
    },
    sorteerOptieLabel = { optie ->
        when (optie) {
            SorteerOptie.FREQUENTIE -> "Most common"
            SorteerOptie.ALFABETISCH -> "A-Z"
        }
    },

    recentAnagramPrefix = "anagram",
    recentLettersPrefix = "letters",
    recentExcludedPrefix = "excluding",
    recentLettersSuffix = "letters"
)

val StringsES = Strings(
    headerSubtitle = "Completa uno o más campos y pulsa Buscar.",
    helpContentDescription = "Ayuda",
    settingsContentDescription = "Ajustes",

    patternTitle = "Patrón",
    patternPlaceholder = "p. ej. s.r.er o ......",
    patternHelper = "Usa puntos para letras desconocidas. Los acentos no cuentan.",
    patternCounter = { n -> if (n == 1) "1 carácter" else "$n caracteres" },

    anagramTitle = "Anagrama",
    anagramPlaceholder = "p. ej. server",
    anagramHelper = "Escribe todas las letras en cualquier orden. Los acentos no cuentan.",
    anagramCounter = { n -> if (n == 1) "1 letra" else "$n letras" },

    customWordsTitle = "Palabras propias",
    customWordsSubtitle = "Gestiona y busca en tus propias palabras.",
    moreOptionsTitle = "Más opciones",
    moreOptionsSubtitle = "Término de búsqueda, longitud, letras excluidas",

    searchButton = "Buscar",
    clearAllButton = "Borrar campos",
    clearAllContentDescription = "Borrar campos",

    recentLabel = "Recientes",
    recentClear = "Borrar",

    onboardingGetStarted = "Empezar",
    onboardingTitle = "Bienvenido a Cryptora",
    onboardingIntro = "Completa uno o más campos, no necesitas saberlo todo. " +
        "Pulsa «Buscar» para ver las palabras que coinciden.",
    onboardingPatternBullet = "• Patrón: escribe la palabra con las letras que conoces y usa un " +
        "punto (.) para cada letra que aún no sepas (p. ej. «s.r.er»).",
    onboardingAnagramBullet = "• Anagrama: ¿tienes todas las letras pero no sabes el orden? " +
        "Escríbelas todas en el campo Anagrama (p. ej. «server»). Patrón y " +
        "Anagrama también se pueden completar a la vez.",
    onboardingCopyBullet = "• Toca una palabra encontrada para copiarla al instante.",
    onboardingReopenHint = "Puedes volver a consultar esta ayuda cuando quieras a través del icono ?.",

    settingsTitle = "Ajustes",
    settingsClose = "Cerrar",
    settingsThemeTitle = "Tema",
    settingsTextSizeTitle = "Tamaño del texto",
    settingsLanguageTitle = "Idioma",
    settingsAboutTitle = "Acerca de",
    settingsAboutBody = "Cryptora funciona totalmente sin conexión y no recopila ningún " +
        "dato. Las listas de palabras se basan en datos de frecuencia del " +
        "proyecto de código abierto pyspellchecker (licencia MIT, © Tyler " +
        "Barrus). La tipografía es Manrope (SIL Open Font License, © The " +
        "Manrope Project Authors).",
    settingsVersionPrefix = "Versión",
    privacyTitle = "Privacidad",
    privacyBody = "Cryptora busca palabras en tu dispositivo. No tiene permiso de internet, anuncios ni analítica. El desarrollador no recibe búsquedas ni palabras propias.\n\nLas búsquedas recientes, las palabras por idioma y los ajustes se guardan localmente. Borra el historial en Recientes → Borrar y las palabras con la cruz. Borrar campos conserva las palabras guardadas. Puedes borrar todos los datos en los ajustes de Android o desinstalando la app. La copia de seguridad automática está desactivada.\n\nGoogle Play gestiona las solicitudes de valoración según la política de privacidad de Google.",

    moreOptionsSearchTermLabel = "Término de búsqueda",
    moreOptionsSearchTermPlaceholder = "p. ej. serv",
    moreOptionsSearchTermHelper = "La palabra debe contener estas letras en algún lugar.",
    moreOptionsLengthLabel = "Longitud",
    moreOptionsLengthPlaceholder = "p. ej. 7",
    moreOptionsLengthHelper = "Solo se usa si no introduces un patrón.",
    moreOptionsExcludedLabel = "Letras excluidas",
    moreOptionsExcludedPlaceholder = "p. ej. xyz",
    moreOptionsExcludedHelper = "Se excluyen las palabras que contengan estas letras.",

    customWordsDialogTitle = "Palabras propias",
    customWordsDialogBody = "Pulsa Añadir para guardar palabras en este idioma. Se incluyen en las búsquedas y se conservan al borrar los campos.",
    customWordsDialogPlaceholder = "Palabras separadas por espacios, comas o saltos de línea.",
    customWordsAddButton = "Añadir",
    customWordsEmptyText = "Todavía no hay palabras propias guardadas.",
    customWordsCount = { n -> if (n == 1) "1 palabra guardada" else "$n palabras guardadas" },
    customWordsRemoveDescription = { word -> "Eliminar $word" },

    resultsBackContentDescription = "Volver a los campos de búsqueda",
    resultsTitle = "Resultados",
    resultsLoadErrorText = "No se pudo cargar la lista de palabras. Reinicia la app; si " +
        "esto sigue ocurriendo, avísanos a través de Ajustes.",
    resultsLoadingText = "Cargando la lista de palabras…",
    resultsStartTypingText = "Empieza a escribir para buscar.",
    resultsNoneWithPatternText = "No se han encontrado palabras, ni siquiera con una letra " +
        "distinta. Prueba con otro patrón.",
    resultsNoneGenericText = "No se han encontrado palabras. Prueba con otros campos.",
    resultsApproximateHeader = "No hay coincidencia exacta con tu patrón. Esto se parece (1 letra distinta):",
    resultsTruncatedHeader = { shown, total ->
        "Primeros $shown de $total resultados (toca una palabra para copiarla)."
    },
    resultsTapToCopyText = "Toca una palabra para copiarla.",
    copyContentDescription = { palabra -> "Copiar $palabra" },
    copiedSnackbarText = { palabra -> "«$palabra» copiado" },

    footerText = { n -> "Busca entre ± $n palabras en español, totalmente sin conexión en tu dispositivo." },

    undoSnackbarText = "Todo borrado",
    undoSnackbarAction = "Deshacer",

    themaLabel = { keuze ->
        when (keuze) {
            ThemaKeuze.SYSTEEM -> "Sistema"
            ThemaKeuze.LICHT -> "Claro"
            ThemaKeuze.DONKER -> "Oscuro"
        }
    },
    tekstGrootteLabel = { optie ->
        when (optie) {
            TekstGrootte.KLEIN -> "Pequeño"
            TekstGrootte.NORMAAL -> "Normal"
            TekstGrootte.GROOT -> "Grande"
        }
    },
    sorteerOptieLabel = { optie ->
        when (optie) {
            SorteerOptie.FREQUENTIE -> "Más usadas"
            SorteerOptie.ALFABETISCH -> "A-Z"
        }
    },

    recentAnagramPrefix = "anagrama",
    recentLettersPrefix = "letras",
    recentExcludedPrefix = "sin",
    recentLettersSuffix = "letras"
)

val StringsFR = Strings(
    headerSubtitle = "Remplissez un ou plusieurs champs ci-dessous et appuyez sur Rechercher.",
    helpContentDescription = "Aide",
    settingsContentDescription = "Paramètres",

    patternTitle = "Motif",
    patternPlaceholder = "ex. s.r.er ou ......",
    patternHelper = "Un point par lettre inconnue. Les accents ne comptent pas.",
    patternCounter = { n -> if (n == 1) "1 caractère" else "$n caractères" },

    anagramTitle = "Anagramme",
    anagramPlaceholder = "ex. server",
    anagramHelper = "Tapez toutes les lettres dans l'ordre voulu. Les accents ne comptent pas.",
    anagramCounter = { n -> if (n == 1) "1 lettre" else "$n lettres" },

    customWordsTitle = "Mots personnalisés",
    customWordsSubtitle = "Gérez et recherchez dans vos mots personnalisés.",
    moreOptionsTitle = "Plus d'options",
    moreOptionsSubtitle = "Terme de recherche, longueur, lettres exclues",

    searchButton = "Rechercher",
    clearAllButton = "Effacer saisie",
    clearAllContentDescription = "Effacer saisie",

    recentLabel = "Récents",
    recentClear = "Effacer",

    onboardingGetStarted = "Commencer",
    onboardingTitle = "Bienvenue sur Cryptora",
    onboardingIntro = "Remplissez un ou plusieurs champs, vous n'avez pas besoin de tout " +
        "savoir. Appuyez sur « Rechercher » pour voir les mots correspondants.",
    onboardingPatternBullet = "• Motif : tapez le mot avec les lettres que vous connaissez, et " +
        "utilisez un point (.) pour chaque lettre encore inconnue (ex. « s.r.er »).",
    onboardingAnagramBullet = "• Anagramme : vous avez toutes les lettres mais pas l'ordre ? " +
        "Tapez-les toutes dans le champ Anagramme (ex. « server »). Motif et " +
        "Anagramme peuvent aussi être remplis en même temps.",
    onboardingCopyBullet = "• Appuyez sur un mot trouvé pour le copier instantanément.",
    onboardingReopenHint = "Vous pouvez toujours retrouver cette explication via l'icône ?.",

    settingsTitle = "Paramètres",
    settingsClose = "Fermer",
    settingsThemeTitle = "Thème",
    settingsTextSizeTitle = "Taille du texte",
    settingsLanguageTitle = "Langue",
    settingsAboutTitle = "À propos",
    settingsAboutBody = "Cryptora fonctionne entièrement hors ligne et ne collecte aucune " +
        "donnée. Les listes de mots reposent sur des données de fréquence du " +
        "projet open source pyspellchecker (licence MIT, © Tyler Barrus). La " +
        "police utilisée est Manrope (SIL Open Font License, © The Manrope " +
        "Project Authors).",
    settingsVersionPrefix = "Version",
    privacyTitle = "Confidentialité",
    privacyBody = "Cryptora recherche des mots sur votre appareil. Aucune autorisation internet, publicité ou analyse d’usage. Le développeur ne reçoit ni recherches ni mots personnalisés.\n\nLes recherches récentes, les mots par langue et les préférences sont conservés localement. Effacez l’historique via Récents → Effacer et les mots avec la croix. Effacer saisie conserve les mots enregistrés. Supprimez toutes les données dans les paramètres Android ou en désinstallant l’application. La sauvegarde automatique est désactivée.\n\nGoogle Play gère les demandes d’avis selon les règles de confidentialité de Google.",

    moreOptionsSearchTermLabel = "Terme de recherche",
    moreOptionsSearchTermPlaceholder = "ex. serv",
    moreOptionsSearchTermHelper = "Le mot doit contenir ces lettres quelque part.",
    moreOptionsLengthLabel = "Longueur",
    moreOptionsLengthPlaceholder = "ex. 7",
    moreOptionsLengthHelper = "Utilisé uniquement si vous ne saisissez pas de motif.",
    moreOptionsExcludedLabel = "Lettres exclues",
    moreOptionsExcludedPlaceholder = "ex. xyz",
    moreOptionsExcludedHelper = "Les mots contenant ces lettres sont exclus.",

    customWordsDialogTitle = "Mots personnalisés",
    customWordsDialogBody = "Appuyez sur Ajouter pour enregistrer des mots pour cette langue. Ils sont inclus dans les recherches et conservés lorsque vous effacez la saisie.",
    customWordsDialogPlaceholder = "Mots séparés par des espaces, des virgules ou des retours à la ligne.",
    customWordsAddButton = "Ajouter",
    customWordsEmptyText = "Aucun mot personnalisé enregistré.",
    customWordsCount = { n -> if (n == 1) "1 mot enregistré" else "$n mots enregistrés" },
    customWordsRemoveDescription = { word -> "Supprimer $word" },

    resultsBackContentDescription = "Retour aux champs de recherche",
    resultsTitle = "Résultats",
    resultsLoadErrorText = "Impossible de charger la liste de mots. Redémarrez l'application ; " +
        "si le problème persiste, signalez-le via Paramètres.",
    resultsLoadingText = "Chargement de la liste de mots…",
    resultsStartTypingText = "Commencez à taper pour rechercher.",
    resultsNoneWithPatternText = "Aucun mot trouvé, même avec une lettre différente. Essayez " +
        "un autre motif.",
    resultsNoneGenericText = "Aucun mot trouvé. Essayez d'autres champs.",
    resultsApproximateHeader = "Aucune correspondance exacte avec votre motif. Voici ce qui s'en rapproche (1 lettre différente) :",
    resultsTruncatedHeader = { shown, total ->
        "Premiers $shown sur $total résultats (appuyez sur un mot pour le copier)."
    },
    resultsTapToCopyText = "Appuyez sur un mot pour le copier.",
    copyContentDescription = { mot -> "Copier $mot" },
    copiedSnackbarText = { mot -> "« $mot » copié" },

    footerText = { n -> "Recherche parmi ± $n mots français, entièrement hors ligne sur votre appareil." },

    undoSnackbarText = "Tout effacé",
    undoSnackbarAction = "Annuler",

    themaLabel = { keuze ->
        when (keuze) {
            ThemaKeuze.SYSTEEM -> "Système"
            ThemaKeuze.LICHT -> "Clair"
            ThemaKeuze.DONKER -> "Sombre"
        }
    },
    tekstGrootteLabel = { optie ->
        when (optie) {
            TekstGrootte.KLEIN -> "Petit"
            TekstGrootte.NORMAAL -> "Normal"
            TekstGrootte.GROOT -> "Grand"
        }
    },
    sorteerOptieLabel = { optie ->
        when (optie) {
            SorteerOptie.FREQUENTIE -> "Les plus courants"
            SorteerOptie.ALFABETISCH -> "A-Z"
        }
    },

    recentAnagramPrefix = "anagramme",
    recentLettersPrefix = "lettres",
    recentExcludedPrefix = "sans",
    recentLettersSuffix = "lettres"
)
