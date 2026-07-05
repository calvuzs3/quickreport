package net.calvuz.qreport.app.app.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * QReport Theme — Arancio e Technical Design
 *
 * Tema personalizzato per QReport basato su Material Design 3, adattato dal
 * design system condiviso con QuickStore (vedi design/design-system.md):
 * arancio come unico accento di brand, grafite come inchiostro. I colori
 * semantici di stato/criticità/modulo (Color.kt) restano indipendenti da
 * questo tema, non sono influenzati dal color scheme M3.
 *
 * Features:
 * - Color scheme arancio/grafite, dynamic color disattivato di default
 * - Typography IBM Plex Sans/Mono su title/body/label/dati
 * - Dark/Light theme automatico
 */

// Primary colors - Arancio brand (riempimento). onPrimary = bianco in tema
// chiaro (il grafite appesantiva troppo i pulsanti pieni, richiesta esplicita
// dopo verifica su device — contrasto 2.66:1, sotto la regola d'inchiostro
// "pura" di design/design-system.md ma scelta deliberata qui).
private val md_theme_light_primary = TechnicalOrangeLight
private val md_theme_light_onPrimary = TechnicalSurfaceLight
private val md_theme_light_primaryContainer = Color(0xFFFBE3C2)
private val md_theme_light_onPrimaryContainer = TechnicalGraphite

// Secondary colors - ruolo neutro (nessun secondo hue): grigio graphite-toned
private val md_theme_light_secondary = TechnicalSlate
private val md_theme_light_onSecondary = TechnicalSurfaceLight
private val md_theme_light_secondaryContainer = TechnicalPaper
private val md_theme_light_onSecondaryContainer = TechnicalGraphite

// Tertiary colors - sfumatura più scura dell'arancio (stesso hue). onTertiary
// = bianco in chiaro, stesso motivo di onPrimary.
private val md_theme_light_tertiary = TechnicalOrangeDarkerLight
private val md_theme_light_onTertiary = TechnicalSurfaceLight
private val md_theme_light_tertiaryContainer = Color(0xFFFBE3C2)
private val md_theme_light_onTertiaryContainer = TechnicalGraphite

// Error colors - Rosso per NOK status
private val md_theme_light_error = Color(0xFFD32F2F)           // Red 700
private val md_theme_light_errorContainer = Color(0xFFFFEBEE)   // Red 50
private val md_theme_light_onError = Color(0xFFFFFFFF)
private val md_theme_light_onErrorContainer = Color(0xFFB71C1C) // Red 900

// Success colors - Verde per OK status
//val QReportGreen = Color(0xFF388E3C)                           // Green 700
val QReportGreenContainer = Color(0xFFE8F5E8)                  // Green 50
val QReportOnGreen = Color(0xFFFFFFFF)
val QReportOnGreenContainer = Color(0xFF1B5E20)                // Green 900

// Warning colors - Ambra per warning status
val QReportAmber = Color(0xFFFFA726)                           // Amber 400
val QReportAmberContainer = Color(0xFFFFF8E1)                  // Amber 50
val QReportOnAmber = Color(0xFF000000)
val QReportOnAmberContainer = Color(0xFFFF6F00)                // Amber 900

// Surface colors - Paper/grafite (vedi design/design-system.md)
private val md_theme_light_background = TechnicalPaper
private val md_theme_light_onBackground = TechnicalGraphite
private val md_theme_light_surface = TechnicalSurfaceLight
private val md_theme_light_onSurface = TechnicalGraphite
private val md_theme_light_surfaceVariant = TechnicalPaper
private val md_theme_light_onSurfaceVariant = TechnicalSlate
private val md_theme_light_outline = TechnicalBorder
private val md_theme_light_outlineVariant = TechnicalBorder

// Surface container family + ruoli "inverse"/scrim/tint — NON lasciarli mai
// impostati implicitamente: senza un valore esplicito, lightColorScheme()/
// darkColorScheme() li derivano dalla palette viola di riferimento di
// Material3 (surfaceTint incluso), non dalla nostra arancio/grafite — è
// quello che ha reso le DashboardSectionCard (surfaceContainerHigh) e
// altri elementi verificati su device chiaramente viola/lavanda invece che
// grigio neutro. Scala di grigi coerente con paper/border/grafite.
private val md_theme_light_surfaceContainerLowest = Color(0xFFFFFFFF)
private val md_theme_light_surfaceContainerLow = Color(0xFFF8F8F8)
private val md_theme_light_surfaceContainer = TechnicalPaper       // #F4F4F4
private val md_theme_light_surfaceContainerHigh = Color(0xFFECECEC)
private val md_theme_light_surfaceContainerHighest = TechnicalBorder // #E3E3E3
private val md_theme_light_surfaceDim = TechnicalBorder
private val md_theme_light_surfaceBright = Color(0xFFFFFFFF)
private val md_theme_light_surfaceTint = TechnicalOrangeLight
private val md_theme_light_inverseSurface = TechnicalGraphite
private val md_theme_light_inverseOnSurface = TechnicalPaper
private val md_theme_light_inversePrimary = TechnicalOrangeDark
private val md_theme_light_scrim = Color(0xFF000000)

// Dark theme colors - Arancio/grafite (vedi design/design-system.md)
private val md_theme_dark_primary = TechnicalOrangeDark
private val md_theme_dark_onPrimary = TechnicalGraphite // grafite in dark (non bianco come in chiaro): l'arancio di tema scuro è più chiaro, il bianco ci starebbe anche peggio (~2.1:1) del chiaro (2.66:1)
private val md_theme_dark_primaryContainer = Color(0xFF4A3417)
private val md_theme_dark_onPrimaryContainer = TechnicalTextDark

