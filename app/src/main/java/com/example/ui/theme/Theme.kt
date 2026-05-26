package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SleekDigitalBlue,
    secondary = SleekSecondaryBlue,
    tertiary = SleekCreditGreen,
    background = SleekBackground,
    surface = Color(0xFF161618), // Subtle premium charcoal gray
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = DarkGreenText,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = SleekSurfaceVariant,
    onSurfaceVariant = TextSecondarySlate,
    error = SleekDebitRed
)

// Banking looks far superior in the "Sleek Interface" clean high-contrast palette.
// We map both systems to the premium Slate-White background aesthetic for a pristine look.
private val LightColorScheme = lightColorScheme(
    primary = SleekDigitalBlue,
    secondary = SleekSecondaryBlue,
    tertiary = SleekCreditGreen,
    background = SleekBackground,
    surface = Color(0xFF161618), // Subtle premium charcoal gray
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = DarkGreenText,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = SleekSurfaceVariant,
    onSurfaceVariant = TextSecondarySlate,
    error = SleekDebitRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to preserve strict premium gold styling
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
