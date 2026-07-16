package com.aesprt.foldgo.presentation.order.components.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.core.util.PriceFormatter
import com.aesprt.foldgo.domain.model.AddOn
import com.aesprt.foldgo.domain.model.Customer
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.ServiceItem
import com.aesprt.foldgo.domain.model.enums.DeliveryMethod
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.presentation.order.OrderEntryUiState
import com.aesprt.foldgo.presentation.order.components.OrderEntryForm
import com.aesprt.foldgo.ui.theme.FoldGoTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OrderEntryContent(
    uiState: OrderEntryUiState,
    onNavigateBack: () -> Unit,
    onCustomerNameChange: (String) -> Unit,
    onPhoneNumberChange: (TextFieldValue) -> Unit,
    onCustomerAddressChange: (String) -> Unit,
    onDeliveryMethodChange: (DeliveryMethod) -> Unit,
    onDeliveryFeeChange: (String) -> Unit,
    onToggleService: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onToggleAddOn: (AddOn) -> Unit,
    onAssignMachine: (Machine) -> Unit,
    onCustomerSelect: (Customer) -> Unit,
    onSaveOrder: () -> Unit,
    onClearError: () -> Unit
) {
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    ModernBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "New Order",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            OrderEntryForm(
                                uiState = uiState,
                                onCustomerNameChange = onCustomerNameChange,
                                onPhoneNumberChange = onPhoneNumberChange,
                                onCustomerAddressChange = onCustomerAddressChange,
                                onDeliveryMethodChange = onDeliveryMethodChange,
                                onDeliveryFeeChange = onDeliveryFeeChange,
                                onToggleService = onToggleService,
                                onWeightChange = onWeightChange,
                                onToggleAddOn = onToggleAddOn,
                                onAssignMachine = onAssignMachine,
                                onCustomerSelect = onCustomerSelect
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Total & Save
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Amount", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    PriceFormatter.format(uiState.totalAmount),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Button(
                                onClick = onSaveOrder,
                                enabled = !uiState.isSaving,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (uiState.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text(
                                        "Create Order",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.error != null) {
                    AlertDialog(
                        onDismissRequest = onClearError,
                        confirmButton = {
                            TextButton(onClick = onClearError) {
                                Text(
                                    "OK",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        },
                        title = { Text("Error", style = MaterialTheme.typography.titleLarge) },
                        text = { Text(uiState.error, style = MaterialTheme.typography.bodyMedium) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrderEntryContentPreview() {
    FoldGoTheme {
        OrderEntryContent(
            uiState = OrderEntryUiState(
                customerName = "Juan Dela Cruz",
                phoneNumber = "09123456789",
                selectedItems = listOf(
                    ServiceItem("Wash & Dry", 5.0, "KG", 65.0, 325.0),
                    ServiceItem("Curtains", 2.0, "PCS", 120.0, 240.0)
                )
            ),
            onNavigateBack = {},
            onCustomerNameChange = {},
            onPhoneNumberChange = {},
            onCustomerAddressChange = {},
            onDeliveryMethodChange = {},
            onDeliveryFeeChange = {},
            onToggleService = {},
            onWeightChange = {},
            onToggleAddOn = {},
            onAssignMachine = {},
            onSaveOrder = {},
            onCustomerSelect = { _ -> },
            onClearError = {}
        )
    }
}