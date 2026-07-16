package com.aesprt.foldgo.presentation.order.components.tablet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.core.util.DevicePreviews
import com.aesprt.foldgo.core.util.PriceFormatter
import com.aesprt.foldgo.domain.model.AddOn
import com.aesprt.foldgo.domain.model.Customer
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.ServiceItem
import com.aesprt.foldgo.domain.model.enums.DeliveryMethod
import com.aesprt.foldgo.presentation.order.OrderEntryUiState
import com.aesprt.foldgo.presentation.order.components.OrderEntryForm
import com.aesprt.foldgo.ui.theme.FoldGoTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OrderEntryTabletContent(
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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left Pane: Form
            Card(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
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
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onSaveOrder,
                        enabled = !uiState.isSaving && uiState.assignedMachine != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
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
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            // Right Pane: Summary
            Card(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "Order Summary",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(uiState.selectedItems) { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${item.name} — ${item.quantity}${item.unit.lowercase()}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        PriceFormatter.format(item.totalPrice),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            items(uiState.selectedAddOns) { addOn ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Add-on: ${addOn.name}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        PriceFormatter.format(addOn.price),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (uiState.assignedMachine != null) {
                                item {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Assigned Machine",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            uiState.assignedMachine.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Total Amount",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    PriceFormatter.format(uiState.totalAmount),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Button(
                            onClick = onSaveOrder,
                            enabled = !uiState.isSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
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
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        if (uiState.assignedMachine == null) {
                            Text(
                                "A machine must be selected before the order can be created.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
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
                        Text("OK")
                    }
                },
                title = { Text("Error") },
                text = { Text(uiState.error) }
            )
        }
    }
}

@DevicePreviews
@Composable
fun OrderEntryContentTabletPreview() {
    FoldGoTheme {
        OrderEntryTabletContent(
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