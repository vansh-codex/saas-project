package com.friday.assistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = IndigoPrimary,
    secondary = TealSecondary,
    background = LightBackground,
    surface = LightSurface,
)

private val DarkColors = darkColorScheme(
    primary = IndigoPrimaryDark,
    secondary = TealSecondaryDark,
    background = DarkBackground,
    surface = DarkSurface,
)

@Composable
fun FridayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = FridayTypography,
        content = content,
    )
}
