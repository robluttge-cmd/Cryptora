package com.cryptora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptora.app.ui.theme.CryptoraTheme

/** Intent-extra waarmee de "Nieuwe zoekopdracht"-app-snelkoppeling (long-press icoon) werkt. */
private const val EXTRA_NIEUWE_ZOEKOPDRACHT = "nieuwe_zoekopdracht"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Moet vóór super.onCreate() aangeroepen worden (AndroidX SplashScreen-
        // compat-vereiste): toont kort het Cryptora-beeldmerk (zie themes.xml)
        // terwijl de eerste woordenlijst nog op de achtergrond wordt geladen.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val moetWissen = intent?.getBooleanExtra(EXTRA_NIEUWE_ZOEKOPDRACHT, false) == true

        setContent {
            // Zelfde ViewModel-instantie als in CryptogramScreen (beide
            // gebruiken de standaard viewModel() binnen dezelfde Activity),
            // zodat de thema-keuze uit de instellingen ook echt het
            // kleurenschema van de app bepaalt.
            val viewModel: CryptogramViewModel = viewModel()

            LaunchedEffect(moetWissen) {
                if (moetWissen) viewModel.wisAlles()
            }

            CryptoraTheme(themaKeuze = viewModel.themaKeuze) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CryptogramScreen(viewModel = viewModel)
                }
            }
        }
    }
}
