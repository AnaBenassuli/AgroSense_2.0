package com.agrosense.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AgroSenseLightColors = lightColorScheme(

    primary = AgroGreen,
    onPrimary = Color.White,

    primaryContainer = AgroGreenSoft,
    onPrimaryContainer = AgroGreenDark,

    secondary = AgroGreenDark,
    onSecondary = Color.White,

    secondaryContainer = AgroSurfaceAlt,
    onSecondaryContainer = AgroTextPrimary,

    background = AgroBackground,
    onBackground = AgroTextPrimary,

    surface = AgroSurface,
    onSurface = AgroTextPrimary,

    surfaceVariant = AgroSurfaceAlt,
    onSurfaceVariant = AgroTextSecondary,

    outline = AgroBorder,

    error = AgroError,
    onError = Color.White,
    errorContainer = AgroErrorSoft,
    onErrorContainer = AgroError
)

@Composable
fun AgroSenseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AgroSenseLightColors,
        typography = AgroTypography,
        content = content
    )
}