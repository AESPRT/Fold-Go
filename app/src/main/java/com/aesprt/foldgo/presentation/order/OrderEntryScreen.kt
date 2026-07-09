package com.aesprt.foldgo.presentation.order

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.aesprt.foldgo.presentation.components.ModernBackground

@Composable
fun OrderEntryScreen() {
    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // TODO: Add TopAppBar
            }
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Order Entry Screen (POS)", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}
