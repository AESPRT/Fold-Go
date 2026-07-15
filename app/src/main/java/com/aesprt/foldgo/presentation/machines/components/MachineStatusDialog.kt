package com.aesprt.foldgo.presentation.machines.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.enums.BatchStatus
import com.aesprt.foldgo.domain.model.enums.MachineStatus
import com.aesprt.foldgo.domain.model.enums.OrderStatus
import com.aesprt.foldgo.domain.model.enums.ServiceType
import com.aesprt.foldgo.presentation.machines.OrderWithBatches
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import com.aesprt.foldgo.ui.theme.IntakeAmber
import com.aesprt.foldgo.ui.theme.SurfaceVariantDark

@Composable
fun MachineStatusDialog(
    machine: Machine,
    onDismiss: () -> Unit,
    onStatusChange: (MachineStatus) -> Unit,
    onStartCycle: () -> Unit
) {
    val hasAssignedOrder = machine.assignedOrderId != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Update Status: ${machine.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!hasAssignedOrder) {
                    Surface(
                        color = IntakeAmber.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Rounded.Warning, contentDescription = null, tint = IntakeAmber)
                            Text(
                                "No order assigned to this machine. Create a new order and assign it here before starting a cycle.",
                                style = MaterialTheme.typography.bodySmall,
                                color = IntakeAmber
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onStartCycle,
                        modifier = Modifier.weight(1f),
                        enabled = hasAssignedOrder,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White,
                            disabledContainerColor = Color.LightGray,
                            disabledContentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start Cycle", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onStatusChange(MachineStatus.IDLE) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Idle", fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                OutlinedButton(
                    onClick = { onStatusChange(MachineStatus.OUT_OF_ORDER) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFF44336)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(Color(0xFFF44336))
                    )
                ) {
                    Text("Mark Out of Order", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartCycleConfig(
    machine: Machine,
    activeOrders: List<OrderWithBatches>,
    selectedOrderId: String?,
    onOrderSelected: (String?) -> Unit,
    selectedServiceType: ServiceType?,
    onServiceTypeSelected: (ServiceType) -> Unit,
    duration: String,
    onDurationChange: (String) -> Unit,
    assignedWeight: String,
    onWeightChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = duration,
            onValueChange = onDurationChange,
            label = { Text("Duration (minutes)", style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        var expanded by remember { mutableStateOf(false) }
        val selectedOrderWithBatches = activeOrders.find { it.order.orderId == selectedOrderId }

        // Logic for orders that need more processing
        val validOrders = activeOrders.filter { item ->
            val order = item.order
            val totalWeight = order.items.sumOf { it.quantity }

            val isWashItems = order.items.any { it.type == ServiceType.WASH }
            val isDryItems = order.items.any { it.type == ServiceType.DRY }
            val isWashDryItems = order.items.any { it.type == ServiceType.WASH_DRY }

            val washedWeight = item.batches?.filter {
                it.status == BatchStatus.READY 
            }?.sumOf { it.weightKg } ?: 0.0

            washedWeight < totalWeight
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedOrderWithBatches?.order?.orderNumber ?: "Select Order (Required)",
                onValueChange = {},
                readOnly = true,
                label = { Text("Associated Order", style = MaterialTheme.typography.bodyMedium) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                        enabled = true
                    )
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = selectedOrderId == null
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                validOrders.forEach { item ->
                    val totalWeight = item.order.items.sumOf { it.quantity }
                    val serviceSummary = item.order.items.map { it.type.name }.distinct().joinToString(", ")
                    
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    "${item.order.orderNumber} - ${item.order.customerName}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Services: $serviceSummary • Total: ${totalWeight}kg",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onOrderSelected(item.order.orderId)
                            expanded = false
                        }
                    )
                }

                if (validOrders.isEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "No pending orders for this machine",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        },
                        onClick = { expanded = false },
                        enabled = false
                    )
                }
            }
        }

        if (selectedOrderId != null && selectedOrderWithBatches != null) {
            val serviceTypes = selectedOrderWithBatches.order.items.map { it.type }.distinct()
            var serviceExpanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = serviceExpanded,
                onExpandedChange = { serviceExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedServiceType?.name ?: "Select Service Item (Required)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Service Type", style = MaterialTheme.typography.bodyMedium) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceExpanded) },
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                            enabled = true
                        )
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = selectedServiceType == null
                )
                ExposedDropdownMenu(
                    expanded = serviceExpanded,
                    onDismissRequest = { serviceExpanded = false }
                ) {
                    serviceTypes.forEach { type ->
                        val typeWeight = selectedOrderWithBatches.order.items.filter { it.type == type }.sumOf { it.quantity }
                        DropdownMenuItem(
                            text = { 
                                Text("${type.name} (${typeWeight}kg)") 
                            },
                            onClick = {
                                onServiceTypeSelected(type)
                                serviceExpanded = false
                            }
                        )
                    }
                }
            }

            val totalOrderWeight = selectedOrderWithBatches.order.items.sumOf { it.quantity }
            val weightAlreadyAssigned = selectedOrderWithBatches.batches?.sumOf { it.weightKg } ?: 0.0
            val currentTenderedWeight = assignedWeight.toDoubleOrNull() ?: 0.0
            val totalRemainingBeforeThisCycle = totalOrderWeight - weightAlreadyAssigned
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Assign Weight (Capacity: ${machine.capacityKg}kg)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = assignedWeight,
                    onValueChange = onWeightChange,
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    suffix = { Text("kg") }
                )
                
                if (totalRemainingBeforeThisCycle > currentTenderedWeight) {
                    val remainingAfterThisCycle = (totalRemainingBeforeThisCycle - currentTenderedWeight).coerceAtLeast(0.0)
                    Surface(
                        color = SurfaceVariantDark.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Remaining weight to process: ${remainingAfterThisCycle}kg",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp),
                            color = IntakeAmber
                        )
                    }
                }

                if (totalRemainingBeforeThisCycle > machine.capacityKg) {
                    Surface(
                        color = SurfaceVariantDark.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Remaining weight is ${totalRemainingBeforeThisCycle}kg. It will be split into multiple batches.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp),
                            color = IntakeAmber
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = color,
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color.White)
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun updateAssignedWeight(
    orderWithBatches: OrderWithBatches,
    serviceType: ServiceType,
    machine: Machine,
    onWeightCalculated: (String) -> Unit
) {
    val totalWeightForType = orderWithBatches.order.items
        .filter { it.type == serviceType }
        .sumOf { it.quantity }

    val weightInAction = orderWithBatches.batches?.filter {
        it.serviceType == serviceType && it.status != BatchStatus.READY
    }?.sumOf { it.weightKg } ?: 0.0

    val remaining = (totalWeightForType - weightInAction).coerceAtLeast(0.0)
    onWeightCalculated(minOf(remaining, machine.capacityKg).toString())
}

@Preview(showBackground = true)
@Composable
fun MachineStatusDialogPreview() {
    FoldGoTheme {
        MachineStatusDialog(
            machine = Machine("1", "shop1", "Washer 01", 8.0, MachineStatus.IDLE, 0L),
            onDismiss = {},
            onStatusChange = {},
            onStartCycle = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatusOptionPreview() {
    FoldGoTheme {
        StatusOption(
            title = "Set to Idle",
            subtitle = "Available for new orders",
            icon = Icons.Rounded.CheckCircle,
            color = Color(0xFF4CAF50),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StartCycleConfigPreview() {
    FoldGoTheme {
        StartCycleConfig(
            machine = Machine("1", "shop1", "Washer 01", 8.0, MachineStatus.IDLE, 0L),
            activeOrders = emptyList(),
            selectedOrderId = null,
            onOrderSelected = {},
            selectedServiceType = null,
            onServiceTypeSelected = {},
            duration = "30",
            onDurationChange = {},
            assignedWeight = "0",
            onWeightChange = {}
        )
    }
}
