package com.example.proyectofinaldms.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary            = Blue40,
    onPrimary          = Color.White,
    primaryContainer   = Blue90,
    onPrimaryContainer = Blue10,
    secondary          = Teal40,
    onSecondary        = Color.White,
    secondaryContainer = Teal90,
    onSecondaryContainer = Blue10,
    tertiary           = Amber40,
    onTertiary         = Color.White,
    background         = Grey99,
    surface            = Grey99,
    surfaceVariant     = Grey95,
    onBackground       = Grey10,
    onSurface          = Grey10,
    onSurfaceVariant   = Grey20,
    error              = Red40,
    errorContainer     = Red90,
    onError            = Color.White,
    onErrorContainer   = Blue10,
    outline            = Color(0xFFB0B8C1)
)

private val DarkColorScheme = darkColorScheme(
    primary            = Blue80,
    onPrimary          = Blue20,
    primaryContainer   = Blue40,
    onPrimaryContainer = Blue90,
    secondary          = Teal80,
    onSecondary        = Blue10,
    tertiary           = Amber80,
    background         = Grey10,
    surface            = Grey10,
    onBackground       = Grey90,
    onSurface          = Grey90,
    error              = Red90,
    onError            = Red40,
)

@Composable
fun ProyectoFinalDMSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dynamic color desactivado: usamos nuestra paleta definida
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
