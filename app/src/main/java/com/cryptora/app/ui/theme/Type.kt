package com.cryptora.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cryptora.app.R

/**
 * Manrope (SIL Open Font License) — statisch gebundeld in res/font/ zodat de
 * app volledig offline blijft werken (geen "downloadable fonts" via Google
 * Play Services, wat een netwerkverbinding zou vereisen). Gekozen voor een
 * strakkere, chiquere uitstraling dan het standaard systeemlettertype
 * (Roboto), met behoud van goede leesbaarheid.
 */
private val ManropeFamily = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold)
)

// Startpunt voor de stijlen die we hieronder niet zelf aanpassen: zo krijgen
// ook die (bv. bodyLarge, headlineSmall) automatisch Manrope, in plaats van
// stilzwijgend op het standaard systeemlettertype terug te vallen.
private val Basis = Typography()

val AppTypography = Typography(
    displayLarge = Basis.displayLarge.copy(fontFamily = ManropeFamily),
    displayMedium = Basis.displayMedium.copy(fontFamily = ManropeFamily),
    displaySmall = Basis.displaySmall.copy(fontFamily = ManropeFamily),
    headlineLarge = TextStyle(
        fontFamily = ManropeFamily,
        // Geen negatieve letterSpacing: in combinatie met ExtraBold en een
        // grote tekstgrootte kon de laatste letter net buiten de rand
        // vallen en afgeknipt worden.
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = Basis.headlineMedium.copy(fontFamily = ManropeFamily),
    headlineSmall = Basis.headlineSmall.copy(fontFamily = ManropeFamily, fontWeight = FontWeight.Bold),
    titleLarge = Basis.titleLarge.copy(fontFamily = ManropeFamily),
    titleMedium = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = Basis.titleSmall.copy(fontFamily = ManropeFamily),
    bodyLarge = Basis.bodyLarge.copy(fontFamily = ManropeFamily),
    bodyMedium = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.6.sp
    ),
    labelMedium = Basis.labelMedium.copy(fontFamily = ManropeFamily),
    labelSmall = Basis.labelSmall.copy(fontFamily = ManropeFamily)
)
