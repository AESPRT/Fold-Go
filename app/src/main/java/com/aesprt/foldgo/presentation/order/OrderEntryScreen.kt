package com.aesprt.foldgo.presentation.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.activity.compose.LocalActivity
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.material.icons.filled.Check
import com.aesprt.foldgo.core.util.PriceFormatter
import com.aesprt.foldgo.domain.model.AddOn
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.ServiceItem
import com.aesprt.foldgo.domain.model.enums.DeliveryMethod
import com.aesprt.foldgo.presentation.components.ModernBackground
import com.aesprt.foldgo.presentation.order.components.ServiceAddDialog
import com.aesprt.foldgo.core.util.DevicePreviews
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun OrderEntryScreen(
    onNavigateBack: () -> Unit,
    viewModel: OrderEntryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val activity = LocalActivity.current ?: return
    val windowSizeClass = calculateWindowSizeClass(activity)
    val isTablet = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    OrderEntryContent(
        uiState = uiState,
        isTablet = isTablet,
        onNavigateBack = onNavigateBack,
        onCustomerNameChange = viewModel::onCustomerNameChange,
        onPhoneNumberChange = viewModel::onPhoneNumberChange,
        onCustomerAddressChange = viewModel::onCustomerAddressChange,
        onDeliveryMethodChange = viewModel::onDeliveryMethodChange,
        onToggleService = viewModel::toggleService,
        onWeightChange = viewModel::onWeightChange,
        onToggleAddOn = viewModel::toggleAddOn,
        onAssignMachine = viewModel::assignMachine,
        onSaveOrder = viewModel::saveOrder,
        onClearError = viewModel::clearError
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
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OrderEntryContent(
    uiState: OrderEntryUiState,
    isTablet: Boolean,
    onNavigateBack: () -> Unit,
    onCustomerNameChange: (String) -> Unit,
    onPhoneNumberChange: (TextFieldValue) -> Unit,
    onCustomerAddressChange: (String) -> Unit,
    onDeliveryMethodChange: (DeliveryMethod) -> Unit,
    onToggleService: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onToggleAddOn: (AddOn) -> Unit,
    onAssignMachine: (Machine) -> Unit,
    onSaveOrder: () -> Unit,
    onClearError: () -> Unit
) {
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    ModernBackground {
        if (isTablet) {
            OrderEntryTabletContent(
                uiState = uiState,
                onNavigateBack = onNavigateBack,
                onCustomerNameChange = onCustomerNameChange,
                onPhoneNumberChange = onPhoneNumberChange,
                onCustomerAddressChange = onCustomerAddressChange,
                onDeliveryMethodChange = onDeliveryMethodChange,
                onToggleService = onToggleService,
                onWeightChange = onWeightChange,
                onToggleAddOn = onToggleAddOn,
                onAssignMachine = onAssignMachine,
                onSaveOrder = onSaveOrder,
                onClearError = onClearError
            )
        } else {
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
                                    onToggleService = onToggleService,
                                    onWeightChange = onWeightChange,
                                    onToggleAddOn = onToggleAddOn,
                                    onAssignMachine = onAssignMachine
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
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OrderEntryTabletContent(
    uiState: OrderEntryUiState,
    onNavigateBack: () -> Unit,
    onCustomerNameChange: (String) -> Unit,
    onPhoneNumberChange: (TextFieldValue) -> Unit,
    onCustomerAddressChange: (String) -> Unit,
    onDeliveryMethodChange: (DeliveryMethod) -> Unit,
    onToggleService: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onToggleAddOn: (AddOn) -> Unit,
    onAssignMachine: (Machine) -> Unit,
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        OrderEntryForm(
                            uiState = uiState,
                            onCustomerNameChange = onCustomerNameChange,
                            onPhoneNumberChange = onPhoneNumberChange,
                            onCustomerAddressChange = onCustomerAddressChange,
                            onDeliveryMethodChange = onDeliveryMethodChange,
                            onToggleService = onToggleService,
                            onWeightChange = onWeightChange,
                            onToggleAddOn = onToggleAddOn,
                            onAssignMachine = onAssignMachine
                        )
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OrderEntryForm(
    uiState: OrderEntryUiState,
    onCustomerNameChange: (String) -> Unit,
    onPhoneNumberChange: (TextFieldValue) -> Unit,
    onCustomerAddressChange: (String) -> Unit,
    onDeliveryMethodChange: (DeliveryMethod) -> Unit,
    onToggleService: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onToggleAddOn: (AddOn) -> Unit,
    onAssignMachine: (Machine) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(
            "Customer Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = uiState.customerName,
            onValueChange = onCustomerNameChange,
            label = { Text("Customer Name") },
            placeholder = { Text("e.g. Maria Santos") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = uiState.phoneTextFieldValue,
            onValueChange = onPhoneNumberChange,
            label = { Text("Phone Number") },
            placeholder = { Text("e.g. 09171234567") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            )
        )

        OutlinedTextField(
            value = uiState.customerAddress,
            onValueChange = onCustomerAddressChange,
            label = { Text("Customer Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Text(
            "Delivery Method",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DeliveryMethod.entries.forEach { method ->
                FilterChip(
                    selected = uiState.deliveryMethod == method,
                    onClick = { onDeliveryMethodChange(method) },
                    label = {
                        Text(method.name.lowercase().replaceFirstChar { it.uppercase() })
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Text(
            "Service Items",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.availableServices.forEach { service ->
                val isSelected = uiState.selectedServiceIds.contains(service.serviceId)
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggleService(service.serviceId) },
                    label = { Text(service.name) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        if (uiState.selectedServiceIds.isNotEmpty()) {
            OutlinedTextField(
                value = uiState.weight,
                onValueChange = onWeightChange,
                label = { Text("Weight (KG)") },
                modifier = Modifier.width(150.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = { Text("kg", modifier = Modifier.padding(end = 12.dp)) }
            )
        }

        Text(
            "Add-Ons",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.availableAddOns.forEach { addOn ->
                val isSelected = uiState.selectedAddOns.contains(addOn)
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggleAddOn(addOn) },
                    label = { Text("${addOn.name} +P${addOn.price}") },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE8F5E9),
                        selectedLabelColor = Color(0xFF2E7D32),
                        selectedLeadingIconColor = Color(0xFF2E7D32)
                    )
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                "Assign Machine",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Select an idle washer/dryer",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (uiState.availableMachines.isEmpty()) {
            Text(
                "No idle machines available. Please wait for a machine to become free.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.availableMachines.forEach { machine ->
                    val isSelected = uiState.assignedMachine?.machineId == machine.machineId
                    Card(
                        onClick = { onAssignMachine(machine) },
                        modifier = Modifier
                            .width(180.dp)
                            .height(80.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.LocalLaundryService,
                                    contentDescription = null,
                                    modifier = Modifier.padding(6.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column {
                                Text(
                                    machine.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Surface(
                                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "IDLE",
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@DevicePreviews
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
            isTablet = false,
            onNavigateBack = {},
            onCustomerNameChange = {},
            onPhoneNumberChange = {},
            onCustomerAddressChange = {},
            onDeliveryMethodChange = {},
            onToggleService = {},
            onWeightChange = {},
            onToggleAddOn = {},
            onAssignMachine = {},
            onSaveOrder = {},
            onClearError = {}
        )
    }
}
