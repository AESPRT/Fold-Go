package com.aesprt.foldgo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aesprt.foldgo.presentation.dashboard.DashboardScreen
import kotlinx.serialization.Serializable

@Serializable
object DashboardRoute

@Serializable
data class OrderDetailRoute(val orderId: String)

@Serializable
object NewOrderRoute

@Composable
fun FoldGoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = DashboardRoute,
        modifier = modifier
    ) {
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
            // TODO: Implement NewOrderScreen
        }
        
        composable<OrderDetailRoute> {
            // TODO: Implement OrderDetailScreen
        }
    }
}
