package com.aesprt.foldgo.presentation.order

import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.activity.compose.LocalActivity
import org.koin.androidx.compose.koinViewModel
import com.aesprt.foldgo.presentation.order.components.mobile.OrderEntryContent
import com.aesprt.foldgo.presentation.order.components.tablet.OrderEntryTabletContent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun OrderEntryScreen(
    onNavigateBack: () -> Unit,
    viewModel: OrderEntryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val activity = LocalActivity.current ?: return
    val windowSizeClass = calculateWindowSizeClass(activity)
    val isTablet = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    if (isTablet) {
        OrderEntryTabletContent(
            uiState = uiState,
            onNavigateBack = onNavigateBack,
            onCustomerNameChange = viewModel::onCustomerNameChange,
            onPhoneNumberChange = viewModel::onPhoneNumberChange,
            onCustomerAddressChange = viewModel::onCustomerAddressChange,
            onDeliveryMethodChange = viewModel::onDeliveryMethodChange,
            onDeliveryFeeChange = viewModel::onDeliveryFeeChange,
            onToggleService = viewModel::toggleService,
            onWeightChange = viewModel::onWeightChange,
            onToggleAddOn = viewModel::toggleAddOn,
            onAssignMachine = viewModel::assignMachine,
            onCustomerSelect = viewModel::selectCustomer,
            onSaveOrder = viewModel::saveOrder,
            onClearError = viewModel::clearError
        )
    } else {
        OrderEntryContent(
            uiState = uiState,
            onNavigateBack = onNavigateBack,
            onCustomerNameChange = viewModel::onCustomerNameChange,
            onPhoneNumberChange = viewModel::onPhoneNumberChange,
            onCustomerAddressChange = viewModel::onCustomerAddressChange,
            onDeliveryMethodChange = viewModel::onDeliveryMethodChange,
            onDeliveryFeeChange = viewModel::onDeliveryFeeChange,
            onToggleService = viewModel::toggleService,
            onWeightChange = viewModel::onWeightChange,
            onToggleAddOn = viewModel::toggleAddOn,
            onAssignMachine = viewModel::assignMachine,
            onCustomerSelect = viewModel::selectCustomer,
            onSaveOrder = viewModel::saveOrder,
            onClearError = viewModel::clearError
        )
    }
}
