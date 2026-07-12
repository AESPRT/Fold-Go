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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aesprt.foldgo.domain.model.Machine
import com.aesprt.foldgo.domain.model.enums.MachineStatus
import com.aesprt.foldgo.domain.model.enums.MachineType
import com.aesprt.foldgo.domain.model.enums.OrderStatus
import com.aesprt.foldgo.domain.model.enums.ServiceType
import com.aesprt.foldgo.presentation.machines.OrderWithBatches
import com.aesprt.foldgo.ui.theme.FoldGoTheme
import com.aesprt.foldgo.ui.theme.IntakeAmber
import com.aesprt.foldgo.ui.theme.SurfaceVariantDark

@Composable
fun MachineStatusDialog(
    machine: Machine,
    activeOrders: List<OrderWithBatches>,
    onDismiss: () -> Unit,
    onStatusChange: (String) -> Unit,
    onStartCycle: (Int, String?, Double?) -> Unit,
    onFinishCycle: () -> Unit
) {
    var showStartCycleConfig by remember { mutableStateOf(false) }
    var selectedOrderId by remember { mutableStateOf<String?>(null) }
    var duration by remember { mutableStateOf("30") }
    var assignedWeight by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (showStartCycleConfig) "Start Cycle: ${machine.name}" else "Update Status: ${machine.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (showStartCycleConfig) {
                StartCycleConfig(
                    machine = machine,
                    activeOrders = activeOrders,
                    selectedOrderId = selectedOrderId,
                    onOrderSelected = { orderId ->
                        selectedOrderId = orderId
                        val orderWithBatches = activeOrders.find { it.order.orderId == orderId }
                        if (orderWithBatches != null) {
                            val totalWeight = orderWithBatches.order.items.sumOf { it.quantity }
                            val hasPendingDry = orderWithBatches.order.status == OrderStatus.WASHED
                            
                            // Calculate weight already assigned to OTHER machines (BUSY batches)
                            // or already finished in a previous washer cycle
                            val weightInAction = when {
                                hasPendingDry -> orderWithBatches.batches?.filter {
                                    it.status == OrderStatus.DRIED
                                }?.sumOf { it.weightKg }
                                else -> orderWithBatches.batches?.filter {
                                    it.status != OrderStatus.READY && it.status != OrderStatus.DELIVERED
                                }?.sumOf { it.weightKg }
                            } ?: 0.0

                            Log.e("adriel-testing", "totalWeight Selected: $totalWeight")
                            Log.e("adriel-testing", "weightInAction Selected: $weightInAction")
                            Log.e("adriel-testing", "hasPendingDry Selected: $hasPendingDry")
                            
                            val remaining = (totalWeight - weightInAction).coerceAtLeast(0.0)
                            assignedWeight = minOf(remaining, machine.capacityKg).toString()
                        }
                    },
                    duration = duration,
                    onDurationChange = { duration = it },
                    assignedWeight = assignedWeight,
                    onWeightChange = { assignedWeight = it }
                )
            } else {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (machine.status == MachineStatus.BUSY) {
                        StatusOption(
                            title = "Finish Cycle",
                            subtitle = "Complete current operation",
                            icon = Icons.Rounded.CheckCircle,
                            color = Color(0xFF4CAF50),
                            onClick = onFinishCycle
                        )
                    } else {
                        StatusOption(
                            title = "Set to Idle",
                            subtitle = "Available for new orders",
                            icon = Icons.Rounded.CheckCircle,
                            color = Color(0xFF4CAF50),
                            onClick = { onStatusChange(MachineStatus.IDLE.name) }
                        )
                        StatusOption(
                            title = "Start Cycle",
                            subtitle = "Begin washing or drying",
                            icon = Icons.Rounded.PlayArrow,
                            color = Color(0xFF03A9F4),
                            onClick = { showStartCycleConfig = true }
                        )
                    }
                    StatusOption(
                        title = "Out of Order",
                        subtitle = "Requires maintenance",
                        icon = Icons.Rounded.Block,
                        color = Color(0xFFF44336),
                        onClick = { onStatusChange(MachineStatus.OUT_OF_ORDER.name) }
                    )
                }
            }
        },
        confirmButton = {
            if (showStartCycleConfig) {
                Button(
                    onClick = { 
                        onStartCycle(
                            duration.toIntOrNull() ?: 30, 
                            selectedOrderId, 
                            assignedWeight.toDoubleOrNull()
                        ) 
                    },
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedOrderId != null
                ) {
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = if (showStartCycleConfig) {
                    { showStartCycleConfig = false }
                } else onDismiss) {
                Text(
                    text = if (showStartCycleConfig) "Back" else "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
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

        // Logic for orders that need more processing in specific machine types
        val validOrders = activeOrders.filter { item ->
            val order = item.order
            val totalWeight = order.items.sumOf { it.quantity }

            val isWashItems = order.items.any { it.type == ServiceType.WASH }
            val isDryItems = order.items.any { it.type == ServiceType.DRY }
            val isWashDryItems = order.items.any { it.type == ServiceType.WASH_DRY }
            val isIronItems = order.items.any { it.type == ServiceType.IRON }

            when (machine.type) {
                MachineType.WASHER_DRYER -> {
                    val washedDriedWeight = item.batches?.filter {
                        it.status in listOf(OrderStatus.WASHED_AND_DRIED, OrderStatus.IRONING, OrderStatus.IRONED, OrderStatus.FOLDING, OrderStatus.READY)
                    }?.sumOf { it.weightKg } ?: 0.0

                    val washingDryingWeight = item.batches?.filter { it.status == OrderStatus.WASHING_AND_DRYING }?.sumOf { it.weightKg } ?: 0.0

                    isWashDryItems && (washedDriedWeight + washingDryingWeight) < (totalWeight - 0.01) && order.status != OrderStatus.FOLDING && order.status != OrderStatus.WASHED
                }
                MachineType.WASHER -> {
                    // Check if there is still weight that hasn't finished washing
                    val washedWeight = item.batches?.filter {
                        it.status in listOf(OrderStatus.WASHED_AND_DRIED, OrderStatus.WASHED, OrderStatus.DRYING, OrderStatus.DRIED, OrderStatus.IRONING, OrderStatus.IRONED, OrderStatus.FOLDING, OrderStatus.READY)
                    }?.sumOf { it.weightKg } ?: 0.0
                    
                    val washingWeight = item.batches?.filter { it.status == OrderStatus.WASHING || it.status == OrderStatus.WASHING_AND_DRYING }?.sumOf { it.weightKg } ?: 0.0

                    (isWashItems || isWashDryItems) && (washedWeight + washingWeight) < (totalWeight - 0.01) && order.status != OrderStatus.FOLDING && order.status != OrderStatus.WASHED
                }
                MachineType.DRYER -> {
                    val washedWeight = item.batches?.filter {
                        it.status in listOf(OrderStatus.WASHED_AND_DRIED, OrderStatus.WASHED)
                    }?.sumOf { it.weightKg } ?: 0.0
                    
                    val driedWeight = item.batches?.filter {
                        it.status in listOf(OrderStatus.DRIED, OrderStatus.IRONING, OrderStatus.IRONED)
                    }?.sumOf { it.weightKg } ?: 0.0

                    val dryingWeight = item.batches?.filter { it.status == OrderStatus.DRYING || it.status == OrderStatus.WASHING_AND_DRYING }?.sumOf { it.weightKg } ?: 0.0

                    // Fix for Single-Cycle and Dry Only: 
                    // 1. If it's "Dry Only", show if not yet dried.
                    // 2. If it's "Wash & Dry", show if some weight is washed but not yet dried.
                    val isDryOnly = order.items.all { it.type == ServiceType.DRY }
                    val needsDrying = (isDryOnly && (driedWeight + dryingWeight) < (totalWeight - 0.01)) ||
                                     ((isDryItems || isWashDryItems) && washedWeight > 0 && (driedWeight + dryingWeight) < (washedWeight - 0.01))
                    
                    // ALSO show if the order status is already WASHED (from single cycle or split)
                    val isWaitingForDryer = (order.status == OrderStatus.WASHED || order.status == OrderStatus.WASHED_AND_DRIED && (isDryItems || isWashDryItems))

                    needsDrying || isWaitingForDryer
                }
                else -> {
                    // Ironing/Steaming
                    val driedWeight = item.batches?.filter {
                        it.status in listOf(OrderStatus.WASHED_AND_DRIED, OrderStatus.DRIED, OrderStatus.IRONING, OrderStatus.IRONED, OrderStatus.FOLDING, OrderStatus.READY)
                    }?.sumOf { it.weightKg } ?: 0.0
                    
                    val ironedWeight = item.batches?.filter {
                        it.status in listOf(OrderStatus.IRONED, OrderStatus.FOLDING, OrderStatus.READY)
                    }?.sumOf { it.weightKg } ?: 0.0
                    
                    val ironingWeight = item.batches?.filter { it.status == OrderStatus.IRONING }?.sumOf { it.weightKg } ?: 0.0

                    isIronItems || driedWeight > 0 && (ironedWeight + ironingWeight) < totalWeight
                }
            }
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
                                
                                val statusSummary = when (machine.type) {
                                    MachineType.WASHER_DRYER -> {
                                        val washedAndDried = item.batches?.filter {
                                            it.status in listOf(OrderStatus.WASHED_AND_DRIED, OrderStatus.DRYING, OrderStatus.DRIED, OrderStatus.IRONING, OrderStatus.IRONED, OrderStatus.FOLDING, OrderStatus.READY)
                                        }?.sumOf { it.weightKg } ?: item.order.items.sumOf { it.quantity }
                                        "Pending Wash & Dry: ${totalWeight - washedAndDried}kg"
                                    }
                                    MachineType.WASHER -> {
                                        val washed = item.batches?.filter {
                                            it.status in listOf(OrderStatus.WASHED, OrderStatus.DRYING, OrderStatus.DRIED, OrderStatus.IRONING, OrderStatus.IRONED, OrderStatus.FOLDING, OrderStatus.READY)
                                        }?.sumOf { it.weightKg } ?: item.order.items.sumOf { it.quantity }
                                        "Pending Wash: ${totalWeight - washed}kg"
                                    }
                                    MachineType.DRYER -> {
                                        val washed = when {
                                            item.order.status == OrderStatus.WASHED -> item.order.items.sumOf { it.quantity }
                                            else -> item.batches?.filter {
                                                it.status in listOf(OrderStatus.WASHED, OrderStatus.IRONING, OrderStatus.IRONED, OrderStatus.FOLDING, OrderStatus.READY)
                                            }?.sumOf { it.weightKg } ?: 0.0
                                        }
                                        val dried = item.batches?.filter {
                                            it.status in listOf(OrderStatus.DRIED, OrderStatus.IRONING, OrderStatus.IRONED, OrderStatus.FOLDING, OrderStatus.READY)
                                        }?.sumOf { it.weightKg } ?: item.order.items.sumOf { it.quantity }
                                        Log.e("adriel-testing", "Dryer: $washed, $dried")

                                        "Washed: ${washed}kg • Pending Dry: ${washed - dried}kg"
                                    }
                                    else -> "Ready for processing"
                                }
                                
                                Text(
                                    statusSummary,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
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

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MachineStatusDialogPreview() {
    FoldGoTheme {
        MachineStatusDialog(
            machine = Machine("1", "shop1", "Washer 01", MachineType.WASHER, 8.0, MachineStatus.IDLE, 0L),
            activeOrders = emptyList(),
            onDismiss = {},
            onStatusChange = {},
            onStartCycle = { _, _, _ -> },
            onFinishCycle = {}
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
            machine = Machine("1", "shop1", "Washer 01", MachineType.WASHER, 8.0, MachineStatus.IDLE, 0L),
            activeOrders = emptyList(),
            selectedOrderId = null,
            onOrderSelected = {},
            duration = "30",
            onDurationChange = {},
            assignedWeight = "0",
            onWeightChange = {}
        )
    }
}