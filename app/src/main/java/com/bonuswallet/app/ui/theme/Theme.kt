
package com.bonuswallet.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF171717),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF171717),
    secondary = Color(0xFF6B6B6B),
    background = Color(0xFFFAF9F6),
    surface = Color.White,
    surfaceVariant = Color(0xFFF2F0EB),
    outline = Color(0xFFE8E5E0),
    onBackground = Color(0xFF171717),
    onSurface = Color(0xFF171717)
)

private val DarkColors = darkColorScheme(
    primary = Color.White,
    onPrimary = Color(0xFF171717),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2A2A2A),
    outline = Color(0xFF3A3A3A),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun BonusWalletTheme(
    themeChoice: String = "system",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeChoice) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}
