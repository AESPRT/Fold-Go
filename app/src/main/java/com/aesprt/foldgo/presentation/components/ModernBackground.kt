package com.aesprt.foldgo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.aesprt.foldgo.ui.theme.DeepOceanBlue
import com.aesprt.foldgo.ui.theme.MintGreen

@Composable
fun ModernBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    val backgroundColor = if (isDark) {
        MaterialTheme.colorScheme.background
    } else {
        Color.White
    }

    val gradientColors = if (isDark) {
        listOf(
            DeepOceanBlue.copy(alpha = 0.15f),
            backgroundColor,
            MintGreen.copy(alpha = 0.1f)
        )
    } else {
        listOf(
            DeepOceanBlue.copy(alpha = 0.08f),
            backgroundColor,
            MintGreen.copy(alpha = 0.05f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            )
    ) {
        content()
    }
}
