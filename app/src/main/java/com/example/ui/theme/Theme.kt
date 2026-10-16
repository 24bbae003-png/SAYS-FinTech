package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Teal500,
    onPrimary = Navy900,
    primaryContainer = Navy700,
    onPrimaryContainer = Teal100,
    secondary = Teal600,
    onSecondary = Color.White,
    secondaryContainer = Navy600,
    onSecondaryContainer = Teal100,
    tertiary = AmberWarning,
    onTertiary = Navy900,
    background = Navy900,
    onBackground = Slate50,
    surface = Navy800,
    onSurface = Slate50,
    surfaceVariant = Navy700,
    onSurfaceVariant = Slate200,
    outline = Slate600
)

private val LightColorScheme = lightColorScheme(
    primary = Navy800,
    onPrimary = Color.White,
    primaryContainer = Navy100,
    onPrimaryContainer = Navy900,
    secondary = Teal600,
    onSecondary = Color.White,
    secondaryContainer = Teal100,
    onSecondaryContainer = Teal700,
    tertiary = AmberWarning,
    onTertiary = Color.White,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = Slate900,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate300
)

@Composable
fun EduFeeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional Navy/Teal palette
    content: @Composable () -> Unit,
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
        typography = Typography,
        content = content
    )
}
