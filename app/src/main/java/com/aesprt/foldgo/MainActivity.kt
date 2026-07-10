package com.aesprt.foldgo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aesprt.foldgo.presentation.components.FoldGoBottomBar
import com.aesprt.foldgo.ui.navigation.*
import com.aesprt.foldgo.ui.theme.FoldGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoldGoTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination?.route

                // Only show bottom bar for primary menu routes
                val menuRoutes = listOf(
                    DashboardRoute::class.qualifiedName,
                    MachineMatrixRoute::class.qualifiedName,
                    HistoryRoute::class.qualifiedName,
                    SettingsRoute::class.qualifiedName
                )
                
                val showBottomBar = currentDestination != null && menuRoutes.any { 
                    currentDestination.contains(it ?: "") 
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    bottomBar = {
                        if (showBottomBar) {
                            FoldGoBottomBar(
                                currentRoute = currentDestination,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    // Navigation Host handles its own background internally.
                    // We only apply the bottom padding to avoid content being hidden by the bottom bar.
                    // The top padding is handled by the screens' TopAppBar to ensure the background
                    // extends behind the status bar.
                    FoldGoNavHost(
                        navController = navController,
                        modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                    )
                }
            }
        }
    }
}
