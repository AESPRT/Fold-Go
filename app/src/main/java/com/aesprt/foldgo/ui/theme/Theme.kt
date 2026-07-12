package com.aesprt.foldgo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DeepOceanBlueDark,
    onPrimary = Color.Black,
    primaryContainer = DeepOceanBlue,
    onPrimaryContainer = Color.White,
    
    secondary = MintGreenDark,
    onSecondary = Color.Black,
    secondaryContainer = MintGreen,
    onSecondaryContainer = Color.White,
    
    tertiary = Pink80,
    
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceDark.copy(alpha = 0.85f),
    
    error = ErrorCrimsonRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = DeepOceanBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = DeepOceanBlue,
    
    secondary = MintGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3FFE3),
    onSecondaryContainer = Color(0xFF003914),
    
    tertiary = Pink40,

    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceLight.copy(alpha = 0.7f),
    
    error = ErrorCrimsonRed,
    onError = Color.White
)

@Composable
fun FoldGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled by default for consistent branding
    dynamicColor: Boolean = false,
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
        typography = Typography
    ) {
        Surface(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            color = colorScheme.background,
            content = content
        )
    }
}
