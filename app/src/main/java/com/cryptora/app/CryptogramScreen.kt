package com.cryptora.app

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.cryptora.app.data.MatchHighlighter
import com.cryptora.app.data.RecentSearch
import com.cryptora.app.data.SearchEngine
import com.cryptora.app.data.SorteerOptie
import com.cryptora.app.data.Taal
import com.cryptora.app.data.TekstGrootte
import com.cryptora.app.data.ThemaKeuze
import com.cryptora.app.ui.StringsNL
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/** Debounce voor live zoeken: kort genoeg om "direct" aan te voelen. */
private val LIVE_SEARCH_DEBOUNCE = 200.milliseconds

/** Langere stilte voordat we een zoekopdracht in de historie bewaren (voorkomt ruis van halfgetypte patronen). */
private val HISTORY_DEBOUNCE = 1200.milliseconds

/** Vaste merknaam, bewust niet vertaald (blijft "Cryptora" in beide talen). */
private const val APP_DISPLAY_NAME = "Cryptora"

/** Actieve UI-taalteksten; wordt bovenin [CryptogramScreen] met de echte waarde overschreven. */
val LocalStrings = staticCompositionLocalOf { StringsNL }

/**
 * Zoekt de omringende [Activity] op vanuit een Compose [Context], die vaak
 * een [ContextWrapper] is (bv. rond een Activity heen). Nodig voor de
 * In-App Review API, die een echte Activity-referentie vereist.
 */
