package com.aesprt.foldgo.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.ui.theme.FoldGoTheme

@Composable
fun FoldGoLoading(
    modifier: Modifier = Modifier,
    fullScreen: Boolean = false
) {
    if (fullScreen) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ExpressiveIndicator(modifier)
        }
    } else {
        ExpressiveIndicator(modifier)
    }
}

@Composable
private fun ExpressiveIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier.size(48.dp),
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = 6.dp,
        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        strokeCap = StrokeCap.Round
    )
}

@Preview(showBackground = true)
@Composable
fun FoldGoLoadingPreview() {
    FoldGoTheme {
        Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
            FoldGoLoading()
        }
    }
}
