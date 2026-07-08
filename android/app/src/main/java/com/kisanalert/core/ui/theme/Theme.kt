package com.kisanalert.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GreenPrimary: Color = Color(0xFF2E7D32)
private val GreenPrimaryDark: Color = Color(0xFF1B5E20)
private val GreenSecondary: Color = Color(0xFF43A047)
private val GreenTertiary: Color = Color(0xFFE8F5E9)
private val EarthBrown: Color = Color(0xFF6D4C41)
private val SkyBlue: Color = Color(0xFF1976D2)
private val WarningAmber: Color = Color(0xFFF57C00)
private val ErrorRed: Color = Color(0xFFD32F2F)

private val EnterpriseLightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = GreenPrimaryDark,
    secondary = EarthBrown,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5F0EE),
    onSecondaryContainer = Color(0xFF3E2723),
    tertiary = SkyBlue,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE3F2FD),
    onTertiaryContainer = Color(0xFF0D47A1),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),
    background = Color.White,
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFF5F7F5),
    onSurfaceVariant = Color(0xFF5F6368),
    outline = Color(0xFFE0E3E0),
    outlineVariant = Color(0xFFF0F2F0),
    scrim = Color(0x66000000),
    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF1F0F4),
    inversePrimary = GreenSecondary
)

object KisanColors {
    val WaterSaving: Color = Color(0xFF00897B)
    val CropHealthGood: Color = GreenSecondary
    val CropHealthWarning: Color = WarningAmber
    val CropHealthCritical: Color = ErrorRed
    val CardGradientStart: Color = GreenPrimary
    val CardGradientEnd: Color = GreenPrimaryDark
    val NavIndicator: Color = GreenPrimary
    val SectionDivider: Color = Color(0xFFE8EBE8)
    val CardBorder: Color = Color(0xFFE8EBE8)
    val AccentTint: Color = Color(0xFFF1F8F1)
}

@Composable
fun KrishakSevaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EnterpriseLightColorScheme,
        typography = KisanTypography,
        content = content
    )
}