private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun CryptogramScreen(viewModel: CryptogramViewModel = viewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    var toonInstellingen by remember { mutableStateOf(false) }
    var toonHelpHandmatig by remember { mutableStateOf(false) }
    var toonMeerOpties by remember { mutableStateOf(false) }
    var toonEigenWoorden by remember { mutableStateOf(false) }
    var toonResultatenScherm by remember { mutableStateOf(false) }

    LaunchedEffect(
        viewModel.patroon,
        viewModel.anagram,
        viewModel.zoekterm,
        viewModel.uitgeslotenLetters,
        viewModel.lengte,
        viewModel.eigenWoorden,
        viewModel.woordenlijstGereed
    ) {
        delay(LIVE_SEARCH_DEBOUNCE)
        viewModel.zoekLive()
    }

    LaunchedEffect(
        viewModel.patroon,
        viewModel.anagram,
        viewModel.zoekterm,
        viewModel.uitgeslotenLetters,
        viewModel.lengte
    ) {
        delay(HISTORY_DEBOUNCE)
        viewModel.onZoekopdrachtBevestigd()
    }

    // Eenmalig (per gebruiker) vriendelijk om een Play Store-beoordeling
    // vragen, pas nadat iemand de app een paar keer met succes heeft
    // gebruikt — nooit bij de allereerste zoekopdracht. Gebruikt de
    // officiële In-App Review API: Google bepaalt zelf of de systeem-
    // dialoog daadwerkelijk verschijnt, dus dit voelt nooit opdringerig.
    LaunchedEffect(viewModel.vraagOmBeoordeling) {
        if (viewModel.vraagOmBeoordeling) {
            val activity = context.findActivity()
            if (activity != null) {
                try {
                    val manager = ReviewManagerFactory.create(context)
                    val reviewInfo = manager.requestReview()
                    manager.launchReview(activity, reviewInfo)
                } catch (e: Exception) {
                    // Mag de app nooit laten crashen of de gebruiker storen;
                    // gewoon overslaan als er iets misgaat (bv. geen Play
                    // Store beschikbaar op dit toestel).
                }
            }
            viewModel.beoordelingAfgehandeld()
        }
    }

    // Systeem-terugknop op het resultatenscherm gaat terug naar het invoerscherm
    // in plaats van de app af te sluiten.
    BackHandler(enabled = toonResultatenScherm) {
        toonResultatenScherm = false
    }

    // Combineer de systeem-lettergrootte (Android's eigen "grotere tekst"-instelling)
    // met onze eigen tekstgrootte-voorkeur, in plaats van die laatste de systeeminstelling
    // volledig te laten overschrijven. Cap de uitkomst zodat een extreme combinatie
    // (bv. systeem al op 130% + onze "Groot") niet opnieuw tot clipping-problemen leidt.
    val gecombineerdeSchaal = (density.fontScale * viewModel.tekstGrootte.schaal).coerceIn(0.85f, 2.2f)

    CompositionLocalProvider(
        LocalDensity provides Density(density.density, fontScale = gecombineerdeSchaal),
        LocalStrings provides viewModel.strings
    ) {
        val t = LocalStrings.current

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                if (toonResultatenScherm) {
                    ResultatenScherm(
                        woordenlijstGereed = viewModel.woordenlijstGereed,
                        laadFout = viewModel.laadFout,
                        heeftGezocht = viewModel.heeftGezocht,
                        result = viewModel.result,
                        patroon = viewModel.patroon,
                        sorteerOptie = viewModel.sorteerOptie,
                        onSorteerOptieKies = viewModel::zetSorteerOptie,
                        onTerugClick = { toonResultatenScherm = false },
                        onWoordGetikt = { woord ->
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            clipboard.setText(AnnotatedString(woord))
                            scope.launch {
                                snackbarHostState.showSnackbar(t.copiedSnackbarText(woord))
                            }
                        }
                    )
                } else {
                    InvoerScherm(
                        viewModel = viewModel,
                        onHelpClick = { toonHelpHandmatig = true },
                        onInstellingenClick = { toonInstellingen = true },
                        onEigenWoordenClick = { toonEigenWoorden = true },
                        onMeerOptiesClick = { toonMeerOpties = true },
                        onZoekenClick = {
                            focusManager.clearFocus()
                            viewModel.zoekLive()
                            toonResultatenScherm = true
                        },
                        onWissen = {
                            viewModel.wisAlles()
                            scope.launch {
                                // Met een actionLabel staat een Snackbar standaard oneindig
                                // lang op het scherm; forceer daarom zelf een timeout van 5s.
                                val timeoutJob = launch {
                                    delay(5.seconds)
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                }
                                val actie = snackbarHostState.showSnackbar(
                                    message = t.undoSnackbarText,
                                    actionLabel = t.undoSnackbarAction,
                                    duration = SnackbarDuration.Indefinite
                                )
                                timeoutJob.cancel()
                                if (actie == SnackbarResult.ActionPerformed) {
                                    viewModel.herstelLaatsteWissing()
                                }
                            }
                        }
                    )
                }
            }
        }

        if (toonInstellingen) {
            InstellingenDialog(
                themaKeuze = viewModel.themaKeuze,
                onThemaKies = viewModel::zetThemaKeuze,
                tekstGrootte = viewModel.tekstGrootte,
                onTekstGrootteKies = viewModel::zetTekstGrootte,
                taal = viewModel.taal,
                onTaalKies = viewModel::zetTaal,
                onSluiten = { toonInstellingen = false }
            )
        }

        if (toonMeerOpties) {
            MeerOptiesDialog(
                zoekterm = viewModel.zoekterm,
                onZoektermChange = viewModel::onZoektermChange,
                lengte = viewModel.lengte,
                onLengteChange = viewModel::onLengteChange,
                uitgeslotenLetters = viewModel.uitgeslotenLetters,
                onUitgeslotenLettersChange = viewModel::onUitgeslotenLettersChange,
                onSluiten = { toonMeerOpties = false }
            )
        }

        if (toonEigenWoorden) {
            EigenWoordenDialog(
                eigenWoorden = viewModel.eigenWoorden,
                onEigenWoordenChange = viewModel::onEigenWoordenChange,
                onSluiten = { toonEigenWoorden = false }
            )
        }

        if (viewModel.toonOnboarding || toonHelpHandmatig) {
            OnboardingDialog(
                onSluiten = {
                    if (viewModel.toonOnboarding) viewModel.onOnboardingGezien()
                    toonHelpHandmatig = false
                }
            )
        }
    }
}

