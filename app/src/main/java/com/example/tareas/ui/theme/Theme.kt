package com.example.tareas.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = SapphirePrimary,
    onPrimary = SapphireOnPrimary,
    primaryContainer = SapphirePrimaryContainer,
    onPrimaryContainer = SapphireOnPrimaryContainer,
    secondary = SapphireSecondary,
    onSecondary = SapphireOnSecondary,
    secondaryContainer = SapphireSecondaryContainer,
    onSecondaryContainer = SapphireOnSecondaryContainer,
    tertiary = SapphireTertiary,
    onTertiary = SapphireOnTertiary,
    tertiaryContainer = SapphireTertiaryContainer,
    onTertiaryContainer = SapphireOnTertiaryContainer,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight
)

private val DarkColorScheme = darkColorScheme(
    primary = SapphirePrimaryDark,
    onPrimary = SapphireOnPrimaryDark,
    primaryContainer = SapphirePrimaryContainerDark,
    onPrimaryContainer = SapphireOnPrimaryContainerDark,
    secondary = SapphireSecondaryDark,
    onSecondary = SapphireOnSecondaryDark,
    secondaryContainer = SapphireSecondaryContainerDark,
    onSecondaryContainer = SapphireOnSecondaryContainerDark,
    tertiary = SapphirePrimaryDark,
    onTertiary = SapphireOnPrimaryDark,
    tertiaryContainer = SapphirePrimaryContainerDark,
    onTertiaryContainer = SapphireOnPrimaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark
)

@Composable
fun TareasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Desactivado por defecto para respetar la paleta Sapphire nightfall whisper
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
