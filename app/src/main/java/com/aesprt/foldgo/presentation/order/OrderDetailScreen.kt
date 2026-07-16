package com.aesprt.foldgo.presentation.order

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.aesprt.foldgo.presentation.components.FoldGoLoading
import com.aesprt.foldgo.presentation.components.ModernBackground
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.activity.compose.LocalActivity
import com.aesprt.foldgo.domain.model.enums.DeliveryMethod
import com.aesprt.foldgo.presentation.order.components.mobile.OrderDetailContent
import com.aesprt.foldgo.presentation.order.components.tablet.OrderDetailTabletContent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    viewModel: OrderDetailViewModel = koinViewModel { parametersOf(orderId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalActivity.current ?: return
    val windowSizeClass = calculateWindowSizeClass(activity)
    val isTablet = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Order Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .imePadding()
            ) {
                if (uiState.isLoading || uiState.isSendingSms) {
                    FoldGoLoading(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.order != null) {
                    if (isTablet) {
                        OrderDetailTabletContent(
                            order = uiState.order!!,
                            machine = uiState.machine,
                            availableAddOns = uiState.availableAddOns,
                            availableMachines = uiState.availableMachines,
                            onReady = viewModel::updateOrderPaymentAndDelivery,
                            onAssignMachine = viewModel::assignMachine,
                            onDelivered = viewModel::markAsDelivered
                        )
                    } else {
                        OrderDetailContent(
                            order = uiState.order!!,
                            machine = uiState.machine,
                            availableAddOns = uiState.availableAddOns,
                            availableMachines = uiState.availableMachines,
                            onReady = viewModel::updateOrderPaymentAndDelivery,
                            onAssignMachine = viewModel::assignMachine,
                            onDelivered = viewModel::markAsDelivered
                        )
                    }
                } else {
                    Text(
                        "Order not found",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                if (uiState.showSmsPrompt && uiState.order != null) {
                    val order = uiState.order!!
                    val isPickupLabel = when {
                        order.deliveryMethod == DeliveryMethod.PICKUP -> "READY FOR PICKUP"
                        else -> "READY FOR DELIVERY"
                    }
                    val message = "FoldGo ${order.orderNumber}\nAmount: P${order.totalAmount}\nStatus: $isPickupLabel\nPlease bring your claim stub to claim. Thank you!"
                    viewModel.sendSmsAndComplete(message)
                }
            }
        }
    }
}
