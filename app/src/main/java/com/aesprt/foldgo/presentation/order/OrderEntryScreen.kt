package com.aesprt.foldgo.presentation.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue
import com.aesprt.foldgo.R
import com.aesprt.foldgo.core.util.PriceFormatter
import com.aesprt.foldgo.domain.model.Service
import com.aesprt.foldgo.domain.model.ServiceItem
import com.aesprt.foldgo.domain.model.enums.DeliveryMethod
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.presentation.order.components.QuantityPromptDialog
import com.aesprt.foldgo.presentation.order.components.ServiceAddDialog
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderEntryScreen(
    onNavigateBack: () -> Unit,
    viewModel: OrderEntryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var serviceToPrompt by remember { mutableStateOf<Service?>(null) }

    OrderEntryContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onCustomerNameChange = viewModel::onCustomerNameChange,
        onPhoneNumberChange = viewModel::onPhoneNumberChange,
        onCustomerAddressChange = viewModel::onCustomerAddressChange,
        onDeliveryMethodChange = viewModel::onDeliveryMethodChange,
        onRemoveItem = viewModel::removeItem,
        onSaveOrder = viewModel::saveOrder,
        onClearError = viewModel::clearError,
        onPromptService = { serviceToPrompt = it }
    )

    if (showAddDialog) {
        ServiceAddDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, qty, unit, price, type, saveAsPreset ->
                viewModel.addItem(name, qty, unit, price, type)
                if (saveAsPreset) {
                    viewModel.addPredefinedService(name, qty, unit, price, type)
                }
            }
        )
    }

    if (serviceToPrompt != null) {
        QuantityPromptDialog(
            service = serviceToPrompt!!,
            onDismiss = { serviceToPrompt = null },
            onConfirm = { qty ->
                viewModel.addItem(
                    serviceToPrompt!!.name,
                    qty,
                    serviceToPrompt!!.unit,
                    serviceToPrompt!!.pricePerUnit,
                    serviceToPrompt!!.type
                )
                serviceToPrompt = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OrderEntryContent(
    uiState: OrderEntryUiState,
    onNavigateBack: () -> Unit,
    onCustomerNameChange: (String) -> Unit,
    onPhoneNumberChange: (TextFieldValue) -> Unit,
    onCustomerAddressChange: (String) -> Unit,
    onDeliveryMethodChange: (DeliveryMethod) -> Unit,
    onRemoveItem: (ServiceItem) -> Unit,
    onSaveOrder: () -> Unit,
    onClearError: () -> Unit,
    onPromptService: (Service) -> Unit
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
                    // Customer Info Section
                    OutlinedTextField(
                        value = uiState.customerName,
                        onValueChange = onCustomerNameChange,
                        label = {
                            Text(
                                "Customer Name",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.phoneTextFieldValue,
                        onValueChange = onPhoneNumberChange,
                        label = {
                            Text(
                                "Phone Number",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                        ),
                        placeholder = { Text(stringResource(R.string.phone_number_sample), style = MaterialTheme.typography.bodySmall) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.customerAddress,
                        onValueChange = onCustomerAddressChange,
                        label = {
                            Text(
                                "Customer Address",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Delivery Method",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DeliveryMethod.entries.forEach { method ->
                            FilterChip(
                                selected = uiState.deliveryMethod == method,
                                onClick = { onDeliveryMethodChange(method) },
                                label = {
                                    Text(
                                        method.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Service Items",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Dynamic Quick Add Presets
                    FlowRow(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.availableServices.forEach { service ->
                            FilterChip(
                                selected = false,
                                onClick = { onPromptService(service) },
                                label = {
                                    Text(
                                        service.name,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Items List
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.selectedItems) { item ->
                            ItemRow(item, onDelete = { onRemoveItem(item) })
                        }
                    }

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

@Composable
fun ItemRow(item: ServiceItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${item.quantity} ${item.unit} x ${PriceFormatter.format(item.pricePerUnit)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                PriceFormatter.format(item.totalPrice),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
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
            onRemoveItem = {},
            onSaveOrder = {},
            onClearError = {},
            onPromptService = {}
        )
    }
}
