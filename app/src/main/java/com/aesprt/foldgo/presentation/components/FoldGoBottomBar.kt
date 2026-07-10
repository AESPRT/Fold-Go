package com.aesprt.foldgo.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.ui.navigation.DashboardRoute
import com.aesprt.foldgo.ui.navigation.HistoryRoute
import com.aesprt.foldgo.ui.navigation.MachineMatrixRoute
import com.aesprt.foldgo.ui.navigation.SettingsRoute

sealed class BottomNavItem(
    val route: Any,
    val title: String,
    val icon: ImageVector,
    val routeName: String
) {
    object Dashboard : BottomNavItem(DashboardRoute, "Dashboard", Icons.Rounded.Dashboard, "DashboardRoute")
    object Machines : BottomNavItem(MachineMatrixRoute, "Machines", Icons.Rounded.LocalLaundryService, "MachineMatrixRoute")
    object History : BottomNavItem(HistoryRoute, "History", Icons.Rounded.History, "HistoryRoute")
    object Settings : BottomNavItem(SettingsRoute, "Settings", Icons.Rounded.Settings, "SettingsRoute")
}

@Composable
fun FoldGoBottomBar(
    currentRoute: String?,
    onNavigate: (Any) -> Unit
) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Machines,
        BottomNavItem.History,
        BottomNavItem.Settings
    )

    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding()
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                val isSelected = currentRoute?.contains(item.routeName, ignoreCase = true) == true
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onNavigate(item.route) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                )
            }
        }
    }
}
