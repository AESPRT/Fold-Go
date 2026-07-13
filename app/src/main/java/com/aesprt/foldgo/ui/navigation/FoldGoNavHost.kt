package com.aesprt.foldgo.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aesprt.foldgo.presentation.auth.LoginScreen
import com.aesprt.foldgo.presentation.auth.StaffSelectionScreen
import com.aesprt.foldgo.presentation.dashboard.DashboardScreen
import com.aesprt.foldgo.presentation.history.HistoryScreen
import com.aesprt.foldgo.presentation.machines.AddMachineScreen
import com.aesprt.foldgo.presentation.machines.EquipmentSetupScreen
import com.aesprt.foldgo.presentation.machines.MachineDetailScreen
import com.aesprt.foldgo.presentation.machines.MachineMatrixScreen
import com.aesprt.foldgo.presentation.onboarding.OnboardingScreen
import com.aesprt.foldgo.presentation.order.OrderDetailScreen
import com.aesprt.foldgo.presentation.order.OrderEntryScreen
import com.aesprt.foldgo.presentation.services.ServicesScreen
import com.aesprt.foldgo.presentation.settings.AppearanceScreen
import com.aesprt.foldgo.presentation.settings.NotificationSettingsScreen
import com.aesprt.foldgo.presentation.settings.SMSSettingsScreen
import com.aesprt.foldgo.presentation.settings.SettingsScreen
import com.aesprt.foldgo.presentation.shop.ShopInfoScreen
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
object LoginRoute

@Serializable
object StaffSelectionRoute

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
object HistoryRoute

@Serializable
object SettingsRoute

@Serializable
object ShopInfoRoute

@Serializable
object ServicesRoute

@Serializable
object EquipmentSetupRoute

@Serializable
object SMSRoute

@Serializable
object NotificationsRoute

@Serializable
object AppearanceRoute

@Composable
fun FoldGoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
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
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToStaffSelection = {
                    navController.navigate(StaffSelectionRoute) {
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
                    navController.navigate(LoginRoute) {
                        popUpTo(OnboardingRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(StaffSelectionRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onCreateShop = {
                    navController.navigate(ShopRegistrationRoute)
                }
            )
        }

        composable<StaffSelectionRoute> {
            StaffSelectionScreen(
                onStaffSelected = {
                    navController.navigate(DashboardRoute) {
                        popUpTo(StaffSelectionRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<ShopRegistrationRoute> {
            ShopRegistrationScreen(
                onRegistrationSuccess = {
                    navController.navigate(StaffSelectionRoute) {
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
                },
                contentPadding = contentPadding
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
                onMachineClick = { machineId ->
                    navController.navigate(MachineDetailRoute(machineId))
                },
                contentPadding = contentPadding
            )
        }

        composable<MachineDetailRoute> { backStackEntry ->
            val machineId = backStackEntry.arguments?.getString("machineId") ?: ""
            MachineDetailScreen(
                machineId = machineId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<HistoryRoute> {
            HistoryScreen(
                onOrderClick = { orderId ->
                    navController.navigate(OrderDetailRoute(orderId))
                },
                contentPadding = contentPadding
            )
        }

        composable<SettingsRoute> {
            SettingsScreen(
                onLogout = {
                    navController.navigate(StaffSelectionRoute) {
                        popUpTo(DashboardRoute) { inclusive = true }
                    }
                },
                onNavigateToShopInfo = {
                    navController.navigate(ShopInfoRoute)
                },
                onNavigateToServices = {
                    navController.navigate(ServicesRoute)
                },
                onNavigateToEquipmentSetup = {
                    navController.navigate(EquipmentSetupRoute)
                },
                onNavigateToSMS = {
                    navController.navigate(SMSRoute)
                },
                onNavigateToNotifications = {
                    navController.navigate(NotificationsRoute)
                },
                onNavigateToAppearance = {
                    navController.navigate(AppearanceRoute)
                },
                contentPadding = contentPadding
            )
        }

        composable<ShopInfoRoute> {
            ShopInfoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ServicesRoute> {
            ServicesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<EquipmentSetupRoute> {
            EquipmentSetupScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddMachine = { navController.navigate(NewMachineRoute) }
            )
        }

        composable<SMSRoute> {
            SMSSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<NotificationsRoute> {
            NotificationSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AppearanceRoute> {
            AppearanceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<OrderDetailRoute> { backStackEntry ->
            OrderDetailScreen(
                orderId = (backStackEntry.arguments?.getString("orderId") ?: ""),
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