@Composable
private fun InvoerScherm(
    viewModel: CryptogramViewModel,
    onHelpClick: () -> Unit,
    onInstellingenClick: () -> Unit,
    onEigenWoordenClick: () -> Unit,
    onMeerOptiesClick: () -> Unit,
    onZoekenClick: () -> Unit,
    onWissen: () -> Unit
) {
    val t = LocalStrings.current
    Column(
        modifier = Modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Header(onHelpClick = onHelpClick, onInstellingenClick = onInstellingenClick)

        Spacer(modifier = Modifier.height(20.dp))

        PatroonKaart(patroon = viewModel.patroon, onPatroonChange = viewModel::onPatroonChange)

        Spacer(modifier = Modifier.height(14.dp))

        AnagramKaart(anagram = viewModel.anagram, onAnagramChange = viewModel::onAnagramChange)

        Spacer(modifier = Modifier.height(14.dp))

        NavigatieRij(
            icoon = Icons.AutoMirrored.Filled.MenuBook,
            titel = t.customWordsTitle,
            subtitel = t.customWordsSubtitle,
            onClick = onEigenWoordenClick
        )

        Spacer(modifier = Modifier.height(10.dp))

        NavigatieRij(
            icoon = Icons.Filled.Tune,
            titel = t.moreOptionsTitle,
            subtitel = t.moreOptionsSubtitle,
            onClick = onMeerOptiesClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onZoekenClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text(
                text = t.searchButton,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onWissen,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.Clear, contentDescription = t.clearAllContentDescription, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(t.clearAllButton)
        }

        if (viewModel.recenteZoekopdrachten.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            RecenteZoekopdrachtenRij(
                items = viewModel.recenteZoekopdrachten,
                onKies = { viewModel.pasRecenteZoekopdrachtToe(it) },
                onWis = { viewModel.wisRecenteZoekopdrachten() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Footer(aantalWoorden = viewModel.woordenboekGrootte, gereed = viewModel.woordenlijstGereed)
    }
}

@Composable
private fun OnboardingDialog(onSluiten: () -> Unit) {
    val t = LocalStrings.current
    AlertDialog(
        onDismissRequest = onSluiten,
        confirmButton = {
            TextButton(onClick = onSluiten) { Text(t.onboardingGetStarted) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        title = { Text(t.onboardingTitle) },
        text = {
            Column {
                Text(
                    text = t.onboardingIntro,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = t.onboardingPatternBullet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = t.onboardingAnagramBullet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = t.onboardingCopyBullet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = t.onboardingReopenHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    )
}

@Composable
private fun Header(onHelpClick: () -> Unit, onInstellingenClick: () -> Unit) {
    val t = LocalStrings.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.ic_cryptora_badge),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = APP_DISPLAY_NAME,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = t.headerSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row {
            IconButton(onClick = onHelpClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = t.helpContentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onInstellingenClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = t.settingsContentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Het Patroon-veld: bekende letters op hun plek, een punt (.) voor elke
 * onbekende letter. Visueel benadrukt (getinte achtergrond) omdat dit
 * samen met Anagram de belangrijkste invoer is.
 */
@Composable
private fun PatroonKaart(patroon: String, onPatroonChange: (String) -> Unit) {
    val t = LocalStrings.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Extension,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = t.patternTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                TellerChip(tekst = t.patternCounter(patroon.length), getint = true)
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = patroon,
                onValueChange = onPatroonChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(t.patternPlaceholder, color = hintKleur()) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = veldKleuren(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = t.patternHelper,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Het Anagram-veld: alle beschikbare letters, willekeurige volgorde.
 * Onafhankelijk van het Patroon-veld — beide mogen tegelijk ingevuld zijn.
 */
@Composable
private fun AnagramKaart(anagram: String, onAnagramChange: (String) -> Unit) {
    val t = LocalStrings.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = t.anagramTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                TellerChip(tekst = t.anagramCounter(anagram.length), getint = false)
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = anagram,
                onValueChange = onAnagramChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(t.anagramPlaceholder, color = hintKleur()) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = veldKleuren(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = t.anagramHelper,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Kleine tellerpil rechtsboven in Patroon/Anagram, bv. "6 tekens". */
@Composable
private fun TellerChip(tekst: String, getint: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (getint) {
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Text(
            text = tekst,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (getint) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/** Rij met icoon, titel en subtitel die naar een apart scherm/dialoog navigeert (bv. "Eigen woorden"). */
@Composable
private fun NavigatieRij(
    icoon: ImageVector,
    titel: String,
    subtitel: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(
                        imageVector = icoon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(8.dp).size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = titel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = subtitel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MeerOptiesDialog(
    zoekterm: String,
    onZoektermChange: (String) -> Unit,
    lengte: String,
    onLengteChange: (String) -> Unit,
    uitgeslotenLetters: String,
    onUitgeslotenLettersChange: (String) -> Unit,
    onSluiten: () -> Unit
) {
    val t = LocalStrings.current
    AlertDialog(
        onDismissRequest = onSluiten,
        confirmButton = {
            TextButton(onClick = onSluiten) { Text(t.settingsClose) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        title = { Text(t.moreOptionsTitle) },
        text = {
            Column {
                VeldMetLabel(label = t.moreOptionsSearchTermLabel) {
                    OutlinedTextField(
                        value = zoekterm,
                        onValueChange = onZoektermChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(t.moreOptionsSearchTermPlaceholder, color = hintKleur()) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = veldKleuren(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = t.moreOptionsSearchTermHelper,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                VeldMetLabel(label = t.moreOptionsLengthLabel) {
                    OutlinedTextField(
                        value = lengte,
                        onValueChange = onLengteChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(t.moreOptionsLengthPlaceholder, color = hintKleur()) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = veldKleuren(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = t.moreOptionsLengthHelper,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                VeldMetLabel(label = t.moreOptionsExcludedLabel) {
                    OutlinedTextField(
                        value = uitgeslotenLetters,
                        onValueChange = onUitgeslotenLettersChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(t.moreOptionsExcludedPlaceholder, color = hintKleur()) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = veldKleuren(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = t.moreOptionsExcludedHelper,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

@Composable
private fun EigenWoordenDialog(
    eigenWoorden: String,
    onEigenWoordenChange: (String) -> Unit,
    onSluiten: () -> Unit
) {
    val t = LocalStrings.current
    AlertDialog(
        onDismissRequest = onSluiten,
        confirmButton = {
            TextButton(onClick = onSluiten) { Text(t.settingsClose) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        title = { Text(t.customWordsDialogTitle) },
        text = {
            Column {
                Text(
                    text = t.customWordsDialogBody,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = eigenWoorden,
                    onValueChange = onEigenWoordenChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    placeholder = { Text(t.customWordsDialogPlaceholder, color = hintKleur()) },
                    shape = RoundedCornerShape(14.dp),
                    colors = veldKleuren(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            }
        }
    )
}

@Composable
private fun InstellingenDialog(
    themaKeuze: ThemaKeuze,
    onThemaKies: (ThemaKeuze) -> Unit,
    tekstGrootte: TekstGrootte,
    onTekstGrootteKies: (TekstGrootte) -> Unit,
    taal: Taal,
    onTaalKies: (Taal) -> Unit,
    onSluiten: () -> Unit
) {
    val t = LocalStrings.current
    val context = LocalContext.current
    val versienaam = remember {
        try {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (_: Exception) {
            null
        }
    }

    AlertDialog(
        onDismissRequest = onSluiten,
        confirmButton = {
            TextButton(onClick = onSluiten) { Text(t.settingsClose) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        textContentColor = MaterialTheme.colorScheme.onBackground,
        title = { Text(t.settingsTitle) },
        text = {
            Column {
                Text(
                    text = t.settingsLanguageTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                TaalKiezer(huidig = taal, onKies = onTaalKies)

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = t.settingsThemeTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                ThemaKiezer(huidig = themaKeuze, onKies = onThemaKies)

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = t.settingsTextSizeTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                TekstGrootteKiezer(huidig = tekstGrootte, onKies = onTekstGrootteKies)

                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_cryptora_badge),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = t.settingsAboutTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = t.settingsAboutBody,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (versienaam != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${t.settingsVersionPrefix} $versienaam",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    )
}

@Composable
private fun TaalKiezer(huidig: Taal, onKies: (Taal) -> Unit) {
    // Horizontaal scrollbaar: met 4 talen (Nederlands/English/Español/
    // Français) past de rij niet altijd meer naast elkaar op een smal
    // schermpje — zo blijft elke chip toch volledig leesbaar i.p.v. dat hij
    // wordt afgekapt of naar een tweede regel duwt.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Taal.entries.forEach { optie ->
            KeuzeChip(
                // Bewust de eigen naam van de taal ("Nederlands"/"English"/
                // "Español"/"Français"), niet vertaald: zo herken je je taal
                // ook als de huidige UI-taal nog niet de jouwe is.
                tekst = optie.ownName,
                geselecteerd = optie == huidig,
                onClick = { onKies(optie) }
            )
        }
    }
}

@Composable
private fun ThemaKiezer(huidig: ThemaKeuze, onKies: (ThemaKeuze) -> Unit) {
    val t = LocalStrings.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ThemaKeuze.entries.forEach { optie ->
            KeuzeChip(
                tekst = t.themaLabel(optie),
                geselecteerd = optie == huidig,
                onClick = { onKies(optie) }
            )
        }
    }
}

@Composable
private fun TekstGrootteKiezer(huidig: TekstGrootte, onKies: (TekstGrootte) -> Unit) {
    val t = LocalStrings.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TekstGrootte.entries.forEach { optie ->
            KeuzeChip(
                tekst = t.tekstGrootteLabel(optie),
                geselecteerd = optie == huidig,
                onClick = { onKies(optie) }
            )
        }
    }
}

/** Kleine "pil"-knop zoals gebruikt in de taal-, thema- en tekstgrootte-kiezers. */
@Composable
private fun KeuzeChip(tekst: String, geselecteerd: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (geselecteerd) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
        border = if (geselecteerd) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = tekst,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (geselecteerd) FontWeight.Bold else FontWeight.Normal,
            color = if (geselecteerd) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
            // Verticale padding bewust ruim (14dp) voor een tikvlak dichtbij de
            // aanbevolen 48dp-minimum, ook voor gebruikers met minder precieze motoriek.
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
        )
    }
}

@Composable
private fun RecenteZoekopdrachtenRij(
    items: List<RecentSearch>,
    onKies: (RecentSearch) -> Unit,
    onWis: () -> Unit
) {
    val t = LocalStrings.current
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = t.recentLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onWis) {
                Text(t.recentClear, style = MaterialTheme.typography.bodySmall)
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { item ->
                AssistChip(
                    onClick = { onKies(item) },
                    label = {
                        Text(
                            item.label(
                                anagramPrefix = t.recentAnagramPrefix,
                                lettersPrefix = t.recentLettersPrefix,
                                excludedPrefix = t.recentExcludedPrefix,
                                lettersSuffix = t.recentLettersSuffix
                            )
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onBackground
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                )
            }
        }
    }
}

@Composable
private fun ResultatenScherm(
    woordenlijstGereed: Boolean,
    laadFout: Boolean,
    heeftGezocht: Boolean,
    result: SearchEngine.Result,
    patroon: String,
    sorteerOptie: SorteerOptie,
    onSorteerOptieKies: (SorteerOptie) -> Unit,
    onTerugClick: () -> Unit,
    onWoordGetikt: (String) -> Unit
) {
    val t = LocalStrings.current
    Column(
        modifier = Modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onTerugClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = t.resultsBackContentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = t.resultsTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            CountBadge(count = result.totalMatches)
        }

        if (heeftGezocht && woordenlijstGereed && !laadFout && result.totalMatches > 1) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SorteerOptie.entries.forEach { optie ->
                    KeuzeChip(
                        tekst = t.sorteerOptieLabel(optie),
                        geselecteerd = optie == sorteerOptie,
                        onClick = { onSorteerOptieKies(optie) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .animateContentSize()
        ) {
            when {
                laadFout -> LegeStaat(
                    tekst = t.resultsLoadErrorText,
                    icoon = Icons.Outlined.ErrorOutline,
                    modifier = Modifier.fillMaxSize()
                )
                !woordenlijstGereed -> LegeStaat(
                    tekst = t.resultsLoadingText,
                    ladend = true,
                    modifier = Modifier.fillMaxSize()
                )
                !heeftGezocht -> LegeStaat(
                    tekst = t.resultsStartTypingText,
                    icoon = Icons.Outlined.Search,
                    modifier = Modifier.fillMaxSize()
                )
                result.totalMatches == 0 -> LegeStaat(
                    tekst = if (patroon.isNotBlank()) t.resultsNoneWithPatternText else t.resultsNoneGenericText,
                    icoon = Icons.Outlined.SearchOff,
                    modifier = Modifier.fillMaxSize()
                )
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (result.isApproximate) {
                            Text(
                                text = t.resultsApproximateHeader,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        } else if (result.isTruncated) {
                            Text(
                                text = t.resultsTruncatedHeader(result.words.size, result.totalMatches),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        } else {
                            Text(
                                text = t.resultsTapToCopyText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(result.words) { woord ->
                                WoordRij(
                                    woord = woord,
                                    patroon = patroon,
                                    onClick = { onWoordGetikt(woord) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Bouwt de woordtekst op met de letters die matchen met het patroon gemarkeerd. */
@Composable
private fun matchAnnotatedWoord(woord: String, patroon: String): AnnotatedString {
    val matchKleur = MaterialTheme.colorScheme.secondary
    val gemarkeerd = MatchHighlighter.matchedIndices(woord, patroon)

    return buildAnnotatedString {
        for (i in woord.indices) {
            if (i in gemarkeerd) {
                withStyle(SpanStyle(color = matchKleur, fontWeight = FontWeight.Bold)) {
                    append(woord[i])
                }
            } else {
                append(woord[i])
            }
        }
    }
}

@Composable
private fun WoordRij(
    woord: String,
    patroon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = LocalStrings.current
    // Ruime tegels (in plaats van compacte rijtjes): makkelijker te raken
    // voor gebruikers met minder precieze motoriek, en fijner voor
    // eenhandig gebruik onderweg.
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = matchAnnotatedWoord(woord, patroon),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = t.copyContentDescription(woord),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun LegeStaat(
    tekst: String,
    modifier: Modifier = Modifier,
    icoon: ImageVector? = null,
    ladend: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when {
                ladend -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                icoon != null -> {
                    Icon(
                        imageVector = icoon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            Text(
                text = tekst,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CountBadge(count: Int) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = if (count > SearchEngine.MAX_RESULTS) "${SearchEngine.MAX_RESULTS}+" else count.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun VeldMetLabel(label: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

/** Gedempte kleur voor placeholder-/hint-tekst, afgeleid van het huidige thema. */
@Composable
private fun hintKleur() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)

@Composable
private fun veldKleuren() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedBorderColor = MaterialTheme.colorScheme.secondary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedTextColor = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
    cursorColor = MaterialTheme.colorScheme.secondary
)

@Composable
private fun Footer(aantalWoorden: Int, gereed: Boolean) {
    if (!gereed) return
    val t = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Text(
            text = t.footerText(aantalWoorden),
            style = MaterialTheme.typography.bodySmall,
            color = hintKleur(),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
