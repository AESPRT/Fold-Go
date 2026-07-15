package com.aesprt.foldgo.presentation.components

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.ui.navigation.*

@Composable
fun TabletScaffold(
    currentRoute: String?,
    onNavigate: (Any) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            header = {
                Box(modifier = Modifier.padding(vertical = 24.dp)) {
                    FoldGoLogo(iconSize = 40.dp, showText = false)
                }
            },
            modifier = Modifier.width(80.dp)
        ) {
            NavigationRailItem(
                selected = currentRoute?.contains(DashboardRoute::class.qualifiedName ?: "") == true,
                onClick = { onNavigate(DashboardRoute) },
                icon = { Icon(Icons.Rounded.Dashboard, contentDescription = "Dashboard") },
                label = { Text("Dashboard") }
            )
            
            NavigationRailItem(
                selected = currentRoute?.contains(MachineMatrixRoute::class.qualifiedName ?: "") == true,
                onClick = { onNavigate(MachineMatrixRoute) },
                icon = { Icon(Icons.Rounded.LocalLaundryService, contentDescription = "Machines") },
                label = { Text("Machines") }
            )
            
            NavigationRailItem(
                selected = currentRoute?.contains(HistoryRoute::class.qualifiedName ?: "") == true,
                onClick = { onNavigate(HistoryRoute) },
                icon = { Icon(Icons.Rounded.History, contentDescription = "History") },
                label = { Text("History") }
            )
            
            NavigationRailItem(
                selected = currentRoute?.contains(SettingsRoute::class.qualifiedName ?: "") == true,
                onClick = { onNavigate(SettingsRoute) },
                icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
                label = { Text("Settings") }
            )
            
            Spacer(modifier = Modifier.weight(1f))
        }

        Box(modifier = Modifier.weight(1f)) {
            content(PaddingValues(0.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 1000, heightDp = 600)
@Composable
fun TabletScaffoldPreview() {
    com.aesprt.foldgo.ui.theme.FoldGoTheme {
        TabletScaffold(
            currentRoute = DashboardRoute::class.qualifiedName,
            onNavigate = {}
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Content Area")
            }
        }
    }
}
