package com.clmf.player.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// CLMF Player brand palette: deep navy/graphite background, electric-blue primary,
// cyan-neon accent — matching the app logo's blue/chrome look.
val ClmfBackground = Color(0xFF060B18)
val ClmfSurface = Color(0xFF0F1B2E)
val ClmfSurfaceVariant = Color(0xFF16273F)
val ClmfPrimary = Color(0xFF1565D8)
val ClmfAccent = Color(0xFF29D9FF)
val ClmfError = Color(0xFFFF5252)
val ClmfOnSurface = Color(0xFFE6EEF7)
val ClmfOnSurfaceMuted = Color(0xFF8CA3BF)

val ClmfTileLive = Color(0xFF0D6EFD)
val ClmfTileMovies = Color(0xFF1447B3)
val ClmfTileSeries = Color(0xFF0FAEE0)
val ClmfTileContinue = Color(0xFF0A2E6B)

private val ClmfDarkColorScheme = darkColorScheme(
    primary = ClmfPrimary,
    secondary = ClmfAccent,
    tertiary = ClmfAccent,
    background = ClmfBackground,
    surface = ClmfSurface,
    surfaceVariant = ClmfSurfaceVariant,
    onBackground = ClmfOnSurface,
    onSurface = ClmfOnSurface,
    error = ClmfError
)

private val ClmfLightColorScheme = lightColorScheme(
    primary = ClmfPrimary,
    secondary = ClmfAccent,
    tertiary = ClmfAccent
)

val ClmfTypography = androidx.compose.material3.Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp)
)

@Composable
fun CLMFPlayerTheme(
    // CLMF Player always defaults to its dark identity regardless of system
    // theme; a future Settings toggle can switch this to isSystemInDarkTheme().
    useDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) ClmfDarkColorScheme else ClmfLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ClmfTypography,
        content = content
    )
}