private val md_theme_dark_secondary = TechnicalTextSecondaryDark
private val md_theme_dark_onSecondary = TechnicalGraphite
private val md_theme_dark_secondaryContainer = TechnicalSurfaceDark
private val md_theme_dark_onSecondaryContainer = TechnicalTextDark

private val md_theme_dark_tertiary = TechnicalOrangeDarkerDark
private val md_theme_dark_onTertiary = TechnicalGraphite
private val md_theme_dark_tertiaryContainer = Color(0xFF4A3417)
private val md_theme_dark_onTertiaryContainer = TechnicalTextDark

private val md_theme_dark_error = Color(0xFFEF5350)            // Red 400
private val md_theme_dark_errorContainer = Color(0xFFD32F2F)   // Red 700
private val md_theme_dark_onError = Color(0xFFFFFFFF)
private val md_theme_dark_onErrorContainer = Color(0xFFFFEBEE) // Red 50

private val md_theme_dark_background = TechnicalBackgroundDark
private val md_theme_dark_onBackground = TechnicalTextDark
private val md_theme_dark_surface = TechnicalSurfaceDark
private val md_theme_dark_onSurface = TechnicalTextDark
private val md_theme_dark_surfaceVariant = TechnicalSurfaceDark
private val md_theme_dark_onSurfaceVariant = TechnicalTextSecondaryDark
private val md_theme_dark_outline = TechnicalBorderDark
private val md_theme_dark_outlineVariant = TechnicalBorderDark

// Vedi commento sopra (light) — stessa ragione, scala di grigi dark coerente.
private val md_theme_dark_surfaceContainerLowest = Color(0xFF141414)
private val md_theme_dark_surfaceContainerLow = Color(0xFF1F1F1F)
private val md_theme_dark_surfaceContainer = TechnicalSurfaceDark        // #262626
private val md_theme_dark_surfaceContainerHigh = Color(0xFF2F2F2F)
private val md_theme_dark_surfaceContainerHighest = TechnicalBorderDark  // #3A3A3A
private val md_theme_dark_surfaceDim = TechnicalBackgroundDark
private val md_theme_dark_surfaceBright = Color(0xFF3A3A3A)
private val md_theme_dark_surfaceTint = TechnicalOrangeDark
private val md_theme_dark_inverseSurface = TechnicalPaper
private val md_theme_dark_inverseOnSurface = TechnicalGraphite
private val md_theme_dark_inversePrimary = TechnicalOrangeLight
private val md_theme_dark_scrim = Color(0xFF000000)

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    outlineVariant = md_theme_light_outlineVariant,
    surfaceContainerLowest = md_theme_light_surfaceContainerLowest,
    surfaceContainerLow = md_theme_light_surfaceContainerLow,
    surfaceContainer = md_theme_light_surfaceContainer,
    surfaceContainerHigh = md_theme_light_surfaceContainerHigh,
    surfaceContainerHighest = md_theme_light_surfaceContainerHighest,
    surfaceDim = md_theme_light_surfaceDim,
    surfaceBright = md_theme_light_surfaceBright,
    surfaceTint = md_theme_light_surfaceTint,
    inverseSurface = md_theme_light_inverseSurface,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inversePrimary = md_theme_light_inversePrimary,
    scrim = md_theme_light_scrim,
)

private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    outlineVariant = md_theme_dark_outlineVariant,
    surfaceContainerLowest = md_theme_dark_surfaceContainerLowest,
    surfaceContainerLow = md_theme_dark_surfaceContainerLow,
    surfaceContainer = md_theme_dark_surfaceContainer,
    surfaceContainerHigh = md_theme_dark_surfaceContainerHigh,
    surfaceContainerHighest = md_theme_dark_surfaceContainerHighest,
    surfaceDim = md_theme_dark_surfaceDim,
    surfaceBright = md_theme_dark_surfaceBright,
    surfaceTint = md_theme_dark_surfaceTint,
    inverseSurface = md_theme_dark_inverseSurface,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    scrim = md_theme_dark_scrim,
)

/**
 * Tema principale QReport
 */
@Composable
fun QReportTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // La palette arancio/grafite è una scelta di brand deliberata (vedi
    // design/design-system.md) e i colori di stato (OK/NOK/Pending, vedi
    // Color.kt) sono segnali di sicurezza: devono restare identici su ogni
    // device, non essere sovrascritti dai colori del wallpaper.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = QReportTypography,
        content = content
    )
}

/**
 * Estensioni ColorScheme per colori custom QReport
 */
val ColorScheme.success: Color
    @Composable get() = QReportGreen

val ColorScheme.onSuccess: Color
    @Composable get() = QReportOnGreen

val ColorScheme.successContainer: Color
    @Composable get() = QReportGreenContainer

val ColorScheme.onSuccessContainer: Color
    @Composable get() = QReportOnGreenContainer

val ColorScheme.warning: Color
    @Composable get() = QReportAmber

val ColorScheme.onWarning: Color
    @Composable get() = QReportOnAmber

val ColorScheme.warningContainer: Color
    @Composable get() = QReportAmberContainer

val ColorScheme.onWarningContainer: Color
    @Composable get() = QReportOnAmberContainer