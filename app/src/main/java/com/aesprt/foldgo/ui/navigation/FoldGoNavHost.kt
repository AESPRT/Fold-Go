package com.aesprt.foldgo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aesprt.foldgo.presentation.dashboard.DashboardScreen
import com.aesprt.foldgo.presentation.inventory.InventoryScreen
import com.aesprt.foldgo.presentation.machines.AddMachineScreen
import com.aesprt.foldgo.presentation.machines.MachineDetailScreen
import com.aesprt.foldgo.presentation.machines.MachineMatrixScreen
import com.aesprt.foldgo.presentation.onboarding.OnboardingScreen
import com.aesprt.foldgo.presentation.order.OrderDetailScreen
import com.aesprt.foldgo.presentation.order.OrderEntryScreen
import com.aesprt.foldgo.presentation.settings.SettingsScreen
import com.aesprt.foldgo.presentation.shop.ShopRegistrationScreen
import com.aesprt.foldgo.presentation.splash.SplashScreen
import kotlinx.serialization.Serializable

@Serializable
object SplashRoute

@Serializable
object OnboardingRoute

@Serializable
object ShopRegistrationRoute

@Serializable
object DashboardRoute

@Serializable
data class OrderDetailRoute(val orderId: String)

@Serializable
data class MachineDetailRoute(val machineId: String)

@Serializable
object NewOrderRoute

@Serializable
object NewMachineRoute

@Serializable
object MachineMatrixRoute

@Serializable
object InventoryRoute

@Serializable
object SettingsRoute

@Composable
fun FoldGoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        modifier = modifier
    ) {
        composable<SplashRoute> {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(OnboardingRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToRegistration = {
                    navController.navigate(ShopRegistrationRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(DashboardRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<OnboardingRoute> {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(ShopRegistrationRoute) {
                        popUpTo(OnboardingRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<ShopRegistrationRoute> {
            ShopRegistrationScreen(
                onRegistrationSuccess = {
                    navController.navigate(DashboardRoute) {
                        popUpTo(ShopRegistrationRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<DashboardRoute> {
            DashboardScreen(
                onOrderClick = { orderId ->
                    navController.navigate(OrderDetailRoute(orderId))
                },
                onNewOrderClick = {
                    navController.navigate(NewOrderRoute)
                }
            )
        }

        composable<NewOrderRoute> {
            OrderEntryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<NewMachineRoute> {
            AddMachineScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<MachineMatrixRoute> {
            MachineMatrixScreen(
                onAddNewMachine = {
                    navController.navigate(NewMachineRoute)
                },
                onMachineClick = { machineId ->
                    navController.navigate(MachineDetailRoute(machineId))
                }
            )
        }

        composable<MachineDetailRoute> { backStackEntry ->
            val machineId = backStackEntry.arguments?.getString("machineId") ?: ""
            MachineDetailScreen(
                machineId = machineId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<InventoryRoute> {
            InventoryScreen()
        }

        composable<SettingsRoute> {
            SettingsScreen()
        }

        composable<OrderDetailRoute> { backStackEntry ->
            OrderDetailScreen(
                orderId = (backStackEntry.arguments?.getString("orderId") ?: ""),
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
