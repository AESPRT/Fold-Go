package com.aesprt.foldgo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aesprt.foldgo.presentation.components.FoldGoBottomBar
import com.aesprt.foldgo.ui.navigation.*
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.presentation.components.TabletScaffold
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val notificationOrderId = MutableStateFlow<String?>(null)
    private val preferenceManager: PreferenceManager by inject()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean ->
        // Handle permission result if needed
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra("orderId")?.let {
            notificationOrderId.value = it
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        intent.getStringExtra("orderId")?.let {
            notificationOrderId.value = it
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val isDarkMode by preferenceManager.isDarkModeEnabled.collectAsState(initial = false)

            FoldGoTheme(darkTheme = isDarkMode) {
                val windowSizeClass = calculateWindowSizeClass(this)
                val isTablet = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination?.route

                val orderIdToNavigate by notificationOrderId.collectAsState()

                LaunchedEffect(orderIdToNavigate) {
                    orderIdToNavigate?.let { id ->
                        navController.navigate(OrderDetailRoute(id)) {
                            // Ensure we don't have multiple copies of order details on stack
                            launchSingleTop = true
                        }
                        notificationOrderId.value = null
                    }
                }

                // Only show bottom bar for primary menu routes
                val menuRoutes = listOf(
                    DashboardRoute::class.qualifiedName,
                    MachineMatrixRoute::class.qualifiedName,
                    HistoryRoute::class.qualifiedName,
                    SettingsRoute::class.qualifiedName
                )
                
                val showNavBars = currentDestination != null && menuRoutes.any { 
                    currentDestination.contains(it ?: "") 
                }

                if (isTablet && showNavBars) {
                    TabletScaffold(
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
                    ) { innerPadding ->
                        FoldGoNavHost(
                            navController = navController,
                            widthSizeClass = windowSizeClass.widthSizeClass,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = innerPadding
                        )
                    }
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        bottomBar = {
                            if (showNavBars) {
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
                        // Pass innerPadding to NavHost so screens can respect bottom bar height
                        FoldGoNavHost(
                            navController = navController,
                            widthSizeClass = windowSizeClass.widthSizeClass,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = innerPadding
                        )
                    }
                }
            }
        }
    }
}
