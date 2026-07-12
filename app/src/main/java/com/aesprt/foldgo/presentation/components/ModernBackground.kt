package com.aesprt.foldgo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import com.aesprt.foldgo.ui.theme.FoldGoTheme

@Composable
fun ModernBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme
    
    val backgroundColor = colorScheme.background

    val gradientColors = if (isDark) {
        listOf(
            colorScheme.primary.copy(alpha = 0.15f),
            backgroundColor,
            colorScheme.secondary.copy(alpha = 0.1f)
        )
    } else {
        listOf(
            colorScheme.primary.copy(alpha = 0.08f),
            backgroundColor,
            colorScheme.secondary.copy(alpha = 0.05f)
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

@Preview(showBackground = true)
@Composable
fun ModernBackgroundPreview() {
    FoldGoTheme {
        ModernBackground {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Modern Background Content")
            }
        }
    }
}
