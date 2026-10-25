package com.cryptora.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.cryptora.app.data.ThemaKeuze

private val DarkColors = darkColorScheme(
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnBackgroundDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    primary = PrimaryDark,
    onPrimary = Color.White,
    secondary = SecondaryDark,
    onSecondary = Color.White,
    error = ErrorDark,
    onError = Color.White
)

private val LightColors = lightColorScheme(
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnBackgroundLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    primary = PrimaryLight,
    onPrimary = Color.White,
    secondary = SecondaryLight,
    onSecondary = Color.White,
    error = ErrorLight,
    onError = Color.White
)

@Composable
fun CryptoraTheme(themaKeuze: ThemaKeuze = ThemaKeuze.SYSTEEM, content: @Composable () -> Unit) {
    val donkerModus = when (themaKeuze) {
        ThemaKeuze.SYSTEEM -> isSystemInDarkTheme()
        ThemaKeuze.LICHT -> false
        ThemaKeuze.DONKER -> true
    }
    MaterialTheme(
        colorScheme = if (donkerModus) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
